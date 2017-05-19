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

package self.micromagic.eterna.dao.reader;

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import self.micromagic.eterna.share.TypeManager;
import self.micromagic.util.converter.LongConverter;

public class LongReader extends ObjectReader
{
	public LongReader(String name)
	{
		super(name);
		this.converter = new LongConverter();
		this.converter.setNeedThrow(true);
	}

	public int getType()
	{
		return TypeManager.TYPE_LONG;
	}

	public Object readCall(CallableStatement call, int index)
			throws SQLException
	{
		long longValue = call.getLong(index);
		return call.wasNull() ? null : new Long(longValue);
	}

	public Object readResult(ResultSet rs)
			throws SQLException
	{
		long longValue = this.isUseColumnIndex() || this.transIndex(rs) ?
				rs.getLong(this.columnIndex) : rs.getLong(this.realAlias);
		return rs.wasNull() ? null : new Long(longValue);
	}

}
