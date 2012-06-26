package org.openmrs.module.diagnosiscapturerwanda.queue.jsonconverter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.diagnosiscapturerwanda.MetadataDictionary;
import org.openmrs.module.diagnosiscapturerwanda.queue.DiagnosisCaptureQueueService;
import org.openmrs.module.diagnosiscapturerwanda.queue.QueueObj;
import org.openmrs.ui.framework.SimpleObject;
import org.springframework.core.convert.converter.Converter;

public class QueueSimpleObjectConverter implements Converter<Map<Integer, QueueObj>, List<SimpleObject>>{

	@Override
	public List<SimpleObject> convert(Map<Integer, QueueObj> source) {
		List<SimpleObject> ret = new ArrayList<SimpleObject>();
		for (Map.Entry<Integer, QueueObj> e : source.entrySet()){
			SimpleObject so = new SimpleObject();
			QueueObj queue =  e.getValue();
			so.put("dateCreated", Context.getDateFormat().format(queue.getDateCreated()));
			so.put("encounterUuid", queue.getEncounterUuid());	
			so.put("patientId", queue.getPatientId());
			{
				Patient p = Context.getPatientService().getPatient(queue.getPatientId());
				String id = p.getPatientIdentifier(MetadataDictionary.IDENTIFIER_TYPE_REGISTRATION).getIdentifier();
				so.put("patientIdentifier", id);
				so.put("gender", p.getGender());
				so.put("givenName" , p.getGivenName());
				so.put("familyName", p.getFamilyName());
			}	
			so.put("queueNumber", queue.getQueueNumber());
			so.put("actionKey", queue.getActionKey()); // p = processed, s=skipped, n=new
			so.put("serviceRequestedId", queue.getServiceRequestedId());
			{
				if (queue.getServiceRequestedId() != null)
					so.put("serviceName", Context.getConceptService().getConcept(Integer.valueOf(queue.getServiceRequestedId())).getName(Context.getLocale()).getName());
			}
			so.put("serviceCount", Context.getService(DiagnosisCaptureQueueService.class).countWaitingPatientsByService(queue.getServiceRequestedId()));
			ret.add(so);
		}
		return ret;
	}

}
