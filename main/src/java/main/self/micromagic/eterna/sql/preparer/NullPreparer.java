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

import java.sql.SQLException;

import self.micromagic.eterna.sql.PreparedStatementWrap;

class NullPreparer extends AbstractValuePreparer
{
	private int type;

	public NullPreparer(ValuePreparerCreater vpc, int type)
	{
		super(vpc);
		this.type = type;
	}

	public void setValueToStatement(int index, PreparedStatementWrap stmtWrap)
			throws SQLException
	{
		stmtWrap.setNull(this.getName(), index, this.type);
	}

	static class Creater extends AbstractCreater
	{
		public Creater(ValuePreparerCreaterGenerator vpcg)
		{
			super(vpcg);
		}

		public ValuePreparer createPreparer(Object value)
		{
			return new NullPreparer(this, java.sql.Types.JAVA_OBJECT);
		}

		public ValuePreparer createPreparer(String value)
		{
			return new NullPreparer(this, java.sql.Types.VARCHAR);
		}

		public ValuePreparer createPreparer(int type)
		{
			return new NullPreparer(this, type);
		}

	}

}