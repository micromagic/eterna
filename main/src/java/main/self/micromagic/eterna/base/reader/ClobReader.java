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

package self.micromagic.eterna.base.reader;

import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.SQLException;

import self.micromagic.eterna.share.TypeManager;

public class ClobReader extends ObjectReader
{
	public int getType()
	{
		return TypeManager.TYPE_CLOB;
	}

	public ClobReader(String name)
	{
		super(name);
	}

	public Object readCall(CallableStatement call, int index)
			throws SQLException
	{
		return call.getClob(index);
	}

	public Object readResult(ResultSet rs)
			throws SQLException
	{
		Clob clob = this.useIndexOrName || this.transIndex(rs) ?
				rs.getClob(this.columnIndex) : rs.getClob(this.columnName);
		return clob;
	}

}