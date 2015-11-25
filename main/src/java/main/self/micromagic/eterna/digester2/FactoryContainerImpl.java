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

import self.micromagic.eterna.share.AbstractFactoryContainer;
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
 * 工厂容器的实现类.
 */
public class FactoryContainerImpl extends AbstractFactoryContainer
		implements FactoryContainer
{
	/**
	 * 构造一个工厂容器.
	 */
	public FactoryContainerImpl()
	{
	}

	/**
	 * 构造一个工厂容器, 并指定其编号.
	 */
	public FactoryContainerImpl(String id)
	{
		this.id = id;
	}

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

	public synchronized void reInit(StringRef msg)
	{
		this.initialized = false;
		this.initErrMsg = null;
		FactoryContainer oldC = ContainerManager.getCurrentContainer();
		Factory oldF = ContainerManager.getCurrentFactory();
		ConfigResource oldR = ContainerManager.getCurrentResource();
		ContainerManager.setCurrentFactory(null);
		ContainerManager.setCurrentContainer(this, false);
		try
		{
			this.destroy();
			ContainerManager.resetSerial();
			ParseException.clearContextInfo();
			SameCheck.clearDealedMap();
			ContainerManager.clearSettedAttribute(this);
			this.resources.clear();
			this.readReloadTime();
			this.initializeXML();
			Factory factory = ContainerManager.getCurrentFactory();
			Factory shareFactory = this.shareContainer == null ?
					null : this.shareContainer.getFactory();
			// 在初始化执行前先对factory赋值, 这样可以在初始化期间获取对象
			this.factory = factory;
			factory.initialize(this, shareFactory);
			this.initializeElse();
			this.initialized = true;
		}
		catch (Throwable ex)
		{
			if (this.factory != null)
			{
				// 如果初始化时失败, 需要销毁工厂
				try
				{
					this.factory.destroy();
				}
				catch (Throwable err) {}
				this.factory = null;
			}
			Digester.log.error("Error in initialize [" + this.getId() + "].", ex);
			String tmpMsg = ParseException.getMessage(ex);
			this.initErrMsg = tmpMsg;
			if (msg != null)
			{
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
					if (tmp != null)
					{
						ParseException.clearContextInfo();
						ConfigResource cr = ContainerManager.getCurrentResource();
						ParseException.setContextInfo(cr.getURI(), include.getSrc());
						this.parseXML(tmp, cr);
					}
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
			return;
		}
		long time = (new LongConverter()).convertToLong(tmpTime);
		if (time > 0L)
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
		if (this.reloadTime != -1L)
		{
			// 设置了重载时间需要进行重载检查
			this.checkReload();
		}
		Factory f = this.factory;
		if (f != null)
		{
			return f;
		}
		if (!this.initialized)
		{
			synchronized (this)
			{
				if (!this.initialized)
				{
					// 同步状态下再次检查是否已初始化
					String msg = "The factory container [" + this.getId()
							+ "] hasn't initialized.";
					if (!StringTool.isEmpty(this.initErrMsg))
					{
						msg += " Error message:" + this.initErrMsg + ".";
					}
					throw new EternaException(msg);
				}
				f = this.factory;
			}
		}
		return f;
	}
	private Factory factory;
	private String initErrMsg;

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
							Digester.log.info("Auto reload [" + this.getId() + "] at time:"
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
		Factory tmp = this.factory;
		if (tmp != null)
		{
			this.factory = null;
			tmp.destroy();
		}
	}

}