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
package org.openmrs.module.diagnosiscapturerwanda.jsonconverter;

import org.openmrs.Concept;
import org.openmrs.OpenmrsObject;
import org.openmrs.api.context.Context;
import org.openmrs.module.diagnosiscapturerwanda.util.DiagnosisUtil;
import org.openmrs.ui.framework.SimpleObject;
import org.springframework.core.convert.converter.Converter;


/**
 * hacky converter to get SimpleObjects that are easily converted to JSON for this module
 * @see DiagnosisUtil.convertToJSON
 */
public class DiagnosisCustomOpenmrsObjectConverter implements Converter<OpenmrsObject, SimpleObject> {

	
	@Override
	public SimpleObject convert(OpenmrsObject o) {
		SimpleObject ret = new SimpleObject();
		OpenmrsObject openmrsObj = (OpenmrsObject) o;
		ret.put("id", openmrsObj.getId());
		if (o instanceof Concept){
			Concept c = (Concept) o;
			ret.put("name", c.getName(Context.getLocale()).getName());
			if (c.getShortNameInLocale(Context.getLocale()) != null)
				ret.put("shortName", c.getShortNameInLocale(Context.getLocale()).getName());
			else
				ret.put("shortName", "");
			//TODO:  add groupings and categories to concepts if they're diagnoses
			Context.getConceptService().getSetsContainingConcept(c);
			ret.put("grouping", "");
			ret.put("category", "");
		} else throw new RuntimeException("DiagnosisCustomOpenmrsObjectConverter only supports Concepts currently");
		return ret;
	}
	
}
