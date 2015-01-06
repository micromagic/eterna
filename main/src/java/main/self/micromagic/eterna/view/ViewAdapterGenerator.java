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

import self.micromagic.eterna.share.EternaException;
import self.micromagic.eterna.share.AdapterGenerator;

public interface ViewAdapterGenerator extends AdapterGenerator
{
	void setName(String name) throws EternaException;

	void setDataPrinterName(String dpName) throws EternaException;

	void setDefaultDataType(String type) throws EternaException;

	void setDynamicViewRes(String res) throws EternaException;

	void addComponent(Component com) throws EternaException;

	void deleteComponent(Component com) throws EternaException;

	void clearComponents() throws EternaException;

	void setDebug(int debug) throws EternaException;

	void setWidth(String width) throws EternaException;

	void setHeight(String height) throws EternaException;

	void setBeforeInit(String condition) throws EternaException;

	void setInitScript(String body) throws EternaException;

	ViewAdapter createViewAdapter() throws EternaException;

	interface ModifiableViewRes extends ViewAdapter.ViewRes
	{
		/**
		 * 添加一个方法.
		 *
		 * @return  添加的方法名称, 方法名称可能会根据当前环境有所变化.
		 */
		public String addFunction(Function fn) throws EternaException;

		public void addTypicalComponentNames(String name) throws EternaException;

		public void addResourceNames(String name) throws EternaException;

		public void addAll(ViewAdapter.ViewRes res) throws EternaException;

	}

}