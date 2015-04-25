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

import self.micromagic.eterna.share.Generator;
import self.micromagic.eterna.share.EternaException;

public interface CheckExecuteGenerator extends Generator
{
	public static final String MAX_LOOP_COUNT_PROPERTY = "self.micromagic.eterna.model.maxLoopCount";

	void setCheckPattern(String pattern) throws EternaException;

	void setLoopType(int type) throws EternaException;

	void setTrueExportName(String name) throws EternaException;

	void setFalseExportName(String name) throws EternaException;

	void setTrueTransactionType(String tType) throws EternaException;

	void setFalseTransactionType(String tType) throws EternaException;

	void setTrueModelName(String name) throws EternaException;

	void setFalseModelName(String name) throws EternaException;

	Execute createExecute() throws EternaException;

}