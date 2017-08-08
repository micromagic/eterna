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

package self.micromagic.dbvm.core;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import self.micromagic.dbvm.OptDesc;
import self.micromagic.eterna.dao.impl.BaseDao;
import self.micromagic.eterna.dao.impl.ScriptParser;
import self.micromagic.eterna.share.EternaFactory;
import self.micromagic.eterna.share.Tool;
import self.micromagic.util.StringAppender;
import self.micromagic.util.StringTool;
import self.micromagic.util.container.ThreadCache;
import self.micromagic.util.ref.BooleanRef;

/**
 * 检测的处理器.
 */
public class CheckHandler
{
	/**
	 * 检测是否存在主键的索引名称.
	 */
	public static final String KEY_FLAG = "$key";

	/**
	 * 线程缓存中放置此对象的名称.
	 */
	private static final String THREAD_CACHE_FLAG = "eterna.dbvm.checkHandler";

	/**
	 * 需要检测的表名.
	 */
	public String tableName;

	/**
	 * 需要检测的列名.
	 */
	public String columnName;

	/**
	 * 需要检测的索引名.
	 */
	public String indexName;

	/**
	 * 检测的是存在还是不存在的标识.
	 */
	public boolean existsFlag;

	// 检测的结果
	private Boolean checkResult;
	// 嵌套时的父检测处理器
	private CheckHandler parent;
	// 相关的操作列表
	private List optList;

	/**
	 * 初始化.
	 */
	public void initialize(EternaFactory factory, OptDesc opt)
	{
		if (this.optList == null)
		{
			this.optList = new ArrayList();
			// 首次初始化 , 需要处理表名及列名等
			this.tableName = ScriptParser.checkNameForQuote(
					Tool.resolveConst(this.tableName, factory));
			this.columnName = ScriptParser.checkNameForQuote(this.columnName);
			String tmpIndexName = Tool.resolveConst(this.indexName, factory);
			if (KEY_FLAG.equalsIgnoreCase(tmpIndexName))
			{
				this.indexName = KEY_FLAG;
			}
			else
			{
				this.indexName = ScriptParser.checkNameForQuote(tmpIndexName);
			}
		}
		this.optList.add(opt.getName());
		if (this.parent != null)
		{
			this.parent.initialize(factory, opt);
		}
	}

	/**
	 * 获取相关的操作个数.
	 */
	public int getOptCount()
	{
		return this.optList == null ? 0 : this.optList.size();
	}

	/**
	 * 执行检测.
	 *
	 * @param first  出参, 是否为第一次进入判断
	 */
	public boolean doCheck(Connection conn, BooleanRef first)
	{
		if (this.checkResult != null)
		{
			first.value = false;
			return this.checkResult.booleanValue();
		}
		if (this.parent != null && !this.parent.doCheck(conn, first))
		{
			this.checkResult = Boolean.FALSE;
			return false;
		}
		first.value = true;
		boolean result;
		Savepoint savepoint = null;
		try
		{
			savepoint = BaseDao.makeSavepoint(conn, "check-".concat(this.tableName));
			result = this.checkTableWithColumn(conn);
			if (result && !StringTool.isEmpty(this.indexName))
			{
				result = this.checkIndex(this.tableName.toUpperCase(), conn)
						|| this.checkIndex(this.tableName.toLowerCase(), conn);
			}
		}
		catch (SQLException ex)
		{
			BaseDao.rollbackWithError(ex, savepoint, conn);
			result = false;;
		}
		this.checkResult = this.existsFlag ^ result ? Boolean.FALSE : Boolean.TRUE;
		return this.checkResult.booleanValue();
	}

	/**
	 * 检查表的索引是否存在.
	 *
	 * @param tableName  表名, 已做了转大写或小写处理
	 */
	private boolean checkIndex(String tableName, Connection conn)
			throws SQLException
	{
		ResultSet result = null;
		try
		{
			if (KEY_FLAG.equals(this.indexName))
			{
				result = conn.getMetaData().getPrimaryKeys(null, null, tableName);
				return result.next();
			}
			else
			{
				result = conn.getMetaData().getIndexInfo(
						null, null, tableName, false, false);
				while (result.next())
				{
					if (this.indexName.equalsIgnoreCase(result.getString("INDEX_NAME")))
					{
						return true;
					}
				}
				return false;
			}
		}
		finally
		{
			BaseDao.doClose(result, null);
		}
	}

	/**
	 * 检查表及列名是否存在.
	 */
	private boolean checkTableWithColumn(Connection conn)
			throws SQLException
	{
		Statement stmt = null;
		ResultSet result = null;
		StringAppender scriptBuf = StringTool.createStringAppender();
		scriptBuf.append("select count(*) from ").append(this.tableName);
		if (!StringTool.isEmpty(this.columnName))
		{
			scriptBuf.append(" where ").append(this.columnName).append(" is null");
		}
		try
		{
			String script = ScriptParser.checkScriptNameQuote(conn, scriptBuf.toString());
			stmt = conn.createStatement();
			result = stmt.executeQuery(script);
			return true;
		}
		finally
		{
			BaseDao.doClose(result, stmt);
		}
	}

	/**
	 * 设置检测的结果.
	 */
	public void setCheckResult(Boolean result)
	{
		this.checkResult = result;
		if (this.parent != null)
		{
			this.parent.setCheckResult(result);
		}
	}

	/**
	 * 获取当前的检测对象.
	 */
	static CheckHandler getCurrentCheck()
	{
		ThreadCache cache = ThreadCache.getInstance();
		return (CheckHandler) cache.getProperty(THREAD_CACHE_FLAG);
	}

	/**
	 * 初始化当前的检测对象.
	 */
	static void initCurrentCheck(CheckHandler current)
	{
		ThreadCache cache = ThreadCache.getInstance();
		CheckHandler old = (CheckHandler) cache.getProperty(THREAD_CACHE_FLAG);
		current.parent = old;
		cache.setProperty(THREAD_CACHE_FLAG, current);
	}

	/**
	 * 移除当前的检测对象.
	 */
	static void removeCurrentCheck()
	{
		ThreadCache cache = ThreadCache.getInstance();
		CheckHandler now = (CheckHandler) cache.getProperty(THREAD_CACHE_FLAG);
		if (now != null && now.parent != null)
		{
			cache.setProperty(THREAD_CACHE_FLAG, now.parent);
		}
		else
		{
			cache.removeProperty(THREAD_CACHE_FLAG);
		}
	}

}
