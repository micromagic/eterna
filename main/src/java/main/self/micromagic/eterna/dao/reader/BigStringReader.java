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

import java.io.IOException;
import java.io.Reader;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.SQLException;

import self.micromagic.eterna.share.TypeManager;
import self.micromagic.util.StringAppender;
import self.micromagic.util.StringTool;
import self.micromagic.util.converter.StringConverter;

public class BigStringReader extends ObjectReader
{
	public BigStringReader(String name)
	{
		super(name);
		this.converter = new StringConverter();
		this.converter.setNeedThrow(true);
	}

	public int getType()
	{
		return TypeManager.TYPE_STRING;
	}

	public Object readCall(CallableStatement call, int index)
			throws SQLException
	{
		Clob clob = call.getClob(index);
		if (clob == null)
		{
			return null;
		}
		Reader reader = clob.getCharacterStream();
		return this.readFromReader(reader);
	}

	public Object readResult(ResultSet rs)
			throws SQLException
	{
		Reader reader = this.isUseColumnIndex() || this.transIndex(rs) ?
				rs.getCharacterStream(this.columnIndex) : rs.getCharacterStream(this.realAlias);
		if (reader == null)
		{
			return null;
		}
		return this.readFromReader(reader);
	}

	private Object readFromReader(Reader reader)
			throws SQLException
	{
		StringAppender result;
		char[] buf = new char[1024];
		try
		{
			int count = reader.read(buf);
			if (count < 1024)
			{
				result = StringTool.createStringAppender(count > 0 ? count : 2);
			}
			else
			{
				result = StringTool.createStringAppender(3072);
			}
			while (count > 0)
			{
				result.append(buf, 0, count);
				count = reader.read(buf);
			}
			reader.close();
		}
		catch (IOException ex)
		{
			log.error("IO error at BigStringReader.", ex);
			throw new SQLException(ex.getMessage());
		}
		return result.toString();
	}

}
