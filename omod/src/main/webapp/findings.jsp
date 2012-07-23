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


<br/>
<div id="errMsg" style="background-color: lightpink;"><c:if test="${more_than_one_primary_diagnosis_err != null}"><i><spring:message code="diagnosiscapturerwanda.onlyOnePrimaryDiagnosisError"/></i></c:if></div>
<div id="mainContentDiv">
	<!--  <div><h3><spring:message code="diagnosiscapturerwanda.diagnosis.diagnoses"/> &nbsp; <openmrs:formatDate date="${visit.startDatetime}" type="short" /></h3></div>-->
	
	
<table><tr><td valign="top">


	<!-- here's the form -->
	<form id="diagnosisForm" method="post" >	
		<!-- form -->
		<div class="boxInner gradient"><div><h3><spring:message code="diagnosiscapturerwanda.addANewFinding"/></h3></div>
			<table>
				<tr>
					<td colspan="4">
					<!-- todo: needs autocomplete -->
					<!--<spring:message code="diagnosiscapturerwanda.diagnosis"/>:--> 
					<span style="font-size:200%"><i><b><span id="editNote"></span><span id="diagnosisName" style="color:red;"><spring:message code="diagnosiscapturerwanda.noneSelectedFindings"/></span></i></b></span>
					<input type="hidden" id="diagnosisId" name="diagnosisId" value="-1" />
					<input type="hidden" name="hiddenVisitId" value="${visit.id}" />
					<c:if test="${!empty obsGroup}">
						<input type="hidden" name="hiddenObsGroupId" value="${obsGroup.id}" />
					</c:if>
					</td>
				</tr>
				<tr>
					<td colspan="4"><br/></td>
				</tr>
				<tr>
					<td>
						<spring:message code="diagnosiscapturerwanda.otherFinding"/>:
					</td>
					<td><textarea rows="10" cols="150" id="diagnosisOtherTextArea" name="diagnosisOther"></textarea></td>
				</tr>
				<tr>
					<td colspan="4"><br/></td>
				</tr>
				<tr>
					<td colspan="4">
						<input type="button" value='<spring:message code="general.cancel"/>' onClick="document.location.href='findings.list?patientId=${visit.patient.patientId}&visitId=${visit.visitId}';"/>
						&nbsp;<input type="button" value='<spring:message code="diagnosiscapturerwanda.returnToPatientDashboard"/>' onclick="document.location.href='diagnosisPatientDashboard.form?patientId=${visit.patient.patientId}&visitId=${visit.visitId}';"/>
						&nbsp;<input name="action" type="submit" value='<spring:message code="diagnosiscapturerwanda.submit"/>'/>
					</td>
				</tr>
			</table>
		</div>

	</form>
	
	
</td><td valign="top">
	
	
	<div class="boxInner gradient">
	<table>
		<tr class="gradient">
			<th><spring:message code="diagnosiscapturerwanda.finding"/></th>
			<th><spring:message code="diagnosiscapturerwanda.otherFindings"/></th>
			<th></th>
		</tr>
		<c:set var="encTest" value=""/>
		<c:forEach items="${visit.encounters}" var="enc" varStatus="pos">
			<c:if test="${enc.encounterType == encounter_type_findings && enc.voided == false}">
				<c:set var="encTest" value="${enc}"/>
				<tr>
					<c:forEach items="${enc.allObs}" var="obs">
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
									<td><openmrs:format concept="${finding.valueCoded}"/></td>
									<td>${findingText.valueText}</td>
									<td>
									&nbsp; <a href="#" onclick="editDiagnosis(${obs.id}, 'findings');"><img src='<%= request.getContextPath() %>/images/edit.gif' alt="edit"/></a>
									&nbsp; <a href="#" onclick="deleteDiagnosis(${obs.id});"><img src='<%= request.getContextPath() %>/images/delete.gif' alt="delete" /></a>
								</td>
								</tr>
							</c:if>
						</c:if>
					</c:forEach>
				</tr>
			</c:if>
		</c:forEach>
		<c:if test="${empty encTest}">
			<td colspan="3"><spring:message code="diagnosiscapturerwanda.noFindingsInThisVisit"/></td>
		</c:if>
	</table>
	</div>

	
</td></tr></table><br/>
	
	
	
	
	<!-- here's the diagnosis picker widget -->
	<div><h3><spring:message code="diagnosiscapturerwanda.lookupDiagnosis"/></h3></div>
	<br/>
	<div class="boxInner gradient">
		<div>
			<table>
				<tr>
				<td style="width:600px" valign=top>
					<div><spring:message code="diagnosiscapturerwanda.lookupFindingsByName"/>:</div>
					<div>&nbsp;</div>
					
					<div><input type="text" value="" id="ajaxDiagnosisLookup" onkeyup="ajaxLookup(this, true);" style="width:100%;"/></div>
					<div>&nbsp;</div>
					<div style="height:25px;"><hr/></div>
					<div><spring:message code="diagnosiscapturerwanda.orFindingsLookupBy"/>:</div>
					<div>&nbsp;</div>
					<div>
						<!-- TODO:  custom tag here for displaying concept names correctly -->
						<c:forEach items="${concept_set_body_system.setMembers}" var="member">
							<div><button onClick="filterByCategory(${member.id}, true)" class="ICPCButtonClass">${member.name}</button></div>
						</c:forEach>
					</div>
				</td>
				<td> &nbsp;&nbsp; </td>
				<td valign=top style="width:70%">
					<div id="categorySearchResults"/>
				</td>
				</tr>
			</table>
		</div>
	</div>
</div> 

<script type="text/javascript">

var _symptom=${concept_symptom.conceptId};
var _injury=${concept_injury.conceptId};
var _infection=${concept_infection.conceptId};
var _diagnosis=${concept_diagnosis.conceptId};

/**
 * this writes a document.ready function to set correct values according to encounterId request param
 */
 /**
  * this writes a document.ready function to set correct values according to encounterId request param
  */
 <c:if test="${!empty obsGroup}">
 $j(document).ready(function() {
 		<c:forEach items="${obsGroup.groupMembers}" var="groupObs"><!--  for each set of group members -->
 			<c:if test="${groupObs.concept == concept_findings}">
 				setNewDiagnosis(${groupObs.valueCoded}, '${groupObs.valueCoded.name.name}');
 				$j("#editNote").html(' (<spring:message code="diagnosiscapturerwanda.editing"/>) ');
 			</c:if>
 			<c:if test="${groupObs.concept == concept_findings_other}">
 				$j("#diagnosisOtherTextArea").html('${groupObs.valueText}');
 			</c:if>
 		</c:forEach>	
 });
 </c:if>
	
</script>
    
    
    
    
    
<%@ include file="/WEB-INF/template/footer.jsp"%>  