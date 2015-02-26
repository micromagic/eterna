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

package self.micromagic.eterna.share;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.commons.logging.Log;

import self.micromagic.eterna.dao.Constant;
import self.micromagic.eterna.dao.Entity;
import self.micromagic.eterna.dao.Query;
import self.micromagic.eterna.dao.ResultFormat;
import self.micromagic.eterna.dao.SpecialLog;
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
import self.micromagic.eterna.security.UserManager;
import self.micromagic.eterna.view.Component;
import self.micromagic.eterna.view.DataPrinter;
import self.micromagic.eterna.view.Function;
import self.micromagic.eterna.view.Resource;
import self.micromagic.eterna.view.StringCoder;
import self.micromagic.eterna.view.View;
import self.micromagic.eterna.view.impl.StringCoderImpl;
import self.micromagic.util.StringTool;
import self.micromagic.util.Utility;

public class EternaFactoryImpl extends AbstractFactory
		implements EternaFactory
{
	protected static final Log log = Tool.log;

	/**
	 * 当前是否在执行初始化.
	 */
	private boolean inInit = false;

	private EternaFactory shareEternaFactory;
	private boolean initialized = false;
	private UserManager userManager = null;
	private DataSourceManager dataSourceManager = null;


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
			if (this.initialized)
			{
				um.initUserManager(this);
			}
			this.userManager = um;
		}
	}

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
			if (this.initialized)
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
		if (!this.initialized)
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
		if (!this.initialized)
		{
			this.inInit = true;
			this.initialized = true;
			try
			{
				this.init0(factoryContainer, shareFactory);
				return true;
			}
			finally
			{
				this.inInit = false;
			}
		}
		return false;
	}

	private void init0(FactoryContainer factoryContainer, Factory shareFactory)
	{
		super.initialize(factoryContainer, shareFactory);
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
			ParseException.setContextInfo("dataSourceManager");
			this.dataSourceManager.initialize(this);
		}
		else
		{
			this.dataSourceManager = getDataSourceFromCache();
			if (this.dataSourceManager != null)
			{
				ParseException.setContextInfo("dataSourceManager");
				this.dataSourceManager.initialize(this);
			}
		}

		// 重新构造列表, 去除对象列表中的多余空间
		List temp = new ArrayList(this.objectList.size() + 8);
		temp.addAll(this.objectList);
		this.objectList = temp;

		// 初始化注册的对象
		int size = this.objectList.size();
		ObjectContainer container;
		for (int i = 0; i < size; i++)
		{
			container = (ObjectContainer) this.objectList.get(i);
			String objName = container.getName() + "(" + container.getType().getName() + ")";
			ParseException.setContextInfo(objName);
			container.initialize(this);
		}

		// 初始化, userManager
		if (this.userManager != null)
		{
			ParseException.setContextInfo("userManager");
			this.userManager.initUserManager(this);
		}

		// 初始化, special-sql-log
		if (this.specialLog != null)
		{
			ParseException.setContextInfo("specialLog");
			this.specialLog.initSpecialLog(this);
		}

		// 初始化, model-caller
		// model-caller不能使用共享工厂中的对象.
		ParseException.setContextInfo("modelCaller");
		this.modelCaller.initModelCaller(this);

		// 初始化, string-coder
		ParseException.setContextInfo("stringCoder");
		this.stringCoder.initStringCoder(this);
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
			container.destroy();
		}
	}


	//----------------------------------  dao  --------------------------------------

	private SpecialLog specialLog = null;

	public String getConstantValue(String name)
			throws EternaException
	{
		Constant constant = (Constant) this.createObject(name);
		return constant.getValue();
	}

	public SpecialLog getSpecialLog()
			throws EternaException
	{
		if (this.specialLog == null && this.shareEternaFactory != null)
		{
			return this.shareEternaFactory.getSpecialLog();
		}
		return this.specialLog;
	}

	public void setSpecialLog(SpecialLog sl)
			throws EternaException
	{
		if (this.specialLog != null)
		{
			if (ContainerManager.getSuperInitLevel() == 0)
			{
				log.warn("Duplicate SpecialLog.");
			}
		}
		else if (sl != null)
		{
			if (this.initialized)
			{
				sl.initSpecialLog(this);
			}
			this.specialLog = sl;
		}
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
		PreparerCreater pc = CreaterManager.createPrepare(type, null, this);
		return pc.createPreparer(value);
	}

	//----------------------------------  search  --------------------------------------

	private SearchManagerGenerator searchManagerGenerator;
	private SearchAttributes searchAttributes;

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

	public void registerSearchManager(SearchManagerGenerator generator)
			throws EternaException
	{
		if (generator == null)
		{
			throw new NullPointerException();
		}
		if (this.searchManagerGenerator != null)
		{
			if (ContainerManager.getSuperInitLevel() == 0)
			{
				log.warn("Duplicate SearchManagerGenerator.");
			}
		}
		else
		{
			if (this.initialized)
			{
				generator.setFactory(this);
			}
			this.searchManagerGenerator = generator;
		}
	}

	public SearchManager createSearchManager()
			throws EternaException
	{
		if (this.searchManagerGenerator == null)
		{
			this.searchManagerGenerator = new SearchManagerImpl();
			this.searchManagerGenerator.setFactory(this);
		}
		SearchManager searchManager = this.searchManagerGenerator.createSearchManager();
		searchManager.setAttributes(this.getSearchAttributes());
		return searchManager;
	}

	public SearchAttributes getSearchAttributes()
			throws EternaException
	{
		String attrs = (String) super.getAttribute(SEARCH_ATTRIBUTES_FLAG);
		if (attrs != null)
		{
			Map attrMap = StringTool.string2Map(attrs, ",", ':', true, false, null, null);
			this.searchAttributes = new SearchAttributes(attrMap);
		}
		else if (this.shareEternaFactory != null)
		{
			this.searchAttributes = this.shareEternaFactory.getSearchAttributes();
		}
		else
		{
			this.searchAttributes = new SearchAttributes(null);
		}
		return this.searchAttributes;
	}


	//----------------------------------  model  --------------------------------------

	private String modelNameTag;
	private final ModelCaller defaultModelCaller = new ModelCallerImpl();
	private ModelCaller modelCaller = this.defaultModelCaller;

	public String getModelNameTag()
			throws EternaException
	{
		if (this.modelNameTag == null)
		{
			this.modelNameTag = (String) this.getAttribute(MODEL_NAME_TAG_FLAG);
			if (this.modelNameTag == null)
			{
				this.modelNameTag = "model";
			}
		}
		return this.modelNameTag;
	}

	public ModelCaller getModelCaller()
	{
		return this.modelCaller;
	}

	public void setModelCaller(ModelCaller mc)
		throws EternaException
	{
		if (this.modelCaller != this.defaultModelCaller)
		{
			if (ContainerManager.getSuperInitLevel() == 0)
			{
				log.warn("Duplicate ModelCaller.");
			}
		}
		else if (mc != null)
		{
			if (this.initialized)
			{
				mc.initModelCaller(this);
			}
			this.modelCaller = mc;
		}
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
	private final StringCoder defaultStringCoder = new StringCoderImpl();
	private StringCoder stringCoder = this.defaultStringCoder;

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

	public StringCoder getStringCoder()
	{
		return this.stringCoder;
	}

	public void setStringCoder(StringCoder sc)
			throws EternaException
	{
		if (this.stringCoder != this.defaultStringCoder)
		{
			if (ContainerManager.getSuperInitLevel() == 0)
			{
				log.warn("Duplicate StringCoder.");
			}
		}
		else if (sc != null)
		{
			if (this.initialized)
			{
				sc.initStringCoder(this);
			}
			this.stringCoder = sc;
		}
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
		if (obj instanceof ObjectCreater)
		{
			container = new ObjectCreaterCon(id, (ObjectCreater) obj);
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
		if (this.initialized)
		{
			container.initialize(this);
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
		return container.create(this.inInit, this);
	}

	public boolean isSingleton(Object key)
			throws EternaException
	{
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
		ObjectContainer container = (ObjectContainer) this.objectMap.get(key);
		if (container == null)
		{
			if (this.shareFactory != null)
			{
				return this.shareFactory.createObject(key);
			}
			throw new ParseException("Not found object [" + key + "].");
		}
		return container.create(this.inInit, this);
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

	// 存放对象的容器
	private final Map objectMap = new HashMap();
	private List objectList = new ArrayList();
	// 列表中是否有空余位置, 如注销了对象之后
	private boolean hasEmptyPosition;

}

/**
 * 存放对象的容器.
 */
abstract class ObjectContainer
{
	ObjectContainer(int id)
	{
		this.id = id;
	}

	/**
	 * 获取对象的编号.
	 */
	public int getId()
	{
		return this.id;
	}
	private final int id;

	/**
	 * 获取对象的名称.
	 */
	public abstract String getName();

	/**
	 * 所创建的对象是否为单例.
	 */
	public abstract boolean isSingleton();

	/**
	 * 获取对象的类型.
	 */
	public abstract Class getType();

	/**
	 * 执行初始化.
	 */
	public abstract boolean initialize(EternaFactory factory) throws EternaException;

	/**
	 * 创建容器中存放的对象.
	 *
	 * @param needInit  是否需要执行初始化
	 */
	public abstract Object create(boolean needInit, EternaFactory factory);

	/**
	 * 销毁存放的对象.
	 */
	public abstract void destroy();

}

/**
 * EternaObject的容器.
 */
class EternaObjectCon extends ObjectContainer
{
	EternaObjectCon(int id, EternaObject obj)
	{
		super(id);
		this.obj = obj;
	}
	private final EternaObject obj;

	public String getName()
	{
		return this.obj.getName();
	}

	public boolean isSingleton()
	{
		return true;
	}

	public Class getType()
	{
		return this.obj.getClass();
	}

	public boolean initialize(EternaFactory factory)
			throws EternaException
	{
		return this.obj.initialize(factory);
	}

	public Object create(boolean needInit, EternaFactory factory)
	{
		if (needInit)
		{
			this.initialize(factory);
		}
		return this.obj;
	}

	public void destroy()
	{
	}

}

/**
 * ObjectCreater的容器.
 */
class ObjectCreaterCon extends ObjectContainer
{
	ObjectCreaterCon(int id, ObjectCreater obj)
	{
		super(id);
		this.obj = obj;
		this.singleton = this.obj.isSingleton();
	}
	private final ObjectCreater obj;
	private Object instance;
	private final boolean singleton;

	public String getName()
	{
		return this.obj.getName();
	}

	public boolean isSingleton()
	{
		return this.singleton;
	}

	public Class getType()
	{
		return this.obj.getObjectType();
	}

	public boolean initialize(EternaFactory factory)
			throws EternaException
	{
		return this.obj.initialize(factory);
	}

	public Object create(boolean needInit, EternaFactory factory)
	{
		if (this.instance != null)
		{
			return this.instance;
		}
		if (needInit)
		{
			this.initialize(factory);
		}
		return this.singleton ? this.instance = this.obj.create() : this.obj.create();
	}

	public void destroy()
	{
		this.obj.destroy();
	}

}

class DataSourceManagerImpl implements DataSourceManager
{
	private String defaultDataSourceName = null;
	private DataSource defaultDataSource = null;

	private Map dataSourceMap = null;

	/**
	 * 初始化这个DataSourceManager.
	 */
	public void initialize(EternaFactory factory)
			throws EternaException
	{
		if (this.dataSourceMap == null)
		{
			throw new EternaException("Not registe any data source.");
		}
		if (this.defaultDataSourceName == null)
		{
			throw new EternaException("Must give this default data source name.");
		}
		this.defaultDataSource = (DataSource) this.dataSourceMap.get(this.defaultDataSourceName);
		if (this.defaultDataSource == null)
		{
			throw new EternaException("Not found the data source:" + this.defaultDataSourceName + ".");
		}

	}

	public DataSource getDefaultDataSource()
	{
		return this.defaultDataSource;
	}

	public DataSource getDataSource(String name)
	{
		return (DataSource) this.dataSourceMap.get(name);
	}

	public Map getDataSourceMap()
	{
		return Collections.unmodifiableMap(this.dataSourceMap);
	}

	protected boolean hasDataSource(String name)
	{
		if (this.dataSourceMap == null)
		{
			return false;
		}
		return this.dataSourceMap.containsKey(name);
	}

	public String getDefaultDataSourceName()
	{
		return this.defaultDataSourceName;
	}

	public void setDefaultDataSourceName(String name)
			throws EternaException
	{
		if (this.defaultDataSource != null)
		{
			throw new EternaException("Can't set default data source name after Initialization.");
		}
		this.defaultDataSourceName = name;
	}

	protected void addDataSource(String name, DataSource ds)
	{
		if (this.dataSourceMap == null)
		{
			this.dataSourceMap = new HashMap();
		}
		this.dataSourceMap.put(name, ds);
	}

	public void addDataSource(Context context, String dataSourceConfig)
			throws EternaException
	{
		String[] items = StringTool.separateString(
				Utility.resolveDynamicPropnames(dataSourceConfig), ";", true);
		try
		{
			for (int i = 0; i < items.length; i++)
			{
				if (this.dataSourceMap == null)
				{
					this.dataSourceMap = new HashMap();
				}
				String item = items[i];
				if (item.length() > 0)
				{
					int index = item.indexOf('=');
					if (index == -1)
					{
						throw new EternaException("Error DataSource define:" + item + ".");
					}
					String key = item.substring(0, index).trim();
					if (this.dataSourceMap.containsKey(key))
					{
						throw new EternaException("Duplicate DataSource name:" + key + ".");
					}
					String name = item.substring(index + 1).trim();
					if (name.length() > 0)
					{
						this.dataSourceMap.put(key, context.lookup(name));
					}
					else
					{
						this.dataSourceMap.put(key, Utility.getDataSource());
					}
				}
			}
		}
		catch (NamingException ex)
		{
			EternaFactoryImpl.log.error("Error when get jdbc in jndi.", ex);
			throw new EternaException(ex);
		}
	}

}
