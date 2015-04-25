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
import self.micromagic.util.MemoryChars;
import self.micromagic.util.Utility;
import self.micromagic.util.converter.ReaderConverter;

public class CharsReader extends ObjectReader
{
	public int getType()
	{
		return TypeManager.TYPE_CHARS;
	}

	public CharsReader(String name)
	{
		super(name);
		this.converter = new ReaderConverter();
		this.converter.setNeedThrow(true);
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
		Reader reader = this.useIndexOrAlias || this.transIndex(rs) ?
				rs.getCharacterStream(this.columnIndex) : rs.getCharacterStream(this.alias);
		if (reader == null)
		{
			return null;
		}
		return this.readFromReader(reader);
	}

	private Object readFromReader(Reader reader)
			throws SQLException
	{
		MemoryChars mcs = new MemoryChars(1, 512);
		try
		{
			Utility.copyChars(reader, mcs.getWriter());
			reader.close();
		}
		catch (IOException ex)
		{
			log.error("IO error at ReaderReader.", ex);
			throw new SQLException(ex.getMessage());
		}
		return mcs;
	}

}