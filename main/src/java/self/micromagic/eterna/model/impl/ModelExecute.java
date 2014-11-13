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

import self.micromagic.eterna.digester.ConfigurationException;
import self.micromagic.eterna.model.AppData;
import self.micromagic.eterna.model.Execute;
import self.micromagic.eterna.model.ModelAdapter;
import self.micromagic.eterna.model.ModelExecuteGenerator;
import self.micromagic.eterna.model.ModelExport;
import self.micromagic.eterna.share.EternaFactory;
import self.micromagic.util.ObjectRef;

public class ModelExecute extends AbstractExecute
		implements Execute, ModelExecuteGenerator
{
	protected String exportName = null;
	protected boolean noJump = false;
	protected ModelExport export = null;
	protected int exeModelIndex = -1;
	protected int transactionType = -1;

	public void initialize(ModelAdapter model)
			throws ConfigurationException
	{
		if (this.initialized)
		{
			return;
		}
		super.initialize(model);

		if (this.exportName != null)
		{
			this.export = model.getFactory().getModelExport(this.exportName);
			if (this.export == null)
			{
				log.warn("The ModelExport [" + this.exportName + "] not found.");
			}
		}
		if (this.getName() != null)
		{
			this.exeModelIndex = model.getFactory().getModelAdapterId(this.getName());
		}

		if (this.exeModelIndex == -1 && this.export == null)
		{
			log.warn("A model-execute dosen't give modelName or exportName.");
		}
	}

	public String getExecuteType()
			throws ConfigurationException
	{
		return "model";
	}

	public String getName()
			throws ConfigurationException
	{
		return super.getName() == null ? "#export" : super.getName();
	}

	public void setNoJump(boolean noJump)
	{
		this.noJump = noJump;
	}

	public void setExportName(String name)
	{
		this.exportName = name;
	}

	public void setTransactionType(String tType)
			throws ConfigurationException
	{
		this.transactionType = ModelAdapterImpl.parseTransactionType(tType);
	}

	public ModelExport execute(AppData data, Connection conn)
			throws ConfigurationException, SQLException, IOException
	{
		if (this.exeModelIndex != -1)
		{
			EternaFactory f = this.getModelAdapter().getFactory();
			ObjectRef preConn = (ObjectRef) data.getSpcialData(ModelAdapter.MODEL_CACHE, ModelAdapter.PRE_CONN);
			ModelAdapter tmpModel = f.createModelAdapter(this.exeModelIndex);
			int tType = this.transactionType == -1 ? tmpModel.getTransactionType() : this.transactionType;
			if (this.noJump)
			{
				try
				{
					f.getModelCaller().callModel(data, tmpModel, this.export, tType, preConn);
				}
				catch (Throwable ex)
				{
					log.error("Error in model execute", ex);
				}
			}
			else
			{
				return f.getModelCaller().callModel(data, tmpModel, this.export, tType, preConn);
			}
		}
		return this.export;
	}

}