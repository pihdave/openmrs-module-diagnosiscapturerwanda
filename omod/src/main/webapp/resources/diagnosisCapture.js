/**
 * NOTE:requires visit in the model map.
 */

/**
 * re-loads the page with a specific diagnosis obsGroup selected
 */
function editDiagnosis(obsGroupId, type, patientId, visitId) {
	if (type == 'findings')
		document.location.href='findings.list?patientId=' + patientId + '&visitId=' + visitId + '&obsGroupId=' + obsGroupId; 
	else	
		document.location.href='diagnosisCapture.list?patientId=' + patientId + '&visitId=' + visitId + '&obsGroupId=' + obsGroupId; 
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
			  data: { "obsGroupId": obsGroupId },
			  success: function(ret) {
				    var json = $j.parseJSON(ret);
				    if (json.result == 'success' ){
				    	document.location.href=window.location.pathname + '?patientId=${visit.patient.patientId}&visitId=${visit.visitId}';
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
	$j.getJSON('getDiagnosesByIcpcSystemJSON.list?groupingId=' + id + "&restrictBySymptom=" + restrictBySymptom, function(json) {
		 
		 //build a little legend
		 var ret = "<br/><span>  <span class='symptom_color'>&nbsp;&nbsp&nbsp;&nbsp</span> <spring:message code='diagnosiscapturerwanda.symptom'/> </span>";
		 if (!restrictBySymptom){
			 ret += "<span> <span class='infection_color'>&nbsp;&nbsp&nbsp;&nbsp</span> <spring:message code='diagnosiscapturerwanda.infection'/> </span>";
			 ret += "<span> <span class='injury_color'>&nbsp;&nbsp&nbsp;&nbsp</span> <spring:message code='diagnosiscapturerwanda.injury'/>  </span>";
			 ret += "<span> <span class='diagnosis_color'>&nbsp;&nbsp&nbsp;&nbsp</span> <spring:message code='diagnosiscapturerwanda.diagnosis'/> </span><br/><br/>";
		 }
	 	 ret += "<table class='icpcResults'><tr valign=top><td>";

	 	 //TODO: reduce this, using an array?
		 $j.each(json, function(item) {
		 	 if (json[item].category == _symptom){
		 		 ret+="<button class='symptom' onclick=\"javascript: setNewDiagnosis(" + json[item].id + ", \'" + json[item].name + "\');\"  >" + json[item].name + "</button><br/>";
		 	 }
	     });
		 if (!restrictBySymptom){
			 ret+="</td><td>";
			 $j.each(json, function(item) {
			 	 if (json[item].category == _infection){
			 		 ret+="<button class='infection' onclick=\"javascript: setNewDiagnosis(" + json[item].id + ", \'" + json[item].name + "\');\" >" + json[item].name + "</button><br/>";
			 	 }
		     });
			 ret+="</td><td>";
			 $j.each(json, function(item) {
			 	 if (json[item].category == _injury){
			 		 ret+="<button class='injury' onclick=\"javascript: setNewDiagnosis(" + json[item].id + ", \'" + json[item].name + "\');\" >" + json[item].name + "</button><br/>";
			 	 }
		     });
			 ret+="</td><td>";
			 $j.each(json, function(item) {
			 	 if (json[item].category == _diagnosis){
			 		 ret+="<button class='diagnosis' onclick=\"javascript: setNewDiagnosis(" + json[item].id + ", \'" + json[item].name + "\');\" >" + json[item].name + "</button><br/>";
			 	 }
		     });
		 }
		 ret+="</td></tr></table>";
		 $j("#categorySearchResults").html(ret);
	});
}


/**
 * this looks up diagnoses by name; only return symptoms if restrictBySymptom = true
 */
function ajaxLookup(item, restrictBySymptom){
	if (item.value.length > 2){
		
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
		});
	}
}



/**
 * use this method to 'choose' the diagnosis from the ajax lookup or the icpc buttons
 */
function setNewDiagnosis(diagnosisId, diagnosisName){
	$j("#diagnosisName").html("<span style='color:blue'>" + diagnosisName + "</span>");
	$j("#diagnosisId").val(diagnosisId);
}
