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
这是第二个页面.
</div>
<br />
<a href="<%= root %>/autoAJAX/first.jsp" onclick="alert('not go!');return false;">不会跳转到第一个页面!</a> |
<a href="<%= root %>/autoAJAX/first.jsp" stopAJAX>整个页面跳转到第一个页面，不发生AJAX</a> |
<br />


<div style="border:1px solid black;height:1200px;width:100px;"></div>

<div><a name="a2">锚点2</a></div>
<div style="border:1px solid red;height:900px;width:200px;"></div>

</body>
</html>