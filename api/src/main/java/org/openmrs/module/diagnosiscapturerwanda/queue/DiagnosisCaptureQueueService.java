package org.openmrs.module.diagnosiscapturerwanda.queue;

import java.util.List;
import java.util.Map;

import org.openmrs.api.OpenmrsService;


public interface DiagnosisCaptureQueueService extends OpenmrsService {
	
	/**
	 * Use this method to add QueueObjects to the queue
	 * @param obj
	 */
	public void addToQueue(QueueObj obj);
	
	/**
	 * lookup the next queue object by service
	 * @param openmrsServiceId
	 * @return
	 */
	public QueueObj pollNextNewQueueObj(Integer openmrsServiceId);
	
	/**
	 * get a list of new queue objects by service 
	 * @param openmrsServiceId
	 * @return
	 */
	public List<QueueObj> getListOfSkippedQueueObjsByService(Integer openmrsServiceId);
	
	/**
	 * get a list of new queue objects by service
	 * @param openmrsServiceId
	 * @return
	 */
	public List<QueueObj> getListOfNewQueueObjsByService(Integer openmrsServiceId);

	/**
	 * This method returns a map of service_request_id : queue objects in order.
	 * @return
	 */
	public Map<Integer, QueueObj> getServiceQueueMap();

	/**
	 * This method returns a map of service_requested_id : skipped queue objects in order
	 * @return
	 */
	public Map<Integer, QueueObj> getAllSkippedQueueItemsMap();
	
	/**
	 * removes a queue object from the queue
	 * @param encId
	 */
	public void removeFromQueue(String encUuid);
	
	/**
	 * Updates a queue item to skipped
	 * @param encId the encounter ID.
	 */
	public void skipQueueObjectByEncounterUuid(String encUuid);
	
	/**
	 * Update a queue item to processed
	 * @param encId
	 */
	public void selectQueueObjectByEncounterUuid(String encUuid);
	

	/**
	 * gets a count of patients waiting for a given service
	 * @param serviceId
	 * @return
	 */
	public Integer countWaitingPatientsByService(Integer serviceId);

}
