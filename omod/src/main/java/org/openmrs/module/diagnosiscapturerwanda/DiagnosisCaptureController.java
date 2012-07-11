/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.diagnosiscapturerwanda;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.ConceptSearchResult;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.api.context.Context;
import org.openmrs.module.diagnosiscapturerwanda.util.DiagnosisUtil;
import org.openmrs.util.OpenmrsConstants;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class DiagnosisCaptureController {
	
	protected final Log log = LogFactory.getLog(getClass());
	
	//TODO:
	@RequestMapping(value="/module/diagnosiscapturerwanda/diagnosisCapture", method=RequestMethod.GET)
    public String processDiagnosisCapturePageGet(@RequestParam(value="patientId") Integer patientId,
    		@RequestParam(value="visitId") Integer visitId,
    		HttpSession session, 
    		ModelMap map){
		
		
		Patient patient = Context.getPatientService().getPatient(patientId);
		if (patient == null)
			return null;
		map.put("patient", patient);
		
		Visit visit = Context.getVisitService().getVisit(visitId);
		if (visit == null)
			throw new RuntimeException("You must pass in a valid visitId to this page.");
		if (visit != null && !visit.getPatient().equals(patient))	
			throw new RuntimeException("visit passed into DiagnosisPatientDashboardController doesn't belong to patient passed into this controller.");
		map.put("visit", visit);
		
		DiagnosisCaptureController.loadMetadata(map);
		
		return null;
    }

	/**
	 * helper that throws concepts in the model
	 * @param map
	 */
    private static ModelMap loadMetadata(ModelMap map){
		map.put("concept_set_primary_diagnosis", MetadataDictionary.CONCEPT_SET_PRIMARY_CARE_PRIMARY_DIAGNOSIS_CONSTRUCT);
		map.put("concept_set_secondary_diagnosis", MetadataDictionary.CONCEPT_SET_PRIMARY_CARE_SECONDARY_DIAGNOSIS_CONSTRUCT);
		map.put("concept_primary_secondary", MetadataDictionary.CONCEPT_DIAGNOSIS_ORDER);
		map.put("concept_primary_care_diagnosis", MetadataDictionary.CONCEPT_PRIMARY_CARE_DIAGNOSIS);
		map.put("concept_confirmed_suspected", MetadataDictionary.CONCEPT_DIAGNOSIS_CONFIRMED_SUSPECTED);
		map.put("concept_diagnosis_other", MetadataDictionary.CONCEPT_DIAGNOSIS_NON_CODED);
		map.put("concept_confirmed", MetadataDictionary.CONCEPT_CONFIRMED);
		map.put("concept_suspected", MetadataDictionary.CONCEPT_SUSPTECTED);
		map.put("concept_set_body_system", MetadataDictionary.CONCEPT_SET_ICPC_DIAGNOSIS_GROUPING_CATEGORIES);
		map.put("concept_set_diagnosis_classification", MetadataDictionary.CONCEPT_SET_ICPC_SYMPTOM_INFECTION_INJURY_DIAGNOSIS);
		map.put("encounter_type_diagnosis", MetadataDictionary.ENCOUNTER_TYPE_DIAGNOSIS);
		map.put("encounter_type_findings", MetadataDictionary.ENCOUNTER_TYPE_FINDINGS);
		map.put("concept_symptom", MetadataDictionary.CONCEPT_CLASSIFICATION_SYMPTOM);
		map.put("concept_infection", MetadataDictionary.CONCEPT_CLASSIFICATION_INFECTION);
		map.put("concept_injury", MetadataDictionary.CONCEPT_CLASSIFICATION_INJURY);
		map.put("concept_diagnosis", MetadataDictionary.CONCEPT_CLASSIFICATION_DIAGNOSIS);
		return map;
    }
	
    @RequestMapping(value="/module/diagnosiscapturerwanda/diagnosisCapture", method=RequestMethod.POST)
    public String processDiagnosisCaptureSubmit(
    		@RequestParam(value="hiddenVisitId") Integer visitId,
    		@RequestParam(value="diagnosisId") Integer diagnosisId,
    		@RequestParam(value="primary_secondary") Integer primarySecondary,
    		@RequestParam(value="confirmed_suspected") Integer confirmedSuspected,
    		@RequestParam(value="diagnosisOther") String diagnosisOther,
    		HttpSession session, 
    		ModelMap map){
    	
    	Visit visit = Context.getVisitService().getVisit(visitId);
    	if (visit == null)
    		throw new RuntimeException("There is no visit for this patient on this day.  Please go to the diagnosis patient dashboard to figure out why...");
    	map.put("visit", visit);
    	map.put("patient", visit.getPatient());
    	DiagnosisCaptureController.loadMetadata(map);
    	
    	boolean hasValidationError = false;
    	//TODO: edit:  switch primary/secondary if necessary, compare values, trim 'other'
    	
    	//New Encounter:
    	Concept diagnosis = Context.getConceptService().getConcept(diagnosisId);
    	if (diagnosis != null || (diagnosisOther != null && !diagnosisOther.equals(""))){
    		
    		Encounter enc = constructPrimaryDiagnosisEncounter(visit.getPatient(), MetadataDictionary.ENCOUNTER_TYPE_DIAGNOSIS);
    		enc = constructDiagnosisObsTree(enc, diagnosis, primarySecondary, Context.getConceptService().getConcept(confirmedSuspected), diagnosisOther);
    		
    		//validate:  
    		hasValidationError = !hasExactlyOnePrimaryDiagnosis(visit, enc);
    		
    		//validate method
    		if (!hasValidationError){
    			//this is total crap;  its for the encounter validator which enforces the encounter between visit start and stop
        		if (enc.getEncounterDatetime().getTime() > visit.getStopDatetime().getTime()){
        			visit.setStopDatetime(DiagnosisUtil.getStartAndEndOfDay(enc.getEncounterDatetime())[1]);
        			Context.getVisitService().saveVisit(visit);
        		}
        		//save
    			enc.setVisit(visit);
	    		enc = Context.getEncounterService().saveEncounter(enc);
	    		visit.getEncounters().add(enc);
    		} else {
    			map.put("more_than_one_primary_diagnosis_err","err");
    			enc = null;
    		}
    	}	
		
    	DiagnosisCaptureController.loadMetadata(map);
    	return null;
    }
    
    /**
     * validates diagnosis encounter creation.  there must be exactly one primary diagnosis.
     * @param v
     * @param newEnc
     * @return
     */
    private boolean hasExactlyOnePrimaryDiagnosis(Visit v, Encounter newEnc){
    	Set<Encounter> visitEncs = v.getEncounters();
    	if (newEnc != null)
    		visitEncs.add(newEnc);
    	int count = 0;
    	for (Encounter e : visitEncs){
    		if (e.getEncounterType().equals(MetadataDictionary.ENCOUNTER_TYPE_DIAGNOSIS)){
    			for (Obs o : e.getObsAtTopLevel(false)){
    				if (o.getConcept().equals(MetadataDictionary.CONCEPT_SET_PRIMARY_CARE_PRIMARY_DIAGNOSIS_CONSTRUCT))
    					count ++;
    			}
    		}
    	}
    	visitEncs.remove(newEnc);
    	return (count == 1? true : false);
    }
    
    
    @RequestMapping(value="/module/diagnosiscapturerwanda/getDiagnosisByNameJSON", method=RequestMethod.GET)
    public String getDiagnosisByNameJSON(@RequestParam("searchPhrase") String searchPhrase,
			HttpSession session, 
			ModelMap model){
    	List<Concept> cList = new ArrayList<Concept>();
    	for (ConceptSearchResult csr : Context.getConceptService().findConceptAnswers(searchPhrase, Context.getLocale(), MetadataDictionary.CONCEPT_PRIMARY_CARE_DIAGNOSIS)){
    		cList.add(csr.getConcept());
    	}
    	model.put("json", DiagnosisUtil.convertToJSONAutoComplete(cList));
    	return "/module/diagnosiscapturerwanda/jsonAjaxResponse";
    }
    
    
    @RequestMapping(value="/module/diagnosiscapturerwanda/getDiagnosesByIcpcSystemJSON", method=RequestMethod.GET)
    public String getDiagnosesByIcpcSystemJSON(@RequestParam("groupingId") int groupingId,
			HttpSession session, 
			ModelMap model){
    	List<Concept> cList = DiagnosisUtil.getConceptListByGroupingAndClassification(groupingId, null);
    	Collections.sort(cList, new Comparator<Concept>(){
			@Override
			public int compare(Concept o1, Concept o2) {
				return o1.getName().getName().compareTo(o2.getName().getName());  //put in alphabetical order by locale
			}
    		
    	});
    	model.put("json", DiagnosisUtil.convertToJSON(cList));
    	return "/module/diagnosiscapturerwanda/jsonAjaxResponse";
    }
    
    /**
     * build the diagnosis encounter
     */
    private Encounter constructPrimaryDiagnosisEncounter(Patient patient, EncounterType encType){
    		Encounter encounter = new Encounter();
            encounter.setPatient(patient);
            encounter.setEncounterDatetime(new Date());
            encounter.setEncounterType(encType);
            
            String locStr = Context.getAuthenticatedUser().getUserProperty(OpenmrsConstants.USER_PROPERTY_DEFAULT_LOCATION);
            Location userLocation = null;
            try { 
                userLocation = Context.getLocationService().getLocation(Integer.valueOf(locStr));
            } catch (Exception ex){
                //pass
            }
            encounter.setLocation(userLocation);
            encounter.setProvider(Context.getAuthenticatedUser().getPerson()); //TODO: fix this

    		return encounter;
    }
    
    /**
     * build the primary diagnosis obs tree and attach to encounter
     */
    private Encounter constructDiagnosisObsTree(Encounter enc, Concept diagnosis, Integer primarySecondary, Concept confirmedSusptectedAnswer, String other){
    		
    		//determine primary or secondary; 0 = primary 1=secondary
    		Concept c = primarySecondary.equals(0) ? MetadataDictionary.CONCEPT_SET_PRIMARY_CARE_PRIMARY_DIAGNOSIS_CONSTRUCT : MetadataDictionary.CONCEPT_SET_PRIMARY_CARE_SECONDARY_DIAGNOSIS_CONSTRUCT;
    		
    		//build the obsGroup
    		Obs oParent = buildObs(enc.getPatient(), c, enc.getEncounterDatetime(), null, null, enc.getLocation());
    		//build the children
    		Obs oDiagnosis = buildObs(enc.getPatient(), MetadataDictionary.CONCEPT_PRIMARY_CARE_DIAGNOSIS, enc.getEncounterDatetime(), diagnosis, null, enc.getLocation());
    		Obs oConfirmedSuspected = buildObs(enc.getPatient(), MetadataDictionary.CONCEPT_DIAGNOSIS_CONFIRMED_SUSPECTED, enc.getEncounterDatetime(), confirmedSusptectedAnswer, null, enc.getLocation());
    		Obs oOther = buildObs(enc.getPatient(), MetadataDictionary.CONCEPT_DIAGNOSIS_NON_CODED, enc.getEncounterDatetime(), null, other, enc.getLocation());
    	
    		oParent.addGroupMember(oDiagnosis);
    		oParent.addGroupMember(oConfirmedSuspected);
    		if (oOther.getValueText() != null)
    			oParent.addGroupMember(oOther);
    		
    		enc.addObs(oParent);
    		
    		return enc;
    }
    
    /**
     * instantiate new obs:
     */
    private Obs buildObs(Patient p, Concept concept, Date obsDatetime, Concept answer, String value, Location location){
    	Obs ret = new Obs();
    	ret.setConcept(concept);
    	ret.setCreator(Context.getAuthenticatedUser());
    	ret.setDateCreated(new Date());
    	ret.setLocation(location);
    	ret.setObsDatetime(obsDatetime);
    	ret.setPerson(p);
    	if (answer != null)
    		ret.setValueCoded(answer);
    	if (value != null && !value.equals(""))
    		ret.setValueText(value);
    	return ret;
    }
}
