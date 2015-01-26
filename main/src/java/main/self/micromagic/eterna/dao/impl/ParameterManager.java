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

package self.micromagic.eterna.dao.impl;

import java.util.ArrayList;
import java.util.Iterator;

import self.micromagic.eterna.dao.Parameter;
import self.micromagic.eterna.share.EternaException;
import self.micromagic.eterna.share.EternaFactory;
import self.micromagic.util.StringAppender;
import self.micromagic.util.StringTool;

/**
 * 数据库操作的参数对象的管理者.
 */
public class ParameterManager
{
	public static final int NORMAL_PARAMETER = 0;
	public static final int DYNAMIC_PARAMETER = 1;

	private Parameter param;
	private final int type;
	private String groupName;
	private boolean parameterSetted;

	private boolean checked;
	private String[] templates = StringTool.EMPTY_STRING_ARRAY;

	private int index;

	public ParameterManager(int type)
	{
		this.type = type;
	}

	public void setGroupName(String groupName)
	{
		this.groupName = groupName;
	}

	public ParameterManager copy(boolean clear)
	{
		ParameterManager other = new ParameterManager(this.type);
		other.checked = this.checked;
		other.param = this.param;
		other.index = this.index;
		other.parameterSetted = clear ? false : this.parameterSetted;
		other.templates = this.templates;
		return other;
	}

	/**
	 * 预查动态参数
	 */
	public void preCheck()
			throws EternaException
	{
		if (this.type == NORMAL_PARAMETER)
		{
			return;
		}
		String template;
		ArrayList partList = new ArrayList();
		ArrayList paramList = new ArrayList();
		ArrayList subSQLList = new ArrayList();
		ArrayList subList = new ArrayList();
		for (int i = 0; i < this.templates.length; i++)
		{
			template = this.templates[i];
			DaoManager.parse(template, true, partList, paramList, subSQLList, subList);
		}
		if (paramList.size() != 1)
		{
			throw new EternaException("Error dynamic parameter template, postion ["
					+ (this.index + 1) + "], group name [" + this.groupName
					+ "], param count [" + paramList.size() + "].");
		}
	}

	public void check(EternaFactory factory)
			throws EternaException
	{
		if (this.checked)
		{
			return;
		}

		this.checked = true;
		// 对于没有设置的不进行判断, 未设置的可以通过数字设置
		/*if (this.param == null)
		{
			throw new EternaException("The parameter not begin at position:"
					+ (this.index + 1) + ".");
		}*/

		if (this.type == NORMAL_PARAMETER)
		{
			return;
		}

		String template;
		ArrayList partList = new ArrayList();
		ArrayList paramList = new ArrayList();
		ArrayList subSQLList = new ArrayList();
		ArrayList subList = new ArrayList();
		for (int i = 0; i < this.templates.length; i++)
		{
			template = this.templates[i];
			DaoManager.parse(template, true, partList, paramList, subSQLList, subList);

			// 根据解析的结果修改template
			StringAppender temp = StringTool.createStringAppender();
			Iterator itr = partList.iterator();
			while (itr.hasNext())
			{
				DaoManager.PartSQL ps = (DaoManager.PartSQL) itr.next();
				ps.initialize(factory);
				temp.append(ps.getSQL());
			}
			if (!template.equals(temp.toString()))
			{
				// 如果相同则不进行处理, 如果不同说明有常量在里面, 要进行intern处理
				this.templates[i] = StringTool.intern(temp.toString(), true);
			}
			partList.clear();
		}
		if (paramList.size() != 1)
		{
			throw new EternaException("Error parameter template, param name ["
					+ this.param.getName() + "].");
		}
	}

	public Parameter getParam()
	{
		return this.param;
	}

	public void setParam(Parameter param)
			throws EternaException
	{
		if (this.param != null)
		{
			throw new EternaException("You can't begin two name in same position:"
					+ (this.index + 1) + ".");
		}
		this.param = param;
	}

	public void clearParam()
	{
		this.param = null;
	}

	public int getType()
	{
		return this.type;
	}

	public void addParameterTemplate(String template)
			throws EternaException
	{
		if (this.type == NORMAL_PARAMETER)
		{
			throw new EternaException("You can't set template in normal parameter, name "
					+ this.param.getName() + ".");
		}
		if (template == null)
		{
			throw new NullPointerException();
		}

		int oldCount = this.templates.length;
		String[] temp = new String[oldCount + 1];
		System.arraycopy(this.templates, 0, temp, 0, oldCount);
		temp[oldCount] = template;
		this.templates = temp;
	}

	public int getParameterTemplateCount()
			throws EternaException
	{
		if (type == NORMAL_PARAMETER)
		{
			throw new EternaException("You can't get template in normal parameter, name "
					+ this.param.getName() + ".");
		}
		return this.templates.length;
	}

	public String getParameterTemplate(int index)
			throws EternaException
	{
		if (type == NORMAL_PARAMETER)
		{
			throw new EternaException("You can't get template in normal parameter, name "
					+ this.param.getName() + ".");
		}
		return this.templates[index];
	}

	public boolean isParameterSetted()
	{
		return this.parameterSetted;
	}

	public void setParameterSetted(boolean parameterSetted)
	{
		this.parameterSetted = parameterSetted;
	}

	public int getIndex()
	{
		return this.index;
	}

	void setIndex(int index)
	{
		this.index = index;
	}

}