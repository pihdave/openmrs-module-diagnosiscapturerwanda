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
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Encounter;
import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.api.context.Context;
import org.openmrs.module.diagnosiscapturerwanda.queue.DiagnosisCaptureQueueService;
import org.openmrs.module.diagnosiscapturerwanda.util.DiagnosisUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class DiagnosisPatientDashboardController {
	
	protected final Log log = LogFactory.getLog(getClass());

	//TODO:
	@RequestMapping(value="/module/diagnosiscapturerwanda/diagnosisPatientDashboard",method=RequestMethod.GET)
    public String processDashboardPageGet(@RequestParam(value="patientId") Integer patientId,  
    		@RequestParam(required=false, value="encounterUuid") String encounterUuid,
    		@RequestParam(required=false, value="encounterId") Integer encounterId,
    		@RequestParam(required=false, value="visitId") Integer visitId,
    		HttpSession session, ModelMap map){
		
		//patient
		Patient patient = Context.getPatientService().getPatient(patientId);
		if (patient == null)
			return null;
		map.put("patient", patient);
		
		//registration enc
		Encounter registrationEnc = null;
		if (encounterUuid != null){
			registrationEnc = Context.getEncounterService().getEncounterByUuid(encounterUuid);
		}
		if (registrationEnc == null && encounterId != null){
			registrationEnc = Context.getEncounterService().getEncounter(encounterId);
		}
		if (registrationEnc != null && !registrationEnc.getPatient().equals(patient))
			throw new RuntimeException("encounter passed into DiagnosisPatientDashboardController doesn't belong to patient passed into this controller.");

		//find the Visit
		Visit visit = null;
		if (visitId != null)
			visit = Context.getVisitService().getVisit(visitId);
		try {
			if (visit == null)
				visit = DiagnosisUtil.findVisit(registrationEnc, patient, session); //null encounter is handled by method 
		} catch (RuntimeException ex){
			//pass -- registration required message handled in jsp, based on visit being null
		}
		if (visit != null && !visit.getPatient().equals(patient))	
			throw new RuntimeException("visit passed into DiagnosisPatientDashboardController doesn't belong to patient passed into this controller.");
		if (registrationEnc == null && visit != null)
			registrationEnc = DiagnosisUtil.findEncounterByTypeInVisit(visit, MetadataDictionary.ENCOUNTER_TYPE_REGISTRATION);

		//add previous primary care visits:
		List<Visit> vListAll = Context.getVisitService().getVisitsByPatient(patient, true, false); //patient, inactive, includeVoided
		vListAll.remove(visit);  //don't show our visit
		List<Visit> v = new ArrayList<Visit>(); //trim visits to only outpatient
		for (Visit vTmp : vListAll){
			if (vTmp.getVisitType().equals(MetadataDictionary.VISIT_TYPE_OUTPATIENT))
				v.add(vTmp);
		}
		
		//redirect to patient dashboard and show previous visits if 
		if(visit == null)
		{
			int backEntryLimit = Integer.parseInt(Context.getAdministrationService().getGlobalProperty("diagnosisCaptureRwanda.backEntryLimit"));
			if(backEntryLimit > 0)
			{
				
				StringBuilder visitIds = new StringBuilder();
				for(Visit v1: vListAll)
				{
					if(calculateDaysDifference(v1.getStartDatetime()) <= backEntryLimit)
					{
						if(visitIds.length() > 0)
						{
							visitIds.append(",");
						}
						visitIds.append(v1.getId());
					}
				}
				
				if(visitIds.length() > 0)
				{
					return "redirect:/module/diagnosiscapturerwanda/diagnosisHomepage.list?visitIds=" + visitIds.toString();
				}
			}
		}
		map.put("visitList", v);
		
		map.put("visit", visit);
		if(visit != null && calculateDaysDifference(visit.getStartDatetime()) == 0)
		{
			map.put("visitToday", true);
		}
		map.put("encounter_type_vitals", MetadataDictionary.ENCOUNTER_TYPE_VITALS);
		map.put("encounter_type_lab", MetadataDictionary.ENCOUNTER_TYPE_LABS);
		map.put("encounter_type_registration", MetadataDictionary.ENCOUNTER_TYPE_REGISTRATION);
		map.put("encounter_type_diagnosis", MetadataDictionary.ENCOUNTER_TYPE_DIAGNOSIS);
		map.put("encounter_type_findings", MetadataDictionary.ENCOUNTER_TYPE_FINDINGS);
		if (visit != null)
			map.put("visitIsToday", DiagnosisUtil.isVisitToday(visit));
		
		//CONCEPTS	
		//vitals
		map.put("concept_temperature", MetadataDictionary.CONCEPT_VITALS_TEMPERATURE);
		map.put("concept_height", MetadataDictionary.CONCEPT_VITALS_HEIGHT);
		map.put("concept_weight", MetadataDictionary.CONCEPT_VITALS_WEIGHT);
		map.put("concept_systolic", MetadataDictionary.CONCEPT_VITALS_SYSTOLIC_BLOOD_PRESSURE);
		map.put("concept_diastolic", MetadataDictionary.CONCEPT_VITALS_DIASTOLIC_BLOOD_PRESSURE);
			//calculated:
		//  BMI could be improved to only include obs from this visit?  But it makes sense to check for previous heights for adults... etc...
		map.put("currentBMI", DiagnosisUtil.bmiAsString(patient));
		
		//findings
		map.put("concept_set_findings", MetadataDictionary.CONCEPT_SET_PRIMARY_CARE_FINDINGS_CONSTRUCT);
		map.put("concept_findings_other", MetadataDictionary.CONCEPT_FINDINGS_OTHER);
		map.put("concept_findings", MetadataDictionary.CONCEPT_FINDINGS);
		
		//labs -- NEED TO MAP OUT SIMPLE LAB ENTRY
		
		
		//diagnosis
		map.put("concept_set_primary_diagnosis", MetadataDictionary.CONCEPT_SET_PRIMARY_CARE_PRIMARY_DIAGNOSIS_CONSTRUCT);
		map.put("concept_set_secondary_diagnosis", MetadataDictionary.CONCEPT_SET_PRIMARY_CARE_SECONDARY_DIAGNOSIS_CONSTRUCT);
		map.put("concept_primary_care_diagnosis", MetadataDictionary.CONCEPT_PRIMARY_CARE_DIAGNOSIS);
		map.put("concept_primary_secondary", MetadataDictionary.CONCEPT_DIAGNOSIS_ORDER);
		map.put("concept_confirmed_suspected", MetadataDictionary.CONCEPT_DIAGNOSIS_CONFIRMED_SUSPECTED);
		map.put("concept_diagnosis_other", MetadataDictionary.CONCEPT_DIAGNOSIS_NON_CODED);
		
		
		//treatment
		
		
		
		
		
		//remove from queue
		if (registrationEnc != null)
			Context.getService(DiagnosisCaptureQueueService.class).selectQueueObjectByEncounterUuid(registrationEnc.getUuid());
		
		return null;
    }

    
	//TODO:
    @RequestMapping(value="/module/diagnosiscapturerwanda/diagnosisPatientDashboard",method=RequestMethod.POST)
    public String processDashboardSubmit(){
    	return null;
    }
    
    private int calculateDaysDifference(Date visitDate)
	{
    	Date todaysDate = Calendar.getInstance().getTime();
    	long milis1 = visitDate.getTime();
		long milis2 = todaysDate.getTime();
		
		long diff = milis2 - milis1;
		
		long diffDays = diff / (24 * 60 * 60 * 1000);
	
		return (int)diffDays;
	}

}
