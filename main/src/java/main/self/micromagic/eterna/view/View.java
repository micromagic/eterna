/*
 * Copyright 2015 xinjunli (micromagic@sina.com).
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

package self.micromagic.eterna.view;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import self.micromagic.eterna.model.AppData;
import self.micromagic.eterna.share.EternaException;
import self.micromagic.eterna.share.EternaFactory;
import self.micromagic.eterna.share.EternaCreater;

public interface View extends EternaCreater
{
	/**
	 * 默认的数据集输出器的名称
	 */
	public static final String DEFAULT_DATA_PRINTER_NAME = "dataPrinter";

	/**
	 * 基本的debug等级, 这个等级之上的debug才会输出信息
	 */
	public static final int ETERNA_VIEW_DEBUG_BASE = 0x10;

	/**
	 * view缓存的名称.
	 *
	 * @see    self.micromagic.eterna.model.AppData#getSpcialDataMap(String)
	 */
	public static final String VIEW_CACHE = "view.cache";

	/**
	 * 是否是动态视图的标记. <p>
	 * 如果是动态视图的话, 请在Component的实现类的print方法中,调用
	 * data.addSpcialData(ViewAdapter.VIEW_CACHE, ViewAdapter.DYNAMIC_VIEW, "1")
	 * 用于标识该视图是动态的不可缓存.
	 *
	 * @see    self.micromagic.eterna.model.AppData#addSpcialData(String, String, Object)
	 */
	public static final String DYNAMIC_VIEW = "dynamic.view";

	/**
	 * 动态方法map的标识. <p>
	 * 如果是动态视图的话且有动态的方法调用, 那需要把这些动态方法添加到这个标识下的
	 * map中. 可以调用如下方法进行添加:
	 * BaseManager.addDynamicFunction(Map)
	 *
	 * @see    self.micromagic.eterna.model.AppData#addSpcialData(String, String, Object)
	 * @see    self.micromagic.eterna.view.impl.ViewTool#addDynamicFunction(Map)
	 */
	public static final String DYNAMIC_FUNCTIONS = "dynamic.functions";

	/**
	 * 动态资源文本set的标识. <p>
	 * 如果是动态视图的话且有动态的资源文本引用, 那需要把这些动态资源文本的名称添加到
	 * 这个标识下的set中. 可以调用如下方法进行添加:
	 * BaseManager.addDynamicResourceName(String)
	 *
	 * @see    self.micromagic.eterna.model.AppData#addSpcialData(String, String, Object)
	 * @see    self.micromagic.eterna.view.impl.ViewTool#addDynamicResourceName(String)
	 */
	public static final String DYNAMIC_RESOURCE_NAMES = "dynamic.resource.names";

	/**
	 * 已使用的typical控件.
	 */
	public static final String USED_TYPICAL_COMPONENTS = "used.TypicalComponents";

	/**
	 * 当前的typical控件.
	 */
	public static final String TYPICAL_COMPONENTS_MAP = "TypicalComponents_MAP";


	/**
	 * 通过参数设置dataType值使用的名称.
	 */
	public static final String DATA_TYPE = "___dataType";

	/**
	 * 仅需要输出数据部分的数据类型.
	 */
	public static final String DATA_TYPE_DATA = "data";

	/**
	 * 去除框架的数据结构, 仅保留数据部分, 可以作为REST的返回值.
	 */
	public static final String DATA_TYPE_REST = "REST";

	/**
	 * 输出框架结构的所有数据.
	 */
	public static final String DATA_TYPE_ALL = "all";

	/**
	 * 输出展现的页面.
	 */
	public static final String DATA_TYPE_WEB = "web";

	/**
	 * 分隔json数据和后面的html数据的标签.
	 */
	public static final String JSON_SPLIT_FLAG = "<!-- eterna json data split -->";

	String getName() throws EternaException;

	DataPrinter getDataPrinter() throws EternaException;

	String getDefaultDataType() throws EternaException;

	String getDataType(AppData data) throws EternaException;

	EternaFactory getFactory() throws EternaException;

	Iterator getComponents() throws EternaException;

	int getDebug() throws EternaException;

	String getWidth() throws EternaException;

	String getHeight() throws EternaException;

	String getBeforeInit() throws EternaException;

	String getInitScript() throws EternaException;

	/**
	 * 获取本view适配器某个设置的属性.
	 */
	Object getAttribute(String name) throws EternaException;

	/**
	 * 获取本view适配器设置的所有属性的名称.
	 */
	String[] getAttributeNames() throws EternaException;

	ViewRes getViewRes() throws EternaException;

	/**
	 * 将界面及数据集信息写入到输出流中.
	 * 会根据<code>DATA_TYPE</code>类型来输出数据.
	 *
	 * @param out    信息写入的数据流
	 * @param data   数据集所在的<code>AppData</code>
	 */
	void printView(Writer out, AppData data) throws IOException, EternaException;

	/**
	 * 将界面及数据集信息写入到输出流中.
	 * 会根据<code>DATA_TYPE</code>类型来输出数据.
	 *
	 * @param out       信息写入的数据流
	 * @param data      数据集所在的<code>AppData</code>
	 * @param cache     需要初始化到_eterna.cache中的值
	 */
	void printView(Writer out, AppData data, Map cache)
			throws IOException, EternaException;

	/**
	 * 输出控件的事件定义.
	 * 由于要根据debug的等级，在事件脚本中加入调试代码，所以此功能在View中实现.
	 */
	void printEvent(Writer out, AppData data, Event event) throws IOException, EternaException;

	interface ViewRes
	{
		public Map getFunctionMap() throws EternaException;

		public Set getTypicalComponentNames() throws EternaException;

		public Set getResourceNames() throws EternaException;

	}

}