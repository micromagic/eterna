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

import java.io.CharArrayReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.sql.SQLException;

import self.micromagic.eterna.dao.PreparedStatementWrap;
import self.micromagic.eterna.share.EternaException;
import self.micromagic.util.MemoryChars;
import self.micromagic.util.StringTool;
import self.micromagic.util.converter.ReaderConverter;

class CharsCreater extends AbstractPreparerCreater
{
	public CharsCreater(String name)
	{
		super(name);
	}
	private static final ReaderConverter convert = new ReaderConverter();

	public Object convertValue(Object value)
	{
		return convert.convertToReader(value);
	}

	public Object convertValue(String value)
	{
		return convert.convertToReader(value);
	}

	public ValuePreparer createPreparer(Object value)
	{
		if (value instanceof String)
		{
			return this.createPreparer((String) value);
		}
		else if (value instanceof char[])
		{
			char[] buf = (char[]) value;
			return new CharsPreparer(this, new CharArrayReader(buf), buf.length);
		}
		else if (value instanceof MemoryChars)
		{
			return new CharsPreparer(this, (MemoryChars) value);
		}
		return new CharsPreparer(this, convert.convertToReader(value));
	}

	public ValuePreparer createPreparer(String value)
	{
		if (StringTool.isEmpty(value))
		{
			return new CharsPreparer(this, null, 0);
		}
		return new CharsPreparer(this, new StringReader(value), value.length());
	}

}

class CharsPreparer extends AbstractValuePreparer
{
	private final Reader value;
	private final int length;

	public CharsPreparer(PreparerCreater creater, Reader value, int length)
	{
		super(creater);
		this.length = length;
		this.value = value;
	}

	public CharsPreparer(PreparerCreater creater, MemoryChars value)
	{
		super(creater);
		long len = value.getUsedSize();
		if (len > Integer.MAX_VALUE)
		{
			throw new EternaException("Too large value for Reader.");
		}
		this.length = (int) len;
		this.value = value.getReader();
	}

	public CharsPreparer(PreparerCreater creater, Reader value)
	{
		super(creater);
		if (value == null)
		{
			this.length = 0;
			this.value = null;
			return;
		}
		MemoryChars mc = new MemoryChars();
		Writer out = mc.getWriter();
		char[] buf = new char[1024];
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
					throw new EternaException("Too large value for Reader.");
				}
			}
			this.length = allCount;
			this.value = mc.getReader();
			// 这里使用MemoryChars, 原始的reader可以关闭
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
		stmtWrap.setCharacterStream(this.getName(), index, this.value, this.length);
	}

}
