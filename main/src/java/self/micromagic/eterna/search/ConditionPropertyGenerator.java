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

package self.micromagic.eterna.search;

import self.micromagic.eterna.share.Generator;
import self.micromagic.eterna.share.EternaException;

/**
 * @author micromagic@sina.com
 */
public interface ConditionPropertyGenerator extends Generator
{
	void setName(String name) throws EternaException;

	void setColumnName(String name) throws EternaException;

	void setColumnCaption(String caption) throws EternaException;

	void setColumnType(String type) throws EternaException;

	/**
	 * 设置对应列的数据准备生成器.
	 */
	void setColumnVPC(String vpcName) throws EternaException;

	/**
	 * 设置是否可见.
	 */
	void setVisible(boolean visible) throws EternaException;

	void setConditionInputType(String type) throws EternaException;

	void setDefaultValue(String value) throws EternaException;

	void setPermissions(String permissions) throws EternaException;

	void setUseDefaultConditionBuilder(boolean use) throws EternaException;

	void setDefaultConditionBuilderName(String name) throws EternaException;

	void setConditionBuilderListName(String name) throws EternaException;

	ConditionProperty createConditionProperty() throws EternaException;

}