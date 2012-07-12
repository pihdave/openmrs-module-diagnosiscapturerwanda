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
import org.openmrs.api.EncounterService;
import org.openmrs.api.context.Context;
import org.openmrs.module.diagnosiscapturerwanda.util.DiagnosisUtil;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.util.OpenmrsUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class DiagnosisCaptureController {
	
	protected final Log log = LogFactory.getLog(getClass());
	
	/**
	 * this is the main getter method
	 * @param patientId
	 * @param visitId
	 * @param encounterId
	 * @param session
	 * @param map
	 * @return
	 */
	@RequestMapping(value="/module/diagnosiscapturerwanda/diagnosisCapture", method=RequestMethod.GET)
    public String processDiagnosisCapturePageGet(@RequestParam(value="patientId") Integer patientId,
    		@RequestParam(value="visitId") Integer visitId,
    		@RequestParam(required=false, value="encounterId") Integer encounterId,
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
		
		if (encounterId != null){
			try {
				map.put("encounter", Context.getEncounterService().getEncounter(encounterId));
			} catch (Exception ex){
				log.error("invalid encounterId passed to DiagnosisCapture Controller.");
			}
		}
		
		return null;
    }

	/**
	 * helper that throws concepts & encounterTypes in the model
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
	
    /**
     * this is the main setter method
     * @param visitId
     * @param diagnosisId
     * @param primarySecondary
     * @param confirmedSuspected
     * @param diagnosisOther
     * @param session
     * @param map
     * @return
     */
    @RequestMapping(value="/module/diagnosiscapturerwanda/diagnosisCapture", method=RequestMethod.POST)
    public String processDiagnosisCaptureSubmit(
    		@RequestParam(value="hiddenVisitId") Integer visitId,
    		@RequestParam(required=false, value="hiddenEncounterId") Integer hiddenEncounterId,
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
    	
    	//Edit Encounter:  switch primary/secondary if necessary, compare values, trim 'other'
    	Encounter enc = null;
    	boolean saveNeeded = false;
    	if (hiddenEncounterId != null){
    		
    		enc = Context.getEncounterService().getEncounter(hiddenEncounterId);
    		for (Obs oParent : enc.getObsAtTopLevel(false)){
    			boolean continueWithThisObs = false;
    			if (oParent.getConcept().equals(MetadataDictionary.CONCEPT_SET_PRIMARY_CARE_PRIMARY_DIAGNOSIS_CONSTRUCT)){
    				continueWithThisObs = true;
	    			if (primarySecondary.equals(1)){
	    				saveNeeded = true;
	    				oParent.setConcept(MetadataDictionary.CONCEPT_SET_PRIMARY_CARE_SECONDARY_DIAGNOSIS_CONSTRUCT);
	    			}	
    			} else if (oParent.getConcept().equals(MetadataDictionary.CONCEPT_SET_PRIMARY_CARE_SECONDARY_DIAGNOSIS_CONSTRUCT)) {
    				continueWithThisObs = true;
    				if (primarySecondary.equals(0)){
	    				saveNeeded = true;
	    				oParent.setConcept(MetadataDictionary.CONCEPT_SET_PRIMARY_CARE_PRIMARY_DIAGNOSIS_CONSTRUCT);
    				}
    			}
    			if (continueWithThisObs){
    				for (Obs o : oParent.getGroupMembers(false)){
    					//new diagnosis
    					if (o.getConcept().equals(MetadataDictionary.CONCEPT_PRIMARY_CARE_DIAGNOSIS) && OpenmrsUtil.nullSafeEquals(diagnosisId, o.getValueCoded().getConceptId()) == false){
    						o.setValueCoded(Context.getConceptService().getConcept(diagnosisId));
    						saveNeeded = true;
    					}
    					if (o.getConcept().equals(MetadataDictionary.CONCEPT_DIAGNOSIS_NON_CODED) && OpenmrsUtil.nullSafeEquals(diagnosisOther, o.getValueText()) == false){
    						o.setValueText(diagnosisOther);
    						saveNeeded = true;
    					}
    					if (o.getConcept().equals(MetadataDictionary.CONCEPT_DIAGNOSIS_CONFIRMED_SUSPECTED) && OpenmrsUtil.nullSafeEquals(confirmedSuspected, o.getValueCoded().getConceptId()) == false){
    						o.setValueCoded(Context.getConceptService().getConcept(confirmedSuspected));
    						saveNeeded = true;
    					}
    				}
    			}
    		}
    	} else {
    	
	    	//else its a New Encounter:
	    	Concept diagnosis = Context.getConceptService().getConcept(diagnosisId);
	    	if (diagnosis != null || (diagnosisOther != null && !diagnosisOther.equals(""))){
	    		
	    		enc = constructPrimaryDiagnosisEncounter(visit.getPatient(), MetadataDictionary.ENCOUNTER_TYPE_DIAGNOSIS);
	    		enc = constructDiagnosisObsTree(enc, diagnosis, primarySecondary, Context.getConceptService().getConcept(confirmedSuspected), diagnosisOther);
	    	}	
	    	saveNeeded = true;
	    	
    	}
    	//validate and save
		if (saveNeeded && enc != null){
			hasValidationError = !hasOneOrNoPrimaryDiagnosis(visit, enc);
    		
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
    			DiagnosisCaptureController.loadMetadata(map);
    			// if encounter already has an ID, then we're editing an encounter, and want to return to editing an encounter
    			if (enc != null && enc.getId() != null)
    				map.put("encounter", enc);
    			return null;
    		}
		}
		//if everything's cool, then reset with a redirect
    	return "redirect:/module/diagnosiscapturerwanda/diagnosisCapture.list?patientId=" + visit.getPatient().getPatientId() + "&visitId=" + visit.getVisitId();
    }
    
    /**
     * validates diagnosis encounter creation.  there must be exactly one primary diagnosis.
     * @param v
     * @param newEnc
     * @return
     */
    private boolean hasOneOrNoPrimaryDiagnosis(Visit v, Encounter newEnc){
    	Set<Encounter> visitEncs = v.getEncounters();
    	if (newEnc != null)
    		visitEncs.add(newEnc);
    	int count = 0;
    	for (Encounter e : visitEncs){
    		if (!e.isVoided() && e.getEncounterType().equals(MetadataDictionary.ENCOUNTER_TYPE_DIAGNOSIS)){
    			for (Obs o : e.getObsAtTopLevel(false)){
    				if (o.getConcept().equals(MetadataDictionary.CONCEPT_SET_PRIMARY_CARE_PRIMARY_DIAGNOSIS_CONSTRUCT))
    					count ++;
    			}
    		}
    	}
    	visitEncs.remove(newEnc);
    	return (count <= 1? true : false);
    }
    
    /**
     * the ajax method for looking up a diagnosis by name
     * @param searchPhrase
     * @param session
     * @param model
     * @return
     */
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
    
    @RequestMapping(value="/module/diagnosiscapturerwanda/deleteDiagnosis", method=RequestMethod.POST)
    public String deleteDiagnosis(@RequestParam("encounterId") Integer encounterId,
			HttpSession session, 
			ModelMap model){
    	try {
    		EncounterService es = Context.getEncounterService();
    		Encounter enc = es.getEncounter(encounterId);
    		es.voidEncounter(enc, "Voided through diagnosisCapture");
    		model.put("json", DiagnosisUtil.convertToJSON("{\"result\":\"success\"}"));
    	} catch (Exception ex){
    		model.put("json", DiagnosisUtil.convertToJSON("{\"result\":\"failed\"}"));
    		ex.printStackTrace();
    	}
    	return "/module/diagnosiscapturerwanda/jsonAjaxResponse";
    }
    
    /**
     * the ajax method that returns all diagnosis in an ICPC chapter
     * @param groupingId
     * @param session
     * @param model
     * @return
     */
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
     * utility to build the diagnosis encounter
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
     * utility build the primary diagnosis obs tree and attach to encounter
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
     * util to instantiate new obs:
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
