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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import self.micromagic.eterna.dao.preparer.ValuePreparer;
import self.micromagic.eterna.model.AppData;
import self.micromagic.eterna.search.Condition;
import self.micromagic.eterna.search.ConditionBuilder;
import self.micromagic.eterna.search.ConditionProperty;
import self.micromagic.eterna.share.AbstractGenerator;
import self.micromagic.eterna.share.EternaException;
import self.micromagic.eterna.share.EternaFactory;
import self.micromagic.util.container.RequestParameterMap;
import self.micromagic.util.converter.BooleanConverter;
import self.micromagic.util.ref.ObjectRef;

/**
 * 模板条件构造器.
 *
 * 可设置的属性有
 * template           构造的条件的模板
 * param_count        模板中的参数个数, 默认值为"?"的个数
 * column_name_flag   需要替换成列名的标识, 默认值为"[C]"
 * sub_flag           需要生成迭代子句的标识, 默认值为"$"
 * sub_cell           迭代子句每个单元的模板, 默认值为"?"
 * sub_link           每个迭代子句的连接符, 默认值为" ,"
 * array_param        传入的参数是否为数组, 默认值为false
 * value_split        数据的分隔符, 只有在array_param为false时才有效, 默认值为","
 *
 *
 * example 1
 * tamplet    [C] IN (SELECT col1 FROM table1 WHERE id = ?)
 * column     theCol
 * result     theCol IN (SELECT col1 FROM table1 WHERE id = ?)
 *
 * example 2
 * tamplet    ([C0] like ? or [C1] like ?)
 * column     col1,col2
 * result     (col1 like ? or col2 like ?)
 */
public class TemplateBuilder extends AbstractGenerator
		implements ConditionBuilder
{
	/**
	 * 多个数据间使用的分隔符.
	 */
	public static final String VALUE_SPLIT = ",";

	/**
	 * 语句模板中默认的子句的标识符.
	 */
	public static final String DEFAULT_SUB_FLAG = "$";

	/**
	 * 默认模板中的列名标识.
	 */
	private static final String DEFAULT_COLUMN_NAME_FLAG = "[C]";

	private String template;
	private int[] indexs;
	private int maxIndex;
	private Object[] subTemplates;

	private boolean arrayParam;
	private String subFlag = DEFAULT_SUB_FLAG;
	/**
	 * 非数字数据的分割符.
	 */
	private String valueSplit = VALUE_SPLIT;
	/**
	 * 子元素间的连接符.
	 */
	protected String subLink = ", ";
	/**
	 * 每个子元素的值.
	 */
	private static final String[] DEFAULT_CELLS = new String[]{"?"};
	protected String[] subCells = DEFAULT_CELLS;
	protected int[] subIndexs;
	/**
	 * 是否设置了子句标识.
	 */
	protected boolean hasSub;

	/**
	 * 模板中参数的个数.
	 */
	protected int paramCount = 1;

	public boolean initialize(EternaFactory factory)
	{
		if (this.factory == null)
		{
			this.factory = factory;
			return false;
		}
		return true;
	}

	public String getCaption()
	{
		return this.caption;
	}
	public void setCaption(String caption)
	{
		this.caption = caption;
	}
	private String caption;

	public void setOperator(String operator)
	{
	}

	public Object create()
			throws EternaException
	{
		return this.createConditionBuilder();
	}

	public ConditionBuilder createConditionBuilder()
			throws EternaException
	{
		this.parseTemplate();
		return this;
	}

	/**
	 * 解析模板中的列名标识
	 */
	private void parseColumnFlag(String template, String cfPrefix,
			List subTemplateList, List indexList)
	{
		int fpLen = cfPrefix.length();
		String str = template;
		int index = str.indexOf(cfPrefix);
		while (index != -1)
		{
			if (str.charAt(index + fpLen + 1) == ']')
			{
				subTemplateList.add(this.createSubTemplate(str.substring(0, index)));
				indexList.add(new Integer(str.substring(index + fpLen, index + fpLen + 1)));
				str = str.substring(index + fpLen + 2);
				index = str.indexOf(cfPrefix);
			}
			else if (str.charAt(index + fpLen + 2) == ']')
			{
				subTemplateList.add(this.createSubTemplate(str.substring(0, index)));
				indexList.add(new Integer(str.substring(index + fpLen, index + fpLen + 2)));
				str = str.substring(index + fpLen + 3);
				index = str.indexOf(cfPrefix);
			}
			else if (str.charAt(index + fpLen) == ']')
			{
				subTemplateList.add(this.createSubTemplate(str.substring(0, index)));
				indexList.add(Utility.INTEGER_0);
				str = str.substring(index + fpLen + 1);
				index = str.indexOf(cfPrefix);
			}
			else
			{
				index = str.indexOf(cfPrefix, index + 1);
			}
		}
		subTemplateList.add(this.createSubTemplate(str));
	}

	private void parseTemplate()
	{
		this.template = (String) this.getAttribute("template");
		if (this.template == null)
		{
			this.template = "";
			this.subTemplates = StringTool.EMPTY_STRING_ARRAY;
		}
		String pCount = (String) this.getAttribute("param_count");
		if (pCount != null)
		{
			this.paramCount = Integer.parseInt(pCount);
		}
		else
		{
			// 当未设置参数个数时, 通过对?计数来设置
			this.paramCount = StringTool.countChar(this.template, '?');
		}

		String tmp;
		tmp = (String) this.getAttribute("column_name_flag");
		tmp = tmp == null ? DEFAULT_COLUMN_NAME_FLAG : tmp;
		if (!tmp.startsWith("[") || !tmp.endsWith("]"))
		{
			throw new EternaException("Error column_name_flag {" + tmp + "}.");
		}
		String colFlagPrefix = tmp.substring(0, tmp.length() - 1);
		tmp = (String) this.getAttribute("sub_flag");
		if (!StringTool.isEmpty(tmp))
		{
			this.subFlag = tmp;
		}

		// 解析模板中的列名标识
		List subTemplateList = new ArrayList();
		List indexList = new ArrayList();
		this.parseColumnFlag(this.template, colFlagPrefix, subTemplateList, indexList);
		this.subTemplates = subTemplateList.toArray(new Object[subTemplateList.size()]);
		// 整理列标识的索引
		this.indexs = new int[indexList.size()];
		this.maxIndex = -1;
		for (int i = 0; i < this.indexs.length; i++)
		{
			this.indexs[i] = ((Integer) indexList.get(i)).intValue();
			if (this.indexs[i] > this.maxIndex)
			{
				this.maxIndex = this.indexs[i];
			}
		}

		if (this.hasSub)
		{
			tmp = (String) this.getAttribute("sub_cell");
			if (!StringTool.isEmpty(tmp))
			{
				subTemplateList = new ArrayList();
				indexList = new ArrayList();
				this.parseColumnFlag(tmp, colFlagPrefix, subTemplateList, indexList);
				this.subCells = (String[]) subTemplateList.toArray(new String[subTemplateList.size()]);
				// 整理子模板列标识的索引
				this.subIndexs = new int[indexList.size()];
				for (int i = 0; i < this.subIndexs.length; i++)
				{
					this.subIndexs[i] = ((Integer) indexList.get(i)).intValue();
					if (this.subIndexs[i] > this.maxIndex)
					{
						this.maxIndex = this.subIndexs[i];
					}
				}
			}
			tmp = (String) this.getAttribute("sub_link");
			if (!StringTool.isEmpty(tmp))
			{
				this.subLink = tmp;
			}
			tmp = (String) this.getAttribute("value_split");
			if (!StringTool.isEmpty(tmp))
			{
				this.valueSplit = tmp;
			}
			tmp = (String) this.getAttribute("array_param");
			if (!StringTool.isEmpty(tmp))
			{
				this.arrayParam = (new BooleanConverter()).convertToBoolean(tmp);
			}
		}
	}

	/**
	 * 创建一个子模板.
	 */
	private Object createSubTemplate(String subTemplate)
	{
		int subIndex = subTemplate.indexOf(this.subFlag);
		if (subIndex != -1)
		{
			if (this.hasSub || subTemplate.indexOf(this.subFlag, subIndex + 1) != -1)
			{
				throw new EternaException(
						"Too many sub flag in template [" + this.template + "].");
			}
			// 如果子模板中存在子句, 则需要创建子句模板
			this.hasSub = true;
			return new SubFlagTemplate(subTemplate.substring(0, subIndex),
					subTemplate.substring(subIndex + this.subFlag.length()));
		}
		return subTemplate;
	}

	public Condition buildeCondition(String colName, Object value, ConditionProperty cp)
			throws EternaException
	{
		if (this.template.length() == 0)
		{
			return null;
		}

		ObjectRef valueOut = new ObjectRef();
		String sqlPart;
		if (this.maxIndex == -1)
		{
			// 模板中没有设置列名
			sqlPart = this.getSubValue(this.subTemplates[0], value, cp,
					StringTool.EMPTY_STRING_ARRAY, valueOut);
		}
		else
		{
			String[] colNames = new String[this.maxIndex + 1];
			Arrays.fill(colNames, "");
			String[] tmpArr = StringTool.separateString(colName, VALUE_SPLIT, true);
			System.arraycopy(tmpArr, 0, colNames, 0, Math.min(tmpArr.length, colNames.length));

			int size = this.template.length() + (this.subTemplates.length - 1) * colName.length();
			StringAppender buf = StringTool.createStringAppender(size);
			for (int i = 0; i < this.subTemplates.length; i++)
			{
				if (i > 0)
				{
					buf.append(colNames[this.indexs[i - 1]]);
				}
				buf.append(this.getSubValue(this.subTemplates[i], value, cp, colNames, valueOut));
			}
			sqlPart = buf.toString();
		}

		ValuePreparer[] preparers;
		if (this.hasSub)
		{
			Object[] values = (Object[]) valueOut.getObject();
			if (values == null)
			{
				preparers = null;
			}
			else
			{
				preparers = new ValuePreparer[values.length];
				for (int i = 0; i < values.length; i++)
				{
					preparers[i] = cp.createValuePreparer(values[i]);
				}
			}
		}
		else
		{
			preparers = new ValuePreparer[this.paramCount];
			if (this.paramCount > 0)
			{
				Arrays.fill(preparers, cp.createValuePreparer(value));
			}
		}
		preparers = this.specialParam(preparers, colName, value, cp);
		return new Condition(sqlPart, preparers);
	}

	/**
	 * 获取一个子模板的值.
	 */
	private String getSubValue(Object subTemplate, Object value, ConditionProperty cp,
			String[] colNames, ObjectRef valueOut)
	{
		if (subTemplate instanceof String)
		{
			return (String) subTemplate;
		}
		return ((SubFlagTemplate) subTemplate).getSub(value, this, cp, colNames, valueOut);
	}

	/**
	 * 对返回的参数进行特殊处理.
	 *
	 * @param old      已准备好的参数
	 * @param colName  要生成的条件的名称
	 * @param value    要生成的条件的值
	 * @param cp       与此条件生成器的相对应的ConditionProperty
	 * @return  处理后的参数
	 */
	protected ValuePreparer[] specialParam(ValuePreparer[] old,
			String colName, Object value, ConditionProperty cp)
	{
		return old;
	}

	/**
	 * 获取需要构成条件的数组.
	 */
	protected Object[] getValues(Object value, ConditionProperty cp)
	{
		Object[] values;
		if (!this.arrayParam)
		{
			// 参数不是数组, 需要根据分隔符进行分割
			values = StringTool.separateString((String) value, this.valueSplit);
		}
		else
		{
			if (value instanceof Object[])
			{
				values = (Object[]) value;
			}
			else
			{
				Map pMap = AppData.getCurrentData().getRequestParameterMap();
				if (pMap instanceof RequestParameterMap)
				{
					RequestParameterMap tmpMap = (RequestParameterMap) pMap;
					if (tmpMap.isParseValue())
					{
						values = (Object[]) pMap.get(cp.getName().concat("[]"));
					}
					else
					{
						values = (Object[]) pMap.get(cp.getName());
					}
				}
				else
				{
					values = (Object[]) pMap.get(cp.getName());
				}
			}
		}
		return values;
	}

}

/**
 * 带有子句标识的子模板.
 */
class SubFlagTemplate
{
	public SubFlagTemplate(String begin, String end)
	{
		this.beginStr = begin;
		this.endStr = end;
	}
	private final String beginStr;
	private final String endStr;

	public String getSub(Object value, TemplateBuilder builder, ConditionProperty cp,
			String[] colNames, ObjectRef valueOut)
	{
		Object[] values = builder.getValues(value, cp);
		if (values != null && values.length > 0)
		{
			valueOut.setObject(values);
			StringAppender sqlPart = StringTool.createStringAppender(
					values.length * 3 + this.beginStr.length() + this.endStr.length());
			sqlPart.append(this.beginStr);
			for (int i = 0; i < values.length; i++)
			{
				if (i > 0)
				{
					sqlPart.append(builder.subLink);
				}
				if (builder.subIndexs != null && builder.subIndexs.length > 0)
				{
					for (int j = 0; j < builder.subCells.length; j++)
					{
						if (j > 0)
						{
							sqlPart.append(colNames[builder.subIndexs[j - 1]]);
						}
						sqlPart.append(builder.subCells[j]);
					}
				}
				else
				{
					sqlPart.append(builder.subCells[0]);
				}
			}
			sqlPart.append(this.endStr);
			return sqlPart.toString();
		}
		return null;
	}

}