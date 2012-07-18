package org.openmrs.module.diagnosiscapturerwanda;

import javax.servlet.http.HttpSession;

import org.openmrs.Encounter;
import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.api.context.Context;
import org.openmrs.module.diagnosiscapturerwanda.util.DiagnosisUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;


@Controller
public class DiagnosisTablePortletController {
	
	@RequestMapping("/module/diagnosiscapturerwanda/diagnosisTable.portlet")
    public String processDashboardPageGet(@RequestParam(value="patientId") Integer patientId,  
    		@RequestParam(required=false, value="encounterUuid") String encounterUuid,
    		@RequestParam(required=false, value="encounterId") Integer encounterId,
    		@RequestParam(required=false, value="visitId") Integer visitId,
    		HttpSession session, ModelMap map){
		Patient patient = Context.getPatientService().getPatient(patientId);
		if (patient == null)
			return null;
		map.put("patient", patient);
		//find the Visit
		Encounter registrationEnc = null;
		if (encounterUuid != null){
			registrationEnc = Context.getEncounterService().getEncounterByUuid(encounterUuid);
		}
		if (registrationEnc == null && encounterId != null){
			registrationEnc = Context.getEncounterService().getEncounter(encounterId);
		}
		if (registrationEnc != null && !registrationEnc.getPatient().equals(patient))
			throw new RuntimeException("encounter passed into DiagnosisPatientDashboardController doesn't belong to patient passed into this controller.");
		
		//this will throw exception if visit is not found.
		Visit visit = null;
		if (visitId != null)
			visit = Context.getVisitService().getVisit(visitId);
		try {
			if (visit == null)
				visit = DiagnosisUtil.findVisit(registrationEnc, patient, session); //null encounter is handled by method 
		} catch (RuntimeException ex){
			//pass -- registration required message handled in jsp, based on visit being null
		}
		map.put("visit", visit);
		map.put("encounter_type_diagnosis", MetadataDictionary.ENCOUNTER_TYPE_DIAGNOSIS);
		map.put("concept_set_primary_diagnosis", MetadataDictionary.CONCEPT_SET_PRIMARY_CARE_PRIMARY_DIAGNOSIS_CONSTRUCT);
		map.put("concept_set_secondary_diagnosis", MetadataDictionary.CONCEPT_SET_PRIMARY_CARE_SECONDARY_DIAGNOSIS_CONSTRUCT);
		map.put("concept_primary_secondary", MetadataDictionary.CONCEPT_DIAGNOSIS_ORDER);
		map.put("concept_confirmed_suspected", MetadataDictionary.CONCEPT_DIAGNOSIS_CONFIRMED_SUSPECTED);
		map.put("concept_diagnosis_other", MetadataDictionary.CONCEPT_DIAGNOSIS_NON_CODED);
		
		return null;
    }
		
}
