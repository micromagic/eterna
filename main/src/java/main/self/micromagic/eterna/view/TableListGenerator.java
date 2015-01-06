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

public interface TableListGenerator extends ComponentGenerator
{
	void setAutoArrange(boolean autoArrange) throws EternaException;

	void setPercentWidth(boolean percentWidth) throws EternaException;

	void setCaculateWidth(boolean caculateWidth) throws EternaException;

	void setCaculateWidthFix(int caculateWidthFix) throws EternaException;

	void setTR(Component tr) throws EternaException;

	void setColumnOrder(String order) throws EternaException;

	/**
	 * @param name  可以是一个query       名称以[query:]开始或直接query的名称
	 *              可以是一个readManager 名称以[reader:]开始，后面接readerManager的名称
	 *              可以是一个search      名称以[search:]开始，后面接search的名称
	 */
	void setBaseName(String name) throws EternaException;

	void setDataName(String dataName) throws EternaException;

	TableList createTableList() throws EternaException;

	void addColumn(TableList.Column column) throws EternaException;

	void deleteColumn(TableList.Column column) throws EternaException;

	void clearColumns() throws EternaException;

	interface ColumnGenerator extends ComponentGenerator
	{
		void setWidth(int width) throws EternaException;

		void setTitleParam(String param) throws EternaException;

		void setIgnoreGlobalTitleParam(boolean ignore) throws EternaException;

		void setCaption(String caption) throws EternaException;

		void setDefaultValue(String value) throws EternaException;

		void setIgnore(boolean ignore) throws EternaException;

		void setSrcName(String srcName) throws EternaException;

		void setTypicalComponentName(String name) throws EternaException;

		void setInitParam(String param) throws EternaException;

		void setCloneInitParam(boolean clone) throws EternaException;

		TableList.Column createColumn() throws EternaException;

	}

}