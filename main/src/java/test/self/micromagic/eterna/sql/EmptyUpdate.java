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

package self.micromagic.eterna.sql;

import java.sql.Connection;
import java.sql.SQLException;

import self.micromagic.eterna.dao.Update;
import self.micromagic.eterna.dao.impl.UpdateImpl;
import self.micromagic.eterna.share.EternaException;

public class EmptyUpdate extends UpdateImpl
{
	public Update createUpdateAdapter()
			throws EternaException
	{
		EmptyUpdate other = new EmptyUpdate();
		this.copy(other);
		return other;
	}

	public int executeUpdate(Connection conn)
			throws EternaException, SQLException
	{
		return 1;
	}

	public void execute(Connection conn)
			throws EternaException, SQLException
	{
	}

}