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

package self.micromagic.eterna.dao.impl;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import self.micromagic.eterna.dao.Query;
import self.micromagic.eterna.share.EternaException;
import self.micromagic.util.StringAppender;
import self.micromagic.util.StringTool;
import self.micromagic.util.Utility;

/**
 * 查询执行的辅助工具.
 * 1 用于处理带记录数限制的查询语句的生成.
 * 2 获取相应的记录.
 */
public class QueryHelper
{
	/**
	 * mysql数据库名称.
	 */
	public static final String DB_NAME_MYSQL = "MySQL";

	/**
	 * oracle数据库名称.
	 */
	public static final String DB_NAME_ORACLE = "Oracle";

	/**
	 * 其他普通数据库名称
	 */
	public static final String DB_NAME_COMMON = "Common";

	/**
	 * 获取一个查询辅助工具的实例.
	 *
	 * @param query      查询的对象, 用于构造查询辅助工具
	 * @param conn       数据库链接, 用于获取数据库的类型
	 * @param oldHelper  旧的查询辅助工具, 如果当前数据库类型和旧的查询辅助工具的类型相同,
	 *                   则返回这个旧的查询辅助工具
	 */
	public static QueryHelper getInstance(Query query, Connection conn, QueryHelper oldHelper)
			throws SQLException
	{
		String dbName = conn.getMetaData().getDatabaseProductName();
		if (DB_NAME_ORACLE.equals(dbName))
		{
			return oldHelper != null && DB_NAME_ORACLE.equals(oldHelper.getType()) ?
					 oldHelper : new OracleQueryHelper(query);
		}
		else if (DB_NAME_MYSQL.equals(dbName))
		{
			return oldHelper != null && DB_NAME_MYSQL.equals(oldHelper.getType()) ?
					 oldHelper : new MySqlQueryHelper(query);
		}
		return oldHelper != null && DB_NAME_COMMON.equals(oldHelper.getType()) ?
				oldHelper : new QueryHelper(query);
	}


	private final Query query;

	/**
	 * 构造函数.
	 *
	 * @param query    执行查询的对象
	 */
	public QueryHelper(Query query)
	{
		this.query = query;
	}

	/**
	 * 获取执行查询的对象.
	 */
	protected Query getQueryAdapter()
	{
		return this.query;
	}

	/**
	 * 获取当前查询工具的类型.
	 */
	public String getType()
	{
		return DB_NAME_COMMON;
	}

	/**
	 * 根据原始语句, 生成处理后的, 带记录数限制的查询语句.
	 */
	public String getQuerySQL(String preparedSQL)
			throws EternaException
	{
		return preparedSQL;
	}

	/**
	 * 获取本次查询从第几条记录开始读取, 默认值为"1".
	 */
	public int getStartRow()
			throws SQLException
	{
		return this.query.getStartRow();
	}

	/**
	 * 获取本次查询读取的最大记录数, 默认值为"-1", 表示取完为止.
	 */
	public int getMaxRows()
			throws SQLException
	{
		return this.query.getMaxRows();
	}

	/**
	 * 获取本次查询设置的总记录数.
	 */
	public int getTotalCount()
			throws EternaException
	{
		return this.query.getTotalCount();
	}

	/**
	 * 获取该查询对象设置的总记录数扩展信息.
	 */
	public Query.TotalCountInfo getTotalCountExt()
			throws EternaException
	{
		return this.query.getTotalCountInfo();
	}

	/**
	 * 读取结果集中的数据.
	 *
	 * @param rs           被读取数据的结果集
	 * @param readerList   描述要读取的数据的ResultReader对象列表
	 * @return     包含数据结果的List对象, 里面的每个值为Object数组
	 */
	public List readResults(ResultSet rs, List readerList)
			throws EternaException, SQLException
	{
		int start = this.getStartRow() - 1;
		int tmpRecordCount = 0;
		this.recordCount = 0;
		this.realRecordCount = 0;
		this.realRecordCountAvailable = false;
		this.hasMoreRecord = false;
		this.needCount = false;
		boolean hasRecord = true;
		boolean isForwardOnly = rs.getType() == ResultSet.TYPE_FORWARD_ONLY;

		if (start > 0)
		{
			if (!isForwardOnly)
			{
				// 这里不需要加1, 因为需要定位到前一条
				hasRecord = rs.absolute(start);
				if (!hasRecord)
				{
					rs.last();
				}
				// 如果是最后一条, 需要将tmpRecordCount加1
				// 因为下面分支中的循环在记录数小于start时会多记1
				tmpRecordCount = hasRecord ? rs.getRow() : rs.getRow() + 1;
			}
			else
			{
				for (; tmpRecordCount < start && hasRecord; tmpRecordCount++, hasRecord = rs.next());
			}
		}
		ArrayList result;
		if (!hasRecord)
		{
			int totalCount = this.getTotalCount();
			if (totalCount >= 0)
			{
				this.setTotalCountInfo(totalCount, this.getTotalCountExt());
			}
			else
			{
				// 没有记录数表示已经移到的最后一条, 临时记录数减1为总记录数
				this.realRecordCount = tmpRecordCount - 1;
				this.realRecordCountAvailable = true;
				this.hasMoreRecord = false;
			}
			result = new ArrayList(0);
		}
		else
		{
			int maxRows = this.getMaxRows();
			result = new ArrayList(maxRows == -1 ? 32 : maxRows);
			if (maxRows == -1)
			{
				while (rs.next())
				{
					tmpRecordCount++;
					result.add(QueryImpl.getResults(this.query, readerList, rs));
				}
				int totalCount = this.getTotalCount();
				if (totalCount >= 0)
				{
					this.setTotalCountInfo(totalCount, this.getTotalCountExt());
				}
				else
				{
					// 因为读取了所有记录, 所以可以设置总记录数
					this.realRecordCount = tmpRecordCount;
					this.realRecordCountAvailable = true;
					this.hasMoreRecord = false;
				}
			}
			else
			{
				int i = 0;
				for (; i < maxRows && (this.hasMoreRecord = rs.next()); i++)
				{
					tmpRecordCount++;
					result.add(QueryImpl.getResults(this.query, readerList, rs));
				}
				// 这么判断是防止某些jdbc在第一次next为false后, 后面的next又变回true
				if (this.hasMoreRecord && (this.hasMoreRecord = rs.next()))
				{
					tmpRecordCount++;
					this.realRecordCountAvailable = false;
				}
				else
				{
					// 如果没有更多记录的话, 则可以确定总记录数
					this.realRecordCountAvailable = true;
				}

				int totalCount = this.getTotalCount();
				if (totalCount == Query.TOTAL_COUNT_AUTO)
				{
					if (!isForwardOnly)
					{
						rs.last();
						this.realRecordCount = rs.getRow();
					}
					else
					{
						if (this.hasMoreRecord)
						{
							for (; rs.next(); tmpRecordCount++);
						}
						this.realRecordCount = tmpRecordCount;
					}
					this.realRecordCountAvailable = true;
				}
				else if (totalCount == Query.TOTAL_COUNT_NONE)
				{
					this.realRecordCount = tmpRecordCount;
				}
				else if (totalCount == Query.TOTAL_COUNT_COUNT)
				{
					if (!this.realRecordCountAvailable)
					{
						// 如果没有确定总记录数, 则需要执行统计查询
						this.needCount = true;
					}
					else
					{
						this.realRecordCount = tmpRecordCount;
					}
				}
				else if (totalCount >= 0)
				{
					this.setTotalCountInfo(totalCount, this.getTotalCountExt());
				}
			}
		}

		this.recordCount = result.size();
		return result;
	}

	/**
	 * 当totalCount为0-N时设置总记录数等信息.
	 */
	protected void setTotalCountInfo(int totalCount, Query.TotalCountInfo ext)
	{
		this.realRecordCount = totalCount;
		this.realRecordCountAvailable = true;
		if (ext != null)
		{
			this.hasMoreRecord = ext.hasMoreRecord;
			this.realRecordCountAvailable = ext.totalCountAvailable;
		}
	}

	protected int recordCount;
	protected int realRecordCount;
	protected boolean realRecordCountAvailable;
	protected boolean hasMoreRecord;
	protected boolean needCount;

	/**
	 * 本次读取结果中的记录数.
	 */
	public int getRecordCount()
	{
		return this.recordCount;
	}

	/**
	 * 实际查询结果中的总记录数.
	 */
	public int getRealRecordCount()
	{
		return this.realRecordCount;
	}

	/**
	 * 总记录数中的值是否有效.
	 */
	public boolean isRealRecordCountAvailable()
	{
		return this.realRecordCountAvailable;
	}

	/**
	 * 实际查询结果中是否还有更多的记录.
	 */
	public boolean isHasMoreRecord()
	{
		return this.hasMoreRecord;
	}

	/**
	 * 是否需要通过计数查询获取总记录数.
	 */
	public boolean needCount()
	{
		return this.needCount;
	}


}


abstract class SpecialQueryHelper extends QueryHelper
{
	protected int nowStartRow = 1;
	protected int nowMaxRows = -1;
	protected int nowTotalCount = Query.TOTAL_COUNT_NONE;
	protected Query.TotalCountInfo nowTotalCountExt;
	protected String oldPreparedSQL;
	protected String cacheSQL;
	protected boolean useOldSQL;

	public SpecialQueryHelper(Query query)
	{
		super(query);
	}

	/**
	 * 构造特殊的用于分页的SQL语句.
	 */
	protected abstract String createSpecialSQL(String preparedSQL);

	public String getQuerySQL(String preparedSQL)
			throws EternaException
	{
		Query query = this.getQueryAdapter();
		if (this.cacheSQL != null)
		{
			try
			{
				if (this.oldPreparedSQL != preparedSQL || this.nowStartRow != query.getStartRow()
						|| this.nowMaxRows != query.getMaxRows() || this.nowTotalCount != query.getTotalCount()
						|| !Utility.objectEquals(this.nowTotalCountExt, query.getTotalCountInfo()))
				{
					this.cacheSQL = null;
				}
			}
			catch (SQLException ex)
			{
				throw new EternaException(ex);
			}
		}
		if (this.cacheSQL == null)
		{
			this.oldPreparedSQL = preparedSQL;
			try
			{
				this.nowStartRow = query.getStartRow();
				this.nowMaxRows = query.getMaxRows();
				this.nowTotalCount = query.getTotalCount();
				this.nowTotalCountExt = query.getTotalCountInfo();
			}
			catch (SQLException ex)
			{
				throw new EternaException(ex);
			}
			// 如果是读取全部记录或使用自动计数, 则使用原始的语句
			this.useOldSQL = (this.nowMaxRows == -1 && this.nowStartRow == 1)
					|| this.nowTotalCount == Query.TOTAL_COUNT_AUTO;
			if (this.useOldSQL)
			{
				this.cacheSQL = preparedSQL;
			}
			else
			{
				this.cacheSQL = this.createSpecialSQL(preparedSQL);
			}
		}
		return this.cacheSQL;
	}

	public List readResults(ResultSet rs, List readerList)
			throws EternaException, SQLException
	{
		if (this.useOldSQL)
		{
			return super.readResults(rs, readerList);
		}
		int tmpRecordCount = 0;
		this.recordCount = 0;
		this.realRecordCount = 0;
		this.realRecordCountAvailable = false;
		this.hasMoreRecord = false;
		this.needCount = false;

		Query query = this.getQueryAdapter();
		ArrayList result = new ArrayList(this.nowMaxRows == -1 ? 32 : this.nowMaxRows);
		if (this.nowMaxRows == -1)
		{
			// 没有限制获取的记录数时的处理
			while (rs.next())
			{
				tmpRecordCount++;
				result.add(QueryImpl.getResults(query, readerList, rs));
			}
			int totalCount = this.nowTotalCount;
			if (totalCount >= 0)
			{
				this.setTotalCountInfo(totalCount, this.nowTotalCountExt);
			}
			else
			{
				if (tmpRecordCount > 0)
				{
					// 必须读取到记录才能设置总记录数
					this.realRecordCount = tmpRecordCount += this.nowStartRow - 1;
					this.realRecordCountAvailable = true;
				}
				else if (totalCount == Query.TOTAL_COUNT_COUNT)
				{
					// 如果没有获取到记录且需要统计计数, 则需要进行统计查询
					// 因为这时可能起始行大于总记录数
					this.needCount = true;
				}
				this.hasMoreRecord = false;
			}
		}
		else
		{
			int i = 0;
			for (; i < this.nowMaxRows && (this.hasMoreRecord = rs.next()); i++)
			{
				tmpRecordCount++;
				result.add(QueryImpl.getResults(query, readerList, rs));
			}
			// 这么判断是防止某些jdbc在第一次next为false后, 后面的next又变回true
			if (this.hasMoreRecord && (this.hasMoreRecord = rs.next()))
			{
				tmpRecordCount += this.nowStartRow;
				this.realRecordCountAvailable = false;
			}
			else if (tmpRecordCount > 0)
			{
				// 如果没有更多记录的话, 则可以确定总记录数
				tmpRecordCount += this.nowStartRow - 1;
				this.realRecordCountAvailable = true;
			}

			int totalCount = this.nowTotalCount;
			if (totalCount == Query.TOTAL_COUNT_NONE)
			{
				this.realRecordCount = tmpRecordCount;
			}
			else if (totalCount == Query.TOTAL_COUNT_COUNT)
			{
				if (!this.realRecordCountAvailable)
				{
					// 如果没有确定总记录数, 则需要执行统计查询
					this.needCount = true;
				}
				else
				{
					this.realRecordCount = tmpRecordCount;
				}
			}
			else if (totalCount >= 0)
			{
				this.setTotalCountInfo(totalCount, this.nowTotalCountExt);
			}
		}

		this.recordCount = result.size();
		return result;
	}

}

class OracleQueryHelper extends SpecialQueryHelper
{
	public OracleQueryHelper(Query query)
	{
		super(query);
	}

	/**
	 * 获取当前查询工具的类型.
	 */
	public String getType()
	{
		return DB_NAME_ORACLE;
	}

	protected String createSpecialSQL(String preparedSQL)
	{
		if (this.nowStartRow == 1)
		{
			String part1 = "select * from (";
			String part2 = ") tmpTable where rownum <= " + (this.nowMaxRows + 1);
			StringAppender buf = StringTool.createStringAppender(
					part1.length() + part2.length() + preparedSQL.length());
			buf.append(part1).append(preparedSQL).append(part2);
			return buf.toString();
		}
		else
		{
			String condition1 = this.nowMaxRows == -1 ? ""
					: " where rownum <= " + (this.nowMaxRows + this.nowStartRow);
			String condition2 = " where theOracleRuwNum >= " + this.nowStartRow;
			String part1 = "select * from (select tmpTable1.*, rownum as theOracleRuwNum from (";
			String part2 = ") tmpTable1" + condition1 + ") tmpTable2" + condition2;
			StringAppender buf = StringTool.createStringAppender(
					part1.length() + part2.length() + preparedSQL.length());
			buf.append(part1).append(preparedSQL).append(part2);
			return buf.toString();
		}
	}

}

class MySqlQueryHelper extends SpecialQueryHelper
{
	public MySqlQueryHelper(Query query)
	{
		super(query);
	}

	/**
	 * 获取当前查询工具的类型.
	 */
	public String getType()
	{
		return DB_NAME_MYSQL;
	}

	protected String createSpecialSQL(String preparedSQL)
	{
		if (this.nowStartRow == 1)
		{
			String appendStr = " limit " + (this.nowMaxRows + 1);
			return preparedSQL + appendStr;
		}
		else
		{
			int count = this.nowMaxRows == -1 ? Integer.MAX_VALUE : this.nowMaxRows + 1;
			String appendStr = " limit " + (this.nowStartRow - 1) + ", " + count;
			return preparedSQL + appendStr;
		}
	}

}