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

package self.micromagic.eterna.search.impl;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import self.micromagic.eterna.dao.EntityItem;
import self.micromagic.eterna.dao.EntityRef;
import self.micromagic.eterna.dao.Query;
import self.micromagic.eterna.dao.ResultIterator;
import self.micromagic.eterna.dao.ResultReaderManager;
import self.micromagic.eterna.dao.impl.EntityImpl;
import self.micromagic.eterna.dao.impl.ParameterGroup;
import self.micromagic.eterna.dao.preparer.PreparerManager;
import self.micromagic.eterna.model.AppData;
import self.micromagic.eterna.search.ColumnSetting;
import self.micromagic.eterna.search.ConditionProperty;
import self.micromagic.eterna.search.ParameterSetting;
import self.micromagic.eterna.search.Search;
import self.micromagic.eterna.search.SearchManager;
import self.micromagic.eterna.search.SearchResult;
import self.micromagic.eterna.security.EmptyPermission;
import self.micromagic.eterna.security.User;
import self.micromagic.eterna.security.UserManager;
import self.micromagic.eterna.share.AbstractGenerator;
import self.micromagic.eterna.share.AttributeManager;
import self.micromagic.eterna.share.EternaCreater;
import self.micromagic.eterna.share.EternaException;
import self.micromagic.eterna.share.EternaFactory;
import self.micromagic.eterna.share.OrderManager;
import self.micromagic.eterna.share.Tool;
import self.micromagic.eterna.view.Component;
import self.micromagic.util.StringTool;
import self.micromagic.util.container.SessionCache;
import self.micromagic.util.converter.BooleanConverter;
import self.micromagic.util.ref.BooleanRef;

/**
 * @author micromagic@sina.com
 */
public class SearchImpl extends AbstractGenerator
		implements Search, EternaCreater
{
	private int pageSize = -1;

	private String sessionQueryTag;
	private String searchManagerName = null;
	private String queryName;
	private int queryId;
	private String columnType;
	private ColumnSetting columnSetting = null;
	private ParameterSetting parameterSetting = null;
	private int countType = Query.TOTAL_COUNT_AUTO;
	private String countReaderName = null;
	private String countSearchName = null;
	private int countSearchId = -1;

	private String otherName;
	private Search[] others = null;

	private boolean needWrap = true;
	private boolean specialCondition = false;
	private int conditionIndex;

	private String conditionPropertyOrder = null;
	private final Map conditionPropertyMap =  new HashMap();
	private List conditionProperties = new LinkedList();
	private ConditionProperty[] allConditionProperties = null;

	/**
	 * 执行搜索时(doSearch), 是否要加同步锁.
	 * 在search的attribute中通过needSynchronize属性名进行设置.
	 */
	protected boolean needSynchronize = false;

	private boolean initialized = false;

	public boolean initialize(EternaFactory factory)
			throws EternaException
	{
		if (this.initialized)
		{
			return true;
		}
		this.initialized = true;
		this.sessionQueryTag = "s:" + factory.getFactoryContainer().getId() + ":" + this.getName();
		if (this.searchManagerName == null)
		{
			this.searchManagerName = this.sessionQueryTag;
		}

		if (this.otherName != null)
		{
			if (this.otherName.indexOf(',') == -1)
			{
				this.others = new Search[1];
				this.others[0] = factory.createSearch(this.otherName);
				if (this.others[0] == null)
				{
					log.warn("The search parent [" + this.otherName + "] not found.");
				}
			}
			else
			{
				StringTokenizer token = new StringTokenizer(this.otherName, ",");
				this.others = new Search[token.countTokens()];
				for (int i = 0; i < this.others.length; i++)
				{
					String temp = token.nextToken().trim();
					this.others[i] = factory.createSearch(temp);
					if (this.others[i] == null)
					{
						log.warn("The search parent [" + temp + "] not found.");
					}
				}
			}
		}

		if (this.queryName != null && !NONE_QUERY_NAME.equals(this.queryName))
		{
			this.queryId = this.getFactory().findObjectId(this.queryName);
			// 如果不需要将query放入会话中, 将sessionQueryTag设为null.
			Object tmpObj = factory.getAttribute(SESSION_QUERY_ATTRIBUTE);
			if (tmpObj != null && !BooleanConverter.toBoolean(tmpObj))
			{
				this.sessionQueryTag = null;
			}
			else
			{
				tmpObj = this.getAttribute(SESSION_QUERY_ATTRIBUTE);
				if (tmpObj != null && !BooleanConverter.toBoolean(tmpObj))
				{
					this.sessionQueryTag = null;
				}
			}
		}
		else
		{
			this.queryName = NONE_QUERY_NAME;
			this.queryId = -1;
		}
		if (this.countSearchName != null)
		{
			this.countSearchId = this.getFactory().findObjectId(this.countSearchName);
		}

		List tmpList = new ArrayList();
		Iterator itr = this.conditionProperties.iterator();
		while (itr.hasNext())
		{
			Object tmp = itr.next();
			if (tmp instanceof ConditionProperty)
			{
				tmpList.add(tmp);
			}
			else
			{
				EntityRef ref = (EntityRef) tmp;
				ConditionContainer cc = new ConditionContainer(this.getName(),
						this.conditionPropertyMap, tmpList);
				EntityImpl.addItems(factory, ref, cc);
			}
		}
		Iterator cps = tmpList.iterator();
		while (cps.hasNext())
		{
			ConditionProperty cp = (ConditionProperty) cps.next();
			cp.initialize(factory);
		}
		if (this.conditionPropertyOrder != null)
		{
			tmpList = OrderManager.doOrder(tmpList,
					this.conditionPropertyOrder, new ConditionNameHandler());
		}
		this.conditionProperties = tmpList;
		this.allConditionProperties = new ConditionProperty[tmpList.size()];
		tmpList.toArray(this.allConditionProperties);

		if (this.parameterSetting != null)
		{
			this.parameterSetting.initParameterSetting(this);
		}
		if (this.columnSetting != null)
		{
			this.columnSetting.initColumnSetting(this);
		}

		/*
		当conditionIndex为0时不设置条件, 无论是否设置了ConditionProperty
		这样在多个search共同运作时便于定义一个公共的search让其它的search继承,
		而这个search本身可能不会设置任何条件
		if (this.conditionIndex == 0)
		{
			if (this.getConditionPropertyCount() > 0)
			{
				throw new EternaException("Can't set conditionIndex 0 in a search witch has conditionProperty.");
			}
		}
		*/

		String tmpStr = (String) this.getAttribute("needSynchronize");
		if (tmpStr != null)
		{
			this.needSynchronize = "true".equalsIgnoreCase(tmpStr);
		}
		return false;
	}

	public Class getObjectType()
	{
		return SearchImpl.class;
	}

	public boolean isSingleton()
	{
		return true;
	}

	public Object create()
			throws EternaException
	{
		return this;
	}

	public EternaFactory getFactory()
	{
		return (EternaFactory) this.factory;
	}

	public void setQueryName(String queryName)
	{
		this.queryName = queryName;
	}

	public String getQueryName()
	{
		return this.queryName;
	}

	public boolean isSpecialCondition()
	{
		return this.specialCondition;
	}

	public void setSpecialCondition(boolean special)
	{
		this.specialCondition = special;
	}

	public void setCountType(String countType)
			throws EternaException
	{
		if ("auto".equals(countType))
		{
			this.countType = Query.TOTAL_COUNT_AUTO;
		}
		else if ("count".equals(countType))
		{
			this.countType = Query.TOTAL_COUNT_COUNT;
		}
		else if ("none".equals(countType))
		{
			this.countType = Query.TOTAL_COUNT_NONE;
		}
		else if (countType != null && countType.startsWith("search:"))
		{
			int index = countType.indexOf(',');
			if (index == -1)
			{
				throw new EternaException("Error count type:[" + countType + "].");
			}
			this.countSearchName = countType.substring(7, index).trim();
			this.countReaderName = countType.substring(index + 1).trim();
		}
		else
		{
			throw new EternaException("Error count type:[" + countType + "].");
		}
	}

	public boolean isNeedWrap()
	{
		return this.needWrap;
	}

	public void setNeedWrap(boolean needWrap)
	{
		this.needWrap = needWrap;
	}

	public int getConditionIndex()
	{
		return this.conditionIndex;
	}

	public void setConditionIndex(int index)
	{
		this.conditionIndex = index;
	}

	public String getColumnSettingType()
	{
		return this.columnType;
	}

	public void setColumnSettingType(String type)
	{
		this.columnType = type;
	}

	public ColumnSetting getColumnSetting()
	{
		return this.columnSetting;
	}

	public void setColumnSetting(ColumnSetting setting)
	{
		this.columnSetting = setting;
	}

	public ParameterSetting getParameterSetting()
	{
		return this.parameterSetting;
	}

	public void setParameterSetting(ParameterSetting setting)
	{
		this.parameterSetting = setting;
	}

	public String getOtherSearchManagerName()
	{
		return this.otherName;
	}

	public Search[] getOtherSearchs()
	{
		if (this.others == null)
		{
			return null;
		}
		Search[] result = new Search[this.others.length];
		System.arraycopy(this.others, 0, result, 0, this.others.length);
		return result;
	}

	public void setOtherSearchManagerName(String otherName)
	{
		this.otherName = otherName;
	}

	public String getConditionPropertyOrder()
	{
		return this.conditionPropertyOrder;
	}

	public void setConditionPropertyOrder(String order)
	{
		this.conditionPropertyOrder = order;
	}

	public void clearConditionPropertys() throws EternaException
	{
		this.allConditionProperties = null;
		this.conditionProperties.clear();
	}

	public void addConditionProperty(ConditionProperty cp) throws EternaException
	{
		this.allConditionProperties = null;
		if (this.conditionPropertyMap.containsKey(cp.getName()))
		{
			throw new EternaException(
					"Duplicate [ConditionProperty] name:" + cp.getName() + ".");
		}
		this.conditionProperties.add(cp);
		this.conditionPropertyMap.put(cp.getName(), cp);
	}

	/**
	 * 添加一个实体的引用.
	 */
	public void addEntityRef(EntityRef ref)
			throws EternaException
	{
		if (this.initialized)
		{
			throw new EternaException("You can't invoke addEntityRef after initialized.");
		}
		this.conditionProperties.add(ref);
	}

	public int getConditionPropertyCount()
			throws EternaException
	{
		return this.getConditionPropertys0().length;
	}

	public ConditionProperty getConditionProperty(int index)
			throws EternaException
	{
		ConditionProperty[] temp = this.getConditionPropertys0();
		return temp[index];
	}

	public ConditionProperty getConditionProperty(String name)
			throws EternaException
	{
		this.getConditionPropertys0();
		return (ConditionProperty) this.conditionPropertyMap.get(name);
	}

	public int getPageSize()
			throws EternaException
	{
		if (this.pageSize == -1)
		{
			int tempSize = -1;
			Object size = this.getFactory().getAttribute(PAGE_SIZE_ATTRIBUTE);
			if (size != null)
			{
				try
				{
					tempSize = Integer.parseInt((String) size);
				}
				catch (NumberFormatException ex) {}
			}
			this.pageSize = tempSize < 1 ? 10 : tempSize;
		}
		return this.pageSize;
	}

	public void setPageSize(int pageSize)
	{
		if (pageSize > 0)
		{
			this.pageSize = pageSize;
		}
	}

	public String getSearchManagerName()
	{
		return this.searchManagerName;
	}

	public void setSearchManagerName(String name)
	{
		this.searchManagerName = name;
	}

	public SearchManager getSearchManager(AppData data)
			throws EternaException
	{
		SearchManager manager = this.getSearchManager0(data);
		manager.setPageNumAndCondition(data, this);
		return manager;
	}

	public SearchResult doSearch(AppData data, Connection conn)
			throws EternaException, SQLException
	{
		if (this.needSynchronize)
		{
			synchronized (this)
			{
				return this.doSearch0(data, conn, false);
			}
		}
		return this.doSearch0(data, conn, false);
	}

	/**
	 * 执行查询, 并获得结果.
	 *
	 * @param data        AppData对象
	 * @param conn        数据库连接
	 * @param onlySearch  是否为仅执行搜索, 不进行列设置或全记录获取
	 */
	protected SearchResult doSearch0(AppData data, Connection conn, boolean onlySearch)
			throws EternaException, SQLException
	{
		if (log.isDebugEnabled())
		{
			log.debug("Start prepare query:" + System.currentTimeMillis());
		}

		Map raMap = data.getRequestAttributeMap();
		BooleanRef isFirst = new BooleanRef();
		SearchManager manager = this.getSearchManager0(data);
		Query query = getQuery(data, conn, this, isFirst, this.sessionQueryTag, manager,
				this.queryId, onlySearch ? null : this.columnSetting, onlySearch ? null : this.columnType);
		manager.setPageNumAndCondition(data, this);

		if (query == null)
		{
			log.warn("The search [" + this.getName() + "] can't execute!");
			return null;
		}
		int maxRow = manager.getPageSize(this.getPageSize());
		int pageNum = manager.getPageNum();
		int startRow = pageNum * maxRow;
		if ("1".equals(raMap.get(READ_ALL_ROW)) && !onlySearch)
		{
			startRow = 0;
			pageNum = -1;
			maxRow = -1;
		}
		else
		{
			Object start_and_count = raMap.get(READ_ROW_START_AND_COUNT);
			if (start_and_count != null & start_and_count instanceof StartAndCount)
			{
				StartAndCount temp = (StartAndCount) start_and_count;
				maxRow = temp.count;
				if (temp.start >= 0)
				{
					startRow = temp.start - 1;
					pageNum = -1;
				}
				else
				{
					startRow = pageNum * maxRow;
				}
			}
		}
		query.setMaxCount(maxRow);
		query.setStartRow(startRow + 1);
		dealOthers(data, conn, this.others, query, isFirst.value);
		if (this.conditionIndex > 0)
		{
			if (this.specialCondition)
			{
				String subConSQL = manager.getSpecialConditionPart(this, this.needWrap);
				PreparerManager spm = manager.getSpecialPreparerManager(this);
				query.setSubScript(this.conditionIndex, subConSQL, spm);
			}
			else
			{
				query.setSubScript(this.conditionIndex, manager.getConditionPart(this.needWrap),
						manager.getPreparerManager());
			}
		}
		if (this.parameterSetting != null)
		{
			this.parameterSetting.setParameter(query, this, isFirst.value, data, conn);
		}
		String orderConfig = null;
		if (query.canOrder())
		{
			orderConfig = manager.getOrderConfig(data);
			if (orderConfig != null)
			{
				query.setMultipleOrder(StringTool.separateString(orderConfig, ",", true));
			}
			else
			{
				orderConfig = query.getOrderConfig();
			}
		}

		if (log.isDebugEnabled())
		{
			log.debug("Search SQL:" + query.getPreparedScript());
			log.debug("End prepare query:" + System.currentTimeMillis());
		}
		//System.out.println("Search SQL:" + query.getPreparedSQL());
		ResultIterator countRitr = null;
		ResultIterator ritr;
		if ("1".equals(raMap.get(HOLD_CONNECTION)) && !onlySearch)
		{
			ritr = query.executeQueryHoldConnection(conn);
		}
		else
		{
			if (this.countSearchId != -1)
			{
				Search tmpSearch = this.getFactory().createSearch(this.countSearchId);
				Object oldObj = raMap.get(READ_ROW_START_AND_COUNT);
				raMap.put(READ_ROW_START_AND_COUNT, new StartAndCount(1, 1));
				countRitr = tmpSearch.doSearch(data, conn).queryResult;
				int count = countRitr.nextRow().getInt(this.countReaderName);
				query.setTotalCount(count);
				countRitr.beforeFirst();
				if (oldObj == null)
				{
					raMap.remove(READ_ROW_START_AND_COUNT);
				}
				else
				{
					raMap.put(READ_ROW_START_AND_COUNT, oldObj);
				}
			}
			else if (this.countType != 0)
			{
				query.setTotalCount(this.countType);
			}
			ritr = query.executeQuery(conn);
		}
		SearchResult result = new SearchResult(this.name, this.queryName, ritr,
				countRitr, maxRow, pageNum, orderConfig, manager.getAttributes());
		if (log.isDebugEnabled())
		{
			log.debug("End execute query:" + System.currentTimeMillis());
		}
		return result;
	}

	/**
	 * 通过其他的辅助search来设置条件.
	 *
	 * @param data      AppData对象
	 * @param conn      数据库连接
	 * @param others    其他的辅助search
	 * @param query     用于执行查询的query对象
	 * @param first     是否为第一次执行
	 */
	protected static void dealOthers(AppData data, Connection conn, Search[] others,
			Query query, boolean first)
			throws EternaException
	{
		if (others == null)
		{
			return;
		}
		for (int i = 0; i < others.length; i++)
		{
			Search other = others[i];
			if (other.getConditionIndex() > 0)
			{
				SearchManager om = other.getSearchManager(data);
				if (other.isSpecialCondition())
				{
					String subConSQL = om.getSpecialConditionPart(other, other.isNeedWrap());
					PreparerManager spm = om.getSpecialPreparerManager(other);
					query.setSubScript(other.getConditionIndex(), subConSQL, spm);
				}
				else
				{
					query.setSubScript(other.getConditionIndex(), om.getConditionPart(other.isNeedWrap()),
							om.getPreparerManager());
				}
			}
			ParameterSetting ps = other.getParameterSetting();
			if (ps != null)
			{
				ps.setParameter(query, other, first, data, conn);
			}
		}
	}


	/**
	 * 获得一个用于执行查询的query对象.
	 *
	 * @param data              AppData对象
	 * @param conn              数据库连接, 在获取列设置时会使用到
	 * @param search            当前的search对象
	 * @param first             出参, 是否为第一次执行, 第一次进入或重新设置了条件时, 值为true
	 * @param sessionQueryTag   query放在session中使用的名称
	 * @param searchManager     搜索的管理器, 用于控制分页及查询条件
	 * @param queryIndex        用于获取查询的索引值
	 * @param columnSetting     用于进行列设置的对象
	 * @param columnType        列设置的类型, 用于区分读取哪个列设置
	 */
	protected static Query getQuery(AppData data, Connection conn, Search search, BooleanRef first,
			String sessionQueryTag, SearchManager searchManager, int queryIndex,
			ColumnSetting columnSetting, String columnType)
			throws EternaException
	{
		if (queryIndex == -1)
		{
			return null;
		}
		Query query = null;
		Map raMap = data.getRequestAttributeMap();
		boolean isFirst;
		if (sessionQueryTag != null)
		{
			Map saMap = data.getSessionAttributeMap();
			Map queryMap = (Map) SessionCache.getInstance().getProperty(saMap, SESSION_SEARCH_QUERY);
			if (queryMap == null)
			{
				queryMap = new HashMap();
				SessionCache.getInstance().setProperty(saMap, SESSION_SEARCH_QUERY, queryMap);
			}
			QueryContainer qc = (QueryContainer) queryMap.get(sessionQueryTag);
			int qcVersion;
			if (qc == null)
			{
				qcVersion = 0;
			}
			else
			{
				qcVersion = qc.conditionVersion;
			}
			isFirst = searchManager.hasQueryType(data) || searchManager.getConditionVersion() > qcVersion;
			qcVersion = searchManager.getConditionVersion();
			if (isFirst || qc == null)
			{
				isFirst = true;
				query = createQuery0(data, search, queryIndex);
				queryMap.put(sessionQueryTag, new QueryContainer(query, qcVersion));
			}
			else
			{
				query = qc.query;
			}
		}
		else
		{
			isFirst = true;
			query = createQuery0(data, search, queryIndex);
		}

		String[] columns = (String[]) raMap.get(COLUMN_SETTING);
		if (columns == null && columnSetting != null)
		{
			columns = columnSetting.getColumnSetting(columnType, query, search, isFirst, data, conn);
		}
		if (columns != null)
		{
			ResultReaderManager readerManager = query.getReaderManager();
			readerManager.setReaderList(columns);
			query.setReaderManager(readerManager);
		}
		first.value = isFirst;
		return query;
	}
	private static Query createQuery0(AppData data, Search search, int queryIndex)
	{
		EternaFactory f = search.getFactory();
		Query r = f.createQuery(queryIndex);
		UserManager um = f.getUserManager();
		if (um != null)
		{
			User user = um.getUser(data);
			if (user != null)
			{
				r.setPermission(user.getPermission());
			}
			else
			{
				r.setPermission(EmptyPermission.getInstance());
			}
		}
		return r;
	}

	private SearchManager getSearchManager0(AppData data)
			throws EternaException
	{
		if ("1".equals(data.getRequestAttributeMap().get(SearchManager.NEW_SEARCH_MANAGER)))
		{
			return this.getFactory().createSearchManager();
		}
		Map saMap = data.getSessionAttributeMap();
		Map managerMap = (Map) SessionCache.getInstance().getProperty(saMap, SESSION_SEARCH_MANAGER);
		if (managerMap == null)
		{
			managerMap = new HashMap();
			SessionCache.getInstance().setProperty(saMap, SESSION_SEARCH_MANAGER, managerMap);
		}
		SearchManager manager = (SearchManager) managerMap.get(this.searchManagerName);
		if (manager == null)
		{
			manager = this.getFactory().createSearchManager();
			managerMap.put(this.searchManagerName, manager);
		}
		return manager;
	}

	private ConditionProperty[] getConditionPropertys0()
			throws EternaException
	{
		return this.allConditionProperties;
	}

	/**
	 * 销毁search.
	 */
	public void destroy()
	{
	}

	/**
	 * 将一个实体元素转换成condition对象.
	 *
	 * @param item        需要转换的实体元素
	 * @param tableAlias  数据库表的别名
	 */
	public static ConditionProperty item2Condition(EntityItem item, String tableAlias)
	{
		ConditionPropertyImpl condition = new ConditionPropertyImpl();
		if (item.getCaption() != null)
		{
			condition.columnCaption = item.getCaption();
		}
		condition.name = item.getName();
		condition.columnType = item.getType();
		String colName = item.getColumnName();
		if (tableAlias != null)
		{
			colName = tableAlias.concat(".").concat(colName);
		}
		condition.columnName = colName;
		condition.inputType = "text";
		condition.attributes = new AttributeManager();
		String[] attrNames = item.getAttributeNames();
		boolean hasInput = false;
		boolean hasPrepare = false;
		for (int i = 0; i < attrNames.length; i++)
		{
			String n = attrNames[i];
			if (ParameterGroup.PREPARE_FLAG.equals(n))
			{
				hasPrepare = true;
				condition.prepareName = (String) item.getAttribute(n);
			}
			else if (Tool.PATTERN_FLAG.equals(n))
			{
				if (!hasPrepare)
				{
					condition.prepareName = Tool.PATTERN_PREFIX.concat((String) item.getAttribute(n));
				}
			}
			else if (INPUT_TYPE_FLAG.equals(n))
			{
				hasInput = true;
				condition.inputType = (String) item.getAttribute(n);
			}
			else if (Component.TYPICAL_COMPONENT_FLAG.equals(n))
			{
				if (!hasInput)
				{
					condition.inputType = (String) item.getAttribute(n);
				}
			}
			else if (DEFAULT_BUILDER_FLAG.equals(n) || BUILDER_FLAG.equals(n))
			{
				condition.defaultBuilderName = (String) item.getAttribute(n);
			}
			else if (BUILDER_LIST_FLAG.equals(n))
			{
				condition.listName = (String) item.getAttribute(n);
			}
			else
			{
				condition.attributes.setAttribute(n, item.getAttribute(n));
			}
		}
		if (condition.listName == null)
		{
			condition.listName = PropertyGenerator.getListName(
					condition.inputType, item.getType());
		}
		return condition;
	}

}

class QueryContainer
{
	public final Query query;
	public final int conditionVersion;

	public QueryContainer(Query query, int conditionVersion)
	{
		this.query = query;
		this.conditionVersion = conditionVersion;
	}

}

/**
 * 处理EntityRef的容器.
 */
class ConditionContainer
		implements EntityImpl.Container
{
	public ConditionContainer(String name, Map nameCache, List itemList)
	{
		this.name = name;
		this.nameCache = nameCache;
		this.itemList = itemList;
	}
	private final Map nameCache;
	private final List itemList;
	private final String name;

	public String getName()
	{
		return this.name;
	}

	public String getType()
	{
		return "Search";
	}

	public boolean contains(String name)
	{
		return this.nameCache.containsKey(name);
	}

	public void add(EntityItem item, String tableAlias)
	{
		ConditionProperty condition = SearchImpl.item2Condition(item, tableAlias);
		this.nameCache.put(item.getName(), condition);
		this.itemList.add(condition);
	}

}

class ConditionNameHandler
		implements OrderManager.NameHandler
{
	public String getName(Object obj)
	{
		return ((ConditionProperty) obj).getName();
	}

}