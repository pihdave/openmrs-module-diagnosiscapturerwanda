<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>
    
<openmrs:require privilege="View Patients" otherwise="/login.htm" redirect="/index.htm" />    
    
<!-- header -->    
<div class="box">
<%@page import="org.openmrs.module.diagnosiscapturerwanda.util.DiagnosisUtil"%>   
<% org.openmrs.Location workstationLocation = DiagnosisUtil.getLocationLoggedIn(session); %>
	<div style="background-color: #f0f0f0; border: 1px black solid; padding: 5px">
		<span ><spring:message code="diagnosiscapturerwanda.hello"/> ${authenticatedUser.personName.givenName}!</span>
		<span style="float:right"><% if (workstationLocation != null) { %><spring:message code="diagnosiscapturerwanda.youAreLoggedInAt"/> <i><%= workstationLocation.getName() %></i><% } %></span>
	</div>   
</div>

<!-- capture user's current location -->
<% if (workstationLocation == null) { %>
	<form method="POST">
		<table>
			<tr>
				<td><spring:message code="diagnosiscapturerwanda.whereAreYouLoggedIn"/></td>
				<td>
					<select name="location">
						<c:forEach items="${locations}" var="location">
								<option value="${location.id}"
									<c:if test="${location.id == userLocation.id}">SELECTED</c:if>
								>${location.name}</option>										
						</c:forEach>
					</select>
				</td>
			</tr>
			<tr>
				<td colspan="2"><input type="submit" value="<spring:message code="diagnosiscapturerwanda.submit"/>"/></td>
			</tr>
		</table>
	</form>	
<% } %>

<!-- user's location is set, so you can proceed with showing possible patients, searching for patients, service queue, etc... -->
<% if (workstationLocation != null) { %>
<br/>

<!-- here's the patient search widget -->

<openmrs:portlet id="diagnosisCapturefindPatient" url="diagnosisFindPatient"  moduleId="diagnosiscapturerwanda" parameters="size=full|postURL=${pageContext.request.contextPath}/module/diagnosiscapturerwanda/diagnosisPatientDashboard.list|showIncludeVoided=false|viewType=shortEdit|hideAddNewPatient=true"/>

<br/>
<!--  here's the list of today's patients -->
<div class="box">
	Here's where seen patients are going to go:
</div>

<% } %>    
<%@ include file="/WEB-INF/template/footer.jsp"%>  