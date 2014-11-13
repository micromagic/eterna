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

class ObjectPreparer extends AbstractValuePreparer
{
	protected Object value;
	protected Integer targetSqlType;
	protected Integer scale;

	public ObjectPreparer(ValuePreparerCreater vpc, Object value)
	{
		this(vpc, value, null, null);
	}

	public ObjectPreparer(ValuePreparerCreater vpc, Object value, Integer targetSqlType)
	{
		this(vpc, value, targetSqlType, null);
	}

	public ObjectPreparer(ValuePreparerCreater vpc, Object value, Integer targetSqlType, Integer scale)
	{
		super(vpc);
		this.value = value;
		this.targetSqlType = targetSqlType;
		this.scale = scale;
	}

	public void setValueToStatement(int index, PreparedStatementWrap stmtWrap)
			throws SQLException
	{
		if (this.targetSqlType == null)
		{
			stmtWrap.setObject(this.getName(), index, this.value);
		}
		else if (this.scale == null)
		{
			stmtWrap.setObject(this.getName(), index, this.value, this.targetSqlType.intValue());
		}
		else
		{
			stmtWrap.setObject(this.getName(), index, this.value,
					this.targetSqlType.intValue(), this.scale.intValue());
		}
	}

	static class Creater extends AbstractCreater
	{
		public Creater(ValuePreparerCreaterGenerator vpcg)
		{
			super(vpcg);
		}

		public ValuePreparer createPreparer(Object value)
		{
			return new ObjectPreparer(this, value);
		}

		public ValuePreparer createPreparer(String value)
		{
			return new ObjectPreparer(this, value);
		}

		public ValuePreparer createPreparer(Object value, int targetSqlType)
		{
			return new ObjectPreparer(this, value, new Integer(targetSqlType));
		}

		public ValuePreparer createPreparer(Object value, int targetSqlType, int scale)
		{
			return new ObjectPreparer(this, value, new Integer(targetSqlType),
					new Integer(scale));
		}

	}

}