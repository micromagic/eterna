<%@ page contentType="text/html;charset=UTF-8" pageEncoding="GBK"%>
<%@ page import ="self.micromagic.eterna.digester.FactoryManager,
                  self.micromagic.util.StringRef,
                  self.micromagic.util.Utility,
                  self.micromagic.util.Utils"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=GBK">
<meta http-equiv="pragma" content="no-cache">
</head>
<body>
<%
	StringRef str = new StringRef();
	if ("1".equals(request.getParameter("p")))
	{
		Utility.reload(str);
		out.println(str);
		out.println("<br/>");
		str.setString("");
		out.println("config reload over!");
		out.println("<br/>");
	}

	if ("1".equals(request.getParameter("r")))
	{
		FactoryManager.reInitEterna(str);
		Utils.dealString2HTML(str.getString(), out, true);
		out.println("<br/>");
		str.setString("");
		out.println("eterna reload over!");
		out.println("<br/>");
	}
%>
<br/><br/><br/>
all reload over!
</body>
</html>