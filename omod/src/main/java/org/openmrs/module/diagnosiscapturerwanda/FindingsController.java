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

import java.util.List;

import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.ConceptSearchResult;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.api.context.Context;
import org.openmrs.module.diagnosiscapturerwanda.util.DiagnosisUtil;
import org.openmrs.util.OpenmrsUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class FindingsController {
	
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
	@RequestMapping(value="/module/diagnosiscapturerwanda/findings", method=RequestMethod.GET)
    public String processDiagnosisCapturePageGet(@RequestParam(value="patientId") Integer patientId,
    		@RequestParam(value="visitId") Integer visitId,
    		@RequestParam(required=false, value="obsGroupId") Integer obsGroupId,
    		@RequestParam(value="visitToday", required=false) String visitToday,
    		HttpSession session, 
    		ModelMap map){
		
		
		Patient patient = Context.getPatientService().getPatient(patientId);
		if (patient == null)
			return null;
		map.put("patient", patient);
		map.put("visitToday", visitToday);
		
		Visit visit = Context.getVisitService().getVisit(visitId);
		if (visit == null)
			throw new RuntimeException("You must pass in a valid visitId to this page.");
		if (visit != null && !visit.getPatient().equals(patient))	
			throw new RuntimeException("visit passed into DiagnosisPatientDashboardController doesn't belong to patient passed into this controller.");
		map.put("visit", visit);
		
		FindingsController.loadMetadata(map);
		
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
    	map.put("encounter_type_findings", MetadataDictionary.ENCOUNTER_TYPE_FINDINGS);
    	map.put("concept_set_findings", MetadataDictionary.CONCEPT_SET_PRIMARY_CARE_FINDINGS_CONSTRUCT);
		map.put("concept_primary_care_diagnosis", MetadataDictionary.CONCEPT_PRIMARY_CARE_DIAGNOSIS);
		map.put("concept_confirmed_suspected", MetadataDictionary.CONCEPT_DIAGNOSIS_CONFIRMED_SUSPECTED);
		map.put("concept_diagnosis_other", MetadataDictionary.CONCEPT_DIAGNOSIS_NON_CODED);
		map.put("concept_confirmed", MetadataDictionary.CONCEPT_CONFIRMED);
		map.put("concept_suspected", MetadataDictionary.CONCEPT_SUSPTECTED);
		map.put("concept_set_body_system", MetadataDictionary.CONCEPT_SET_ICPC_DIAGNOSIS_GROUPING_CATEGORIES);
		map.put("concept_set_diagnosis_classification", MetadataDictionary.CONCEPT_SET_ICPC_SYMPTOM_INFECTION_INJURY_DIAGNOSIS);
		map.put("concept_symptom", MetadataDictionary.CONCEPT_CLASSIFICATION_SYMPTOM);
		map.put("concept_infection", MetadataDictionary.CONCEPT_CLASSIFICATION_INFECTION);
		map.put("concept_injury", MetadataDictionary.CONCEPT_CLASSIFICATION_INJURY);
		map.put("concept_diagnosis", MetadataDictionary.CONCEPT_CLASSIFICATION_DIAGNOSIS);
		map.put("concept_findings_other", MetadataDictionary.CONCEPT_FINDINGS_OTHER);
		map.put("concept_findings", MetadataDictionary.CONCEPT_FINDINGS);
		
		List<Concept> findingsConcepts = MetadataDictionary.CONCEPT_CLASSIFICATION_SYMPTOM.getSetMembers();
		 
		map.put("findingsConcepts", DiagnosisUtil.convertConceptToAutoCompleteObj(findingsConcepts));
		
		return map;
    }

    
    /**
     * this is the main post method
     * @param visitId
     * @param hiddenObsGroupId
     * @param diagnosisId
     * @param primarySecondary
     * @param confirmedSuspected
     * @param diagnosisOther
     * @param session
     * @param map
     * @return
     */
    @RequestMapping(value="/module/diagnosiscapturerwanda/findings", method=RequestMethod.POST)
    public String processDiagnosisCaptureSubmit(
    		@RequestParam(value="hiddenVisitId") Integer visitId,
    		@RequestParam(required=false, value="hiddenObsGroupId") Integer hiddenObsGroupId,
    		@RequestParam(value="findingsId") Integer diagnosisId,
    		@RequestParam(required=false, value="findingsOther") String diagnosisOther,
    		@RequestParam(value="visitToday", required=false) String visitToday,
    		HttpSession session, 
    		ModelMap map){
    	
    	Visit visit = Context.getVisitService().getVisit(visitId);
    	if (visit == null)
    		throw new RuntimeException("There is no visit for this patient on this day.  Please go to the diagnosis patient dashboard to figure out why...");
   
    	map.put("patient", visit.getPatient());
    	map.put("visit", visit);
    	map.put("visitToday", visitToday);
    	FindingsController.loadMetadata(map);
    	
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
    		
			for (Obs o : oParent.getGroupMembers(false)){
				//new diagnosis
				Integer existingVal = o.getValueCoded()== null ? null : o.getValueCoded().getConceptId();
				if (o.getConcept().equals(MetadataDictionary.CONCEPT_FINDINGS) && (OpenmrsUtil.nullSafeEquals(diagnosisId, existingVal) == false)){
					o.setValueCoded(Context.getConceptService().getConcept(diagnosisId));
					saveNeeded = true;
				}
				if (o.getConcept().equals(MetadataDictionary.CONCEPT_FINDINGS_OTHER) && OpenmrsUtil.nullSafeEquals(diagnosisOther, o.getValueText()) == false){
					o.setValueText(diagnosisOther);
					saveNeeded = true;
				}
			}
    	} else {
    		enc = DiagnosisUtil.findEncounterByTypeInVisit(visit, MetadataDictionary.ENCOUNTER_TYPE_FINDINGS);
	    	Concept diagnosis = Context.getConceptService().getConcept(diagnosisId);
	    	if (diagnosis != null || (diagnosisOther != null && !diagnosisOther.equals(""))){
	    		if (enc == null)
	    			enc = DiagnosisUtil.buildEncounter(visit.getPatient(), MetadataDictionary.ENCOUNTER_TYPE_FINDINGS, visit);
	    		enc = constructDiagnosisObsTree(enc, diagnosis, diagnosisOther);
	    	}	
	    	saveNeeded = true;
    	}
    	//validate and save
		if (saveNeeded && enc != null){

    			//this is total crap;  its for the encounter validator which enforces the encounter between visit start and stop
        		if (enc.getEncounterDatetime().getTime() > visit.getStopDatetime().getTime()){
        			visit.setStopDatetime(DiagnosisUtil.getStartAndEndOfDay(enc.getEncounterDatetime())[1]);
        			Context.getVisitService().saveVisit(visit);
        		}
        		//save
    			enc.setVisit(visit);
	    		enc = Context.getEncounterService().saveEncounter(enc);

		}
		//if everything's cool, then reset with a redirect
    	return "redirect:/module/diagnosiscapturerwanda/findings.list?patientId=" + visit.getPatient().getPatientId() + "&visitId=" + visit.getVisitId();
    }
    
    /**
     * utility build the primary diagnosis obs tree and attach to encounter
     */
    private Encounter constructDiagnosisObsTree(Encounter enc, Concept diagnosis, String other){
    		
    		//determine primary or secondary; 0 = primary 1=secondary
    		Concept c = MetadataDictionary.CONCEPT_SET_PRIMARY_CARE_FINDINGS_CONSTRUCT;
    		
    		//build the obsGroup
    		Obs oParent = DiagnosisUtil.buildObs(enc.getPatient(), c, enc.getEncounterDatetime(), null, null, null, enc.getLocation());
    		//build the children
    		Obs oDiagnosis = DiagnosisUtil.buildObs(enc.getPatient(), MetadataDictionary.CONCEPT_FINDINGS, enc.getEncounterDatetime(), diagnosis, null, null, enc.getLocation());
    		Obs oOther = DiagnosisUtil.buildObs(enc.getPatient(), MetadataDictionary.CONCEPT_FINDINGS_OTHER, enc.getEncounterDatetime(), null, other, null, enc.getLocation());
    	
    		oParent.addGroupMember(oDiagnosis);
    		oParent.addGroupMember(oOther);
    		
    		enc.addObs(oParent);
    		
    		return enc;
    }
    
}
