<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>
<%@ include file="resources/localHeader.jsp" %>
<openmrs:htmlInclude file="/moduleResources/orderextension/chosen/chosen.jquery.js" />
<openmrs:htmlInclude file="/moduleResources/orderextension/chosen/chosen.css" />

<script type="text/javascript">
var _symptom=${concept_symptom.conceptId};
var _injury=${concept_injury.conceptId};
var _infection=${concept_infection.conceptId};
var _diagnosis=${concept_diagnosis.conceptId};

/**
 * this writes a document.ready function to set correct values according to encounterId request param
 */

jQuery(document).ready(function() {
	
	jQuery(".conceptCategory").hide();
	jQuery("#conceptSearchSumbit").attr("disabled", "disabled");
	jQuery("#findingsSelect").chosen({allow_single_deselect: true});
	
});
<%@ include file="resources/diagnosisCapture.js" %>
	
</script>

<openmrs:htmlInclude file="/scripts/jquery-ui/js/jquery-ui.custom.min.js" />
<openmrs:htmlInclude file="/moduleResources/diagnosiscapturerwanda/diagnosiscapturerwanda.css" />


<div class="summaryLink">
<input type="button" class='genericButton' value='<spring:message code="diagnosiscapturerwanda.returnToPatientDashboard"/>' onclick="document.location.href='diagnosisPatientDashboard.form?patientId=${patient.patientId}&visitId=${visit.visitId}';"/>
</div>

<div id="errMsg" style="background-color: lightpink;"><c:if test="${more_than_one_primary_diagnosis_err != null}"><i><spring:message code="diagnosiscapturerwanda.onlyOnePrimaryDiagnosisError"/></i></c:if></div>

<div class="boxHeader"><spring:message code="diagnosiscapturerwanda.addANewFinding"/></div>
<div class="box">
	
	<div class="boxInnerDiagnosis">
		<table class="dashboardTable">	
			<!-- Diagnoses -->
			<tr>
				<td class="dashboardHeading">
					<spring:message code="diagnosiscapturerwanda.currentFindings"/>
				</td>
			
				<td class="dashboardValue">
					<div id="diagnosisDiv">
						<table width="100%">
							<thead>
								<tr class="gradient">
									<th width="90%"><spring:message code="diagnosiscapturerwanda.finding"/></th>
									<th width="10%"></th>
								</tr>
							</thead>
								
								<c:set var="enc" value=""/>
								<!-- primary diagnosis -->
								<c:forEach items="${visit.encounters}" var="encTmp" varStatus="pos">
								<c:if test="${encTmp.encounterType == encounter_type_findings && encTmp.voided == false}">
									
									<c:set var="enc" value="${encTmp}"/>
									<c:forEach items="${encTmp.allObs}" var="obs">
										<c:if test="${obs.concept == concept_set_findings}"><!-- the findings conceptSet -->	
											<c:set var="finding" value=""/>
											<c:set var="findingText" value=""/>
											<c:forEach items="${obs.groupMembers}" var="groupObs">
												<c:if test="${groupObs.concept == concept_findings}">
													<c:set var="finding" value="${groupObs}"/>
												</c:if>
												<c:if test="${groupObs.concept == concept_findings_other}">
													<c:set var="findingText" value="${groupObs}"/>
												</c:if>
											</c:forEach>
											<c:if test="${!empty finding || !empty findingText }">
												<tr>
													<td><c:if test="${!empty finding}"><openmrs:format concept="${finding.valueCoded}"/></c:if>
													    <c:if test="${!empty findingText}"><i>${findingText.valueText}</i></c:if></td>
													<td align="center">
													<a href="#" onclick="deleteDiagnosis(${obs.id});"><img src='<%= request.getContextPath() %>/images/delete.gif' alt="delete" /></a>
												</td>
												</tr>
											</c:if>
										</c:if>
									</c:forEach>
								</c:if>
							</c:forEach>
							<c:if test="${empty enc}">
								<tr><td colspan="2"><spring:message code="diagnosiscapturerwanda.noFindingsInThisVisit"/></td></tr>
							</c:if>
						</table>
					</div>
				</td>			
			</tr>
		</table>
	</div>
	
	<form id="findingsForm" method="post" >
	<input type="hidden" name="hiddenVisitId" value="${visit.id}" />
	<input type="hidden" id="findingsId" name="findingsId" value="-1" />
		<!-- here's the diagnosis picker widget -->
	<div class="diagnosisBoxHeader"><spring:message code="diagnosiscapturerwanda.lookupFindings"/></div>
	
	<div class="diagnosisBox">
		<div><strong><spring:message code="diagnosiscapturerwanda.lookupFindingsByName"/>:</strong>
		<select name="findingsSelect" id="findingsSelect" data-placeholder="<spring:message code="diagnosiscapturerwanda.findingsPlaceholder" />" style="width:550px;" onChange="highlightFindingsSubmit()">
			<option value="" selected="selected"></option>
			<c:forEach items="${findingsConcepts}" var="findingsConcept">
				<option value="${findingsConcept.value}">${findingsConcept.label}</option>
			</c:forEach>
		</select>
		<span class="diagnosisSubmitSpan"><input id="conceptSearchSumbit" class="genericButton" type="submit" value='<spring:message code="diagnosiscapturerwanda.submit"/>'/></span>
		</div>
		<br/>
		<div><strong><spring:message code="diagnosiscapturerwanda.orFindingsLookupBy"/>:</strong></div>
		<div>
			<table width="100%">
				<tr>	
					<td width="33%" valign="top">
					<c:forEach items="${concept_set_body_system}" var="member" varStatus="status">
						<input type="button" onClick="filterByCategory(${member.value}, true)" class="ICPCButtonClass" id="${member.value}" value="${member.label}"/>
						<div class="conceptCategory" id="conceptCategory${member.value}"></div>
						<c:if test="${status.index == 6 || status.index == 13}"></td><td width="33%" valign="top"></c:if>	
					</c:forEach> 
					</td>
				</tr>
			</table>
		</div>	
		<br/>
		<div><strong><spring:message code="diagnosiscapturerwanda.otherFinding"/>:</strong><textarea rows="1" cols="50" id="findingsOther" name="findingsOther"></textarea>
		<span class="diagnosisSubmitSpan"><input name="action" id="otherFindingsSumbit" class="genericButton" type="submit" value='<spring:message code="diagnosiscapturerwanda.submit"/>'/></span></div>
	</div> 
	</br>
	</form>
	<div><input type="button" class="genericButton" value='<spring:message code="general.cancel"/>' onclick="document.location.href='diagnosisPatientDashboard.form?patientId=${patient.patientId}&visitId=${visit.visitId}';"/></div>
</div>

<%@ include file="/WEB-INF/template/footer.jsp"%>  