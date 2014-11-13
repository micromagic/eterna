<%@ page contentType="text/html;charset=UTF-8"%>
<%@ page import ="self.micromagic.eterna.sql.SQLAdapter,
                  self.micromagic.util.Jdk14Factory,
                  self.micromagic.eterna.model.AppData,
                  self.micromagic.eterna.digester.FactoryManager,
                  self.micromagic.util.Utility"%>
<html>
<head>
<meta http-equiv="content-type" content="text/html; charset=utf-8">
<meta http-equiv="pragma" content="no-cache">
</head>

<body>

<%
		String sqlLog = request.getParameter("sqlLog");
		if (sqlLog != null)
		{
			Utility.setProperty(SQLAdapter.SQL_LOG_PROPERTY, sqlLog);
		}
		String errorLog = request.getParameter("errorLog");
		if (errorLog != null)
		{
			Utility.setProperty(Jdk14Factory.EXCEPTION_LOG_PROPERTY, errorLog);
		}
		String appLog = request.getParameter("appLog");
		if (appLog != null)
		{
			Utility.setProperty(AppData.APP_LOG_PROPERTY, appLog);
		}
		String check = request.getParameter("check");
		if (check != null)
		{
			Utility.setProperty(FactoryManager.CHECK_GRAMMER_PROPERTY, check);
		}
%>

sqlLog: <%= Utility.getProperty(SQLAdapter.SQL_LOG_PROPERTY) %>
&nbsp; &nbsp;
errorLog: <%= Utility.getProperty(Jdk14Factory.EXCEPTION_LOG_PROPERTY) %>
&nbsp; &nbsp;
appLog: <%= Utility.getProperty(AppData.APP_LOG_PROPERTY) %>
&nbsp; &nbsp;
checkGrammer: <%= Utility.getProperty(FactoryManager.CHECK_GRAMMER_PROPERTY) %>
<br>


<br>
<a href="reload.jsp?r=1&p=1" target="_blank">重载eterna配置</a> &nbsp;
<a href="setting.jsp?check=false">不检查语法</a> &nbsp;
<a href="setting.jsp?check=true">需要检查语法</a> &nbsp;

<br>
<a href="setting.jsp?sqlLog=2">打开sql日志</a> &nbsp;
<a href="setting.jsp?sqlLog=0">关闭sql日志</a> &nbsp;
<a href="showLog.jsp?sqlLog=1" target="_blank">察看sql日志</a> &nbsp;
<a href="showLog.jsp?sqlLog=-1" target="_blank">察看并清空sql日志</a> &nbsp;
<br>
<a href="setting.jsp?errorLog=1">打开error日志</a> &nbsp;
<a href="setting.jsp?errorLog=0">关闭error日志</a> &nbsp;
<a href="showLog.jsp?errorLog=1" target="_blank">察看error日志</a> &nbsp;
<a href="showLog.jsp?errorLog=-1" target="_blank">察看并清空error日志</a> &nbsp;
<br>
<a href="setting.jsp?appLog=1">打开app日志</a> &nbsp;
<a href="setting.jsp?appLog=0">关闭app日志</a> &nbsp;
<a href="showLog.jsp?appLog=1" target="_blank">察看app日志</a> &nbsp;
<a href="showLog.jsp?appLog=-1" target="_blank">察看并清空app日志</a> &nbsp;
<br>

</body>

</html>

