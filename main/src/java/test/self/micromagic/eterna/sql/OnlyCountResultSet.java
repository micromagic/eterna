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

package self.micromagic.eterna.sql;

import java.sql.ResultSet;
import java.sql.SQLException;

public class OnlyCountResultSet extends ResultSetWrap
{
	/**
	 * 总记录数.
	 */
	private int recordCount;

	/**
	 * 当前记录的位置.
	 */
	private int current;

	/**
	 * 结果集的类型.
	 */
	private int type;

	public OnlyCountResultSet(int recordCount)
	{
		this.recordCount = recordCount;
		this.type = ResultSet.TYPE_FORWARD_ONLY;
	}

	public OnlyCountResultSet(int recordCount, int type)
	{
		this.type = type;
		this.recordCount = recordCount;
	}

	public boolean next() throws SQLException
	{
		return this.current <= this.recordCount ? this.current++ < this.recordCount : false;
	}

	public boolean isBeforeFirst() throws SQLException
	{
		return this.current == 0;
	}

	public boolean isAfterLast() throws SQLException
	{
		return this.current == this.recordCount + 1;
	}

	public boolean isFirst() throws SQLException
	{
		return this.current == 1;
	}

	public boolean isLast() throws SQLException
	{
		return this.current == this.recordCount;
	}

	public void beforeFirst() throws SQLException
	{
		this.current = 0;
	}

	public void afterLast() throws SQLException
	{
		this.current = this.recordCount + 1;
	}

	public boolean first() throws SQLException
	{
		if (this.type == ResultSet.TYPE_FORWARD_ONLY)
		{
			throw new SQLException();
		}
		if (this.recordCount > 0)
		{
			this.current = 1;
			return true;
		}
		return false;
	}

	public boolean last() throws SQLException
	{
		if (this.type == ResultSet.TYPE_FORWARD_ONLY)
		{
			throw new SQLException();
		}
		this.current = this.recordCount;
		return this.recordCount > 0;
	}

	public int getRow() throws SQLException
	{
		return this.current > 0 && this.current <= this.recordCount ? this.current : 0;
	}

	public boolean absolute(int row) throws SQLException
	{
		if (this.type == ResultSet.TYPE_FORWARD_ONLY)
		{
			throw new SQLException();
		}
		int result = row > 0 ? row : this.recordCount - row + 1;
		if (result <= 0)
		{
			this.current = 0;
			return false;
		}
		if (result > this.recordCount)
		{
			this.current = this.recordCount + 1;
			return false;
		}
		this.current = result;
		return true;
	}

	public boolean relative(int rows) throws SQLException
	{
		if (this.type == ResultSet.TYPE_FORWARD_ONLY)
		{
			throw new SQLException();
		}
		int result = this.current + rows;
		if (result <= 0)
		{
			this.current = 0;
			return false;
		}
		if (result > this.recordCount)
		{
			this.current = this.recordCount + 1;
			return false;
		}
		this.current = result;
		return true;
	}

	public boolean previous() throws SQLException
	{
		if (this.type == ResultSet.TYPE_FORWARD_ONLY)
		{
			throw new SQLException();
		}
		return this.current > 0 ? this.current-- >= 1 : false;
	}

	public int getType() throws SQLException
	{
		return this.type;
	}

}