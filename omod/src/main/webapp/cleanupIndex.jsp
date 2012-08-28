<META HTTP-EQUIV="CACHE-CONTROL" CONTENT="NO-CACHE">
<META HTTP-EQUIV="EXPIRES" CONTENT="01 Jan 1970 00:00:00 GMT">
<META HTTP-EQUIV="PRAGMA" CONTENT="NO-CACHE">
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>
    
1. Cleanup Concept Name Tags: <form action="cleanupConceptNameTags.form" method="get"><input type="submit" value="Execute"/></form>
<br/>
2. Cleanup Individual Concepts: <form action="cleanupIndividualConcepts.form" method="get"><input type="submit" value="Execute"/></form>
<br/>
3. Cleanup General Concept Issues: 
<c:forEach items="${startIds}" var="startId" varStatus="startIdStatus">
	<c:set var="endId" value="${endIds[startIdStatus.index]}"/>
	<form action="cleanupConcepts.form" method="get">
		<input type="hidden" name="startConceptId" value="${startId}"/>
		<input type="hidden" name="endConceptId" value="${endId}"/>
		<input type="submit" value="Execute ${startId} - ${endId}"/>
	</form>
	<br/>
</c:forEach>

<%@ include file="/WEB-INF/template/footer.jsp"%>  