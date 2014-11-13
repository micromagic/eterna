<%@ page session="false" contentType="text/html;charset=UTF-8"%>
<%@ page import="java.util.HashMap,java.util.Map"%>
<%@ taglib prefix="e" uri="http://code.google.com/p/eterna" %>
<%
	String root = request.getContextPath(); // 上下文根
%>

<%-- 在eterna框架外调用eterna中的对象及方法的例子 --%>
<html>
<head>
<meta http-equiv="content-type" content="text/html; charset=utf-8">
<meta http-equiv="pragma" content="no-cache">
<script type="text/javascript" src="<%= root %>/res/jquery.js"></script>
<script type="text/javascript" src="<%= root %>/res/eterna.js"></script>
<script type="text/javascript" src="<%= root %>/My97DatePicker/WdatePicker.js"></script>
<%
	// 准备数据集
	Map<String, Map<String, String>> data = new HashMap<String, Map<String, String>>();
	Map<String, String> tableData = new HashMap<String, String>();
	tableData.put("name", "测试者");
	tableData.put("sex", "女");
	tableData.put("address", "xx省xx市xx路xx号");
	tableData.put("birth", "2012-9-7");
	data.put("tableData", tableData);
	request.setAttribute("tmpData", data);
%>
<e:def name="tmp" data="tmpData" viewName="tmp.view"/>
</head>

<body style="background-color:white;margin:5px">
<div>
<input type="text" id="srcNameObj" value="name">
&nbsp;
<button onclick="tmp.callFunction('test.fn', $('#srcNameObj').val())">测试方法调用</button>
<br />
根据前面文本框中的名称获取其对应的值，见前面jsp代码中的tableData。
<br />
<button onclick="tmp.newComponent('tmp.table')">测试生成对象</button>
&nbsp; &nbsp;
<button onclick="$('#tTable').remove()">删除生成的对象</button>
</div>
</body>

</html>