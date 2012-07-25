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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Order;
import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.api.context.Context;
import org.openmrs.module.diagnosiscapturerwanda.util.DiagnosisUtil;
import org.openmrs.util.OpenmrsUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.StringUtils;
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
    		@RequestParam(required=false, value="readOnly", defaultValue="false") String readOnly,
    		HttpSession session, 
    		ModelMap map){
		
		//used in the jsp for read-only view of labs in this visit
		if (readOnly.equals("true"))
			map.put("readOnly", true);
		
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
		
		List<Concept> supportedLabTests = DiagnosisUtil.getSupportedLabTests();
		LabsController.loadMetadata(map);
		
		//existing lab obs and orders
		for (Encounter e: visit.getEncounters()){
			if (!e.isVoided() && e.getEncounterType().equals(MetadataDictionary.ENCOUNTER_TYPE_LABS)){
				map.put("labEncounter", e);
				List<Order> labOrders = new ArrayList<Order>();
				
				for (Order o :e.getOrders()){
					if (!o.isVoided() && supportedLabTests.contains(o.getConcept()))
						labOrders.add(o);
				}
				map.put("labOrders", labOrders);
				map.put("encounterObs", e.getAllObs(false));
				map.put("labOrders", labOrders);
				break;
			}
		}
		
		//we need to load up lab concepts themselves.
		Map<Concept, List<Concept>> testMap = getSupportedTestMap(supportedLabTests);
		map.put("testMap", testMap);
		//map.put("now", new Date());
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
    
    
    /**
     * returns a map with key: conceptId of lab panel; value: list of tests in that panel.
     * if the GP points to a single test that's not a ConceptSet, the map's value will be Collections.singletonList(key)
     * @param supportedLabTests
     * @return
     */
    private Map<Concept, List<Concept>> getSupportedTestMap(List<Concept> supportedLabTests){
    	
    	Map<Concept, List<Concept>> testMap = new LinkedHashMap<Concept, List<Concept>>();
		for (Concept c : supportedLabTests){
			if (c.getSetMembers() == null || c.getSetMembers().size() == 0)
				testMap.put(c, Collections.singletonList(c));
			else {
				List<Concept> testList = new ArrayList<Concept>();
				for (Concept cTest : c.getSetMembers()){
					testList.add(cTest);
				}
				testMap.put(c, testList);
			}
		}
		return testMap;
    }

    
    /**
     * the main post method
     * @param map
     * @param session
     * @param request
     * @return
     */
    @RequestMapping(value="/module/diagnosiscapturerwanda/labs", method=RequestMethod.POST)
    public String processLabsSubmit(ModelMap map, HttpSession session, HttpServletRequest request){
    	
    	Visit visit = Context.getVisitService().getVisit(Integer.valueOf(request.getParameter("hiddenVisitId")));
    	
    	List<Concept> testsRequested = new ArrayList<Concept>();
    	String root = "lab_";
    	for (Concept c : DiagnosisUtil.getSupportedLabTests()){
    		String requestParam = root + c.getConceptId();
    		if (request.getParameter(requestParam) != null){
    			//for lazy loading:
    			c.getSetMembers();
    			testsRequested.add(c);
    		}	
    	}
    	
    	boolean saveNeeded = false;
    	Encounter labEnc = DiagnosisUtil.findEncounterByTypeInVisit(visit, DiagnosisUtil.getLabTestEncounterType());
    	//check for encounter, if not exists, add
    	if (labEnc == null){
    		labEnc = DiagnosisUtil.buildEncounter(visit.getPatient(), DiagnosisUtil.getLabTestEncounterType(), visit);
    		saveNeeded = true;
    	}	
    	
    	//void deselected orders and associated obs (leaving the encounter unvoided for now...)
    	if (labEnc.getOrders() != null){
	    	for (Order o : labEnc.getOrders()){
	    		if (!testsRequested.contains(o.getConcept()))
	    			o = Context.getOrderService().voidOrder(o, "deselected in diagnosiscapture");
	    	} 
    		for (Obs o : labEnc.getAllObs()){ //void all obs if their corresponding order is voided
    			if (!o.isVoided() && o.getOrder() != null && o.getOrder().isVoided()){
    				Context.getObsService().voidObs(o, "assoc. lab order voided");
    			}
    		}
    	}	
    	
    	
    	//now add orders, if necessary;  this will exclude the tests requested that have been deselected
    	if (testsRequested.size() > 0){
    		Map<Concept, List<Concept>> testMap = getSupportedTestMap(DiagnosisUtil.getSupportedLabTests());  //map representing all possibilities
    		SimpleDateFormat sdf = Context.getDateFormat(); //is this right?
    		
	    	for (Concept c : testsRequested){ //for each selected lab pannel
	    		
	    		// build or find the order:
	    		Order labPannelOrder = null;
	    		if (labEnc.getOrders() != null)
		    		for (Order o : labEnc.getOrders()){
		    			if (o.getConcept().equals(c) && !o.isVoided())
		    				labPannelOrder = o;
		    		}
	    		if (labPannelOrder == null){
	    			//we need to create new:
	    			labPannelOrder = DiagnosisUtil.buildOrder(visit.getPatient(), c, labEnc);
	    			labEnc.addOrder(labPannelOrder);
	    			saveNeeded = true;
	    		}
	    		
	    		//get test result date, if there is one...  In order for there to be one, the order already has to exist (or there'd be no testResultDate_ input on the page for this order
	    		String resultDateStr = request.getParameter("testResultDate_" + c.getId());
	    		//get result date for the pannel
	    		if (StringUtils.hasText(resultDateStr)){
		    		Date resultDate = new Date();
		    		try {
						resultDate = sdf.parse(resultDateStr);
					} catch (ParseException e) {
						//pass; just use default of now()
					}
					//set discontinuedDate on the order
	    			if ((labPannelOrder.getDiscontinuedDate() == null || !labPannelOrder.getDiscontinuedDate().equals(resultDate))){
	    				labPannelOrder.setDiscontinued(true);
	    				labPannelOrder.setDiscontinuedBy(Context.getAuthenticatedUser());
	    				labPannelOrder.setDiscontinuedReasonNonCoded("diagnosiscapture lab results");
	    				labPannelOrder.setDiscontinuedDate(resultDate);
	    				saveNeeded = true;
	    			}
	    			
					//finally, ALL OBS HANDLING:
					List<Concept> potentialObsConcepts = testMap.get(c);
					for (Concept cTest : potentialObsConcepts){
						String labResStr = request.getParameter("testResult_" + cTest.getId()); 
						Obs oExisting = findNonVoidedObsInEncounter(labEnc, cTest); //get the existing obs for that result, if there is one.
						if (StringUtils.hasText(labResStr)){
							//get the new test result value
							Double labTestResult = null;
							try {
								labTestResult = Double.valueOf(labResStr); //the lab test result
							} catch (Exception ex){
								ex.printStackTrace();
								throw new RuntimeException("I'm only currently supporting valueNumeric obs for lab tests.");
							}
							
							if (oExisting != null){ // there's an existing obs
									if (!OpenmrsUtil.nullSafeEquals(oExisting.getValueNumeric(),labTestResult)){ //only change it if necessary; else do nothing
										oExisting.setValueNumeric(labTestResult);
										saveNeeded = true;
									}
							} else { //or create a new obs:
								Obs resObs = DiagnosisUtil.buildObs(visit.getPatient(), cTest, resultDate, null, null, labTestResult ,labEnc.getLocation());
								resObs.setOrder(labPannelOrder); //associate obs with order
								labEnc.addObs(resObs);
								saveNeeded = true;
							}
						} else if (oExisting != null) { //if empty value returned from page, void obs, or do nothing
							oExisting.setVoided(true);
							oExisting.setVoidedBy(Context.getAuthenticatedUser());
							oExisting.setVoidReason("diagnosiscapture");
							oExisting.setDateVoided(new Date());
							saveNeeded = true;
						}
					}
	    		}	
	    	} 
    	}
    	
    	if (saveNeeded) {
    		//stretch visit duration, if necessary
    		if (labEnc.getEncounterDatetime().getTime() > visit.getStopDatetime().getTime()){
    			visit.setStopDatetime(DiagnosisUtil.getStartAndEndOfDay(labEnc.getEncounterDatetime())[1]);
    			Context.getVisitService().saveVisit(visit);
    		}
    		//save
    		labEnc.setVisit(visit);
    		labEnc = Context.getEncounterService().saveEncounter(labEnc);
    	}
    	
    	//TODO:  if there are NO non-voided obs or orders, void the encounter?

    	return "redirect:/module/diagnosiscapturerwanda/labs.list?patientId=" + visit.getPatient().getPatientId() + "&visitId=" + visit.getVisitId();
    }
    
    private Obs findNonVoidedObsInEncounter(Encounter e, Concept c){
    	for (Obs o: e.getAllObs(false)){
    		if (o.getConcept().equals(c))
    			return o;
    	}
    	return null;
    }
    
}
