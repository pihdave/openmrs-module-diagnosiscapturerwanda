<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>

<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>
<%@ include file="resources/localHeader.jsp"%>

<script type="text/javascript">
<%@ include file="resources/diagnosisCapture.js" %>
</script>

<openmrs:htmlInclude file="/scripts/jquery-ui/js/jquery-ui.custom.min.js" />

<openmrs:htmlInclude file="/scripts/calendar/calendar.js" />

<style>
<%@ include file="resources/diagnosiscapturerwanda.css" %>
</style>

<c:set var="enc" value="" />
<c:forEach items="${visit.encounters}" var="encTmp">
	<c:if test="${encTmp.encounterType == labEncounterType}">
		<c:set var="enc" value="${encTmp}" />
	</c:if>
</c:forEach>
<br />
<br />
<div class="box">
	<form id="labTestForm" method="post">
		<!-- the select test pannels table -->
		<table class="labTable">
			<tr>
				<td>
					<div>
						<h3>
							<spring:message code="diagnosiscapturerwanda.orders" />
						</h3>
					</div>
				</td>
			</tr>
			<tr>
				<td><div>
						<spring:message code="diagnosiscapturerwanda.availableLabs" />
					</div>
				</td>
			</tr>
			<tr>
				<td><div>
						<c:forEach items="${supportedTests}" var="test">
			 			&nbsp;&nbsp;<span><input type="checkbox"
								name="lab_${test.id}"
								<c:if test="${!empty enc}">
					 				<c:forEach items="${enc.orders}" var="ord">
					 					<c:if test="${!ord.voided && ord.concept == test}">
					 						CHECKED
					 					</c:if>
									</c:forEach>
								</c:if>

						/>
						<openmrs:format concept="${test}" />
						</span>
						</c:forEach>
					</div>
				</td>
			</tr>
		</table>
		<br />


		<!-- each selected test panel -->

		<table class="labTable">
			<c:if test="${!empty labOrders}">
				<!-- if the lab encounter exists -->
				<tr>
					<c:forEach var="entry" items="${testMap}">
						<c:forEach items="${enc.orders}" var="ord">
							<c:if test="${!ord.voided && ord.concept == entry.key}">
								<th><openmrs:format concept="${entry.key}" /></th>
							</c:if>
						</c:forEach>
					</c:forEach>
				</tr>
				<tr>
					<c:forEach var="entry" items="${testMap}">
						<c:forEach items="${enc.orders}" var="ord">
							<c:if test="${!ord.voided && ord.concept == entry.key}">
								<td valign=top>
									<table>
										<c:forEach items="${entry.value}" var="testResult">
											<c:set var="showTestResultDate" value="true" />
											<tr>
												<td><openmrs:format concept="${testResult}" /></td>
												<td><input type="text" value="" name="testResult_${testResult.id}" /></td>
											</tr>
										</c:forEach>
										<tr>
											<td><b><spring:message code="diagnosiscapturerwanda.testResultDate" />
											</b>
											</td>
											<!-- set the date -->
											<c:set var="dateValue" value=""/>
											<c:if test="${!empty ord.discontinuedDate}">
												<c:set var="dateValue" value="${ord.discontinuedDate}"/>
											</c:if>
											<cif test="${empty dateValue && !empty now}">
												<c:set var="dateValue" value="${now}"/>
											</cif>
											<td><input type="text" name="testResultDate_${entry.key.id}" value='<openmrs:formatDate date="${dateValue}" type="long"/>' onFocus="showCalendar(this)">
											</td>
										</tr>
									</table></td>
							</c:if>
						</c:forEach>
					</c:forEach>
				</tr>
			</c:if>
		</table>
		<br />
		<div>
			&nbsp;&nbsp;<input type="button"
				value='<spring:message code="general.cancel"/>'
				onClick="document.location.href='labs.list?patientId=${visit.patient.patientId}&visitId=${visit.visitId}';" />
			&nbsp;&nbsp;<input type="button"
				value='<spring:message code="diagnosiscapturerwanda.returnToPatientDashboard"/>'
				onclick="document.location.href='diagnosisPatientDashboard.form?patientId=${visit.patient.patientId}&visitId=${visit.id}';" />
			&nbsp;&nbsp;<input name="action" type="submit"
				value='<spring:message code="diagnosiscapturerwanda.submit"/>' /> <input
				type="hidden" name="hiddenVisitId" value="${visit.id}" />
		</div>
	</form>
</div>






<%@ include file="/WEB-INF/template/footer.jsp"%>
