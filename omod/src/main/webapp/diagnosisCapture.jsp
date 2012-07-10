<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>
<%@ include file="resources/localHeader.jsp" %>
<openmrs:htmlInclude file="/scripts/jquery-ui/js/jquery-ui.custom.min.js" />
<style>
	<%@ include file="resources/diagnosiscapturerwanda.css" %>
</style>
<br/>
<div class="box">
	<!--  <div><h3><spring:message code="diagnosiscapturerwanda.diagnosis.diagnoses"/> &nbsp; <openmrs:formatDate date="${visit.startDatetime}" type="short" /></h3></div>-->
	
	
	<!--  TODO:  convert this table to a portlet; its the same as on the patient dashboard -->
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
					<span style="font-size:200%"><i><b><span id="diagnosisName" style="color:red;"><spring:message code="diagnosiscapturerwanda.noneSelected"/></span></i></b></span>
					<input type="hidden" id="diagnosisId" name="diagnosisId" value="-1" />
					<input type="hidden" name="hiddenVisitId" value="${visit.id}" />
					</td>
				</tr>
				<tr>
					<td>
						<spring:message code="diagnosiscapturerwanda.primarySecondary"/>:
						<select name="primary_secondary">
							<option value="0" SELECTED><spring:message code="diagnosiscapturerwanda.primary"/></option>
							<option value="1"><spring:message code="diagnosiscapturerwanda.secondary"/></option>
						</select>
					</td>
					<td>
						<spring:message code="diagnosiscapturerwanda.confirmedSusptected"/>: 
						<select name="confirmed_suspected">
							<option value="${concept_confirmed.id}"><spring:message code="diagnosiscapturerwanda.confirmed"/></option>
							<option value="${concept_suspected.id}" SELECTED><spring:message code="diagnosiscapturerwanda.suspected"/></option>
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
	
	
	<!-- here's the diagnosis picker widget -->
	<div><h3><spring:message code="diagnosiscapturerwanda.lookupDiagnosis"/></h3></div>
	<div class="box">
		<div>
			<table style="background-color:whitesmoke;">
				<tr>
				<td style="width:600px" valign=top>
					<div><spring:message code="diagnosiscapturerwanda.lookupDiagnosisByName"/>:</div>
					<div>&nbsp;</div>
					
					<div><input type="text" value="" id="ajaxDiagnosisLookup" onkeyup="ajaxLookup(this);" style="width:100%;"/></div>
					<div>&nbsp;</div>
					<div style="height:25px;"><hr/></div>
					<div><spring:message code="diagnosiscapturerwanda.orDiagnosisLookupBy"/>:</div>
					<div>&nbsp;</div>
					<div>
						<!-- TODO:  custom tag here for displaying concept names correctly -->
						<c:forEach items="${concept_set_body_system.setMembers}" var="member">
							<div><button onClick="filterByCategory(${member.id})" class="ICPCButtonClass">${member.name}</button></div>
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

function ajaxLookup(item){
	if (item.value.length > 2){
		
		$j.getJSON('getDiagnosisByNameJSON.list?searchPhrase=' + item.value, function(json){
			
			$j("input#ajaxDiagnosisLookup").autocomplete({
			    source: json,
			    minLength: 2,
			    select: function(event, ui) { 
			    			setNewDiagnosis(ui.item.value, ui.item.label); 
			    			$j('#categorySearchResults').empty();
			    			return false;
			    		}
			});
		});
	}
}




function filterByCategory(id){
	$j.getJSON('getDiagnosesByIcpcSystemJSON.list?groupingId=' + id, function(json) {
		 
		 //build a little legend
		 var ret = "<br/><span>  <span class='symptom_color'>&nbsp;&nbsp&nbsp;&nbsp</span>   <spring:message code='diagnosiscapturerwanda.symptom'/> </span>";
		 ret += "<span> <span class='infection_color'>&nbsp;&nbsp&nbsp;&nbsp</span>    <spring:message code='diagnosiscapturerwanda.infection'/> </span>";
		 ret += "<span> <span class='injury_color'>&nbsp;&nbsp&nbsp;&nbsp</span> <spring:message code='diagnosiscapturerwanda.injury'/>  </span>";
		 ret += "<span> <span class='diagnosis_color'>&nbsp;&nbsp&nbsp;&nbsp</span> <spring:message code='diagnosiscapturerwanda.diagnosis'/> </span><br/><br/>";
	 	 ret += "<table class='icpcResults'><tr valign=top><td>";

	 	 //TODO: reduce this, using an array
		 $j.each(json, function(item) {
		 	 if (json[item].category == _symptom){
		 		 ret+="<button class='symptom' onclick=\"javascript: setNewDiagnosis(" + json[item].id + ", \'" + json[item].name + "\');\"  >" + json[item].name + "</button><br/>";
		 	 }
	     });
		 ret+="</td><td>";
		 $j.each(json, function(item) {
		 	 if (json[item].category == _infection){
		 		 ret+="<button class='infection' onclick=\"javascript: setNewDiagnosis(" + json[item].id + ", \'" + json[item].name + "\');\" >" + json[item].name + "</button><br/>";
		 	 }
	     });
		 ret+="</td><td>";
		 $j.each(json, function(item) {
		 	 if (json[item].category == _injury){
		 		 ret+="<button class='injury' onclick=\"javascript: setNewDiagnosis(" + json[item].id + ", \'" + json[item].name + "\');\" >" + json[item].name + "</button><br/>";
		 	 }
	     });
		 ret+="</td><td>";
		 $j.each(json, function(item) {
		 	 if (json[item].category == _diagnosis){
		 		 ret+="<button class='diagnosis' onclick=\"javascript: setNewDiagnosis(" + json[item].id + ", \'" + json[item].name + "\');\" >" + json[item].name + "</button><br/>";
		 	 }
	     });
		 ret+="</td></tr></table>";
		 $j("#categorySearchResults").html(ret);
	});
}

function setNewDiagnosis(diagnosisId, diagnosisName){
	$j("#diagnosisName").html("<span style='color:blue'>" + diagnosisName + "</span>");
	$j("#diagnosisId").val(diagnosisId);
}

	
</script>
    
<%@ include file="/WEB-INF/template/footer.jsp"%>  