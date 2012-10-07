<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>
<%@ include file="resources/localHeader.jsp" %>
<openmrs:htmlInclude file="/moduleResources/diagnosiscapturerwanda/diagnosiscapturerwanda.css" />

<div class="summaryLink">
<input type="button" class='genericButton' value='<spring:message code="diagnosiscapturerwanda.returnToPatientDashboard"/>' onclick="document.location.href='diagnosisPatientDashboard.form?patientId=${patient.patientId}&visitId=${visit.visitId}';"/>
</div>

<div class="boxInner gradient">
	<div class="boxHeader">
    	<spring:message code="diagnosiscapturerwanda.vitals"/>
	</div>
	<div class="box">
    <form:form commandName="vitalsCommand" method="post">
        <table class="vitalsForm">
            <tr>
                <td>
                    <spring:message code="diagnosiscapturerwanda.temperature"/>
                </td>
                <td>
                    <form:input path="values['temperature']" size="10"/>
                    <form:errors path="values['temperature']" cssClass="error"/>
                    <spring:message code="diagnosiscapturerwanda.temperatureUnits"/>
                </td>
            </tr> 
            <tr>
                <td>
                    <spring:message code="diagnosiscapturerwanda.height"/>
                </td>
                <td>
                    <form:input path="values['height']" size="10"/>
                    <form:errors path="values['height']" cssClass="error"/>
                    <spring:message code="diagnosiscapturerwanda.heightUnits"/>
                </td>
            </tr>
             <tr>
                <td>
                    <spring:message code="diagnosiscapturerwanda.weight"/>
                </td>
                <td>
                    <form:input path="values['weight']" size="10"/>
                    <form:errors path="values['weight']" cssClass="error"/>
                    <spring:message code="diagnosiscapturerwanda.weightUnits"/>
                </td>
            </tr>
	        <tr>
	            <td>
	            	<spring:message code="diagnosiscapturerwanda.bloodPressure"/>
	            </td>
	            <td>
	                <form:input path="values['systolicBp']" size="10"/> / <form:input path="values['diastolicBp']" size="10"/>
	                <form:errors path="values['systolicBp']" cssClass="error"/>
                    <form:errors path="values['diastolicBp']" cssClass="error"/>
                    <spring:message code="diagnosiscapturerwanda.bloodPressureUnits"/>
                </td>
	        </tr>
		    <tr id="buttonsAtBottom">
		        <td colspan="2">
		        	<input name="action" class="genericButton" type="submit" value='<spring:message code="diagnosiscapturerwanda.submit"/>'/>
		        	<input type="button" class="genericButton" value='<spring:message code="general.cancel"/>' onclick="document.location.href='diagnosisPatientDashboard.form?patientId=${vitalsCommand.visit.patient.patientId}&visitId=${vitalsCommand.visit.visitId}';"/>
		        </td>
		    </tr>
	    </table>
        
    </form:form>
	</div>
</div>
</div>

<%@ include file="/WEB-INF/template/footer.jsp"%>  