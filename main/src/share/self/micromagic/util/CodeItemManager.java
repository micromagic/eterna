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

package self.micromagic.util;

import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import self.micromagic.app.WebApp;
import self.micromagic.eterna.digester.ConfigurationException;
import self.micromagic.eterna.digester.FactoryManager;
import self.micromagic.eterna.sql.QueryAdapter;
import self.micromagic.eterna.sql.ResultIterator;

public class CodeItemManager
{
	private static Map eNameMap = new HashMap();
	private static Map cNameMap = new HashMap();
	private static Map treeNameMap = new HashMap();

	private static Map itemMap = new HashMap();

	static
	{
		reInit();
	}

	public static void reInit()
	{
		reInit(null);
	}

	public static void reInit(StringRef msg)
	{
		try
		{
			eNameMap = new HashMap();
			cNameMap = new HashMap();
			treeNameMap = new HashMap();
			itemMap = new HashMap();

			String cn = Utility.getProperty("self.micromagic.codeItem.init");
			if (cn != null)
			{
				Class cl = Class.forName(cn);
				CodeItemManagerInit initObj = (CodeItemManagerInit) cl.newInstance();
				initObj.initCodeItem(eNameMap, cNameMap, treeNameMap);
			}

			ClassLoader cl = Utility.getContextClassLoader();
			if (cl == null)
			{
				cl = Utility.class.getClassLoader();
			}
			InputStream is = cl.getResourceAsStream("eterna_config.xml");
			if (is != null)
			{
				SAXReader saxReader = new SAXReader();
				Document document = saxReader.read(is);
				Element codeItems = document.getRootElement().element("code-items");
				List items = codeItems.elements("code-item");
				Iterator itr = items.iterator();
				while (itr.hasNext())
				{
					Element codeItem = (Element) itr.next();
					String cName = codeItem.attributeValue("cName");
					String eName = codeItem.attributeValue("eName");
					CodeItemProperty cip = new CodeItemProperty(cName, eName);
					eNameMap.put(eName, cip);
					cNameMap.put(cName, cip);

					String tableName = codeItem.attributeValue("tableName");
					if (tableName != null)
					{
						cip.setCodeTableName(tableName);
						String idColName = codeItem.attributeValue("idColName");
						String valueColName = codeItem.attributeValue("valueColName");
						cip.setCodeColumnName(idColName, valueColName);
						String indexColName = codeItem.attributeValue("indexColName");
						if (indexColName != null)
						{
							cip.setCodeIndexColumnName(indexColName);
						}
						String conditions = codeItem.attributeValue("conditions");
						if (conditions != null)
						{
							cip.setConditions(conditions);
						}

						String canCache = codeItem.attributeValue("canCache");
						if (canCache != null)
						{
							cip.setCanCache("true".equalsIgnoreCase(canCache));
						}
						String cacheTime = codeItem.attributeValue("cacheTime");
						if (cacheTime != null)
						{
							try
							{
								cip.setCacheTime(Long.parseLong(cacheTime));
							}
							catch (NumberFormatException ex) {}
						}

						List linkTables = codeItem.elements("link-table");
						Iterator tmpItr = linkTables.iterator();
						while (tmpItr.hasNext())
						{
							Element linkTable = (Element) tmpItr.next();
							cip.addLinkTable(linkTable.attributeValue("tableName"),
									linkTable.attributeValue("columnName"));
						}
					}
				}
			}
		}
		catch (Throwable ex)
		{
			WebApp.log.warn("Init code items.", ex);
			if (msg != null)
			{
				msg.setString(ex.getMessage());
			}
		}
	}

	public static CodeItemProperty getPropertyByCName(String name)
	{
		return (CodeItemProperty) cNameMap.get(name);
	}

	public static CodeItemProperty getPropertyByEName(String name)
	{
		return (CodeItemProperty) eNameMap.get(name);
	}

	public static String getTreeCName(String name)
	{
		return (String) treeNameMap.get(name);
	}

	public static List getCodeItems(Connection conn, CodeItemProperty cp)
			throws SQLException, ConfigurationException
	{
		if (cp == null)
		{
			throw new ConfigurationException("代码名称不正确!");
		}
		List list = (List) itemMap.get(cp.getEName());
		if (list == null || !cp.isCacheValid())
		{
			QueryAdapter query;
			if (cp.getCodeTableName() == null)
			{
				query = FactoryManager.getEternaFactory().createQueryAdapter(
						"util.get.codes");
				query.setString("typeId", cp.getEName());
			}
			else
			{
				query = FactoryManager.getEternaFactory().createQueryAdapter(
						"util.get.codes.other_table");
				query.setSubSQL(1, cp.getCodeIdColumnName());
				query.setSubSQL(2, cp.getCodeValueColumnName());
				if (cp.getCodeIndexColumnName() == null)
				{
					query.setSubSQL(3, "-1 AS theIndex");
					query.setSubSQL(6, "");
				}
				else
				{
					if (cp.getCodeIndexColumnName().equals(cp.getCodeIdColumnName()))
					{
						query.setSubSQL(3, "0 AS theIndex");
					}
					else
					{
						query.setSubSQL(3, cp.getCodeIndexColumnName());
					}
					query.setSubSQL(6, cp.getCodeIndexColumnName());
				}
				query.setSubSQL(5, cp.getConditions());
				query.setSubSQL(4, cp.getCodeTableName());
			}
			//System.out.println("sql:" + query.getPreparedSQL());

			ResultIterator ritr = query.executeQuery(conn);
			list = new ArrayList(ritr.getRecordCount());
			while (ritr.hasMoreRow())
			{
				list.add(ritr.nextRow());
			}

			list = Collections.unmodifiableList(list);
			if (cp.isCanCache())
			{
				itemMap.put(cp.getEName(), list);
				cp.itemCached();
			}
		}
		return list;
	}

	public static List getCodeItems(Connection conn, String codeType)
			throws SQLException, ConfigurationException
	{
		CodeItemProperty cp = getPropertyByEName(codeType);
		try
		{
			return getCodeItems(conn, cp);
		}
		catch (ConfigurationException ex)
		{
			WebApp.log.error("Error codeType:" + codeType + ".");
			throw ex;
		}
	}

	/**
	 * 读取树菜单数据的定义.
	 * 定义query名称的格式为："util.get.trees." + treeName。
	 * query中需给出：nodeId（节点的id），parentId（父节点的id），
	 *       nodeName（节点显示的名称），leafNode（是否为叶子节点）。
	 * 注：nodeId的名称必须是唯一的。
	 */
	public static List getTreeItems(Connection conn, String treeName)
			throws SQLException, ConfigurationException
	{
		if (!treeNameMap.containsKey(treeName))
		{
			throw new ConfigurationException("树菜单名称\"" + treeName + "\"不正确!");
		}

		List list = (List) itemMap.get("[tree]:" + treeName);
		if (list == null)
		{
			QueryAdapter query =  null;
			try
			{
				query = FactoryManager.getEternaFactory().createQueryAdapter(
						"util.get.trees." + treeName);
			}
			catch (ConfigurationException ex)
			{
				throw new ConfigurationException("未定义\"" + treeName + "\"的query，请参照说明定义!");
			}

			ResultIterator ritr = query.executeQuery(conn);
			list = new ArrayList(ritr.getRecordCount());
			while (ritr.hasMoreRow())
			{
				list.add(ritr.nextRow());
			}

			list = Collections.unmodifiableList(list);
			if (!treeName.endsWith(".noCache"))
			{
				itemMap.put("[tree]:" + treeName, list);
			}
		}
		return list;
	}

	public static void reloadCodeItems(String codeType)
	{
		itemMap.remove(codeType);
	}

	public static void reloadTreeItems(String treeName)
	{
		itemMap.remove("[tree]:" + treeName);
	}

	public static class CodeItemProperty
	{
		private String cName;
		private String eName;

		private String codeTableName;
		private String codeIdColName = "codeId";
		private String codeValueColName = "codeValue";
		private String codeIndexColName = null;
		private String conditions = "";
		private Object conditionObj = null;

		private boolean canCache = true;
		private long cacheTime = -1L;
		private long beginCacheTime = -1L;

		private ArrayList linkTables;

		public CodeItemProperty(String cName, String eName)
		{
			this.cName = cName;
			this.eName = eName;
			this.linkTables = new ArrayList();
		}

		public String getCName()
		{
			return this.cName;
		}

		public void setCName(String cName)
		{
			this.cName = cName;
		}

		public String getEName()
		{
			return this.eName;
		}

		public void setEName(String eName)
		{
			this.eName = eName;
		}

		public String getCodeTableName()
		{
			return this.codeTableName;
		}

		public void setCodeTableName(String codeTableName)
		{
			this.codeTableName = codeTableName;
		}

		public String getCodeIdColumnName()
		{
			return this.codeIdColName;
		}

		public String getCodeValueColumnName()
		{
			return this.codeValueColName;
		}

		public void setCodeColumnName(String codeIdCol, String codeValueCol)
		{
			this.codeIdColName = codeIdCol;
			this.codeValueColName = codeValueCol;
		}

		public void addLinkTable(String tabelName, String columnName)
		{
			this.linkTables.add(tabelName);
			this.linkTables.add(columnName);
		}

		public String getCodeIndexColumnName()
		{
			return this.codeIndexColName;
		}

		public void setCodeIndexColumnName(String codeIndexColName)
		{
			this.codeIndexColName = codeIndexColName;
		}

		public String getConditions()
		{
			if (this.conditionObj != null)
			{
				return this.conditionObj.toString();
			}
			return this.conditions;
		}

		public void setConditions(String conditions)
		{
			if (conditions != null)
			{
				if (conditions.startsWith("class:"))
				{
					String[] tmps = StringTool.separateString(conditions.substring(6), ",", true);
					String className = tmps[0];
					try
					{
						if (tmps.length > 1)
						{
							String[] params = new String[tmps.length - 1];
							System.arraycopy(tmps, 1, params, 0, params.length);
							Class c = Class.forName(className);
							Class[] ps = new Class[params.length];
							Arrays.fill(ps, String.class);
							Constructor constructor = c.getDeclaredConstructor(ps);
							this.conditionObj = constructor.newInstance(params);
						}
						else
						{
							this.conditionObj = Class.forName(className).newInstance();
						}
					}
					catch (Exception ex)
					{
						WebApp.log.error("In codeItem property set condition.", ex);
					}
				}
				else
				{
					this.conditions = conditions;
				}
			}
		}

		public void clearLinkTable()
		{
			this.linkTables.clear();
		}

		public String getLinkTableTableName(int index)
		{
			return (String) this.linkTables.get(index * 2);
		}

		public String getLinkTableColumnName(int index)
		{
			return (String) this.linkTables.get(index * 2 + 1);
		}

		public int getLinkTableCount()
		{
			return this.linkTables.size() / 2;
		}

		public boolean isCanCache()
		{
			return this.canCache;
		}

		public void setCanCache(boolean canCache)
		{
			this.canCache = canCache;
		}

		public long getCacheTime()
		{
			return this.cacheTime;
		}

		public void setCacheTime(long cacheTime)
		{
			this.cacheTime = cacheTime;
		}

		public boolean isCacheValid()
		{
			if (this.canCache)
			{
				if (this.cacheTime == -1L)
				{
					return true;
				}
				return this.beginCacheTime + System.currentTimeMillis() <= this.cacheTime;
			}
			return false;
		}

		public void itemCached()
		{
			this.beginCacheTime = System.currentTimeMillis();
		}

	}

}