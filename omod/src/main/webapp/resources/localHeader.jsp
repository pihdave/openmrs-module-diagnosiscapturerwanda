<%@ include file="/WEB-INF/template/include.jsp"%>

<openmrs:require privilege="View Patients" otherwise="/login.htm" redirect="/index.htm" />

<openmrs:globalProperty key="visits.enabled" var="visitsEnabled" />

<%-- Header showing preferred name, id, and treatment status --%>
<c:if test="${empty patientReasonForExit}">
	<div id="patientHeader" class="boxHeader">
</c:if>
<c:if test="${not empty patientReasonForExit}">
	<div id="patientHeader" class="boxHeaderRed">
</c:if>
<div id="patientHeaderPatientName">${patient.personName}</div>
<div id="patientHeaderPreferredIdentifier">
	<c:if test="${fn:length(patient.activeIdentifiers) > 0}">
		<c:forEach var="identifier" items="${patient.activeIdentifiers}"
			begin="0" end="0">
			<span class="patientHeaderPatientIdentifier"><span
				id="patientHeaderPatientIdentifierType">${identifier.identifierType.name}<openmrs:extensionPoint
						pointId="org.openmrs.patientDashboard.afterPatientHeaderPatientIdentifierType"
						type="html"
						parameters="identifierLocation=${identifier.location.name}" />:
			</span> ${identifier.identifier}</span>
		</c:forEach>
	</c:if>
</div>
<table id="patientHeaderGeneralInfo">
	<tr class="patientHeaderGeneralInfoRow">
		<td id="patientHeaderPatientGender"><c:if
				test="${patient.gender == 'M'}">
				<img src="${pageContext.request.contextPath}/images/male.gif"
					alt='<spring:message code="Person.gender.male"/>'
					id="maleGenderIcon" />
			</c:if> <c:if test="${patient.gender == 'F'}">
				<img src="${pageContext.request.contextPath}/images/female.gif"
					alt='<spring:message code="Person.gender.female"/>'
					id="femaleGenderIcon" />
			</c:if></td>
		<td id="patientHeaderPatientAge"><openmrs:extensionPoint
				pointId="org.openmrs.patientDashboard.beforePatientHeaderPatientAge"
				type="html" parameters="patientId=${patient.patientId}" /> <c:if
				test="${patient.age > 0}">${patient.age} <spring:message
					code="Person.age.years" />
			</c:if> <c:if test="${patient.age == 0}">< 1 <spring:message
					code="Person.age.year" />
			</c:if> <span id="patientHeaderPatientBirthdate"><c:if
					test="${not empty patient.birthdate}">(<c:if
						test="${patient.birthdateEstimated}">~</c:if>
					<openmrs:formatDate date="${patient.birthdate}" type="medium" />)</c:if>
				<c:if test="${empty patient.birthdate}">
					<spring:message code="Person.age.unknown" />
				</c:if></span></td>

		<%-- Display selected person attributes from the manage person attributes page --%>
		<openmrs:forEachDisplayAttributeType personType="patient"
			displayType="header" var="attrType">
			<td class="patientHeaderPersonAttribute"><spring:message
					code="PersonAttributeType.${fn:replace(attrType.name, ' ', '')}"
					text="${attrType.name}" />: <b>${patient.attributeMap[attrType.name]}</b>
			</td>
		</openmrs:forEachDisplayAttributeType>

		<td style="width: 100%;" class="patientHeaderEmptyData">&nbsp;</td>
		<td id="patientHeaderOtherIdentifiers"><c:if
				test="${fn:length(patient.activeIdentifiers) > 1}">
				<c:forEach var="identifier"
					items="${patient.activeIdentifiers}" begin="1" end="1">
					<span class="patientHeaderPatientIdentifier">${identifier.identifierType.name}<openmrs:extensionPoint
							pointId="org.openmrs.patientDashboard.afterPatientHeaderPatientIdentifierType"
							type="html"
							parameters="identifierLocation=${identifier.location.name}" />:
						${identifier.identifier}
					</span>
				</c:forEach>
			</c:if> <c:if test="${fn:length(patient.activeIdentifiers) > 2}">
				<div id="patientHeaderMoreIdentifiers">
					<c:forEach var="identifier"
						items="${patient.activeIdentifiers}" begin="2">
						<span class="patientHeaderPatientIdentifier">${identifier.identifierType.name}<openmrs:extensionPoint
								pointId="org.openmrs.patientDashboard.afterPatientHeaderPatientIdentifierType"
								type="html"
								parameters="identifierLocation=${identifier.location.name}" />:
							${identifier.identifier}
						</span>
					</c:forEach>
				</div>
			</c:if></td>
		<c:if test="${fn:length(patient.activeIdentifiers) > 2}">
			<td width="32" class="patientHeaderShowMoreIdentifiersData"><small><a
					id="patientHeaderShowMoreIdentifiers"
					onclick="return showMoreIdentifiers()"
					title='<spring:message code="patientDashboard.showMoreIdentifers"/>'><spring:message
							code="general.nMore"
							arguments="${fn:length(patient.activeIdentifiers) - 2}" /></a></small>
			</td>
		</c:if>
	</tr>
</table>
</div>

<openmrs:globalProperty var="programIdsToShow"
	key="dashboard.header.programs_to_show" listSeparator="," />
<%--
			Clever(?) hack: because there's no JSTL function for array membership I'm going to add a comma before
			and after the already-comma-separated list, so I can search for the substring ",ID,"
		--%>
<openmrs:globalProperty var="workflowsToShow"
	key="dashboard.header.workflows_to_show" />
<c:set var="workflowsToShow" value=",${workflowsToShow}," />



<script type="text/javascript">
	function showMoreIdentifiers() {
		if (identifierElement.style.display == '') {
			linkElement.innerHTML = '<spring:message code="general.nMore" arguments="${fn:length(patient.activeIdentifiers) - 2}"/>';
			identifierElement.style.display = "none";
		} else {
			linkElement.innerHTML = '<spring:message code="general.nLess" arguments="${fn:length(patient.activeIdentifiers) - 2}"/>';
			identifierElement.style.display = "";
		}
	}

	var identifierElement = document
			.getElementById("patientHeaderMoreIdentifiers");
	var linkElement = document
			.getElementById("patientHeaderShowMoreIdentifiers");
	if (identifierElement)
		identifierElement.style.display = "none";
</script>
