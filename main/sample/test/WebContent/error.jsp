<%@ page session="false" contentType="text/html;charset=UTF-8" %><%@
    page import="self.micromagic.eterna.model.AppData,
                 self.micromagic.app.WebApp,
                 self.micromagic.util.Utils"%><html>
<head>
<meta http-equiv="content-type" content="text/html; charset=utf-8"/>
<meta http-equiv="pragma" content="no-cache"/>
</head>
<body>
<%
	AppData data = (AppData) request.getAttribute(WebApp.APPDATA_TAG);
	if (data != null)
	{
		out.println("发生错误, 编号为：" + data.getAppId());
		out.println("<br>错误信息为：" + Utils.dealString2HTML(String.valueOf(data.dataMap.get("errorMsg"))));
		out.println("<br>您可以记下此编号并与管理员联系.");
	}
	else
	{
		out.println("错误的页面访问!");
	}
%>
</body>
</html>
