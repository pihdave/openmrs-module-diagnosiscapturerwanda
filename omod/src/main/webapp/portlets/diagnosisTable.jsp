<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/template/include.jsp"%>

<script type="text/javascript">

jQuery(document).ready(function() {
		
		jQuery('#editDiagnosisDialog').dialog({
			position: 'middle',
			autoOpen: false,
			modal: true,
			title: '<spring:message code="diagnosiscapturerwanda.editDiagnosis" javaScriptEscape="true"/>',
			height: 280,
			width: '50%',
			buttons: { '<spring:message code="diagnosiscapturerwanda.submit"/>': function() { sumbitEditDiagnosis(); },
					   '<spring:message code="general.cancel"/>': function() { jQuery(this).dialog("close"); }
			}
		});	
});

function sumbitEditDiagnosis() {
	
	if($j('#primarySecondarySelectEdit').attr("selectedIndex") == 0)
	{
		if($j("#primary").val() == 1)
		{
			jQuery("#editDiagnosisForm").submit();
		}
		else {
			var submit = checkForPrimaryDiagnosis("#openmrs_error_edit");
			if(submit)
			{
				jQuery("#editDiagnosisForm").submit();
			}
		}
	}
	else
	{
		jQuery("#editDiagnosisForm").submit();
	}
}
	
</script>

<table width="100%">
	<thead>
		<tr class="gradient">
			<th width="15%"><spring:message code="diagnosiscapturerwanda.primaryDiagnosis"/></th>
			<th width="45%"><spring:message code="diagnosiscapturerwanda.diagnosis"/></th>
			<th width="30%"><spring:message code="diagnosiscapturerwanda.confirmedSusptected"/></th>
			<th width="10%"></th>
		</tr>
	</thead>
		
		<c:set var="enc" value=""/>
		<!-- primary diagnosis -->
		<c:forEach items="${visit.encounters}" var="encTmp" varStatus="pos">
			<c:if test="${encTmp.encounterType == encounter_type_diagnosis && encTmp.voided == false}">
				<c:set var="enc" value="${encTmp}"/>
				<c:forEach items="${encTmp.allObs}" var="obs">
					<c:if test="${obs.concept == concept_set_primary_diagnosis}"><!-- the primary diagnosis conceptSet -->
						<c:set var="diagnosis" value=""/>
						<c:set var="diagnosisText" value=""/>
						<c:set var="confirmedSusptected" value=""/>
						<c:forEach items="${obs.groupMembers}" var="groupObs"><!--  for each set of group members -->
							<c:if test="${groupObs.concept == concept_primary_care_diagnosis}">
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
							<tr id="diagnosisRow_${enc.encounterId}">
								<td align="center"><img src='<%= request.getContextPath() %>/images/checkmark.png' alt="X"/> </td>
								<td><c:if test="${!empty diagnosis}"><openmrs:format concept="${diagnosis.valueCoded}"/></c:if>
									<c:if test="${!empty diagnosisText}"><i>${diagnosisText.valueText}</i></c:if>
								</td>
								<td><c:if test="${!empty confirmedSusptected}"><openmrs:format concept="${confirmedSusptected.valueCoded}" withConceptNameType="SHORT"/></c:if></td>
								<td align="center">
									<a href="#" onclick="editDiagnosis(${obs.id},true,${confirmedSusptected.valueCoded });"><img src='<%= request.getContextPath() %>/images/edit.gif' alt="edit"/></a>
									<a href="#" onclick="deleteDiagnosis(${obs.id});"><img src='<%= request.getContextPath() %>/images/delete.gif' alt="delete" /></a>
								</td>
							</tr>
						</c:if>
					</c:if>
				</c:forEach>
			</c:if>
		</c:forEach>		
		<!-- all seconadary diagnoses -->
		<c:forEach items="${visit.encounters}" var="encTmp" varStatus="pos">
			<c:if test="${encTmp.encounterType == encounter_type_diagnosis && encTmp.voided == false}">
				<c:set var="enc" value="${encTmp}"/>
				<c:forEach items="${encTmp.allObs}" var="obs">
					<c:if test="${obs.concept == concept_set_secondary_diagnosis}"><!-- the secondary diagnosis conceptSet -->
						<c:set var="diagnosis" value=""/>
						<c:set var="diagnosisText" value=""/>
						<c:set var="confirmedSusptected" value=""/>
						<c:forEach items="${obs.groupMembers}" var="groupObs"><!--  for each set of group members -->
							<c:if test="${groupObs.concept == concept_primary_care_diagnosis}">
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
							<tr id="diagnosisRow_${encTmp.encounterId}">
								<td></td>
								<td><c:if test="${!empty diagnosis}"><openmrs:format concept="${diagnosis.valueCoded}"/></c:if>
									<c:if test="${!empty diagnosisText}"><i>${diagnosisText.valueText}</i></c:if>
								</td>
								<td><c:if test="${!empty confirmedSusptected}"><openmrs:format concept="${confirmedSusptected.valueCoded}" withConceptNameType="SHORT"/></c:if></td>
								<td align="center"> 
									 <a href="#" onclick="editDiagnosis(${obs.id},false,${confirmedSusptected.valueCoded });"><img src='<%= request.getContextPath() %>/images/edit.gif' alt="edit"/></a>
									 <a href="#" onclick="deleteDiagnosis(${obs.id});"><img src='<%= request.getContextPath() %>/images/delete.gif' alt="delete" /></a>
								</td>
							</tr>
						</c:if>
					</c:if>
				</c:forEach>
			</c:if>
		</c:forEach>			
		<!-- no diagnosis encounters in visit -->		
		<c:if test="${empty enc}">
			<tr><td colspan="3"><spring:message code="diagnosiscapturerwanda.noDiagnosesInThisVisit"/></td></tr>
		</c:if>
	</table>
</div>	

<div id="editDiagnosisDialog">	
	<div id="openmrs_error_edit" class="openmrs_error"></div>
	<br/>
	<div class="box">
		<form id="editDiagnosisForm" name="editDiagnosisForm" method="post">
			<input type="hidden" id="obsGroupId" name="hiddenObsGroupId" value="-1"/>
			<input type="hidden" id="editDiagnosisId" name="diagnosisId" value="-1" />
			<input type="hidden" id="diagnosisTextArea" name="diagnosisOther" />
			<input type="hidden" name="hiddenVisitId" value="${visit.id}" />
			<input type="hidden" name="primary" id="primary" value="-1" />
			<table>
				<tr>
					<td>
						<spring:message code="diagnosiscapturerwanda.primarySecondary"/>:
						<select name="primary_secondary" id="primarySecondarySelectEdit">
							<option value="0" ><spring:message code="diagnosiscapturerwanda.primary"/></option>
							<option value="1"><spring:message code="diagnosiscapturerwanda.secondary"/></option>
						</select>
					</td>
					<td>
						<spring:message code="diagnosiscapturerwanda.confirmedSusptected"/>: 
						<select name="confirmed_suspected" id="confirmedSuspectedSelectEdit">
							<option value="${concept_suspected.id}" ><spring:message code="diagnosiscapturerwanda.suspected"/></option>
							<option value="${concept_confirmed.id}"><spring:message code="diagnosiscapturerwanda.confirmed"/></option>
						</select>
					</td>
				</tr>
			</table>
		</form>
	</div>
</div>