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
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.api.context.Context;
import org.openmrs.module.diagnosiscapturerwanda.jsonconverter.DiagnosisCustomListConverter;
import org.openmrs.module.diagnosiscapturerwanda.jsonconverter.DiagnosisCustomOpenmrsObjectConverter;
import org.openmrs.module.diagnosiscapturerwanda.util.DiagnosisUtil;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.openmrs.test.Verifies;
import org.openmrs.ui.framework.BasicUiUtils;
import org.openmrs.ui.framework.SimpleObject;


public class DiagnosisUtilTest extends BaseModuleContextSensitiveTest {

	protected final Log log = LogFactory.getLog(getClass());
	
	
	@Before
	public void setupDatabase() throws Exception {
		executeDataSet(BaseModuleContextSensitiveTest.INITIAL_XML_DATASET_PACKAGE_PATH);
		executeDataSet(BaseModuleContextSensitiveTest.EXAMPLE_XML_DATASET_PACKAGE_PATH);
	}
	
	@Override
	public Boolean useInMemoryDatabase(){
		return true;
	}
	

	/**
	 * TODO:  if time permitting, build dbunit for global property and conceptSets...
	 * @throws Exception
	 */
//	@Test
//	@Verifies(value = "should ", method = "getLocation(String)")
//	public void metadataDictionary_shouldHaveLoadedConcepts() throws Exception {
//		MetadataDictionary.getInstance();
//		Assert.assertTrue(MetadataDictionary.CONCEPT_CONFIRMED != null);
//	}
	
	/**
	 * Here's now you return JSON for ajax queries for single openmrs objects:
	 */
	@Test
	@Verifies(value = "should convert concept to JSON", method = "DiagnosisCustomOpenmrsObjectConverter.convert")
	public void metadataDictionary_shouldConvertConcept() throws Exception {
		Concept c = Context.getConceptService().getConcept(3);
		Assert.assertTrue(c != null);
		DiagnosisCustomOpenmrsObjectConverter conv = new DiagnosisCustomOpenmrsObjectConverter();
		SimpleObject so = conv.convert(c);
		BasicUiUtils bu = new BasicUiUtils();
		Assert.assertTrue(bu.toJson(so).equals("{\"id\":3,\"name\":\"COUGH SYRUP\",\"shortName\":\"\",\"grouping\":\"\",\"category\":\"\"}"));
	}
	
	/**
	 * and, here's how you do a list of OpenmrsObjects
	 * @throws Exception
	 */
	@Test
	@Verifies(value = "should convert concept list to JSON", method = "DiagnosisCustomListConverter.convert")
	public void metadataDictionary_shouldHaveLoadedConcepts() throws Exception {
		Concept c = Context.getConceptService().getConcept(3);
		Concept c2 = Context.getConceptService().getConcept(4);
		List<Concept> list = new ArrayList<Concept>();
		list.add(c);
		list.add(c2);
		Assert.assertTrue(c != null);
		DiagnosisCustomListConverter conv = new DiagnosisCustomListConverter();
		List<SimpleObject> so = conv.convert(list);
		BasicUiUtils bu = new BasicUiUtils();
		//System.out.println(bu.toJson(so));
		Assert.assertTrue(bu.toJson(so).equals("[{\"id\":3,\"name\":\"COUGH SYRUP\",\"shortName\":\"\",\"grouping\":\"\",\"category\":\"\"},{\"id\":4,\"name\":\"CIVIL STATUS\",\"shortName\":\"\",\"grouping\":\"\",\"category\":\"\"}]"));
	}
	
	/**
	 * Here's now you return JSON for ajax queries for single openmrs objects:
	 */
	@Test
	@Verifies(value = "should convert concept to JSON", method = "DiagnosisUtil.convertToJSON")
	public void metadataDictionary_shouldConvertConceptUsingDiagnosisUtil() throws Exception {
		Concept c = Context.getConceptService().getConcept(3);
		Assert.assertTrue(DiagnosisUtil.convertToJSON(c).equals("{\"id\":3,\"name\":\"COUGH SYRUP\",\"shortName\":\"\",\"grouping\":\"\",\"category\":\"\"}"));
	}
	
	/**
	 * and, here's how you do a list of OpenmrsObjects
	 * @throws Exception
	 */
	@Test
	@Verifies(value = "should convert concept list to JSON", method = "DiagnosisUtil.convertToJSON")
	public void metadataDictionary_shouldHaveLoadedConceptsUsingDiagnosisUtil() throws Exception {
		Concept c = Context.getConceptService().getConcept(3);
		Concept c2 = Context.getConceptService().getConcept(4);
		List<Concept> list = new ArrayList<Concept>();
		list.add(c);
		list.add(c2);
		//System.out.println(bu.toJson(so));
		Assert.assertTrue(DiagnosisUtil.convertToJSON(list).equals("[{\"id\":3,\"name\":\"COUGH SYRUP\",\"shortName\":\"\",\"grouping\":\"\",\"category\":\"\"},{\"id\":4,\"name\":\"CIVIL STATUS\",\"shortName\":\"\",\"grouping\":\"\",\"category\":\"\"}]"));
	}
}
