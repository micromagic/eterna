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

package self.micromagic.eterna.model.impl;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import org.dom4j.Element;

import self.micromagic.cg.ClassGenerator;
import self.micromagic.eterna.model.AppData;
import self.micromagic.eterna.model.Execute;
import self.micromagic.eterna.model.ModelAdapter;
import self.micromagic.eterna.model.ModelExport;
import self.micromagic.eterna.share.AbstractGenerator;
import self.micromagic.eterna.share.EternaException;
import self.micromagic.eterna.share.EternaFactory;
import self.micromagic.eterna.share.ObjectCreater;
import self.micromagic.util.ObjectRef;

/**
 * @author micromagic@sina.com
 */
public class ModelAdapterImpl extends AbstractGenerator
		implements ModelAdapter, ObjectCreater
{
	private boolean needFrontModel = true;
	private boolean keepCaches = false;
	private String frontModelName = null;
	private String modelExportName = null;
	private String errorExportName = null;
	private String dataSourceName = null;
	private int transactionType = T_REQUARED;
	private ModelExport modelExport = null;
	private ModelExport errorExport = null;
	private int frontModelIndex = -1;

	private int allowPositions = AppData.POSITION_SERVLET + AppData.POSITION_PORTLET_RENDER;

	protected List executes = new LinkedList();

	protected boolean initialized = false;

	public boolean initialize(EternaFactory factory)
			throws EternaException
	{
		if (this.initialized)
		{
			return true;
		}
		this.initialized = true;
		if (this.modelExportName != null)
		{
			this.modelExport = factory.getModelExport(this.modelExportName);
			if (this.modelExport == null)
			{
				log.warn("The Model Export [" + this.modelExportName + "] not found.");
			}
		}
		if (this.errorExportName != null)
		{
			if (!"".equals(this.errorExportName))
			{
				this.errorExport = factory.getModelExport(this.errorExportName);
				if (this.errorExport == null)
				{
					log.warn("The Model Export [" + this.errorExportName + "] not found.");
				}
			}
		}
		else
		{
			String tmpName = (String) factory.getAttribute(DEFAULT_ERROR_EXPORT_NAME);
			if (tmpName != null)
			{
				this.errorExport = factory.getModelExport(tmpName);
				if (this.errorExport == null)
				{
					log.warn("The Model Export [" + tmpName + "] not found.");
				}
			}
		}
		Iterator itr = this.executes.iterator();
		while (itr.hasNext())
		{
			Execute execute = (Execute) itr.next();
			execute.initialize(this);
		}
		if (this.needFrontModel)
		{
			if (this.frontModelName != null)
			{
				this.frontModelIndex = factory.findObjectId(this.frontModelName);
			}
			else
			{
				String tmpName = (String) factory.getAttribute(FRONT_MODEL_ATTRIBUTE);
				if (tmpName != null && tmpName.length() > 0)
				{
					this.frontModelIndex = factory.findObjectId(tmpName);
				}
			}
		}
		//log.info("Model:" + this.getName() + "," + this.allowPositions);

		return false;
	}

	public EternaFactory getFactory()
	{
		return (EternaFactory) this.factory;
	}

	public boolean isKeepCaches()
	{
		return this.keepCaches;
	}

	public void setKeepCaches(boolean keepCaches)
	{
		this.keepCaches = keepCaches;
	}

	public String getFrontModelName()
	{
		return this.frontModelName;
	}

	public void setFrontModelName(String frontModelName)
	{
		this.frontModelName = frontModelName;
	}

	public boolean isNeedFrontModel()
	{
		return this.needFrontModel;
	}

	public void setNeedFrontModel(boolean needFrontModel)
	{
		this.needFrontModel = needFrontModel;
	}

	public ModelExport doModel(AppData data, Connection conn)
			throws EternaException, SQLException, IOException
	{
		if (!this.checkPosition(data))
		{
			log.warn("The model [" + this.getName() + "] isn't execute because the position.");
			return null;
		}
		boolean hasModelPos = (data.position & AppData.POSITION_MODEL) != 0;
		// 如果原先不存在model的位置则添加
		if (!hasModelPos)
		{
			data.position |= AppData.POSITION_MODEL;
		}
		Object[] caches = null;
		if (this.keepCaches)
		{
			caches = new Object[data.caches.length];
			System.arraycopy(data.caches, 0, caches, 0, caches.length);
		}
		Element beginNode = null;
		if (data.getLogType() > 0)
		{
			beginNode = data.beginNode("model", this.getName(), null);
		}
		ObjectRef errorRef = new ObjectRef();
		try
		{
			if (this.frontModelIndex != -1)
			{
				Object tmp = data.getSpcialData(MODEL_CACHE, "frontModel.executeIn");
				if (tmp == null)
				{
					data.addSpcialData(MODEL_CACHE, "frontModel.executeIn", this);
					ModelAdapter tmpModel = this.getFactory().createModelAdapter(this.frontModelIndex);
					ObjectRef preConn = (ObjectRef) data.getSpcialData(MODEL_CACHE, PRE_CONN);
					ModelExport export = this.getFactory().getModelCaller().callModel(
							data, tmpModel, null, tmpModel.getTransactionType(), preConn);
					if (export != null)
					{
						data.export = export;
						return export;
					}
				}
			}
			return this.doModelExt(data, conn, beginNode != null, errorRef);
		}
		finally
		{
			// 如果原先不存在model的位置则去除
			if (!hasModelPos)
			{
				data.position |= AppData.POSITION_MODEL;
				data.position ^= AppData.POSITION_MODEL;
			}
			if (beginNode != null)
			{
				data.endNode(beginNode, (Throwable) errorRef.getObject(), data.export);
			}
			if (this.keepCaches)
			{
				System.arraycopy(caches, 0, data.caches, 0, caches.length);
			}
		}
	}

	public Execute getExecute(int index)
			throws EternaException
	{
		if (index < 1 || index > this.executes.size())
		{
			throw new EternaException("Error execute index:" + index + ".");
		}
		return (Execute) this.executes.get(index - 1);
	}

	protected ModelExport doModelExt(AppData data, Connection conn, boolean needLogApp, ObjectRef errorRef)
			throws EternaException, SQLException, IOException
	{
		boolean executed = false;
		int index = 0;
		String executeType = "";
		ModelExport tmpExport = null;
		Element el = null;
		try
		{
			Iterator itr = this.executes.iterator();
			while (itr.hasNext())
			{
				Execute execute = (Execute) itr.next();
				executeType = execute.getExecuteType();
				if (needLogApp)
				{
					el = data.beginNode("execute", execute.getExecuteType(),
							"index:" + (index + 1) + ", name:" + execute.getName());
					el.addAttribute("class", ClassGenerator.getClassName(execute.getClass()));
				}
				ModelExport export = execute.execute(data, conn);
				if (needLogApp)
				{
					data.endNode(el, null, export);
				}
				if (export != null)
				{
					tmpExport = export;
					executed = true;
					return export;
				}
				index++;
			}
			executed = true;
		}
		catch (EternaException ex)
		{
			errorRef.setObject(ex);
			throw ex;
		}
		catch (SQLException ex)
		{
			errorRef.setObject(ex);
			throw ex;
		}
		catch (RuntimeException ex)
		{
			errorRef.setObject(ex);
			throw ex;
		}
		catch (Error ex)
		{
			errorRef.setObject(ex);
			throw ex;
		}
		finally
		{
			if (!executed)
			{
				if (needLogApp)
				{
					// 出错时需要多设置一次结束, 因为在循环中的那个被跳过了
					data.endNode(el, (Throwable) errorRef.getObject(), tmpExport);
				}
				log.error("Error in model:" + this.getName() + ", executeType:" + executeType
						+ ", executeIndex:" + (index + 1) + ", id:" + data.getAppId());
			}
			if (tmpExport == null)
			{
				tmpExport = executed ? this.getModelExport() : this.getErrorExport();
			}
			data.export = tmpExport;
		}
		return tmpExport;
	}

	public int getTransactionType()
	{
		return this.transactionType;
	}

	public void setTransactionType(String tType)
			throws EternaException
	{
		this.transactionType = parseTransactionType(tType);
	}

	public String getDataSourceName()
	{
		return this.dataSourceName;
	}

	public void setDataSourceName(String dsName)
	{
		this.dataSourceName = dsName;
	}

	public boolean checkPosition(AppData data)
	{
		return (data.position & this.allowPositions) != 0;
	}

	public void setAllowPosition(String positions)
	{
		this.allowPositions = 0;
		StringTokenizer token = new StringTokenizer(positions, ",");
		while (token.hasMoreTokens())
		{
			String pos = token.nextToken().trim();
			if ("".equals(pos))
			{
				continue;
			}
			if ("servlet".equals(pos))
			{
				this.allowPositions |= AppData.POSITION_SERVLET;
			}
			else if ("portletAction".equals(pos))
			{
				this.allowPositions |= AppData.POSITION_PORTLET_ACTION;
			}
			else if ("portletRender".equals(pos))
			{
				this.allowPositions |= AppData.POSITION_PORTLET_RENDER;
			}
			else if ("special".equals(pos))
			{
				this.allowPositions |= AppData.POSITION_SPECIAL;
			}
			else if ("model".equals(pos))
			{
				this.allowPositions |= AppData.POSITION_MODEL;
			}
			else if ("other1".equals(pos) || "other".equals(pos))
			{
				this.allowPositions |= AppData.POSITION_OTHER1;
			}
			else if ("other2".equals(pos))
			{
				this.allowPositions |= AppData.POSITION_OTHER2;
			}
			else if ("other3".equals(pos))
			{
				this.allowPositions |= AppData.POSITION_OTHER3;
			}
		}
	}

	public ModelExport getModelExport()
	{
		return this.modelExport;
	}

	public ModelExport getErrorExport()
	{
		return this.errorExport;
	}

	public void setModelExportName(String name)
	{
		this.modelExportName = name;
	}

	public void setErrorExportName(String name)
	{
		this.errorExportName = name;
	}

	public void addExecute(Execute execute)
			throws EternaException
	{
		this.executes.add(execute);
	}

	public Object create() throws EternaException
	{
		return this.createModelAdapter();
	}

	public ModelAdapter createModelAdapter()
	{
		return this;
	}

	public static int parseTransactionType(String tType)
			throws EternaException
	{
		if ("requared".equals(tType))
		{
			return T_REQUARED;
		}
		else if ("new".equals(tType))
		{
			return T_NEW;
		}
		else if ("hold".equals(tType))
		{
			return T_HOLD;
		}
		else if ("none".equals(tType))
		{
			return T_NONE;
		}
		else if ("notNeed".equals(tType))
		{
			return T_NOTNEED;
		}
		else if ("idel".equals(tType))
		{
			return T_IDLE;
		}
		throw new EternaException("Error transaction type:" + tType + ".");
	}


	public Class getObjectType()
	{
		return ModelAdapterImpl.class;
	}

	public boolean isSingleton()
	{
		return true;
	}

	public void destroy()
	{
		Iterator itr = this.executes.iterator();
		while (itr.hasNext())
		{
			Execute execute = (Execute) itr.next();
			execute.destroy();
		}
	}

}