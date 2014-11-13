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

package self.micromagic.app;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import self.micromagic.eterna.digester.ConfigurationException;
import self.micromagic.eterna.model.AppData;
import self.micromagic.eterna.model.Execute;
import self.micromagic.eterna.model.ModelAdapter;
import self.micromagic.eterna.model.ModelExport;
import self.micromagic.eterna.model.impl.AbstractExecute;
import self.micromagic.eterna.share.EternaFactory;
import self.micromagic.eterna.share.Generator;
import self.micromagic.cg.ClassGenerator;
import self.micromagic.util.ObjectRef;
import org.apache.commons.logging.Log;

/**
 * 基础执行器. <p>
 * 实现了<code>self.micromagic.eterna.model.Execute</code>接口.
 * 特殊初始化内容, 可重写plusInit方法.
 * 具体的业务逻辑, 可重写dealProcess方法.
 */
public class BaseExecute extends AbstractExecute
		implements Execute, Generator
{
	/**
	 * 标识是否是使用数据集中的指定值来作为属性值.
	 */
	public static final String DATA_ATTRIBUTE_NAME_PREFIX = "$data.";

	/**
	 * 此属性会在初始化时赋值, 可在需要时使用.
	 */
	protected EternaFactory factory;

	/**
	 * 默认值在初始化时设置, 为当前的类名.
	 */
	protected String executeType;

	/**
	 * 日志记录.
	 */
	protected static final Log log = WebApp.log;

	/**
	 * 实现了<code>self.micromagic.eterna.model.Execute</code>接口的初始化方法.
	 * 如果还有特殊初始化内容, 可重写plusInit方法.
	 *
	 * @see #plusInit
	 */
	public void initialize(ModelAdapter model)
			throws ConfigurationException
	{
		if (this.initialized)
		{
			return;
		}
		super.initialize(model);
		this.factory = model.getFactory();
		this.executeType = "class:" + ClassGenerator.getClassName(this.getClass());
		this.plusInit();
	}

	/**
	 * 特殊初始化内容可重写此方法实现.
	 */
	protected void plusInit()
			throws ConfigurationException
	{
	}

	/**
	 * 实现了<code>self.micromagic.eterna.model.Execute</code>接口的方法,
	 * 返回当前执行器的类型.
	 */
	public String getExecuteType()
			throws ConfigurationException
	{
		return this.executeType;
	}

	/**
	 * 实现了<code>self.micromagic.eterna.model.Execute</code>接口的执行方法,
	 * 具体的业务逻辑, 可重写dealProcess方法.
	 *
	 * @see #plusInit
	 */
	public ModelExport execute(AppData data, Connection conn)
			throws ConfigurationException, SQLException, IOException
	{
		try
		{
			return this.dealProcess(data, conn);
		}
		catch (InnerExport e)
		{
			return e.export;
		}
	}

	/**
	 * 具体的业务逻辑可重写此方法实现.
	 */
	protected ModelExport dealProcess(AppData data, Connection conn)
			throws ConfigurationException, SQLException, IOException, InnerExport
	{
		return null;
	}

	/**
	 * 通过指定export的名称来执行跳转.
	 *
	 * @param exportName    执行跳转的export的名称
	 */
	protected ModelExport doExport(String exportName)
			throws ConfigurationException, SQLException, IOException, InnerExport
	{
		ModelExport export = this.factory.getModelExport(exportName);
		if (export == null)
		{
				log.warn("The ModelExport [" + exportName + "] not found.");
		}
		else
		{
			throw new InnerExport(export);
		}
		return export;
	}

	/**
	 * 通过指定名称来调用一个model.
	 *
	 * @param modelName    要调用的model的名称
	 */
	protected ModelExport callModel(AppData data, Connection conn, String modelName)
			throws ConfigurationException, SQLException, IOException, InnerExport
	{
		return this.callModel(data, conn, modelName, false);
	}

	/**
	 * 通过指定名称来调用一个model.
	 *
	 * @param modelName    要调用的model的名称
	 * @param noJump       设为<code>true</code>, 则任何情况都不会跳出
	 */
	protected ModelExport callModel(AppData data, Connection conn, String modelName, boolean noJump)
			throws ConfigurationException, SQLException, IOException, InnerExport
	{
		ObjectRef preConn = (ObjectRef) data.getSpcialData(ModelAdapter.MODEL_CACHE, ModelAdapter.PRE_CONN);
		ModelAdapter tmpModel = this.factory.createModelAdapter(modelName);
		int tType = tmpModel.getTransactionType();
		if (noJump)
		{
			try
			{
				return this.factory.getModelCaller().callModel(data, tmpModel, null, tType, preConn);
			}
			catch (Throwable ex)
			{
				log.error("Error in call model", ex);
				return null;
			}
		}
		else
		{
			ModelExport export = this.factory.getModelCaller().callModel(data, tmpModel, null, tType, preConn);
			if (export != null)
			{
				throw new InnerExport(export);
			}
			return null;
		}
	}

	/**
	 * 如果调用model后需要立刻转向一个<code>ModelExport</code>, 可以抛出此异常.
	 */
	protected static class InnerExport extends RuntimeException
	{
		/**
		 * 要立刻转向的<code>ModelExport</code>.
		 */
		public final ModelExport export;

		public InnerExport(ModelExport export)
		{
			this.export = export;
		}

	}

}