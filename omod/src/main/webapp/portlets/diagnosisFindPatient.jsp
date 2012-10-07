<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/template/include.jsp" %>

<c:if test="${model.authenticatedUser != null}">

			<openmrs:require privilege="View Patients" otherwise="/login.htm" redirect="/index.htm" />
			<style>
				#openmrsSearchTable_wrapper{
				/* Removes the empty space between the widget and the Create New Patient section if the table is short */
				/* Over ride the value set by datatables */
					min-height: 0px; height: auto !important;
				}
			</style>
			<openmrs:htmlInclude file="/dwr/interface/DWRPatientService.js"/>
			<openmrs:htmlInclude file="/scripts/jquery/dataTables/css/dataTables_jui.css"/>
			<openmrs:htmlInclude file="/scripts/jquery/dataTables/js/jquery.dataTables.min.js"/>
			<openmrs:htmlInclude file="/scripts/jquery-ui/js/openmrsSearch.js" />
			<openmrs:htmlInclude file="/moduleResources/diagnosiscapturerwanda/diagnosiscapturerwanda.css" />

			<script type="text/javascript">
				var lastSearch;
				$j(document).ready(function() {
					jQuery("#searchError").hide();
					new OpenmrsSearch("findPatients", false, doPatientSearch, doSelectionHandler,
						[	{fieldName:"identifier", header:omsgs.identifier},
							{fieldName:"givenName", header:omsgs.givenName},
							{fieldName:"middleName", header:omsgs.middleName},
							{fieldName:"familyName", header:omsgs.familyName},
							{fieldName:"age", header:omsgs.age},
							{fieldName:"gender", header:omsgs.gender},
							{fieldName:"birthdateString", header:omsgs.birthdate}
						],
						{
                            searchLabel: '<spring:message code="diagnosiscapturerwanda.patientIdentifier"/> ',
                            resultsHandler: doHandleResults,
                            searchPlaceholder:'<spring:message code="diagnosiscapturerwanda.searchPlaceHolder"/>'
                            <c:if test="${not empty param.phrase}">
                                , searchPhrase: '<spring:message text="${ param.phrase }"/>'
                            </c:if>
                        });

					//set the focus to the first input box on the page(in this case the text box for the search widget)
					var inputs = document.getElementsByTagName("input");
				    if(inputs[0])
				    	inputs[0].focus();

				});
				
				//this will only return a patient to the user if the patient's identifier is equal to the search string.  In which case, there's a redirect to the patient dashboard.
				function doHandleResults(results){
					var notFound = true;
					for(var r in results) {
						notFound = false;
						if (results.length == 1) {
							document.location = "${model.postURL}?patientId=" + results[r].patientId + "&phrase=" + lastSearch;
						} 
						else {
							
							jQuery("#searchError").html('<spring:message code="diagnosiscapturerwanda.identifierError" javaScriptEscape="true"/>');
							jQuery("#searchError").show();
							jQuery("#searchErrorRecent").hide();
							jQuery("#recentVisitDiv").html('');
						}
					}
					if(notFound){
						alert('<spring:message code="diagnosiscapturerwanda.searchError" javaScriptEscape="true"/><br/>');
					}
				}
						
				function doSelectionHandler(index, data) {
					document.location = "${model.postURL}?patientId=" + data.patientId + "&phrase=" + lastSearch;
				}

				//searchHandler for the Search widget
				function doPatientSearch(text, resultHandler, getMatchCount, opts) {
					lastSearch = text;
					
					if(text.length > 9)
					{	
						DWRPatientService.findCountAndPatients(text, opts.start, opts.length, getMatchCount, resultHandler);
					}
				}

			</script>

			
			<openmrs:globalProperty var="searchByNameUrl" key="diagnosiscapturerwanda.searchByNameSystemUrl" defaultValue=""/>
			<div class="boxHeader"><spring:message code="Patient.find"/></div>
			<div class="box">
				<c:if test="${!empty searchByNameUrl}">
					<div id="searchByName">
						<button class="blue" onclick="window.location = '${pageContext.request.contextPath}/${searchByNameUrl}'" type="button">
							<span><spring:message code="diagnosiscapturerwanda.searchByName" /></span>
						</button>
					</div>
				</c:if>
				<div class="searchWidgetContainer" id="findPatients"></div>
			</div>		
			<br/>	
			
			<div class="error" id="searchError">
			</div>
			
			
			<c:if test="${empty model.hideAddNewPatient}">
				<openmrs:hasPrivilege privilege="Add Patients">
					<br/> &nbsp; <spring:message code="general.or"/><br/><br/>
					<openmrs:portlet id="addPersonForm" url="addPersonForm" parameters="personType=patient|postURL=admin/person/addPerson.htm|viewType=${model.viewType}" />
				</openmrs:hasPrivilege>
			</c:if>
			
</c:if>
