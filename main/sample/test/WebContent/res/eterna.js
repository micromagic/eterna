/*
 * Copyright 2009-2015 xinjunli (micromagic@sina.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
  * version: 1.5.7
  */

(function(window) {

if (typeof $eternaInitialized == "undefined")
{
	window.$eternaInitialized = 1;
}
else
{
	return;
}

var ___ETERNA_VERSION = "1.5.7";
// ED = ETERNA_DEBUG, FN = FUNCTION, FNC = FUNCTION_CALLED, COM = COMPONENT
window.ED_GET_VALUE = 0x1;
window.ED_EXECUTE_SCRIPT = 0x2;
window.ED_SHOW_CREATED_HTML = 0x4;
window.ED_SHOW_OTHERS = 0x8;
window.ED_BASE = 0x10;
window.ED_HIGHER = 0x80;
window.ED_FN_CALLED = 0x30;
window.ED_FNC_STACK = 0x40;
window.ED_COM_CREATED = 0x20;

var oldBodyWidth;
function registerResizeEvent()
{
	oldBodyWidth = jQuery("body").width();
	jQuery(window).resize(function (){
		var bodyObj = jQuery("body");
		if (Math.abs(bodyObj.width() - oldBodyWidth) > EG_EVENT_WIDTH_SENSITIVITY)
		{
			oldBodyWidth = bodyObj.width();
			Eterna.fireWidthChange(bodyObj);
		}
	});
}

var jQueryInitCount = 0;
function jQueryInit()
{
	if (typeof jQuery == 'undefined' && typeof $ == "object" && typeof $.jquery == "string")
	{
		window.jQuery = $;
	}
	if (typeof jQuery != 'undefined')
	{
		// 不使用深度序列化方式
		jQuery.ajaxSettings.traditional = true;

		if (document.readyState == "complete" || document.readyState == "loaded")
		{
			registerResizeEvent();
		}
		else
		{
			jQuery(registerResizeEvent);
		}
	}
	else
	{
		if (jQueryInitCount < 16)
		{
			jQueryInitCount++;
			setTimeout(jQueryInit, 200);
		}
	}
}
jQueryInit();



// EE = ETERNA_EVENT
var EE_SUB_WINDOW_CLOSE = "lock_close";
window.eterna_table_td_empty_value = "&nbsp;";
window.eterna_tableForm_title_pluse = "";
window.eterna_tableForm_title_fn = function (cellConfig, titleValue, titleObj, containerObj, _eterna)
{
	if (titleValue.caption != null)
	{
		eg_temp.caption = titleValue.caption;
	}
	if (titleValue.config != null)
	{
		_eterna.appendParam(titleObj, titleValue.config, null);
	}
	if (titleValue.exists && !ef_isEmpty(titleValue.value))
	{
		if (titleValue.html)
		{
			if (typeof titleValue.value == "object" && typeof titleValue.value.type == "string")
			{
				var tmpCom = _eterna.createComponent(titleValue.value, titleObj);
				if (tmpCom != null)
				{
					titleObj.append(tmpCom);
				}
			}
			else
			{
				titleObj.html(titleValue.value);
			}
		}
		else
		{
			titleObj.text(titleValue.value);
		}
		if (eterna_tableForm_title_pluse != null && eterna_tableForm_title_pluse != "")
		{
			titleObj.append(eterna_tableForm_title_pluse);
		}
		if (cellConfig.container != null && cellConfig.container.required)
		{
			eterna_tableForm_requared_fn(cellConfig, titleValue, titleObj, containerObj, _eterna);
		}
	}
	else
	{
		titleObj.html(eterna_table_td_empty_value);
	}
};
window.eterna_tableForm_requared_fn = function (cellConfig, titleValue, titleObj, containerObj, _eterna)
{
	var tObj = jQuery("<span>*</span>");
	tObj.css("color", "red");
	titleObj.prepend(tObj);
};
window.eterna_tableList_title_fn = function (columnConfig, titleObj, titleValue, upTitle, _eterna)
{
	if (titleValue.caption != null)
	{
		eg_temp.caption = titleValue.caption;
	}
	if (titleValue.config != null)
	{
		_eterna.appendParam(titleObj, titleValue.config, null);
	}
	if (titleValue.exists && !ef_isEmpty(titleValue.value))
	{
		if (titleValue.html)
		{
			if (typeof titleValue.value == "object" && typeof titleValue.value.type == "string")
			{
				var tmpCom = _eterna.createComponent(titleValue.value, titleObj);
				if (tmpCom != null)
				{
					titleObj.append(tmpCom);
				}
			}
			else
			{
				titleObj.html(titleValue.value);
			}
		}
		else
		{
			titleObj.text(titleValue.value);
		}
	}
	else
	{
		titleObj.html(eterna_table_td_empty_value);
	}
};
window.eterna_select_default_value = [["","-不限-"]];

var eterna_com_stack = [];
var eterna_fn_stack = [];


// eg = eterna_globe
window.EG_SMA = "searchManager_attributes";
window.EG_DATA_TYPE = "___dataType";
window.EG_DATA_TYPE_ONLYRECORD = "data";
window.EG_DATA_TYPE_DATA = "data";
window.EG_DATA_TYPE_REST = "REST";
window.EG_DATA_TYPE_ALL = "all";
window.EG_DATA_TYPE_WEB = "web";
window.EG_ORDER_SUFIX = ".order";
window.EG_ORDER_TYPE = ".orderType";

window.EG_EVENT_WIDTH_CHANGE = "widthChange";
// 对宽度变化的灵敏度, 变化量多大时才会触发widthChange事件
window.EG_EVENT_WIDTH_SENSITIVITY = 15;

var EG_SCRIPT_STR_PREFIX_ARR = ["javascript:", "vbscript:"];
var EG_JSON_SPLIT_FLAG = "<!-- eterna json data split -->";
var EG_HTML_DATA_DIV = "tempHTML_data_div_forEterna";
var EG_NO_SUB = "noSub";
var EG_STOP_AJAX = "stopAJAX";
var EG_EVENT_WILL_INIT = "willInit";
var EG_AUTO_INIT_FLAG = "autoInit";
var EG_SUFFIX_ID_FLAG = "eSuffixId";
var EG_ROOT_OBJ_ID_FLAG = "eRootObjId";
var EG_FLAG_TAG = "eFlag";
var EG_SCATTER_FLAG = "scatterFlag";
var EG_SWAP_FLAG = "swapId";
var EG_SWAP_INFO = "___swapInfo";
var EG_SCATTER_INIT_FLAG = "initId";
var EG_INHERIT_FLAG = "inherit";
var EG_INHERIT_INFO = "___inheritInfo";
var EG_INHERIT_BASE = "___baseObj";
var EG_INHERIT_GLOBAL_SEARCH = "inheritGlobalSearch";
var EG_KEEP_BASE_OBJ = "keepBaseObj";
var EG_OLD_BASE_OBJ = "oldBaseObj";
var EG_KEEP_OBJ_WHEN_USE = "keepObjWhenUse";
var EG_TEMPLATE_OBJ_FLAG = "templateObj";
window.EG_BINDED_ETERNA = "binded.eterna";


window.eg_cache = {
	willInitObjs:[],staticInitFns:[],openedObjs:[],loadedScripts:{},serialId:1,
	eternaCache:{},nextEternaId:1,logHistoryInAJAX:true,stopHashChangeEvent:false,
	currentEterna:null
};

var EG_TEMP_NAMES = [
	"dataName", "srcName", "index", "columnCount", "rowNum",
	"rowType", /* row, title, beforeTable, afterTable, beforeTitle, afterTitle, beforeRow, afterRow */
	"name", "caption", "valueObj", "param", "tempData",
	"extInfo", "widthLevel", "widthBlocks", /* 自适应宽度调整时显示扩充信息的标识 当前的宽度等级 宽度分割块 */
	"sysTemplateRoot", "sysOptions",
];

window.eg_temp = {};


// 初始化parentEterna
if (typeof dialogArguments == 'object')
{
	if (dialogArguments.parentEterna != null)
	{
		eg_cache.parentEterna = dialogArguments.parentEterna;
		window.opener = dialogArguments.parentWindow;
	}
}
if (opener != null && eg_cache.parentEterna == null)
{
	try
	{
		if (opener.eterna_getParentEterna != null)
		{
			eg_cache.parentEterna = opener.eterna_getParentEterna(window);
		}
	}
	catch (tmpEX) {}
}

var eg_caculateWidth_fix;
var eg_defaultWidth = 80;



function Eterna($E, eterna_debug, rootWebObj, eternaId)
{
	$E = eterna_checkEternaData($E);
	if (typeof eternaId != "undefined")
	{
		this.id = eternaId;
	}
	else
	{
		this.id = "E" + eg_cache.nextEternaId++;
	}
	eg_cache.eternaCache[this.id] = this;
	this.$E = $E;
	this.eternaData = this.$E;
	var eternaData = this.$E;

	this.eterna_debug = eterna_debug;
	this.rootWebObj = rootWebObj;
	this.cache = {};
	this.nowWindow = null;
	//this.rootWebObjOld_HTML = this.rootWebObj == null ? "" : this.rootWebObj.html();
	eterna_initEternaCache(this, $E);
	eterna_checkBinded(this);

	if (typeof Eterna._initialized == 'undefined')
	{
		Eterna._initialized = true;
		Eterna._oldDebug = eterna_debug;
	}

	if (this.eterna_debug >= ED_FN_CALLED)
	{
		this.swapComponent = function(baseObj, subs, swapFlag)
		{
			this.pushFunctionStack(new Array("swapComponent", Eterna.prototype.swapComponent,
					"baseObj", baseObj, "subs", subs, "swapFlag", swapFlag));
			var result = Eterna.prototype.swapComponent.call(this, baseObj, subs, swapFlag);
			this.popFunctionStack();
			if (typeof result != 'undefined')
			{
				return result;
			}
		}

		this.getValue_fromRecords = function(dataName, srcName, index)
		{
			this.pushFunctionStack(new Array("getValue_fromRecords", Eterna.prototype.getValue_fromRecords,
					"dataName", dataName, "srcName", srcName, "index", index));
			var result = Eterna.prototype.getValue_fromRecords.call(this, dataName, srcName, index);
			this.popFunctionStack();
			if (typeof result != 'undefined')
			{
				return result;
			}
		}

		this.getValue = function(str, valueObj)
		{
			this.pushFunctionStack(new Array("getValue", Eterna.prototype.getValue,
					"str", str, "valueObj", valueObj));
			var result = Eterna.prototype.getValue.call(this, str, valueObj);
			this.popFunctionStack();
			if (typeof result != 'undefined')
			{
				return result;
			}
		}

		this.executeScript = function(webObj, objConfig, scriptStr)
		{
			this.pushFunctionStack(new Array("executeScript", Eterna.prototype.executeScript,
					"webObj", webObj, "objConfig", objConfig, "scriptStr", scriptStr));
			var result = Eterna.prototype.executeScript.call(this, webObj, objConfig, scriptStr);
			this.popFunctionStack();
			if (typeof result != 'undefined')
			{
				return result;
			}
		}

		this.createComponent = function(configData, parent, options)
		{
			this.pushFunctionStack(new Array("createComponent", Eterna.prototype.createComponent,
					"configData", configData, "parent", parent, "options", options));
			var result = Eterna.prototype.createComponent.call(this, configData, parent, options);
			this.popFunctionStack();
			if (typeof result != 'undefined')
			{
				return result;
			}
		}

		this.changeSpecialObjEvent = function(configData, webObj)
		{
			this.pushFunctionStack(new Array("changeSpecialObjEvent", Eterna.prototype.changeSpecialObjEvent,
					"configData", configData, "webObj", webObj));
			var result = Eterna.prototype.changeSpecialObjEvent.call(this, configData, webObj);
			this.popFunctionStack();
			if (typeof result != 'undefined')
			{
				return result;
			}
		}

		this.dealEvents = function(configData, webObj, parent, myTemp)
		{
			this.pushFunctionStack(new Array("dealEvents", Eterna.prototype.dealEvents,
					"configData", configData, "webObj", webObj, "parent", parent, "myTemp", myTemp));
			var result = Eterna.prototype.dealEvents.call(this, configData, webObj, parent, myTemp);
			this.popFunctionStack();
			if (typeof result != 'undefined')
			{
				return result;
			}
		}

		this.dealLoopComponent = function(configData, webObj, options)
		{
			this.pushFunctionStack(new Array("dealLoopComponent", Eterna.prototype.dealLoopComponent,
					"configData", configData, "webObj", webObj, "options", options));
			var result = Eterna.prototype.dealLoopComponent.call(this, configData, webObj, options);
			this.popFunctionStack();
			if (typeof result != 'undefined')
			{
				return result;
			}
		}

		this.dealSubComponent = function(configData, webObj, options)
		{
			this.pushFunctionStack(new Array("dealSubComponent", Eterna.prototype.dealSubComponent,
					"configData", configData, "webObj", webObj, "options", options));
			var result = Eterna.prototype.dealSubComponent.call(this, configData, webObj, options);
			this.popFunctionStack();
			if (typeof result != 'undefined')
			{
				return result;
			}
		}

		this.createInheritObj = function(configData, parent, options)
		{
			this.pushFunctionStack(new Array("createInheritObj", Eterna.prototype.createInheritObj,
					"configData", configData, "parent", parent, "options", options));
			var result = Eterna.prototype.createInheritObj.call(this, configData, parent, options);
			this.popFunctionStack();
			if (typeof result != 'undefined')
			{
				return result;
			}
		}

		this.findInheritObj = function(configData, findBase, swapFlag, result, needClone, inTemplate)
		{
			this.pushFunctionStack(new Array("findInheritObj", Eterna.prototype.findInheritObj,
					"configData", configData, "findBase", findBase, "swapFlag", swapFlag,
					"result", result, "needClone", needClone, "inTemplate", inTemplate));
			var result = Eterna.prototype.findInheritObj.call(this, configData, findBase, swapFlag,
					result, needClone, inTemplate);
			this.popFunctionStack();
			if (typeof result != 'undefined')
			{
				return result;
			}
		}

		this.createWebObj = function(configData, type, extType)
		{
			this.pushFunctionStack(new Array("createWebObj", Eterna.prototype.createWebObj,
					"configData", configData, "type", type, "extType", extType));
			var result = Eterna.prototype.createWebObj.call(this, configData, type, extType);
			this.popFunctionStack();
			if (typeof result != 'undefined')
			{
				return result;
			}
		}

		this.createTR = function(configData, tableObj, type, model)
		{
			this.pushFunctionStack(new Array("createTR", Eterna.prototype.createTR,
					"configData", configData, "tableObj", tableObj, "type", type, "model", model));
			var result = Eterna.prototype.createTR.call(this, configData, tableObj, type, model);
			this.popFunctionStack();
			if (typeof result != 'undefined')
			{
				return result;
			}
		}

		this.createTableForm = function(configData)
		{
			this.pushFunctionStack(new Array("createTableForm", Eterna.prototype.createTableForm,
					"configData", configData));
			var result = Eterna.prototype.createTableForm.call(this, configData);
			this.popFunctionStack();
			if (typeof result != 'undefined')
			{
				return result;
			}
		}

		this.ctf_createUsedList = function(table, index, cell)
		{
			this.pushFunctionStack(new Array("ctf_createUsedList", Eterna.prototype.ctf_createUsedList,
					"table", table, "index", index, "cell", cell));
			var result = Eterna.prototype.ctf_createUsedList.call(this, table, index, cell);
			this.popFunctionStack();
			if (typeof result != 'undefined')
			{
				return result;
			}
		}

		this.ctf_getColumnCount = function(table)
		{
			this.pushFunctionStack(new Array("ctf_getColumnCount", Eterna.prototype.ctf_getColumnCount,
					"table", table));
			var result = Eterna.prototype.ctf_getColumnCount.call(this, table);
			this.popFunctionStack();
			if (typeof result != 'undefined')
			{
				return result;
			}
		}

		this.ctf_getSize = function(configData, table)
		{
			this.pushFunctionStack(new Array("ctf_getSize", Eterna.prototype.ctf_getSize,
					"configData", configData, "table", table));
			var result = Eterna.prototype.ctf_getSize.call(this, configData, table);
			this.popFunctionStack();
			if (typeof result != 'undefined')
			{
				return result;
			}
		}

		this.ctf_getColWidth = function(columns, index, count, table)
		{
			this.pushFunctionStack(new Array("ctf_getColWidth", Eterna.prototype.ctf_getColWidth,
					"columns", columns, "index", index, "count", count, "table", table));
			var result = Eterna.prototype.ctf_getColWidth.call(this, columns, index, count, table);
			this.popFunctionStack();
			if (typeof result != 'undefined')
			{
				return result;
			}
		}

		this.ctf_cell = function(columns, colIndex, percentWidth, cell, table)
		{
			this.pushFunctionStack(new Array("ctf_cell", Eterna.prototype.ctf_cell,
					"columns", columns, "colIndex", colIndex, "percentWidth", percentWidth,
					"cell", cell, "table", table));
			var result = Eterna.prototype.ctf_cell.call(this, columns, colIndex, percentWidth, cell, table);
			this.popFunctionStack();
			if (typeof result != 'undefined')
			{
				return result;
			}
		}

		this.ctf_title = function(configData, theSize, rowSpan)
		{
			this.pushFunctionStack(new Array("ctf_title", Eterna.prototype.ctf_title,
					"configData", configData, "theSize", theSize, "rowSpan", rowSpan));
			var result = Eterna.prototype.ctf_title.call(this, configData, theSize, rowSpan);
			this.popFunctionStack();
			if (typeof result != 'undefined')
			{
				return result;
			}
		}

		this.ctf_container = function(configData, theSize, rowSpan)
		{
			this.pushFunctionStack(new Array("ctf_container", Eterna.prototype.ctf_container,
					"configData", configData, "theSize", theSize, "rowSpan", rowSpan));
			var result = Eterna.prototype.ctf_container.call(this, configData, theSize, rowSpan);
			this.popFunctionStack();
			if (typeof result != 'undefined')
			{
				return result;
			}
		}

		this.ctf_tr = function(configData, tableObj)
		{
			this.pushFunctionStack(new Array("ctf_tr", Eterna.prototype.ctf_tr,
					"configData", configData, "tableObj", tableObj));
			var result = Eterna.prototype.ctf_tr.call(this, configData, tableObj);
			this.popFunctionStack();
			if (typeof result != 'undefined')
			{
				return result;
			}
		}

		this.createTableList = function(configData)
		{
			this.pushFunctionStack(new Array("createTableList", Eterna.prototype.createTableList,
					"configData", configData));
			var result = Eterna.prototype.createTableList.call(this, configData);
			this.popFunctionStack();
			if (typeof result != 'undefined')
			{
				return result;
			}
		}

		this.ctl_tr = function(configData, tableObj, rowType)
		{
			this.pushFunctionStack(new Array("ctl_tr", Eterna.prototype.ctl_tr,
					"configData", configData, "tableObj", tableObj, "rowType", rowType));
			var result = Eterna.prototype.ctl_tr.call(this, configData, tableObj, rowType);
			this.popFunctionStack();
			if (typeof result != 'undefined')
			{
				return result;
			}
		}

		this.ctl_dealUpTitle_checkNone = function(columns, layer, start, count, setSame, upPlace)
		{
			this.pushFunctionStack(new Array("ctl_dealUpTitle_checkNone", Eterna.prototype.ctl_dealUpTitle_checkNone,
					"columns", columns, "layer", layer, "start", start, "count", count, "setSame", setSame, "upPlace", upPlace));
			var result = Eterna.prototype.ctl_dealUpTitle_checkNone.call(this, columns, layer, start, count, setSame, upPlace);
			this.popFunctionStack();
			if (typeof result != 'undefined')
			{
				return result;
			}
		}

		this.ctl_dealUpTitle = function(columns)
		{
			this.pushFunctionStack(new Array("ctl_dealUpTitle", Eterna.prototype.ctl_dealUpTitle,
					"columns", columns));
			var result = Eterna.prototype.ctl_dealUpTitle.call(this, columns);
			this.popFunctionStack();
			if (typeof result != 'undefined')
			{
				return result;
			}
		}

		this.ctl_getColWidth = function(columns, start, count)
		{
			this.pushFunctionStack(new Array("ctl_getColWidth", Eterna.prototype.ctl_getColWidth,
					"columns", columns, "start", start, "count", count));
			var result = Eterna.prototype.ctl_getColWidth.call(this, columns, start, count);
			this.popFunctionStack();
			if (typeof result != 'undefined')
			{
				return result;
			}
		}

		this.ctl_title = function(percentWidth, trObj, column, layer)
		{
			this.pushFunctionStack(new Array("ctl_title", Eterna.prototype.ctl_title,
					"percentWidth", percentWidth, "trObj", trObj, "column", column,
					"layer", layer));
			var result = Eterna.prototype.ctl_title.call(this, percentWidth, trObj, column, layer);
			this.popFunctionStack();
			if (typeof result != 'undefined')
			{
				return result;
			}
		}

		this.ctl_container = function(index, percentWidth, trObj, column)
		{
			this.pushFunctionStack(new Array("ctl_container", Eterna.prototype.ctl_container,
					"index", index, "percentWidth", percentWidth, "trObj", trObj, "column", column));
			var result = Eterna.prototype.ctl_container.call(this, index, percentWidth, trObj, column);
			this.popFunctionStack();
			if (typeof result != 'undefined')
			{
				return result;
			}
		}

		this.appendParam = function(obj, configData, objType)
		{
			this.pushFunctionStack(new Array("appendParam", Eterna.prototype.appendParam,
					"obj", obj, "configData", configData, "objType", objType));
			var result = Eterna.prototype.appendParam.call(this, obj, configData, objType);
			this.popFunctionStack();
			if (typeof result != 'undefined')
			{
				return result;
			}
		}

	}

}

window.Eterna = Eterna;


/**
 * 一些全局的属性及方法.
 */

Eterna.prototype.eternaVersion = Eterna.eternaVersion = ___ETERNA_VERSION;

Eterna.prototype.fireWidthChange = Eterna.fireWidthChange = function(rootObj)
{
	jQuery("*", rootObj).each(function (){
		var theObj = jQuery(this);
		var theEvents = theObj.data("events");
		if (theEvents != null && theEvents[EG_EVENT_WIDTH_CHANGE] != null)
		{
			theObj.trigger(EG_EVENT_WIDTH_CHANGE);
		}
	});
}

Eterna.prototype.isArray = Eterna.isArray = function(obj)
{
	return jQuery.isArray(obj);
}

Eterna.prototype.isFunction = Eterna.isFunction = function(obj)
{
	return jQuery.isFunction(obj);
}

Eterna.prototype.createJSON = Eterna.createJSON = function()
{
	return {};
}

Eterna.prototype.cloneJSON = Eterna.cloneJSON = function(obj)
{
	if (typeof obj == "object")
	{
		if (Eterna.isArray(obj))
		{
			var newObj = [];
			for(var i = 0; i < obj.length; i++)
			{
				newObj[i] = Eterna.cloneJSON(obj[i]);
			}
			return newObj;
		}
		else if (Eterna.isFunction(obj))
		{
			return obj;
		}
		else
		{
			var newObj = {};
			for(var key in obj)
			{
				newObj[key] = Eterna.cloneJSON(obj[key]);
			}
			return newObj;
		}
	}
	else
	{
		return obj;
	}
}


Eterna.prototype.egTemp = function(temp)
{
	var key;
	if (temp != null)
	{
		for (key in eg_temp)
		{
			eg_temp[key] = null;
		}
		for (var i = 0; i < EG_TEMP_NAMES.length; i++)
		{
			key = EG_TEMP_NAMES[i];
			if (temp[key] != null)
			{
				eg_temp[key] = temp[key];
			}
		}
		return eg_temp;
	}
	else
	{
		temp = {};
		for (var i = 0; i < EG_TEMP_NAMES.length; i++)
		{
			key = EG_TEMP_NAMES[i];
			if (eg_temp[key] != null)
			{
				temp[key] = eg_temp[key];
			}
		}
		return temp;
	}
}

Eterna.prototype.egTempParam = function(copy)
{
	if (eg_temp.param == null)
	{
		eg_temp.param = {};
	}
	else
	{
		if (copy)
		{
			eg_temp.param = this.cloneJSON(eg_temp.param);
		}
	}
	return eg_temp.param;
}

Eterna.prototype.egTempData = function(copy)
{
	if (eg_temp.tempData == null)
	{
		eg_temp.tempData = {};
	}
	else
	{
		if (copy)
		{
			eg_temp.tempData = this.cloneJSON(eg_temp.tempData);
		}
	}
	return eg_temp.tempData;
}

Eterna.prototype.changeEternaData = function(newData)
{
	if (this.$E == newData)
	{
		return;
	}
	eterna_initEternaCache(this, newData);
	newData = eterna_checkEternaData(newData);
	for (var key in this.$E)
	{
		this.$E[key] = null;
	}
	for (var key in newData)
	{
		this.$E[key] = newData[key];
	}
}

Eterna.prototype.getRemoteJSON = function(url, formObj, async, successFunction, completeFunction)
{
	var httpRequest = null;
	var _egTemp = null;
	if (async)
	{
		// 如果是异步处理, 需要保存环境变量, 并在执行回调函数前恢复
		_egTemp = this.egTemp();
	}
	var successFn = function (data, textStatus)
	{
		try
		{
			var _eterna = successFn._eterna;
			var eterna_debug = successFn.eterna_debug;
			var $E = successFn._eterna.$E;
			var eternaData = $E;
			successFn._eterna.cache.textData = data;
			successFn.status = textStatus;
			if (eterna_checkRemoteData(data))
			{
				successFn.data = eval("(" + eterna_dealRemoteData(_eterna, data) + ")");
			}
			else
			{
				successFn.status = "not json data"
			}
		}
		catch (pEx)
		{
			successFn.status = "json_parse_error," + pEx;
			successFn.data = null;
			if ((this.eterna_debug & ED_EXECUTE_SCRIPT) != 0)
			{
				successFn._eterna.printException(pEx);
			}
		}
		if ((successFn.eterna_debug & ED_SHOW_CREATED_HTML) != 0 && successFn.httpRequest != null)
		{
			successFn._eterna.showMessage(successFn.httpRequest.responseText
					+ "\n---------------------------------\n" + "status:" + successFn.status + ",url:" + url);
		}
		if (successFn.customerFn != null)
		{
			var oldTemp;
			if (successFn.egTemp != null)
			{
				oldTemp = successFn._eterna.egTemp();
				successFn._eterna.egTemp(successFn.egTemp);
			}
			successFn.customerFn(successFn.data, successFn.status);
			if (successFn.egTemp != null)
			{
				successFn._eterna.egTemp(oldTemp);
			}
		}
	};
	successFn.eterna_debug = this.eterna_debug;
	successFn._eterna = this;
	if (successFunction != null)
	{
		successFn.customerFn = successFunction;
		if (_egTemp != null)
		{
			successFn.egTemp = _egTemp;
		}
	}
	try
	{
		var opts = {dataType:"text",url:url,async:false,cache:false};
		if (typeof formObj == "string")
		{
			opts.type = formObj;
		}
		else if (typeof formObj == "object")
		{
			opts.type = "POST";
			if (typeof formObj.jquery == "string")
			{
				opts.data = formObj.serialize();
			}
			else
			{
				opts.data = jQuery.param(formObj);
			}
		}
		if (async != null)
		{
			opts.async = async;
		}
		opts.success = successFn;
		if (completeFunction != null)
		{
			var completeFn = function (request, textStatus)
			{
				var ts = completeFn.successInfo.status != null ?
						completeFn.successInfo.status : textStatus;
				var oldTemp;
				if (completeFn.egTemp != null)
				{
					oldTemp = completeFn._eterna.egTemp();
					completeFn._eterna.egTemp(completeFn.egTemp);
				}
				completeFn.customerFn(request, ts, completeFn._eterna);
				if (completeFn.egTemp != null)
				{
					completeFn._eterna.egTemp(oldTemp);
				}
			};
			completeFn.successInfo = successFn;
			completeFn._eterna = this;
			completeFn.customerFn = completeFunction;
			if (_egTemp != null)
			{
				completeFn.egTemp = _egTemp;
			}
			opts.complete = completeFn;
		}
		httpRequest = jQuery.ajax(opts);
		if (async)
		{
			successFn.httpRequest = httpRequest;
		}
		else
		{
			if ((this.eterna_debug & ED_SHOW_CREATED_HTML) != 0 && httpRequest != null)
			{
				this.showMessage(httpRequest.responseText + "\n---------------------------------\n"
						+ "status:" + successFn.status + ",url:" + url);
			}
		}
		return opts.async ? successFn : successFn.data;
	}
	catch (ex)
	{
		this.printException(ex);
		throw ex;
	}
}

Eterna.prototype.loadEterna = function(url, param, parentObj, useAJAX, debug, recall)
{
	return ef_loadEterna(url, param, parentObj, useAJAX, debug, recall);
}

Eterna.prototype.destroy = function(clearHTML, changeURL)
{
	if (this.rootWebObj != null)
	{
		if (clearHTML !== false)
		{
			this.rootWebObj.html("");
		}
		this.rootWebObj.removeData(EG_BINDED_ETERNA);
	}
	delete eg_cache.eternaCache[this.id];
	if (eg_cache.logHistoryInAJAX && this.cache.useAJAX)
	{
		// 停止hash值变化的监控
		eg_cache.stopHashChangeEvent = true;
		eterna_hashCodeURL(this.id, changeURL != null ? changeURL : null);
	}
}

Eterna.prototype.getRemoteText = function(url, param, completeFunction)
{
	var httpRequest = null;
	try
	{
		var opts = {dataType:"text",url:url,async:false,cache:false};
		if (param == null)
		{
			opts.type = "GET";
		}
		else
		{
			opts.type = "POST";
			if (typeof param.jquery == "string")
			{
				opts.data = param.serialize();
			}
			else
			{
				opts.data = jQuery.param(param);
			}
		}
		var completeFn;
		if (completeFunction != null)
		{
			completeFn = function (request, textStatus)
			{
				var result = completeFn.httpRequest.responseText;
				if ((completeFn.eterna_debug & ED_SHOW_CREATED_HTML) != 0 && completeFn.httpRequest != null)
				{
					completeFn._eterna.showMessage(result + "\n---------------------------------\n"
							+ "status:" + textStatus + ",url:" + url);
				}
				var oldTemp = completeFn._eterna.egTemp();
				completeFn._eterna.egTemp(completeFn.egTemp);
				completeFn.customerFn(result, completeFn._eterna, request, textStatus);
				completeFn._eterna.egTemp(oldTemp);
			};
			completeFn.eterna_debug = this.eterna_debug;
			completeFn._eterna = this;
			completeFn.customerFn = completeFunction;
			// 是异步处理, 需要保存环境变量, 并在执行回调函数前恢复
			completeFn.egTemp = this.egTemp();
			opts.complete = completeFn;
			opts.async = true;
		}
		httpRequest = jQuery.ajax(opts);
		if (completeFunction != null)
		{
			completeFn.httpRequest = httpRequest;
		}
		else
		{
			if ((this.eterna_debug & ED_SHOW_CREATED_HTML) != 0 && httpRequest != null)
			{
				this.showMessage(httpRequest.responseText + "\n---------------------------------\n"
						+ "url:" + url);
			}
		}
		return completeFunction != null ? completeFunction : httpRequest.responseText;
	}
	catch (ex)
	{
		this.printException(ex);
		throw ex;
	}
}

/**
 * 将form中的数据转换成json对象.
 */
Eterna.prototype.serializeFormData = function(formObj)
{
	if (formObj == null)
	{
		return {};
	}
	if (typeof formObj == "object")
	{
		if (typeof formObj.jquery == "string")
		{
			var result = {};
			var tmpArr = formObj.serializeArray();
			for (var i = 0; i < tmpArr.length; i++)
			{
				result[tmpArr[i].name] = tmpArr[i].value;
			}
			return result;
		}
		else
		{
			return formObj;
		}
	}
	var str = formObj + "";
	if (str.length > 0)
	{
		var result = {};
		var tmpArr = str.split("&");
		for (var i = 0; i < tmpArr.length; i++)
		{
			var tStr = tmpArr[i];
			var index = tStr.indexOf("=");
			if (index != -1)
			{
				result[tStr.substring(0, index)] = tStr.substring(index + 1);
			}
			else
			{
				result[tStr] = null;
			}
		}
		return result;

	}
	else
	{
		return {};
	}
}

/**
 * 准备初始化需要的信息.
 */
Eterna.prototype.initPrepare = function(scatter)
{
	this.cache.initialized = 0;
	if (this.cache[EG_ROOT_OBJ_ID_FLAG] != null)
	{
		var rootObjId = this.cache[EG_ROOT_OBJ_ID_FLAG];
		if (this.cache[EG_SUFFIX_ID_FLAG] != null)
		{
			rootObjId += this.cache[EG_SUFFIX_ID_FLAG];
		}
		if (this.rootWebObj == null || rootObjId != this.rootWebObj.attr("id"))
		{
			var tmpObj = jQuery("#" + rootObjId);
			if (tmpObj.size() == 0 && typeof this.cache.retryFindCount == "number")
			{
				// 当控件未找到且设置了重查次数, 则过200毫秒后再查一次
				if (this.cache.retryFindCount > 0)
				{
					this.cache.retryFindCount--;
					var retryFn = function ()
					{
						retryFn._eterna.reInit();
					};
					retryFn._eterna = this;
					setTimeout(retryFn, 200);
					return false;
				}
			}
			if (typeof this.cache.retryFindCount == "number")
			{
				// 如果控件找到了且设置了重查次数, 则将其清除
				this.cache.retryFindCount = null;
			}
			if (tmpObj.size() == 1)
			{
				if (this.rootWebObj == null)
				{
					this.rootWebObj = tmpObj;
				}
				else
				{
					this.rootWebObj.after(tmpObj);
					this.rootWebObj.remove();
					this.rootWebObj = tmpObj;
				}
				if (this.cache[EG_ROOT_OBJ_ID_FLAG + ".width"] != null)
				{
					this.rootWebObj.css("width", this.cache[EG_ROOT_OBJ_ID_FLAG + ".width"]);
				}
				if (this.cache[EG_ROOT_OBJ_ID_FLAG + ".height"] != null)
				{
					this.rootWebObj.css("height", this.cache[EG_ROOT_OBJ_ID_FLAG + ".height"]);
				}
			}
		}
	}
	var result = this.rootWebObj != null;
	eterna_checkBinded(this);
	if (result && scatter && this.rootWebObj.data(EG_SCATTER_FLAG))
	{
		if ((this.eterna_debug & ED_EXECUTE_SCRIPT) != 0)
		{
			this.printException("The rootWebObj has did scatter!");
		}
		result = false;
	}
	if (result && !scatter)
	{
		this.rootWebObj.html("");
	}
	return result;
}

Eterna.prototype.scatterInit = function()
{
	try
	{
		var needCreate = this.initPrepare(true);
		if (!needCreate)
		{
			return;
		}
		if (this.$E.beforeInit != null)
		{
			needCreate = this.executeScript(this.rootWebObj, this.$E, this.$E.beforeInit);
		}
		if (needCreate)
		{
			this.rootWebObj.data(EG_SCATTER_FLAG, 1);
			var initFlag = EG_SCATTER_INIT_FLAG;
			if (this.cache[EG_SCATTER_FLAG] != null)
			{
				initFlag = this.cache[EG_SCATTER_FLAG];
			}
			this.swapComponent(this.rootWebObj, this.$E.V, initFlag);
			if (this.$E.init != null)
			{
				this.executeScript(this.rootWebObj, this.$E, this.$E.init);
			}
		}
	}
	catch (ex)
	{
		this.printException(ex);
		throw ex;
	}
	finally
	{
		this.cache.initialized = 1;
		eterna_doInitObjs(this.rootWebObj);
	}
	if ((this.eterna_debug & ED_SHOW_CREATED_HTML) != 0)
	{
		this.showMessage(this.rootWebObj.html());
	}
}

Eterna.prototype.reInit = function()
{
	if (this.cache[EG_SCATTER_FLAG] != null)
	{
		this.scatterInit();
		return;
	}
	try
	{
		var needCreate = this.initPrepare(false);
		if (!needCreate)
		{
			return;
		}
		if (this.$E.beforeInit != null)
		{
			needCreate = this.executeScript(this.rootWebObj, this.$E, this.$E.beforeInit);
		}
		if (needCreate)
		{
			for (var i = 0; i < this.$E.V.length; i++)
			{
				var tmpCom = this.createComponent(this.$E.V[i], this.rootWebObj);
				if (tmpCom != null)
				{
					this.rootWebObj.append(tmpCom);
				}
			}
			if (this.$E.init != null)
			{
				this.executeScript(this.rootWebObj, this.$E, this.$E.init);
			}
		}
	}
	catch (ex)
	{
		this.printException(ex);
		throw ex;
	}
	finally
	{
		this.cache.initialized = 1;
		eterna_doInitObjs(this.rootWebObj);
	}
	if ((this.eterna_debug & ED_SHOW_CREATED_HTML) != 0)
	{
		this.showMessage(this.rootWebObj.html());
	}
}

Eterna.prototype.queryWebObj = function(queryStr, container)
{
	if (container == null)
	{
		container = this.rootWebObj;
	}
	return jQuery(queryStr, container);
}

Eterna.prototype.getWebObj = function(id, container, index)
{
	if (typeof container == "number")
	{
		index = container;
		container = null;
	}
	if (container == null)
	{
		container = this.rootWebObj;
	}
	var idObj = this.checkIdValid(id);
	if (index == null)
	{
		return jQuery(idObj.idStr, container);
	}
	else
	{
		return jQuery(idObj.idStr + ":eq(" + index + ")", container);
	}
}

Eterna.prototype.checkIdValid = function(id)
{
	var str = id + "";
	var valid = 1;
	if (str.length > 32)
	{
		valid = 0;
	}
	else
	{
		for (var i = 0; i < str.length; i++)
		{
			var c = str.charCodeAt(i);
			if (c != 95 && !(c >= 48 && c <= 57) && !(c >= 97 && c <= 122)
					&& !(c >= 65 && c <= 90))
			{
				valid = 0;
				break;
			}
		}
	}
	if (valid)
	{
		return {valid:1,idStr:"#" + str};
	}
	else
	{
		return {valid:0,idStr:"[id='" + ef_toScriptString(str) + "']"};
	}
}

Eterna.prototype.reloadWebObj = function(webObj)
{
	var temp = this.egTemp();
	var parentWebObj = null;
	var tmpCom = null;
	try
	{
		var objName = "";
		if (typeof webObj == "string")
		{
			objName = webObj;
			webObj = this.getWebObj(webObj);
		}
		if (typeof webObj.jquery != "string" || webObj.size() != 1)
		{
			if ((this.eterna_debug & ED_SHOW_OTHERS) != 0)
			{
				if (objName == "")
				{
					alert("The param isn't a jQuery obj!");
				}
				else
				{
					alert("The obj[id:" + objName + "](count:" + webObj.size() + ") isn't only one!");
				}
			}
			return null;
		}
		var configObj = webObj.data("configData");
		if (configObj == null)
		{
			if ((this.eterna_debug & ED_SHOW_OTHERS) != 0)
			{
				alert("The obj[id:" + objName + "] hasn't configData, can't reload!");
			}
			return null;
		}
		var myTemp = webObj.data("egTemp");
		this.egTemp(myTemp);
		parentWebObj = webObj.data("parentWebObj");
		tmpCom = this.createComponent(configObj, parentWebObj);
		if (tmpCom != null)
		{
			webObj.after(tmpCom);
		}
		var inheritInfo = webObj.data(EG_INHERIT_INFO);
		if (inheritInfo == null || !inheritInfo[EG_KEEP_BASE_OBJ])
		{
			// 如果模板节点没有被设置继承信息或保持信息, 则要移除
			webObj.remove();
		}
		// 这里如果tmpCom为null, 那说明没有生成, 因为reload只能针对实体对象
		eterna_doInitObjs(tmpCom);
	}
	catch (ex)
	{
		this.egTemp(temp);
		this.printException(ex);
		throw ex;
	}
	this.egTemp(temp);
	if ((this.eterna_debug & ED_SHOW_CREATED_HTML) != 0 && tmpCom != null)
	{
		this.showMessage("id:" + tmpCom.attr("id") + "\n" + tmpCom.html());
	}
	return tmpCom;
}

Eterna.prototype.detachTopOnfocus = function(eventFunction)
{
	try
	{
		eventFunction.maskDiv.remove();
		var theBody = jQuery("body", eventFunction.theWindow.document);
		theBody.css("overflow-x", eventFunction.oldOverflow.x);
		theBody.css("overflow-y", eventFunction.oldOverflow.y);
		jQuery(eventFunction.theWindow).unbind("focus", eventFunction);
	}
	catch (ex)
	{
		this.printException(ex);
	}
}

Eterna.prototype.attachTopOnfocus = function(theWindow, eventFunction)
{
	try
	{
		jQuery(theWindow).bind("focus", {move:0}, eventFunction);
		eventFunction.theWindow = theWindow;
		var theBody = jQuery("body", theWindow.document);
		eventFunction.maskDiv = jQuery("<div></div>", theBody);
		eventFunction.maskDiv.appendTo(theBody);
		eventFunction.oldOverflow = {x:theBody.css("overflow-x"),y:theBody.css("overflow-y")};
		eventFunction.maskDiv.css({width:"100%",height:"100%",position:"absolute","z-index":20000});
		eventFunction.maskDiv.css("top", jQuery(theWindow.document).scrollTop() + "px");
		eventFunction.maskDiv.css("left", jQuery(theWindow.document).scrollLeft() + "px");
		eventFunction.maskDiv.bind("click", {move:0}, eventFunction);
		eventFunction.maskDiv.bind("mousemove", {move:1}, eventFunction);
		eventFunction.maskDiv.bind("mouseup", {move:0}, eventFunction);
		eventFunction.maskDiv.bind("mousedown", {move:0}, eventFunction);
		theBody.css("overflow-x", "hidden");
		theBody.css("overflow-y", "hidden");
		if (!jQuery.support.boxModel || document.all)  // IE
		{
			eventFunction.maskDiv.css("filter", "alpha(opacity=0)");
			eventFunction.maskDiv.css("background-color", "white");
		}
		return true;
	}
	catch (ex)
	{
		this.printException(ex);
		return false;
	}
}

Eterna.prototype.closeAllWindow = function()
{
	for (var i = 0; i < eg_cache.openedObjs.length; i++)
	{
		var tmpObj = eg_cache.openedObjs[i];
		if (tmpObj.openedEterna == this)
		{
			if (tmpObj.winObj != null && !tmpObj.winObj.closed)
			{
				tmpObj.winObj.close();
			}
		}
	}
}

Eterna.prototype.hasOpened = function()
{
	for (var i = 0; i < eg_cache.openedObjs.length; i++)
	{
		var tmpObj = eg_cache.openedObjs[i];
		if (tmpObj.openedEterna == this)
		{
			if (tmpObj.winObj != null && !tmpObj.winObj.closed)
			{
				return true;
			}
		}
	}
	return false;
}

Eterna.prototype.openWindow = function(url, name, param, lock, closeEvent)
{
	var theWindow = open(url, name, param);
	var canInsertIndex = -1;
	var needAdd = true;
	for (var i = 0; i < eg_cache.openedObjs.length; i++)
	{
		var tmpObj = eg_cache.openedObjs[i];
		if (tmpObj.winObj == theWindow)
		{
			needAdd = false;
			break;
		}
		if (tmpObj.winObj == null || tmpObj.winObj.closed)
		{
			canInsertIndex = i;
		}
	}
	if (needAdd)
	{
		if (canInsertIndex == -1)
		{
			eg_cache.openedObjs.push({winObj:theWindow,openedEterna:this});
		}
		else
		{
			eg_cache.openedObjs[canInsertIndex] = {winObj:theWindow,openedEterna:this};
		}
	}
	theWindow.focus();
	if (lock)
	{
		this.nowWindow = theWindow;
		if (this.isFunction(closeEvent))
		{
			jQuery(window).bind(EE_SUB_WINDOW_CLOSE, closeEvent);
		}

		var onOpenWindowThisFocus = function(event)
		{
			var theEterna = onOpenWindowThisFocus._eterna;
			if (theEterna.nowWindow == null || theEterna.nowWindow.closed)
			{
				theEterna.detachTopOnfocus(onOpenWindowThisFocus);
				if (theEterna.nowWindow != null)
				{
					jQuery(window).trigger(EE_SUB_WINDOW_CLOSE);
					jQuery(window).unbind(EE_SUB_WINDOW_CLOSE);
				}
				theEterna.nowWindow = null;
			}
			else if (!event.data.move)
			{
				theEterna.nowWindow.blur();
				theEterna.nowWindow.focus();
				event.preventDefault();
				return false;
			}
		};
		onOpenWindowThisFocus._eterna = this;
		if (!this.attachTopOnfocus(window.top, onOpenWindowThisFocus))
		{
			this.attachTopOnfocus(window, onOpenWindowThisFocus)
		}
	}
	return theWindow;
}

Eterna.prototype.showMessage = function(msg, theWindow)
{
	var winObj;
	if (theWindow == null || theWindow.closed)
	{
		winObj = this.openWindow("", "_blank", "resizable=yes", false);
		winObj.document.write("<html><body><textarea id='msg' style='border:0;width:100%;height:100%'></textarea></body></html>");
	}
	else
	{
		winObj = theWindow;
	}
	if (winObj.document.getElementById("msg") != null)
	{
		winObj.document.getElementById("msg").value += msg;
	}
	return winObj;
}

Eterna.prototype.printException = function(ex, noShow)
{
	if (this.eterna_debug < ED_BASE || ex.dealed)
	{
		return;
	}

	var str = "* exception info:\n";
	if (typeof ex == "object")
	{
		str += "	" + ex + "\n";
		for(var key in ex)
		{
			str += "	" + key + ":" + ex[key] + "\n";
		}
	}
	else
	{
		str += "	" + ex;
	}

	str += "\n\n\n* component stack info:\n";
	for (var i = 0; i < eterna_com_stack.length; i++)
	{
		str += "	" + eterna_com_stack[i] + "\n";
	}

	str += "\n\n\n* now function info:\n";
	if (eterna_fn_stack.length > 0)
	{
		var fnInfo = eterna_fn_stack[eterna_fn_stack.length - 1];
		for (var i = 2; i < fnInfo.length; i += 2)
		{
			if (i > 2)
			{
				str += ",\n"
			}
			var tmpParam = fnInfo[i + 1];
			str += "	" + fnInfo[i] + ":" + tmpParam;
			if (typeof tmpParam == "object" && tmpParam != null)
			{
				if (tmpParam.name != null)
				{
					str += "[name:" + tmpParam.name + "]";
				}
				if (tmpParam.type != null)
				{
					str += "[type:" + tmpParam.type + "]";
				}
			}
		}
		str += "\n\n" + fnInfo[0] + ":" + fnInfo[1];
	}


	if (this.eterna_debug >= ED_FNC_STACK)
	{
		for (var i = eterna_fn_stack.length - 2; i >= 0; i--)
		{
			str += "\n\n\n* stack(" + i + ") function info:\n";
			var fnInfo = eterna_fn_stack[i];
			for (var j = 2; j < fnInfo.length; j += 2)
			{
				if (j > 2)
				{
					str += ",\n"
				}
				var tmpParam = fnInfo[j + 1];
				str += "	" + fnInfo[j] + ":" + tmpParam;
				if (typeof tmpParam == "object" && tmpParam != null)
				{
					if (tmpParam.name != null)
					{
						str += "[name:" + tmpParam.name + "]";
					}
					if (tmpParam.type != null)
					{
						str += "[type:" + tmpParam.type + "]";
					}
				}
			}
			str += "\n\n	" + fnInfo[0] + ":" + fnInfo[1];
		}
	}
	else
	{
		ex.dealed = true;
	}
	str += "\n\n\n-------------------------------------------------------------------\n\n";

	if (!noShow)
	{
		eg_cache.msgListWindow = this.showMessage(str, eg_cache.msgListWindow);
	}
	return str;
}

Eterna.prototype.pushFunctionStack = function(info)
{
	if (this.eterna_debug >= ED_FN_CALLED)
	{
		eterna_fn_stack.push(info);
	}
}

Eterna.prototype.popFunctionStack = function()
{
	if (this.eterna_debug >= ED_FN_CALLED)
	{
		eterna_fn_stack.pop();
	}
}


/**
 * 在框架外(或非初始化时)创建一个控件.
 * tName   typical-component的名称
 * parent  性创建的控件所在的父节点, 可以是节点id 也可以是节点对象,
 *         如果未给出此参数, 则直接放到body中
 * 例子:
 * var newObj = _eterna.newComponent("templateId", "parentId");
 */
Eterna.prototype.newComponent = function(tName, parent)
{
	var objConfig = null;
	if (typeof tName == "string")
	{
		objConfig = this.$E.T[tName];
	}
	else if (typeof tName == "object" && typeof tName.type == "string")
	{
		objConfig = tName;
	}
	if (objConfig == null)
	{
		return null;
	}
	var pObj = null;
	if (typeof parent == "object")
	{
		if (typeof parent.jquery == "string")
		{
			pObj = parent;
		}
		else
		{
			pObj = jQuery(parent);
		}
	}
	else if (typeof parent == "string")
	{
		pObj = jQuery("#" + parent);
	}
	else
	{
		pObj = this.rootWebObj == null ? jQuery("body") : this.rootWebObj;
	}
	var tmpParent = new WebObjList(pObj);
	var result = this.createComponent(objConfig, tmpParent);
	for (var i = 0; i < result.size(); i++)
	{
		var tmpObj = result.eq(i);
		pObj.append(tmpObj);
		eterna_doInitObjs(tmpObj);
	}
	if (result.size() == 1)
	{
		return result.eq(0);
	}
	return result;
}

/**
 * 在框架外调用一个方法.
 * fnName  要调用的方法的名称
 * ...     调用方法需要的参数
 * 例子:
 * _eterna.callFunction("getData_value", "userInfo", "id", 0);
 */
Eterna.prototype.callFunction = function(fnName)
{
	var fn = this.$E.F[fnName];
	if (fn != null)
	{
		var params = [];
		if (arguments.length > 1)
		{
			for (var i = 1; i < arguments.length; i++)
			{
				params.push(arguments[i]);
			}
		}
		var result = fn.apply(this, params);
		if (typeof result != "undefined")
		{
			return result;
		}
	}
}

/**
 * 在框架外获得一个方法对象.
 * fnName  要获得的方法的名称
 * 例子:
 * var fn = _eterna.getFunction("getData_value");
 * fn("userInfo", "id", 0);
 */
Eterna.prototype.getFunction = function(fnName)
{
	return this.$E.F[fnName];
}

/**
 * 在框架外获取或设置数据集中的值.
 * dataName  获取或设置的值的名称
 * value     要设置的值
 * 例子:
 * var value = _eterna.data("data1");
 * _eterna.data("data2", value);
 */
Eterna.prototype.data = function(dataName, value)
{
	var oldV = this.$E.D[dataName];
	if (typeof value != "undefined")
	{
		this.$E.D[dataName] = value;
	}
	return oldV;
}

/**
 * 执行一次页面访问, 会根据_eterna.cache.useAJAX的设置,
 * 进行页面跳转或ajax方式访问.
 * url  访问的地址
 */
Eterna.prototype.doVisit = function(url)
{
	if (this.cache.useAJAX)
	{
		this.ajaxVisit(url);
	}
	else
	{
		location.href = url;
	}
}

/**
 * 通过ajax获取数据并更新整个区域.
 * url         获取数据的地址
 * param       传递的参数
 * logHistory  是否要记录历史
 */
Eterna.prototype.ajaxVisit = function(url, param, logHistory)
{
	if (param != null)
	{
		logHistory = false;
	}
	var tmpURL = eterna_parseURL(this, url, false);
	var aIndex = tmpURL.indexOf("#");
	if (aIndex != 0 || param != null)
	{
		param = this.serializeFormData(param);
		param[EG_DATA_TYPE] = EG_DATA_TYPE_ALL;
		this.closeAllWindow();
		url = eterna_parseURL(this, url, true);
		var tmpData = this.getRemoteJSON(url, param, false);
		if (tmpData != null)
		{
			this.changeEternaData(tmpData);
			var oldTemp = eg_temp;
			try
			{
				eg_temp = {};
				this.reInit();
			}
			finally
			{
				eg_temp = oldTemp;
			}
		}
		else
		{
			var text = this.cache.textData;
			eterna_initTextData(this, text);
		}
		this.cache.textData = null;
	}
	else
	{
		url = eterna_parseURL(this, url, true);
	}
	eterna_gotoAnchor(this, url);
	this.changeLocation(url);
	if (logHistory !== false && eg_cache.logHistoryInAJAX)
	{
		// 停止hash值变化的监控
		eg_cache.stopHashChangeEvent = true;
		eterna_hashCodeURL(this.id, url);
	}
}

Eterna.prototype.changeLocation = function(url)
{
	var oldURL = this.cache.currentURL;
	this.cache.currentURL = url;
	url = eterna_getPurePath(url);
	if (url != "")
	{
		this.cache.location = url;
	}
	else
	{
		this.cache.location = null;
	}
	this.fireLocationChange(oldURL, this.cache.currentURL);
}
Eterna.prototype.fireLocationChange = function(oldURL, url)
{
	if (this.cache.locationListeners == null)
	{
		return;
	}
	for (var i = 0; i < this.cache.locationListeners.length; i++)
	{
		if (this.cache.locationListeners[i] != null)
		{
			this.cache.locationListeners[i](this, oldURL, url);
		}
	}
}

/**
 * 添加一个地址变化的监听者.
 * l  监听的方法, 需要3个参数, 第一个为eterna对象, 第二个为原来的地址,
 *    第三个为修改后的地址
 */
Eterna.prototype.addLocationListener = function(l)
{
	if (l == null)
	{
		return;
	}
	var firstNull = -1;
	if (this.cache.locationListeners == null)
	{
		this.cache.locationListeners = [l];
	}
	else
	{
		for (var i = 0; i < this.cache.locationListeners.length; i++)
		{
			if (this.cache.locationListeners[i] == null)
			{
				firstNull = i;
			}
			else if (this.cache.locationListeners[i] == l)
			{
				firstNull = i;
				break;
			}
		}
		if (firstNull == -1)
		{
			this.cache.locationListeners.push(l);
		}
		else
		{
			this.cache.locationListeners[firstNull] = l;
		}
	}
}

/**
 * 移除一个地址变化的监听者.
 */
Eterna.prototype.removeLocationListener = function(l)
{
	if (l == null || this.cache.locationListeners == null)
	{
		return;
	}
	for (var i = 0; i < this.cache.locationListeners.length; i++)
	{
		if (this.cache.locationListeners[i] == l)
		{
			this.cache.locationListeners[i] = null;
			break;
		}
	}
}

/**
 * 通过ajax方式重载部分区域.
 * url    重载的地址
 * param  传递的参数
 * objs   需要重载的控件列表
 * datas  需要更新的数据集名称列表
 */
Eterna.prototype.partReload = function(url, param, objs, datas)
{
	param = this.serializeFormData(param);
	param[EG_DATA_TYPE] = EG_DATA_TYPE_DATA;
	var tmpData = this.getRemoteJSON(url, param, false);
	this.cache.textData = null;
	if (this.$E.D[EG_SMA] == null && tmpData.$E.D[EG_SMA] != null)
	{
		this.$E.D[EG_SMA] = tmpData.$E.D[EG_SMA];
	}
	var datas = tData.reloadDatas;
	if (datas != null)
	{
		for (var i = 0; i < datas.length; i++)
		{
			this.$E.D[datas[i]] = tmpData.$E.D[datas[i]];
		}
	}
	var objs = tData.reloadObjs;
	if (objs != null)
	{
		for (var i = 0; i < objs.length; i++)
		{
			this.reloadWebObj(objs[i]);
		}
	}
	if (tmpData.$E.D.msg != null) alert(tmpData.$E.D.msg);
	this.$E.D.___pageChanged = 0;
}

Eterna.prototype.swapComponent = function(baseObj, subs, swapFlag)
{
	if (swapFlag == null)
	{
		swapFlag = EG_SWAP_FLAG;
	}
	var cacheObj = baseObj.data(EG_TEMPLATE_OBJ_FLAG);
	if (cacheObj == null)
	{
		cacheObj = {rootObj:baseObj};
		cacheObj[EG_FLAG_TAG] = swapFlag;
		baseObj.data(EG_TEMPLATE_OBJ_FLAG, cacheObj);
	}
	var oldTemplateRoot = eg_temp.sysTemplateRoot;
	eg_temp.sysTemplateRoot = cacheObj;
	try
	{
		for (var i = 0; i < subs.length; i++)
		{
			var name = subs[i].name;
			var pObj = jQuery("[" + swapFlag + "='" + ef_toScriptString(name) + "']", baseObj);
			if (pObj.size() == 1)
			{
				var infoObj = {name:name};
				infoObj[EG_FLAG_TAG] = swapFlag;
				pObj.data(EG_SWAP_INFO, infoObj);
				var tmpParent = new WebObjList(baseObj, pObj);
				var rList = this.createComponent(subs[i], tmpParent);
				var inheritInfo = pObj.data(EG_INHERIT_INFO);
				if (inheritInfo == null || !inheritInfo[EG_KEEP_BASE_OBJ])
				{
					// 如果模板节点没有被设置继承信息或保持信息, 则要替换掉
					for (var j = rList.size() - 1; j >= 0; j--)
					{
						pObj.after(rList.eq(j));
					}
					pObj.remove();
				}
			}
		}
		var specialObjs = this.queryWebObj("a, form", baseObj);
		for (var i = 0; i < specialObjs.size(); i++)
		{
			var tmpConfig = {name:"$specail",type:EG_INHERIT_FLAG};
			this.changeSpecialObjEvent(tmpConfig, specialObjs.eq(i));
		}
	}
	finally
	{
		eg_temp.sysTemplateRoot = oldTemplateRoot;
	}
}

Eterna.prototype.getValue_fromRecords = function(dataName, srcName, index)
{
	var valueObj = {html:0,exists:0};
	if (typeof dataName == "object")
	{
		valueObj.valueData = dataName;
		valueObj.srcName = srcName;
	}
	else
	{
		valueObj.valueData = this.$E.D[dataName];
		valueObj.dataName = dataName;
		valueObj.srcName = srcName;
	}
	if (typeof valueObj.valueData == "object")
	{
		if (index == null)
		{
			if (typeof valueObj.valueData.rowCount == "number"
					|| typeof valueObj.valueData.length == "number")
			{
				if ((this.eterna_debug & ED_FN_CALLED) != 0)
				{
					var msg = "The data";
					if (typeof valueObj.dataName == "string")
					{
						msg += ":[" + valueObj.dataName + "]"
					}
					msg += " must be a ResultRow."
					this.printException(msg);
				}
			}
			valueObj.value = valueObj.valueData[srcName];
			valueObj.exists = (typeof valueObj.value == "undefined") ? 0 : 1;
		}
		else
		{
			valueObj.index = index;
			try
			{
				if (typeof valueObj.valueData.rowCount == "number")
				{
					valueObj.value = valueObj.valueData.rows[index][valueObj.valueData.names[srcName] - 1];
				}
				else if (typeof valueObj.valueData.length == "number")
				{
					valueObj.value = valueObj.valueData[index][srcName];
				}
				else
				{
					if ((this.eterna_debug & ED_FN_CALLED) != 0)
					{
						var msg = "The data";
						if (typeof valueObj.dataName == "string")
						{
							msg += ":[" + valueObj.dataName + "]"
						}
						msg += " must be a ResultIterator."
						this.printException(msg);
					}
				}
			}
			catch (ex)
			{
				if ((this.eterna_debug & ED_GET_VALUE) != 0)
				{
					this.printException(ex);
				}
			}
			valueObj.exists = (typeof valueObj.value == "undefined") ? 0 : 1;
		}
	}
	return valueObj
}

Eterna.prototype.getValue = function(str, valueObj)
{
	if (str == null)
	{
		return {html:0,value:"",exists:0};
	}
	if (str == "")
	{
		return {html:0,value:"",exists:1};
	}
	// 如果是其他类型的数据, 需要转换成字符串
	str = str + "";
	if (str.indexOf("[script]:") == 0)
	{
		try
		{
			if (valueObj == null)
			{
				valueObj = {html:0,value:"",exists:-1};
			}
			var _eterna = this;
			var $E = this.$E;
			var eternaData = $E;
			var eterna_debug = this.eterna_debug;
			var tmpResult = eval(str.substring(9));
			if (valueObj.exists == -1)
			{
				if (!ef_isEmpty(tmpResult))
				{
					valueObj.exists = 1;
					valueObj.value = tmpResult;
				}
				else
				{
					valueObj.exists = 0;
				}
			}
			return valueObj;
		}
		catch (ex)
		{
			if ((this.eterna_debug & ED_GET_VALUE) != 0)
			{
				//var msg = "getValue(str:" + str + ",valueObj:" +　valueObj + ");\nex:" + ex + "/" + ex.message + "\n\n";
				this.pushFunctionStack(new Array("getValue", "str:" + str + ",valueObj:" +　valueObj));
				this.printException(ex);
				this.popFunctionStack();
			}
			if (valueObj != null)
			{
				valueObj.exists = 0;
				return valueObj;
			}
			else
			{
				return {html:0,value:"",exists:0};
			}
		}
	}
	else if (str.indexOf("[html]:") == 0)
	{
		return {html:1,value:str.substring(7),exists:1};
	}
	else if (str.indexOf("[text]:") == 0)
	{
		return {html:0,value:str.substring(7),exists:1};
	}
	else
	{
		return {html:0,value:str,exists:1};
	}
}

Eterna.prototype.executeScript = function(webObj, objConfig, scriptStr)
{
	var checkResult = true;
	try
	{
		var _eterna = this;
		var $E = this.$E;
		var eternaData = $E;
		var eterna_debug = this.eterna_debug;
		var eventData = webObj; //重命名一个变量, 在event处理中使用, 不用和webObj混淆
		var configData = objConfig; //使配置的名称可以与data中的一致
		eval(scriptStr);
	}
	catch (ex)
	{
		if ((this.eterna_debug & ED_EXECUTE_SCRIPT) != 0)
		{
			this.pushFunctionStack(new Array("executeScript", scriptStr));
			//var msg = "executeScript:{" + scriptStr + "}\nex:" + ex + "/" + ex.message + "\n\n";
			this.printException(ex);
			this.popFunctionStack();
		}
	}
	return checkResult;
}

Eterna.prototype.createComponent = function(configData, parent, options)
{
	if (configData == null || configData.type == null)
	{
		return null;
	}

	if (this.eterna_debug >= ED_COM_CREATED)
	{
		eterna_com_stack.push("name:" + configData.name + ",type:" + configData.type);
	}

	var temp = this.egTemp();
	if (options == null)
	{
		options = eg_temp.sysOptions;
	}
	if (options == null)
	{
		options = {virtualParent:false,inTemplate:false};
	}
	else
	{
		var oldVP = options.virtualParent;
		var oldIn = options.inTemplate;
		options = {virtualParent:oldVP,inTemplate:oldIn};
	}
	eg_temp.sysOptions = options;
	var nullParent = parent == null;
	if (parent == null)
	{
		parent = new WebObjList();
	}

	if (configData.beforeInit != null)
	{
		if (!this.executeScript(null, configData, configData.beforeInit))
		{
			if (this.eterna_debug >= ED_COM_CREATED)
			{
				eterna_com_stack.pop();
			}
			if (configData.type == EG_INHERIT_FLAG)
			{
				// 如果为继承的类型, 这要删除或隐藏控件
				var result = this.createInheritObj(configData, parent, options);
				if (result.webObj != null)
				{
					var tmpInfo = result.webObj.data(EG_INHERIT_INFO);
					if (tmpInfo[EG_KEEP_BASE_OBJ])
					{
						result.webObj.hide();
					}
					else
					{
						result.webObj.remove();
					}
				}
			}
			this.egTemp(temp);
			return null;
		}
	}

	var myTemp = this.egTemp();

	var returnNULL = false;
	var nullObj = null;
	var doLoop = false;
	var type = configData.type;
	var webObj = null;
	if (configData.creater != null && this.isFunction(configData.creater))
	{
		webObj = configData.creater(configData, parent.webObjList ? parent.realParent : parent);
	}
	else if (type == "tableForm")
	{
		webObj = this.createTableForm(configData);
	}
	else if (type == "tableList")
	{
		webObj = this.createTableList(configData);
	}
	else if (type == "none")
	{
		webObj = parent;
		returnNULL = true;
		options.virtualParent = true;
	}
	else if (type == "loop")
	{
		webObj = parent;
		returnNULL = true;
		doLoop = true;
		options.virtualParent = true;
	}
	else if (type == "replacement")
	{
		options.inTemplate = true;
		var tConfig = this.$E.T[configData.typicalComponent];
		webObj = this.createComponent(tConfig, parent, options);
	}
	else if (type == EG_INHERIT_FLAG)
	{
		var result = this.createInheritObj(configData, parent, options);
		returnNULL = result.returnNULL;
		nullObj = result.nullObj;
		webObj = result.webObj;
	}
	else
	{
		var index = type.indexOf("-");
		if (index != -1)
		{
			var tmpType = type.substring(0, index);
			var extType = type.substring(index + 1);
			if (tmpType.toLowerCase() == "input")
			{
				webObj = this.createWebObj(configData, "input", {name:"type",value:extType});
			}
			else
			{
				webObj = this.createWebObj(configData, tmpType, {name:"",value:extType});
			}
		}
		else
		{
			webObj = this.createWebObj(configData, type, null);
		}
	}

	if (webObj != null)
	{
		var oldTemplateRoot;
		if (configData[EG_TEMPLATE_OBJ_FLAG])
		{
			var tObjConfig = configData[EG_TEMPLATE_OBJ_FLAG];
			var tmpTemplateData = webObj.data(EG_TEMPLATE_OBJ_FLAG);
			if (tmpTemplateData == null)
			{
				tmpTemplateData = {rootObj:webObj};
				if (tObjConfig[EG_FLAG_TAG] != null)
				{
					tmpTemplateData[EG_FLAG_TAG] = tObjConfig[EG_FLAG_TAG];
				}
				webObj.data(EG_TEMPLATE_OBJ_FLAG, tmpTemplateData)
			}
			oldTemplateRoot = eg_temp.sysTemplateRoot;
			eg_temp.sysTemplateRoot = tmpTemplateData;
		}
		try
		{
			if (configData.subs != null && !configData[EG_NO_SUB])
			{
				if (doLoop)
				{
					this.dealLoopComponent(configData, webObj, options);
				}
				else
				{
					this.dealSubComponent(configData, webObj, options);
				}
			}
		}
		finally
		{
			if (configData[EG_TEMPLATE_OBJ_FLAG])
			{
				eg_temp.sysTemplateRoot = oldTemplateRoot;
			}
		}

		if (configData.init != null)
		{
			this.executeScript(returnNULL ? nullObj : webObj, configData, configData.init);
		}

		if (!returnNULL || nullObj != null)
		{
			var theObj = returnNULL ? nullObj : webObj;
			if (configData.events != null)
			{
				var tmpInfo = theObj.data(EG_INHERIT_INFO);
				if (tmpInfo == null || !tmpInfo[EG_OLD_BASE_OBJ])
				{
					this.dealEvents(configData, theObj, parent, myTemp);
				}
			}
			this.changeSpecialObjEvent(configData, theObj);
			if (parent.webObjList)
			{
				if (parent.realParent != null)
				{
					theObj.data("parentWebObj", parent.realParent);
				}
			}
			else
			{
				theObj.data("parentWebObj", parent);
			}
			theObj.data("configData", configData);
			theObj.data("objConfig", configData);
			theObj.data("egTemp", myTemp);
		}
		else if (type == "none" && configData.subs == null)
		{
			if (configData.text != null)
			{
				var tmpObj = this.getValue(configData.text);
				if (tmpObj.exists)
				{
					parent.append(tmpObj.value);
				}
			}
		}
	}

	this.egTemp(temp);
	if (this.eterna_debug >= ED_COM_CREATED)
	{
		eterna_com_stack.pop();
	}
	if (returnNULL)
	{
		// 如果没有给出parent, 或parent是webObj列表, 则返回webObj列表
		if (nullParent || parent.webObjList)
		{
			return parent;
		}
		return null;
	}
	return webObj;
}

/**
 * 针对一些特殊的控件, 需要替换掉它的原有事件.
 */
Eterna.prototype.changeSpecialObjEvent = function(configData, webObj)
{
	if (webObj.data("___specialEventChanged"))
	{
		return;
	}
	webObj.data("___specialEventChanged", 1);
	var elem = webObj.get(0);
	var tagA = jQuery.nodeName(elem, "a");
	var tagForm = jQuery.nodeName(elem, "form");
	if (tagA || tagForm)
	{
		var eventNames, eventNameOns, specialTypes;
		var count = 1;
		if (tagA)
		{
			eventNames = ["click"];
			eventNameOns = ["onclick"];
			specialTypes = ["a:click"];
		}
		else
		{
			eventNames = ["submit"];
			eventNameOns = ["onsubmit"];
			specialTypes = ["form:submit"];
		}
		for (var eventIndex = 0; eventIndex < count; eventIndex++)
		{
			var eventName = eventNames[eventIndex];
			var eventNameOn = eventNameOns[eventIndex];
			var specialType = specialTypes[eventIndex];
			var tmpParamData = {
				webObj:webObj,objConfig:configData,_eterna:this,specialType:specialType
			};
			var theEvents = webObj.data("events");
			if (theEvents != null)
			{
				theEvents = theEvents[eventName];
			}
			if (theEvents == null)
			{
				theEvents = [];
			}
			else
			{
				// 这里要把数组复制一份, 因为jQuery在remove事件时会把数组清空
				var newArr = [];
				for (var i = 0; i < theEvents.length; i++)
				{
					if (theEvents[i].handler == eterna_specialEventHandler)
					{
						// 如果事件的handler就是特殊事件, 则取出它的事件列表
						var tmpArr = theEvents[i].data.events;
						for (var j = 0; j < tmpArr.length; j++)
						{
							newArr.push(tmpArr[j]);
						}
					}
					else
					{
						newArr.push(theEvents[i]);
					}
				}
				theEvents = newArr;
			}
			tmpParamData.events = theEvents;
			if (elem[eventNameOn] != null)
			{
				tmpParamData.eventHandler = elem[eventNameOn];
				elem[eventNameOn] = null;
			}
			webObj.unbind(eventName);
			webObj.bind(eventName, tmpParamData, eterna_specialEventHandler);
		}
	}
}

Eterna.prototype.dealEvents = function(configData, webObj, parent, myTemp)
{
	for (var i = 0; i < configData.events.length; i++)
	{
		var theEvent = configData.events[i];
		var objParam = false;
		var tmpParamData = {
			webObj:webObj,objConfig:configData,_eterna:this,eventConfig:theEvent
		};

		if (parent != null)
		{
			if (parent.webObjList)
			{
				if (parent.realParent != null)
				{
					tmpParamData.parentWebObj = parent.realParent;
				}
			}
			else
			{
				tmpParamData.parentWebObj = parent;
			}
		}

		tmpParamData.egTemp = myTemp;
		if (theEvent.param != null && theEvent.param.indexOf("[script]:") == 0)
		{
			objParam = this.executeScript(tmpParamData, configData, theEvent.param.substring(9));
		}
		if (!objParam)
		{
			tmpParamData.eventParam = theEvent.param;
		}
		webObj.bind(theEvent.type, tmpParamData, eterna_normalEventHandler);
		var autoInit = false;
		if (tmpParamData.eventParam != null)
		{
			var ep = tmpParamData.eventParam;
			if ((typeof ep == "string" && ep == EG_AUTO_INIT_FLAG)
					|| (typeof ep == "object" && ep[EG_AUTO_INIT_FLAG]))
			{
				autoInit = true;
			}
		}
		if (theEvent.type == EG_EVENT_WILL_INIT && autoInit && !eterna_existsWillInitObj(webObj))
		{
			// 如果当前事件名称为[EG_EVENT_WILL_INIT]且当前对象未添加到待初始化列表中,
			// 则将其添加到列表中
			eterna_addWillInitObj(webObj);
		}
	}
}

Eterna.prototype.dealLoopComponent = function(configData, webObj, options)
{
	if (configData.loopCondition)
	{
		while (this.executeScript(null, configData, configData.loopCondition))
		{
			this.dealSubComponent(configData, webObj, options);
		}
	}
	else if (eg_temp.dataName != null)
	{
		var tmpData = (typeof eg_temp.dataName == "string") ?
				this.$E.D[eg_temp.dataName] : eg_temp.dataName;
		var useData = (typeof eg_temp.dataName == "string"); // 标识是否数据集里的数据
		if (tmpData != null)
		{
			var theCount = null;
			var theData = tmpData;
			if (typeof tmpData == "object")
			{
				if (typeof tmpData.rowCount == "number")
				{
					theCount = tmpData.rowCount;
				}
				else if (typeof tmpData.length == "number")
				{
					theCount = tmpData.length;
				}
			}
			else if (typeof tmpData == "number")
			{
				theCount = tmpData;
			}
			if (theCount != null)
			{
				var temp_index = eg_temp.index;
				for (var index = 0; index < theCount; index++)
				{
					eg_temp.index = index;
					this.dealSubComponent(configData, webObj, options);
					if (useData)
					{
						// 将数据重新赋值, 这样即使循环体里改变了, 这里能改回来
						this.$E.D[eg_temp.dataName] = theData;
					}
				}
				eg_temp.index = temp_index;
			}
		}
	}
}

Eterna.prototype.dealSubComponent = function(configData, webObj, options)
{
	for (var i = 0; i < configData.subs.length; i++)
	{
		var sub = configData.subs[i];
		var tmpObj = this.createComponent(sub, webObj, options);
		if (tmpObj != null)
		{
			webObj.append(tmpObj);
		}
	}
}

/**
 * 对于保留模板的继承控件, 第二次生成时, 需要清理其内部的子节点
 */
Eterna.prototype.clearInheritSub = function(webObj)
{
	var tmpSubs = webObj.children();
	for (var i = tmpSubs.size() - 1; i >= 0; i--)
	{
		var obj = tmpSubs.eq(i);
		if (obj.data("configData") == null)
		{
			// 没有配置信息, 说明到了原始节点, 清理完成
			break;
		}
		var tmpInfo = obj.data(EG_INHERIT_INFO);
		if (tmpInfo == null || !tmpInfo[EG_KEEP_BASE_OBJ])
		{
			// 如果为普通控件或不保留模板的继承控件, 则将其删除
			obj.remove();
		}
		else
		{
			// 否则说明到了保留模板的继承控件, 清理完成
			break;
		}
	}
}

Eterna.prototype.createInheritObj = function(configData, parent, options)
{
	var result = {returnNULL:true,nullObj:null,webObj:null};
	var baseObj = null;
	var tmpInheritInfo = null;
	var tmpInheritObj = null;
	var theParent = null;
	if (parent != null)
	{
		tmpInheritObj = parent.webObjList ? parent.inheritObj : parent;
		theParent = parent.webObjList ? parent.realParent : parent;
	}
	if (tmpInheritObj != null && typeof tmpInheritObj.jquery == "string"
			&& tmpInheritObj.data(EG_SWAP_INFO) != null && !options.virtualParent)
	{
		var swapInfo = tmpInheritObj.data(EG_SWAP_INFO);
		if (swapInfo.name == configData.name && tmpInheritObj.data(EG_INHERIT_INFO) == null)
		{
			result.webObj = tmpInheritObj;
			// 这里需要返回null, 但在初始化的时候需要有对象
			result.nullObj = tmpInheritObj;
			baseObj = tmpInheritObj;
			tmpInheritInfo = {name:configData.name};
			tmpInheritInfo[EG_FLAG_TAG] = swapInfo[EG_FLAG_TAG];
			tmpInheritInfo[EG_KEEP_BASE_OBJ] = 1;
			tmpInheritInfo.primaryDisplay = tmpInheritObj.css("display");
			var inheritBase = {baseObj:tmpInheritObj,name:configData.name};
			inheritBase[EG_FLAG_TAG] = swapInfo[EG_FLAG_TAG];
			inheritBase[EG_KEEP_BASE_OBJ] = 1;
			inheritBase.primaryDisplay = tmpInheritInfo.primaryDisplay;
			this.inheritBaseObj(configData, options.inTemplate, inheritBase);
		}
	}
	if (baseObj == null && configData[EG_INHERIT_GLOBAL_SEARCH] != null)
	{
		var searchObj = configData[EG_INHERIT_GLOBAL_SEARCH];
		tmpInheritInfo = this.findInheritObj(configData, jQuery("body"),
				searchObj[EG_FLAG_TAG], result, true, options.inTemplate);
		if (tmpInheritInfo != null)
		{
			baseObj = result.webObj;
		}
	}
	if (baseObj == null && theParent != null && theParent.data(EG_INHERIT_INFO) != null)
	{
		var inheritInfo = theParent.data(EG_INHERIT_INFO);
		tmpInheritInfo = this.findInheritObj(configData, theParent, inheritInfo[EG_FLAG_TAG],
				result, options.virtualParent, options.inTemplate);
		if (tmpInheritInfo != null)
		{
			baseObj = result.webObj;
		}
	}
	if (baseObj == null)
	{
		var tObjCache = eg_temp.sysTemplateRoot;
		if (tObjCache != null)
		{

			var rootObj = tObjCache.rootObj;
			var swapFlag = tObjCache[EG_FLAG_TAG];
			tmpInheritInfo = this.findInheritObj(configData, rootObj, swapFlag, result,
					true, options.inTemplate);
			if (tmpInheritInfo != null)
			{
				baseObj = result.webObj;
			}
		}
	}
	if (baseObj == null)
	{
		// 如果以上操作都没获取到模板控件, 则使用配置中保存的
		var ib = this.inheritBaseObj(configData, options.inTemplate);
		if (ib != null)
		{
			var keepBase = ib[EG_KEEP_BASE_OBJ];
			tmpInheritInfo = {name:ib.name};
			if (keepBase)
			{
				tmpInheritInfo.primaryDisplay = ib.primaryDisplay;
				tmpInheritInfo[EG_OLD_BASE_OBJ] = 1;
				baseObj = ib.baseObj;
				// 这里需要返回null, 但在初始化的时候需要有对象
				result.nullObj = baseObj;
				if (baseObj.css("display") != tmpInheritInfo.primaryDisplay)
				{
					baseObj.css("display", tmpInheritInfo.primaryDisplay);
				}
				this.clearInheritSub(baseObj);
			}
			else
			{
				baseObj = ib.baseObj.clone(true);
				result.returnNULL = false;
			}
			tmpInheritInfo[EG_FLAG_TAG] = ib[EG_FLAG_TAG];
			tmpInheritInfo[EG_KEEP_BASE_OBJ] = keepBase;
			result.webObj = baseObj;
		}
	}
	else
	{
		var ib = this.inheritBaseObj(configData, options.inTemplate);
		if (ib != null && tmpInheritInfo[EG_KEEP_BASE_OBJ])
		{
			if (ib.baseObj.get(0) == baseObj.get(0))
			{
				// 如果节点对象和配置中保存的节点对象相同, 则要进行清理
				tmpInheritInfo[EG_OLD_BASE_OBJ] = 1;
				if (baseObj.css("display") != tmpInheritInfo.primaryDisplay)
				{
					baseObj.css("display", tmpInheritInfo.primaryDisplay);
				}
				this.clearInheritSub(baseObj);
			}
		}
	}
	if (baseObj != null)
	{
		this.appendParam(baseObj, configData, null);
		if (configData.objValue != null)
		{
			var tmpObj = this.getValue(configData.objValue);
			if (tmpObj.exists)
			{
				baseObj.val(tmpObj.value);
			}
		}
		if (configData.text != null)
		{
			var tmpObj = this.getValue(configData.text);
			if (tmpObj.exists)
			{
				baseObj.text(tmpObj.value);
			}
		}
		baseObj.data(EG_INHERIT_INFO, tmpInheritInfo);
	}
	else
	{
		if ((this.eterna_debug & ED_EXECUTE_SCRIPT) != 0)
		{
			this.printException("The inheritObj's template not found or not valid!");
		}
	}
	return result;
}

Eterna.prototype.findInheritObj = function(configData, findBase, swapFlag, result,
		needClone, inTemplate)
{
	if (swapFlag == null)
	{
		swapFlag = EG_SWAP_FLAG;
	}
	var searchName = configData.name;
	if (configData[EG_SWAP_FLAG] != null)
	{
		searchName = configData[EG_SWAP_FLAG];
	}
	var gSearch = configData[EG_INHERIT_GLOBAL_SEARCH] != null;
	if (gSearch && this.cache[EG_SUFFIX_ID_FLAG] != null)
	{
		// 全局搜索时, 需要加上名称后缀
		searchName += this.cache[EG_SUFFIX_ID_FLAG];
	}
	var tmpInheritInfo = null;
	var tObj = jQuery("[" + swapFlag + "='" + ef_toScriptString(searchName) + "']", findBase);
	if (tObj.size() == 1 && tObj.data(EG_INHERIT_INFO) == null)
	{
		tmpInheritInfo = {name:searchName};
		tmpInheritInfo[EG_FLAG_TAG] = swapFlag;
		if (needClone)
		{
			tmpInheritInfo[EG_KEEP_BASE_OBJ] = 0;
			var inheritBase = {baseObj:tObj.clone(true),name:searchName};
			inheritBase[EG_FLAG_TAG] = swapFlag;
			inheritBase[EG_KEEP_BASE_OBJ] = 0;
			this.inheritBaseObj(configData, inTemplate, inheritBase);
			var baseObj = tObj.clone(true);
			result.webObj = baseObj;
			result.returnNULL = false;
			if (tObj.attr(EG_KEEP_OBJ_WHEN_USE) != "1")
			{
				tObj.remove();
			}
		}
		else
		{
			tmpInheritInfo[EG_KEEP_BASE_OBJ] = 1;
			tmpInheritInfo.primaryDisplay = tObj.css("display");
			var inheritBase = {baseObj:tObj,name:searchName};
			inheritBase[EG_FLAG_TAG] = swapFlag;
			inheritBase[EG_KEEP_BASE_OBJ] = 1;
			inheritBase.primaryDisplay = tmpInheritInfo.primaryDisplay;
			this.inheritBaseObj(configData, inTemplate, inheritBase);
			result.webObj = tObj;
			// 这里需要返回null, 但在初始化的时候需要有对象
			result.nullObj = tObj;
		}
	}
	return tmpInheritInfo;
}

Eterna.prototype.inheritBaseObj = function(configData, inTemplate, ibo)
{
	if (inTemplate)
	{
		var tObjCache = eg_temp.sysTemplateRoot;
		if (tObjCache != null)
		{
			var sId = configData[EG_INHERIT_BASE + "_sId"];
			if (sId == null)
			{
				sId = EG_INHERIT_BASE + eg_cache.serialId++;
				configData[EG_INHERIT_BASE + "_sId"] = sId;
			}
			if (ibo == null)
			{
				return tObjCache[sId];
			}
			else
			{
				tObjCache[sId] = ibo;
			}
		}
	}
	else
	{
		if (ibo == null)
		{
			return configData[EG_INHERIT_BASE];
		}
		else
		{
			configData[EG_INHERIT_BASE] = ibo;
		}
	}
}

Eterna.prototype.createWebObj = function(configData, type, extType)
{
	var objStr = type;
	if (extType != null && extType.name != "")
	{
		objStr += " " + extType.name + "=\"" + extType.value + "\"";
	}
	if (configData.objName != null)
	{
		var tmpObj = this.getValue(configData.objName);
		if (tmpObj.exists && !ef_isEmpty(tmpObj.value))
		{
			objStr += " name=\"" + tmpObj.value + "\"";
		}
	}
	else if (eg_temp.name != null)
	{
		var tStr = type.toLowerCase();
		if (tStr == "input" || tStr == "select" || tStr == "textarea" || tStr == "button")
		{
			objStr += " name=\"" + eg_temp.name + "\"";
		}
	}
	var obj = jQuery("<" + objStr + "/>");

	if (extType != null)
	{
		if (extType.name == "type" || extType.name == "")
		{
			type = type + "-" + extType.value;
		}
		else
		{
			type = type + "-" + extType.name + "." + extType.value;
		}
	}

	this.appendParam(obj, configData, type);
	if (configData.objValue != null)
	{
		var tmpObj = this.getValue(configData.objValue);
		if (tmpObj.exists)
		{
			obj.val(tmpObj.value);
		}
	}
	if (configData.text != null)
	{
		var tmpObj = this.getValue(configData.text);
		if (tmpObj.exists)
		{
			obj.text(tmpObj.value);
		}
	}
	else if (configData.html != null)
	{
		var tmpObj = this.getValue(configData.html);
		if (tmpObj.exists)
		{
			obj.html(tmpObj.value);
		}
	}

	return obj;
}

Eterna.prototype.createTR = function(configData, tableObj, type, model)
{
	if (configData != null && configData.preObj != null)
	{
		var oldTemp = eg_temp;
		var myTemp = configData.preTemp;
		eg_temp = myTemp;
		var preObj = configData.preObj;
		configData.preObj = null;
		configData.preTemp = null;
		if (configData.init != null)
		{
			this.executeScript(preObj, configData, configData.init);
		}
		if (configData.events != null)
		{
			this.dealEvents(configData, preObj, tableObj, myTemp);
		}
		preObj.data("parentWebObj", tableObj);
		preObj.data("configData", configData);
		preObj.data("egTemp", myTemp);
		eg_temp = oldTemp;
	}
	if (model == "final")
	{
		return;
	}
	if (model != "noBeforeInit" && configData != null && configData.beforeInit != null)
	{
		if (!this.executeScript(null, configData, configData.beforeInit))
		{
			return null;
		}
	}
	var trObj = jQuery("<tr></tr>");
	tableObj.append(trObj);
	this.appendParam(trObj, configData, type);
	if (configData != null)
	{
		if (model == "sub" && configData.subs != null)
		{
			this.dealSubComponent(configData, trObj);
		}
		configData.preObj = trObj;
		configData.preTemp = this.egTemp();
	}
	return trObj;
}

// 构造一个表单式的表格
Eterna.prototype.createTableForm = function(configData)
{
	var tableObj = jQuery("<table></table>");
	this.appendParam(tableObj, configData, "tableForm");

	var percentWidth = true;
	if (configData.percentWidth != null && !configData.percentWidth)
	{
		percentWidth = false;
	}

	var temp = this.egTemp();
	eg_caculateWidth_fix = 0;
	configData.rowOff = null;
	configData.used_counts = null;
	var columns = configData.columns;
	var columnLeft = columns.length;
	eg_temp.columnCount = columns.length;
	if (!percentWidth && configData.caculateWidth != null && configData.caculateWidth)
	{
		if (configData.caculateWidth_fix != null)
		{
			eg_caculateWidth_fix = configData.caculateWidth_fix;
		}
		else if (this.$E.G.tableForm.caculateWidth_fix != null)
		{
			eg_caculateWidth_fix = this.$E.G.tableForm.caculateWidth_fix;
		}
		var tmpWidth = this.ctf_getColWidth(columns, 0, columns.length, configData);
		// 这里只需再补上2个，因为在计算时，中间的已经加了columns.length - 1个
		tmpWidth += eg_caculateWidth_fix * 2;
		tableObj.attr("width", tmpWidth);
	}
	eg_temp.rowNum = 0;
	eg_temp.rowType = "row";

	if (configData.cells.length >= 1 && configData.cells[0].clearRowNum)
	{
		eg_temp.rowNum = -1;
	}
	var trObj = this.ctf_tr(configData.tr, tableObj);
	for(var i = 0; i < configData.cells.length; i++)
	{
		var cell = configData.cells[i];
		var tmpSize = this.ctf_getSize(cell.title, configData) + this.ctf_getSize(cell.container, configData);
		var tmpLeft = columnLeft;
		var needNewRow = false;
		if ((tmpSize > columnLeft && columnLeft < columns.length)
				|| (cell.clearRowNum && columnLeft < this.ctf_getColumnCount(configData)))
		{
			do
			{
				// 由于要在新的一行, 所以要将rowNum增1
				eg_temp.rowNum++;
				configData.rowOff = configData.rowOff == null ? 1 : configData.rowOff + 1;
				tmpLeft = this.ctf_getColumnCount(configData);
			} while (tmpLeft < tmpSize && tmpLeft < columns.length);
			needNewRow = true;
			// 可能要生成新的一行, 先将前一行的init-script执行了
			this.createTR(configData.tr, tableObj, "tableForm_tr", "final");
		}
		// 如果是要强制新行, 则将eg_temp.rowNum设为0
		var oldRowNum = eg_temp.rowNum;
		if (cell.clearRowNum)
		{
			eg_temp.rowNum = 0;
		}
		var cellObj = this.ctf_cell(columns, this.ctf_getColumnCount(configData) - tmpLeft, percentWidth, cell, configData);
		if (cell.clearRowNum)
		{
			// 恢复原来eg_temp.rowNum的值
			eg_temp.rowNum = oldRowNum;
		}
		if (configData.rowOff)
		{
			// 将rowNum恢复原值
			eg_temp.rowNum -= configData.rowOff;
			configData.rowOff = null;
		}
		if (cellObj.exists)
		{
			if (needNewRow)
			{
				do
				{
					if (columnLeft > 0)
					{
						var tdObj = this.ctf_container(null, columnLeft, 1);
						tdObj.html(eterna_table_td_empty_value);
						tdObj.attr("width",
								this.ctf_getColWidth(columns, this.ctf_getColumnCount(configData) - columnLeft, columnLeft, configData)
								+ (percentWidth ? "%" : ""));
						trObj.append(tdObj);
					}
					if (cell.clearRowNum)
					{
						eg_temp.rowNum = -1;
					}
					// 生成新的一行, 并且更新相关信息
					trObj = this.ctf_tr(configData.tr, tableObj);
					if (configData.used_counts != null)
					{
						configData.used_counts.shift();
						if (configData.used_counts.length == 0)
						{
							configData.used_counts = null;
						}
					}
					columnLeft = this.ctf_getColumnCount(configData);
				} while (columnLeft < tmpSize && columnLeft < columns.length);
			}
			if (cell.rowSpan != null && cell.rowSpan > 1)
			{
				this.ctf_createUsedList(configData, this.ctf_getColumnCount(configData) - columnLeft, cell);
			}
			if (cellObj.title != null)
			{
				trObj.append(cellObj.title);
			}
			if (cellObj.container != null)
			{
				trObj.append(cellObj.container);
			}
			columnLeft -= tmpSize;
		}
	}
	if (columnLeft > 0)
	{
		var tdObj = this.ctf_container(null, columnLeft, 1);
		tdObj.html(eterna_table_td_empty_value);
		tdObj.attr("width",
				this.ctf_getColWidth(columns, this.ctf_getColumnCount(configData) - columnLeft, columnLeft, configData)
				+ (percentWidth ? "%" : ""));
		trObj.append(tdObj);
	}
	this.createTR(configData.tr, tableObj, "tableForm_tr", "final");
	this.egTemp(temp);
	eg_caculateWidth_fix = 0;

	return tableObj;
}

// 生成已使用列的列表
Eterna.prototype.ctf_createUsedList = function(table, index, cell)
{
	if (table.used_counts == null)
	{
		table.used_counts = [];
	}
	if (table.used_counts.length == 0)
	{
		table.used_counts.push({count:0, spaned:this.ctf_createSpanedArr(table)});
	}
	var cellSize = this.ctf_getSize(cell.title, table) + this.ctf_getSize(cell.container, table);
	for (var i = 1; i < cell.rowSpan; i++)
	{
		if (table.used_counts.length < i + 1)
		{
			table.used_counts.push({count:cellSize, spaned:this.ctf_createSpanedArr(table)});
		}
		else if (i == 1)
		{
			table.used_counts[i].count += cellSize;
		}
		var tmpSize = cellSize;
		for (var j = 0; j < table.columns.length && tmpSize > 0; j++)
		{
			if (j < index)
			{
				if (table.used_counts[i].spaned[j])
				{
					index++;
				}
			}
			else
			{
				if (!table.used_counts[i].spaned[j])
				{
					table.used_counts[i].spaned[j] = true;
					tmpSize--;
				}
			}
		}
	}
}

// 创建一个和table的columns等长的数组
Eterna.prototype.ctf_createSpanedArr = function(table)
{
	var tmp = [];
	for (var i = 0; i < table.columns.length; i++)
	{
		tmp.push(false);
	}
	return tmp;
}

// 获得当前行的最大列数
Eterna.prototype.ctf_getColumnCount = function(table)
{
	if (table.used_counts != null)
	{
		if (table.rowOff && table.used_counts.length > table.rowOff)
		{
			return eg_temp.columnCount - table.used_counts[table.rowOff].count;
		}
		else
		{
			return eg_temp.columnCount - table.used_counts[0].count;
		}
	}
	return eg_temp.columnCount;
}

// 获得size属性, 如果没有默认为1
Eterna.prototype.ctf_getSize = function(configData, table)
{
	var tmpSize = 1;
	if (configData.size != null)
	{
		if (configData.size < 0)
		{
			tmpSize = this.ctf_getColumnCount(table) + configData.size + 1;
		}
		else
		{
			tmpSize = configData.size;
		}
	}
	return tmpSize;
}

Eterna.prototype.ctf_getColWidth = function(columns, index, count, table)
{
	var colCount = this.ctf_getColumnCount(table);
	if (index >= colCount)
	{
		return 0;
	}
	if (table.used_counts != null)
	{
		var tmpIndex = 0;
		if (table.rowOff)
		{
			tmpIndex = table.rowOff;
		}
		if (tmpIndex < table.used_counts.length)
		{
			var tmpColumns = [];
			for (var i = 0; i < columns.length; i++)
			{
				if (!table.used_counts[tmpIndex].spaned[i])
				{
					tmpColumns.push(columns[i]);
				}
			}
			columns = tmpColumns;
		}
	}
	var sumWidth = 0;
	for (var i = index; i < colCount && count > 0; i++, count--)
	{
		sumWidth += columns[i];
		if (i > index)
		{
			 sumWidth += eg_caculateWidth_fix;
		}
	}
	return sumWidth;
}

Eterna.prototype.ctf_cell = function(columns, colIndex, percentWidth, cell, table)
{
	var temp = this.egTemp();

	eg_temp.name = cell.name;
	if (cell.container.value != null)
	{
		eg_temp.dataName = cell.container.value.dataName;
		eg_temp.srcName = cell.container.value.srcName;
	}
	if (cell.initParam != null)
	{
		eg_temp.param = cell.initParam;
	}
	else
	{
		eg_temp.param = {};
	}
	var tmpObj;
	if (cell.container.value != null)
	{
		if (cell.container.needIndex && eg_temp.index != null)
		{
			tmpObj = this.getValue_fromRecords(cell.container.value.dataName, cell.container.value.srcName, eg_temp.index);
		}
		else
		{
			tmpObj = this.getValue_fromRecords(cell.container.value.dataName, cell.container.value.srcName);
		}
		if (!tmpObj.exists)
		{
			var tmpObj2 = this.getValue(cell.container.defaultValue);
			tmpObj.exists = tmpObj2.exists;
			tmpObj.html = tmpObj2.html;
			tmpObj.value = tmpObj2.value;
		}
	}
	else
	{
		tmpObj = this.getValue(cell.container.defaultValue);
	}
	eg_temp.valueObj = tmpObj;
	var valueObj = tmpObj;
	var titleObj;
	if (cell.title.caption != null)
	{
		titleObj = this.getValue(cell.title.caption);
		if (titleObj.exists && !ef_isEmpty(titleObj.value))
		{
			eg_temp.caption = titleObj.value;
		}
	}
	var cellObj = {title:null,container:null,exists:false};
	if (cell.beforeInit != null)
	{
		if (!this.executeScript(null, cell, cell.beforeInit))
		{
			this.egTemp(temp);
			return cellObj;
		}
	}

	var titleSize = this.ctf_getSize(cell.title, table);
	var tdObj_t = this.ctf_title(cell.title, titleSize, cell.rowSpan);
	if (tdObj_t != null)
	{
		cellObj.title = tdObj_t;
		cellObj.exists = true;
		if (cell.title.caption != null)
		{
			eterna_tableForm_title_fn(cell, titleObj, tdObj_t, tdObj_c, this);
		}
		tdObj_t.attr("width",
				this.ctf_getColWidth(columns, colIndex, titleSize, table)
				+ (percentWidth ? "%" : ""));
	}

	var containerSize = this.ctf_getSize(cell.container, table);
	var tdObj_c = this.ctf_container(cell.container, containerSize, cell.rowSpan);
	if (tdObj_c != null)
	{
		cellObj.container = tdObj_c;
		cellObj.exists = true;
		if (cell.subs == null && cell.typicalComponent == null)
		{
			if (valueObj.exists && !ef_isEmpty(valueObj.value))
			{
				if (valueObj.html)
				{
					tdObj_c.html(valueObj.value);
				}
				else
				{
					tdObj_c.text(valueObj.value);
				}
			}
			else
			{
				tdObj_c.html(eterna_table_td_empty_value);
			}
		}
		tdObj_c.attr("width",
				this.ctf_getColWidth(columns, colIndex + titleSize, containerSize, table)
				+ (percentWidth ? "%" : ""));

		if (cell.subs != null)
		{
			this.dealSubComponent(cell, tdObj_c);
		}

		if (cell.typicalComponent != null)
		{
			var tmpObj = this.createComponent(this.$E.T[cell.typicalComponent],
					tdObj_c, {virtualParent:false,inTemplate:true});
			if (tmpObj != null)
			{
				tdObj_c.append(tmpObj);
			}
		}
	}

	if (cell.init != null)
	{
		this.executeScript(tdObj_c, cell, cell.init);
	}

	this.egTemp(temp);
	return cellObj;
}

Eterna.prototype.ctf_title = function(configData, theSize, rowSpan)
{
	if (theSize == 0)
	{
		return null;
	}
	var tdObj = jQuery("<td></td>");
	if (theSize > 1)
	{
		tdObj.attr("colSpan", theSize);
	}
	if (rowSpan != null && rowSpan > 1)
	{
		tdObj.attr("rowSpan", rowSpan);
	}
	this.appendParam(tdObj, configData, "tableForm_title");
	return tdObj;
}

Eterna.prototype.ctf_container = function(configData, theSize, rowSpan)
{
	if (theSize == 0)
	{
		return null;
	}
	var tdObj = jQuery("<td></td>");
	if (theSize > 1)
	{
		tdObj.attr("colSpan", theSize);
	}
	if (rowSpan != null && rowSpan > 1)
	{
		tdObj.attr("rowSpan", rowSpan);
	}
	this.appendParam(tdObj, configData, "tableForm_container");
	return tdObj;
}

Eterna.prototype.ctf_tr = function(configData, tableObj)
{
	eg_temp.rowNum++; // table form 新生成一行, 所以row number就增1
	return this.createTR(configData, tableObj, "tableForm_tr", "normal");
}

// 构造一个列表式的表格
Eterna.prototype.createTableList = function(configData)
{
	var tableObj = jQuery("<table></table>");
	this.appendParam(tableObj, configData, "tableList");

	var percentWidth = true;
	if (configData.percentWidth != null && !configData.percentWidth)
	{
		percentWidth = false;
	}

	var temp = this.egTemp();
	// 先对每列执行初始化函数, 如果返回值为false则不显示该列
	var columns = new Array();
	var tmpColumns = configData.columns;
	eg_temp.rowNum = -1;
	for(var i = 0; i < tmpColumns.length; i++)
	{
		var column = tmpColumns[i];
		eg_temp.name = column.name;
		if (column.container.value != null)
		{
			eg_temp.dataName = column.container.value.dataName;
			eg_temp.srcName = column.container.value.srcName;
		}
		else
		{
			eg_temp.dataName = null;
			eg_temp.srcName = null;
		}
		if (column.initParam != null)
		{
			eg_temp.param = column.initParam;
		}
		else
		{
			eg_temp.param = {};
		}
		if (column.done)
		{
			column.title.upTitles = this.cloneJSON(column.title.originUpTitles);
			column.done = false;
		}
		else if (column.title.originUpTitles == null)
		{
			column.title.originUpTitles = this.cloneJSON(column.title.upTitles);
		}
		if (column.beforeInit != null)
		{
			if (this.executeScript(null, column, column.beforeInit))
			{
				columns.push(column);
			}
		}
		else
		{
			columns.push(column);
		}
	}
	eg_temp.columnCount = columns.length;

	eg_caculateWidth_fix = 0;
	if (!percentWidth && configData.caculateWidth != null && configData.caculateWidth)
	{
		if (configData.caculateWidth_fix != null)
		{
			eg_caculateWidth_fix = configData.caculateWidth_fix;
		}
		else if (this.$E.G.tableList.caculateWidth_fix != null)
		{
			eg_caculateWidth_fix = this.$E.G.tableList.caculateWidth_fix;
		}
		var tmpWidth = this.ctl_getColWidth(columns);
		if (tmpWidth != null)
		{
			// 这里只需再补上2个，因为在计算时，中间的已经加了columns.length - 1个
			tmpWidth += eg_caculateWidth_fix * 2;
			tableObj.attr("width", tmpWidth);
		}
	}

	this.ctl_tr(configData.tr, tableObj, "beforeTable");

	eg_temp.rowNum = 0;
	this.ctl_tr(configData.tr, tableObj, "beforeTitle");
	var maxLayer = this.ctl_dealUpTitle(columns);
	for (var layer = maxLayer - 1; layer >= 0; layer--)
	{
		var trObj = this.ctl_tr(configData.tr, tableObj, "title");
		if (trObj != null)
		{
			for(var i = 0; i < columns.length; i++)
			{
				var column = columns[i];
				this.ctl_title(percentWidth, trObj, column, layer);
			}
		}
	}
	this.ctl_tr(configData.tr, tableObj, "afterTitle");

	if (this.$E.D[configData.dataName] != null)
	{
		var tmpData = this.$E.D[configData.dataName];
		var rowCount = (typeof tmpData.rowCount == "number") ? tmpData.rowCount : tmpData.length;
		eg_temp.dataName = configData.dataName;
		var nowRowNum = 1;
		for (var index = 0; index < rowCount; index++)
		{
			eg_temp.index = index;
			eg_temp.rowNum = nowRowNum;
			var trObj = this.ctl_tr(configData.tr, tableObj, "row");
			if (trObj != null)
			{
				nowRowNum++;
				// beforeRow 在ctl_tr中处理
				for(var i = 0; i < columns.length; i++)
				{
					var column = columns[i];
					this.ctl_container(index, percentWidth, trObj, column);
				}
				this.ctl_tr(configData.tr, tableObj, "afterRow");
			}
		}
	}

	eg_temp.index = null;
	eg_temp.rowNum = -1;
	this.ctl_tr(configData.tr, tableObj, "afterTable");

	this.createTR(configData.tr, tableObj, "tableList_tr", "final");
	this.egTemp(temp);
	eg_caculateWidth_fix = 0;

	return tableObj;
}

Eterna.prototype.ctl_tr = function(configData, tableObj, rowType)
{
	if (rowType == null || rowType == "row" || rowType == "title")
	{
		eg_temp.rowType = rowType == null ? "row" : rowType;
		if (configData != null && configData.beforeInit != null)
		{
			if (!this.executeScript(null, configData, configData.beforeInit))
			{
				return null;
			}
		}
		if (eg_temp.rowType == "row")
		{
			this.ctl_tr(configData, tableObj, "beforeRow");
			eg_temp.rowType = "row";
		}
		return this.createTR(configData, tableObj, "tableList_tr", "noBeforeInit");
	}
	else
	{
		eg_temp.rowType = rowType;
		if (configData != null)
		{
			var trObj = null;
			var old_moreRow = eg_cache.moreRow;
			do
			{
				eg_cache.moreRow = false;
				trObj = this.createTR(configData, tableObj, "tableList_tr", "sub");
			} while (trObj != null && eg_cache.moreRow);
			eg_cache.moreRow = old_moreRow;
		}
	}
}

// 检查是否都是未使用的空格
Eterna.prototype.ctl_dealUpTitle_checkNone = function(columns, layer, start, count, setSame, upPlace)
{
	var end = start + count;
	for(var i = start; i < end; i++)
	{
		var column = columns[i];
		if (column.title.upTitles != null)
		{
			if (column.title.upTitles.length > layer)
			{
				if (column.title.upTitles[layer].none)
				{
					if (setSame)
					{
						column.title.upTitles[layer].none = false;
						if (upPlace && i == start)
						{
							column.title.upTitles[layer].up = true;
						}
						else
						{
							column.title.upTitles[layer].same = true;
						}
					}
				}
				else
				{
					return false;
				}
			}
		}
	}
	return true;
}

Eterna.prototype.ctl_dealUpTitle = function(columns)
{
	var maxLayer = 0;
	for(var i = 0; i < columns.length; i++)
	{
		var column = columns[i];
		if (column.title.upTitles != null)
		{
			if (column.title.upTitles.length > maxLayer)
			{
				// 取出最高的层数
				maxLayer = column.title.upTitles.length;
			}
			// 初始化未设值的层
			for (var j = 0; j < column.title.upTitles.length; j++)
			{
				var upTitle = column.title.upTitles[j];
				if (upTitle.colSpan != null)
				{
					upTitle.width = this.ctl_getColWidth(columns, i, upTitle.colSpan);
					for (var k = i + 1; k < columns.length; k++)
					{
						var tmpCol = columns[k];
						if (tmpCol.title.upTitles == null)
						{
							tmpCol.title.upTitles = new Array();
						}
						for (var tmpI = tmpCol.title.upTitles.length; tmpI <= j; tmpI++)
						{
							tmpCol.title.upTitles.push({none:true});
						}
						if (k - i < upTitle.colSpan)
						{
							tmpCol.title.upTitles[j].same = true;
							tmpCol.title.upTitles[j].none = false;
						}
					}
				}
			}
		}
	}

	for(var i = 0; i < columns.length; i++)
	{
		var column = columns[i];
		if (column.title.upTitles != null)
		{
			for (var j = column.title.upTitles.length - 1; j >= 0; j--)
			{
				var upTitle = column.title.upTitles[j];
				if (upTitle.colSpan != null)
				{
					upTitle.rowSpan = 1;
					// 检测并合并上面的空格
					for (var nowL = j + 1; nowL < maxLayer; nowL++)
					{
						if (this.ctl_dealUpTitle_checkNone(columns, nowL, i, upTitle.colSpan))
						{
							upTitle.rowSpan++;
							this.ctl_dealUpTitle_checkNone(columns, nowL, i, upTitle.colSpan, true, true);
						}
						else
						{
							break;
						}
					}
					// 检测并合并下方的空格
					for (var nowL = j - 1; nowL >= 0; nowL--)
					{
						if (this.ctl_dealUpTitle_checkNone(columns, nowL, i, upTitle.colSpan))
						{
							upTitle.rowSpan++;
							this.ctl_dealUpTitle_checkNone(columns, nowL, i, upTitle.colSpan, true, false);
						}
						else
						{
							break;
						}
					}
				}
			}
			// 处理底层标题的向上合并
			column.rowSpan = 1;
			for (var j = 0; j < column.title.upTitles.length; j++)
			{
				var upTitle = column.title.upTitles[j];
				if (upTitle.none)
				{
					column.rowSpan++;
					upTitle.none = false;
					upTitle.up = true;
				}
				else
				{
					break;
				}
			}
		}
		else if (maxLayer > 0)
		{
			column.rowSpan = maxLayer + 1;
		}
	}

	return maxLayer + 1; // 需要增加一层 本身的标题层
}

Eterna.prototype.ctl_getColWidth = function(columns, start, count)
{
	if (start == null)
	{
		start = 0;
	}
	if (count == null)
	{
		count = columns.length;
	}
	var sumWidth = 0;
	var end = start + count;
	for (var i = start; i < end; i++)
	{
		if (columns[i].width == null)
		{
			return null;
		}
		sumWidth += columns[i].width;
		if (i > 0)
		{
			 sumWidth += eg_caculateWidth_fix;
		}
	}
	return sumWidth;
}

Eterna.prototype.ctl_title = function(percentWidth, trObj, column, layer)
{
	var temp = this.egTemp();

	eg_temp.name = column.name;
	if (column.container.value != null)
	{
		eg_temp.dataName = column.container.value.dataName;
		eg_temp.srcName = column.container.value.srcName;
	}
	if (column.initParam != null)
	{
		eg_temp.param = column.initParam;
	}
	else
	{
		eg_temp.param = {};
	}

	var showTitle = true;
	if (column.title.upTitles != null)
	{
		if (layer > 0)
		{
			// 层数大于0, 表示不是底层标题
			var nowL;
			if (column.title.upTitles.length > layer - 1)
			{
				nowL = layer - 1;
			}
			else
			{
				nowL = column.title.upTitles.length - 1;
			}
			var upTitle = column.title.upTitles[nowL];
			if (upTitle.same || upTitle.done)
			{
				// 如果是已处理或是和其他格相同, 则跳过
				upTitle = null;
				showTitle = false;
			}
			else if (upTitle.up)
			{
				// 如果是提升处理, 则判断下层的格
				upTitle.done = true;
				upTitle = null;
				for (var i = nowL - 1; i >= 0; i--)
				{
					if (column.title.upTitles[i].up)
					{
						column.title.upTitles[i].done = true;
					}
					else
					{
						upTitle = column.title.upTitles[i];
						break;
					}
				}
			}
			if (upTitle != null)
			{
				showTitle = false;
				if (upTitle.none)
				{
					// 空格的处理
					var tdObj_t = jQuery("<td>" + eterna_table_td_empty_value + "</td>");
					this.appendParam(tdObj_t, upTitle, "tableList_title");
					if (column.width != null)
					{
						tdObj_t.attr("width", column.width + (percentWidth ? "%" : ""));
					}
					trObj.append(tdObj_t);
					upTitle.done = true;
				}
				else
				{
					// 上层标题的处理
					var tdObj_t = jQuery("<td></td>");
					this.appendParam(tdObj_t, upTitle, "tableList_title");
					if (upTitle.width != null)
					{
						tdObj_t.attr("width", upTitle.width + (percentWidth ? "%" : ""));
					}
					if (upTitle.rowSpan != null && upTitle.rowSpan > 1)
					{
						tdObj_t.attr("rowSpan", upTitle.rowSpan);
					}
					if (upTitle.colSpan != null && upTitle.colSpan > 1)
					{
						tdObj_t.attr("colSpan", upTitle.colSpan);
					}
					var tmpObj;
					if (upTitle.caption != null)
					{
						tmpObj = this.getValue(upTitle.caption);
					}
					else
					{
						tmpObj = {exists:0,value:"",html:0};
					}
					eterna_tableList_title_fn(column, tdObj_t, tmpObj, true, this);
					trObj.append(tdObj_t);
					upTitle.done = true;
				}
			}
		}
	}
	if (showTitle && !column.done)
	{
		var tdObj_t = jQuery("<td></td>");
		this.appendParam(tdObj_t, column.title, "tableList_title");
		if (column.rowSpan != null && column.rowSpan > 1)
		{
			tdObj_t.attr("rowSpan", column.rowSpan);
		}

		var tmpObj;
		if (column.title.caption != null)
		{
			tmpObj = this.getValue(column.title.caption);
			if (tmpObj.exists && !ef_isEmpty(tmpObj.value))
			{
				eg_temp.caption = tmpObj.value;
			}
		}
		else
		{
			tmpObj = {exists:0,value:"",html:0};
		}
		eterna_tableList_title_fn(column, tdObj_t, tmpObj, false, this);
		if (eg_temp.caption != null)
		{
			column.titleCaption = eg_temp.caption;
		}
		if (column.width != null)
		{
			tdObj_t.attr("width", column.width + (percentWidth ? "%" : ""));
		}
		trObj.append(tdObj_t);
		column.done = true;
	}

	this.egTemp(temp);
}

Eterna.prototype.ctl_container = function(index, percentWidth, trObj, column)
{
	var temp = this.egTemp();

	eg_temp.name = column.name;
	if (column.container.value != null)
	{
		eg_temp.dataName = column.container.value.dataName;
		eg_temp.srcName = column.container.value.srcName;
	}
	if (column.initParam != null)
	{
		eg_temp.param = column.initParam;
	}
	else
	{
		eg_temp.param = {};
	}
	if (column.titleCaption != null)
	{
		eg_temp.caption = column.titleCaption;
	}
	var tmpObj;
	if (column.container.value != null)
	{
		tmpObj = this.getValue_fromRecords(column.container.value.dataName, column.container.value.srcName, index);
		if (!tmpObj.exists)
		{
			var tmpObj2 = this.getValue(column.container.defaultValue);
			tmpObj.exists = tmpObj2.exists;
			tmpObj.html = tmpObj2.html;
			tmpObj.value = tmpObj2.value;
		}
	}
	else
	{
		tmpObj = this.getValue(column.container.defaultValue);
	}
	eg_temp.valueObj = tmpObj;
	if (column.beforeInit != null && !column.DBI)
	{
		if (!this.executeScript(null, column, column.beforeInit))
		{
			this.egTemp(temp);
			return;
		}
	}

	var tdObj_c = jQuery("<td></td>");
	trObj.append(tdObj_c);
	this.appendParam(tdObj_c, column.container, "tableList_container");

	if (column.subs == null && column.typicalComponent == null)
	{
		if (tmpObj.exists && !ef_isEmpty(tmpObj.value))
		{
			if (tmpObj.html)
			{
				tdObj_c.html(tmpObj.value);
			}
			else
			{
				tdObj_c.text(tmpObj.value);
			}
		}
		else
		{
			tdObj_c.html(eterna_table_td_empty_value);
		}
	}
	if (column.width != null)
	{
		tdObj_c.attr("width", column.width + (percentWidth ? "%" : ""));
	}

	if (column.subs != null)
	{
		this.dealSubComponent(column, tdObj_c);
	}

	if (column.typicalComponent != null)
	{
		var tmpObj = this.createComponent(this.$E.T[column.typicalComponent],
				tdObj_c, {virtualParent:false,inTemplate:true});
		if (tmpObj != null)
		{
			tdObj_c.append(tmpObj);
		}
	}
	if (column.init != null)
	{
		this.executeScript(tdObj_c, column, column.init);
	}

	this.egTemp(temp);
}

Eterna.prototype.appendParam = function(obj, configData, objType)
{
	if (configData == null || configData.ignoreGlobal == null || !configData.ignoreGlobal)
	{
		if (objType != null && this.$E.G[objType] != null)
		{
			if (this.$E.G[objType].className != null)
			{
				obj.addClass(this.$E.G[objType].className);
			}
			if (this.$E.G[objType].attr != null)
			{
				obj.attr(this.$E.G[objType].attr);
			}
			if (this.$E.G[objType].css != null)
			{
				obj.css(this.$E.G[objType].css);
			}
			if (this.$E.G[objType].prop != null)
			{
				obj.prop(this.$E.G[objType].prop);
			}
		}
	}
	if (configData != null)
	{
		if (configData.className != null)
		{
			obj.addClass(configData.className);
		}
		if (configData.attr != null)
		{
			obj.attr(configData.attr);
		}
		if (configData.css != null)
		{
			obj.css(configData.css);
		}
		if (configData.prop != null)
		{
			obj.prop(configData.prop);
		}
	}
}

// 检查框架的数据是否合法, 不完整的将会补充完整, 并且在数据结构上与老版本兼容
var EG_ED_COMPATIBLE_NAMES = {
	G:"global", D:"records", V:"view", T:"typical", F:"eFns", R:"res"
};
function eterna_checkEternaData($E)
{
	if ($E == null)
	{
		$E = {G:{},D:{},V:[]};
	}
	else
	{
		if ($E.G == null)
		{
			$E.G = {};
		}
		if ($E.D == null)
		{
			$E.D = {};
		}
		if ($E.V == null)
		{
			$E.V = [];
		}
	}
	for (var key in $E)
	{
		if (EG_ED_COMPATIBLE_NAMES[key] != null && $E[EG_ED_COMPATIBLE_NAMES[key]] == null)
		{
			$E[EG_ED_COMPATIBLE_NAMES[key]] = $E[key];
		}
	}
	return $E;
}

function eterna_initEternaCache(_eterna, newData)
{
	if (newData != null && newData.cache != null)
	{
		var cache = newData.cache;
		for (var key in cache)
		{
			_eterna.cache[key] = cache[key];
		}
	}
	if (_eterna.cache.initialized == null)
	{
		_eterna.cache.initialized = 1;
	}
}

function eterna_checkBinded(_eterna)
{
	if (_eterna.rootWebObj != null)
	{
		var oldEterna = _eterna.rootWebObj.data(EG_BINDED_ETERNA);
		if (oldEterna == null)
		{
			_eterna.rootWebObj.data(EG_BINDED_ETERNA, _eterna);
		}
		else if (oldEterna != _eterna)
		{
        	if ((_eterna.eterna_debug & ED_SHOW_OTHERS) != 0)
			{
				var id1 = _eterna.id;
				var id2 = oldEterna.id;
				alert("Tow eterna instance (" + id1 + ", " + id2 + ") binded same obj.");
			}
		}
	}
}

/**
 * 当获得的对象不是json时, 将数据以html的方式放入到根节点中.
 */
function eterna_initTextData(_eterna, textData)
{
	var oldEterna = eg_cache.currentEterna;
	eg_cache.currentEterna = _eterna;
	try
	{
		if (_eterna.rootWebObj != null)
		{
			_eterna.rootWebObj.html(textData);
			eterna_doInitObjs(_eterna.rootWebObj);
			var specialObjs = _eterna.queryWebObj("a, form");
			for (var i = 0; i < specialObjs.size(); i++)
			{
				var tmpConfig = {name:"$specail",type:EG_INHERIT_FLAG};
				_eterna.changeSpecialObjEvent(tmpConfig, specialObjs.eq(i));
			}
		}
	}
	finally
	{
		eg_cache.currentEterna = oldEterna;
	}
}

// 添加一个需要等待初始化的对象
window.eterna_addWillInitObj = function(obj, priority)
{
	if (priority == null)
	{
		eg_cache.willInitObjs.push({obj:obj,p:100});
		return;
	}
	var tmp = parseInt(priority);
	if (isNaN(tmp) || tmp < 0 || tmp >= 100)
	{
		throw new Error("Error priority:[" + priority + "], must in [0, 99].");
	}
	eg_cache.willInitObjs.push({obj:obj,p:tmp});
	var tmpI = eg_cache.willInitObjs.length - 1;
	for (var i = 0; i < eg_cache.willInitObjs.length; i++)
	{
		if (tmp < eg_cache.willInitObjs[i].p)
		{
			tmpI = i;
			break;
		}
	}
	if (tmpI < eg_cache.willInitObjs.length - 1)
	{
		eterna_moveArrayValue(eg_cache.willInitObjs,
				eg_cache.willInitObjs.length - 1, tmpI);
	}
}

// 检查参数中的对象是否在等待初始化的对象列表中
function eterna_existsWillInitObj(obj)
{
	for (var i = 0; i < eg_cache.willInitObjs.length; i++)
	{
		if (obj == eg_cache.willInitObjs[i].obj)
		{
			return true;
		}
	}
	return false;
}

// 注册一个静态的初始化方法, 如果priority为-1, 则删除已注册的
window.eterna_registerStaticInitFn = function(fn, priority)
{
	var delIndex = -1;
	for (var i = 0; i < eg_cache.staticInitFns.length; i++)
	{
		if (eg_cache.staticInitFns[i] != null && fn == eg_cache.staticInitFns[i].fn)
		{
			eg_cache.staticInitFns[i] = null;
			delIndex = i;
			break;
		}
	}
	if (delIndex != -1)
	{
		eterna_moveArrayValue(eg_cache.staticInitFns,
				delIndex, eg_cache.staticInitFns.length - 1);
	}
	var lastIndex = eg_cache.staticInitFns.length > 0 ? eg_cache.staticInitFns.length - 1 : -1;
	if (priority == null)
	{
		if (lastIndex >= 0 && eg_cache.staticInitFns[lastIndex] == null)
		{
			eg_cache.staticInitFns[lastIndex] = {fn:fn,p:100};
		}
		else
		{
			eg_cache.staticInitFns.push({fn:fn,p:100});
		}
		return;
	}
	var tmp = parseInt(priority);
	if (tmp == -1)
	{
		return;
	}
	if (isNaN(tmp) || tmp < 0 || tmp >= 100)
	{
		throw new Error("Error priority:[" + priority + "], must in [0, 99].");
	}
	if (lastIndex >= 0 && eg_cache.staticInitFns[lastIndex] == null)
	{
		eg_cache.staticInitFns[lastIndex] = {fn:fn,p:tmp};
	}
	else
	{
		eg_cache.staticInitFns.push({fn:fn,p:tmp});
	}
	var tmpI = eg_cache.staticInitFns.length - 1;
	for (var i = 0; i < eg_cache.staticInitFns.length; i++)
	{
		if (eg_cache.staticInitFns[i] != null && tmp < eg_cache.staticInitFns[i].p)
		{
			tmpI = i;
			break;
		}
	}
	if (tmpI < eg_cache.staticInitFns.length - 1)
	{
		eterna_moveArrayValue(eg_cache.staticInitFns,
				eg_cache.staticInitFns.length - 1, tmpI);
	}
}

function eterna_moveArrayValue(arr, fromIndex, toIndex)
{
	var tmp = arr[fromIndex];
	if (fromIndex > toIndex)
	{
		for (var i = fromIndex; i > toIndex; i--)
		{
			arr[i] = arr[i - 1];
		}
	}
	else
	{
		for (var i = fromIndex; i < toIndex; i++)
		{
			arr[i] = arr[i + 1];
		}
	}
	arr[toIndex] = tmp;
}

function eterna_allEternaReady()
{
	for (var key in eg_cache.eternaCache)
	{
		var _eterna = eg_cache.eternaCache[key];
		if (_eterna != null && !_eterna.cache.initialized)
		{
			return false;
		}
	}
   return true;
}

// 触发所有等待初始化的对象的[EG_EVENT_WILL_INIT]事件
// 然后执行所有的静态初始化方法
window.eterna_doInitObjs = function(theObj)
{
	if (!eterna_allEternaReady())
	{
		return;
	}
	var objs = eg_cache.willInitObjs;
	eg_cache.willInitObjs = [];
	for (var i = 0; i < objs.length; i++)
	{
		var tmpObj = objs[i].obj;
		try
		{
			if (typeof tmpObj.jquery == "string")
			{
				tmpObj.trigger(EG_EVENT_WILL_INIT);
			}
			else
			{
				// 如果不是jQuery对象, 则作为方法调用
				tmpObj();
			}
		}
		catch (ex) {}
	}
	if (theObj != null)
	{
		var fns = eg_cache.staticInitFns;
		for (var i = 0; i < fns.length; i++)
		{
			try
			{
				if (fns[i] != null)
				{
					fns[i].fn(theObj);
				}
			}
			catch (ex) {}
		}
	}
}

// 获取打开此窗口的eterna对象
window.eterna_getParentEterna = function(winObj)
{
	for (var i = 0; i < eg_cache.openedObjs.length; i++)
	{
		if (eg_cache.openedObjs[i].winObj == winObj)
		{
			return eg_cache.openedObjs[i].openedEterna;
		}
	}
	return null;
}

// 页面重载是关闭所有打开的窗口
function eterna_closeAllWindow()
{
	for (var i = 0; i < eg_cache.openedObjs.length; i++)
	{
		var tmpObj = eg_cache.openedObjs[i];
		if (tmpObj.winObj != null && !tmpObj.winObj.closed)
		{
			tmpObj.winObj.close();
		}
	}
}

/**
 * 当地址栏中的hash值发生变化时, 要处理历史结果
 */
function eterna_hashChange(event)
{
	if (eg_cache.stopHashChangeEvent)
	{
		// 监控每次只能被停止1轮, 这里重新开启
		eg_cache.stopHashChangeEvent = false;
		return;
	}
	var hObj = eterna_parseHashCodeURL();
	for (var key in eg_cache.eternaCache)
	{
		var _eterna = eg_cache.eternaCache[key];
		if (_eterna != null)
		{
			var tmpURL = hObj[key];
			if (tmpURL != null && _eterna.cache.currentURL != tmpURL)
			{
				_eterna.ajaxVisit(tmpURL, null, false);
			}
			else if (tmpURL == null && _eterna.cache.primitiveURL != _eterna.cache.currentURL)
			{
				_eterna.ajaxVisit(_eterna.cache.primitiveURL, null, false);
			}
		}
	}
}

/**
 * 获取或设置eternaId和url的对应关系
 */
function eterna_hashCodeURL(eternaId, url)
{
	if (typeof url == "undefined")
	{
		var hObj = eterna_parseHashCodeURL();
		return hObj[eternaId];
	}
	else
	{
		var hObj = eterna_parseHashCodeURL();
		hObj[eternaId] = url;
		eterna_parseHashCodeURL(hObj);
	}
}

/**
 * 解析或设置地址栏中的hash值, eternaId和url的对应关系
 */
function eterna_parseHashCodeURL(obj)
{
	if (obj == null)
	{
		var index = location.href.indexOf('#');
		var obj = {};
		if (index == -1)
		{
			return obj;
		}
		var hCode = decodeURIComponent(location.href.substring(index + 1));
		if (hCode.indexOf("$EH:\n") == 0)
		{
			var arr = hCode.substring(5).split("\n");
			for (var i = 0; i < arr.length; i++)
			{
				var tmpI = arr[i].indexOf('.');
				obj[arr[i].substring(0, tmpI)] = arr[i].substring(tmpI + 1);
			}
		}
		return obj;
	}
	else
	{
		var index = location.href.indexOf('#');
		var emptyObj = true;
		var str = "$EH:";
		for (var key in obj)
		{
			var tmpURL = obj[key];
			if (tmpURL != null)
			{
				str += "\n" + key + "." + tmpURL;
				emptyObj = false;
			}
		}
		if (!emptyObj || index != -1)
		{
			location.href = "#" + encodeURIComponent(str);
		}
		return str;
	}
}

if (typeof jQuery == 'undefined')
{
	if (window.addEventListener)
	{
		window.addEventListener("unload", eterna_closeAllWindow, false);
		window.addEventListener("hashchange", eterna_hashChange, false);
	}
	else if (window.attachEvent)
	{
		window.attachEvent("onunload", eterna_closeAllWindow);
		window.attachEvent("onhashchange", eterna_hashChange);
	}
	else
	{
		window.onunload = eterna_closeAllWindow;
		window.onhashchange = eterna_hashChange;
	}
}
else
{
	jQuery(window).unload(eterna_closeAllWindow);
	jQuery(window).bind("hashchange", eterna_hashChange);
}

/**
 * 获取文本资源的值
 */
window.eterna_getResourceValue = function(resArray, params)
{
	if (params == null)
	{
		params = [];
	}
	if (params.length == 1 && jQuery.isArray(params[0]))
	{
		params = params[0];
	}
	var str = "";
	for (var i = 0; i < resArray.length; i++)
	{
		var res = resArray[i];
		if (typeof res == "number")
		{
			if (res < params.length && params[res] != null)
			{
				str += params[res];
			}
		}
		else
		{
			str += res;
		}
	}
	return str;
}

/**
 * 执行特殊控件的特殊事件.
 */
function eterna_specialEventHandler(event)
{
	var webObj = event.data.webObj;
	var specialType = event.data.specialType;
	var _eterna = event.data._eterna;
	var events = event.data.events;
	var eventHandler = event.data.eventHandler;
	var oldData = event.data;
	var oldHandleObj = event.handleObj;
	var needExecute = true;
	try
	{
		if (eventHandler != null)
		{
			event.data = null;
			event.handleObj = null;
			if (eventHandler.call(event.target, event) === false)
			{
				needExecute = false;
			}
		}
		for (var i = 0; i < events.length; i++)
		{
			var eObj = events[i];
			event.data = eObj.data;
			event.handleObj = eObj;
			if (eObj.handler.call(event.target, event) === false)
			{
				needExecute = false;
			}
		}
	}
	catch (ex)
	{
		_eterna.pushFunctionStack(new Array("eterna_specialEventHandler", eterna_specialEventHandler));
		_eterna.printException(ex);
		_eterna.popFunctionStack();
	}
	event.data = oldData;
	event.handleObj = oldHandleObj;
	if (needExecute)
	{
		var target = webObj.attr("target");
		if (_eterna.cache.useAJAX && !event[EG_STOP_AJAX] && webObj.attr(EG_STOP_AJAX) == null
				&& (target == null || target == ""))
		{
			if (specialType == "a:click")
			{
				var url = webObj.attr("href");
				if (url == null || (url == "" && document.all))
				{
					// 未设置href时返回true触发事件的冒泡
					return true;
				}
				if (url == "#")
				{
					return false;
				}
				if (eterna_checkScriptStr(url))
				{
					return true;
				}
				_eterna.ajaxVisit(url, event.clickParam);
				return false;
			}
			else if (specialType == "form:submit")
			{
				var action = webObj.attr("action");
				if (action == null)
				{
					return false;
				}
				if (eterna_checkScriptStr(action))
				{
					return true;
				}
				var tData = eg_temp.tempData;
				if (tData == null || (tData.reloadObjs == null && tData.reloadDatas == null))
				{
					_eterna.ajaxVisit(action, webObj);
				}
				else
				{
					action = eterna_parseURL(_eterna, action, true);
					_eterna.partReload(action, webObj, tData.reloadObjs, tData.reloadDatas);
				}
				return false;
			}
		}
		else
		{
			return true;
		}
	}
	else
	{
		return false;
	}
}

/**
 * 解析给出的地址, 补上实际的location.
 */
function eterna_parseURL(_eterna, url, autoAddLocation)
{
	var tmpLocation = "";
	if (autoAddLocation)
	{
		tmpLocation = _eterna.cache.location != null ? _eterna.cache.location : "";
	}
	if (url == null)
	{
		return tmpLocation;
	}
	if (typeof url != "string")
	{
		url = url + "";
	}
	var lURL = eterna_getPureLocation(url);
	var lHref = eterna_getPureLocation(location.href);
	if (lURL == lHref)
	{
		// 如果纯地址部分和location的纯地址部分相同, 则判断为是ie浏览器补上的
		url = url.substring(lURL.length);
	}
	if (url.length > 0)
	{
		var index = url.indexOf("?");
		if (index == 0)
		{
			return tmpLocation + url;
		}
		else
		{
			index = url.indexOf("#");
			if (index == 0)
			{
				return tmpLocation + url;
			}
		}
		return url;
	}
	else
	{
		return tmpLocation;
	}
}

/**
 * 获得纯路径, 不需要服务器 参数及锚点部分.
 */
function eterna_getPurePath(url)
{
	url = eterna_getPureLocation(url);
	var index = url.indexOf("://");
	if (index == -1)
	{
		return url;
	}
	index = url.indexOf("/", index + 3);
	if (index == -1)
	{
		return "";
	}
	return url.substring(index);
}

/**
 * 获得纯地址, 不需要参数及锚点部分.
 */
function eterna_getPureLocation(url)
{
	if (url == null)
	{
		return "";
	}
	if (typeof url != "string")
	{
		url = url + "";
	}
	var index = url.indexOf("?");
	if (index >= 0)
	{
		url = url.substring(0, index);
	}
	else
	{
		index = url.indexOf("#");
		if (index >= 0)
		{
			url = url.substring(0, index);
		}
	}
	return url;
}

/**
 * ajax请求完成后, 将位置移动到锚点.
 */
function eterna_gotoAnchor(_eterna, url)
{
	if (url == null)
	{
		return;
	}
	if (typeof url != "string")
	{
		return;
	}
	var index = url.indexOf("#");
	if (index == -1)
	{
		return;
	}
	var name = url.substring(index + 1);
	var obj = _eterna.queryWebObj("a[name='" + ef_toScriptString(name) + "']");
	if (obj.size() == 0)
	{
		return;
	}
	var obj = obj.eq(0);
	var posObj = obj.position();
	var posX = posObj.left;
	var posY = posObj.top;
	if (posX != 0 || posY != 0)
	{
		if (posX + obj.width() > window.document.body.clientWidth)
		{
			posX = posX + obj.width() - window.document.body.clientWidth;
		}
		window.scrollTo(posX, posY);
	}
}

/**
 * 检查是否为执行脚本的字符串.
 */
function eterna_checkScriptStr(str)
{
	if (str == null)
	{
		return false;
	}
	if (typeof str != "string")
	{
		return false;
	}
	str = str.toLowerCase();
	for (var i = 0; i < EG_SCRIPT_STR_PREFIX_ARR.length; i++)
	{
		if (str.indexOf(EG_SCRIPT_STR_PREFIX_ARR[i]) == 0)
		{
			return true;
		}
	}
	return false;
}

/**
 * 执行普通的事件.
 */
function eterna_normalEventHandler(event)
{
	var eventConfig = event.data.eventConfig;
	if (eventConfig.type == EG_EVENT_WILL_INIT)
	{
		// 如果为[EG_EVENT_WILL_INIT], 则要停止事件的冒泡
		event.stopPropagation();
	}
	var _eterna = event.data._eterna;
	var tempBak = _eterna.egTemp();
	try
	{
		_eterna.egTemp(event.data.egTemp);
		var webObj = event.data.webObj;
		var objConfig = event.data.objConfig;
		var result = eventConfig.fn.call(event.target, event, webObj, objConfig);
		if (typeof result != "undefined")
		{
			return result;
		}
	}
	finally
	{
		_eterna.egTemp(tempBak);
	}
}

/**
 * 检查远程返回的数据是否是JSON类型.
 */
function eterna_checkRemoteData(str)
{
	if (str == null)
	{
		return false;
	}
	var endPos = str.length;
	for (var i = 0; i < str.length; i++)
	{
		if (str.charCodeAt(i) > 0x20 && str.charAt(i) == '{')
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	return false;
}

/**
 * 处理获得的远程数据, 去除字符串右边的空白字符, 存储html文本等.
 */
function eterna_dealRemoteData(_eterna, str)
{
	if (str == null)
	{
		return null;
	}
	var endPos = str.length;
	for (var i = str.length - 1; i >= 0; i--)
	{
		if (str.charCodeAt(i) <= 0x20)
		{
			endPos--;
		}
		else
		{
			break;
		}
	}
	str = str.substring(0, endPos);
	var index = str.indexOf(EG_JSON_SPLIT_FLAG);
	if (index != -1)
	{
		if (index + EG_JSON_SPLIT_FLAG.length < str.length)
		{
			var oldEterna = eg_cache.currentEterna;
			eg_cache.currentEterna = _eterna;
			try
			{
				var tmpDiv = jQuery("<div/>");
				tmpDiv.html(str.substring(index + EG_JSON_SPLIT_FLAG.length));
				var dataDiv = jQuery("#" + EG_HTML_DATA_DIV);
				if (dataDiv.size() == 0)
				{
					dataDiv = jQuery("<div id=\"" + EG_HTML_DATA_DIV + "\"/>");
					dataDiv.hide();
					jQuery("body").append(dataDiv);
				}
				var tmpSubs = tmpDiv.children();
				for (var i = 0; i < tmpSubs.size(); i++)
				{
					dataDiv.append(tmpSubs.eq(i));
				}
			}
			finally
			{
				eg_cache.currentEterna = oldEterna;
			}
		}
		return str.substring(0, index);
	}
	return str;
}

/**
 * 动态载入一个Ererna对象.
 * url        初始化此Ererna对象的url
 * param      请求url地址时需要传递的参数
 * parentObj  生成的界面所在的父节点, 必须是jQuery对象
 * useAJAX    是否需要自动将请求转为ajax的方式
 * debug      调试信息的输出等级
 * recall     如果需要异步加载, 要给出回调函数
 */
window.ef_loadEterna = function(url, param, parentObj, useAJAX, debug, recall)
{
	var oldEterna = null;
	if (parentObj != null)
	{
		// 如果父对象中已绑定了一个Eterna对象, 则要先将其清除
    	var oldEterna = parentObj.data(EG_BINDED_ETERNA);
    	if (oldEterna != null)
    	{
    		oldEterna.destroy(false, url);
		}
	}
	var eterna_debug = debug;
	var $E = {};
	var eternaData = $E;
	var _eterna;
	if (oldEterna == null)
	{
		_eterna = new Eterna($E, debug, parentObj);
		_eterna.cache.primitiveURL = url;
		var tmpURL = eterna_hashCodeURL(_eterna.id);
		if (tmpURL != null)
		{
			url = tmpURL;
		}
	}
	else
	{
		_eterna = new Eterna($E, debug, parentObj, oldEterna.id);
		_eterna.cache.primitiveURL = oldEterna.cache.primitiveURL;
	}
	_eterna.changeLocation(url);
	if (useAJAX !== false && useAJAX !== 0)
	{
		_eterna.cache.useAJAX = true;
	}
	param = _eterna.serializeFormData(param);
	try
	{
		param[EG_DATA_TYPE] = EG_DATA_TYPE_ALL;
		if (recall == null)
		{
			var textData = _eterna.getRemoteText(url, param);
			try
			{
				if (eterna_checkRemoteData(textData))
				{
					var str = eterna_dealRemoteData(_eterna, textData);
					var tmpData = eval("(" + str + ")");
					_eterna.changeEternaData(tmpData);
					var oldTemp = eg_temp;
					try
					{
						eg_temp = {};
						_eterna.reInit();
					}
					finally
					{
						eg_temp = oldTemp;
					}
				}
				else
				{
					eterna_initTextData(_eterna, textData);
				}
			}
			catch (ex)
			{
				eterna_initTextData(_eterna, textData);
			}
			return _eterna;
		}
		else
		{
			var completeFn = function (result, oldEterna, request, textStatus)
			{
				var _eterna = completeFn._eterna;
				var eterna_debug = completeFn.eterna_debug;
				var $E = completeFn._eterna.$E;
				var eternaData = $E;
				try
				{
					if (eterna_checkRemoteData(result))
					{
						var str = eterna_dealRemoteData(_eterna, result);
						var tmpData = eval("(" + str + ")");
						_eterna.changeEternaData(tmpData);
						var oldTemp = eg_temp;
						try
						{
							eg_temp = {};
							_eterna.reInit();
						}
						finally
						{
							eg_temp = oldTemp;
						}
					}
					else
					{
						eterna_initTextData(_eterna, result);
					}
				}
				catch (ex)
				{
					eterna_initTextData(_eterna, result);
				}
				completeFn.recall(completeFn._eterna, result, request, textStatus);
			};
			completeFn.eterna_debug = debug;
			completeFn._eterna = _eterna;
			completeFn.recall = recall;
			_eterna.getRemoteText(url, param, completeFn)
			return _eterna;
		}
	}
	catch (ex)
	{
		_eterna.printException(ex);
		throw ex;
	}
}

/**
 * 判断字符串是否为空.
 * null : true
 * ""   : true
 * " "  : false
 */
window.ef_isEmpty = function(str)
{
	return str == null || (typeof str == "string" && str == "");
}

/**
 * 将字符串中需转义的字符设上转义符
 */
window.ef_toScriptString = function(str)
{
	if (str == null)
	{
		return "";
	}
	str = str + "";
	var temp = "";
	for (var i = 0; i < str.length; i++)
	{
		var c = str.charAt(i);
		if (c < " ")
		{
			if (c == "\r")
			{
				temp += "\\r";
			}
			else if (c == "\n")
			{
				temp += "\\n";
			}
			else if (c == "\t")
			{
				temp += "\\t";
			}
			else if (c == "\b")
			{
				temp +=  "\\b";
			}
			else if (c == "\f")
			{
				temp += "\\f";
			}
			else
			{
				temp += " ";
			}
		}
		else if (c == "\"")
		{
			temp += "\\\"";
		}
		else if (c == "'")
		{
			temp += "\\'";
		}
		else if (c == "\\")
		{
			temp += "\\\\";
		}
		else if (c == "/")
		{
			temp += "\\/";
		}
		else
		{
			temp += c;
		}
	}
	return temp;
}

/**
 * 动态加载脚本
 */
if (typeof eg_pageInitializedURL == "undefined")
{
	window.eg_pageInitializedURL = {};
}

window.ef_loadResource = function (jsResource, url, charset)
{
	if (window.eg_pageInitializedURL[url])
	{
		if (!jsResource || !jsResource.alwaysExecute)
		{
			return;
		}
	}
	window.eg_pageInitializedURL[url] = 1;
	if (typeof eg_resVersion != "undefined")
	{
		if (url.indexOf("?") == -1)
		{
			url += "?_v=" + eg_resVersion;
		}
		else
		{
			url += "&_v=" + eg_resVersion;
		}
	}
	var resObj;
	if (jsResource)
	{
		if (typeof jQuery != 'undefined' && jsResource.async != true)
		{
			// 注: 除非返回数据里有编码格式头, 否则默认使用utf-8编码格式
			var opt = {type:"GET",global:false,url:url,async:false,dataType:"script",cache:true};
			if (typeof jsResource.cache != "undefined")
			{
				opt.cache = jsResource.cache;
			}
			jQuery.ajax(opt);
			// 同步加载完成后直接退出, 不执行后面的代码
			return;
		}
		resObj = document.createElement("script");
		resObj.type = "text/javascript";
		resObj.async = true;
		resObj.src = url;
	}
	else
	{
		resObj = document.createElement("link");
		resObj.type = "text/css";
		resObj.rel = "stylesheet";
		resObj.href = url;
	}
	if (charset != null)
	{
		resObj.charset = charset;
	}
	var s = document.getElementsByTagName("script")[0];
	s.parentNode.insertBefore(resObj, s);
}

window.ef_loadScript = function(flag, scriptPath, recall)
{
	if (window.eg_pageInitializedURL[scriptPath])
	{
		return;
	}
	window.eg_pageInitializedURL[scriptPath] = 1;
	if (typeof eg_resVersion != "undefined")
	{
		if (scriptPath.indexOf("?") == -1)
		{
			scriptPath += "?_v=" + eg_resVersion;
		}
		else
		{
			scriptPath += "&_v=" + eg_resVersion;
		}
	}
	var scriptObj = document.createElement('script');
	scriptObj.type = 'text/javascript';
	scriptObj.async = true;
	scriptObj.src = scriptPath;
	var s = document.getElementsByTagName('script')[0];
	s.parentNode.insertBefore(scriptObj, s);
	if (scriptObj.readyState) //IE
	{
		scriptObj.onreadystatechange = function()
		{
			if (scriptObj.readyState == "complete" || scriptObj.readyState == "loaded")
			{
				if (recall != null) recall();
			}
	  };
	}
	else //Others
	{
		scriptObj.onload = function()
		{
			if (recall != null) recall();
		};
	}
}

/**
 * 格式化数字显示方式
 * 用法
 * formatNumber(12345.999, "#,##0.00");
 * formatNumber(12345.999, "#,##0.##");
 * formatNumber(123, "000000");
 * @param num
 * @param pattern
 */
window.ef_formatNumber = function(num, pattern)
{
	if (typeof num != "number")
	{
		num = parseFloat(num);
	}
	if (isNaN(num))
	{
		return "?";
	}
	var firstStr = "";
	var lastStr = "";
	var strarr = num ? num.toString().split('.') : ['0'];
	var fmtarr = pattern ? pattern.split('.') : [''];
	var retstr = '';
	var fmt1 = null;
	var fmt2 = null;
	// 处理有多个"."的情况
	for (var i = 0; i < fmtarr.length; i++)
	{
		var tmpStr = fmtarr[i];
		if (fmt1 == null)
		{
			if (/[0#]/.test(tmpStr))
			{
				fmt1 = tmpStr;
				var checkStr = fmt1.substr(fmt1.length - 1, 1);
				if (checkStr != "#" && checkStr != "0" && checkStr != ",")
				{
					fmt2 = "";
				}
				if (i > 0)
				{
					checkStr = tmpStr.substr(0, 1);
					if (checkStr == "#" || checkStr == "0")
					{
						firstStr = firstStr.substr(0, firstStr.length - 1);
						fmt1 = "";
						fmt2 = tmpStr;
					}
				}
			}
			else
			{
				firstStr += tmpStr + (i < fmtarr.length - 1 ? "." : "");
			}
		}
		else if (fmt2 == null)
		{
			fmt2 = tmpStr;
		}
		else
		{
			lastStr += "." + tmpStr;
		}
	}
	if (fmt1 == null)
	{
		fmt1 = "";
		fmt2 = "";
	}
	if (fmt2 == null)
	{
		fmt2 = "";
	}

	/* 整数部分 */
	var str = strarr[0];
	var fmt = fmt1;
	var i = str.length - 1;
	var comma = 0;
	var tmpCommaCount = 0;
	// 处理起始的format字符
	if (fmt.length > 0)
	{
		var tmpI = 0;
		var checkStr = fmt.substr(tmpI++, 1);
		while (checkStr != "#" && checkStr != "0" && checkStr != "," && tmpI < fmt.length)
		{
			firstStr += checkStr;
			checkStr = fmt.substr(tmpI++, 1);
		}
		if (checkStr != "#" && checkStr != "0" && checkStr != ",")
		{
			firstStr += checkStr;
		}
	}
	// 去掉其他的符号
	fmt = fmt.replace(/[^0,#]/g, "");
	// 处理负号
	var negative = "";
	if (str.length > 0)
	{
		if (str.substr(0, 1) == "-")
		{
			str = str.substr(1, str.length - 1);
			negative = "-";
			i--;
		}
	}
	for (var f = fmt.length - 1; f >= 0; f--)
	{
		switch (fmt.substr(f, 1))
		{
			case '#':
				if (i >= 0) retstr = str.substr(i--, 1) + retstr;
				tmpCommaCount++;
				break;
			case '0':
				if (i >= 0) retstr = str.substr(i--, 1) + retstr;
				else retstr = '0' + retstr;
				tmpCommaCount++;
				break;
			case ',':
				comma = tmpCommaCount;
				tmpCommaCount = 0;
				retstr = ',' + retstr;
				break;
		}
	}
	// 如果还有数字并且需要证书部分, 则补充数字
	if (i >= 0 && fmt != "")
	{
		if (comma)
		{
			var l = str.length;
			// 先把第一个","补齐
			if (i >= comma - tmpCommaCount && comma - tmpCommaCount > 0)
			{
				retstr = ',' + str.substr(i - comma + tmpCommaCount + 1, comma - tmpCommaCount) + retstr;
				i -= comma - tmpCommaCount;
			}
			var tmpCount = 0;
			for (; i >= 0; i--)
			{
				tmpCount++;
				retstr = str.substr(i, 1) + retstr;
				// 根据计算得出的","间隔进行补充
				if (i > 0 && (tmpCount % comma) == 0) retstr = ',' + retstr;
			}
		}
		else retstr = str.substr(0, i + 1) + retstr;
  }

	retstr = retstr + '.';
	/* 处理小数部分 */
	str = strarr.length > 1 ? strarr[1] : '';
	var tmpLast = fmt2 == "" ? fmt1 : fmt2;
	fmt = fmt2;
	i = 0;
	// 处理结束的format字符
	if (tmpLast.length > 0)
	{
		var tmpI = tmpLast.length - 1;
		var checkStr = tmpLast.substr(tmpI--, 1);
		while (checkStr != "#" && checkStr != "0" && tmpI >= 0)
		{
			lastStr = checkStr + lastStr;
			checkStr = tmpLast.substr(tmpI--, 1);
		}
		if (checkStr != "#" && checkStr != "0")
		{
			lastStr = checkStr + lastStr;
		}
	}
	// 去掉其他的符号
	fmt = fmt.replace(/[^0#]/g, "");
	for (var f = 0; f < fmt.length; f++)
	{
		switch (fmt.substr(f, 1))
		{
			case '#':
				if (i < str.length) retstr += str.substr(i++, 1);
				break;
			case '0':
				if (i < str.length)
				{
					var tmpChar = str.substr(i++, 1);
					if (tmpChar != "0") retstr += tmpChar;
					else retstr += "(0)";
				}
				else retstr += "(0)"; // 这里补上(0), 以区分不需要的0
				break;
		}
	}
	//alert(retstr);

	var result = retstr.replace(/^,+/, '')	  //去除前面的","
			.replace(/0+$/, '')						//去除末尾的"0"
			.replace(/\(0\)/g, '0')					 //将"(0)"替换回"0"
			.replace(/\.$/, '');					  //去除末尾的"."
	//alert(retstr);
	if (/^0*\.?0*$/.test(result))
	{
		negative = "";
	}
	return firstStr + negative + result + lastStr;
}

/**
 * 用于存放创建控件时产生临时列表
 */
function WebObjList(parent, inheritObj)
{
	this.data = [];
	this.realParent = parent;
	this.inheritObj = inheritObj;

	if (typeof WebObjList._initialized == 'undefined')
	{
		WebObjList._initialized = true;
		WebObjList.prototype.webObjList = true;

		WebObjList.prototype.append = function(obj)
		{
			this.data.push(obj);
		}

		WebObjList.prototype.get = function(index)
		{
			return this.data[index].get(0);
		}

		WebObjList.prototype.eq = function(index)
		{
			return this.data[index];
		}

		WebObjList.prototype.size = function()
		{
			return this.data.length;
		}
	}

}

})(window);