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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class DiagnosisHomepageController {
	
	protected final Log log = LogFactory.getLog(getClass());

	//TODO:
	@RequestMapping(value="/module/diagnosiscapturerwanda/diagnosisHomepage",method=RequestMethod.GET)
    public String processHomePageGet(){
		return null;
    }

    
	//TODO:
    @RequestMapping(value="/module/diagnosiscapturerwanda/diagnosisHomepage",method=RequestMethod.POST)
    public String processHomePageSubmit(){
    	return null;
    }
}
