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
package org.openmrs.module.diagnosiscapturerwanda.queue;

import java.util.Date;

public class QueueObj {
	
	private Date dateCreated;
	private Integer patientId;
	private String encounterUuid; //this needs to be uuid because Encounter constructor sets uuid, but id is still empty in AOP around advice.
	private String actionKey;  //n = new queue item, p = patient seen, s = patient skipped
	private Integer serviceRequestedId;
	private Integer queueNumber; //this is the number you'd give a patient after registering on a piece of paper.
	
	//keys representing possible actions.  there could be more i suppose...
	public static final String NEW = "n";
	public static final String PROCESSED = "p";
	public static final String SKIPPED = "s";
	
	public QueueObj(){
		this.setActionNew();
		this.setDateCreated(new Date());
	}
	
	private QueueObj(String action){
		this.actionKey = action;
	}
	
	public Date getDateCreated() {
		return dateCreated;
	}
	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}
	public Integer getPatientId() {
		return patientId;
	}
	public void setPatientId(Integer patientId) {
		this.patientId = patientId;
	}
	public Integer getServiceRequestedId() {
		return serviceRequestedId;
	}
	public void setServiceRequestedId(Integer serviceRequestedId) {
		this.serviceRequestedId = serviceRequestedId;
	}
	public Integer getQueueNumber() {
		return queueNumber;
	}
	public void setQueueNumber(Integer queueNumber) {
		this.queueNumber = queueNumber;
	}
	public String getEncounterUuid() {
		return encounterUuid;
	}
	public void setEncounterUuid(String encounterUuid) {
		this.encounterUuid = encounterUuid;
	}
	public String getActionKey(){
		return this.actionKey;
	}
	public void setActionProcessed(){
		this.actionKey = QueueObj.PROCESSED;
	}
	public void setActionSkipped(){
		this.actionKey = QueueObj.SKIPPED;
	}
	public void setActionNew(){
		this.actionKey = QueueObj.NEW;
	}
	
	public QueueObj copy(){
		QueueObj ret = new QueueObj(this.getActionKey());
		ret.setDateCreated(this.getDateCreated());
		ret.setEncounterUuid(this.getEncounterUuid());
		ret.setPatientId(this.getPatientId());
		ret.setQueueNumber(this.getQueueNumber());
		ret.setServiceRequestedId(this.getServiceRequestedId());
		return ret;
	}
	
	@Override
	public boolean equals(Object o){
		if (o instanceof QueueObj == false)
			return false;
		else {
			QueueObj tmp = (QueueObj) o;
			if (tmp.getEncounterUuid().equals(this.getEncounterUuid()))
				return true;
		}
		return false;
	}
}
