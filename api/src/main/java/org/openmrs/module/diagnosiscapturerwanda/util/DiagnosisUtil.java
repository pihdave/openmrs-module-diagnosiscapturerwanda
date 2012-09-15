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
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;

import javax.servlet.http.HttpSession;

import org.openmrs.Concept;
import org.openmrs.ConceptName;
import org.openmrs.ConceptNumeric;
import org.openmrs.ConceptSearchResult;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.OpenmrsObject;
import org.openmrs.Order;
import org.openmrs.OrderType;
import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.module.diagnosiscapturerwanda.MetadataDictionary;
import org.openmrs.module.diagnosiscapturerwanda.jsonconverter.DiagnosisCustomListConverter;
import org.openmrs.module.diagnosiscapturerwanda.jsonconverter.DiagnosisCustomOpenmrsObjectConverter;
import org.openmrs.ui.framework.BasicUiUtils;
import org.openmrs.util.OpenmrsConstants;
import org.springframework.util.StringUtils;


/**
 * this is a util class for business logic type functions.
 * NOTE -- the lab metadata lookups are here, because I want these to use the simplelabentry global properties to load lab metadata.
 */
public class DiagnosisUtil {

	
	/**
	 * This is the main method for converting either a List<Object> or OpenmrsObject to JSON
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
		} else { //what the hell, try to convert even if we don't know what it is:
			toConvert = o;
		}	
		return bu.toJson(toConvert);
	}
	
	/**
	 * jquery autocomplete widget needs shit to look like:  { label: "Choice1", value: "id1" } , { label: "Choice2", value: "id2" }
	 * @param cList
	 * @return
	 */
	public static String convertToJSONAutoComplete(List<Concept> cList){
		BasicUiUtils bu = new BasicUiUtils();
		List<AutoCompleteObj> ret = new ArrayList<AutoCompleteObj>();
		Locale currentLocale = Context.getLocale();
		if (cList != null){
			for (Concept c: cList){
				for(ConceptName cn: c.getNames())
				{
					AutoCompleteObj o = new AutoCompleteObj();
					o.setValue(c.getConceptId());
					
					if(cn.getLocale().equals(currentLocale))
					{
						o.setLabel(cn.getName());
					}
					else
					{
						String label = cn.getName() + "<span class='otherHit'>  &rArr; " + c.getName(currentLocale) + "</span>";
						o.setLabel(label);
					}
					
					ret.add(o);
				}
			}
		}
		return bu.toJson(ret);
	}
	
	/*
	 * Returns a list of concepts based on choice of grouping and classification
	 * must handle invalid ids
	 * grouping ID are the diagnosis body systems
	 * classificaitonId is the injury/diagnosis/symptom/... conceptSet
	 */
	public static List<Concept> getConceptListByGroupingAndClassification(Integer groupingId, Integer classificationId){
		List<Concept> groupsRet = new ArrayList<Concept>();
		List<Concept> classificationRet = new ArrayList<Concept>();
		if (groupingId != null){
			Concept groups = Context.getConceptService().getConcept(groupingId);
			if (groups != null){
				for (Concept cs : groups.getSetMembers()){
					if (!groupsRet.contains(cs))
						groupsRet.add(cs);
				}
			}
		}
		if (classificationId !=null){
			Concept cats = Context.getConceptService().getConcept(classificationId);
			if (cats != null){
				for (Concept cs: cats.getSetMembers()){
					if (!classificationRet.contains(cs))
						classificationRet.add(cs);
				}
			}
		}

		//filter and return:
		if (groupingId != null && classificationId != null){
			List<Concept> ret = intersection(groupsRet,classificationRet);
			Collections.sort(ret, new ConceptNameComparator());
			return ret;
		} else if (classificationId == null){
			Collections.sort(groupsRet, new ConceptNameComparator());
			return groupsRet;
		} else if (groupingId == null){
			Collections.sort(classificationRet, new ConceptNameComparator());
			return classificationRet;
		} else
			return new ArrayList<Concept>();
	}
	
	
	/**
	 * concept name comparator
	 * @author dthomas
	 *
	 */
	private static class ConceptNameComparator implements Comparator<Concept>{
		@Override
		public int compare(Concept o1, Concept o2) {
			return o1.getName().getName().compareTo(o2.getName().getName());  //put in alphabetical order by locale
		}
	}
	
	/**
	 * helper class to intersect two arrayLists
	 * @param <T>
	 * @param list1
	 * @param list2
	 * @return
	 */
	private static <T> List<T> intersection(List<T> list1, List<T> list2) {
        List<T> list = new ArrayList<T>();

        for (T t : list1) {
            if(list2.contains(t)) {
                list.add(t);
            }
        }

        return list;
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
			if (c != null) {
				//for lazy loading
				c.getSetMembers();
				ret.add(c);
			} else
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
    
    public static Date[] getStartAndEndOfDay(Date datetime) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(datetime);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date startOfDay = cal.getTime();
        cal.add(Calendar.DAY_OF_MONTH, 1);
        cal.add(Calendar.MILLISECOND, -1);
        Date endOfDay = cal.getTime();
        return new Date[] { startOfDay, endOfDay };
    }
    
    /**
     * Util method for finding the visit.  EncounterUuid and encounterId can be null.
     * @param encounter -- usually the registration encounter, but it can be any encounter associated with a primary care Visit
     * @param patient (required)
     * @param session (required)
     * @return the visit
     */
    public static Visit findVisit(Encounter enc, Patient patient, HttpSession session) throws RuntimeException {
    	if (patient == null || session == null)
    		throw new RuntimeException("patient and session are required in util method findVisit");
		Visit visit = null;
		if (enc != null)
			visit = enc.getVisit();
		if (visit == null){
			//find the visit the hard way...
			Date earliestStartDate = DiagnosisUtil.getStartAndEndOfDay(new Date())[0];
			Date latestStartDate = DiagnosisUtil.getStartAndEndOfDay(new Date())[1];
			if (enc != null){
				earliestStartDate = DiagnosisUtil.getStartAndEndOfDay(enc.getEncounterDatetime())[0];
				latestStartDate = DiagnosisUtil.getStartAndEndOfDay(enc.getEncounterDatetime())[1];
			}
	
			List<Visit> vList = Context.getVisitService().getVisits(Collections.singletonList(MetadataDictionary.VISIT_TYPE_OUTPATIENT), Collections.singletonList(patient), Collections.singleton(getLocationLoggedIn(session)), null, earliestStartDate, latestStartDate,  null, null, null, true, false);
			if (vList != null && !vList.isEmpty())
				visit = vList.get(0);
		}
		if (visit == null)
			throw new RuntimeException("You must register the patient first!");
		return visit;
    }
    
    /**
     * check to see if visit start date is between (or equal to) day's start and end
     * @param v
     * @return
     */
    public static boolean isVisitToday(Visit v){
    	if ((getStartAndEndOfDay(v.getStartDatetime())[0].getTime() <= v.getStartDatetime().getTime()) 
    			&& (v.getStartDatetime().getTime() <= getStartAndEndOfDay(v.getStartDatetime())[1].getTime()))
    		return true;
    	return false;
    }
    
    /**
     * calculate BMI.  Ripped directly out of default openmrs portlet controller
     */
	public static String bmiAsString(Patient p){
		AdministrationService as = Context.getAdministrationService();
		ConceptService cs = Context.getConceptService();
		List<Obs> patientObs = Context.getObsService().getObservationsByPerson(p);
		Obs latestWeight = null;
		Obs latestHeight = null;
		String bmiAsString = "?";
		try {
			String weightString = as.getGlobalProperty("concept.weight");
			ConceptNumeric weightConcept = null;
			if (StringUtils.hasLength(weightString))
				weightConcept = cs.getConceptNumeric(cs.getConcept(Integer.valueOf(weightString))
				        .getConceptId());
			String heightString = as.getGlobalProperty("concept.height");
			ConceptNumeric heightConcept = null;
			if (StringUtils.hasLength(heightString))
				heightConcept = cs.getConceptNumeric(cs.getConcept(Integer.valueOf(heightString))
				        .getConceptId());
			for (Obs obs : patientObs) {
				if (obs.getConcept().equals(weightConcept)) {
					if (latestWeight == null
					        || obs.getObsDatetime().compareTo(latestWeight.getObsDatetime()) > 0)
						latestWeight = obs;
				} else if (obs.getConcept().equals(heightConcept)) {
					if (latestHeight == null
					        || obs.getObsDatetime().compareTo(latestHeight.getObsDatetime()) > 0)
						latestHeight = obs;
				}
			}
			if (latestWeight != null && latestHeight != null) {
				double weightInKg;
				double heightInM;
				if (weightConcept.getUnits().equals("kg"))
					weightInKg = latestWeight.getValueNumeric();
				else if (weightConcept.getUnits().equals("lb"))
					weightInKg = latestWeight.getValueNumeric() * 0.45359237;
				else
					throw new IllegalArgumentException("Can't handle units of weight concept: "
					        + weightConcept.getUnits());
				if (heightConcept.getUnits().equals("cm"))
					heightInM = latestHeight.getValueNumeric() / 100;
				else if (heightConcept.getUnits().equals("m"))
					heightInM = latestHeight.getValueNumeric();
				else if (heightConcept.getUnits().equals("in"))
					heightInM = latestHeight.getValueNumeric() * 0.0254;
				else
					throw new IllegalArgumentException("Can't handle units of height concept: "
					        + heightConcept.getUnits());
				double bmi = weightInKg / (heightInM * heightInM);
				String temp = "" + bmi;
				bmiAsString = temp.substring(0, temp.indexOf('.') + 2);
			}
		}
		catch (Exception ex) {
			ex.printStackTrace();
			return "";
		}
		return bmiAsString;
	}

	
	/**
	 * hack to best approximate real-time extension of a visit by adding another encounter, or if encounter being added to visit is back-entry.
	 * I'm giving an 8 hour window to extend a visit.
	 * @param V
	 */
	private static Date getEncounterDatetimeFromVisit(Visit v){
		Date maxDateInVisit = v.getStartDatetime();
		try{
			for (Encounter e : v.getEncounters()){
				if (!e.isVoided() && e.getEncounterDatetime().getTime() > maxDateInVisit.getTime())
					maxDateInVisit = e.getEncounterDatetime();
			}
		} catch (Exception ex){
			//pass
		}
		
		Calendar visit = Calendar.getInstance();
		visit.setTime(maxDateInVisit);
		visit.add(Calendar.HOUR_OF_DAY, 8);//this is a guess
		
		//either return now if encounter is happening within 8 hours of max event, or return max event
		if (visit.getTime().getTime() < (new Date()).getTime()) //its back entry
			return visit.getTime();
		else 
			return new Date();
	}
	
    /**
     * utility to build the diagnosis/findings encounter
     */
    public static Encounter buildEncounter(Patient patient, EncounterType encType, Visit visit){
    		Encounter encounter = new Encounter();
            encounter.setPatient(patient);
            encounter.setEncounterDatetime(getEncounterDatetimeFromVisit(visit));
            encounter.setEncounterType(encType);
            
            String locStr = Context.getAuthenticatedUser().getUserProperty(OpenmrsConstants.USER_PROPERTY_DEFAULT_LOCATION);
            Location userLocation = null;
            try { 
                userLocation = Context.getLocationService().getLocation(Integer.valueOf(locStr));
            } catch (Exception ex){
                //pass
            }
            encounter.setLocation(userLocation);
            encounter.setProvider(Context.getAuthenticatedUser().getPerson()); //TODO: fix this

    		return encounter;
    }
    
    /**
     * util to instantiate new obs:
     */
    public static Obs buildObs(Patient p, Concept concept, Date obsDatetime, Concept answer, String value, Double valueNumeric, Location location){
    	Obs ret = new Obs();
    	ret.setConcept(concept);
    	ret.setCreator(Context.getAuthenticatedUser());
    	ret.setDateCreated(new Date());
    	ret.setLocation(location);
    	ret.setObsDatetime(obsDatetime);
    	ret.setPerson(p);
    	if (answer != null)
    		ret.setValueCoded(answer);
    	if (value != null && !value.equals(""))
    		ret.setValueText(value);
    	if (valueNumeric != null )
    		ret.setValueNumeric(valueNumeric);
    	return ret;
    }
    
    /**
     * util to build a new order
     */
    public static Order buildOrder(Patient p, Concept c, Encounter e){
    	Order order = new Order();
    	order.setConcept(c);
    	order.setCreator(Context.getAuthenticatedUser());
    	order.setDateCreated(new Date());
    	order.setDiscontinued(false);
    	order.setOrderer(Context.getAuthenticatedUser());
    	order.setOrderType(getLabOrderType());
    	order.setPatient(p);
    	order.setStartDate(e.getEncounterDatetime());
    	order.setVoided(false);
    	return order;
    }
    
    /**
     * pull the most recent encounter out of a visit by encounterType
     * @param v
     * @param et
     * @return
     */
    public static Encounter findEncounterByTypeInVisit(Visit v, EncounterType et){
    	if (et == null)
    		return null;
    	List<Encounter> eList = new ArrayList<Encounter>();
    	for (Encounter e: v.getEncounters()){
    		if (!e.isVoided() && et.equals(e.getEncounterType()))
    			eList.add(e);
    	}
    	if (eList.size() == 0)
    		return null;
    	Collections.sort(eList, new Comparator<Encounter>(){
			@Override
			public int compare(Encounter o1, Encounter o2) {
				return o2.getEncounterDatetime().compareTo(o1.getEncounterDatetime()); //this is supposed to be chronological desc
			}
    	});
    	return eList.get(eList.size()-1);
    }

	/**
     * Auto generated method comment
     * 
     * @param diagnosisConcepts
     * @return
     */
    public static List<ConceptName> convertToAutoComplete(List<ConceptSearchResult> diagnosisConcepts) {
    	
		List<ConceptName> ret = new ArrayList<ConceptName>();
		if (diagnosisConcepts != null){
			for (ConceptSearchResult c: diagnosisConcepts){
				for(ConceptName cn: c.getConcept().getNames())
				{
					ret.add(cn);
				}
			}
		}
		
		Collections.sort(ret, new Comparator<ConceptName>() {

			@Override
            public int compare(ConceptName o1, ConceptName o2) {
	            return o1.getName().compareTo(o2.getName());
            }
			
		});
		return ret;
    }
    
    public static List<AutoCompleteObj> convertToAutoCompleteObj(List<ConceptSearchResult> diagnosisConcepts) {
    	
    	List<AutoCompleteObj> ret = new ArrayList<AutoCompleteObj>();
		Locale currentLocale = Context.getLocale();
		if (diagnosisConcepts != null){
			for (ConceptSearchResult c: diagnosisConcepts){
				for(ConceptName cn: c.getConcept().getNames())
				{
					AutoCompleteObj o = new AutoCompleteObj();
					o.setValue(c.getConcept().getConceptId());
					
					if(cn.getLocale().equals(currentLocale))
					{
						o.setLabel(cn.getName());
					}
					else
					{
						String label = cn.getName() + "<span class='otherHit'>  &rArr; " + c.getConcept().getName(currentLocale) + "</span>";
						o.setLabel(label);
					}
					
					ret.add(o);
				}
			}
		}
		
		Collections.sort(ret, new Comparator<AutoCompleteObj>() {

			@Override
            public int compare(AutoCompleteObj o1, AutoCompleteObj o2) {
	            return o1.getLabel().compareTo(o2.getLabel());
            }
			
		});
		return ret;
    }

	/**
     * Auto generated method comment
     * 
     * @param findingsConcepts
     * @return
     */
    public static List<AutoCompleteObj> convertConceptToAutoCompleteObj(List<Concept> findingsConcepts) {
   
    	List<AutoCompleteObj> ret = new ArrayList<AutoCompleteObj>();
		Locale currentLocale = Context.getLocale();
		if (findingsConcepts != null){
			for (Concept c: findingsConcepts){
				for(ConceptName cn: c.getNames())
				{
					AutoCompleteObj o = new AutoCompleteObj();
					o.setValue(c.getConceptId());
					
					if(cn.getLocale().equals(currentLocale))
					{
						o.setLabel(cn.getName());
					}
					else
					{
						String label = cn.getName() + "<span class='otherHit'>  &rArr; " + c.getName(currentLocale) + "</span>";
						o.setLabel(label);
					}
					
					ret.add(o);
				}
			}
		}
		Collections.sort(ret, new Comparator<AutoCompleteObj>() {

			@Override
            public int compare(AutoCompleteObj o1, AutoCompleteObj o2) {
	            return o1.getLabel().compareTo(o2.getLabel());
            }
			
		});
		return ret;
    }
    
    public static List<AutoCompleteObj> getCategories(){
    	
    	List<AutoCompleteObj> categories = new ArrayList<AutoCompleteObj>();
    	
    	Concept concept = MetadataDictionary.CONCEPT_SET_ICPC_DIAGNOSIS_GROUPING_CATEGORIES;
    	
    	for(Concept c: concept.getSetMembers())
    	{
    		AutoCompleteObj obj = new AutoCompleteObj();
    		obj.setValue(c.getConceptId());
    		
    		ConceptName cn = c.getShortNameInLanguage(Context.getLocale().getLanguage());
    		if(cn == null)
    		{
    			cn = c.getName();
    		}
    		obj.setLabel(cn.getName());
    		categories.add(obj);
    	}
    	return categories;
    }
}
