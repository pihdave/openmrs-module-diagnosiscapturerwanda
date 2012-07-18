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
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.ConceptSearchResult;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.api.ObsService;
import org.openmrs.api.context.Context;
import org.openmrs.module.diagnosiscapturerwanda.util.DiagnosisUtil;
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
	 * this is the main get method
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
    		@RequestParam(required=false, value="obsGroupId") Integer obsGroupId,
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
		
		if (obsGroupId != null){
			try {
				map.put("obsGroup", Context.getObsService().getObs(obsGroupId));
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
     * this is the main post method
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
    		@RequestParam(required=false, value="hiddenObsGroupId") Integer hiddenObsGroupId,
    		@RequestParam(value="diagnosisId") Integer diagnosisId,
    		@RequestParam(value="primary_secondary") Integer primarySecondary,
    		@RequestParam(value="confirmed_suspected") Integer confirmedSuspected,
    		@RequestParam(value="diagnosisOther") String diagnosisOther,
    		HttpSession session, 
    		ModelMap map){
    	
    	Visit visit = Context.getVisitService().getVisit(visitId);
    	if (visit == null)
    		throw new RuntimeException("There is no visit for this patient on this day.  Please go to the diagnosis patient dashboard to figure out why...");
   
    	map.put("patient", visit.getPatient());
    	map.put("visit", visit);
    	DiagnosisCaptureController.loadMetadata(map);
    	
    	boolean hasValidationError = false;
    	Encounter enc = null;
    	Obs oParent = null;
    	boolean saveNeeded = false;
    	
    	//Edit Encounter:  switch primary/secondary if necessary, compare values
    	if (hiddenObsGroupId != null){	
    		
    		//for lazy loading.  Total fucking crap
    		oParent = Context.getObsService().getObs(hiddenObsGroupId);
    		enc = Context.getEncounterService().getEncounter(oParent.getEncounter().getId());
    		for (Obs oTmp : enc.getObsAtTopLevel(false)){
    			if (oTmp.getId().equals(oParent.getId())){
    				oParent = oTmp;
    				break;
    			}	
    		}
    		
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
    	} else {
    		enc = DiagnosisUtil.findEncounterByTypeInVisit(visit, MetadataDictionary.ENCOUNTER_TYPE_DIAGNOSIS);
	    	Concept diagnosis = Context.getConceptService().getConcept(diagnosisId);
	    	if (diagnosis != null || (diagnosisOther != null && !diagnosisOther.equals(""))){
	    		if (enc == null)
	    			enc = DiagnosisUtil.buildEncounter(visit.getPatient(), MetadataDictionary.ENCOUNTER_TYPE_DIAGNOSIS);
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
    		
    		} else {
    			map.put("more_than_one_primary_diagnosis_err","err");
    			//editing an existing diagnosis:
    			if (oParent != null && oParent.getId() != null)
    				map.put("obsGroup", oParent);
    			else if (enc != null) {  // adding a new one, and its invalid, just drop it.
    				for (Obs o : enc.getObsAtTopLevel(false)){
    					if (o.getId() == null)
    						enc.removeObs(o);
    				}
    			}	
    			return null;
    			
    		}
		}
		//if everything's cool, then reset with a redirect
    	return "redirect:/module/diagnosiscapturerwanda/diagnosisCapture.list?patientId=" + visit.getPatient().getPatientId() + "&visitId=" + visit.getVisitId();
    }
    
    /**
     * validates diagnosis encounter creation.  there must be exactly one primary diagnosis.
     * doesn't add encounter to visit, if necessary
     * @param v
     * @param newEnc
     * @return
     */
    private boolean hasOneOrNoPrimaryDiagnosis(Visit v, Encounter enc){
    	Set<Encounter> visitEncs = v.getEncounters();
    	boolean remove = false;
    	if (enc != null && !visitEncs.contains(enc)){
    		visitEncs.add(enc);
    		remove = true;
    	}	
    	int count = 0;
    	for (Encounter e : visitEncs){
    		if (!e.isVoided() && e.getEncounterType().equals(MetadataDictionary.ENCOUNTER_TYPE_DIAGNOSIS)){
    			for (Obs o : e.getObsAtTopLevel(false)){
    				if (o.getConcept().equals(MetadataDictionary.CONCEPT_SET_PRIMARY_CARE_PRIMARY_DIAGNOSIS_CONSTRUCT))
    					count ++;
    			}
    		}
    	}
    	if (remove)
    		visitEncs.remove(enc);
    	return (count <= 1? true : false);
    }
    
    /**
     * the ajax method for looking up a diagnosis by name; if restrictBySymptom is true, then only return symptoms, else, return all
     * @param searchPhrase
     * @param session, restrictBySymptoms, searchPharse
     * @param model
     * @return
     */
    @RequestMapping(value="/module/diagnosiscapturerwanda/getDiagnosisByNameJSON", method=RequestMethod.GET)
    public String getDiagnosisByNameJSON(@RequestParam("searchPhrase") String searchPhrase,
    		@RequestParam("restrictBySymptom") boolean restrictBySymptom,
			HttpSession session, 
			ModelMap model){
    	List<Concept> cList = new ArrayList<Concept>();
    	for (ConceptSearchResult csr : Context.getConceptService().findConceptAnswers(searchPhrase, Context.getLocale(), MetadataDictionary.CONCEPT_PRIMARY_CARE_DIAGNOSIS)){
    		if (restrictBySymptom && MetadataDictionary.CONCEPT_CLASSIFICATION_SYMPTOM.getSetMembers().contains(csr.getConcept())){
    				cList.add(csr.getConcept());
    		} else {
    			cList.add(csr.getConcept());
    		}
    	}

    	
    	model.put("json", DiagnosisUtil.convertToJSONAutoComplete(cList));
    	return "/module/diagnosiscapturerwanda/jsonAjaxResponse";
    }
    
    //TODO: this is wrong, should only delete obsGroup, and maybe encounter only if empty
    //to do this, you need the jsps to send obsGroupId, not encounterId
    @RequestMapping(value="/module/diagnosiscapturerwanda/deleteDiagnosis", method=RequestMethod.POST)
    public String deleteDiagnosis(@RequestParam("obsGroupId") Integer obsGroupId,
			HttpSession session, 
			ModelMap model){
    	try {
    		ObsService os = Context.getObsService();
    		Obs o = os.getObs(obsGroupId);
    		os.voidObs(o, "voided through diagnosiscapture");
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
    		@RequestParam("restrictBySymptom") boolean restrictBySymptom,
			HttpSession session, 
			ModelMap model){
    	Integer classificationId = null;
    	if (restrictBySymptom)
    		classificationId = MetadataDictionary.CONCEPT_CLASSIFICATION_SYMPTOM.getId();
    	List<Concept> cList = DiagnosisUtil.getConceptListByGroupingAndClassification(groupingId, classificationId); //these come out of this method sorted by name
    	model.put("json", DiagnosisUtil.convertToJSON(cList));
    	return "/module/diagnosiscapturerwanda/jsonAjaxResponse";
    }
    
    /**
     * utility build the primary diagnosis obs tree and attach to encounter
     */
    private Encounter constructDiagnosisObsTree(Encounter enc, Concept diagnosis, Integer primarySecondary, Concept confirmedSusptectedAnswer, String other){
    		
    		//determine primary or secondary; 0 = primary 1=secondary
    		Concept c = primarySecondary.equals(0) ? MetadataDictionary.CONCEPT_SET_PRIMARY_CARE_PRIMARY_DIAGNOSIS_CONSTRUCT : MetadataDictionary.CONCEPT_SET_PRIMARY_CARE_SECONDARY_DIAGNOSIS_CONSTRUCT;
    		
    		//build the obsGroup
    		Obs oParent = DiagnosisUtil.buildObs(enc.getPatient(), c, enc.getEncounterDatetime(), null, null, enc.getLocation());
    		//build the children
    		Obs oDiagnosis = DiagnosisUtil.buildObs(enc.getPatient(), MetadataDictionary.CONCEPT_PRIMARY_CARE_DIAGNOSIS, enc.getEncounterDatetime(), diagnosis, null, enc.getLocation());
    		Obs oConfirmedSuspected = DiagnosisUtil.buildObs(enc.getPatient(), MetadataDictionary.CONCEPT_DIAGNOSIS_CONFIRMED_SUSPECTED, enc.getEncounterDatetime(), confirmedSusptectedAnswer, null, enc.getLocation());
    		Obs oOther = DiagnosisUtil.buildObs(enc.getPatient(), MetadataDictionary.CONCEPT_DIAGNOSIS_NON_CODED, enc.getEncounterDatetime(), null, other, enc.getLocation());
    	
    		oParent.addGroupMember(oDiagnosis);
    		oParent.addGroupMember(oConfirmedSuspected);
    		oParent.addGroupMember(oOther);
    		
    		enc.addObs(oParent);
    		
    		return enc;
    }
}
