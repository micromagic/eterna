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

import java.sql.SQLException;

import self.micromagic.eterna.share.EternaException;

public interface ParamBind
{
	void initialize(Model model, Execute execute) throws EternaException;

	public int setParam(AppData data, ParamSetManager psm, int loopIndex)
			throws EternaException, SQLException;

	public boolean isLoop() throws EternaException;

	public boolean isSubSQL() throws EternaException;

}