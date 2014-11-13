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

package self.micromagic.eterna.view;

import self.micromagic.eterna.digester.ConfigurationException;
import self.micromagic.eterna.share.AdapterGenerator;

public interface ViewAdapterGenerator extends AdapterGenerator
{
	void setName(String name) throws ConfigurationException;

	void setDataPrinterName(String dpName) throws ConfigurationException;

	void setDefaultDataType(String type) throws ConfigurationException;

	void setDynamicViewRes(String res) throws ConfigurationException;

	void addComponent(Component com) throws ConfigurationException;

	void deleteComponent(Component com) throws ConfigurationException;

	void clearComponents() throws ConfigurationException;

	void setDebug(int debug) throws ConfigurationException;

	void setWidth(String width) throws ConfigurationException;

	void setHeight(String height) throws ConfigurationException;

	void setBeforeInit(String condition) throws ConfigurationException;

	void setInitScript(String body) throws ConfigurationException;

	ViewAdapter createViewAdapter() throws ConfigurationException;

	interface ModifiableViewRes extends ViewAdapter.ViewRes
	{
		/**
		 * 添加一个方法.
		 *
		 * @return  添加的方法名称, 方法名称可能会根据当前环境有所变化.
		 */
		public String addFunction(Function fn) throws ConfigurationException;

		public void addTypicalComponentNames(String name) throws ConfigurationException;

		public void addResourceNames(String name) throws ConfigurationException;

		public void addAll(ViewAdapter.ViewRes res) throws ConfigurationException;

	}

}