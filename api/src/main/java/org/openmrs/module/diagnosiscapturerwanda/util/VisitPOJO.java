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
package org.openmrs.module.diagnosiscapturerwanda.util;

import java.util.Date;

import org.openmrs.Patient;


/**
 *
 */
public class VisitPOJO {
	
	private String location;
	private Date date;
	private String provider;
	private String diagnosis;
	private Patient patient;
	private Integer id;
    
    public Patient getPatient() {
    	return patient;
    }

    public void setPatient(Patient patient) {
    	this.patient = patient;
    }

	public String getLocation() {
    	return location;
    }
	
    public void setLocation(String location) {
    	this.location = location;
    }
	
    public Date getDate() {
    	return date;
    }
	
    public void setDate(Date date) {
    	this.date = date;
    }
	
    public String getProvider() {
    	return provider;
    }
	
    public void setProvider(String provider) {
    	this.provider = provider;
    }
	
    public String getDiagnosis() {
    	return diagnosis;
    }
	
    public void setDiagnosis(String diagnosis) {
    	this.diagnosis = diagnosis;
    }
	
    public Integer getId() {
    	return id;
    }

    public void setId(Integer id) {
    	this.id = id;
    }
}
