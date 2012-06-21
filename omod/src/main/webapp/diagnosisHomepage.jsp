<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>
    
<div class="box">
<%@page import="org.openmrs.module.diagnosiscapturerwanda.util.DiagnosisUtil"%>   
<% org.openmrs.Location workstationLocation = DiagnosisUtil.getLocationLoggedIn(session); %>

<div style="background-color: #f0f0f0; border: 1px black solid; padding: 5px">
	<span ><spring:message code="diagnosiscapturerwanda.hello"/> ${authenticatedUser.personName.givenName}!</span>
	<span style="position:absolute align:right">blah!<% if (workstationLocation != null) { %><spring:message code="diagnosiscapturerwanda.at"/> <i><%= workstationLocation.getName() %></i><% } %>.</span>
</div>   
</div>
    
<%@ include file="/WEB-INF/template/footer.jsp"%>  