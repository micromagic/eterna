<%@ page session="false" contentType="text/html;charset=UTF-8"%><%@
    page import ="self.micromagic.eterna.view.impl.ViewTool"%><%@
    taglib prefix="e" uri="http://code.google.com/p/eterna" %><%
	String id = "_" + ViewTool.createEternaId();
%><e:init parentElement="eternaShow" divClass="eternaFrame" printHTML="2" suffixId="<%= id %>">
<meta name="viewport" content="width=device-width, initial-scale=1" />
<e:res url="/res/jquery.js"/>
<e:res url="/res/eterna.js"/>
<e:res url="/My97DatePicker/WdatePicker.js"/>
<e:res url="/res/sample.css" jsResource="false"/>
</e:init>