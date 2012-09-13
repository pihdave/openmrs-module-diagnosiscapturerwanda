<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/template/include.jsp"%>

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
		<c:if test="${enc.encounterType == encounter_type_findings && enc.voided == false}">
			<c:set var="enc" value="${encTmp}"/>
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
								<td><c:if test="${!empty finding}"><openmrs:format concept="${finding.valueCoded}"/></c:if>
								    <c:if test="${!empty findingText}"><i>${findingText.valueText}</i></c:if></td>
								<td align="center">
								<a href="#" onclick="deleteDiagnosis(${obs.id});"><img src='<%= request.getContextPath() %>/images/delete.gif' alt="delete" /></a>
							</td>
							</tr>
						</c:if>
					</c:if>
				</c:forEach>
			</tr>
		</c:if>
	</c:forEach>
	<c:if test="${empty encTest}">
		<td colspan="2"><spring:message code="diagnosiscapturerwanda.noFindingsInThisVisit"/></td>
	</c:if>
</table>					
	
