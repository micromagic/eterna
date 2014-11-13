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

import org.apache.commons.logging.Log;
import self.micromagic.eterna.digester.ConfigurationException;
import self.micromagic.eterna.digester.FactoryManager;
import self.micromagic.eterna.digester.ObjectLogRule;
import self.micromagic.eterna.digester.ShareSet;
import self.micromagic.eterna.model.ModelAdapter;
import self.micromagic.eterna.model.ModelAdapterGenerator;
import self.micromagic.eterna.model.ModelCaller;
import self.micromagic.eterna.model.ModelExport;
import self.micromagic.eterna.model.impl.ModelCallerImpl;
import self.micromagic.eterna.search.ConditionBuilder;
import self.micromagic.eterna.search.SearchAdapter;
import self.micromagic.eterna.search.SearchAdapterGenerator;
import self.micromagic.eterna.search.SearchManager;
import self.micromagic.eterna.search.SearchManagerGenerator;
import self.micromagic.eterna.search.impl.SearchManagerImpl;
import self.micromagic.eterna.security.UserManager;
import self.micromagic.eterna.sql.QueryAdapter;
import self.micromagic.eterna.sql.QueryAdapterGenerator;
import self.micromagic.eterna.sql.ResultFormat;
import self.micromagic.eterna.sql.ResultReaderManager;
import self.micromagic.eterna.sql.SQLParameterGroup;
import self.micromagic.eterna.sql.SpecialLog;
import self.micromagic.eterna.sql.UpdateAdapter;
import self.micromagic.eterna.sql.UpdateAdapterGenerator;
import self.micromagic.eterna.sql.preparer.ValuePreparerCreater;
import self.micromagic.eterna.sql.preparer.ValuePreparerCreaterGenerator;
import self.micromagic.eterna.sql.preparer.ValuePreparerCreaterGeneratorImpl;
import self.micromagic.eterna.view.Component;
import self.micromagic.eterna.view.DataPrinter;
import self.micromagic.eterna.view.Function;
import self.micromagic.eterna.view.Resource;
import self.micromagic.eterna.view.StringCoder;
import self.micromagic.eterna.view.ViewAdapter;
import self.micromagic.eterna.view.ViewAdapterGenerator;
import self.micromagic.eterna.view.impl.StringCoderImpl;

public class EternaFactoryImpl extends AbstractFactory
		implements EternaFactory
{
	protected static final Log log = Tool.log;

	private EternaFactory shareEternaFactory;
	private EternaFactoryImpl sameShare;
	private boolean initialized = false;
	private UserManager userManager = null;
	private DataSourceManager dataSourceManager = null;


	//----------------------------------  初始化及公共  --------------------------------------

	public EternaFactory getShareFactory()
	{
		return this.shareEternaFactory;
	}

	public UserManager getUserManager()
			throws ConfigurationException
	{
		if (this.userManager == null && this.shareEternaFactory != null)
		{
			return this.shareEternaFactory.getUserManager();
		}
		return this.userManager;
	}

	public void setUserManager(UserManager um)
			throws ConfigurationException
	{
		if (this.userManager != null)
		{
			if (!FactoryManager.isSuperInit())
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
			throws ConfigurationException
	{
		if (this.dataSourceManager == null && this.shareEternaFactory != null)
		{
			return this.shareEternaFactory.getDataSourceManager();
		}
		return this.dataSourceManager;
	}

	public void setDataSourceManager(DataSourceManager dsm)
			throws ConfigurationException
	{
		if (this.dataSourceManager != null)
		{
			if (!FactoryManager.isSuperInit())
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
			throws ConfigurationException
	{
		String[] tmpP = this.shareEternaFactory != null ?
				this.shareEternaFactory.getAttributeNames() : null;
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
			throws ConfigurationException
	{
		Object tmp = super.getAttribute(name);
		if (tmp == null && this.shareEternaFactory != null)
		{
			tmp = this.shareEternaFactory.getAttribute(name);
		}
		return tmp;
	}

	public Object setAttribute(String name, Object value)
			throws ConfigurationException
	{
		if (!this.initialized)
		{
			// 未初始化完成时, 需要检查全局属性是否被重复设置了
			if (super.hasAttribute(name))
			{
				if (!FactoryManager.isSuperInit())
				{
					log.warn("Duplicate global attribute [" + name + "].");
				}
				return null;
			}
		}
		return super.setAttribute(name, value);
	}

	public void initialize(FactoryManager.Instance factoryManager, Factory shareFactory)
			throws ConfigurationException
	{
		if (!this.initialized)
		{
			this.initialized = true;
			super.initialize(factoryManager, shareFactory);
			this.shareEternaFactory = (EternaFactory) shareFactory;
			if (shareFactory != null && shareFactory instanceof EternaFactoryImpl)
			{
				this.sameShare = (EternaFactoryImpl) shareFactory;
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
				ObjectLogRule.setObjName("dataSourceManager");
				this.dataSourceManager.initialize(this);
			}
			else
			{
				this.dataSourceManager = ShareSet.getDataSourceFromCache(this.getFactoryManager());
				if (this.dataSourceManager != null)
				{
					ObjectLogRule.setObjName("dataSourceManager");
					this.dataSourceManager.initialize(this);
				}
			}

			String dName = (String) super.getAttribute(ValuePreparerCreater.DEFAULT_VPC_ATTRIBUTE);
			if (dName != null)
			{
				// 如果指定了默认的vpc, 则获取这个vpc
				this.defaultVPCG = (ValuePreparerCreaterGenerator) this.valuePreparerMap.get(dName);
				if (this.defaultVPCG == null && this.shareEternaFactory != null)
				{
					this.defaultVPCG = this.shareEternaFactory.getValuePreparerCreaterGenerator(dName);
				}
				if (this.defaultVPCG == null)
				{
					log.error("The default vpc [" + dName + "] not found, use sys default.");
				}
			}
			else if (this.shareEternaFactory != null)
			{
				// 如果有共享的factory, 则获取共享factory的默认vpcg
				this.defaultVPCG = this.shareEternaFactory.getDefaultValuePreparerCreaterGenerator();
			}
			if (this.defaultVPCG == null)
			{
				// 如果前面没有获取到默认的vpcg, 则生成一个默认的
				this.defaultVPCG = new ValuePreparerCreaterGeneratorImpl();
				this.registerValuePreparerGenerator(this.defaultVPCG);
			}


			// 初始化, Resource
			Iterator itr = this.resourceMap.values().iterator();
			while (itr.hasNext())
			{
				Resource resource = (Resource) itr.next();
				ObjectLogRule.setObjName("resource", resource.getName());
				resource.initialize(this);
			}

			// 初始化, vpc
			itr = this.valuePreparerMap.values().iterator();
			while (itr.hasNext())
			{
				ValuePreparerCreaterGenerator vpcg = (ValuePreparerCreaterGenerator) itr.next();
				ObjectLogRule.setObjName("vpc", String.valueOf(vpcg.getName()));
				vpcg.initialize(this);
			}

			// 初始化, ResultFormat
			itr = this.formatMap.values().iterator();
			while (itr.hasNext())
			{
				ResultFormat format = (ResultFormat) itr.next();
				ObjectLogRule.setObjName("format", format.getName());
				format.initialize(this);
			}

			// 初始化, ResultReaderManager
			itr = this.readerManagerMap.values().iterator();
			while (itr.hasNext())
			{
				ResultReaderManager manager = (ResultReaderManager) itr.next();
				ObjectLogRule.setObjName("readerManager", manager.getName());
				manager.initialize(this);
				manager.lock();
			}

			// 初始化, SQLParameterGroup
			itr = this.paramGroupMap.values().iterator();
			while (itr.hasNext())
			{
				SQLParameterGroup group = (SQLParameterGroup) itr.next();
				ObjectLogRule.setObjName("parameterGroup", group.getName());
				group.initialize(this);
			}

			// 初始化, SQL
			if (this.sameShare != null)
			{
				this.queryManager.initialize(this.sameShare.queryManager);
				this.updateManager.initialize(this.sameShare.updateManager);
			}
			else
			{
				this.queryManager.initialize(FactoryGeneratorManager.createQueryFGM(this.shareEternaFactory));
				this.updateManager.initialize(FactoryGeneratorManager.createUpdateFGM(this.shareEternaFactory));
			}

			// 初始化, userManager
			if (this.userManager != null)
			{
				ObjectLogRule.setObjName("userManager");
				this.userManager.initUserManager(this);
			}

			// 初始化, special-sql-log
			if (this.specialLog != null)
			{
				ObjectLogRule.setObjName("specialLog");
				this.specialLog.initSpecialLog(this);
			}

			// 初始化, ConditionBuilder
			itr = this.conditionBuilderMap.values().iterator();
			while (itr.hasNext())
			{
				ConditionBuilder cb = (ConditionBuilder) itr.next();
				ObjectLogRule.setObjName("builder", cb.getName());
				cb.initialize(this);
			}

			// 初始化, ConditionBuilderList
			itr = this.conditionBuilderNameListMap.keySet().iterator();
			while (itr.hasNext())
			{
				Object name = itr.next();
				List names = (List) this.conditionBuilderNameListMap.get(name);
				ObjectLogRule.setObjName("builderList", (String) name);
				this.initConditionBuilderList((String) name, names);
			}
			this.conditionBuilderNameListMap.clear();

			// 初始化, search
			if (this.sameShare != null)
			{
				this.searchAdapterManager.initialize(this.sameShare.searchAdapterManager);
			}
			else
			{
				this.searchAdapterManager.initialize(FactoryGeneratorManager.createSearchFGM(this.shareEternaFactory));
			}

			// 初始化, model-caller
			if (this.modelCaller == this.defaultModelCaller && this.shareEternaFactory != null)
			{
				// 如果未设置过model-caller且有共享工厂, 则使用共享工厂的
				this.modelCaller = this.shareEternaFactory.getModelCaller();
			}
			else
			{
				ObjectLogRule.setObjName("modelCaller");
				this.modelCaller.initModelCaller(this);
			}

			// 初始化, model
			if (this.sameShare != null)
			{
				this.modelManager.initialize(this.sameShare.modelManager);
			}
			else
			{
				this.modelManager.initialize(FactoryGeneratorManager.createModelFGM(this.shareEternaFactory));
			}

			// 初始化, string-coder
			ObjectLogRule.setObjName("stringCoder");
			this.stringCoder.initStringCoder(this);

			// 初始化, DataPrinter
			itr = this.dataPrinterMap.values().iterator();
			while (itr.hasNext())
			{
				DataPrinter dataPrinter = (DataPrinter) itr.next();
				ObjectLogRule.setObjName("dataPrinter", dataPrinter.getName());
				dataPrinter.initialize(this);
			}

			// 初始化, Typical Component
			itr = this.typicalComponentMap.values().iterator();
			while (itr.hasNext())
			{
				Component component = (Component) itr.next();
				ObjectLogRule.setObjName("typical", component.getName());
				component.initialize(this, null);
			}

			// 初始化, view
			if (this.sameShare != null)
			{
				this.viewManager.initialize(this.sameShare.viewManager);
			}
			else
			{
				this.viewManager.initialize(FactoryGeneratorManager.createViewFGM(this.shareEternaFactory));
			}
		}
	}

	public void destroy()
	{
		super.destroy();
		this.queryManager.destroy();
		this.updateManager.destroy();
		this.searchAdapterManager.destroy();
		this.modelManager.destroy();
		this.viewManager.destroy();
	}


	//----------------------------------  SQLFactory  --------------------------------------

	private Map constantMap = new HashMap();
	private SpecialLog specialLog = null;
	private Map formatMap = new HashMap();
	private Map readerManagerMap = new HashMap();
	private Map paramGroupMap = new HashMap();
	private Map valuePreparerMap = new HashMap();

	private ValuePreparerCreaterGenerator defaultVPCG;
	private FactoryGeneratorManager queryManager
			= new FactoryGeneratorManager("QueryAdapter", this);
	private FactoryGeneratorManager updateManager
			= new FactoryGeneratorManager("UpdateAdapter", this);

	public String getConstantValue(String name)
			throws ConfigurationException
	{
		String result = (String) this.constantMap.get(name);
		if (result == null && this.shareEternaFactory != null)
		{
			result = this.shareEternaFactory.getConstantValue(name);
		}
		return result;
	}

	public void addConstantValue(String name, String value)
	{
		if (this.constantMap.containsKey(name))
		{
			if (!FactoryManager.isSuperInit())
			{
				log.warn("Duplicate ConstantValue [" + name + "].");
			}
		}
		else if (value != null)
		{
			this.constantMap.put(name, value);
		}
	}

	public SpecialLog getSpecialLog()
			throws ConfigurationException
	{
		if (this.specialLog == null && this.shareEternaFactory != null)
		{
			return this.shareEternaFactory.getSpecialLog();
		}
		return this.specialLog;
	}

	public void setSpecialLog(SpecialLog sl)
			throws ConfigurationException
	{
		if (this.specialLog != null)
		{
			if (!FactoryManager.isSuperInit())
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

	public ResultFormat getFormat(String name)
			throws ConfigurationException
	{
		ResultFormat result = (ResultFormat) this.formatMap.get(name);
		if (result == null && this.shareEternaFactory != null)
		{
			result = this.shareEternaFactory.getFormat(name);
		}
		return result;
	}

	public void addFormat(String name, ResultFormat format)
			throws ConfigurationException
	{
		if (this.formatMap.containsKey(name))
		{
			if (!FactoryManager.isSuperInit())
			{
				log.warn("Duplicate Format [" + name + "].");
			}
		}
		else if (format != null)
		{
			if (this.initialized)
			{
				format.initialize(this);
			}
			this.formatMap.put(name, format);
		}
	}

	public ResultReaderManager getReaderManager(String name)
			throws ConfigurationException
	{
		ResultReaderManager result = (ResultReaderManager) this.readerManagerMap.get(name);
		if (result == null && this.shareEternaFactory != null)
		{
			result = this.shareEternaFactory.getReaderManager(name);
		}
		return result;
	}

	public void addReaderManager(String name, ResultReaderManager manager)
			throws ConfigurationException
	{
		if (this.readerManagerMap.containsKey(name))
		{
			if (!FactoryManager.isSuperInit())
			{
				log.warn("Duplicate reader manager [" + name + "].");
			}
		}
		else if (manager != null)
		{
			if (this.initialized)
			{
				manager.initialize(this);
			}
			this.readerManagerMap.put(name, manager);
		}
	}

	public SQLParameterGroup getParameterGroup(String name)
			throws ConfigurationException
	{
		SQLParameterGroup group = (SQLParameterGroup) this.paramGroupMap.get(name);
		if (group == null && this.shareEternaFactory != null)
		{
			group = this.shareEternaFactory.getParameterGroup(name);
		}
		return group;
	}

	public void addParameterGroup(String name, SQLParameterGroup group)
			throws ConfigurationException
	{
		if (this.paramGroupMap.containsKey(name))
		{
			if (!FactoryManager.isSuperInit())
			{
				log.warn("Duplicate SQLParameterGroup [" + name + "].");
			}
		}
		else if (group != null)
		{
			if (this.initialized)
			{
				group.initialize(this);
			}
			this.paramGroupMap.put(name, group);
		}
	}

	public QueryAdapter createQueryAdapter(String name)
			throws ConfigurationException
	{
		return (QueryAdapter) this.queryManager.create(name);
	}

	public QueryAdapter createQueryAdapter(int id)
			throws ConfigurationException
	{
		return (QueryAdapter) this.queryManager.create(id);
	}

	public int getQueryAdapterId(String name)
			throws ConfigurationException
	{
		return this.queryManager.getIdByName(name);
	}

	public void registerQueryAdapter(QueryAdapterGenerator generator)
			throws ConfigurationException
	{
		this.queryManager.register(generator);
	}

	public void deregisterQueryAdapter(String name)
			throws ConfigurationException
	{
		this.queryManager.deregister(name);
	}

	public UpdateAdapter createUpdateAdapter(String name)
			throws ConfigurationException
	{
		return (UpdateAdapter) this.updateManager.create(name);
	}

	public UpdateAdapter createUpdateAdapter(int id)
			throws ConfigurationException
	{
		return (UpdateAdapter) this.updateManager.create(id);
	}

	public int getUpdateAdapterId(String name)
			throws ConfigurationException
	{
		return this.updateManager.getIdByName(name);
	}

	public void registerUpdateAdapter(UpdateAdapterGenerator generator)
			throws ConfigurationException
	{
		this.updateManager.register(generator);
	}

	public void deregisterUpdateAdapter(String name)
			throws ConfigurationException
	{
		this.updateManager.deregister(name);
	}

	public void registerValuePreparerGenerator(ValuePreparerCreaterGenerator generator)
			throws ConfigurationException
	{
		if (generator == null)
		{
			throw new NullPointerException();
		}
		if (this.valuePreparerMap.containsKey(generator.getName()))
		{
			if (!FactoryManager.isSuperInit())
			{
				log.warn("Duplicate VPGenerator [" + generator.getName() + "].");
			}
		}
		else
		{
			if (this.initialized)
			{
				generator.initialize(this);
			}
			this.valuePreparerMap.put(generator.getName(), generator);
		}
	}

	public ValuePreparerCreaterGenerator getDefaultValuePreparerCreaterGenerator()
	{
		return this.defaultVPCG;
	}

	public ValuePreparerCreaterGenerator getValuePreparerCreaterGenerator(String name)
			throws ConfigurationException
	{
		if (name == null)
		{
			return this.defaultVPCG;
		}
		ValuePreparerCreaterGenerator vpcg = (ValuePreparerCreaterGenerator) this.valuePreparerMap.get(name);
		if (vpcg == null)
		{
			if (this.shareEternaFactory != null)
			{
				return this.shareEternaFactory.getValuePreparerCreaterGenerator(name);
			}
			throw new ConfigurationException(
					"Not found [ValuePreparerCreaterGenerator] name:" + name + ".");
		}
		return vpcg;
	}

	public ValuePreparerCreater createValuePreparerCreater(int type)
			throws ConfigurationException
	{
		int pureType = TypeManager.getPureType(type);
		return this.defaultVPCG.createValuePreparerCreater(pureType);
	}

	public ValuePreparerCreater createValuePreparerCreater(String name, int type)
			throws ConfigurationException
	{
		int pureType = TypeManager.getPureType(type);
		ValuePreparerCreaterGenerator vpcg = (ValuePreparerCreaterGenerator) this.valuePreparerMap.get(name);
		if (vpcg == null)
		{
			if (this.shareEternaFactory != null)
			{
				return this.shareEternaFactory.createValuePreparerCreater(name, pureType);
			}
			throw new ConfigurationException(
					"Not found [ValuePreparerCreaterGenerator] name:" + name + ".");
		}
		return vpcg.createValuePreparerCreater(pureType);
	}


	//----------------------------------  SearchFactory  --------------------------------------

	private Map conditionBuilderMap = new HashMap();
	private Map conditionBuilderListMap = new HashMap();
	private Map conditionBuilderNameListMap = new HashMap();

	private FactoryGeneratorManager searchAdapterManager
			= new FactoryGeneratorManager("SearchAdapter", this);

	private SearchManagerGenerator searchManagerGenerator;
	private SearchManager.Attributes searchManagerAttributes;

	private void initConditionBuilderList(String name, List builderNames)
			throws ConfigurationException
	{
		Iterator itrName = builderNames.iterator();
		List builders = new ArrayList(builderNames.size());
		while (itrName.hasNext())
		{
			String cbName = (String) itrName.next();
			ConditionBuilder cb = this.getConditionBuilder(cbName);
			if (cb == null)
			{
				throw new ConfigurationException(
						"The ConditionBuilder [" + cbName + "] not found at list [" + name + "].");
			}
			builders.add(cb);
		}
		if (this.conditionBuilderListMap.containsKey(name))
		{
			log.warn("Duplicate ConditionBuilderList [" + name + "].");
		}
		else
		{
			this.conditionBuilderListMap.put(name, Collections.unmodifiableList(builders));
		}
	}

	public ConditionBuilder getConditionBuilder(String name)
			throws ConfigurationException
	{
		ConditionBuilder result = (ConditionBuilder) this.conditionBuilderMap.get(name);
		if (result == null && this.shareEternaFactory != null)
		{
			result = this.shareEternaFactory.getConditionBuilder(name);
		}
		return result;
	}

	public void addConditionBuilder(String name, ConditionBuilder builder)
			throws ConfigurationException
	{
		if (this.conditionBuilderMap.containsKey(name))
		{
			if (!FactoryManager.isSuperInit())
			{
				log.warn("Duplicate ConditionBuilder [" + name + "].");
			}
		}
		else if (builder != null)
		{
			if (this.initialized)
			{
				builder.initialize(this);
			}
			this.conditionBuilderMap.put(name, builder);
		}
	}

	public List getConditionBuilderList(String name)
			throws ConfigurationException
	{
		List result = (List) this.conditionBuilderListMap.get(name);
		if (result == null && this.shareEternaFactory != null)
		{
			result = this.shareEternaFactory.getConditionBuilderList(name);
		}
		return result;
	}

	public void addConditionBuilderList(String name, List builderNames)
			throws ConfigurationException
	{
		if (builderNames == null)
		{
			throw new NullPointerException();
		}
		if (this.initialized)
		{
			this.initConditionBuilderList(name, builderNames);
		}
		else
		{
			if (this.conditionBuilderNameListMap.containsKey(name))
			{
				if (!FactoryManager.isSuperInit())
				{
					log.warn("Duplicate ConditionBuilderList [" + name + "].");
				}
			}
			else
			{
				this.conditionBuilderNameListMap.put(name, builderNames);
			}
		}
	}

	public SearchAdapter createSearchAdapter(String name)
			throws ConfigurationException
	{
		return (SearchAdapter) this.searchAdapterManager.create(name);
	}

	public SearchAdapter createSearchAdapter(int id)
			throws ConfigurationException
	{
		return (SearchAdapter) this.searchAdapterManager.create(id);
	}

	public int getSearchAdapterId(String name)
			throws ConfigurationException
	{
		return this.searchAdapterManager.getIdByName(name);
	}

	public void registerSearchAdapter(SearchAdapterGenerator generator)
			throws ConfigurationException
	{
		this.searchAdapterManager.register(generator);
	}

	public void deregisterSearchAdapter(String name)
			throws ConfigurationException
	{
		this.searchAdapterManager.deregister(name);
	}

	public void registerSearchManager(SearchManagerGenerator generator)
			throws ConfigurationException
	{
		if (generator == null)
		{
			throw new NullPointerException();
		}
		if (this.searchManagerGenerator != null)
		{
			if (!FactoryManager.isSuperInit())
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

	public SearchManager createSearchManager() throws ConfigurationException
	{
		if (this.searchManagerGenerator == null)
		{
			this.searchManagerGenerator = new SearchManagerImpl();
			this.searchManagerGenerator.setFactory(this);
		}
		SearchManager searchManager = this.searchManagerGenerator.createSearchManager();
		searchManager.setAttributes(this.getSearchManagerAttributes());
		return searchManager;
	}

	public SearchManager.Attributes getSearchManagerAttributes()
			throws ConfigurationException
	{
		if (this.searchManagerAttributes == null)
		{
			String pageNumTag = (String) this.getAttribute(
					SEARCH_MANAGER_ATTRIBUTE_PREFIX + "pageNumTag");
			String pageSizeTag = (String) this.getAttribute(
					SEARCH_MANAGER_ATTRIBUTE_PREFIX + "pageSizeTag");
			String querySettingTag = (String) this.getAttribute(
					SEARCH_MANAGER_ATTRIBUTE_PREFIX + "querySettingTag");
			String queryTypeTag = (String) this.getAttribute(
					SEARCH_MANAGER_ATTRIBUTE_PREFIX + "queryTypeTag");
			String queryTypeClear = (String) this.getAttribute(
					SEARCH_MANAGER_ATTRIBUTE_PREFIX + "queryTypeClear");
			String queryTypeReset = (String) this.getAttribute(
					SEARCH_MANAGER_ATTRIBUTE_PREFIX + "queryTypeReset");
			this.searchManagerAttributes = new SearchManager.Attributes(pageNumTag, pageSizeTag,
					querySettingTag, queryTypeTag, queryTypeClear, queryTypeReset);
		}
		return this.searchManagerAttributes;
	}


	//----------------------------------  ModelFactory  --------------------------------------

	private String modelNameTag;
	private Map exportMap = new HashMap();
	private ModelCaller defaultModelCaller = new ModelCallerImpl();
	private ModelCaller modelCaller = this.defaultModelCaller;

	private FactoryGeneratorManager modelManager
			= new FactoryGeneratorManager("ModelAdapter", this);

	public String getModelNameTag()
			throws ConfigurationException
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
		throws ConfigurationException
	{
		if (this.modelCaller != this.defaultModelCaller)
		{
			if (!FactoryManager.isSuperInit())
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

	public void addModelExport(String exportName, ModelExport modelExport)
	{
		if (this.exportMap.containsKey(exportName))
		{
			if (!FactoryManager.isSuperInit())
			{
				log.warn("Duplicate export [" + exportName + "].");
			}
		}
		else if (modelExport != null)
		{
			this.exportMap.put(exportName, modelExport);
		}
	}

	public ModelExport getModelExport(String exportName)
			throws ConfigurationException
	{
		ModelExport result = (ModelExport) this.exportMap.get(exportName);
		if (result == null && this.shareEternaFactory != null)
		{
			result = this.shareEternaFactory.getModelExport(exportName);
		}
		return result;
	}

	public ModelAdapter createModelAdapter(String name)
			throws ConfigurationException
	{
		return (ModelAdapter) this.modelManager.create(name);
	}

	public ModelAdapter createModelAdapter(int id)
			throws ConfigurationException
	{
		return (ModelAdapter) this.modelManager.create(id);
	}

	public int getModelAdapterId(String name)
			throws ConfigurationException
	{
		return this.modelManager.getIdByName(name);
	}

	public void registerModelAdapter(ModelAdapterGenerator generator)
			throws ConfigurationException
	{
		this.modelManager.register(generator);
	}

	public void deregisterModelAdapter(String name)
			throws ConfigurationException
	{
		this.modelManager.deregister(name);
	}


	//----------------------------------  ViewFactory  --------------------------------------

	private String viewGlobalSetting;
	private Map typicalComponentMap = new HashMap();
	private Map dataPrinterMap = new HashMap();
	private Map functionMap = new HashMap();
	private Map resourceMap = new HashMap();
	private StringCoder defaultStringCoder = new StringCoderImpl();
	private StringCoder stringCoder = this.defaultStringCoder;

	private FactoryGeneratorManager viewManager
			= new FactoryGeneratorManager("ViewAdapter", this);

	public String getViewGlobalSetting() throws ConfigurationException
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
			throws ConfigurationException
	{
		DataPrinter result = (DataPrinter) this.dataPrinterMap.get(name);
		if (result == null && this.shareEternaFactory != null)
		{
			result = this.shareEternaFactory.getDataPrinter(name);
		}
		return result;
	}

	public void addDataPrinter(String name, DataPrinter dataPrinter)
			throws ConfigurationException
	{
		if (this.dataPrinterMap.containsKey(name))
		{
			if (!FactoryManager.isSuperInit())
			{
				log.warn("Duplicate DataPrinter [" + name + "].");
			}
		}
		else if (dataPrinter != null)
		{
			if (this.initialized)
			{
				dataPrinter.initialize(this);
			}
			this.dataPrinterMap.put(name, dataPrinter);
		}
	}

	public Function getFunction(String name)
			throws ConfigurationException
	{
		Function result = (Function) this.functionMap.get(name);
		if (result == null && this.shareEternaFactory != null)
		{
			result = this.shareEternaFactory.getFunction(name);
		}
		if (result == null)
		{
			throw new ConfigurationException("Not found the function [" + name + "].");
		}
		return result;
	}

	public void addFunction(String name, Function fun)
	{
		if (this.functionMap.containsKey(name))
		{
			if (!FactoryManager.isSuperInit())
			{
				log.warn("Duplicate function [" + name + "].");
			}
		}
		else if (fun != null)
		{
			this.functionMap.put(name, fun);
		}
	}

	public Component getTypicalComponent(String name)
			throws ConfigurationException
	{
		Component result = (Component) this.typicalComponentMap.get(name);
		if (result == null && this.shareEternaFactory != null)
		{
			result = this.shareEternaFactory.getTypicalComponent(name);
		}
		return result;
	}

	public void addTypicalComponent(String name, Component com)
			throws ConfigurationException
	{
		if (this.typicalComponentMap.containsKey(name))
		{
			if (!FactoryManager.isSuperInit())
			{
				log.warn("Duplicate typical component [" + name + "].");
			}
		}
		else if (com != null)
		{
			if (this.initialized)
			{
				com.initialize(this, null);
			}
			this.typicalComponentMap.put(name, com);
		}
	}

	public StringCoder getStringCoder()
	{
		return this.stringCoder;
	}

	public void setStringCoder(StringCoder sc)
			throws ConfigurationException
	{
		if (this.stringCoder != this.defaultStringCoder)
		{
			if (!FactoryManager.isSuperInit())
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

	public ViewAdapter createViewAdapter(String name)
			throws ConfigurationException
	{
		return (ViewAdapter) this.viewManager.create(name);
	}

	public ViewAdapter createViewAdapter(int id)
			throws ConfigurationException
	{
		return (ViewAdapter) this.viewManager.create(id);
	}

	public int getViewAdapterId(String name)
			throws ConfigurationException
	{
		return this.viewManager.getIdByName(name);
	}

	public void registerViewAdapter(ViewAdapterGenerator generator)
			throws ConfigurationException
	{
		this.viewManager.register(generator);
	}

	public void deregisterViewAdapter(String name)
			throws ConfigurationException
	{
		this.viewManager.deregister(name);
	}

	public Resource getResource(String name)
			throws ConfigurationException
	{
		Resource result = (Resource) this.resourceMap.get(name);
		if (result == null && this.shareEternaFactory != null)
		{
			result = this.shareEternaFactory.getResource(name);
		}
		return result;
	}

	public void addResource(String name, Resource resource)
			throws ConfigurationException
	{
		if (this.resourceMap.containsKey(name))
		{
			if (!FactoryManager.isSuperInit())
			{
				log.warn("Duplicate Resource [" + name + "].");
			}
		}
		else if (resource != null)
		{
			if (this.initialized)
			{
				resource.initialize(this);
			}
			this.resourceMap.put(name, resource);
		}
	}

}