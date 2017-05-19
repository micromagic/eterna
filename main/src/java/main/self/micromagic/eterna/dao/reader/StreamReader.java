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
import java.io.InputStream;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import self.micromagic.eterna.share.TypeManager;
import self.micromagic.util.MemoryStream;
import self.micromagic.util.Utility;
import self.micromagic.util.converter.StreamConverter;

public class StreamReader extends ObjectReader
{
	public StreamReader(String name)
	{
		super(name);
		this.converter = new StreamConverter();
		this.converter.setNeedThrow(true);
	}

	public int getType()
	{
		return TypeManager.TYPE_STREAM;
	}

	public Object readCall(CallableStatement call, int index)
			throws SQLException
	{
		Blob blob = call.getBlob(index);
		if (blob == null)
		{
			return null;
		}
		InputStream ins = blob.getBinaryStream();
		return this.readFromStream(ins);
	}

	public Object readResult(ResultSet rs)
			throws SQLException
	{
		InputStream ins = this.isUseColumnIndex() || this.transIndex(rs) ?
				rs.getBinaryStream(this.columnIndex) : rs.getBinaryStream(this.realAlias);
		if (ins == null)
		{
			return null;
		}
		return this.readFromStream(ins);
	}

	private Object readFromStream(InputStream ins)
			throws SQLException
	{
		MemoryStream ms = new MemoryStream(1, 512);
		try
		{
			Utility.copyStream(ins, ms.getOutputStream());
			ins.close();
		}
		catch (IOException ex)
		{
			log.error("IO error at StreamReader.", ex);
			throw new SQLException(ex.getMessage());
		}
		return ms;
	}

}
