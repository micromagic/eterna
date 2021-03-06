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

import java.util.Iterator;

import self.micromagic.eterna.share.EternaException;

public interface TableList extends Component
{
	public static final String TR_NAME_PERFIX = "tableList_TR";

	boolean isAutoArrange() throws EternaException;

	boolean isPercentWidth() throws EternaException;

	boolean isCaculateWidth() throws EternaException;

	int getCaculateWidthFix() throws EternaException;

	Component getTR() throws EternaException;

	String getBaseName() throws EternaException;

	String getDataName() throws EternaException;

	Iterator getColumns() throws EternaException;

	interface Column extends Component
	{
		int getWidth() throws EternaException;

		String getTitleParam() throws EternaException;

		String getContainerParam() throws EternaException;

		boolean isIgnoreGlobalTitleParam() throws EternaException;

		boolean isIgnoreGlobalContainerParam() throws EternaException;

		String getCaption() throws EternaException;

		String getDefaultValue() throws EternaException;

		boolean isIgnore() throws EternaException;

		String getSrcName() throws EternaException;

		String getDataName() throws EternaException;

		boolean isOtherData() throws EternaException;

		Component getTypicalComponent() throws EternaException;

		String getInitParam() throws EternaException;

		boolean getCloneInitParam() throws EternaException;

	}
}