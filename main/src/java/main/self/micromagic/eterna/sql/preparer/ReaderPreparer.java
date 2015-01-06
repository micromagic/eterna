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

import java.io.Reader;
import java.sql.SQLException;

import self.micromagic.eterna.sql.PreparedStatementWrap;
import self.micromagic.util.converter.ReaderConverter;

class ReaderPreparer extends AbstractValuePreparer
{
	private final Reader value;
	private final int length;

	public ReaderPreparer(ValuePreparerCreater vpc, Reader value, int length)
	{
		super(vpc);
		this.length = length;
		this.value = value;
	}

	public ReaderPreparer(ValuePreparerCreater vpc, Reader value)
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
			stmtWrap.setObject(this.getName(), index, this.value, java.sql.Types.LONGVARCHAR);
		}
		else
		{
			stmtWrap.setCharacterStream(this.getName(), index, this.value, this.length);
		}
	}

	static class Creater extends AbstractCreater
	{
		ReaderConverter convert = new ReaderConverter();

		public Creater(ValuePreparerCreaterGenerator vpcg)
		{
			super(vpcg);
		}

		public ValuePreparer createPreparer(Object value)
		{
			return new ReaderPreparer(this, this.convert.convertToReader(value));
		}

		public ValuePreparer createPreparer(String value)
		{
			return new ReaderPreparer(this, this.convert.convertToReader(value));
		}

		public ValuePreparer createPreparer(Reader value)
		{
			return new ReaderPreparer(this, value);
		}

		public ValuePreparer createPreparer(Reader value, int length)
		{
			return new ReaderPreparer(this, value, length);
		}

	}

}