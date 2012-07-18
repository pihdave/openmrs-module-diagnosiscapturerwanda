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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.Order;
import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.api.context.Context;
import org.openmrs.module.diagnosiscapturerwanda.util.DiagnosisUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
/**
 * Lab controller
 * Data Model:
 * step 1.  choose person, location, date, 
 * result: an encounter, and orders with concept_id pointing to the lab panels or lab test, order type = 3
 * step 2. enter results:
 * result: the result you just create obs, map obs to order  , no obs groups
 */
@Controller
public class LabsController {
	
	protected final Log log = LogFactory.getLog(getClass());

	@RequestMapping(value="/module/diagnosiscapturerwanda/labs", method=RequestMethod.GET)
    public String processLabsPageGet(@RequestParam(value="patientId") Integer patientId,
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
		
		LabsController.loadMetadata(map);
		
		
		return null;
    }

	/**
	 * helper that throws concepts & encounterTypes in the model
	 * @param map
	 */
    private static ModelMap loadMetadata(ModelMap map){
		map.put("supportedTests", DiagnosisUtil.getSupportedLabTests());
		map.put("labOrderType", DiagnosisUtil.getLabOrderType());
		map.put("labEncounterType", DiagnosisUtil.getLabTestEncounterType());
		return map;
    }

    @RequestMapping(value="/module/diagnosiscapturerwanda/labs", method=RequestMethod.POST)
    public String processLabsSubmit(ModelMap model, HttpSession session, HttpServletRequest request){
    	
    	Visit visit = Context.getVisitService().getVisit(Integer.valueOf(request.getParameter("hiddenVisitId")));
    	
    	List<Concept> testsRequested = new ArrayList<Concept>();
    	String root = "lab_";
    	for (Concept c : DiagnosisUtil.getSupportedLabTests()){
    		String requestParam = root + c.getConceptId();
    		if (request.getParameter(requestParam) != null){
    			testsRequested.add(c);
    		}	
    	}
    	
    	boolean saveNeeded = false;
    	Encounter labEnc = DiagnosisUtil.findEncounterByTypeInVisit(visit, DiagnosisUtil.getLabTestEncounterType());
    	//check for encounter, if not exists, add
    	if (labEnc == null){
    		labEnc = DiagnosisUtil.buildEncounter(visit.getPatient(), DiagnosisUtil.getLabTestEncounterType());
    		saveNeeded = true;
    	}	
    	
    	//void deselected orders:
    	if (labEnc.getOrders() != null)
	    	for (Order o : labEnc.getOrders()){
	    		if (!testsRequested.contains(o.getConcept()))
	    			o = Context.getOrderService().voidOrder(o, "deselected in diagnosiscapture");
	    	} 
    	
    	//TODO:  do the same for obs
    	
    	//now add, if necessary
    	for (Concept c : testsRequested){
    		boolean found = false;
    		if (labEnc.getOrders() != null)
	    		for (Order o : labEnc.getOrders()){
	    			if (o.getConcept().equals(c) && !o.isVoided())
	    				found = true;
	    		}
    		if (!found){
    			//we need to create new:
    			labEnc.addOrder(DiagnosisUtil.buildOrder(visit.getPatient(), c, labEnc));
    			saveNeeded = true;
    		}
    	}
    	
    	if (saveNeeded) {
    		if (labEnc.getEncounterDatetime().getTime() > visit.getStopDatetime().getTime()){
    			visit.setStopDatetime(DiagnosisUtil.getStartAndEndOfDay(labEnc.getEncounterDatetime())[1]);
    			Context.getVisitService().saveVisit(visit);
    		}
    		//save
    		labEnc.setVisit(visit);
    		labEnc = Context.getEncounterService().saveEncounter(labEnc);
    	}
    	return null;
    }
    
}
