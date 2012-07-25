<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>
<%@ include file="resources/localHeader.jsp" %>
    
<style>
	<%@ include file="resources/diagnosiscapturerwanda.css" %>
</style>

<br/>
<b class="boxHeader">
    <spring:message code="diagnosiscapturerwanda.treatment"/>
</b>
<div class="boxInner">

    <openmrs:globalProperty var="displayDrugSetIds" key="dashboard.regimen.displayDrugSetIds" defaultValue="ANTIRETROVIRAL DRUGS,TUBERCULOSIS TREATMENT DRUGS" />
    <openmrs:portlet url="patientRegimen" id="patientDashboardRegimen" patientId="${param.patientId}" parameters="displayDrugSetIds=${displayDrugSetIds}" />

</div>
<br/>
<div><input type="button" class='genericButton' value='<spring:message code="diagnosiscapturerwanda.returnToPatientDashboard"/>' onclick="document.location.href='diagnosisPatientDashboard.form?patientId=${visit.patient.patientId}&visitId=${visit.visitId}';"/></div>

<%@ include file="/WEB-INF/template/footer.jsp"%>  