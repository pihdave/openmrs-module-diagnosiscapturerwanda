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

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.api.context.Context;
import org.openmrs.module.diagnosiscapturerwanda.util.DiagnosisUtil;
import org.openmrs.propertyeditor.EncounterEditor;
import org.openmrs.propertyeditor.VisitEditor;
import org.openmrs.util.OpenmrsUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class VitalsController {
	
	protected final Log log = LogFactory.getLog(getClass());

    @InitBinder
	public void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
    	binder.registerCustomEditor(Encounter.class, new EncounterEditor());
        binder.registerCustomEditor(Visit.class, new VisitEditor());
	}

    /**
     * Construct the form-backing object
     */
    @ModelAttribute("vitalsCommand")
	public VitalsCommand getEncounter(@RequestParam(value="visitId") Visit visit,
                                  @RequestParam(value="encounterId", required=false) Encounter encounter,
                                  HttpSession session, ModelMap map) {
        Location location = DiagnosisUtil.getLocationLoggedIn(session);
        return new VitalsCommand(visit, encounter, location);
    }

    public Map<String, Concept> getQuestions() {
        Map<String, Concept> questions = new LinkedHashMap<String, Concept>();
        questions.put("temperature", MetadataDictionary.CONCEPT_VITALS_TEMPERATURE);
        questions.put("weight", MetadataDictionary.CONCEPT_VITALS_WEIGHT);
        questions.put("height", MetadataDictionary.CONCEPT_VITALS_HEIGHT);
        questions.put("systolicBp", MetadataDictionary.CONCEPT_VITALS_SYSTOLIC_BLOOD_PRESSURE);
        questions.put("diastolicBp", MetadataDictionary.CONCEPT_VITALS_DIASTOLIC_BLOOD_PRESSURE);
        return questions;
    }

    /**
     * Control the rendering of the vitals form
     */
	@RequestMapping(value="/module/diagnosiscapturerwanda/vitals", method=RequestMethod.GET)
    public void processVitalsPageGet(@RequestParam(value="patientId") Integer patientId,
                                     @RequestParam(value="visitId") Integer visitId,
                                     @RequestParam(value="visitToday", required=false) String visitToday,
                                     ModelMap map) {
		
		Patient patient = Context.getPatientService().getPatient(patientId);
		if (patient != null) {
			map.addAttribute("patient", patient);
		}
		
		map.addAttribute("visitToday", visitToday);
		
		Visit visit = Context.getVisitService().getVisit(visitId);
		if (visit == null)
			throw new RuntimeException("You must pass in a valid visitId to this page.");
		if (visit != null && !visit.getPatient().equals(patient))	
			throw new RuntimeException("visit passed into DiagnosisPatientDashboardController doesn't belong to patient passed into this controller.");
		map.addAttribute("visit", visit);
		
        map.addAttribute("questions", getQuestions());
        
    }

    /**
     * Controls the submission of the vitals form
     */
    @RequestMapping(value="/module/diagnosiscapturerwanda/vitals", method=RequestMethod.POST)
    public String processVitalsSubmit(@ModelAttribute("vitalsCommand") VitalsCommand vitalsCommand,
                                      BindingResult errors, HttpServletRequest request) {

        Visit visit = vitalsCommand.getVisit();
        Encounter encounter = vitalsCommand.getEncounter();
        for (Map.Entry<String, Concept> entry : getQuestions().entrySet()) {
            Double newValue = vitalsCommand.getValues().get(entry.getKey());
            Obs existingObs = vitalsCommand.getObs(entry.getValue());
            Obs newObs = null;
            if (existingObs != null) {
                if (!OpenmrsUtil.nullSafeEquals(existingObs.getValueNumeric(), newValue)) {
                    if (newValue != null) {
                        newObs = Obs.newInstance(existingObs);
                        newObs.setPreviousVersion(existingObs);
                        newObs.setValueNumeric(newValue);
                    }
                    existingObs.setVoided(true);
                    existingObs.setVoidedBy(Context.getAuthenticatedUser());
                    existingObs.setVoidReason("Obs value changed on vitals page");
                }
            }
            else if (newValue != null) {
                newObs = new Obs();
                newObs.setConcept(entry.getValue());
                newObs.setValueNumeric(newValue);
            }
            if (newObs != null) {
                encounter.addObs(newObs);
            }
        }
        
      //this is total crap;  its for the encounter validator which enforces the encounter between visit start and stop
		if (encounter.getEncounterDatetime().getTime() > visit.getStopDatetime().getTime()){
			visit.setStopDatetime(DiagnosisUtil.getStartAndEndOfDay(encounter.getEncounterDatetime())[1]);
			Context.getVisitService().saveVisit(visit);
		}
        encounter.setVisit(visit);
        Context.getEncounterService().saveEncounter(encounter);

        return "redirect:/module/diagnosiscapturerwanda/diagnosisPatientDashboard.list?patientId=" + visit.getPatient().getPatientId() + "&visitId=" + visit.getId();
    }

    public class VitalsCommand {

        private Visit visit;
        private Encounter encounter;
        private Map<String, Double> values = new HashMap<String, Double>();

        public VitalsCommand(Visit visit, Encounter encounter, Location location) {

            this.visit = visit;
            
            EncounterType vitalEncounterType = MetadataDictionary.ENCOUNTER_TYPE_VITALS;
            if (encounter == null) {
                for (Encounter e : visit.getEncounters()) {
                    if (e.getEncounterType().equals(vitalEncounterType)) {
                        encounter = e;
                    }
                }
            }

            if (encounter == null) {
                encounter = new Encounter();
                encounter.setPatient(visit.getPatient());
                encounter.setEncounterDatetime(visit.getStartDatetime()); //vitals should always correspond to the start of a visit?
                encounter.setEncounterType(vitalEncounterType);
                encounter.setLocation(location);
                encounter.setProvider(Context.getAuthenticatedUser().getPerson());  // TODO: Fix this
            }
            this.encounter = encounter;

            for (Map.Entry<String, Concept> entry : getQuestions().entrySet()) {
                values.put(entry.getKey(), getObsValue(entry.getValue()));
            }
        }

        public Obs getObs(Concept c) {
            if (encounter != null) {
                for (Obs o : encounter.getAllObs(false)) {
                    if (o.getConcept().equals(c)) {
                        return o;
                    }
                }
            }
            return null;
        }

        public Double getObsValue(Concept c) {
            Obs o = getObs(c);
            return (o == null ? null : o.getValueNumeric());
        }

        public Visit getVisit() {
            return visit;
        }

        public void setVisit(Visit visit) {
            this.visit = visit;
        }

        public Encounter getEncounter() {
            return encounter;
        }

        public void setEncounter(Encounter encounter) {
            this.encounter = encounter;
        }

        public Map<String, Double> getValues() {
            return values;
        }

        public void setValues(Map<String, Double> values) {
            this.values = values;
        }
    }
}
