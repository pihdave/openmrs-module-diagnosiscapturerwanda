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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openmrs.Concept;
import org.openmrs.ConceptSet;
import org.openmrs.OpenmrsObject;
import org.openmrs.api.context.Context;
import org.openmrs.module.diagnosiscapturerwanda.MetadataDictionary;
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
			ret.put("name", c.getName().getName().toUpperCase());
			if (c.getShortNameInLocale(Context.getLocale()) != null)
				ret.put("shortName", c.getShortNameInLocale(Context.getLocale()).getName());
			else
				ret.put("shortName", "");
			//add groupings and categories to concepts if they're diagnoses.  TODO:  could definitely be optimized.
			List<ConceptSet> conceptSets = Context.getConceptService().getSetsContainingConcept(c);
			Set<Integer> firstLevelSetIds = new HashSet<Integer>();
			for (ConceptSet cs : conceptSets)
				firstLevelSetIds.add(cs.getConceptSet().getConceptId());
			
			ret.put("grouping", "");
			boolean found = false;
			for (Concept cChapter : MetadataDictionary.CONCEPT_SET_ICPC_DIAGNOSIS_GROUPING_CATEGORIES.getSetMembers()){
				if (firstLevelSetIds.contains(cChapter.getConceptId())){
					//for lazy loading:
					//cChapter = Context.getConceptService().getConcept(cChapter.getConceptId());
					for (Concept cInner: cChapter.getSetMembers()){
						if (cInner.getConceptId().equals(c.getConceptId())){
							ret.put("grouping", cChapter.getConceptId());
							found = true;
							break;
						}	
					}
					if (found)
						break;
				}
			}

			ret.put("category", "");
			found = false;
			for (Concept cCategory : MetadataDictionary.CONCEPT_SET_ICPC_SYMPTOM_INFECTION_INJURY_DIAGNOSIS.getSetMembers()){
				if (firstLevelSetIds.contains(cCategory.getConceptId())){
					//for lazy loading:
					//cCategory = Context.getConceptService().getConcept(cCategory.getConceptId());
					for (Concept cInner: cCategory.getSetMembers()){
						if (cInner.getConceptId().equals(c.getConceptId())){
							ret.put("category", cCategory.getConceptId());
							found = true;
							break;
						}	
					}
					if (found)
						break;
				}
			}
			
		} else throw new RuntimeException("DiagnosisCustomOpenmrsObjectConverter only supports Concepts currently");
		return ret;
	}
	
}
