<%@ page session="false" contentType="text/html;charset=UTF-8"%>
<%
	String root = request.getContextPath();
%>

<html>
<head>
<meta http-equiv="content-type" content="text/html; charset=utf-8">
<meta http-equiv="pragma" content="no-cache">
<style>
.eternaFrame .text_list_hiliterows {
	background-color:#CCCCCC;
	color:white;
}
.eternaFrame .text_list_selectedrows {
	background-color:#10246A;
	color:white;
}
</style>
<script type="text/javascript" src="<%= root %>/res/jquery.js"></script>
<script type="text/javascript">
jQuery.noConflict();
</script>
<script type="text/javascript" src="<%= root %>/res/eterna.js"></script>
<script type="text/javascript">

var eventListener = null;
var $E = {};
var eternaData = $E;
var _eterna = null;
var windowFocused = false;
var nowIndex = -1;
var hasSelected = false;

function loadView(url, params)
{
	if (_eterna != null)
	{
		_eterna.getRemoteJSON(url, params, true, reInitView);
	}
}

function getPrintDivHeight()
{
	if (_eterna != null)
	{
		return _eterna.rootWebObj.height();
	}
	return -1;
}

function clearView()
{
	if (_eterna != null)
	{
		_eterna.changeEternaData(null);
		_eterna.reInit();
	}
}

function reInitView(initData)
{
	if (_eterna == null)
	{
		var divObj = jQuery("#print_simpleView");
		_eterna = new Eterna($E, 0, divObj);
	}
	if (initData != null)
	{
		_eterna.changeEternaData(initData);
	}
	_eterna.reInit();
	jQuery("*").unbind("mousedown", mousePressed);
	jQuery("*").bind("mousedown", mousePressed);
}

function triggerEvent(optName)
{
	if (eventListener != null)
	{
		eventListener.trigger(optName);
	}
}

function mousePressed(event)
{
	<%-- 延迟1毫秒再触发focus, IE6会在之后触发blur, 使立刻执行的focus无效 --%>
	setTimeout("window.focus();", 1);
}

jQuery(document).ready(function(){
	reInitView();
});
jQuery(window).focus(function(){
	windowFocused = true;
});
jQuery(window).blur(function(){
	windowFocused = false;
});

</script>
</head>

<body style="background-color:white;margin:5px">
<div id="print_simpleView" class="eternaFrame"></div>
</body>

</html>