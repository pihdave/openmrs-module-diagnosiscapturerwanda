<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>

<%@ include file="/WEB-INF/template/include.jsp"%>

<c:if test="${!readOnly}">
	<%@ include file="/WEB-INF/template/header.jsp"%>
	<%@ include file="resources/localHeader.jsp"%>
</c:if>

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
<div class="boxInner <c:if test="${!readOnly}">gradient</c:if>">
	<form id="labTestForm" method="post">
		<!-- the select test pannels table -->
		<table class="labTable">
			<tr>
				<td>
					<div <c:if test="${readOnly}">class="gradient"</c:if>>
						<h3>
							<spring:message code="diagnosiscapturerwanda.orders" />
						</h3>
					</div>
				</td>
			</tr>
			<tr>
				<td><div>
						<b><spring:message code="diagnosiscapturerwanda.availableLabs" /></b>
					</div>
				</td>
			</tr>
			<tr>
				<td><div>
						<c:forEach items="${supportedTests}" var="test">
			 			&nbsp;&nbsp;
			 			<span>
			 				<c:if test="${!readOnly}">
				 			<input type="checkbox" name="lab_${test.id}"
									<c:if test="${!empty enc}">
						 				<c:forEach items="${enc.orders}" var="ord">
						 					<c:if test="${!ord.voided && ord.concept == test}">
						 						CHECKED
						 					</c:if>
										</c:forEach>
									</c:if>
	
							/>
							</c:if>
							<c:if test="${readOnly}">
								<c:if test="${!empty enc}">
					 				<c:forEach items="${enc.orders}" var="ord">
					 					<c:if test="${!ord.voided && ord.concept == test}">
					 						<img src='<%= request.getContextPath() %>/images/checkmark.png' alt="X"/>
					 					</c:if>
									</c:forEach>
								</c:if>
							</c:if>
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
										<c:set var="showTestResultDateInReadOnlyMode" value="false"/>
										<c:forEach items="${entry.value}" var="testResult">
											<c:set var="showTestResultDate" value="true" />
											<!-- get the test value:  match concept to testresult, order to entry -->
											<c:set var="resValue" value=""/>
											<c:forEach items="${enc.allObs}" var="resObs">
												<c:if test="${resObs.voided == false && resObs.concept == testResult && resObs.order.concept == entry.key}">
													<c:set var="resValue" value="${resObs.valueNumeric}"/>
												</c:if>
											</c:forEach>
											
											<c:if test="${!readOnly || (readOnly && !empty resValue)}">	
											<tr>
												
												<td><openmrs:format concept="${testResult}" /></td>
												
												<td>
													<c:if test="${!readOnly}">
														<input type="text" name="testResult_${testResult.id}" value="${resValue}"/>
													</c:if>
													<c:if test="${readOnly}">
														<c:set var="showTestResultDateInReadOnlyMode" value="true"/>
														<span style="color:blue"><i>${resValue}</i></span>
													</c:if>
												</td>
											</tr>
											</c:if>
											
										</c:forEach>
										<c:if test="${!readOnly || (readOnly && showTestResultDateInReadOnlyMode)}">	
										<tr>
											<td><b><spring:message code="diagnosiscapturerwanda.testResultDate" /></b></td>
											<!-- set the date -->
											<c:set var="dateValue" value=""/>
											<c:if test="${!empty ord.discontinuedDate}">
												<c:set var="dateValue" value="${ord.discontinuedDate}"/>
											</c:if>
											<td>
												<c:if test="${!readOnly}">
													<input type="text" name="testResultDate_${entry.key.id}" value='<openmrs:formatDate date="${dateValue}" type="short"/>' onFocus="showCalendar(this)">
												</c:if>
												<c:if test="${readOnly}">
													<span style="color:blue"><i><b><openmrs:formatDate date="${dateValue}" type="short"/></b></i></span>
												</c:if>
											</td>
										</tr>
										</c:if>
									</table></td>
							</c:if>
						</c:forEach>
					</c:forEach>
				</tr>
			</c:if>
		</table>
		<br />
		<c:if test="${!readOnly}">
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
				<br/>
		</c:if>
	</form>
</div>





<c:if test="${!readOnly}">
<%@ include file="/WEB-INF/template/footer.jsp"%>
</c:if>
