<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>
<%@ include file="resources/localHeader.jsp" %>
    
<div class="box">
PATIENT DASHBOARD!
</div>    

<div>
this page accepts parameters visitId, encounterId of any visit encounter, encounterUuid of any visit encounter
visit = ${visit}<br/>
vitalsEncounterType = ${vitalsEncounterType}<br/>
findingsEncounterType = ${findingsEncounterType}<br/>
labEncounterType = ${labEncounterType}<br/>
vitalsEncounterType = ${vitalsEncounterType}<br/>
diagnosisEncounterType =${diagnosisEncounterType}

</div>
    
    
    
    
    
    
<%@ include file="/WEB-INF/template/footer.jsp"%>  