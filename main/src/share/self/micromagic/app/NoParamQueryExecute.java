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

package self.micromagic.app;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.HashMap;

import javax.sql.DataSource;

import self.micromagic.eterna.model.AppData;
import self.micromagic.eterna.model.Execute;
import self.micromagic.eterna.model.ModelAdapter;
import self.micromagic.eterna.model.ModelExport;
import self.micromagic.eterna.model.impl.AbstractExecute;
import self.micromagic.eterna.share.EternaFactory;
import self.micromagic.eterna.share.Generator;
import self.micromagic.eterna.share.EternaException;
import self.micromagic.eterna.sql.QueryAdapter;
import self.micromagic.eterna.sql.ResultIterator;
import self.micromagic.util.Utils;
import self.micromagic.util.container.SynHashMap;

/**
 * 根据传入的参数queryName执行相应的query, 被执行的query必须
 * 是没有参数的.
 *
 * 可设置的属性列表
 *
 * queryNameTag    从参数中获取query名称的参数名, 默认值为"queryName"
 *
 * queryName       直接设置query的名称, 如果设置了此参数, 将忽略queryNameTag
 *
 * cacheName       查询结果缓存cache的名称, 可以在多个实例中共享同一个缓存,
 *                 默认值为: cache
 *
 *
 * 可在对应的query中设置的属性
 *
 * cacheMinute    查询结果缓存的分钟数, -1表示永久缓存, 0表示不缓存, 默认值为0
 */
public class NoParamQueryExecute extends AbstractExecute
		implements Execute, Generator
{
	public static final String CACHE_TIME_TAG = "cacheMinute";

	/**
	 * cacheMap的缓存.
	 */
	private static final Map caches = new HashMap();

	/**
	 * 默认的缓存名称.
	 */
	private static final String DEFAULT_CACHE_NAME = "cache";

	protected Map cacheMap;

	protected String queryNameTag = "queryName";
	protected String queryName = null;
	protected String dataSourceName;
	protected EternaFactory factory;

	public void initialize(ModelAdapter model)
			throws EternaException
	{
		if (this.initialized)
		{
			return;
		}
		this.factory = model.getFactory();
		this.dataSourceName = model.getDataSourceName();
		String tmp = (String) this.getAttribute("queryNameTag");
		if (tmp != null)
		{
			this.queryNameTag = tmp;
		}
		tmp = (String) this.getAttribute("queryName");
		if (tmp != null)
		{
			this.queryName = tmp;
		}

		String cacheName = (String) this.getAttribute("cacheName");
		if (cacheName == null)
		{
			cacheName = DEFAULT_CACHE_NAME;
		}
		synchronized (caches)
		{
			this.cacheMap = (Map) caches.get(cacheName);
			if (this.cacheMap == null)
			{
				this.cacheMap = new SynHashMap();
				caches.put(cacheName, this.cacheMap);
			}
		}
	}

	public String getExecuteType()
			throws EternaException
	{
		return "noParamQuery";
	}

	public ModelExport execute(AppData data, Connection conn)
			throws EternaException, SQLException, IOException
	{
		String name = this.queryName != null ? this.queryName
				: data.getRequestParameter(this.queryNameTag);
		QueryAdapter query = factory.createQueryAdapter(name);
		String tmp = (String) query.getAttribute(CACHE_TIME_TAG);
		if (tmp != null)
		{
			int minute = Utils.parseInt(tmp);
			if (minute == 0)
			{
				data.dataMap.put(name, this.queryCodes(query, conn));
			}
			else
			{
				CacheContainer cc = (CacheContainer) this.cacheMap.get(name);
				long now = System.currentTimeMillis();
				if (cc != null && (minute == -1 || now < cc.expiredTime))
				{
					data.dataMap.put(name, cc.getQueryResult());
				}
				else
				{
					ResultIterator ritr = this.queryCodes(query, conn);
					cc = new CacheContainer(ritr, minute == -1 ? -1L : now + (minute * 60 * 1000L));
					this.cacheMap.put(name, cc);
					data.dataMap.put(name, cc.getQueryResult());
				}
			}
		}
		else
		{
			data.dataMap.put(name, this.queryCodes(query, conn));
		}
		return null;
	}

	protected ResultIterator queryCodes(QueryAdapter query, Connection conn)
			throws EternaException, SQLException
	{
		Connection myConn = conn;
		try
		{
			if (conn == null)
			{
				DataSource ds;
				if (this.dataSourceName == null)
				{
					ds = this.factory.getDataSourceManager().getDefaultDataSource();
				}
				else
				{
					ds = this.factory.getDataSourceManager().getDataSource(this.dataSourceName);
				}
				myConn = ds.getConnection();
				myConn.setAutoCommit(true);
			}
			return query.executeQuery(myConn);
		}
		finally
		{
			if (conn == null && myConn != null)
			{
				myConn.close();
			}
		}
	}

	/**
	 * 查询结果缓存的容器.
	 */
	static class CacheContainer
	{
		/**
		 * 查询的结果.
		 */
		private ResultIterator queryResult;

		/**
		 * 缓存的过期时间.
		 */
		public final long expiredTime;

		public CacheContainer(ResultIterator queryResult, long expiredTime)
		{
			this.queryResult = queryResult;
			this.expiredTime = expiredTime;
		}

		/**
		 * 获取缓存的查询结果, 会将结果集复制后返回.
		 */
		public ResultIterator getQueryResult()
				throws EternaException
		{
			return this.queryResult.copy();
		}

	}

}