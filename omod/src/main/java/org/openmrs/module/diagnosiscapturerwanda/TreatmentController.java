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

import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.api.context.Context;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;


@Controller
public class TreatmentController {

	protected final Log log = LogFactory.getLog(getClass());
	
	//TODO:

	@RequestMapping(value="/module/diagnosiscapturerwanda/treatment", method=RequestMethod.GET)
    public void processTreatmentPageGet(@RequestParam(value="patientId") Integer patientId,  
    		@RequestParam(value="visitId") Integer visitId,
    		@RequestParam(value="visitToday", required=false) String visitToday,
    		HttpSession session, ModelMap map){
		
		Patient patient = Context.getPatientService().getPatient(patientId);
		
		if (patient != null)
			map.put("patient", patient);
		
		map.put("visitToday", visitToday);
		Visit visit = Context.getVisitService().getVisit(visitId);
		if (visit == null)
			throw new RuntimeException("You must pass in a valid visitId to this page.");
		if (visit != null && !visit.getPatient().equals(patient))	
			throw new RuntimeException("visit passed into DiagnosisPatientDashboardController doesn't belong to patient passed into this controller.");
		map.put("visit", visit);
		
    }

    
	//TODO:
	@RequestMapping(value="/module/diagnosiscapturerwanda/treatment", method=RequestMethod.POST)
    public String processTreatmentSubmit(){
    	return null;
    }
    
}
