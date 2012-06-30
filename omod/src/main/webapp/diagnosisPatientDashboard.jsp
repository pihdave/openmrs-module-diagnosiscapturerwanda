<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>
<%@ include file="resources/localHeader.jsp" %>
    
<div class="box">
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
	<div class="box" id="mainDiv">
	
	
		<div><spring:message code="diagnosiscapturerwanda.registeredToday"/> 
			<c:if test="${visitIsToday}"><spring:message code="diagnosiscapturerwanda.yes"/></c:if>
		    <c:if test="${!visitIsToday}"><spring:message code="diagnosiscapturerwanda.no"/></c:if>
		</div><br/>
		
		
		<table>
			
			<!-- VITALS -->
			<tr><td>
			<div><b><spring:message code="diagnosiscapturerwanda.vitals"/></b></div>
			<div id="vitalsDiv">
				<table>
					<tr style='background-color: whitesmoke;'>
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
						<c:if test="${encTmp.encounterType == vitalsEncounterType}">
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
							<!-- build row -->
							<!-- TODO:  BMI -->
							<tr>
								<td>${tempObs.valueNumeric}</td>
								<td>${heightObs.valueNumeric}</td>
								<td>${weightObs.valueNumeric}</td>
								<td>TODO</td><!-- BMI -->
								<td>${systolicObs.valueNumeric}/${diastolicObs.valueNumeric}</td>
								<td></td>
							</tr>
						</c:if>
					</tr>
				</table>
			</div>
			</td>
			<td><button onclick="document.location='./vitals.form?visitId=${visit.visitId}'"><spring:message code="diagnosiscapturerwanda.changeVitals"/></button></td>
			</tr>
			<tr><td colspan="2"><br/>&nbsp;</td></tr><!-- spacer -->
			
			
			
			<!-- Findings -->
			<tr><td>
			<div><b><spring:message code="diagnosiscapturerwanda.findings"/></b></div>
			<div id="findingsDiv">
				<table>
					<tr style='background-color: whitesmoke;'>
						<th><spring:message code="diagnosiscapturerwanda.finding"/></th>
						<th><spring:message code="diagnosiscapturerwanda.otherFindings"/></th>
						<th></th>
					</tr>
					<c:set var="enc" value=""/>
					<c:forEach items="${visit.encounters}" var="encTmp" varStatus="pos">
						<c:if test="${encTmp.encounterType == findingsEncounterType}">
							<c:set var="enc" value="${encTmp}"/>
						</c:if>
					</c:forEach>
					<tr>
						<c:if test="${empty enc}">
							<td colspan="3"><spring:message code="diagnosiscapturerwanda.noFindingsInThisVisit"/></td>
						</c:if>
						<c:if test="${!empty enc}">
							<c:forEach items="${enc.obs}" var="obs">
								<c:if test="${obs.concept == concept_set_finding}"><!-- the findings conceptSet -->
									<c:set var="finding" value=""/>
									<c:set var="findingText" value=""/>
									<c:forEach items="obs.groupMembers" var="groupObs"><!--  for each set of group members -->
										<c:if test="${groupObs.concept == concept_diagnosis}">
											<c:set var="finding" value="${groupObs}"/>
										</c:if>
										<c:if test="${groupObs.concept == concept_findings_other}">
											<c:set var="findingText" value="${groupObs}"/>
										</c:if>
									</c:forEach>
									<c:if test="${!empty finding || !empty findingText }">
										<tr>
											<td><openmrs:format concept="${finding.valueCoded}"/></td>
											<td>${finding.valueText}</td>
											<td></td>
										</tr>
									</c:if>
								</c:if>
							</c:forEach>
						</c:if>
					</tr>
				</table>
			</div>
			</td>
			<td><button onclick="document.location='./findings.form?visitId=${visit.visitId}&patientId=${patient.patientId}'"><spring:message code="diagnosiscapturerwanda.changeFindings"/></button></td>
			</tr>
			<tr><td colspan="2"><br/>&nbsp;</td></tr><!-- spacer -->
	
	
	
	
	
	
	
			<!-- labs -->
			<tr><td>
			<div><b><spring:message code="diagnosiscapturerwanda.labs"/></b></div>
			<div id="labsDiv">
				<table>
					<tr style='background-color: whitesmoke;'>
						<th><spring:message code="diagnosiscapturerwanda.pannel"/></th>
						<th><spring:message code="diagnosiscapturerwanda.test"/></th>
						<th><spring:message code="diagnosiscapturerwanda.result"/></th>
						<th><spring:message code="diagnosiscapturerwanda.units"/></th>
						<th></th>
					</tr>
				</table>
			</div>
			</td>
			<td><button onclick="document.location='./labs.form?visitId=${visit.visitId}&patientId=${patient.patientId}'"><spring:message code="diagnosiscapturerwanda.changeLabs"/></button></td>
			</tr>
			<tr><td colspan="2"><br/>&nbsp;</td></tr><!-- spacer -->
			
			
			<!-- Diagnoses -->
			<tr><td>
				<div><b><spring:message code="diagnosiscapturerwanda.diagnosis"/></b></div>
				<div id="diagnosisDiv">
					<table>
						<tr style='background-color: whitesmoke;'>
							<th><spring:message code="diagnosiscapturerwanda.primaryDiagnosis"/></th>
							<th><spring:message code="diagnosiscapturerwanda.diagnosis"/></th>
							<th><spring:message code="diagnosiscapturerwanda.otherDiagnosis"/></th>
							<th><spring:message code="diagnosiscapturerwanda.confirmedSusptected"/></th>
							<th></th>
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
				</div>
			 </td>
			 <td><button onclick="document.location='./diagnosisCapture.form?visitId=${visit.visitId}&patientId=${patient.patientId}'"><spring:message code="diagnosiscapturerwanda.changeDiagnosis"/></button></td>
			 </tr>
			<tr><td colspan="2"><br/>&nbsp;</td></tr><!-- spacer -->
			
			<!-- treatment -->
			<tr><td>
			<div><b><spring:message code="diagnosiscapturerwanda.treatment"/></b></div>
			<div id="labsDiv">
				<table>
					<tr style='background-color: whitesmoke;'>
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
			<td><button onclick="document.location='./treatment.form?visitId=${visit.visitId}&patientId=${patient.patientId}'"><spring:message code="diagnosiscapturerwanda.changeTreatment"/></button></td>
			</tr>
			<tr><td colspan="2"><br/>&nbsp;</td></tr><!-- spacer -->
			
			
		</table>	
	</div><!-- end mainDiv -->
</c:if>

<div id="previousVisits">
</div>

</div>    

<div>
this page accepts parameters visitId, encounterId of any visit encounter, encounterUuid of any visit encounter
visit = ${visit}<br/>
<a href="./vitals.form?visitId=${visit.visitId}">vitalsEncounterType = ${vitalsEncounterType}</a><br/>
findingsEncounterType = ${findingsEncounterType}<br/>
labEncounterType = ${labEncounterType}<br/>
vitalsEncounterType = ${vitalsEncounterType}<br/>
diagnosisEncounterType =${diagnosisEncounterType}

<br/>
<a href="./treatment.form?patientId=${visit.patient.patientId}">Treatment</a>
<br/>
TODO on this page:  BMI, format numeric values, "change" buttons, finish labs, finish treatment, css this shit
</div>
    
    
    
    
    
    
<%@ include file="/WEB-INF/template/footer.jsp"%>  