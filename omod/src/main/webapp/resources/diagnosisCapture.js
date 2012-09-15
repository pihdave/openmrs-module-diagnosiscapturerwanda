/**
 * NOTE:requires visit in the model map.
 */

/**
 * re-loads the page with a specific diagnosis obsGroup selected
 */
function editDiagnosis(obsGroupId, primary, confirmed) {
	
	$j("#obsGroupId").val(obsGroupId);
	if(!primary)
	{
		$j("#primarySecondarySelectEdit").attr("selectedIndex", 1);
	}
	else {
		$j("#primarySecondarySelectEdit").attr("selectedIndex", 0);
		$j("#primary").val(1);
	}
	
	if(confirmed == ${concept_confirmed.id})
	{
		$j("#confirmedSuspectedSelectEdit").attr("selectedIndex", 1);
	}
	else {
		$j("#confirmedSuspectedSelectEdit").attr("selectedIndex", 0);
	}
	
	$j(".openmrs_error").hide();
	$j('#editDiagnosisDialog').dialog('open');
}

/**
 * deletes a diagnosis
 */
function deleteDiagnosis(obsGroupId) {
	var conf = confirm("<spring:message code="diagnosiscapturerwanda.areYouSureYouWantToDeleteThisDiagnosis"/>");
	if (conf){
		$j.ajax({
			  type: "POST",
			  url: "deleteDiagnosis.list",
			  dataType: 'json',
			  data: { "obsGroupId": obsGroupId},
			  success: function(ret) {
				    var json = $j.parseJSON(ret);
				    if (json.result == 'success' ){
				    	document.location.href=window.location.pathname + '?patientId=${visit.patient.patientId}&visitId=${visit.visitId}&visitToday=${visitToday}';
						return;
				    } else {
						alert('<spring:message code="diagnosiscapturerwanda.deleteFailed" />');
						return;
				 	}
				  }
		});
	}
}

/**
 * this is the method that looks up and creates the icpc buttons
 */
function filterByCategory(id, restrictBySymptom){
	
	var categoryDiv = "#conceptCategory" + id;
	
	if( $j(categoryDiv).is(':visible') ) {
		$j(categoryDiv).hide();
		$j(".selected").removeClass("selected");
	}
	else {
		$j.getJSON('getDiagnosesByIcpcSystemJSON.list?groupingId=' + id + "&restrictBySymptom=" + restrictBySymptom, function(json) {
			 
			 //build a little legend
			 var ret = "<div class='categoryBox'>";
			 //if (!restrictBySymptom){
			 //	 ret += "<span> <span class='infection_color'>&nbsp;&nbsp&nbsp;&nbsp</span> <spring:message code='diagnosiscapturerwanda.infection'/> </span>";
			 //	 ret += "<span> <span class='injury_color'>&nbsp;&nbsp&nbsp;&nbsp</span> <spring:message code='diagnosiscapturerwanda.injury'/>  </span>";
			 //	 ret += "<span> <span class='diagnosis_color'>&nbsp;&nbsp&nbsp;&nbsp</span> <spring:message code='diagnosiscapturerwanda.diagnosis'/> </span><br/><br/>";
			 // }
		 	 // ret += "<table class='icpcResults'><tr valign=top><td>";
	
		 	 //TODO: reduce this, using an array?
			
			 if (!restrictBySymptom){
				 $j.each(json, function(item) {
				 	 if (json[item].category == _symptom){
				 		 ret+="<input type='button' class='symptom' onclick=\"javascript: setNewDiagnosis(" + json[item].id + ", \'" + json[item].name + "\');\"  value='" + json[item].name + "'/><br/>";
				 	 }
			     });
				 //	 ret+="</td><td>";
				 $j.each(json, function(item) {
				 	 if (json[item].category == _infection){
				 		 ret+="<input type='button' class='infection' onclick=\"javascript: setNewDiagnosis(" + json[item].id + ", \'" + json[item].name + "\');\" value='" + json[item].name + "'/><br/>";
				 	 }
			     });
			 //	 ret+="</td><td>";
				 $j.each(json, function(item) {
				 	 if (json[item].category == _injury){
				 		 ret+="<input type='button' class='injury' onclick=\"javascript: setNewDiagnosis(" + json[item].id + ", \'" + json[item].name + "\');\" value='" + json[item].name + "'/><br/>";
				 	 }
			     });
			 //	 ret+="</td><td>";
				 $j.each(json, function(item) {
				 	 if (json[item].category == _diagnosis){
				 		 ret+="<input type='button' class='diagnosis' onclick=\"javascript: setNewDiagnosis(" + json[item].id + ", \'" + json[item].name + "\');\" value='" + json[item].name + "'/><br/>";
				 	 }
			     });
			 }
			 else {
				 $j.each(json, function(item) {
				 	 if (json[item].category == _symptom){
				 		 ret+="<input type='button' class='symptom' onclick=\"javascript: setNewFinding(" + json[item].id + ");\"  value='" + json[item].name + "'/><br/>";
				 	 }
			     });
			 }
			 ret+="</div>";
			 $j(".conceptCategory").hide();
			
			 $j(".selected").removeClass("selected");
			 var categoryInput = "#" + id;
			 $j(categoryInput).addClass("selected");
			 
			 $j(categoryDiv).html(ret);
			 $j(categoryDiv).show();
		});
	}
}


/**
 * this looks up diagnoses by name; only return symptoms if restrictBySymptom = true
 */
function ajaxLookup(item, restrictBySymptom){
	$j("#conceptId").val("");
	if (item.value.length > 2){
		$j("#spinner").show();
		$j.getJSON('getDiagnosisByNameJSON.list?searchPhrase=' + item.value + '&restrictBySymptom=' + restrictBySymptom, function(json){
			
			$j("input#ajaxDiagnosisLookup").autocomplete({
			    source: json,
			    minLength: 2,
			    select: function(event, ui) { 
			    			setNewDiagnosis(ui.item.value, ui.item.label); 
			    			$j('#categorySearchResults').empty();
			    			return false;
			    		}
			});
			$j("#spinner").hide();
		});
	}
}

/**
 * this checks to see if a primary diagnosis already exists
 */
function checkForPrimaryDiagnosis(id, form){
	
	$j.ajax({
		  type: "POST",
		  url: "checkPrimaryStatus.list",
		  dataType: 'json',
		  data: { "visitId": ${visit.visitId} },
		  success: function(ret) {
			    var json = $j.parseJSON(ret);
			    if (json.result == 'true' ){
			    	$j(id).html('<spring:message code="diagnosiscapturerwanda.onlyOnePrimaryDiagnosisError" />');
					$j(id).show();
			    } else {
			    	$j(form).submit();
			 	}
			  }
	});
}

/**
 * this determines if the submit button should be enabled for the diagnosis drop down box.
 */
function highlightSubmit(){
	
	$j("#diagnosisId").val($j("#diagnosisSelect").val());
	
	if($j("#diagnosisSelect").attr("selectedIndex") > 0)
	{
		$j("#conceptSearchSumbit").removeAttr("disabled");
	}
	else
	{
		$j("#conceptSearchSumbit").attr("disabled", "disabled");
	}
}

/**
 * this determines if the submit button should be enabled for the symptoms drop down box.
 */
function highlightFindingsSubmit(){
	
	$j("#findingsId").val($j("#findingsSelect").val());
	
	if($j("#findingsSelect").attr("selectedIndex") > 0)
	{
		$j("#conceptSearchSumbit").removeAttr("disabled");
	}
	else
	{
		$j("#conceptSearchSumbit").attr("disabled", "disabled");
	}
}


/**
 * use this method to 'choose' the diagnosis from the ajax lookup or the icpc buttons
 */
function setNewDiagnosis(diagnosisId, diagnosisName){
	$j("#diagnosisId").val(diagnosisId);
	$j('#diagnosisDialog').dialog('open');
}

function setNewFinding(findingsId){
	$j("#findingsId").val(findingsId);
	$j('#findingsForm').submit();
}


function sumbitOtherDiagnosis() {
	
	$j('#diagnosisOtherTextArea').val($j('#diagnosisOther').val());
	
	if($j('#primarySecondarySelectOther').attr("selectedIndex") == 0)
	{
		checkForPrimaryDiagnosis("#openmrs_error_other", '#otherDiagnosisForm');
	}
	else
	{
		$j('#otherDiagnosisForm').submit();
	}
}

function submitDiagnosis(){
	
	if($j('#primarySecondarySelect').attr("selectedIndex") == 0)
	{
		checkForPrimaryDiagnosis("#openmrs_error", '#diagnosisForm');
	}
	else
	{
		$j('#diagnosisForm').submit();
	}
}

