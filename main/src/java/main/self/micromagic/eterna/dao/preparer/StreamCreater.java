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

package self.micromagic.eterna.dao.preparer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;

import self.micromagic.eterna.dao.PreparedStatementWrap;
import self.micromagic.eterna.share.AttributeManager;
import self.micromagic.eterna.share.EternaException;
import self.micromagic.util.MemoryStream;
import self.micromagic.util.converter.StreamConverter;

class StreamCreater extends AbstractPreparerCreater
{
	public StreamCreater(String name)
	{
		super(name);
	}
	private static final StreamConverter convert = new StreamConverter();
	String charset = "UTF-8";

	public void setCharset(String charset)
	{
		this.charset = charset;
	}

	public ValuePreparer createPreparer(Object value)
	{
		return new StreamPreparer(this, convert.convertToStream(value, this.charset));
	}

	public ValuePreparer createPreparer(String value)
	{
		return new StreamPreparer(this, convert.convertToStream(value, this.charset));
	}

	protected void setAttributes(AttributeManager attributes)
	{
		super.setAttributes(attributes);
		String tStr = (String) attributes.getAttribute("charset");
		if (tStr != null)
		{
			this.setCharset(tStr);
		}
	}

}

class StreamPreparer extends AbstractValuePreparer
{
	private final InputStream value;
	private final int length;

	public StreamPreparer(PreparerCreater creater, InputStream value, int length)
	{
		super(creater);
		this.length = length;
		this.value = value;
	}
	public StreamPreparer(PreparerCreater creater, InputStream value)
	{
		super(creater);
		if (value == null)
		{
			this.length = 0;
			this.value = null;
			return;
		}
		MemoryStream ms = new MemoryStream();
		OutputStream out = ms.getOutputStream();
		byte[] buf = new byte[1024];
		int allCount = 0;
		int rCount;
		try
		{
			while ((rCount = value.read(buf)) >= 0)
			{
				out.write(buf, 0, rCount);
				allCount += rCount;
				if (allCount < 0)
				{
					throw new EternaException("Too large value for InputStream.");
				}
			}
			this.length = allCount;
			this.value = ms.getInputStream();
		}
		catch (IOException ex)
		{
			throw new EternaException(ex);
		}
	}

	public void setValueToStatement(int index, PreparedStatementWrap stmtWrap)
			throws SQLException
	{
		stmtWrap.setBinaryStream(this.getName(), index, this.value, this.length);
	}

}