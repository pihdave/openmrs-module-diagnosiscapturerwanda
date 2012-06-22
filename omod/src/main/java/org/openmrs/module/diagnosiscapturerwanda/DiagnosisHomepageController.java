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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Location;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsConstants;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

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

    
	//TODO:
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
}
