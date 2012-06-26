package org.openmrs.module.diagnosiscapturerwanda.queue;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.openmrs.Encounter;
import org.openmrs.api.context.Context;
import org.openmrs.module.diagnosiscapturerwanda.MetadataDictionary;

/**
 * Used to hold queue object.
 * Queue is just a list, rather than LinkedList or Queue because of service breakdown (we're actually supporting n lists)
 * Remove Queue items after 24 hours...
 * 
 * @author dthomas
 *
 */
public class InMemoryDiagnosisCaptureQueueServiceImpl implements DiagnosisCaptureQueueService {

	private static List<QueueObj> queue = new ArrayList<QueueObj>();
	private static int queueNumber = -1;
	
	/**
	 * add a registration encounter to the queue if new, or verify that its correct otherwise.
	 */
	@Override
	public synchronized void addToQueue(QueueObj obj){
		cleanupOldQueueItems();
		if (!queue.contains(obj)){
			obj.setQueueNumber(getNextQueueNumber());
			queue.add(obj); //appends to end of list
		} else {
			validateServiceRequested(obj); //make sure service requested hasn't been edited.
		}
	}
	
	/**
	 * make sure the service requested hasn't been edited...
	 * @param obj
	 */
	private static void validateServiceRequested(QueueObj newObj){
		int pos = -1;
		boolean found = false;
		for (QueueObj o : queue){
			pos++;
			if (o.getEncounterUuid().equals(newObj.getEncounterUuid()) && !o.getServiceRequestedId().equals(newObj.getServiceRequestedId())){
				found = true;
				break;
			}
		}
		if (pos >= 0 && found){
			QueueObj o = queue.remove(pos);
			o.setActionProcessed();
			queue.add(pos, newObj);
		}
	}
	
	@Override
	public synchronized QueueObj pollNextNewQueueObj(Integer openmrsServiceId){
		if (queue.size() == 0)
			return null;
		else {
			for (QueueObj o : queue){
				if (o.getServiceRequestedId().equals(openmrsServiceId)
						&& o.getActionKey().equals(QueueObj.NEW)) //null means not skippped or processed
					return o.copy();
			}
		}
		return null;
	}
	
	@Override
	public List<QueueObj> getListOfSkippedQueueObjsByService(Integer openmrsServiceId){
		List<QueueObj> ret = new ArrayList<QueueObj>();
		for (QueueObj o : queue){
			if (o.getActionKey().equals(QueueObj.SKIPPED) 
					&& o.getServiceRequestedId().equals(openmrsServiceId))
			ret.add(o.copy());
		}
		return ret;
	}
	
	@Override
	public List<QueueObj> getListOfNewQueueObjsByService(Integer openmrsServiceId){
		List<QueueObj> ret = new ArrayList<QueueObj>();
		for (QueueObj o : queue){
			if (o.getActionKey().equals(QueueObj.NEW) //null means new
					&& o.getServiceRequestedId().equals(openmrsServiceId))
			ret.add(o.copy());
		}
		return ret;
	}
	
	/**
	 * This is the method you want to use to display the queue on the homepage.
	 * @return
	 */
	@Override
	public  Map<Integer, QueueObj> getServiceQueueMap(){
		Map<Integer, QueueObj> ret = new TreeMap<Integer, QueueObj>();
		for (QueueObj o : queue){
			if (o.getActionKey().equals(QueueObj.NEW) && !ret.containsKey(o.getServiceRequestedId())) //see == null means not processed or skipped
				ret.put(o.getServiceRequestedId(), o.copy());
		}
		return ret;
	}
	
	/**
	 * 
	 */
	@Override
	public Integer countWaitingPatientsByService(Integer serviceRequestedId){
		Integer ret = 0;
		for (QueueObj o : queue){
			if (o.getActionKey().equals(QueueObj.NEW) && o.getServiceRequestedId().equals(serviceRequestedId)){
				ret++;
			}
		}	
		return ret;
	}
	
	
	
	/**
	 * This is the method you want to use to display the queue on the homepage.
	 * @return
	 */
	@Override
	public synchronized Map<Integer, QueueObj> getAllSkippedQueueItemsMap(){
		Map<Integer, QueueObj> ret = new TreeMap<Integer, QueueObj>();
		for (QueueObj o : queue){
			if (o.getActionKey().equals(QueueObj.SKIPPED) && !ret.containsKey(o.getServiceRequestedId())) //see == null means not processed or skipped
				ret.put(o.getServiceRequestedId(), o.copy());
		}
		return ret;
	}
	
	@Override
	public synchronized void removeFromQueue(String encUuid){
		cleanupOldQueueItems();
		for (QueueObj o : queue){
			if (o.getEncounterUuid().equals(encUuid)){
				queue.remove(o);
				return;
			}	
		}
	}
	
	@Override
	public synchronized void skipQueueObjectByEncounterUuid(String encUuid){
		cleanupOldQueueItems();
		int pos = -1;
		boolean found = false;
		for (QueueObj o : queue){
			pos ++;
			if (o.getEncounterUuid().equals(encUuid)){
				found = true;
				break;
			}
		}
		if (pos >= 0 && found){
			QueueObj o = queue.remove(pos);
			o.setActionSkipped(); 
			queue.add(pos, o.copy());
		}
	}
	
	@Override
	public synchronized void selectQueueObjectByEncounterUuid(String encUuid) {
		cleanupOldQueueItems();
		int pos = -1;
		boolean found = false;
		for (QueueObj o : queue){
			pos++;
			if (o.getEncounterUuid().equals(encUuid)){
				found = true;
				break;
			}
		}
		if (pos >= 0 && found){
			QueueObj o = queue.remove(pos);
			o.setActionProcessed();
			queue.add(pos, o.copy());
		}
	}
	
	/**
	 * Increments the running queue number counter
	 * Initial value is number of registration encounters today (for server restarts in 'regular' work hours...)
	 */
	private synchronized int getNextQueueNumber(){
		if (queueNumber == -1){ //just initialized
			
			Calendar cal = Calendar.getInstance();
			cal.setTime(new Date());
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			
			List<Encounter> e = Context.getEncounterService().getEncounters(null, null, cal.getTime(), new Date(),
			        null, Collections.singletonList(MetadataDictionary.ENCOUNTER_TYPE_REGISTRATION), null,
			        null, null, false);
			if (e != null && e.size() > 0)
				queueNumber = e.size();
			else
				queueNumber = 1;
		} else if (queueNumber >= 999) { //wrap around
			queueNumber = 1;
		} else {
			queueNumber++; //regular increment
		}
		return queueNumber;
	}
	
	
	@Override
	public void onShutdown() {}

	@Override
	public void onStartup() {}
	
	/**
	 * this cleans out old queue items. I'm removing processed and skippped queue items.  'new' items need to get cleared by hand.
	 * for queue length of all items, this routine removes the first item, examines it, and the decides to add it to the end.
	 * at the end of the loop, the first item pulled should have returned to position[0]
	 * 
	 * TODO: scheduled task?
	 * 
	 */
	private synchronized void cleanupOldQueueItems(){
		for (int i = 0; i < queue.size(); i++){
			QueueObj o = queue.remove(0);
			if (!o.getActionKey().equals(QueueObj.NEW) && o.getDateCreated().getTime() < ((new Date()).getTime() - 1000*60*60*12)) //12 hours
				continue; //don't re-add to the list
			else
				queue.add(o.copy());
		}
	}

}
