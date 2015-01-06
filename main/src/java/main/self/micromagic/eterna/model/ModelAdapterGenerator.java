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

package self.micromagic.eterna.model;

import self.micromagic.eterna.share.EternaException;
import self.micromagic.eterna.share.AdapterGenerator;

public interface ModelAdapterGenerator extends AdapterGenerator
{
	void setName(String name) throws EternaException;

	void setKeepCaches(boolean keep) throws EternaException;

	void setNeedFrontModel(boolean needFrontModel) throws EternaException;

	void setFrontModelName(String frontModelName) throws EternaException;

	void setModelExportName(String name) throws EternaException;

	void setErrorExportName(String name) throws EternaException;

	void addExecute(Execute execute) throws EternaException;

	void setTransactionType(String tType) throws EternaException;

	void setDataSourceName(String dsName) throws EternaException;

	void setAllowPosition(String positions) throws EternaException;

	ModelAdapter createModelAdapter() throws EternaException;

}