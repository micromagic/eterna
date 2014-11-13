<%@ page contentType="text/xml;charset=UTF-8" pageEncoding="GBK"%><%@
    page import ="self.micromagic.eterna.digester.FactoryManager,
                  self.micromagic.util.Jdk14Factory,
                  self.micromagic.eterna.model.AppData"
%><%
	String sqlLog = request.getParameter("sqlLog");
	String errorLog = request.getParameter("errorLog");
	String appLog = request.getParameter("appLog");
	if (sqlLog != null)
	{
		response.setContentType("text/xml;charset=utf-8");
		FactoryManager.printLog(out, "-1".equals(sqlLog));
	}
	else if (errorLog != null)
	{
		response.setContentType("text/xml;charset=utf-8");
		Jdk14Factory.printException(out, "-1".equals(errorLog));
	}
	else if (appLog != null)
	{
		response.setContentType("text/xml;charset=utf-8");
		AppData.printLog(out, "-1".equals(appLog));
	}
%>