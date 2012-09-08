<META HTTP-EQUIV="CACHE-CONTROL" CONTENT="NO-CACHE">
<META HTTP-EQUIV="EXPIRES" CONTENT="01 Jan 1970 00:00:00 GMT">
<META HTTP-EQUIV="PRAGMA" CONTENT="NO-CACHE">
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>
    
<openmrs:require privilege="View Patients" otherwise="/login.htm" redirect="/index.htm" />    
<openmrs:htmlInclude file="/dwr/interface/DWRPatientService.js"/>
<openmrs:htmlInclude file="/scripts/jquery/dataTables/css/dataTables_jui.css"/>
<openmrs:htmlInclude file="/scripts/jquery/dataTables/js/jquery.dataTables.min.js"/>

<openmrs:htmlInclude file="/moduleResources/diagnosiscapturerwanda/diagnosiscapturerwanda.css" />

<script type="text/javascript">
	var lastSearch;
	$j(document).ready(function() {
		
	});
</script>
    
<!-- header -->    
<div>
<%@page import="org.openmrs.module.diagnosiscapturerwanda.util.DiagnosisUtil"%>   
<% org.openmrs.Location workstationLocation = DiagnosisUtil.getLocationLoggedIn(session); %>
	<div style="background-color: #f0f0f0; border: 1px black solid; padding: 5px">
		<span ><spring:message code="diagnosiscapturerwanda.hello"/> ${authenticatedUser.personName.givenName}!</span>
		<span style="float:right"><% if (workstationLocation != null) { %><spring:message code="diagnosiscapturerwanda.youAreLoggedInAt"/> <i><%= workstationLocation.getName() %></i><% } %></span>
	</div>   
</div>

<!-- capture user's current location -->
<% if (workstationLocation == null) { %>
	<br/>
	<div class="boxHeader"><spring:message code="diagnosiscapturerwanda.location" /></div>
	<div class="box">
	<form method="POST">
		<table class="locationTable">
			<tr>
				<td><strong><spring:message code="diagnosiscapturerwanda.whereAreYouLoggedIn"/></strong></td>
				<td>
					<select name="location">
						<c:forEach items="${locations}" var="location">
								<option value="${location.id}"
									<c:if test="${location.id == userLocation.id}">SELECTED</c:if>
								>${location.name}</option>										
						</c:forEach>
					</select>
				</td>
				<td>
					<input type="submit" value="<spring:message code="diagnosiscapturerwanda.submit"/>"/>
				</td>
			</tr>
		</table>
	</form>
	</div>	
<% } %>

<!-- user's location is set, so you can proceed with showing possible patients, searching for patients, service queue, etc... -->
<% if (workstationLocation != null) { %>
<br/>


<!-- here's the patient search widget -->
<openmrs:portlet id="diagnosisCapturefindPatient" url="diagnosisFindPatient"  moduleId="diagnosiscapturerwanda" parameters="size=full|postURL=${pageContext.request.contextPath}/module/diagnosiscapturerwanda/diagnosisPatientDashboard.list|showIncludeVoided=false|viewType=shortEdit|hideAddNewPatient=true"/>
<c:if test="${!empty recentVisits}">
	<div class="error" id="searchErrorRecent">
		<spring:message code="diagnosiscapturerwanda.recentVisitError" />
		<br/>
		<br/>
	</div>
</c:if>


<table id="registrationTable">
	<tr>
		<td>
			<span id="registrationButton">
				<openmrs:globalProperty var="registrationUrl" key="diagnosisCaptureRwanda.registrationSystemUrl" defaultValue=""/>
				<c:if test="${!empty registrationUrl}">
					<button class="blue" onclick="window.location = '${pageContext.request.contextPath}/${registrationUrl}'" type="button">
						<span><spring:message code="diagnosiscapturerwanda.registrationSystem" /></span>
					</button>
				</c:if>
			</span>
		</td>
		<td>	
			<c:if test="${!empty recentVisits}">
				<div id="recentVisitDiv">
				<div class="boxHeader"><spring:message code="diagnosiscapturerwanda.previousVisit" /></div>
				<div class="box">
				<c:forEach items="${recentVisits}" var="visit">
					<div class="previousVisit">
						<input type="button" value='${visit.patient.givenName} ${visit.patient.familyName} ${visit.patient.age}<spring:message code="Person.age.years" /> - <openmrs:formatDate date="${visit.startDatetime}" type="medium" />' onclick="document.location.href='diagnosisPatientDashboard.form?patientId=${visit.patient.patientId}&visitId=${visit.id}';"/>		
					</div>							
				</c:forEach>
				</div>
				</div>
			</c:if>
		</td>
</table>


			

<!--  here's the list of today's patients -->
<!-- <div>
	<span><spring:message code='diagnosiscapturerwanda.nextPatientInQueue'/></span>
</div><br/>
<div class="boxInner" id="queueDiv">
</div>
	 <script type="text/javascript">
	 $j(document).ready(function() {
		 loadPatientQueue();
	 });
	 
	 var poll;
	 
	 function runPoll() {
		 poll = setTimeout(function(){loadPatientQueue(); runPoll();}, 60000);
	 }
	 
	 function loadPatientQueue(){
		 clearTimeout(poll);
		 $j.getJSON('getJSONQueue.list', function(json) {
			 var ret = "<table class='thinBorder'>";
			 if (json == null || json.length == 0) {
				 ret = "<tr><td>&nbsp;<span> <spring:message code='diagnosiscapturerwanda.noQueueItemsToDisplay'/> </span>&nbsp;</td></tr>";
			 } else {
				 
				 //build table headers:
			     ret += "<tr style='background-color: whitesmoke;'>"		 
				 $j.each(json, function(item) {
				 	 ret += "<th>&nbsp;<span>";
					 ret += json[item]["serviceName"];
					 ret += "</span>&nbsp;</th>";
			     });
			     ret += "</tr>";
			    // this row is 'serving number XXX'
			    ret+="<tr>";
			    $j.each(json, function(item) {
			    	ret += "<td> &nbsp; <spring:message code='diagnosiscapturerwanda.nowServingNumber'/> &nbsp;&nbsp; <span style='font-size:120%'><b>" + json[item]['queueNumber'] + "</b></span> &nbsp;</td>";
			    });	
			    ret+="<tr>";
			    
			    //number of people waiting for this service
			    ret+="<tr>";
			    $j.each(json, function(item) {
			    	ret += "<td> &nbsp; <spring:message code='diagnosiscapturerwanda.numberOfPeopleWaitingForThisService'/> " + json[item]['serviceCount'] + " &nbsp;</td>";
			    });	
			    ret+="<tr>";
			    
			 	//build table rows:
			     ret += "<tr>"		 
				 $j.each(json, function(item) {
				 	 ret += "<td> &nbsp;<button class='genericButton' onClick='processQueueSelection(" + json[item]["patientId"] + ", \"" + json[item]["encounterUuid"] + "\", \"process\" )'>";
					 ret += json[item]["patientIdentifier"] + " " + json[item]["familyName"] + " " + json[item]["givenName"]+ " (" + json[item]["gender"] + ")";
					 ret += "</button> &nbsp;&nbsp; <button class='genericButton' onClick='processQueueSelection(" + json[item]["patientId"] + ", \"" + json[item]["encounterUuid"] + "\", \"skip\" )'><spring:message code='diagnosiscapturerwanda.skip'/></button></td>";
			     });
			     ret += "</tr>";	
			 }	
			 ret+="</table>";
			 $j("#queueDiv").html(ret);
			 runPoll();
		 });
	 }
	 
	 function processQueueSelection(patientId, encounterUuid, action){ 
		 if (action == "skip"){
			 var conf = confirm("<spring:message code='diagnosiscapturerwanda.areYouSureYouWantToSkipThisPatient'/>");
			 if (!conf)
				 return;
		 }	
		 $j.getJSON('processQueueItem.list?patientId=' + patientId + '&encounterUuid=' + encounterUuid + '&action=' + action, function(json){
			 if (json.result == "SUCCESS" && action == "skip"){
				 loadPatientQueue();
			 } else if (json.result == "SUCCESS" && action == "process"){
				 document.location = "${pageContext.request.contextPath}/module/diagnosiscapturerwanda/diagnosisPatientDashboard.list?patientId=" + patientId + "&encounterUuid=" + encounterUuid;
			 } else {
				 alert("Sorry!  Processing Queue failed because: " + json.reason);
			 }
		 });
	 }
	 
	 </script> -->

<% } %>   

<%@ include file="/WEB-INF/template/footer.jsp"%>  