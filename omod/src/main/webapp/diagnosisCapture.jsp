<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>
<%@ include file="resources/localHeader.jsp" %>

<script type="text/javascript">
<%@ include file="resources/diagnosisCapture.js" %>
</script>

<openmrs:htmlInclude file="/scripts/jquery-ui/js/jquery-ui.custom.min.js" />
<openmrs:htmlInclude file="/moduleResources/diagnosiscapturerwanda/diagnosiscapturerwanda.css" />


<br/>
<div id="errMsg" style="background-color: lightpink;"><c:if test="${more_than_one_primary_diagnosis_err != null}"><i><spring:message code="diagnosiscapturerwanda.onlyOnePrimaryDiagnosisError"/></i></c:if></div>
<div id="mainContent">
	<!--  <div><h3><spring:message code="diagnosiscapturerwanda.diagnosis.diagnoses"/> &nbsp; <openmrs:formatDate date="${visit.startDatetime}" type="short" /></h3></div>-->
	
<table><tr><td valign="top">


	<!-- here's the form -->
	<form id="diagnosisForm" method="post" >	
		<!-- form -->
		<div class="boxInner gradient">
			<div><h3><spring:message code="diagnosiscapturerwanda.addANewDiagnosis"/></h3></div>
			<table>
				<tr>
					<td colspan="4">
					<!-- todo: needs autocomplete -->
					<!--<spring:message code="diagnosiscapturerwanda.diagnosis"/>:--> 
					<span style="font-size:200%"><i><b><span id="editNote"></span><span id="diagnosisName" style="color:red;"><spring:message code="diagnosiscapturerwanda.noneSelected"/></span></i></b></span>
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
						<spring:message code="diagnosiscapturerwanda.primarySecondary"/>:
						<select name="primary_secondary" id="primarySecondarySelect">
							<option value="0" SELECTED><spring:message code="diagnosiscapturerwanda.primary"/></option>
							<option value="1"><spring:message code="diagnosiscapturerwanda.secondary"/></option>
						</select>
					</td>
					<td>
						<spring:message code="diagnosiscapturerwanda.confirmedSusptected"/>: 
						<select name="confirmed_suspected" id="confirmedSuspectedSelect">
							<option value="${concept_confirmed.id}"><spring:message code="diagnosiscapturerwanda.confirmed"/></option>
							<option value="${concept_suspected.id}" SELECTED><spring:message code="diagnosiscapturerwanda.suspected"/></option>
						</select>
					</td>
					<td>
						<spring:message code="diagnosiscapturerwanda.otherDiagnosis"/>:
					</td>
					<td><textarea rows="1" cols="50" id="diagnosisOtherTextArea" name="diagnosisOther"></textarea></td>
				</tr>
				<tr>
					<td colspan="4"><br/></td>
				</tr>
				<tr>
					<td colspan="4">
						<input type="button" class="genericButton" value='<spring:message code="general.cancel"/>' onClick="document.location.href='diagnosisCapture.list?patientId=${visit.patient.patientId}&visitId=${visit.visitId}';"/>
						&nbsp;<input type="button" class="genericButton" value='<spring:message code="diagnosiscapturerwanda.returnToPatientDashboard"/>' onclick="document.location.href='diagnosisPatientDashboard.form?patientId=${visit.patient.patientId}&visitId=${visit.visitId}';"/>
						&nbsp;<input name="action" class="genericButton" type="submit" value='<spring:message code="diagnosiscapturerwanda.submit"/>'/>
					</td>
				</tr>
			</table>
		</div>
	</form>
	
</td><td valign="top">

	<div class="boxInner gradient">
		<openmrs:portlet url="diagnosisTable" id="diagnosisTable" moduleId="diagnosiscapturerwanda" />
	</div>


</td></tr></table><br/>


	<!-- here's the diagnosis picker widget -->
	<div><h3><spring:message code="diagnosiscapturerwanda.lookupDiagnosis"/></h3></div>
	<br/>
	<div class="boxInner" style="background-color:#FAFAFA;">
		<div>
			<table>
				<tr>
				<td style="width:600px" valign=top>
					<div><spring:message code="diagnosiscapturerwanda.lookupDiagnosisByName"/>:</div>
					<div>&nbsp;</div>
					
					<div><input type="text" value="" id="ajaxDiagnosisLookup" onkeyup="ajaxLookup(this, false);" style="width:100%;"/></div>
					<div>&nbsp;</div>
					<div style="height:25px;"><hr/></div>
					<div><spring:message code="diagnosiscapturerwanda.orDiagnosisLookupBy"/>:</div>
					<div>&nbsp;</div>
					<div>
						<!-- TODO:  custom tag here for displaying concept names correctly -->
						<c:forEach items="${concept_set_body_system.setMembers}" var="member">
							<div><button onClick="filterByCategory(${member.id}, false)" class="ICPCButtonClass">${member.name}</button></div>
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
<c:if test="${!empty obsGroup}">
$j(document).ready(function() {
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
});
</c:if>
	
</script>
<br/><br/>
<%@ include file="/WEB-INF/template/footer.jsp"%>  