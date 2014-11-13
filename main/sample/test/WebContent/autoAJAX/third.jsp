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
这是第三个页面.
</div>


<form action="<%= root %>/autoAJAX/first.jsp">
<input name="gotoParam">
<input type="submit" value="提交到第一个页面">
</form>
<form action="#a1">
<input name="gotoParam">
<input type="submit" value="提交到锚点1">
</form>

<br /><br />
<a href="#a1">跳转到锚点1</a> |
<a href="<%= root %>/autoAJAX/second.jsp#a2">跳转到锚点2(在第二个页面)</a> |
<a href="#a3">跳转到锚点3</a> |

<div style="border:1px solid black;height:1200px;width:100px;"></div>
<div><a name="a1">锚点1</a></div>
<div style="border:1px solid red;height:900px;width:200px;"></div>
<div><a name="a3">锚点3</a></div>

</body>
</html>