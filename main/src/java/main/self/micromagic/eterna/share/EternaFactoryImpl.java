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

package self.micromagic.eterna.share;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;

import self.micromagic.eterna.dao.Constant;
import self.micromagic.eterna.dao.DaoLogger;
import self.micromagic.eterna.dao.Entity;
import self.micromagic.eterna.dao.Query;
import self.micromagic.eterna.dao.ResultFormat;
import self.micromagic.eterna.dao.Update;
import self.micromagic.eterna.dao.preparer.CreaterManager;
import self.micromagic.eterna.dao.preparer.PreparerCreater;
import self.micromagic.eterna.dao.preparer.ValuePreparer;
import self.micromagic.eterna.digester2.ContainerManager;
import self.micromagic.eterna.digester2.ParseException;
import self.micromagic.eterna.model.Model;
import self.micromagic.eterna.model.ModelCaller;
import self.micromagic.eterna.model.ModelExport;
import self.micromagic.eterna.model.impl.ModelCallerImpl;
import self.micromagic.eterna.search.ConditionBuilder;
import self.micromagic.eterna.search.Search;
import self.micromagic.eterna.search.SearchAttributes;
import self.micromagic.eterna.search.SearchManager;
import self.micromagic.eterna.search.SearchManagerGenerator;
import self.micromagic.eterna.search.impl.SearchManagerImpl;
import self.micromagic.eterna.security.PermissionSet;
import self.micromagic.eterna.security.PermissionSetGenerator;
import self.micromagic.eterna.security.PermissionSetImpl;
import self.micromagic.eterna.security.UserManager;
import self.micromagic.eterna.view.Component;
import self.micromagic.eterna.view.DataPrinter;
import self.micromagic.eterna.view.Function;
import self.micromagic.eterna.view.Resource;
import self.micromagic.eterna.view.StringCoder;
import self.micromagic.eterna.view.View;
import self.micromagic.util.Utility;
import self.micromagic.util.converter.IntegerConverter;

public class EternaFactoryImpl extends AbstractFactory
		implements EternaFactory
{
	// 日志
	protected static final Log log = Tool.log;

	/**
	 * 初始化标志-未初始化.
	 */
	protected static final int INIT_FLAG_NONE = 0;
	/**
	 * 初始化标志-正在初始化.
	 */
	protected static final int INIT_FLAG_RUNNING = 1;
	/**
	 * 初始化标志-初始化完成.
	 */
	protected static final int INIT_FLAG_FINISH = 3;

	// 初始化标志
	private int initFlag;

	private EternaFactory shareEternaFactory;
	private UserManager userManager;
	private DataSourceManager dataSourceManager;


	//----------------------------------  初始化及公共  --------------------------------------

	public EternaFactory getShareFactory()
	{
		return this.shareEternaFactory;
	}

	public UserManager getUserManager()
			throws EternaException
	{
		if (this.userManager == null && this.shareEternaFactory != null)
		{
			return this.shareEternaFactory.getUserManager();
		}
		return this.userManager;
	}

	public void setUserManager(UserManager um)
			throws EternaException
	{
		if (this.userManager != null)
		{
			if (ContainerManager.getSuperInitLevel() == 0)
			{
				log.warn("Duplicate UserManager.");
			}
		}
		else if (um != null)
		{
			if (this.initFlag > INIT_FLAG_NONE)
			{
				um.initUserManager(this);
			}
			this.userManager = um;
		}
	}

	public PermissionSet createPermissionSet(String permission)
			throws EternaException
	{
		if (this.permissionSetGenerator != null)
		{
			return this.permissionSetGenerator.createPermissionSet(permission, this);
		}
		throw new EternaException("The permission config hasn't initialized.");
	}

	/**
	 * 初始化权限的配置.
	 */
	private void initPermissionConfig()
	{
		if (this.isObjectExists(PERMISSION_SET_GENERATOR_NAME, false))
		{
			this.permissionSetGenerator = (PermissionSetGenerator) this.createObject(
					PERMISSION_SET_GENERATOR_NAME);
			return;
		}
		if (this.shareEternaFactory instanceof EternaFactoryImpl)
		{
			// 如果本工厂没有设置搜索配置, 则直接使用共享工厂的
			EternaFactoryImpl tmp = (EternaFactoryImpl) this.shareEternaFactory;
			this.searchManagerGenerator = tmp.searchManagerGenerator;
		}
		else
		{
			PermissionSetImpl tmp = new PermissionSetImpl();
			tmp.initialize(this);
			this.permissionSetGenerator = tmp;
		}
	}
	private PermissionSetGenerator permissionSetGenerator;

	public DataSourceManager getDataSourceManager()
			throws EternaException
	{
		if (this.dataSourceManager == null && this.shareEternaFactory != null)
		{
			return this.shareEternaFactory.getDataSourceManager();
		}
		return this.dataSourceManager;
	}

	public void setDataSourceManager(DataSourceManager dsm)
			throws EternaException
	{
		if (this.dataSourceManager != null)
		{
			if (ContainerManager.getSuperInitLevel() == 0)
			{
				log.warn("Duplicate DataSourceManager.");
			}
		}
		else if (dsm != null)
		{
			if (this.initFlag > INIT_FLAG_NONE)
			{
				dsm.initialize(this);
			}
			this.dataSourceManager = dsm;
		}
	}

	public String[] getAttributeNames()
			throws EternaException
	{
		String[] tmpP = this.shareFactory != null ? this.shareFactory.getAttributeNames() : null;
		String[] tmpThis = super.getAttributeNames();
		if (tmpP != null && tmpP.length > 0 && tmpThis != null && tmpThis.length > 0)
		{
			HashSet set = new HashSet(tmpP.length + tmpThis.length);
			set.addAll(Arrays.asList(tmpP));
			set.addAll(Arrays.asList(tmpThis));
			return (String[]) set.toArray(new String[set.size()]);
		}
		return tmpP == null || tmpP.length == 0 ? tmpThis : tmpP;
	}

	public Object getAttribute(String name)
			throws EternaException
	{
		Object tmp = super.getAttribute(name);
		if (tmp == null && this.shareFactory != null)
		{
			tmp = this.shareFactory.getAttribute(name);
		}
		return tmp;
	}

	public Object setAttribute(String name, Object value)
			throws EternaException
	{
		if (this.initFlag == INIT_FLAG_NONE)
		{
			// 未初始化完成时, 需要检查工厂属性是否被重复设置了
			if (super.hasAttribute(name))
			{
				if (ContainerManager.getSuperInitLevel() == 0)
				{
					log.warn("Duplicate factory attribute [" + name + "].");
				}
				return null;
			}
		}
		return super.setAttribute(name, value);
	}

	public boolean initialize(FactoryContainer factoryContainer, Factory shareFactory)
			throws EternaException
	{
		if (this.initFlag == INIT_FLAG_NONE)
		{
			this.initFlag = INIT_FLAG_RUNNING;
			try
			{
				this.init0(factoryContainer, shareFactory);
				return true;
			}
			finally
			{
				this.initFlag = INIT_FLAG_FINISH;
			}
		}
		return false;
	}

	private void init0(FactoryContainer factoryContainer, Factory shareFactory)
	{
		super.initialize(factoryContainer, shareFactory);
		ParseException.setContextInfo(this.getFactoryContainer().getId(), "", "");
		if (shareFactory instanceof EternaFactory)
		{
			this.shareEternaFactory = (EternaFactory) shareFactory;
		}

		// 初始化, attributes
		//this.putSuperAttributes();

		// 注册bean类, 这里只需要获得当前的属性, share的会在自己的工厂中注册
		String beans = (String) super.getAttribute(Tool.BEAN_CLASS_NAMES);
		if (beans != null)
		{
			Tool.registerBean(beans);
		}

		// 初始化, dataSourceManager
		if (this.dataSourceManager != null)
		{
			ParseException.setContextInfo(null, "dataSourceManager", "");
			this.dataSourceManager.initialize(this);
		}
		else
		{
			this.dataSourceManager = this.getDataSourceFromCache();
			if (this.dataSourceManager != null)
			{
				ParseException.setContextInfo(null, "dataSourceManager", "");
				this.dataSourceManager.initialize(this);
			}
		}

		// 初始化, userManager
		if (this.userManager != null)
		{
			ParseException.setContextInfo(null, "userManager", "");
			this.userManager.initUserManager(this);
		}

		// 初始化各个配置
		this.initPermissionConfig();
		this.initDaoLoggerConfig();
		this.initSearchConfig();
		this.initModelConfig();

		// 初始化注册的对象
		int size = this.objectList.size();
		ObjectContainer container;
		// 这里不使用迭代方式, 可以在初始化时继续添加对象
		for (int i = 0; i < size; i++)
		{
			container = (ObjectContainer) this.objectList.get(i);
			if (container == null)
			{
				continue;
			}
			String objName = container.getName() + "(" + container.getType().getName() + ")";
			ParseException.setContextInfo(null, objName, "");
			container.initialize(this);
		}
		// 新添加的对象不需要再初始化, 因为在添加时已经初始化了

		// 重新构造列表, 去除对象列表中的多余空间
		Object leaveSizeObj = factoryContainer.getAttribute(FactoryContainer.LEAVE_SIZE_FLAG);
		int leaveSize = 2;
		if (leaveSizeObj != null)
		{
			leaveSize = IntegerConverter.toInt(leaveSizeObj);
		}
		List temp = new ArrayList(this.objectList.size() + leaveSize);
		temp.addAll(this.objectList);
		this.objectList = temp;
	}

	public DataSourceManager getDataSourceFromCache()
			throws EternaException
	{
		Map dsMap = (Map) this.getAttribute(DataSourceManager.DATA_SOURCE_MAP);
		if (dsMap != null && dsMap.size() > 0)
		{
			DataSourceManagerImpl dsmi = new DataSourceManagerImpl();
			String defaultDataSourceName = (String) this.getAttribute(
					DataSourceManager.DEFAULT_DATA_SOURCE_NAME);
			if (defaultDataSourceName == null)
			{
				Map.Entry entry = (Map.Entry) dsMap.entrySet().iterator().next();
				defaultDataSourceName = (String) entry.getKey();
			}
			dsmi.setDefaultDataSourceName(defaultDataSourceName);
			Iterator itr = dsMap.entrySet().iterator();
			while (itr.hasNext())
			{
				Map.Entry entry = (Map.Entry) itr.next();
				String dsName = (String) entry.getKey();
				dsmi.addDataSource(dsName, (DataSource) entry.getValue());
			}
			return dsmi;
		}
		return null;
	}

	public void destroy()
	{
		int size = this.objectList.size();
		ObjectContainer container;
		for (int i = 0; i < size; i++)
		{
			container = (ObjectContainer) this.objectList.get(i);
			if (container != null)
			{
				container.destroy();
			}
		}
		this.objectList.clear();
		this.objectMap.clear();
	}


	//----------------------------------  dao  --------------------------------------

	/**
	 * 初始化日志记录器的配置.
	 */
	private void initDaoLoggerConfig()
	{
		int pCount = 0;
		String[] names = this.getObjectNames(DaoLogger.class);
		if (this.shareEternaFactory != null)
		{
			pCount = this.shareEternaFactory.getDaoLoggerCount();
		}
		// 如果本工厂没有设置日志, 则直接使用共享工厂的
		if (names.length > 0)
		{
			ArrayList tmpArr = new ArrayList();
			Map daoLoggerIndexMap = new HashMap(2);
			for (int i = 0; i < pCount; i++)
			{
				DaoLogger l = this.shareEternaFactory.getDaoLogger(i);
				daoLoggerIndexMap.put(l.getName(), Utility.createInteger(i));
				tmpArr.add(l);
			}
			for (int i = 0; i < names.length; i++)
			{
				Integer oldI = (Integer) daoLoggerIndexMap.get(names[i]);
				if (oldI != null)
				{
					tmpArr.set(oldI.intValue(), this.createObject(names[i]));
				}
				else
				{
					int tmpI = tmpArr.size();
					tmpArr.add(this.createObject(names[i]));
					daoLoggerIndexMap.put(names[i], Utility.createInteger(tmpI));
				}
			}
			int tmpCount = tmpArr.size();
			if (tmpCount > MAX_DAO_LOGGER_COUNT)
			{
				throw new EternaException("Too many dao logger [" + tmpCount + "].");
			}
			DaoLogger[] daoLoggers = new DaoLogger[tmpCount];
			tmpArr.toArray(daoLoggers);
			this.daoLoggerConfig = new DaoLoggerConfig(daoLoggerIndexMap, daoLoggers);
		}
		else if (this.shareEternaFactory instanceof EternaFactoryImpl)
		{
			EternaFactoryImpl tmp = (EternaFactoryImpl) this.shareEternaFactory;
			this.daoLoggerConfig = tmp.daoLoggerConfig;
		}
	}
	private DaoLoggerConfig daoLoggerConfig;

	public DaoLogger getDaoLogger(int index)
			throws EternaException
	{
		if (this.daoLoggerConfig == null && this.shareEternaFactory != null)
		{
			return this.shareEternaFactory.getDaoLogger(index);
		}
		if (index < 0 || index >= this.getDaoLoggerCount())
		{
			String msg = "Error dao logger index [" + index
					+ "], logger count is [" + this.getDaoLoggerCount() + "].";
			throw new EternaException(msg);
		}
		return this.daoLoggerConfig.daoLoggers[index];
	}

	public int getDaoLoggerIndex(String name)
	{
		if (this.daoLoggerConfig == null)
		{
			if (this.shareEternaFactory == null)
			{
				return -1;
			}
			return this.shareEternaFactory.getDaoLoggerIndex(name);
		}
		return this.daoLoggerConfig.getDaoLoggerIndex(name);
	}

	public int getDaoLoggerCount()
	{
		if (this.daoLoggerConfig == null)
		{
			if (this.shareEternaFactory == null)
			{
				return 0;
			}
			return this.shareEternaFactory.getDaoLoggerCount();
		}
		return this.daoLoggerConfig.daoLoggers.length;
	}

	public String getConstantValue(String name)
			throws EternaException
	{
		Constant constant = (Constant) this.createObject(name);
		return constant.getValue();
	}

	public Entity getEntity(String name)
			throws EternaException
	{
		return (Entity) this.createObject(name);
	}

	public ResultFormat getFormat(String name)
			throws EternaException
	{
		return (ResultFormat) this.createObject(name);
	}

	public Query createQuery(String name)
			throws EternaException
	{
		return (Query) this.createObject(name);
	}

	public Query createQuery(int id)
			throws EternaException
	{
		return (Query) this.createObject(id);
	}

	public Update createUpdate(String name)
			throws EternaException
	{
		return (Update) this.createObject(name);
	}

	public Update createUpdate(int id)
			throws EternaException
	{
		return (Update) this.createObject(id);
	}

	public PreparerCreater getPrepare(String name)
			throws EternaException
	{
		return (PreparerCreater) this.createObject(name);
	}

	public ValuePreparer createValuePreparer(int type, Object value)
			throws EternaException
	{
		PreparerCreater pc = CreaterManager.createPreparerCreater(type, null, this);
		return pc.createPreparer(value);
	}

	//----------------------------------  search  --------------------------------------

	/**
	 * 初始化搜索的配置.
	 */
	private void initSearchConfig()
	{
		if (this.isObjectExists(SEARCH_MANAGER_GENERATOR_NAME, false))
		{
			this.searchManagerGenerator = (SearchManagerGenerator) this.createObject(
					SEARCH_MANAGER_GENERATOR_NAME);
			return;
		}
		String attrs = (String) super.getAttribute(SEARCH_ATTRIBUTES_FLAG);
		if (attrs == null && this.shareEternaFactory instanceof EternaFactoryImpl)
		{
			// 如果本工厂没有设置搜索配置, 则直接使用共享工厂的
			EternaFactoryImpl tmp = (EternaFactoryImpl) this.shareEternaFactory;
			this.searchManagerGenerator = tmp.searchManagerGenerator;
		}
		else
		{
			SearchManagerImpl tmp = new SearchManagerImpl();
			tmp.setFactory(this);
			tmp.initialize(this);
			this.searchManagerGenerator = tmp;
		}
	}
	private SearchManagerGenerator searchManagerGenerator;

	public ConditionBuilder getConditionBuilder(String name)
			throws EternaException
	{
		return (ConditionBuilder) this.createObject(name);
	}

	public List getConditionBuilderList(String name)
			throws EternaException
	{
		return (List) this.createObject(name);
	}

	public Search createSearch(String name)
			throws EternaException
	{
		return (Search) this.createObject(name);
	}

	public Search createSearch(int id)
			throws EternaException
	{
		return (Search) this.createObject(id);
	}

	public SearchManager createSearchManager()
			throws EternaException
	{
		if (this.searchManagerGenerator != null)
		{
			return this.searchManagerGenerator.createSearchManager(this);
		}
		throw new EternaException("The search config hasn't initialized.");
	}

	public SearchAttributes getSearchAttributes()
			throws EternaException
	{
		if (this.searchManagerGenerator != null)
		{
			return this.searchManagerGenerator.getSearchAttributes();
		}
		if (this.shareEternaFactory != null)
		{
			return this.shareEternaFactory.getSearchAttributes();
		}
		throw new EternaException("The search config hasn't initialized.");
	}


	//----------------------------------  model  --------------------------------------

	/**
	 * 初始化模块的配置.
	 */
	private void initModelConfig()
	{
		if (this.isObjectExists(MODEL_CALLER_NAME, false))
		{
			this.modelCaller = (ModelCaller) this.createObject(MODEL_CALLER_NAME);
		}
		else if (this.shareEternaFactory == null || this.getObjectNames(Model.class).length > 0)
		{
			// 存在模块对象, 则不能使用共享工厂中的
			ParseException.setContextInfo(null, MODEL_CALLER_NAME, "");
			ModelCallerImpl tmp = new ModelCallerImpl();
			tmp.setName(MODEL_CALLER_NAME);
			tmp.setFactory(this);
			tmp.initialize(this);
			this.modelCaller = tmp;
		}
		else if (this.shareEternaFactory instanceof EternaFactoryImpl)
		{
			EternaFactoryImpl tmp = (EternaFactoryImpl) this.shareEternaFactory;
			this.modelCaller = tmp.modelCaller;
		}
	}
	private ModelCaller modelCaller;

	public String getModelNameTag()
			throws EternaException
	{
		return this.getModelCaller().getModelNameTag();
	}

	public ModelCaller getModelCaller()
	{
		if (this.modelCaller != null)
		{
			return this.modelCaller;
		}
		if (this.shareEternaFactory != null)
		{
			return this.shareEternaFactory.getModelCaller();
		}
		throw new EternaException("The model config hasn't initialized.");
	}

	public ModelExport getModelExport(String exportName)
			throws EternaException
	{
		return (ModelExport) this.createObject(exportName);
	}

	public Model createModel(String name)
			throws EternaException
	{
		return (Model) this.createObject(name);
	}

	public Model createModel(int id)
			throws EternaException
	{
		return (Model) this.createObject(id);
	}


	//----------------------------------  view  --------------------------------------

	private String viewGlobalSetting;

	public String getViewGlobalSetting() throws EternaException
	{
		if (this.viewGlobalSetting == null)
		{
			this.viewGlobalSetting = (String) this.getAttribute(VIEW_GLOBAL_SETTING_FLAG);
			if (this.viewGlobalSetting == null)
			{
				this.viewGlobalSetting = "";
			}
		}
		return this.viewGlobalSetting;
	}

	public DataPrinter getDataPrinter(String name)
			throws EternaException
	{
		return (DataPrinter) this.createObject(name);
	}

	public StringCoder getStringCoder(String name)
	{
		return (StringCoder) this.createObject(name);
	}

	public Function getFunction(String name)
			throws EternaException
	{
		return (Function) this.createObject(name);
	}

	public Component getTypicalComponent(String name)
			throws EternaException
	{
		return (Component) this.createObject(name);
	}

	public View createView(String name)
			throws EternaException
	{
		return (View) this.createObject(name);
	}

	public View createView(int id)
			throws EternaException
	{
		return (View) this.createObject(id);
	}

	public Resource getResource(String name)
			throws EternaException
	{
		return (Resource) this.createObject(name);
	}


	//----------------------  object register & deregister ...  ------------------------

	public void registerObject(Object obj)
			throws EternaException
	{
		if (obj == null)
		{
			throw new NullPointerException("Param obj is null.");
		}
		int id = this.findEmptyPosition();
		if (id >= Factory.MAX_OBJECT_COUNT)
		{
			String msg = "Max object count:" + id
					+ "," + Factory.MAX_OBJECT_COUNT + ".";
			throw new ParseException(msg);
		}
		ObjectContainer container;
		if (obj instanceof EternaCreater)
		{
			container = new EternaCreaterCon(id, (EternaCreater) obj);
		}
		else if (obj instanceof EternaObject)
		{
			container = new EternaObjectCon(id, (EternaObject) obj);
		}
		else
		{
			String msg = "The param obj must be "
					+ "ObjectCreater or EternaObject, but it is ["
					+ obj.getClass().getName() + "].";
			throw new ParseException(msg);
		}
		this.registerObject0(id, container);
	}
	public void registerObject(String name, Object obj)
			throws EternaException
	{
		if (obj == null)
		{
			throw new NullPointerException("Param obj is null.");
		}
		int id = this.findEmptyPosition();
		if (id >= Factory.MAX_OBJECT_COUNT)
		{
			String msg = "Max object count:" + id
					+ "," + Factory.MAX_OBJECT_COUNT + ".";
			throw new ParseException(msg);
		}
		ObjectContainer container = new NormalObjectCon(id, name, obj);
		this.registerObject0(id, container);
	}
	private void registerObject0(int id, ObjectContainer container)
	{
		String name = container.getName();
		if (this.objectMap.containsKey(name))
		{
			throw new ParseException("Duplicate object name [" + name + "].");
		}
		if (id == this.objectList.size())
		{
			this.objectList.add(container);
		}
		else
		{
			this.objectList.set(id, container);
		}
		this.objectMap.put(name, container);
		if (this.initFlag > INIT_FLAG_NONE)
		{
			initObject(true, this, container);
		}
	}
	private int findEmptyPosition()
	{
		if (this.hasEmptyPosition)
		{
			return this.objectList.size();
		}
		int count = this.objectList.size();
		for (int i = 0; i < count; i++)
		{
			if (this.objectList.get(i) == null)
			{
				return i;
			}
		}
		this.hasEmptyPosition = false;
		return this.objectList.size();
	}

	/**
	 * 初始化一个对象.
	 *
	 * @param newEnv     是否需要构造新的环境信息
	 * @param factory    对象所属的工厂
	 * @param container  对象所在的容器
	 */
	static void initObject(boolean newEnv, EternaFactory factory, ObjectContainer container)
	{
		if (newEnv)
		{
			String tmpName = factory.getFactoryContainer().getId() + "," + container.getName()
					+ "(" + container.getType().getName() + ")";
			Object old = ParseException.changeContextInfo(tmpName);
			try
			{
				container.initialize(factory);
			}
			finally
			{
				ParseException.changeContextInfo(old);
			}
		}
		else
		{
			container.initialize(factory);
		}
	}

	/**
	 * 注销一个已注册的对象.
	 */
	public void deregisterObject(Object key)
			throws EternaException
	{
		ObjectContainer container = (ObjectContainer) this.objectMap.get(key);
		if (container == null)
		{
			throw new ParseException("Not found object [" + name + "].");
		}
		this.objectList.set(container.getId(), null);
		this.objectMap.remove(key);
		container.destroy();
		this.hasEmptyPosition = true;
	}

	/**
	 * 查询已注册的对象的编号.
	 */
	public int findObjectId(Object key)
	{
		ObjectContainer container = (ObjectContainer) this.objectMap.get(key);
		if (container == null)
		{
			if (this.shareEternaFactory != null)
			{
				return this.shareEternaFactory.findObjectId(key) + Factory.MAX_OBJECT_COUNT;
			}
			throw new ParseException("Not found object [" + key + "].");
		}
		return container.getId();
	}

	/**
	 * 根据给出的编号创建对象.
	 */
	public Object createObject(int id)
			throws EternaException
	{
		if (id < 0 || id >= this.objectList.size())
		{
			if (this.shareEternaFactory != null && id >= Factory.MAX_OBJECT_COUNT)
			{
				return this.shareEternaFactory.createObject(id - Factory.MAX_OBJECT_COUNT);
			}
			throw new EternaException("Not found object id:" + id + ".");
		}
		ObjectContainer container = (ObjectContainer) this.objectList.get(id);
		if (container == null)
		{
			throw new EternaException("Not found object id:" + id + ".");
		}
		return container.create(this.initFlag == INIT_FLAG_RUNNING, this);
	}

	public boolean isObjectExists(Object key, boolean checkOther)
	{
		if (checkOther && key instanceof String)
		{
			Object obj = this.createExtObject((String) key);
			if (obj != null)
			{
				return true;
			}
		}
		ObjectContainer container = (ObjectContainer) this.objectMap.get(key);
		if (container == null)
		{
			if (checkOther && this.shareFactory != null)
			{
				return this.shareFactory.isObjectExists(key, checkOther);
			}
			return false;
		}
		return true;
	}

	public boolean isSingleton(Object key)
			throws EternaException
	{
		if (key instanceof String)
		{
			String n = (String) key;
			int index = n.indexOf(':');
			if (index != -1)
			{
				String fName = FactoryContainer.EXT_PREFIX.concat(n.substring(0, index));
				ExtObjectFinder finder = getExtFinder(this, fName);
				if (finder != null)
				{
					Boolean b = finder.isSingleton(n.substring(index + 1));
					if (b != null)
					{
						return b.booleanValue();
					}
				}
			}
		}
		ObjectContainer container = (ObjectContainer) this.objectMap.get(key);
		if (container == null)
		{
			if (this.shareFactory != null)
			{
				return this.shareFactory.isSingleton(key);
			}
			throw new ParseException("Not found object [" + key + "].");
		}
		return container.isSingleton();
	}

	public Object createObject(Object key)
			throws EternaException
	{
		if (key instanceof String)
		{
			Object obj = this.createExtObject((String) key);
			if (obj != null)
			{
				return obj;
			}
		}
		ObjectContainer container = (ObjectContainer) this.objectMap.get(key);
		if (container == null)
		{
			if (this.shareFactory != null)
			{
				return this.shareFactory.createObject(key);
			}
			throw new ParseException("Not found object [" + key + "].");
		}
		return container.create(this.initFlag == INIT_FLAG_RUNNING, this);
	}

	/**
	 * 创建一个扩展对象.
	 */
	private Object createExtObject(String key)
	{
		String n = key;
		int index = n.indexOf(':');
		if (index != -1)
		{
			String fName = FactoryContainer.EXT_PREFIX.concat(n.substring(0, index));
			ExtObjectFinder finder = getExtFinder(this, fName);
			if (finder != null)
			{
				Object obj = finder.findObject(n.substring(index + 1));
				if (obj != null)
				{
					return obj;
				}
			}
		}
		return null;
	}

	/**
	 * 从给出的工厂中获取扩展对象查找者.
	 * 此方法会递归共享工厂来获取.
	 */
	private static ExtObjectFinder getExtFinder(EternaFactory factory, String fName)
	{
		if (factory != null)
		{
			ExtObjectFinder finder = (ExtObjectFinder) factory
					.getFactoryContainer().getAttribute(fName);
			if (finder != null)
			{
				return finder;
			}
			return getExtFinder(factory.getShareFactory(), fName);
		}
		return null;
	}

	/**
	 * 获取某种类型的对象的所有名称.
	 */
	public String[] getObjectNames(Class type)
	{
		List result = new ArrayList();
		Iterator itr = this.objectList.iterator();
		int count = this.objectList.size();
		for (int i = 0; i < count; i++)
		{
			ObjectContainer container = (ObjectContainer) itr.next();
			if (container != null)
			{
				if (type == null || type.isAssignableFrom(container.getType()))
				{
					result.add(container.getName());
				}
			}
		}
		String[] names = new String[result.size()];
		result.toArray(names);
		return names;
	}

	public int getObjectCount()
	{
		return this.objectList.size();
	}

	// 存放对象的容器
	private final Map objectMap = new HashMap();
	private List objectList = new ArrayList();
	// 列表中是否有空余位置, 如注销了对象之后
	private boolean hasEmptyPosition;

}

/**
 * 日志记录器的配置对象.
 */
class DaoLoggerConfig
{
	public DaoLoggerConfig(Map daoLoggerIndexMap, DaoLogger[] daoLoggers)
	{
		this.daoLoggerIndexMap = daoLoggerIndexMap;
		this.daoLoggers = daoLoggers;
	}
	final Map daoLoggerIndexMap;
	final DaoLogger[] daoLoggers;

	public int getDaoLoggerIndex(String name)
	{
		Integer i = (Integer) this.daoLoggerIndexMap.get(name);
		return i == null ? -1 : i.intValue();
	}

}
