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

package self.micromagic.eterna.digester2;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.dom4j.DocumentException;

import self.micromagic.eterna.share.AttributeManager;
import self.micromagic.eterna.share.ConfigInclude;
import self.micromagic.eterna.share.EternaException;
import self.micromagic.eterna.share.Factory;
import self.micromagic.eterna.share.FactoryContainer;
import self.micromagic.eterna.share.InitializeListener;
import self.micromagic.util.FormatTool;
import self.micromagic.util.StringAppender;
import self.micromagic.util.StringTool;
import self.micromagic.util.Utility;
import self.micromagic.util.converter.LongConverter;
import self.micromagic.util.ref.StringRef;

/**
 *
 */
public class FactoryContainerImpl
		implements FactoryContainer
{
	public String getId()
	{
		return this.id;
	}
	void setId(String id)
	{
		this.id = id;
	}
	private String id;

	void setConfig(String config, String[] parents)
	{
		this.config = config;
		this.parents = parents;
	}
	private String config;
	private String[] parents;

	public void reInit()
	{
		this.reInit(null);
	}

	public synchronized void reInit(StringRef msg)
	{
		this.initialized = false;
		FactoryContainer oldC = ContainerManager.getCurrentContainer();
		Factory oldF = ContainerManager.getCurrentFactory();
		ConfigResource oldR = ContainerManager.getCurrentResource();
		ContainerManager.setCurrentFactory(null);
		ContainerManager.setCurrentContainer(this, false);
		try
		{
			this.destroy();
			ParseException.clearContextInfo();
			SameCheck.clearDealedMap();
			this.resources.clear();
			this.readReloadTime();
			this.initializeXML();
			Factory factory = ContainerManager.getCurrentFactory();
			Factory shareFactory = this.shareContainer == null ?
					null : this.shareContainer.getFactory();
			factory.initialize(this, shareFactory);
			this.factory = factory;
			this.initializeElse();
			this.initialized = true;
		}
		catch (Throwable ex)
		{
			Digester.log.error("Error in initialize [" + this.id + "].", ex);
			if (msg != null)
			{
				String tmpMsg = ParseException.getMessage(ex);
				if (msg.getString() != null)
				{
					StringAppender tmpBuf = StringTool.createStringAppender();
					tmpBuf.append(msg.getString()).appendln().append(tmpMsg);
					tmpMsg = tmpBuf.toString();
				}
				msg.setString(tmpMsg);
			}
		}
		finally
		{
			ContainerManager.setCurrentResource(oldR);
			ContainerManager.setCurrentFactory(oldF);
			ContainerManager.setCurrentContainer(oldC, false);
			SameCheck.clearDealedMap();
			ParseException.clearContextInfo();
		}
	}

	public boolean isInitialized()
	{
		return this.initialized;
	}
	private boolean initialized;

	/**
	 * 解析配置信息并分别进行初始化.
	 */
	protected void initializeXML()
			throws Throwable
	{
		Thread currentThread = Thread.currentThread();
		ClassLoader oldCL = currentThread.getContextClassLoader();
		currentThread.setContextClassLoader(this.loader);
		try
		{
			this.dealXML(this.config);
			if (this.parents != null)
			{
				for (int i = 0; i < this.parents.length; i++)
				{
					if (this.parents[i] != null)
					{
						// 打开父配置正在初始化的标志, 将父配置初始化进来
						ContainerManager.setSuperInitLevel(i + 1);
						try
						{
							this.dealXML(this.parents[i]);
						}
						finally
						{
							ContainerManager.setSuperInitLevel(0);
						}
					}
				}
			}
		}
		finally
		{
			// 清除已加载的资源地址缓存
			this.removeAttribute(URIS_FLAG);
			currentThread.setContextClassLoader(oldCL);
		}
	}

	/**
	 * 根据配置读取xml流并进行初始化.
	 *
	 * @param config  配置信息
	 */
	protected void dealXML(String config)
			throws IOException, EternaException, DocumentException
	{
		StringTokenizer token = new StringTokenizer(this.resolveConfig(config), ";");
		while (token.hasMoreTokens())
		{
			String temp = token.nextToken().trim();
			if (temp.length() == 0)
			{
				continue;
			}
			ConfigResource cr = ContainerManager.createResource(temp, this);
			if (ContainerManager.checkResourceURI(cr.getURI()))
			{
				// 如果资源已被加载过, 则不进行初始化.
				continue;
			}
			if (this.reloadTime != -1L)
			{
				// 如果需要检查并重新载入, 需要保存配置资源
				this.resources.add(cr);
			}
			InputStream in = cr.getAsStream();
			if (in != null)
			{
				if (Digester.log.isDebugEnabled())
				{
					Digester.log.debug("The config is \"" + temp + "\".");
				}
				ContainerManager.setCurrentResource(cr);
				ParseException.setContextInfo(cr.getURI(), temp);
				this.parseXML(in, cr);
			}
			else
			{
				Digester.log.info("The config \"" + temp + "\" isn't avilable.");
			}
		}
	}
	/**
	 * 根据给出的InputStream或Reader进行xml解析.
	 *
	 * @param src  InputStream或Reader
	 */
	protected void parseXML(Object src, ConfigResource res)
			throws IOException, EternaException, DocumentException
	{
		try
		{
			if (src instanceof InputStream)
			{
				InputStream in = (InputStream) src;
				this.digester.parse(in);
				in.close();
			}
			else
			{
				Reader reader = (Reader) src;
				this.digester.parse(reader);
				reader.close();
			}
			// 处理需要引用的配置.
			List includes = (List) this.getAttribute(ConfigInclude.INCLUDE_LIST_FLAG);
			if (includes != null)
			{
				this.removeAttribute(ConfigInclude.INCLUDE_LIST_FLAG);
				int count = includes.size();
				Iterator itr = includes.iterator();
				for (int i = 0; i < count; i++)
				{
					ConfigInclude include = (ConfigInclude) itr.next();
					Object tmp = include.getIncludeRes(res);
					ConfigResource cr = ContainerManager.getCurrentResource();
					ParseException.setContextInfo(cr.getURI(), include.getSrc());
					this.parseXML(tmp, cr);
				}
			}
		}
		finally
		{
			ParseException.clearContextInfo();
		}
	}
	private final List resources = new ArrayList(6);

	/**
	 * 触发注册的监听器等剩余处理.
	 */
	protected void initializeElse()
	{
		this.fireListener();
		if (this.reloadTime != -1L)
		{
			// 如果需要检查并重新载入, 需要跟新相关时间
			long time = System.currentTimeMillis();
			if (this.preInitTime < time)
			{
				this.preCheckTime = this.preInitTime = time;
			}
		}
	}

	/**
	 * 处理配置中的引用信息.
	 */
	private String resolveConfig(String config)
	{
		Map param = (Map) this.getAttribute(CONFIG_PARAM_FLAG);
		return Utility.resolveDynamicPropnames(config, param);
	}

	void setDigester(Digester digester)
	{
		this.digester = digester;
	}
	private Digester digester;
	void setClassLoader(ClassLoader loader)
	{
		this.loader = loader;
		this.setAttribute(CLASSLOADER_FLAG, loader);
	}
	private ClassLoader loader;

	/**
	 * 设置共享的工厂容器.
	 */
	protected void setShareContainer(FactoryContainer shareContainer)
	{
		this.shareContainer = shareContainer;
	}
	protected FactoryContainer shareContainer;

	public void setAttribute(String name, Object attr)
	{
		this.attrs.setAttribute(name, attr);
	}
	public void removeAttribute(String name)
	{
		this.attrs.removeAttribute(name);
	}
	public Object getAttribute(String name)
	{
		return this.attrs.getAttribute(name);
	}
	protected AttributeManager attrs = new AttributeManager();

	private void fireListener()
	{
		int count = this.listeners.size();
		Iterator itr = this.listeners.iterator();
		for (int i = 0; i < count; i++)
		{
			((InitializeListener) itr.next()).afterInitialize(this);
		}
	}
	public void addInitializeListener(InitializeListener l)
	{
		this.listeners.add(l);
	}
	private final List listeners = new ArrayList(2);

	/**
	 * 读取最小重新载入配置资源的时间间隔.
	 */
	private void readReloadTime()
	{
		Object tmpTime = this.getAttribute(RELOAD_TIME_FLAG);
		if (tmpTime == null)
		{
			this.reloadTime = -1L;
		}
		long time = (new LongConverter()).convertToLong(tmpTime);
		if (time >= 0L)
		{
			this.reloadTime = time < 200L ? 200L : time;
		}
		else
		{
			this.reloadTime = -1L;
		}
	}
	private long reloadTime = -1L;
	/**
	 * 前一次检查配置资源更新的时间
	 */
	private long preCheckTime = 0L;
	/**
	 * 前一次执行初始化的时间
	 */
	private long preInitTime = 0L;

	public Factory getFactory()
	{
		if (this.factory != null && this.reloadTime != -1)
		{
			this.checkReload();
		}
		if (!this.initialized)
		{
			synchronized (this)
			{
				if (!this.initialized)
				{
					// 同步状态下再次检查是否已初始化
					throw new EternaException("The factory container ["
							+ this.id + "] hasn't initialized.");
				}
			}
		}
		return this.factory;
	}
	private Factory factory;


	private void checkReload()
	{
		if (System.currentTimeMillis() - this.reloadTime > this.preCheckTime)
		{
			boolean needReload = false;
			int count = this.resources.size();
			Iterator itr = this.resources.iterator();
			for (int i = 0; i < count; i++)
			{
				long lm = ((ConfigResource) itr.next()).getLastModified();
				if (lm > this.preInitTime)
				{
					needReload = true;
					this.preInitTime = lm;
					break;
				}
			}
			if (needReload)
			{
				synchronized (this)
				{
					// 再次检查前一次检测时间, 如果未到说明已在其他线程中重载了
					if (System.currentTimeMillis() - this.reloadTime > this.preCheckTime)
					{
						StringRef msg = new StringRef();
						this.reInit(msg);
						if (Digester.log.isInfoEnabled())
						{
							Digester.log.info("Auto reload [" + this.id + "] at time:"
									+ FormatTool.getCurrentDatetimeString()
									+ ". with message:" + msg.toString());
						}
					}
				}
			}
			this.preCheckTime = System.currentTimeMillis();
		}
	}

	public void destroy()
	{
		if (this.factory != null)
		{
			this.factory.destroy();
			this.factory = null;
		}
	}

}