<%@ page session="false" contentType="text/html;charset=UTF-8"%>
<%
	String root = request.getContextPath();
%>
<html>
<head>
</head>
<body>

<a href="<%= root %>/autoAJAX/first.jsp">去第一个页面</a> |
<a href="<%= root %>/autoAJAX/second.jsp">去第二个页面</a> |
<a href="<%= root %>/autoAJAX/third.jsp">去第三个页面</a> |

<div>
这是第一个页面.
</div>



</body>
</html>