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

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.servlet.http.HttpSession;

import org.openmrs.Concept;
import org.openmrs.EncounterType;
import org.openmrs.Location;
import org.openmrs.OpenmrsObject;
import org.openmrs.Order;
import org.openmrs.OrderType;
import org.openmrs.api.context.Context;
import org.openmrs.module.diagnosiscapturerwanda.MetadataDictionary;
import org.openmrs.module.diagnosiscapturerwanda.jsonconverter.DiagnosisCustomListConverter;
import org.openmrs.module.diagnosiscapturerwanda.jsonconverter.DiagnosisCustomOpenmrsObjectConverter;
import org.openmrs.ui.framework.BasicUiUtils;


/**
 * this is a util class for business logic type functions.
 * NOTE -- the lab metadata lookups are here, because I want these to use the simplelabentry global properties to load lab metadata.
 */
public class DiagnosisUtil {

	
	/**
	 * This is the main method for converting either a List<OpenmrsObject> or OpenmrsObject to JSON
	 * @param o
	 * @return
	 */
	public static String convertToJSON(Object o){
		BasicUiUtils bu = new BasicUiUtils();
		Object toConvert = null;
		if (o instanceof List){
			DiagnosisCustomListConverter conv = new DiagnosisCustomListConverter();
			toConvert = conv.convert((List<?>) o);
		} else if (o instanceof OpenmrsObject) {
			DiagnosisCustomOpenmrsObjectConverter conv = new DiagnosisCustomOpenmrsObjectConverter();
			toConvert = conv.convert((OpenmrsObject) o);
		}
		if (toConvert != null)
			return bu.toJson(toConvert);
		else
			throw new RuntimeException("DiagnosisUtil.convertToJSON couldn't convert object " + o.toString());
	}
	
	/*
	 * TODO
	 * Returns a list of concepts based on choice of grouping and classification
	 * must handle invalid ids
	 * grouping ID are the diagnosis body systems
	 * classificaitonId is the injury/diagnosis/symptom/... conceptSet
	 */
	public static List<Concept> getConceptListByGroupingAndClassification(Integer groupingId, Integer classificationId){
		List<Concept> ret = new ArrayList<Concept>();
		if (groupingId != null){
			Concept groups = Context.getConceptService().getConcept(groupingId);
			if (groups != null){
				for (Concept cs : groups.getSetMembers()){
					ret.add(cs);
				}
			}
		}
		if (classificationId !=null){
			Concept cats = Context.getConceptService().getConcept(classificationId);
			if (cats != null){
				for (Concept cs: cats.getSetMembers()){
					ret.add(cs);
				}
			}
		}
		return ret;
	}
	
	/**
	 * Returns the simplelabentry supported lab tests based on global property
	 * simplelabentry.supportedTests
	 * 
	 * @return
	 */
	public static List<Concept> getSupportedLabTests(){
		String gpString = Context.getAdministrationService().getGlobalProperty("simplelabentry.supportedTests");
		List<Concept> ret = new ArrayList<Concept>();
		for (StringTokenizer st = new StringTokenizer(gpString, ","); st.hasMoreTokens(); ) {
			String s = st.nextToken().trim();
			Concept c = Context.getConceptService().getConceptByUuid(s);
			if (c == null){
				try {
					c = Context.getConceptService().getConcept(Integer.valueOf(s));
				} catch (Exception ex){}
			}
			if (c != null)
				ret.add(c);
			else
				throw new RuntimeException("Unable to load concept " + s + " from global property simplelabentry.supportedTests");
		}	
		return ret;
	}
	
	/**
	 * return the encounterType specified by simplelabentry.labTestEncounterType
	 * @return
	 */
	public static EncounterType getLabTestEncounterType(){
		String gpString = Context.getAdministrationService().getGlobalProperty("simplelabentry.labTestEncounterType");
		EncounterType ret = Context.getEncounterService().getEncounterTypeByUuid(gpString);
		if (ret == null){
			try {
				ret = Context.getEncounterService().getEncounterType(Integer.valueOf(gpString));
			} catch (Exception ex){}
		}
		if (ret != null)
			return ret;
		else 
			throw new RuntimeException("Unable to load EncounterType " + gpString + " from global property simplelabentry.labTestEncounterType");	
	}
	
	/**
	 * should return the orderType specified by simplelabentry.labOrderType
	 * @return
	 */
	public static OrderType getLabOrderType(){
		String gpString = Context.getAdministrationService().getGlobalProperty("simplelabentry.labOrderType");
		OrderType ret = Context.getOrderService().getOrderTypeByUuid(gpString);
		Order o = new Order();
		if (ret == null){
			try {
				ret = Context.getOrderService().getOrderType(Integer.valueOf(gpString));
			} catch (Exception ex){}
		}
		if (ret != null)
			return ret;
		else 
			throw new RuntimeException("Unable to load EncounterType " + gpString + " from global property simplelabentry.labOrderType");	
	}
	
    public static Location getLocationLoggedIn(HttpSession session) {
        return (Location) session.getAttribute(MetadataDictionary.SESSION_ATTRIBUTE_DIAGNOSIS_WORKSTATION_LOCATION);
    }
	
	
}
