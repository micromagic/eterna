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

package self.micromagic.util;

import java.sql.SQLException;

import self.micromagic.eterna.digester.ConfigurationException;
import self.micromagic.eterna.model.AppData;
import self.micromagic.eterna.model.Execute;
import self.micromagic.eterna.model.ModelAdapter;
import self.micromagic.eterna.model.ParamBind;
import self.micromagic.eterna.model.ParamBindGenerator;
import self.micromagic.eterna.model.ParamSetManager;
import self.micromagic.eterna.share.AbstractGenerator;
import self.micromagic.eterna.share.TypeManager;
import self.micromagic.eterna.sql.preparer.PreparerManager;
import self.micromagic.eterna.sql.preparer.ValuePreparer;
import self.micromagic.eterna.sql.preparer.ValuePreparerCreater;

public class InConditionParamBind extends AbstractGenerator
		implements ParamBindGenerator, ParamBind
{
	private int[] subIndexs = null;
	private String paramName = null;
	private int cacheIndex = -1;
	private String paramType = null;
	private String seperate = null;
	private ValuePreparerCreater vpg;

	public void initialize(ModelAdapter model, Execute execute)
			throws ConfigurationException
	{
		this.vpg = model.getFactory().createValuePreparerCreater(
				TypeManager.getPureType(TypeManager.getTypeId(this.paramType)));
	}

	public int setParam(AppData data, ParamSetManager psm, int loopIndex)
			throws ConfigurationException, SQLException
	{
		Object obj = this.cacheIndex != -1 ? data.caches[this.cacheIndex]
				: data.getRequestParameterMap().get(this.paramName);
		String[] values = null;
		if (obj != null)
		{
			if (this.seperate == null)
			{
				if (obj instanceof String[])
				{
					values = (String[]) obj;
				}
				else
				{
					values = new String[]{String.valueOf(obj)};
				}
			}
			else
			{
				if (obj instanceof String[])
				{
					values = StringTool.separateString(((String[]) obj)[0], this.seperate);
				}
				else
				{
					values = StringTool.separateString(String.valueOf(obj), this.seperate);
				}
			}
		}

		if (values == null || values.length == 0)
		{
			for (int i = 0; i < this.subIndexs.length; i++)
			{
				psm.setSubSQL(this.subIndexs[i], "null");
			}
		}
		else
		{
			StringAppender subStr = StringTool.createStringAppender(values.length * 3);
			PreparerManager pm = new PreparerManager(values.length);
			for (int i = 0; i < values.length; i++)
			{
				if (i > 0)
				{
					subStr.append(", ");
				}
				subStr.append('?');
				ValuePreparer preparer = vpg.createPreparer(values[i]);
				preparer.setRelativeIndex(i + 1);
				preparer.setName(this.paramName);
				pm.setValuePreparer(preparer);
			}
			for (int i = 0; i < this.subIndexs.length; i++)
			{
				psm.setSubSQL(this.subIndexs[i], subStr.toString(), pm);
			}
		}
		return -1;
	}

	public boolean isLoop()
	{
		return false;
	}

	public boolean isSubSQL()
	{
		return true;
	}

	public void setSrc(String src)
			throws ConfigurationException
	{
		try
		{
			int index = src.indexOf(';');
			if (index == -1)
			{
				this.paramName = src;
				this.paramType = "String";
			}
			else
			{
				this.paramName = src.substring(0, index);
				this.paramType = src.substring(index + 1);
				index = this.paramType.indexOf(';');
				if (index != -1)
				{
					this.seperate = this.paramType.substring(index + 1);
					this.paramType = this.paramType.substring(0, index);
				}
			}
			if (this.paramName.startsWith("cache:"))
			{
				this.cacheIndex = Integer.parseInt(this.paramName.substring(6));
			}
		}
		catch (Exception ex)
		{
			throw new ConfigurationException("[IN condition] src pattern is:"
					+ "[(paramName|cache:N)(;paramType(;seperate)?)?], but you give:[" + src + "].");
		}
	}

	public void setNames(String names)
			throws ConfigurationException
	{
		try
		{
			String[] tmps = StringTool.separateString(names, ",", true);
			this.subIndexs = new int[tmps.length];
			for (int i = 0; i < tmps.length; i++)
			{
				this.subIndexs[i] = Integer.parseInt(tmps[i]);
			}
		}
		catch (Exception ex)
		{
			throw new ConfigurationException("[IN condition] names must set sub sql indexs, not like this:["
					+ names + "].");
		}
	}

	public void setLoop(boolean loop)
			throws ConfigurationException
	{
		if (loop)
		{
			throw new ConfigurationException("[IN condition] can't used in loop.");
		}
	}

	public void setSubSQL(boolean subSQL)
			throws ConfigurationException
	{
		if (!subSQL)
		{
			throw new ConfigurationException("[IN condition] must use in sub sql.");
		}
	}

	public ParamBind createParamBind()
			throws ConfigurationException
	{
		if (this.subIndexs == null || this.paramName == null)
		{
			throw new ConfigurationException("Must set src and names attribute.");
		}
		return this;
	}

	public Object create()
			throws ConfigurationException
	{
		return this.createParamBind();
	}

}