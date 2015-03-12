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

package self.micromagic.eterna;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import self.micromagic.eterna.digester2.ContainerManager;
import self.micromagic.eterna.model.AppData;
import self.micromagic.eterna.model.ModelExport;
import self.micromagic.eterna.share.EternaException;
import self.micromagic.eterna.share.EternaFactory;
import self.micromagic.eterna.share.FactoryContainer;
import self.micromagic.util.Jdk14Factory;
import self.micromagic.util.Utility;
import self.micromagic.util.container.ThreadCache;
import self.micromagic.util.ref.ObjectRef;

/**
 * 测试的工具.
 */
public class TestTool
{
	/**
	 * 使用一个Map对象中的数据作为请求的参数来构造一个AppData.
	 *
	 * @param param   存放请求的参数的Map对象
	 */
	public static AppData getAppData(Map param)
	{
		return getAppData(param, 0, null);
	}

	/**
	 * 使用一个Map对象中的数据作为请求的参数来构造一个AppData.
	 *
	 * @param param            存放请求的参数的Map对象
	 * @param appendPosition   添加额外的当前位置信息
	 * @param oldAppData       一个出参, 如果传入将会把原来的AppData备份到里面
	 *                         如果参数对象相同或原来没有AppData, 则不会备份
	 */
	public static AppData getAppData(Map param, int appendPosition, ObjectRef oldAppData)
	{
		AppData tmpData = AppData.getCurrentData();
		if (oldAppData != null)
		{
			if (tmpData.maps[AppData.REQUEST_PARAMETER_MAP] != null
					&& tmpData.maps[AppData.REQUEST_PARAMETER_MAP] != param)
			{
				oldAppData.setObject(tmpData);
				tmpData = new AppData();
				ThreadCache.getInstance().setProperty(AppData.CACHE_NAME, tmpData);
			}
		}
		if (tmpData.maps[AppData.REQUEST_PARAMETER_MAP] != param)
		{
			tmpData.clearData();
			tmpData.position |= appendPosition;
			tmpData.maps[AppData.REQUEST_PARAMETER_MAP] = param == null ? new HashMap() : param;
			tmpData.maps[AppData.REQUEST_ATTRIBUTE_MAP] = new HashMap();
			tmpData.maps[AppData.SESSION_ATTRIBUTE_MAP] = new HashMap();
		}
		return tmpData;
	}

	protected Map parameterMap = new HashMap();
	protected Class baseClass;

	public TestTool()
	{
		this("");
	}

	public TestTool(String globalConfig)
	{
		this.baseClass = this.getClass();
		Utility.setProperty(Jdk14Factory.USE_JDK_LOG_FLAG, "true");
		Utility.setProperty("self.micromagic.defaultLogger.console.off", "false");
		Utility.setProperty("self.micromagic.defaultLogger.console.delay_time", "-1");
		Utility.setProperty("self.micromagic.defaultLogger.level", "INFO");
		//Utility.setProperty(FactoryManager.CHECK_GRAMMER_PROPERTY, "false");
		Utility.setProperty(ContainerManager.INIT_CONFIG_FLAG, globalConfig);
		this.globalConfig = globalConfig;
	}
	private final String globalConfig;

	public void setBaseClass(Class baseClass)
	{
		this.baseClass = baseClass;
	}

	public void setSubGlobalConfig(String config)
	{
		Utility.setProperty(ContainerManager.INIT_PARENT_PREFIX + "1", globalConfig);
		Utility.setProperty(ContainerManager.INIT_CONFIG_FLAG, config);
	}

	public EternaFactory getEternaFactory()
			throws EternaException
	{
		FactoryContainer fc = ContainerManager.createFactoryContainer(this.baseClass);
		return (EternaFactory) fc.getFactory();
	}

	public EternaFactory getEternaFactory(String config, boolean registry)
			throws EternaException
	{
		String id = this.baseClass.getName();
		FactoryContainer share = ContainerManager.getGlobalContainer();
		FactoryContainer fc = ContainerManager.createFactoryContainer(id, config, null,
				null, null, this.baseClass.getClassLoader(), share, false);
		if (registry)
		{
			ContainerManager.registerFactoryContainer(fc, true);
		}
		return (EternaFactory) fc.getFactory();
	}

	public void setParameter(String name, String value)
	{
		this.parameterMap.put(name, value);
	}

	public void setParameterValues(String name, String[] values)
	{
		this.parameterMap.put(name, values);
	}

	public void clearParameter()
	{
		this.parameterMap.clear();
	}

	public AppData getAppData()
	{
		AppData data = AppData.getCurrentData();
		data.clearData();
		data.position = AppData.POSITION_SERVLET;
		data.maps[AppData.REQUEST_PARAMETER_MAP] = this.parameterMap;
		data.maps[AppData.REQUEST_ATTRIBUTE_MAP] = new HashMap();
		data.maps[AppData.SESSION_ATTRIBUTE_MAP] = new HashMap();
		return data;
	}

	public ModelExport callModel(EternaFactory factory, String modelName)
			throws EternaException, SQLException, IOException
	{
		AppData data = AppData.getCurrentData();
		data.modelName = modelName;
		return factory.getModelCaller().callModel(data);
	}

}