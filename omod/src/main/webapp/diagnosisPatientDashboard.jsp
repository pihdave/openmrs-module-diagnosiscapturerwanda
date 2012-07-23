<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>
<%@ include file="resources/localHeader.jsp" %>
<style>
	<%@ include file="resources/diagnosiscapturerwanda.css" %>
</style>
<script type="text/javascript">
<%@ include file="resources/diagnosisCapture.js" %>
</script>

<openmrs:htmlInclude file="/scripts/jquery-ui/js/jquery-ui.custom.min.js" />
    
<div class="boxInner">
<br/>
<!-- patient is not registered -->
<c:if test="${empty visit}">
	<div class="box">
		<openmrs:globalProperty var="registrationUrl" key="diagnosiscapturerwanda.registrationSystemUrl" defaultValue=""/>
		<spring:message code="diagnosiscapturerwanda.youMustRegisterThisPatientFirst"/> &nbsp;
		<c:if test="${!empty registrationUrl}">
			&nbsp;<a href="${pageContext.request.contextPath}/${registrationUrl}"><spring:message code="diagnosiscapturerwanda.registrationSystem"/></a>
		</c:if>
	</div>	
</c:if>
<!-- patient is registered -->
<c:if test="${!empty visit}">
	<div><spring:message code="diagnosiscapturerwanda.visitDate"/> &nbsp; <openmrs:formatDate date="${visit.startDatetime}" type="short" /></div>
	<div><spring:message code="diagnosiscapturerwanda.registeredToday"/> 
			<c:if test="${visitIsToday}"><spring:message code="diagnosiscapturerwanda.yes"/></c:if>
		    <c:if test="${!visitIsToday}"><spring:message code="diagnosiscapturerwanda.no"/></c:if>
	</div><br/>
	<div class="boxInner" id="mainDiv" style="background-color:#FAFAFA">
		<table id="frameTable"> <!-- frames the page  -->
		
			<!-- VITALS -->
			<tr><td>
			<div><b><spring:message code="diagnosiscapturerwanda.vitals"/></b></div><br/>
			<div id="vitalsDiv" class="boxInner">
				<table>
					<tr class="gradient">
						<th><spring:message code="diagnosiscapturerwanda.temperature"/></th>
						<th><spring:message code="diagnosiscapturerwanda.height"/></th>
						<th><spring:message code="diagnosiscapturerwanda.weight"/></th>
						<th><spring:message code="diagnosiscapturerwanda.bmi"/></th>
						<th><spring:message code="diagnosiscapturerwanda.bloodPressure"/></th>
						<th></th>
					</tr>
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
					<tr>
						<c:if test="${empty enc}">
							<td colspan="6"><spring:message code="diagnosiscapturerwanda.noVitalsInThisVisit"/></td>
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
								<td>${tempObs.valueNumeric}</td>
								<td>${heightObs.valueNumeric}</td>
								<td>${weightObs.valueNumeric}</td>
								<td>${currentBMI}</td>
								<td>${systolicObs.valueNumeric}/${diastolicObs.valueNumeric}</td>
								<td></td>
							</tr>
						</c:if>
					</tr>
				</table>
			</div>
			</td>
			<td valign="top">
				<button onclick="document.location='./vitals.form?visitId=${visit.visitId}'"><spring:message code="diagnosiscapturerwanda.changeVitals"/></button>
			</td>
			</tr>
			<tr><td colspan="2" class="spacer"><br/>&nbsp;</td></tr><!-- spacer -->
			
			
			
			<!-- Findings -->
			<tr><td>
			<div><b><spring:message code="diagnosiscapturerwanda.findings"/></b></div><br/>
			<div id="findingsDiv" class="boxInner">
					<table>
						<tr class="gradient">
							<th><spring:message code="diagnosiscapturerwanda.finding"/></th>
							<th><spring:message code="diagnosiscapturerwanda.otherFindings"/></th>
							<th></th>
						</tr>
						<c:set var="encTest" value=""/>
						<c:forEach items="${visit.encounters}" var="enc" varStatus="pos">
							<c:if test="${enc.encounterType == encounter_type_findings && enc.voided == false}">
								<c:set var="encTest" value="${enc}"/>
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
													<td><openmrs:format concept="${finding.valueCoded}"/></td>
													<td>${findingText.valueText}</td>
													<td>
													&nbsp; <a href="#" onclick="editDiagnosis(${obs.id}, 'findings');"><img src='<%= request.getContextPath() %>/images/edit.gif' alt="edit"/></a>
													&nbsp; <a href="#" onclick="deleteDiagnosis(${obs.id});"><img src='<%= request.getContextPath() %>/images/delete.gif' alt="delete" /></a>
												</td>
												</tr>
											</c:if>
										</c:if>
									</c:forEach>
								</tr>
							</c:if>
						</c:forEach>
						<c:if test="${empty encTest}">
							<td colspan="3"><spring:message code="diagnosiscapturerwanda.noFindingsInThisVisit"/></td>
						</c:if>
					</table>
			</div>
			</td>
			<td valign="top"><button onclick="document.location='./findings.form?visitId=${visit.visitId}&patientId=${patient.patientId}'"><spring:message code="diagnosiscapturerwanda.changeFindings"/></button></td>
			</tr>
			<tr><td colspan="2" class="spacer" ><br/>&nbsp;</td></tr><!-- spacer -->
	
	
	
	
	
	
	
			<!-- labs -->
			<tr><td>
			<div><b><spring:message code="diagnosiscapturerwanda.labs"/></b></div>
			<div id="labsDiv">
					<script>
					$j(document).ready(function() {
						 $j('#labsDiv').load('http://localhost:8088/openmrs19/module/diagnosiscapturerwanda/labs.form?visitId=26&patientId=124&readOnly=true');
					});
					
					</script>
			</div>
			</td>
			<td valign="top"><button onclick="document.location='./labs.form?visitId=${visit.visitId}&patientId=${patient.patientId}'"><spring:message code="diagnosiscapturerwanda.changeLabs"/></button></td>
			</tr>
			<tr><td colspan="2" class="spacer"><br/>&nbsp;</td></tr><!-- spacer -->
			
			
			<!-- Diagnoses -->
			<tr><td>
				<div><b><spring:message code="diagnosiscapturerwanda.diagnosis"/></b></div><br/>
				<div id="diagnosisDiv" class="boxInner">
					<openmrs:portlet url="diagnosisTable" id="diagnosisTable" moduleId="diagnosiscapturerwanda" />
				</div>
			 </td>
			 <td valign="top"><button onclick="document.location='./diagnosisCapture.form?visitId=${visit.visitId}&patientId=${patient.patientId}'"><spring:message code="diagnosiscapturerwanda.changeDiagnosis"/></button></td>
			 </tr>
			<tr><td colspan="2"><br/>&nbsp;</td></tr><!-- spacer -->
			
			<!-- treatment -->
			<tr><td>
			<div><b><spring:message code="diagnosiscapturerwanda.treatment"/></b></div><br/>
			<div id="labsDiv" class="boxInner">
				<table>
					<tr class="gradient">
						<th><spring:message code="diagnosiscapturerwanda.drugOrRegimen"/></th>
						<th><spring:message code="diagnosiscapturerwanda.dosage"/></th>
						<th><spring:message code="diagnosiscapturerwanda.startDate"/></th>
						<th><spring:message code="diagnosiscapturerwanda.duration"/></th>
						<th><spring:message code="diagnosiscapturerwanda.frequency"/></th>
						<th></th>
					</tr>
				</table>
			</div>
			</td>
			<td valign="top"><button onclick="document.location='./treatment.form?visitId=${visit.visitId}&patientId=${patient.patientId}'"><spring:message code="diagnosiscapturerwanda.changeTreatment"/></button></td>
			</tr>
			<tr><td colspan="2" class="spacer"><br/>&nbsp;</td></tr><!-- spacer -->
			
			
		</table>	
	</div><!-- end mainDiv -->
</c:if>

<div id="previousVisits">
	TODO
</div>

</div>    
  
<%@ include file="/WEB-INF/template/footer.jsp"%>  