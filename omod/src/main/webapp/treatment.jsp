<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>
<%@ include file="resources/localHeader.jsp" %>
    
<br/><br/>
<b class="boxHeader">
    <spring:message code="diagnosiscapturerwanda.treatment"/>
</b>
<div class="box">

    <openmrs:globalProperty var="displayDrugSetIds" key="dashboard.regimen.displayDrugSetIds" defaultValue="ANTIRETROVIRAL DRUGS,TUBERCULOSIS TREATMENT DRUGS" />
    <openmrs:portlet url="patientRegimen" id="patientDashboardRegimen" patientId="${param.patientId}" parameters="displayDrugSetIds=${displayDrugSetIds}" />

</div>

<%@ include file="/WEB-INF/template/footer.jsp"%>  