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

public interface TableFormGenerator extends ComponentGenerator
{
	void setAutoArrange(boolean autoArrange) throws EternaException;

	void setPercentWidth(boolean percentWidth) throws EternaException;

	void setCaculateWidth(boolean caculateWidth) throws EternaException;

	void setCaculateWidthFix(int caculateWidthFix) throws EternaException;

	void setColumns(String columns) throws EternaException;

	void setTR(Component tr) throws EternaException;

	void setCellOrder(String order) throws EternaException;

	/**
	 * @param name  可以是一个query       名称以[query:]开始或直接query的名称
	 *              可以是一个readManager 名称以[reader:]开始，后面接readerManager的名称
	 *              可以是一个search      名称以[search:]开始，后面接search的名称
	 */
	void setBaseName(String name) throws EternaException;

	void setDataName(String dataName) throws EternaException;

	TableForm createTableForm() throws EternaException;

	void addCell(TableForm.Cell cell) throws EternaException;

	void deleteCell(TableForm.Cell cell) throws EternaException;

	void clearCells() throws EternaException;

	interface CellGenerator extends ComponentGenerator
	{
		void setTitleSize(int size) throws EternaException;

		void setTitleParam(String param) throws EternaException;

		void setContainerSize(int size) throws EternaException;

		void setIgnoreGlobalTitleParam(boolean ignore) throws EternaException;

		void setRowSpan(int rowSpan) throws EternaException;

		void setCaption(String caption) throws EternaException;

		void setDefaultValue(String value) throws EternaException;

		void setIgnore(boolean ignore) throws EternaException;

		void setNewRow(boolean newRow) throws EternaException;

		void setSrcName(String srcName) throws EternaException;

		void setRequired(boolean required) throws EternaException;

		void setNeedIndex(boolean needIndex) throws EternaException;

		void setTypicalComponentName(String name) throws EternaException;

		void setInitParam(String param) throws EternaException;

		TableForm.Cell createCell() throws EternaException;

	}

}