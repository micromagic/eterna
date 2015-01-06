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

package self.micromagic.eterna.sql.preparer;

import java.io.InputStream;
import java.sql.SQLException;

import org.apache.commons.logging.Log;
import self.micromagic.eterna.sql.PreparedStatementWrap;
import self.micromagic.util.converter.StreamConverter;
import self.micromagic.util.Utility;

class StreamPreparer extends AbstractValuePreparer
{
	private InputStream value;
	private int length;

	public StreamPreparer(ValuePreparerCreater vpc, InputStream value, int length)
	{
		super(vpc);
		this.length = length;
		this.value = value;
	}
	public StreamPreparer(ValuePreparerCreater vpc, InputStream value)
	{
		super(vpc);
		this.length = -1;
		this.value = value;
	}

	public void setValueToStatement(int index, PreparedStatementWrap stmtWrap)
			throws SQLException
	{
		if (this.length == -1)
		{
			stmtWrap.setObject(this.getName(), index, this.value, java.sql.Types.LONGVARBINARY);
		}
		else
		{
			stmtWrap.setBinaryStream(this.getName(), index, this.value, this.length);
		}
	}

	static class Creater extends AbstractCreater
	{
		StreamConverter convert = new StreamConverter();
		String charset = "UTF-8";

		public Creater(ValuePreparerCreaterGenerator vpcg)
		{
			super(vpcg);
		}

		public void setCharset(String charset)
		{
			this.charset = charset;
		}

		public ValuePreparer createPreparer(Object value)
		{
			return new StreamPreparer(this, this.convert.convertToStream(value, this.charset));
		}

		public ValuePreparer createPreparer(String value)
		{
			return new StreamPreparer(this, this.convert.convertToStream(value, this.charset));
		}

		public ValuePreparer createPreparer(InputStream value)
		{
			return new StreamPreparer(this, value);
		}

		public ValuePreparer createPreparer(InputStream value, int length)
		{
			return new StreamPreparer(this, value, length);
		}

	}

}