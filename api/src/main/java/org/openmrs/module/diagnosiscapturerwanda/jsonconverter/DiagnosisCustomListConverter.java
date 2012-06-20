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

import java.util.ArrayList;
import java.util.List;

import org.openmrs.OpenmrsObject;
import org.openmrs.module.diagnosiscapturerwanda.util.DiagnosisUtil;
import org.openmrs.ui.framework.SimpleObject;
import org.springframework.core.convert.converter.Converter;

/**
 * @see DiagnosisUtil.convertToJSON
 * @author dthomas
 *
 */
public class DiagnosisCustomListConverter implements Converter<List<?>, List<SimpleObject>> {
	
	@Override
	public List<SimpleObject> convert(List<?> list) {
		List<SimpleObject> ret = new ArrayList<SimpleObject>();
		DiagnosisCustomOpenmrsObjectConverter conv = new DiagnosisCustomOpenmrsObjectConverter();
		for (Object o : list){
			if (o instanceof OpenmrsObject == false){
				throw new RuntimeException("DiagnosisCustomListConverter can only handle lists of OpenmrsObjects.");
			} else {
				ret.add(conv.convert((OpenmrsObject) o));
			}
		}
		return ret;
	}
}
