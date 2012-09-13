<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>
<%@ include file="resources/localHeader.jsp" %>
<openmrs:htmlInclude file="/moduleResources/diagnosiscapturerwanda/diagnosiscapturerwanda.css" />
<script type="text/javascript">
<%@ include file="resources/diagnosisCapture.js" %>
</script>

<openmrs:htmlInclude file="/scripts/jquery-ui/js/jquery-ui.custom.min.js" />
    
<!-- patient is not registered -->
<c:if test="${empty visit}">
	<div class="box">
		<openmrs:globalProperty var="registrationUrl" key="diagnosisCaptureRwanda.registrationSystemUrl" defaultValue=""/>
		<spring:message code="diagnosiscapturerwanda.youMustRegisterThisPatientFirst"/> &nbsp;
		<c:if test="${!empty registrationUrl}">
			&nbsp;<a href="${pageContext.request.contextPath}/${registrationUrl}"><spring:message code="diagnosiscapturerwanda.registrationSystem"/></a>
		</c:if>
	</div>	
</c:if>
<!-- patient is registered -->
<c:if test="${!empty visit}">
	
	<br/>
	<br/>
	
	<div class="boxHeader"><spring:message code="diagnosiscapturerwanda.patientSummary"/></div>
		
	
	<div class="box">
		<div class="boxInnerDiagnosis">
			<table class="dashboardTable">
				<tr>
					<td class="dashboardHeading">
						<spring:message code="diagnosiscapturerwanda.vitals"/>
					</td>
					
					<td class="dashboardValue">
						<div id="vitalsDiv">
							<table width="100%">
								<thead>
									<tr class="gradient">
										<th width="20%"><spring:message code="diagnosiscapturerwanda.temperature"/></th>
										<th width="20%"><spring:message code="diagnosiscapturerwanda.height"/></th>
										<th width="20%"><spring:message code="diagnosiscapturerwanda.weight"/></th>
										<th width="20%"><spring:message code="diagnosiscapturerwanda.bmi"/></th>
										<th width="20%"><spring:message code="diagnosiscapturerwanda.bloodPressure"/></th>
									</tr>
								</thead>
								<c:set var="enc" value=""/>
								<c:set var="tempObs" value=""/>
								<c:set var="heightObs" value=""/>
								<c:set var="weightObs" value=""/>
								<c:set var="BMI" value=""/>
								<c:set var="systolicObs" value=""/>
								<c:set var="diastolicObs" value=""/>
								<c:forEach items="${visit.encounters}" var="encTmp" varStatus="pos">
									<c:if test="${encTmp.encounterType == encounter_type_vitals && encTmp.voided == false}">
										<c:set var="enc" value="${encTmp}"/>
									</c:if>
								</c:forEach>
								
								<c:if test="${empty enc}">
									<tr><td colspan="5"><spring:message code="diagnosiscapturerwanda.noVitalsInThisVisit"/></td></tr>
								</c:if>
								<c:if test="${!empty enc}">
								<!-- setup values -->
									<c:forEach items="${enc.obs}" var="obs">
										<c:if test="${obs.concept == concept_temperature && obs.voided == false}">
											<c:set var="tempObs" value="${obs}"/>							
										</c:if>
										<c:if test="${obs.concept == concept_height && obs.voided == false}">
											<c:set var="heightObs" value="${obs}"/>					
										</c:if>
										<c:if test="${obs.concept == concept_weight && obs.voided == false}">
											<c:set var="weightObs" value="${obs}"/>					
										</c:if>
										<c:if test="${obs.concept == concept_systolic && obs.voided == false}">
											<c:set var="systolicObs" value="${obs}"/>					
										</c:if>
										<c:if test="${obs.concept == concept_diastolic && obs.voided == false}">
											<c:set var="diastolicObs" value="${obs}"/>					
										</c:if>			
									</c:forEach>
									<tr>
										<td align="center"><c:if test="${!empty tempObs}">${tempObs.valueNumeric} <spring:message code="diagnosiscapturerwanda.temperatureUnits"/></c:if></td>
										<td align="center"><c:if test="${!empty heightObs}">${heightObs.valueNumeric} <spring:message code="diagnosiscapturerwanda.heightUnits"/></c:if></td>
										<td align="center"><c:if test="${!empty weightObs}">${weightObs.valueNumeric} <spring:message code="diagnosiscapturerwanda.weightUnits"/></c:if></td>
										<td align="center">${currentBMI}</td>
										<td align="center"><c:if test="${!empty systolicObs}">${systolicObs.valueNumeric}</c:if>/<c:if test="${!empty diastolicObs}">${diastolicObs.valueNumeric} <spring:message code="diagnosiscapturerwanda.bloodPressureUnits"/></c:if></td>
									</tr>
								</c:if>
							</table>
						</div>
					</td>
					
					<td class="dashboardInput">
						<div class="inputButton">
							<input type="button" onclick="document.location='./vitals.form?visitId=${visit.visitId}&visitToday=${visitToday}&patientId=${patient.patientId}'" value='<spring:message code="diagnosiscapturerwanda.changeVitals"/>'/>
						</div>
					</td>
				</tr>
			</table>
		</div>
		
		<div class="boxInnerDiagnosis">
			<table class="dashboardTable">
			
				<!-- Findings -->
				<tr>
					<td class="dashboardHeading">
						<spring:message code="diagnosiscapturerwanda.findings"/>
					</td>
					
					<td class="dashboardValue">
						<div id="findingsDiv">
							<table width="100%">
							<thead>
								<tr class="gradient">
									<th width="90%"><spring:message code="diagnosiscapturerwanda.finding"/></th>
									<th width="10%"></th>
								</tr>
							</thead>
								
								<c:set var="encTest" value=""/>
								<!-- primary diagnosis -->
								<c:forEach items="${visit.encounters}" var="enc" varStatus="pos">
								
								<c:if test="${enc.encounterType == encounter_type_findings && enc.voided == false}">
									<c:set var="encTest" value="${enc}"/>
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
								</c:if>
							</c:forEach>
							<c:if test="${empty encTest}">
								<tr><td colspan="2"><spring:message code="diagnosiscapturerwanda.noFindingsInThisVisit"/></td></tr>
							</c:if>
						</table>
						</div>
					</td>
		
					<td class="dashboardInput">
						<div class="inputButton">
							<input type="button" onclick="document.location='./findings.form?visitId=${visit.visitId}&patientId=${patient.patientId}&visitToday=${visitToday}'" value='<spring:message code="diagnosiscapturerwanda.changeFindings"/>'/>
						</div>
					</td>
				</tr>
			</table>
		</div>
		
		<!-- <div class="boxInnerDiagnosis">
			<table class="dashboardTable">	
				<tr>
					<td class="dashboardHeading">
						<spring:message code="diagnosiscapturerwanda.labs"/>
					</td>
					
					<td class="dashboardValue">
						<div id="labsDiv">
							<script>
							$j(document).ready(function() {
								 $j('#labsDiv').load('${pageContext.request.contextPath}/module/diagnosiscapturerwanda/labs.form?visitId=${visit.visitId}&patientId=${patient.patientId}&readOnly=true');
							});
							
							</script>
						</div>
					</td>
					
					<td class="dashboardInput">	
						<div class="inputButton">		
							<input type="button" onclick="document.location='./labs.list?visitId=${visit.visitId}&patientId=${patient.patientId}'" value='<spring:message code="diagnosiscapturerwanda.changeLabs"/>'/>
						</div>
					</td>
				</tr>
			</table>
		</div> -->
			
		<div class="boxInnerDiagnosis">
			<table class="dashboardTable">	
				<!-- Diagnoses -->
				<tr>
					<td class="dashboardHeading">
						<spring:message code="diagnosiscapturerwanda.diagnosis"/>
					</td>
				
					<td class="dashboardValue">
						<div id="diagnosisDiv">
							<openmrs:portlet url="diagnosisTable" id="diagnosisTable" moduleId="diagnosiscapturerwanda" />
						</div>
					</td>
					
					<td class="dashboardInput">	
						<div class="inputButton">	
							<input type="button" onclick="document.location='./diagnosisCapture.form?visitId=${visit.visitId}&visitToday=${visitToday}&patientId=${patient.patientId}'" value='<spring:message code="diagnosiscapturerwanda.changeDiagnosis"/>'/>
						</div>
					</td>
				</tr>
			</table>
		</div>
		
		<div class="boxInnerDiagnosis">
			<table class="dashboardTable">	
				<!-- treatment -->
				<tr>
					<td class="dashboardHeading">
						<spring:message code="diagnosiscapturerwanda.treatment"/>
					</td>
					 	
					<td class="dashboardValue">
							<openmrs:portlet url="currentregimen" moduleId="orderextension" id="patientRegimenCurrent" patientId="${patient.patientId}" parameters="mode=current|readOnly=true"/>	
					</td>
					
					<td class="dashboardInput">	
						<div class="inputButton">
							<input type="button" onclick="document.location='./treatment.form?visitId=${visit.visitId}&patientId=${patient.patientId}'" value='<spring:message code="diagnosiscapturerwanda.changeTreatment"/>'/>
						</div>	
					</td>
				</tr>	
			</table>
		</div>
	</div>		
	<br/>
	
	<div class="pastVisit">
		<div class="boxHeader"><spring:message code="diagnosiscapturerwanda.previousVisits"/></div>
		<div id="vitalsDiv" class="box">
			<table>
				<tr>
					<th class="gradient"><spring:message code="diagnosiscapturerwanda.visitDate"/></th>
				</tr>
				<c:forEach items="${visitList}" var="otherVisit">
				<tr><td>
					<a href="#" onclick="javascript:document.location.href='diagnosisPatientDashboard.list?patientId=${otherVisit.patient.id}&visitId=${otherVisit.id}'"><openmrs:formatDate date="${otherVisit.startDatetime}" type="short" />  :  ${visit.location}</a>
				</td></tr>	
				</c:forEach>
			</table>
		</div>
	</div>
	</div>
</c:if>
</div>
    
<%@ include file="/WEB-INF/template/footer.jsp"%>  