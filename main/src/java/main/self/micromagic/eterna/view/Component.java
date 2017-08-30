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

import self.micromagic.eterna.model.AppData;
import self.micromagic.eterna.share.EternaException;
import self.micromagic.eterna.share.EternaFactory;

public interface Component
{
	/**
	 * 在<code>EternaFactory</code>中设置view外覆控件的属性名称.
	 */
	String VIEW_WRPA_TYPE_FLAG = "view.wrpa.type";

	/**
	 * 在<code>EternaFactory</code>中设置view外覆控件是否需要执行script的属性名称.
	 */
	String VIEW_WRPA_NEED_SCRIPT_FLAG = "view.wrpa.needScript";

	/**
	 * 在item的attribute中设置使用控件名称的标识.
	 */
	String TYPICAL_COMPONENT_FLAG = "typical";

	/**
	 * 不用处理子节点标识的标签.
	 */
	String NO_SUB_FLAG = "noSub";

	/**
	 * 普通的节点类型: div.
	 */
	String NORMAL_TYPE_DIV = "div";

	/**
	 * 特殊的节点类型: none. <p>
	 * 一个空节点, 此节点不会生成, 会将其子节点直接挂到它的父节点上.
	 */
	String SPECIAL_TYPE_NONE = "none";

	/**
	 * 特殊的节点类型: loop. <p>
	 * 一个循环节点, 此节点不会生成, 会将其子节点循环生成并挂到它
	 * 的父节点上.
	 */
	String SPECIAL_TYPE_LOOP = "loop";

	/**
	 * 特殊的节点类型: inherit. <p>
	 * 此节点的类型会自动设为模板节点中的对应类型.
	 */
	String SPECIAL_TYPE_INHERIT = "inherit";

	/**
	 * 用于存放标签名称的属性.
	 */
	String FLAG_TAG = "eFlag";

	/**
	 * 对于inherit类型, 需要进行全局查找模板节点的标志.
	 */
	String INHERIT_GLOBAL_SEARCH = "inheritGlobalSearch";

	/**
	 * 初始化页面控件对象.
	 *
	 * @param factory  当前的工厂对象
	 * @param parent   页面控件的父控件对象, 如果为null表示此控件在第一层
	 */
	void initialize(EternaFactory factory, Component parent) throws EternaException;

	String getName() throws EternaException;

	String getType() throws EternaException;

	Component getParent() throws EternaException;

	Iterator getSubComponents() throws EternaException;

	Iterator getEvents() throws EternaException;

	boolean isIgnoreGlobalParam() throws EternaException;

	String getComponentParam() throws EternaException;

	String getBeforeInit() throws EternaException;

	String getInitScript() throws EternaException;

	/**
	 * 获取本Component某个设置的属性.
	 */
	Object getAttribute(String name) throws EternaException;

	/**
	 * 获取本Component设置的所有属性的名称.
	 */
	String[] getAttributeNames() throws EternaException;

	EternaFactory getFactory() throws EternaException;

	View.ViewRes getViewRes() throws EternaException;

	void print(Writer out, AppData data, View view) throws IOException, EternaException;

	void printBody(Writer out, AppData data, View view) throws IOException, EternaException;

	void printSpecialBody(Writer out, AppData data, View view) throws IOException, EternaException;

}