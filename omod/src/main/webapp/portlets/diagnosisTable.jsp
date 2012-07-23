<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/template/include.jsp"%>
<table style="width:100%">
		<tr class="gradient">
			<th><spring:message code="diagnosiscapturerwanda.primaryDiagnosis"/></th>
			<th><spring:message code="diagnosiscapturerwanda.diagnosis"/></th>
			<th><spring:message code="diagnosiscapturerwanda.otherDiagnosis"/></th>
			<th><spring:message code="diagnosiscapturerwanda.confirmedSusptected"/></th>
		</tr>
		
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
								<td> &nbsp;&nbsp; <img src='<%= request.getContextPath() %>/images/checkmark.png' alt="X"/> </td>
								<td><c:if test="${!empty diagnosis}"><openmrs:format concept="${diagnosis.valueCoded}"/></c:if></td>
								<td><c:if test="${!empty diagnosisText}">${diagnosisText.valueText}</c:if></td>
								<td><c:if test="${!empty confirmedSusptected}"><openmrs:format concept="${confirmedSusptected.valueCoded}" withConceptNameType="SHORT"/></c:if></td>
								<td>
									&nbsp; <a href="#" onclick="editDiagnosis(${obs.id},'diagnosis');"><img src='<%= request.getContextPath() %>/images/edit.gif' alt="edit"/></a>
									&nbsp; <a href="#" onclick="deleteDiagnosis(${obs.id});"><img src='<%= request.getContextPath() %>/images/delete.gif' alt="delete" /></a>
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
								<td> &nbsp; </td>
								<td><c:if test="${!empty diagnosis}"><openmrs:format concept="${diagnosis.valueCoded}"/></c:if></td>
								<td><c:if test="${!empty diagnosisText}">${diagnosisText.valueText}</c:if></td>
								<td><c:if test="${!empty confirmedSusptected}"><openmrs:format concept="${confirmedSusptected.valueCoded}" withConceptNameType="SHORT"/></c:if></td>
								<td> 
									&nbsp; <a href="#" onclick="editDiagnosis(${obs.id}, 'diagnosis');"><img src='<%= request.getContextPath() %>/images/edit.gif' alt="edit"/></a>
									&nbsp; <a href="#" onclick="deleteDiagnosis(${obs.id});"><img src='<%= request.getContextPath() %>/images/delete.gif' alt="delete" /></a>
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