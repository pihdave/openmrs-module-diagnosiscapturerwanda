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
import java.util.List;

import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.ConceptSearchResult;
import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.api.context.Context;
import org.openmrs.module.diagnosiscapturerwanda.util.DiagnosisUtil;
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
		
		map.put("concept_set_primary_diagnosis", MetadataDictionary.CONCEPT_SET_PRIMARY_CARE_PRIMARY_DIAGNOSIS_CONSTRUCT);
		map.put("concept_set_secondary_diagnosis", MetadataDictionary.CONCEPT_SET_PRIMARY_CARE_SECONDARY_DIAGNOSIS_CONSTRUCT);
		map.put("concept_primary_secondary", MetadataDictionary.CONCEPT_DIAGNOSIS_ORDER);
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

		
		return null;
    }

    
	//TODO:
    @RequestMapping(value="/module/diagnosiscapturerwanda/diagnosisCapture", method=RequestMethod.POST)
    public String processDiagnosisCaptureSubmit(){
    	return null;
    }
    
    
    @RequestMapping(value="/module/diagnosiscapturerwanda/getDiagnosisByNameJSON", method=RequestMethod.GET)
    public String getDiagnosisByNameJSON(@RequestParam("searchPhrase") String searchPhrase,
			HttpSession session, 
			ModelMap model){
    	List<Concept> cList = new ArrayList<Concept>();
    	for (ConceptSearchResult csr : Context.getConceptService().findConceptAnswers(searchPhrase, Context.getLocale(), MetadataDictionary.CONCEPT_PRIMARY_CARE_DIAGNOSIS)){
    		cList.add(csr.getConcept());
    	}
    	model.put("json", DiagnosisUtil.convertToJSON(cList));
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
}
