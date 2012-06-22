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

			<script type="text/javascript">
				var lastSearch;
				$j(document).ready(function() {
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
                            searchLabel: '<spring:message code="diagnosiscapturerwanda.patientIdentifier" javaScriptEscape="true"/> ',
                            resultsHandler: doHandleResults,
                            searchPlaceholder:'<spring:message code="Patient.searchBox.placeholder" javaScriptEscape="true"/>'
                            <c:if test="${not empty param.phrase}">
                                , searchPhrase: '<spring:message text="${ param.phrase }" javaScriptEscape="true"/>'
                            </c:if>
                        });

					//set the focus to the first input box on the page(in this case the text box for the search widget)
					var inputs = document.getElementsByTagName("input");
				    if(inputs[0])
				    	inputs[0].focus();


				});
				
				//this will only return a patient to the user if the patient's identifier is equal to the search string.  In which case, there's a redirect to the patient dashboard.
				function doHandleResults(results){
					for(var r in results) {
						if (results.length == 1 && results[r].identifier == lastSearch) {
							document.location = "${model.postURL}?patientId=" + results[r].patientId + "&phrase=" + lastSearch;
						} 
					}
				}
						
				function doSelectionHandler(index, data) {
					document.location = "${model.postURL}?patientId=" + data.patientId + "&phrase=" + lastSearch;
				}

				//searchHandler for the Search widget
				function doPatientSearch(text, resultHandler, getMatchCount, opts) {
					lastSearch = text;
					DWRPatientService.findCountAndPatients(text, opts.start, opts.length, getMatchCount, resultHandler);
				}

			</script>

			<div>
				<b class="boxHeader"><spring:message code="Patient.find"/></b>
				<div class="box">
					<div class="searchWidgetContainer" id="findPatients"></div>
					<openmrs:globalProperty var="registrationUrl" key="diagnosiscapturerwanda.registrationSystemUrl" defaultValue=""/>
					<c:if test="${!empty registrationUrl}">
					<div>&nbsp;<a href="${pageContext.request.contextPath}/${registrationUrl}"><spring:message code="diagnosiscapturerwanda.registrationSystem"/></a></div>
					</c:if>
				</div>
			</div>
			
            

			<c:if test="${empty model.hideAddNewPatient}">
				<openmrs:hasPrivilege privilege="Add Patients">
					<br/> &nbsp; <spring:message code="general.or"/><br/><br/>
					<openmrs:portlet id="addPersonForm" url="addPersonForm" parameters="personType=patient|postURL=admin/person/addPerson.htm|viewType=${model.viewType}" />
				</openmrs:hasPrivilege>
			</c:if>
			
			

</c:if>
