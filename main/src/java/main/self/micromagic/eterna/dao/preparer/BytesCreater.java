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

import java.sql.SQLException;

import self.micromagic.eterna.dao.PreparedStatementWrap;
import self.micromagic.eterna.share.AttributeManager;
import self.micromagic.util.converter.BytesConverter;

class BytesCreater extends AbstractPreparerCreater
{
	public BytesCreater(String name)
	{
		super(name);
	}
	private static final BytesConverter convert = new BytesConverter();
	String charset = "UTF-8";

	public void setCharset(String charset)
	{
		this.charset = charset;
	}

	public ValuePreparer createPreparer(Object value)
	{
		return new BytesPreparer(this, convert.convertToBytes(value, this.charset));
	}

	public ValuePreparer createPreparer(String value)
	{
		return new BytesPreparer(this, convert.convertToBytes(value, this.charset));
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

	public void setFormat(String format)
	{
	}

}

class BytesPreparer extends AbstractValuePreparer
{
	private final byte[] value;

	public BytesPreparer(PreparerCreater creater, byte[] value)
	{
		super(creater);
		this.value = value;
	}

	public void setValueToStatement(int index, PreparedStatementWrap stmtWrap)
			throws SQLException
	{
		stmtWrap.setBytes(this.getName(), index, this.value);
	}

}