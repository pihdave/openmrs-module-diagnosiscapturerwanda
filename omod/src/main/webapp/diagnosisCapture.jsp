<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>
<%@ include file="resources/localHeader.jsp" %>

<script type="text/javascript">
var _symptom=${concept_symptom.conceptId};
var _injury=${concept_injury.conceptId};
var _infection=${concept_infection.conceptId};
var _diagnosis=${concept_diagnosis.conceptId};

/**
 * this writes a document.ready function to set correct values according to encounterId request param
 */

$j(document).ready(function() {
	<c:if test="${!empty obsGroup}">
		<c:if test="${obsGroup.concept == concept_set_primary_diagnosis}">
			$j("#primarySecondarySelect").val(0);
		</c:if>
		<c:if test="${obsGroup.concept == concept_set_secondary_diagnosis}">
			$j("#primarySecondarySelect").val(1);
		</c:if>
		<c:forEach items="${obsGroup.groupMembers}" var="groupObs"><!--  for each set of group members -->
			<c:if test="${groupObs.concept == concept_primary_care_diagnosis && !empty groupObs.valueCoded}">
				setNewDiagnosis(${groupObs.valueCoded}, '${groupObs.valueCoded.name.name}');
				$j("#editNote").html(' (<spring:message code="diagnosiscapturerwanda.editing"/>) ');
			</c:if>
			<c:if test="${groupObs.concept == concept_diagnosis_other}">
			    $j("#editNote").html(' (<spring:message code="diagnosiscapturerwanda.editing"/>) ');
				$j("#diagnosisOtherTextArea").html('${groupObs.valueText}');
			</c:if>
			<c:if test="${groupObs.concept == concept_confirmed_suspected && !empty groupObs.valueCoded}">
				$j("#confirmedSuspectedSelect").val(${groupObs.valueCoded});
			</c:if>
		</c:forEach>
		</c:if>
		
		$j("#spinner").hide();
		$j("#conceptSearchSumbit").attr("disabled", "disabled");
});
<%@ include file="resources/diagnosisCapture.js" %>
	
</script>

<openmrs:htmlInclude file="/scripts/jquery-ui/js/jquery-ui.custom.min.js" />
<openmrs:htmlInclude file="/moduleResources/diagnosiscapturerwanda/diagnosiscapturerwanda.css" />


<div class="summaryLink">
<input type="button" class='genericButton' value='<spring:message code="diagnosiscapturerwanda.returnToPatientDashboard"/>' onclick="document.location.href='diagnosisPatientDashboard.form?patientId=${patient.patientId}&visitId=${visit.visitId}';"/>
</div>

<div id="errMsg" style="background-color: lightpink;"><c:if test="${more_than_one_primary_diagnosis_err != null}"><i><spring:message code="diagnosiscapturerwanda.onlyOnePrimaryDiagnosisError"/></i></c:if></div>

<div class="boxHeader"><spring:message code="diagnosiscapturerwanda.addANewDiagnosis"/></div>
<div class="box">
	
	<div class="boxInnerDiagnosis">
		<table class="dashboardTable">	
			<!-- Diagnoses -->
			<tr>
				<td class="dashboardHeading">
					<spring:message code="diagnosiscapturerwanda.currentDiagnosis"/>
				</td>
			
				<td class="dashboardValue">
					<div id="diagnosisDiv">
						<openmrs:portlet url="diagnosisTable" id="diagnosisTable" moduleId="diagnosiscapturerwanda" />
					</div>
				</td>			
			</tr>
		</table>
	</div>
		<!-- here's the diagnosis picker widget -->
	<div class="diagnosisBoxHeader"><spring:message code="diagnosiscapturerwanda.lookupDiagnosis"/></div>
	
	<div class="diagnosisBox">
		<div><strong><spring:message code="diagnosiscapturerwanda.lookupDiagnosisByName"/>:</strong><input type="text" value="" id="ajaxDiagnosisLookup" onkeydown="ajaxLookup(this, false);"  style="width:30%;"/>
		<img id="spinner" src="/openmrs/images/loading.gif">
		<input id="conceptSearchSumbit" class="genericButton" type="submit" value='<spring:message code="diagnosiscapturerwanda.submit"/>'/>
		<input type="hidden" name="conceptId" id="conceptId" value="">
		</div>
		<br/>
		<div><strong><spring:message code="diagnosiscapturerwanda.orDiagnosisLookupBy"/>:</strong></div>
		<div>
			<table width="100%">
				<tr>	
					<td width="33%" valign="top">
					<c:forEach items="${concept_set_body_system.setMembers}" var="member" varStatus="status">
						<input type="button" onClick="filterByCategory(${member.id}, false)" class="ICPCButtonClass" id="${member.id}" value="${member.name}"/>
						<div class="conceptCategory" id="conceptCategory${member.id}"></div>
						<c:if test="${status.index == 6 || status.index == 13}"></td><td width="33%" valign="top"></c:if>	
					</c:forEach> 
					</td>
				</tr>
			</table>
		</div>	
		<br/>
		<div><strong><spring:message code="diagnosiscapturerwanda.orOtherDiagnosis"/>:</strong><textarea rows="1" cols="50" id="diagnosisOtherTextArea" name="diagnosisOther"></textarea>
		<input name="action" class="genericButton" type="submit" value='<spring:message code="diagnosiscapturerwanda.submit"/>'/></div>
	</div> 
	</br>
	<div><input type="button" class="genericButton" value='<spring:message code="general.cancel"/>' onClick="document.location.href='diagnosisCapture.list?patientId=${visit.patient.patientId}&visitId=${visit.visitId}';"/></div>
</div>

	


<br/><br/>
<%@ include file="/WEB-INF/template/footer.jsp"%>  