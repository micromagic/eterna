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

package self.micromagic.eterna.model;

import self.micromagic.eterna.share.EternaException;
import self.micromagic.eterna.share.Generator;

public interface QueryExecuteGenerator extends Generator
{
	void setCache(int cacheIndex) throws EternaException;

	void setDoExecute(boolean execute) throws EternaException;

	void setPushResult(boolean push) throws EternaException;

	void setStart(int start) throws EternaException;

	void setCount(int count) throws EternaException;

	/**
	 * 设置计算总记录数的方式. <p>
	 * 分别为auto, count, none. 默认值为: none.
	 */
	void setCountType(String countType) throws EternaException;

	void addParamBind(ParamBind bind) throws EternaException;

	Execute createExecute() throws EternaException;
}