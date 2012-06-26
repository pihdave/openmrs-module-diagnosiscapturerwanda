package org.openmrs.module.diagnosiscapturerwanda.queue.advice;

import java.lang.reflect.Method;

import org.aopalliance.aop.Advice;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.api.context.Context;
import org.openmrs.module.diagnosiscapturerwanda.MetadataDictionary;
import org.openmrs.module.diagnosiscapturerwanda.queue.DiagnosisCaptureQueueService;
import org.openmrs.module.diagnosiscapturerwanda.queue.QueueObj;
import org.springframework.aop.Advisor;
import org.springframework.aop.support.StaticMethodMatcherPointcutAdvisor;


public class QueueEncounterInterceptor extends StaticMethodMatcherPointcutAdvisor implements Advisor {
	
	private static final long serialVersionUID = 3333L;
	
	private Log log = LogFactory.getLog(this.getClass());
	
	public boolean matches(Method method, Class targetClass) {
        // only 'run' this advice on the getter methods
        if (method.getName().equals("saveEncounter"))
            return true;
        return false;
    }
	
	@Override
	public Advice getAdvice() {
	        return new PrintingAroundAdvice();
	    }
	 
	private class PrintingAroundAdvice implements MethodInterceptor {
	public Object invoke(MethodInvocation invocation) throws Throwable {
		
	            //log.debug("Before " + invocation.getMethod().getName() + ".");
	            try {
	            	// create the queue item
	            	Encounter e = (Encounter) invocation.getArguments()[0];
	            	DiagnosisCaptureQueueService service = Context.getService(DiagnosisCaptureQueueService.class);
	            	//scan the encounter and find the service requested concept, and set the service requested on the queue item
	            	if (e.getEncounterType().equals(MetadataDictionary.ENCOUNTER_TYPE_REGISTRATION)){
		            	for (Obs o : e.getAllObs(false)){
		            		if (o.getConcept().getConceptId().equals(MetadataDictionary.CONCEPT_SERVICE_REQUESTED.getConceptId())){
		            			QueueObj q = new QueueObj();
		    	            	q.setEncounterUuid(e.getUuid());
		    	            	q.setPatientId(e.getPatient().getPatientId());
		            			q.setServiceRequestedId(o.getValueCoded().getConceptId());
		            			service.addToQueue(q);
		            			log.info("Adding patient " + e.getPatientId() + " to in-memory queue after registration.");
		            			break;
		            		}
		            	}
	            	}
	            } catch (Exception ex){
	            	log.error("Unable to create queue item");
	            	ex.printStackTrace();
	            }
	            Object o = invocation.proceed();
	            return o;
	        }
	}
		
}
