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
		jQuery(".openmrs_error").hide();
		jQuery("#conceptSearchSumbit").attr("disabled", "disabled");
		jQuery("#diagnosisSelect").chosen({allow_single_deselect: true});
		
		jQuery('#conceptSearchSumbit').click(function(){ 
			jQuery('#diagnosisDialog').dialog('open');
		});
		
		jQuery('#otherDiagnosisSumbit').click(function(){ 
			jQuery('#otherDiagnosisDialog').dialog('open');
		});
		
		jQuery('#diagnosisDialog').dialog({
			position: 'middle',
			autoOpen: false,
			modal: true,
			title: '<spring:message code="diagnosiscapturerwanda.submitDiagnosis" javaScriptEscape="true"/>',
			height: 280,
			width: '50%',
			zIndex: 100,
			buttons: { '<spring:message code="diagnosiscapturerwanda.submit" />': function() { submitDiagnosis(); },
					   '<spring:message code="general.cancel" />': function() { jQuery(this).dialog("close"); }
			}
		});	
		
		jQuery('#otherDiagnosisDialog').dialog({
			position: 'middle',
			autoOpen: false,
			modal: true,
			title: '<spring:message code="diagnosiscapturerwanda.submitDiagnosis" javaScriptEscape="true"/>',
			height: 280,
			width: '50%',
			zIndex: 100,
			buttons: { '<spring:message code="diagnosiscapturerwanda.submit" />': function() { sumbitOtherDiagnosis() },
					   '<spring:message code="general.cancel" />': function() { jQuery(this).dialog("close"); }
			}
		});	
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
		<div><strong><spring:message code="diagnosiscapturerwanda.lookupDiagnosisByName"/>:</strong>
		<select name="diagnosisSelect" id="diagnosisSelect" data-placeholder="<spring:message code="diagnosiscapturerwanda.diagnosisPlaceholder" />" style="width:550px;" onChange="highlightSubmit()">
			<option value="" selected="selected"></option>
			<c:forEach items="${diagnosisConcepts}" var="diagnosisConcept">
				<option value="${diagnosisConcept.value}">${diagnosisConcept.label}</option>
			</c:forEach>
		</select>
		<span class="diagnosisSubmitSpan"><input id="conceptSearchSumbit" class="genericButton" type="submit" value='<spring:message code="diagnosiscapturerwanda.submit"/>'/></span>
		</div>
		<br/>
		<div id="legend">
		     <span> <span class='symptom_color'>&nbsp;&nbsp&nbsp;&nbsp</span> <spring:message code='diagnosiscapturerwanda.symptom'/> </span>
		    <span> <span class='infection_color'>&nbsp;&nbsp&nbsp;&nbsp</span> <spring:message code='diagnosiscapturerwanda.infection'/> </span>
			<span> <span class='injury_color'>&nbsp;&nbsp&nbsp;&nbsp</span> <spring:message code='diagnosiscapturerwanda.injury'/>  </span>
			<span> <span class='diagnosis_color'>&nbsp;&nbsp&nbsp;&nbsp</span> <spring:message code='diagnosiscapturerwanda.diagnosis'/> </span></div>
		<div><strong><spring:message code="diagnosiscapturerwanda.orDiagnosisLookupBy"/>:</strong></div>
		<div>
			
			<table width="100%">
				<tr>	
					<td width="33%" valign="top">
					<c:forEach items="${concept_set_body_system}" var="member" varStatus="status">
						<input type="button" onClick="filterByCategory(${member.value}, false)" class="ICPCButtonClass" id="${member.value}" value="${member.label}"/>
						<div class="conceptCategory" id="conceptCategory${member.value}"></div>
						<c:if test="${status.index == 6 || status.index == 13}"></td><td width="33%" valign="top"></c:if>	
					</c:forEach> 
					</td>
				</tr>
			</table>
		</div>	
		<br/>
		<div><strong><spring:message code="diagnosiscapturerwanda.orOtherDiagnosis"/>:</strong><textarea rows="1" cols="50" id="diagnosisOther" name="diagnosisOtherArea"></textarea>
		<span class="diagnosisSubmitSpan"><input name="action" id="otherDiagnosisSumbit" class="genericButton" type="submit" value='<spring:message code="diagnosiscapturerwanda.submit"/>'/></span></div>
	</div> 
	</br>
	<div><input type="button" class="genericButton" value='<spring:message code="general.cancel"/>' onclick="document.location.href='diagnosisPatientDashboard.form?patientId=${patient.patientId}&visitId=${visit.visitId}';"/></div>
</div>

<div id="diagnosisDialog">	
	<div id="openmrs_error" class="openmrs_error"></div>
	<br/>
	<div class="box">
		<form id="diagnosisForm" name="diagnosisForm" method="post">
			<input type="hidden" id="diagnosisId" name="diagnosisId" value="-1" />
			<input type="hidden" name="hiddenVisitId" value="${visit.id}" />
			<c:if test="${!empty obsGroup}">
				<input type="hidden" name="hiddenObsGroupId" value="${obsGroup.id}" />
			</c:if>
			<table>
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
				</tr>
			</table>
		</form>
	</div>
</div>

<div id="otherDiagnosisDialog">	
	<div id="openmrs_error_other" class="openmrs_error"></div>
	</br>
	<div class="box">
		<form id="otherDiagnosisForm" name="diagnosisForm" method="post">
			<input type="hidden" id="otherDiagnosisId" name="diagnosisId" value="-1" />
			<input type="hidden" id="diagnosisOtherTextArea" name="diagnosisOther" />
			<input type="hidden" name="hiddenVisitId" value="${visit.id}" />
			<c:if test="${!empty obsGroup}">
				<input type="hidden" name="hiddenObsGroupId" value="${obsGroup.id}" />
			</c:if>
			<table>
				<tr>
					<td>
						<spring:message code="diagnosiscapturerwanda.primarySecondary"/>:
						<select name="primary_secondary" id="primarySecondarySelectOther">
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
				</tr>
			</table>
		</form>
	</div>
</div>	

</div>
<%@ include file="/WEB-INF/template/footer.jsp"%>  