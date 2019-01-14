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

package self.micromagic.eterna.dao.preparer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;

import self.micromagic.eterna.dao.PreparedStatementWrap;
import self.micromagic.eterna.share.AttributeManager;
import self.micromagic.eterna.share.EternaException;
import self.micromagic.util.MemoryStream;
import self.micromagic.util.StringTool;
import self.micromagic.util.converter.StreamConverter;

class StreamCreater extends AbstractPreparerCreater
{
	private static final StreamConverter convert = new StreamConverter();
	String charset = "UTF-8";

	public StreamCreater(String name)
	{
		super(name);
	}

	public void setCharset(String charset)
	{
		this.charset = charset;
	}

	public Object convertValue(Object value)
	{
		return convert.convertToStream(value, this.charset);
	}

	public Object convertValue(String value)
	{
		return convert.convertToStream(value, this.charset);
	}

	public ValuePreparer createPreparer(Object value)
	{
		if (value instanceof String)
		{
			return this.createPreparer((String) value);
		}
		else if (value instanceof byte[])
		{
			byte[] buf = (byte[]) value;
			return new StreamPreparer(this, new ByteArrayInputStream(buf), buf.length);
		}
		else if (value instanceof MemoryStream)
		{
			return new StreamPreparer(this, (MemoryStream) value);
		}
		return new StreamPreparer(this, convert.convertToStream(value, this.charset));
	}

	public ValuePreparer createPreparer(String value)
	{
		if (StringTool.isEmpty(value))
		{
			return new StreamPreparer(this, null, 0);
		}
		try
		{
			byte[] buf = value.getBytes(this.charset);
			return new StreamPreparer(this, new ByteArrayInputStream(buf), buf.length);
		}
		catch (IOException ex)
		{
			throw new EternaException(ex);
		}
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

	public StreamPreparer(PreparerCreater creater, MemoryStream value)
	{
		super(creater);
		long len = value.getUsedSize();
		if (len > Integer.MAX_VALUE)
		{
			throw new EternaException("Too large value for InputStream.");
		}
		this.length = (int) len;
		this.value = value.getInputStream();
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
			// 这里使用MemoryStream, 原始的流可以关闭
			value.close();
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
