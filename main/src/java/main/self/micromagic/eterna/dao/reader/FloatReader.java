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
import self.micromagic.util.converter.FloatConverter;

public class FloatReader extends ObjectReader
{
	public FloatReader(String name)
	{
		super(name);
		this.converter = new FloatConverter();
		this.converter.setNeedThrow(true);
	}

	public int getType()
	{
		return TypeManager.TYPE_FLOAT;
	}

	public Object readCall(CallableStatement call, int index)
			throws SQLException
	{
		float floatValue = call.getFloat(index);
		return call.wasNull() ? null : new Float(floatValue);
	}

	public Object readResult(ResultSet rs)
			throws SQLException
	{
		float floatValue = this.isUseColumnIndex() || this.transIndex(rs) ?
				rs.getFloat(this.columnIndex) : rs.getFloat(this.realAlias);
		return rs.wasNull() ? null : new Float(floatValue);
	}

}
