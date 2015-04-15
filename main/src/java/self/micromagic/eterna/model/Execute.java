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

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import self.micromagic.eterna.share.EternaException;

public interface Execute
{
	void initialize(ModelAdapter model) throws EternaException;

	String getName() throws EternaException;

	ModelAdapter getModelAdapter() throws EternaException;

	String getExecuteType() throws EternaException;

	ModelExport execute(AppData data, Connection conn)
			throws EternaException, SQLException, IOException;

	void destroy();

}