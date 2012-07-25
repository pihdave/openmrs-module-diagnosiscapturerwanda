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

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Location;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.module.diagnosiscapturerwanda.queue.DiagnosisCaptureQueueService;
import org.openmrs.module.diagnosiscapturerwanda.queue.QueueObj;
import org.openmrs.module.diagnosiscapturerwanda.queue.jsonconverter.QueueSimpleObjectConverter;
import org.openmrs.module.diagnosiscapturerwanda.util.DiagnosisUtil;
import org.openmrs.util.OpenmrsConstants;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class DiagnosisHomepageController {
	
	protected final Log log = LogFactory.getLog(getClass());

	//TODO:
	@RequestMapping(value="/module/diagnosiscapturerwanda/diagnosisHomepage",method=RequestMethod.GET)
    public void processHomePageGet(HttpSession session, ModelMap map){
		map.put("locations", Context.getLocationService().getAllLocations(false));
		map.put("user", Context.getAuthenticatedUser());
		
		//add user location to model, so default can be set in UI.
		String locStr = Context.getAuthenticatedUser().getUserProperty(OpenmrsConstants.USER_PROPERTY_DEFAULT_LOCATION);
        Location userLocation = null;
        try { 
            userLocation = Context.getLocationService().getLocation(Integer.valueOf(locStr));
        } catch (Exception ex){
            //pass
        }
        if (userLocation == null){
        	map.put("userLocation", null);
        } else {
        	map.put("userLocation", userLocation);
        }        
    }

    
    @RequestMapping(value="/module/diagnosiscapturerwanda/diagnosisHomepage",method=RequestMethod.POST)
    public void processHomePageSubmit(ModelMap model, HttpSession session, HttpServletRequest request){
    	String locationStr = request.getParameter("location");
    		
         if (locationStr != null && !locationStr.equals("")){
             
             Location location = Context.getLocationService().getLocation(Integer.valueOf(locationStr));
             if (location == null)
                 throw new NullPointerException();
             
             //note:  these allow you to switch seamlessly to the rwandaprimarycare module homepage.  the actual strings used by the two modules are the same.
             session.setAttribute(MetadataDictionary.SESSION_ATTRIBUTE_PRIMARY_CARE_WORKSTATION_LOCATION, location);
             session.setAttribute(MetadataDictionary.SESSION_ATTRIBUTE_DIAGNOSIS_WORKSTATION_LOCATION, location);
             Context.setVolatileUserData(MetadataDictionary.VOLATILE_USER_DATA_LOGIN_LOCATION, location);
             
             User user = Context.getAuthenticatedUser();
             model.addAttribute("user", user);
             
             if (Context.getAuthenticatedUser().getUserProperty(OpenmrsConstants.USER_PROPERTY_DEFAULT_LOCATION) == null || !Context.getAuthenticatedUser().getUserProperty(OpenmrsConstants.USER_PROPERTY_DEFAULT_LOCATION).equals(locationStr)){
                 user.setUserProperty(OpenmrsConstants.USER_PROPERTY_DEFAULT_LOCATION, location.getLocationId().toString());
                 Context.getUserService().saveUser(user, null);
             }
         }    
    }
    
    /**
     * this loads the patientQueue, called by ajax
     * @param session
     * @param model
     * @return
     */
    @RequestMapping(value="/module/diagnosiscapturerwanda/getJSONQueue",method=RequestMethod.GET)
    public String getJSONQueue(HttpSession session, ModelMap model){
    	Map<Integer, QueueObj> map = Context.getService(DiagnosisCaptureQueueService.class).getServiceQueueMap();
    	model.put("json", DiagnosisUtil.convertToJSON((new QueueSimpleObjectConverter()).convert(map)));
    	return "/module/diagnosiscapturerwanda/jsonAjaxResponse";
    }	
    
    /**
     * this removes someone from the patient queue, called by ajax
     * @param patientId
     * @param encUuid
     * @param action
     * @param session
     * @param model
     * @return
     */
    @RequestMapping(value="/module/diagnosiscapturerwanda/processQueueItem",method=RequestMethod.GET)
    public String processQueueSelection( @RequestParam("patientId") int patientId,  
    			@RequestParam("encounterUuid") String encUuid,  
    			@RequestParam("action") String action,
    			HttpSession session, 
    			ModelMap model){
    	String json = "{\"result\": \"FAILED\",\"reason\":\" action can only be process, select or remove\"}";
    	try {
	    	if (action.equals("process")) {
	    		//this is now handled by the DiagnosisPatientDashboardController, so that direct jumps from registration to diagnosis app dashboard removes patients from queue...
	    		//Context.getService(DiagnosisCaptureQueueService.class).selectQueueObjectByEncounterUuid(encUuid);
	    		json = "{\"result\": \"SUCCESS\"}";
	    	} else if (action.equals("skip")) {
	    		Context.getService(DiagnosisCaptureQueueService.class).skipQueueObjectByEncounterUuid(encUuid);
	    		json = "{\"result\": \"SUCCESS\"}";
	    	} else if (action.equals("remove")) {
	    		Context.getService(DiagnosisCaptureQueueService.class).removeFromQueue(encUuid);
	    		json = "{\"result\": \"SUCCESS\"}";
	    	}
    	} catch (Exception ex){
    		json = "{\"result\": \"FAILED\",\"reason\":\"" + ex.getLocalizedMessage() + "\"}";
    	}
    	model.put("json", json);
    	return "/module/diagnosiscapturerwanda/jsonAjaxResponse";
    }
}
