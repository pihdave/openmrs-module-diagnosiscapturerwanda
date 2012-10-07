<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>
<%@ include file="resources/localHeader.jsp" %>
<openmrs:htmlInclude file="/moduleResources/diagnosiscapturerwanda/diagnosiscapturerwanda.css" />

<div class="summaryLink">
<input type="button" class='genericButton' value='<spring:message code="diagnosiscapturerwanda.returnToPatientDashboard"/>' onclick="document.location.href='diagnosisPatientDashboard.form?patientId=${visit.patient.patientId}&visitId=${visit.visitId}';"/>
</div>

<div class="boxInner gradient">
	<div class="boxHeader">
    	<spring:message code="diagnosiscapturerwanda.treatment"/>
	</div>
	<div class="box">
    	<openmrs:portlet url="patientRegimen" id="patientDashboardRegimen" patientId="${param.patientId}" parameters="returnUrl=/module/diagnosiscapturerwanda/treatment.form?visitId=${visit.visitId}&visitToday=${visitToday }" />
	</div>
</div>
</div>

<%@ include file="/WEB-INF/template/footer.jsp"%>  
