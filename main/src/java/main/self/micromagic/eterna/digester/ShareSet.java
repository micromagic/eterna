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

package self.micromagic.eterna.digester;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.commons.digester.Digester;
import org.apache.commons.digester.Rule;
import org.apache.commons.digester.RuleSetBase;
import org.xml.sax.Attributes;
import self.micromagic.eterna.security.UserManager;
import self.micromagic.eterna.share.DataSourceManager;
import self.micromagic.eterna.share.EternaFactory;
import self.micromagic.eterna.share.EternaFactoryImpl;
import self.micromagic.eterna.share.Tool;
import self.micromagic.eterna.share.FactoryContainer;
import self.micromagic.util.Utility;

/**
 * 公共的初始化规则集.
 *
 * @author micromagic@sina.com
 */
public class ShareSet extends RuleSetBase
{
	public ShareSet()
	{
	}

	public void addRuleInstances(Digester digester)
	{
		this.addSameCheckRule(digester);

		PropertySetter setter;
		Rule rule;

		//--------------------------------------------------------------------------------
		// 设置工厂的读取规则，默认的实现类为EternaFactoryImpl
		rule = new FactoryRegisterRule(EternaFactoryImpl.class.getName(), "type",
				EternaFactory.class, FactoryManager.ETERNA_FACTORY);
		digester.addRule("eterna-config/factory", rule);
		digester.addRule("eterna-config/factory/attributes/attribute",
				new AttributeSetRule());


		//--------------------------------------------------------------------------------
		// 构造UserManager
		digester.addRule("eterna-config/factory/user-manager",
				new ObjectCreateRule(null, "className", UserManager.class));
		setter = new StackPropertySetter("setUserManager", UserManager.class, 1);
		digester.addRule("eterna-config/factory/user-manager", new PropertySetRule(setter));
		digester.addRule("eterna-config/factory/user-manager",
				new ObjectLogRule("name", "UserManager"));


		//--------------------------------------------------------------------------------
		// 构造DataSourceManager
		setter = new DataSourceManagerSetter("className", "setDataSourceManager");
		digester.addRule("eterna-config/factory/data-source-manager", new PropertySetRule(setter));
		digester.addRule("eterna-config/factory/data-source-manager",
				new ObjectLogRule("name", "DataSourceManager"));

		setter = new AddDataSourceSetter("addDataSource");
		digester.addRule("eterna-config/factory/data-source-manager/data-source", new PropertySetRule(setter));

	}

	/**
	 * 添加相同对象检查的规则
	 */
	private void addSameCheckRule(Digester digester)
	{
		digester.addRule("eterna-config/factory/search-manager", new SameCheckRule("search-manager", null));
		digester.addRule("eterna-config/factory/special-log", new SameCheckRule("special-log", null));
		digester.addRule("eterna-config/factory/model-caller", new SameCheckRule("model-caller", null));
		digester.addRule("eterna-config/factory/string-coder", new SameCheckRule("string-coder", null));
		digester.addRule("eterna-config/factory/user-manager", new SameCheckRule("user-manager", null));
		digester.addRule("eterna-config/factory/data-source-manager", new SameCheckRule("data-source-manager", null));

		digester.addRule("eterna-config/factory/objs/constant", new SameCheckRule("constant", "name"));
		digester.addRule("eterna-config/factory/objs/vpc", new SameCheckRule("vpc", "name"));
		digester.addRule("eterna-config/factory/objs/format", new SameCheckRule("format", "name"));
		digester.addRule("eterna-config/factory/objs/query", new SameCheckRule("query", "name"));
		digester.addRule("eterna-config/factory/objs/update", new SameCheckRule("update", "name"));
		digester.addRule("eterna-config/factory/objs/reader-manager", new SameCheckRule("reader-manager", "name"));
		digester.addRule("eterna-config/factory/objs/parameter-group", new SameCheckRule("parameter-group", "name"));
		digester.addRule("eterna-config/factory/objs/builder", new SameCheckRule("builder", "name"));
		digester.addRule("eterna-config/factory/objs/builder-list", new SameCheckRule("builder-list", "name"));
		digester.addRule("eterna-config/factory/objs/search", new SameCheckRule("search", "name"));
		digester.addRule("eterna-config/factory/objs/model", new SameCheckRule("model", "name"));
		digester.addRule("eterna-config/factory/objs/export", new SameCheckRule("export", "name"));
		digester.addRule("eterna-config/factory/objs/view", new SameCheckRule("view", "name"));
		digester.addRule("eterna-config/factory/objs/data-printer", new SameCheckRule("data-printer", "name"));
		digester.addRule("eterna-config/factory/objs/typical-component", new SameCheckRule("typical-component", "name"));
		digester.addRule("eterna-config/factory/objs/typical-replacement", new SameCheckRule("typical-replacement", "name"));
		digester.addRule("eterna-config/factory/objs/function", new SameCheckRule("function", "name"));
		digester.addRule("eterna-config/factory/objs/resource", new SameCheckRule("resource", "name"));
	}

	public static DataSourceManager getDataSourceFromCache(FactoryContainer instance)
			throws ConfigurationException
	{
		Map dsMap = (Map) instance.getAttribute(DataSourceManager.DATA_SOURCE_MAP);
		if (dsMap != null && dsMap.size() > 0)
		{
			DataSourceManagerImpl dsmi = new DataSourceManagerImpl();
			String defaultDataSourceName = (String) instance.getAttribute(
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


	public static class DataSourceManagerSetter extends SinglePropertySetter
	{
		protected Class classType;

		public DataSourceManagerSetter(String attributeName, String methodName)
		{
			super(attributeName, methodName, DataSourceManagerImpl.class.getName());
			this.type = new Class[]{DataSourceManager.class};
		}

		public Object prepareProperty(String namespace, String name, Attributes attributes)
				throws Exception
		{
			String cName = ObjectCreateRule.getClassName(
					this.attributeName, this.defaultValue, attributes);

			this.digester.getLogger().debug("New " + cName);
			Object instance = ObjectCreateRule.createObject(cName);
			ObjectCreateRule.checkType(this.classType, instance);

			String defaultName = attributes.getValue("defaultName");
			if (defaultName == null)
			{
				throw new InvalidAttributesException("Not fount the attribute 'defaultName' in " + name + ".");
			}
			DataSourceManager dsm = (DataSourceManager) instance;
			dsm.setDefaultDataSourceName(defaultName);
			this.value = new Object[]{dsm};
			return dsm;
		}

	}

	public static class AddDataSourceSetter extends PropertySetter
	{
		protected Class[] types;
		protected Context context;
		protected String dataSourceConfig;

		public AddDataSourceSetter(String methodName)
		{
			super(methodName);
			this.types = new Class[]{Context.class, String.class};
		}

		public Object prepareProperty(String namespace, String name, Attributes attributes)
				throws Exception
		{
			java.util.Hashtable ht = new java.util.Hashtable();
			boolean needEnv = false;
			for (int i = 0; i < attributes.getLength(); i++)
			{
				ht.put(attributes.getQName(i), attributes.getValue(i));
			}
			if (!ht.contains(Context.INITIAL_CONTEXT_FACTORY))
			{
				String tmp = Utility.getProperty(Context.INITIAL_CONTEXT_FACTORY);
				if (tmp != null && tmp.length() > 0)
				{
					ht.put(Context.INITIAL_CONTEXT_FACTORY, tmp);
				}
			}
			String fName = (String) ht.get(Context.INITIAL_CONTEXT_FACTORY);
			if (fName != null)
			{
				if (fName.length() == 0)
				{
					// 如果INITIAL_CONTEXT_FACTORY的名字为空字符串, 则移除
					ht.remove(Context.INITIAL_CONTEXT_FACTORY);
					needEnv = true;
				}
				else
				{
					Object obj = ObjectCreateRule.createObject(fName, false);
					if (obj == null)
					{
						log.error("Not found Context.INITIAL_CONTEXT_FACTORY:[" + fName + "].");
						ht.remove(Context.INITIAL_CONTEXT_FACTORY);
						needEnv = true;
					}
				}
			}
			if (ht.size() > 0)
			{
				this.context = new InitialContext(ht);
			}
			else
			{
				this.context = new InitialContext();
				needEnv = true;
			}
			if (needEnv)
			{
				this.context = (Context) this.context.lookup("java:comp/env");
			}
			return this.context;
		}

		public boolean isMustExist()
		{
			return true;
		}

		public boolean requireBodyValue()
		{
			return true;
		}

		public Object prepareProperty(String namespace, String name, BodyText text)
				throws Exception
		{
			this.dataSourceConfig = text.trimEveryLineSpace(false);
			return this.dataSourceConfig;
		}

		public void setProperty()
				throws Exception
		{
			Object obj = this.digester.peek(this.objectIndex);
			Object[] values = new Object[]{this.context, this.dataSourceConfig};
			try
			{
				Tool.invokeExactMethod(obj, this.methodName, values, this.types);
			}
			catch (Exception ex)
			{
				log.error("Method invoke error. method:" + this.methodName + "  param:"
						+ Arrays.asList(this.types) + "  obj:" + (obj == null ? null : obj.getClass())
						+ "  value:" + Arrays.asList(values));
				throw ex;
			}
		}

	}

	public static class DataSourceManagerImpl
			implements DataSourceManager
	{
		private String defaultDataSourceName = null;
		private DataSource defaultDataSource = null;

		private Map dataSourceMap = null;

		/**
		 * 初始化这个DataSourceManager.
		 */
		public void initialize(EternaFactory factory)
				throws ConfigurationException
		{
			if (this.dataSourceMap == null)
			{
				throw new ConfigurationException("Not registe any data source.");
			}
			if (this.defaultDataSourceName == null)
			{
				throw new ConfigurationException("Must give this default data source name.");
			}
			this.defaultDataSource = (DataSource) this.dataSourceMap.get(this.defaultDataSourceName);
			if (this.defaultDataSource == null)
			{
				throw new ConfigurationException("Not found the data source:" + this.defaultDataSourceName + ".");
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
				throws ConfigurationException
		{
			return this.defaultDataSourceName;
		}

		public void setDefaultDataSourceName(String name)
				throws ConfigurationException
		{
			if (this.defaultDataSource != null)
			{
				throw new ConfigurationException("Can't set default data source name after Initialization.");
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
				throws ConfigurationException
		{
			StringTokenizer token = new StringTokenizer(Utility.resolveDynamicPropnames(dataSourceConfig), ";");
			try
			{
				while (token.hasMoreTokens())
				{
					if (this.dataSourceMap == null)
					{
						this.dataSourceMap = new HashMap();
					}
					String entry = token.nextToken().trim();
					if (entry.length() > 0)
					{
						int index = entry.indexOf('=');
						if (index == -1)
						{
							throw new ConfigurationException("Error DataSource define:" + entry + ".");
						}
						String key = entry.substring(0, index).trim();
						if (this.dataSourceMap.containsKey(key))
						{
							throw new ConfigurationException("Duplicate DataSource name:" + key + ".");
						}
						String name = entry.substring(index + 1).trim();
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
				FactoryManager.log.error("Error when get jdbc in jndi.", ex);
				throw new ConfigurationException(ex);
			}
		}

	}

}