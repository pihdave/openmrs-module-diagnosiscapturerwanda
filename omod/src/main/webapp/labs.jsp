<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>
<%@ include file="resources/localHeader.jsp" %>

<script type="text/javascript">
<%@ include file="resources/diagnosisCapture.js" %>
</script>

<openmrs:htmlInclude file="/scripts/jquery-ui/js/jquery-ui.custom.min.js" />

<style>
	<%@ include file="resources/diagnosiscapturerwanda.css" %>
</style>

<br/><br/>    
<div class="box">
	 <form id="labTestForm" method="post" >
		 <table class="labTable">
			<tr><td> <div><h3> <spring:message code="diagnosiscapturerwanda.orders" /></h3></div></td></tr>
			 <tr><td><div><spring:message code="diagnosiscapturerwanda.availableLabs" /></div></td></tr>
			 <tr><td><div>
			 	<c:forEach items="${supportedTests}" var="test">
			 		&nbsp;&nbsp;<span><input type="checkbox" name="lab_${test.id}"/> <openmrs:format concept="${test}"/></span>
			 	</c:forEach>
			 </div></td></tr>
			 <tr><td><div>
					&nbsp;&nbsp;<input type="button" value='<spring:message code="general.cancel"/>' onClick="document.location.href='labs.list?patientId=${visit.patient.patientId}&visitId=${visit.visitId}';"/>
					&nbsp;&nbsp;<input type="button" value='<spring:message code="diagnosiscapturerwanda.returnToPatientDashboard"/>' onclick="document.location.href='diagnosisPatientDashboard.form?patientId=${visit.patient.patientId}&visitId=${visit.id}';"/>
					&nbsp;&nbsp;<input name="action" type="submit" value='<spring:message code="diagnosiscapturerwanda.submit"/>'/>
					<input type="hidden" name="hiddenVisitId" value="${visit.id}" />
			 </div></td></tr>
		 </table>
	 </form>
</div>    
    
    
    
    
    
    
<%@ include file="/WEB-INF/template/footer.jsp"%>    