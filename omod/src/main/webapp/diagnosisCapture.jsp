<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>
<%@ include file="resources/localHeader.jsp" %>
<style>
	<%@ include file="resources/diagnosiscapturerwanda.css" %>
</style>
<br/>
<div class="box">
	<!--  <div><h3><spring:message code="diagnosiscapturerwanda.diagnosis.diagnoses"/> &nbsp; <openmrs:formatDate date="${visit.startDatetime}" type="short" /></h3></div>-->
	<!--  TODO:  convert this to a portlet; its the same as on the patient dashboard -->
	<table>
		<tr style='background-color: whitesmoke;'>
			<th><spring:message code="diagnosiscapturerwanda.primaryDiagnosis"/></th>
			<th><spring:message code="diagnosiscapturerwanda.diagnosis"/></th>
			<th><spring:message code="diagnosiscapturerwanda.otherDiagnosis"/></th>
			<th><spring:message code="diagnosiscapturerwanda.confirmedSusptected"/></th>
		</tr>
		<c:set var="enc" value=""/>
			<c:forEach items="${visit.encounters}" var="encTmp" varStatus="pos">
				<c:if test="${encTmp.encounterType == diagnosisEncounterType}">
					<c:set var="enc" value="${encTmp}"/>
				</c:if>
			</c:forEach>
			<tr>
				<c:if test="${empty enc}">
					<td colspan="3"><spring:message code="diagnosiscapturerwanda.noDiagnosesInThisVisit"/></td>
				</c:if>
				<c:if test="${!empty enc}">
					<!-- primary diagnosis -->
					<c:forEach items="${enc.obs}" var="obs">
						<c:if test="${obs.concept == concept_set_primary_diagnosis}"><!-- the primary diagnosis conceptSet -->
							<c:set var="diagnosis" value=""/>
							<c:set var="diagnosisText" value=""/>
							<c:set var="confirmedSusptected" value=""/>
							<c:forEach items="obs.groupMembers" var="groupObs"><!--  for each set of group members -->
								<c:if test="${groupObs.concept == concept_diagnosis}">
									<c:set var="diagnosis" value="${groupObs}"/>
								</c:if>
								<c:if test="${groupObs.concept == concept_diagnosis_other}">
									<c:set var="diagnosisText" value="${groupObs}"/>
								</c:if>
								<c:if test="${groupObs.concept == concept_confirmed_suspected}">
									<c:set var="confirmedSusptected" value="${groupObs}"/>
								</c:if>
							</c:forEach>
							<c:if test="${!empty diagnosis || !empty diagnosisText }">
								<tr>
									<td><input type="checkbox" CHECKED/></td>
									<td><openmrs:format concept="${diagnosis.valueCoded}"/></td>
									<td>${diagnosisText.valueText}</td>
									<td><openmrs:format concept="${confirmedSusptected.valueCoded}"/></td>
									<td></td>
								</tr>
							</c:if>
						</c:if>
					</c:forEach>
					<!-- seconadary diagnosis -->
					<c:forEach items="${enc.obs}" var="obs">
						<c:if test="${obs.concept == concept_set_secondary_diagnosis}"><!-- the primary diagnosis conceptSet -->
							<c:set var="diagnosis" value=""/>
							<c:set var="diagnosisText" value=""/>
							<c:set var="confirmedSusptected" value=""/>
							<c:forEach items="obs.groupMembers" var="groupObs"><!--  for each set of group members -->
								<c:if test="${groupObs.concept == concept_diagnosis}">
									<c:set var="diagnosis" value="${groupObs}"/>
								</c:if>
								<c:if test="${groupObs.concept == concept_diagnosis_other}">
									<c:set var="diagnosisText" value="${groupObs}"/>
								</c:if>
								<c:if test="${groupObs.concept == concept_confirmed_suspected}">
									<c:set var="confirmedSusptected" value="${groupObs}"/>
								</c:if>
							</c:forEach>
							<c:if test="${!empty diagnosis || !empty diagnosisText }">
								<tr>
									<td></td>
									<td><openmrs:format concept="${diagnosis.valueCoded}"/></td>
									<td>${diagnosisText.valueText}</td>
									<td><openmrs:format concept="${confirmedSusptected.valueCoded}"/></td>
									<td></td>
								</tr>
							</c:if>
						</c:if>
					</c:forEach>
				</c:if>
			</tr>
	</table>
	<br/>
	<!-- here's the form -->
	<form id="diagnosisForm" method="post" >	
	<div><h3><spring:message code="diagnosiscapturerwanda.addANewDiagnosis"/></h3></div>
	
	<div>
		<!-- form -->
		<div class="box">
			<table>
				<tr>
					<td colspan="4">
					<!-- todo: needs autocomplete -->
					<!--<spring:message code="diagnosiscapturerwanda.diagnosis"/>:--> 
					<span id="diagnosisName" style="color:red;font-size:120%"><i><b><spring:message code="diagnosiscapturerwanda.noneSelected"/></i></b></span>
					<input type="hidden" name="diagnosisId" value="" />
					</td>
				</tr>
				<tr>
					<td>
						<spring:message code="diagnosiscapturerwanda.primarySecondary"/>:
						<select name="primary_secondary">
							<option val="0" SELECTED><spring:message code="diagnosiscapturerwanda.primary"/></option>
							<option val="1"><spring:message code="diagnosiscapturerwanda.secondary"/></option>
						</select>
					</td>
					<td>
						<spring:message code="diagnosiscapturerwanda.confirmedSusptected"/>: 
						<select name="primary_secondary">
							<option val="${concept_confirmed.id}"><spring:message code="diagnosiscapturerwanda.confirmed"/></option>
							<option val="${concept_suspected.id}" SELECTED><spring:message code="diagnosiscapturerwanda.suspected"/></option>
						</select>
					</td>
					<td>
						<spring:message code="diagnosiscapturerwanda.otherDiagnosis"/>:
					</td>
					<td><textarea rows="1" cols="50" name="diagnosisOther"></textarea></td>
				</tr>
				<tr>
					<td colspan="4">
						<input name="action" type="submit" value='<spring:message code="diagnosiscapturerwanda.submit"/>'/>
			      		<input type="button" value='<spring:message code="diagnosiscapturerwanda.returnToPatientDashboard"/>' onclick="document.location.href='diagnosisPatientDashboard.form?patientId=${visit.patient.patientId}';"/>
					</td>
				</tr>
			</table>
		</div>
	</div>
	</form>
	<div>&nbsp;</div>
	<div><h3><spring:message code="diagnosiscapturerwanda.lookupDiagnosis"/></h3></div>
	<div class="box">
		<div>
			<table>
				<tr>
				<td style="width:50%">
					<div><spring:message code="diagnosiscapturerwanda.lookupDiagnosisByName"/>:</div>
					<div>&nbsp;</div>
					
					<div><input type="text" value="" id="ajaxDiagnosisLookup" onkeyup="ajaxLookup(this);" style="width:100%;"/></div>
					<div>&nbsp;</div>
					
					<div><spring:message code="diagnosiscapturerwanda.orDiagnosisLookupBy"/>:</div>
					<div>&nbsp;</div>
					<div>
						<!-- TODO:  custom tag here for displaying concept names correctly -->
						<c:forEach items="${concept_set_body_system.setMembers}" var="member">
							<div><button onClick="filterByCategory(${member.id})" class="ICPCButtonClass">${member.name}</button></div>
						</c:forEach>
					</div>
				</td>
				<td>
					<div id="categorySearchResults"/>
				</td>
				</tr>
			</table>
		</div>
	</div>

</div>
    
    
    
<br/><br/>
<div>
	patient:${patient}<br/>
	visit:${visit}<br/>
	concept_set_primary_diagnosis:${concept_set_primary_diagnosis}<br/>
	concept_set_secondary_diagnosis:${concept_set_secondary_diagnosis}<br/>
	concept_primary_secondary:${concept_primary_secondary}<br/>
	concept_confirmed_suspected:${concept_confirmed_suspected}<br/>
	concept_diagnosis_other:${concept_diagnosis_other}<br/>
	concept_set_body_system:${concept_set_body_system}<br/>
	concept_set_diagnosis_classification:${concept_set_diagnosis_classification}<br/>
	encounter_type_diagnosis:${encounter_type_diagnosis}<br/>
	encounter_type_findings:${encounter_type_findings}<br/>
	

</div>    

<script type="text/javascript">

var _symptom=${concept_symptom.conceptId};
var _injury=${concept_injury.conceptId};
var _infection=${concept_infection.conceptId};
var _diagnosis=${concept_diagnosis.conceptId};

function ajaxLookup(item){
	alert(item.value);
	if (item.value.length > 2){
		$j.getJSON('getDiagnosisByNameJSON.list?searchPhrase=' + item.value, function(json) {
			alert(json);
		});
	}
}

function filterByCategory(id){
	alert('here2');
	$j.getJSON('getDiagnosesByIcpcSystemJSON.list?groupingId=' + id, function(json) {
		alert(json);
	});
}

</script>
    
<%@ include file="/WEB-INF/template/footer.jsp"%>  