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

package self.micromagic.eterna.sql.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import self.micromagic.eterna.share.EternaException;
import self.micromagic.eterna.share.EternaFactory;
import self.micromagic.eterna.share.Tool;
import self.micromagic.eterna.sql.SQLParameter;
import self.micromagic.util.StringTool;
import self.micromagic.util.StringAppender;

public class SQLManager
{
	public static final char EXTEND_FLAG = '#';
	public static final String EXTEND_FLAG_STR = "#";
	public static final char PARAMETER_FLAG = '?';
	public static final char SUBPART_FLAG = '$';

	public static final char EXTEND_NAME_BEGIN = '(';
	public static final char EXTEND_NAME_END = ')';

	public static final String AUTO_NAME = "auto";
	public static final String SUBSQL_NAME = "sub";
	public static final String PARAMETER_NAME = "param";
	public static final String CONSTANT_NAME = "const";

	public static final String AUTO_TYPE_AND = "and";
	public static final String AUTO_TYPE_OR = "or";
	public static final String AUTO_TYPE_UPDATE = "update";
	public static final String AUTO_TYPE_INSERT_N = "insertN";
	public static final String AUTO_TYPE_INSERT_V = "insertV";
	public static final String AUTO_TYPE_QUERY = "query";

	public static final char TEMPLATE_BEGIN = '[';
	public static final char TEMPLATE_END = ']';

	protected static final Log log = Tool.log;


	private PartSQL[] partSQLs = new PartSQL[0];

	private ParameterManager[] parameterManagers = new ParameterManager[0];
	private int[] subPartIndexs = new int[0];

	private boolean changed = true;
	private String cacheSQL;

	public void initialize(EternaFactory factory)
			throws EternaException
	{
		int subIndex = 1;
		for (int i = 0; i < this.partSQLs.length; i++)
		{
			this.partSQLs[i].initialize(factory);
			if (this.partSQLs[i] instanceof SubPart)
			{
				((SubPart) this.partSQLs[i]).setSubIndex(subIndex++);
			}
		}
		for (int i = 0; i < this.parameterManagers.length; i++)
		{
			this.parameterManagers[i].check(factory);
		}
	}

	public String getPreparedSQL()
			throws EternaException
	{
		if (this.changed)
		{
			int size = 0;
			for (int i = 0; i < this.partSQLs.length; i++)
			{
				size += this.partSQLs[i].getLength();
			}
			StringAppender temp = StringTool.createStringAppender(size);
			for (int i = 0; i < this.partSQLs.length; i++)
			{
				temp.append(this.partSQLs[i].getSQL());
			}
			this.cacheSQL = temp.toString().trim();
			this.changed = false;
			if (log.isDebugEnabled())
			{
				log.debug("size:" + size + " bufSize:" + this.cacheSQL.length());
			}
		}
		return this.cacheSQL;
	}

	public String getTempPreparedSQL(int[] indexs, String[] subParts)
			throws EternaException
	{
		this.backupSubParts();
		try
		{
			this.setSubParts(indexs, subParts);
			int size = 0;
			for (int i = 0; i < this.partSQLs.length; i++)
			{
				size += this.partSQLs[i].getLength();
			}
			StringAppender temp = StringTool.createStringAppender(size);
			for (int i = 0; i < this.partSQLs.length; i++)
			{
				temp.append(this.partSQLs[i].getSQL());
			}
			return temp.toString().trim();
		}
		finally
		{
			this.recoverSubParts();
		}
	}

	public SQLManager copy(boolean clear)
	{
		SQLManager other = new SQLManager();
		other.parameterManagers = new ParameterManager[this.parameterManagers.length];
		for (int i = 0; i < this.parameterManagers.length; i++)
		{
			other.parameterManagers[i] = this.parameterManagers[i].copy(clear);
		}
		other.subPartIndexs = this.subPartIndexs;

		other.partSQLs = new PartSQL[this.partSQLs.length];
		for (int i = 0; i < this.partSQLs.length; i++)
		{
			other.partSQLs[i] = this.partSQLs[i].copy(clear, other);
		}

		if (!clear)
		{
			other.cacheSQL = this.cacheSQL;
			other.changed = this.changed;
		}
		return other;
	}

	public String frontParse(String sql, SQLParameter[] paramArray)
			throws EternaException
	{
		StringAppender buf = StringTool.createStringAppender(sql.length() + 16);
		String dealedSql = sql;
		int index = dealedSql.indexOf(EXTEND_FLAG + AUTO_NAME);
		while (index != -1)
		{
			// 不是个转义标记, 处理自动生成
			if (!isExtendBefore(dealedSql, index))
			{
				buf.append(dealedSql.substring(0, index));
				dealedSql = dealedSql.substring(index);
				if (dealedSql.length() <= 1 + AUTO_NAME.length()
						|| dealedSql.charAt(1 + AUTO_NAME.length()) != TEMPLATE_BEGIN)
				{
					throw new EternaException("After #auto must with a \"[\".");
				}
				int endI = dealedSql.indexOf(TEMPLATE_END);
				if (endI == -1)
				{
					throw new EternaException("After #auto not found \"]\".");
				}
				String tStr = dealedSql.substring(1 + AUTO_NAME.length() + 1, endI);
				String[] arr = StringTool.separateString(tStr, ",;", true);
				if (arr.length != 3)
				{
					throw new EternaException("The #auto must with 3 parameters.");
				}
				try
				{
					int begin = this.getAutoParamIndex(arr[1], paramArray) - 1;
					int end = this.getAutoParamIndex(arr[2], paramArray);
					String autoName = arr[0];
					boolean dynamicAuto = false;
					if (autoName.endsWith("D"))
					{
						autoName = autoName.substring(0, autoName.length() - 1);
						dynamicAuto = true;
					}
					if (AUTO_TYPE_UPDATE.equals(autoName))
					{
						this.dealAuto(" = ?", ", ", tStr, buf, paramArray, begin, end, true, dynamicAuto);
					}
					else if (AUTO_TYPE_INSERT_N.equals(autoName))
					{
						this.dealAuto("", ", ", tStr, buf, paramArray, begin, end, true, dynamicAuto);
					}
					else if (AUTO_TYPE_INSERT_V.equals(autoName))
					{
						this.dealAuto("?", ", ", tStr, buf, paramArray, begin, end, false, dynamicAuto);
					}
					else if (AUTO_TYPE_OR.equals(autoName))
					{
						this.dealAuto(" = ?", " or ", tStr, buf, paramArray, begin, end, true, dynamicAuto);
					}
					else if (AUTO_TYPE_AND.equals(autoName) || AUTO_TYPE_QUERY.equals(autoName))
					{
						this.dealAuto(" = ?", " and ", tStr, buf, paramArray, begin, end, true, dynamicAuto);
					}
					else
					{
						throw new EternaException("Error #auto type [" + tStr + "].");
					}
				}
				catch (Exception ex)
				{
					if (ex instanceof EternaException)
					{
						throw (EternaException) ex;
					}
					throw new EternaException("Error #auto parameters [" + tStr
							+ "], parameter count:" + paramArray.length + ".", ex);
				}
				dealedSql = dealedSql.substring(endI + 1);
			}
			else
			{
				buf.append(sql.substring(0, index + 1 + AUTO_NAME.length()));
				dealedSql = sql.substring(index + 1 + AUTO_NAME.length());
			}
			index = dealedSql.indexOf(EXTEND_FLAG + AUTO_NAME);
		}
		buf.append(dealedSql);
		return new String(buf.toString());
	}

	/**
	 * 获取自动代码生成的索引值.
	 */
	private int getAutoParamIndex(String indexExp, SQLParameter[] paramArray)
			throws EternaException
	{
		if (indexExp.charAt(0) == 'i')
		{
			// i+XXX, i-XXX, i=XXX
			char flag = indexExp.charAt(1);
			String name = indexExp.substring(2);
			for (int i = 0; i < paramArray.length; i++)
			{
				if (name.equals(paramArray[i].getName()))
				{
					if (flag == '+')
					{
						return i + 2;
					}
					else if (flag == '=')
					{
						return i + 1;
					}
					else if (flag == '-')
					{
						return i;
					}
					else
					{
						throw new IllegalArgumentException("Error flag:[" + flag + "].");
					}
				}
			}
			throw new IllegalArgumentException("Not found the param name:[" + name + "].");
		}
		else
		{
			// number
			int index = Integer.parseInt(indexExp);
			if (index < 0)
			{
				index = paramArray.length + index + 1;
			}
			return index;
		}
	}

	private void dealAuto(String plus, String separator, String template, StringAppender buf,
			SQLParameter[] paramArray, int begin, int end, boolean needName, boolean dynamicAuto)
			throws EternaException
	{
		if (begin > end || begin < 0)
		{
			throw new EternaException("Error #auto range [" + template + "].");
		}
		boolean first = true;
		for (int i = begin; i < end; i++)
		{
			if (dynamicAuto)
			{
				buf.append("#param(dAuto_").append(i).append(")[");
			}
			if (!first || dynamicAuto)
			{
				buf.append(separator);
			}
			first = false;
			if (needName)
			{
				buf.append(paramArray[i].getColumnName());
			}
			buf.append(plus);
			if (dynamicAuto)
			{
				buf.append(']');
			}
		}
	}

	public void parse(String sql)
			throws EternaException
	{
		this.changed = true;
		ArrayList partList = new ArrayList();
		ArrayList paramList = new ArrayList();
		ArrayList subSQLList = new ArrayList();
		ArrayList subList = new ArrayList();
		parse(sql, false, partList, paramList, subSQLList, subList);

		this.partSQLs = (PartSQL[]) partList.toArray(new PartSQL[0]);
		this.parameterManagers = (ParameterManager[]) paramList.toArray(new ParameterManager[0]);
		for (int i = 0; i < this.parameterManagers.length; i++)
		{
			this.parameterManagers[i].setIndex(i);
			this.parameterManagers[i].preCheck();
		}
		this.subPartIndexs = new int[subSQLList.size()];
		Iterator itr = subSQLList.iterator();
		for (int i = 0; i < this.subPartIndexs.length; i++)
		{
			this.subPartIndexs[i] = ((Integer) itr.next()).intValue();
		}
	}

	/**
	 * @param onlyC   是否只能有常量
	 */
	public static void parse(String sql, boolean onlyC, List partList, List paramList,
			List subSQLList, List subList)
			throws EternaException
	{
		HashMap paramMap = new HashMap();
		String dealedSql = sql;
		int index = dealedSql.indexOf(EXTEND_FLAG);
		PartSQL partSQL;
		while (index != -1)
		{
			if (index == dealedSql.length() - 1)
			{
				// EXTEND_FLAG 在最后, 所以作为normal sql处理
				addNormalPart(partList, paramList, subList, index, dealedSql);
				partList.add(new NormalSQL(EXTEND_FLAG_STR));
				dealedSql = "";
			}
			else if (dealedSql.charAt(index + 1) == EXTEND_FLAG)
			{
				// 连续两个EXTEND_FLAG, 合并成一个处理
				dealedSql = addNormalPart(partList, paramList, subList, index + 1, dealedSql);
			}
			else if (dealedSql.charAt(index + 1) == PARAMETER_FLAG || dealedSql.charAt(index + 1) == SUBPART_FLAG
					|| dealedSql.charAt(index + 1) == EXTEND_NAME_BEGIN || dealedSql.charAt(index + 1) == EXTEND_NAME_END
					|| dealedSql.charAt(index + 1) == TEMPLATE_BEGIN || dealedSql.charAt(index + 1) == TEMPLATE_END)
			{
				// 接着的其他特殊标记, 作为普通的sql处理
				dealedSql = addNormalPart(partList, paramList, subList, index, dealedSql);
				partList.add(new NormalSQL(dealedSql.substring(0, 1)));
				dealedSql = dealedSql.substring(1);
			}
			else
			{
				String checked = checkExtendType(dealedSql, index);
				if (checked == SUBSQL_NAME)
				{
					if (onlyC)
					{
						throw new EternaException("In template can't use sub_sql, sql:" + sql + ".");
					}
					// 是一个sub sql
					dealedSql = addNormalPart(partList, paramList, subList, index, dealedSql);
					dealedSql = dealedSql.substring(SUBSQL_NAME.length());
					if (dealedSql.length() > 0 && dealedSql.charAt(0) == TEMPLATE_BEGIN)
					{
						index = getEndTemplateIndex(dealedSql);
						partSQL = new SubPart(dealedSql.substring(1, index), paramList.size());
						dealedSql = dealedSql.substring(index + 1);
					}
					else
					{
						partSQL = new SubPart(SUBPART_FLAG + "",  paramList.size());
					}
					Integer tempIndex = new Integer(partList.size());
					partList.add(partSQL);
					subSQLList.add(tempIndex);
				}
				else if (checked == PARAMETER_NAME)
				{
					if (onlyC)
					{
						throw new EternaException("In template can't use parameter, sql:" + sql + ".");
					}
					// 是一个动态参数
					dealedSql = addNormalPart(partList, paramList, subList, index, dealedSql);
					dealedSql = dealedSql.substring(PARAMETER_NAME.length());
					if (dealedSql.length() == 0 || dealedSql.charAt(0) != EXTEND_NAME_BEGIN)
					{
						throw new EternaException("Not found dynamic parameter group, sql:" + sql + ".");
					}
					dealedSql = addParamPart(partList, paramList, paramMap, dealedSql);
				}
				else if (checked == CONSTANT_NAME)
				{
					// 是一个常量
					dealedSql = addNormalPart(partList, paramList, subList, index, dealedSql);
					dealedSql = dealedSql.substring(CONSTANT_NAME.length());
					if (dealedSql.length() == 0 || dealedSql.charAt(0) != EXTEND_NAME_BEGIN)
					{
						throw new EternaException("Not found constant name, sql:" + sql + ".");
					}
					index = dealedSql.indexOf(EXTEND_NAME_END);
					if (index == -1)
					{
						throw new EternaException("Not end constant name, sql:" + sql + ".");
					}
					partSQL = new ConstantSQL(dealedSql.substring(1, index));
					partList.add(partSQL);
					dealedSql = dealedSql.substring(index + 1);
				}
				else
				{
					// 没有发现匹配的EXTEND类型, 所以作为normal sql处理, 扩展标志作为普通字符
					addNormalPart(partList, paramList, subList, index + 1, dealedSql);
					dealedSql = dealedSql.substring(index + 1);
				}
			}
			index = dealedSql.indexOf(EXTEND_FLAG);
		}
		if (dealedSql.length() > 0)
		{
			addNormalPart(partList, paramList, subList, dealedSql.length(), dealedSql);
		}
	}

	/**
	 * @return 此subSql前面的参数个数
	 */
	public int setSubPart(int index, String subPart)
			throws EternaException
	{
		if (index < 0 || index >= this.subPartIndexs.length)
		{
			throw new EternaException("The position [" + (index + 1) + "] hasn't sub sql.");
		}
		if (subPart == null)
		{
			throw new EternaException("The position [" + (index + 1) + "] sub sql can't set [null].");
		}
		int temp = this.subPartIndexs[index];
		SubPart sp = (SubPart) this.partSQLs[temp];
		if (!sp.checkSubPartSame(subPart))
		{
			this.changed = true;
			sp.setSubPart(subPart);
		}
		return sp.getAheadParamCount();
	}

	private void setSubParts(int[] indexs, String[] subParts)
			throws EternaException
	{
		if (indexs.length != subParts.length)
		{
			throw new EternaException("The index count [" + indexs.length
					+ "] must same as sub part count [" + subParts.length + "].");
		}
		for (int i = 0; i < indexs.length; i++)
		{
			int index = indexs[i];
			if (index < 0 || index >= this.subPartIndexs.length)
			{
				throw new EternaException("The position [" + (index + 1) + "] hasn't sub sql.");
			}
			if (subParts[i] == null)
			{
				throw new EternaException("The position [" + (index + 1) + "] sub sql can't set [null].");
			}
			int temp = this.subPartIndexs[index];
			SubPart sp = (SubPart) this.partSQLs[temp];
			sp.setSubPart(subParts[i]);
		}
	}

	private void backupSubParts()
	{
		for (int i = 0; i < this.subPartIndexs.length; i++)
		{
			SubPart sp = (SubPart) this.partSQLs[this.subPartIndexs[i]];
			sp.backup();
		}
	}

	private void recoverSubParts()
	{
		for (int i = 0; i < this.subPartIndexs.length; i++)
		{
			SubPart sp = (SubPart) this.partSQLs[this.subPartIndexs[i]];
			sp.recover();
		}
	}

	public void setParamSetted(int index, boolean setted)
			throws EternaException
	{
		ParameterManager pm = this.parameterManagers[index];
		boolean isDynamicType = pm.getType() == ParameterManager.DYNAMIC_PARAMETER;
		if (!setted && !isDynamicType)
		{
			throw new EternaException("Only dynamic parameter can ignore.");
		}
		boolean old = pm.isParameterSetted();
		if (old != setted)
		{
			pm.setParameterSetted(setted);
			this.changed = this.changed || isDynamicType;
		}
	}

	public boolean isDynamicParameter(int index)
			throws EternaException
	{
		ParameterManager pm = this.parameterManagers[index];
		boolean isDynamicType = pm.getType() == ParameterManager.DYNAMIC_PARAMETER;
		return isDynamicType;
	}

	public int getSubPartCount()
	{
		return this.subPartIndexs.length;
	}

	public int getParameterCount()
	{
		return this.parameterManagers.length;
	}

	public ParameterManager getParameterManager(int index)
	{
		return this.parameterManagers[index];
	}

	private static String checkExtendType(String dealedSql, int extendFlagIndex)
	{
		int size = dealedSql.length();
		int tempLimit = SUBSQL_NAME.length() + 1;
		String tempStr;
		if (extendFlagIndex + tempLimit <= size)
		{
			tempStr = dealedSql.substring(extendFlagIndex + 1, extendFlagIndex + tempLimit);
			if (SUBSQL_NAME.equals(tempStr))
			{
				return SUBSQL_NAME;
			}
		}
		tempLimit = PARAMETER_NAME.length() + 1;
		if (extendFlagIndex + tempLimit <= size)
		{
			tempStr = dealedSql.substring(extendFlagIndex + 1, extendFlagIndex + tempLimit);
			if (PARAMETER_NAME.equals(tempStr))
			{
				return PARAMETER_NAME;
			}
		}
		tempLimit = CONSTANT_NAME.length() + 1;
		if (extendFlagIndex + tempLimit <= size)
		{
			tempStr = dealedSql.substring(extendFlagIndex + 1, extendFlagIndex + tempLimit);
			if (CONSTANT_NAME.equals(tempStr))
			{
				return CONSTANT_NAME;
			}
		}
		return null;
	}

	/**
	 * 将结束位置前面部分的sql语句作为normal部分, 添加到PartSQL列表中. <p>
	 * 添加时, 还将解析sql中的普通参数和子句标志.
	 *
	 * @return   返回结束位置之后的sql语句
	 */
	private static String addNormalPart(List partList, List paramList, List subList, int endIndex, String dealedSql)
	{
		String temp = dealedSql.substring(0, endIndex);
		paramList.addAll(getNormalParameters(temp));
		int index = temp.indexOf(SUBPART_FLAG);
		while (index != -1)
		{
			partList.add(new NormalSQL(temp.substring(0, index)));
			SubFlagPart sfp = new SubFlagPart();
			partList.add(sfp);
			subList.add(sfp);
			temp = temp.substring(index + 1);
			index = temp.indexOf(SUBPART_FLAG);
		}
		if (temp.length() > 0)
		{
			partList.add(new NormalSQL(temp));
		}
		return endIndex < dealedSql.length() ? dealedSql.substring(endIndex + 1) : "";
	}

	private static int getEndTemplateIndex(String dealedSql)
			throws EternaException
	{
		int index = 0;
		do
		{
			index = dealedSql.indexOf(TEMPLATE_END, index + 1);
			if (index == -1)
			{
				throw new EternaException("Not found the template end, dealedSql:" + dealedSql + ".");
			}
		// 如果模板结束标记前是个扩展标记, 则继续寻找下一个结束标记
		} while (isExtendBefore(dealedSql, index));
		return index;
	}

	/**
	 * 判断给定位置前是否存在有效的扩展标记. <p>
	 * 出现连续的奇数个扩展标记就是有效的扩展标记.
	 */
	private static boolean isExtendBefore(String dealedSql, int index)
	{
		int count = 0;
		for (int i = index - 1; i >= 0; i--)
		{
			if (dealedSql.charAt(i) == EXTEND_FLAG)
			{
				count++;
			}
			else
			{
				break;
			}
		}
		return (count & 0x1) == 1;
	}

	private static String addParamPart(List partList, List paramList, Map paramMap, String dealedSql)
			throws EternaException
	{
		int index = dealedSql.indexOf(EXTEND_NAME_END);
		if (index == -1)
		{
			throw new EternaException("Not end dynamic parameter group name, dealedSql:" + dealedSql + ".");
		}

		// 根据组名 归类动态参数
		String group = dealedSql.substring(1, index);
		ParameterManager pm = (ParameterManager) paramMap.get(group);
		if (pm == null)
		{
			pm = new ParameterManager(ParameterManager.DYNAMIC_PARAMETER);
			pm.setGroupName(group);
			paramMap.put(group, pm);
		}

		// 获取动态参数的template
		dealedSql = dealedSql.substring(index + 1);
		if (dealedSql.charAt(0) != TEMPLATE_BEGIN)
		{
			throw new EternaException("Not found dynamic parameter template, dealedSql:" + dealedSql + ".");
		}
		index = getEndTemplateIndex(dealedSql);

		String temp = dealedSql.substring(1, index);
		pm.addParameterTemplate(temp);
		PartSQL partSQL = new ParameterPart(pm, pm.getParameterTemplateCount() - 1);
		partList.add(partSQL);
		int tmpI = temp.indexOf(PARAMETER_FLAG);
		if (tmpI != -1)
		{
			boolean isParam = true;
			// 如果参数标记前是个扩展标记, 则继续寻找下一个参数标记
			while (isExtendBefore(temp, tmpI))
			{
				tmpI = temp.indexOf(PARAMETER_FLAG, tmpI + 1);
				if (tmpI == -1)
				{
					isParam = false;
					break;
				}
			}
			if (isParam)
			{
				paramList.add(pm);
			}
		}
		return dealedSql.substring(index + 1);
	}

	private static List getNormalParameters(String sql)
	{
		ArrayList paramList = new ArrayList();
		int index = sql.indexOf(PARAMETER_FLAG);
		while (index != -1)
		{
			paramList.add(new ParameterManager(ParameterManager.NORMAL_PARAMETER));
			index = sql.indexOf(PARAMETER_FLAG, index + 1);
		}
		return paramList;
	}


	/**
	 * sql 语句的片断, 可以是一部分普通的sql语句, 或一个子sql语句,
	 * 或可选参数, 或一个常量.
	 */
	protected static abstract class PartSQL
	{
		public void initialize(EternaFactory factory)
				throws EternaException
		{
		}

		public abstract PartSQL copy(boolean clear, SQLManager manager);

		public abstract int getLength() throws EternaException;

		public abstract String getSQL() throws EternaException;

	}

	protected static class ParameterPart extends PartSQL
	{
		private ParameterManager paramManager;
		private int templateIndex;

		public ParameterPart(ParameterManager paramManager, int templateIndex)
		{
			if (paramManager == null)
			{
				throw new NullPointerException();
			}
			this.paramManager = paramManager;
			this.templateIndex = templateIndex;
		}

		private ParameterPart()
		{
		}

		public PartSQL copy(boolean clear, SQLManager manager)
		{
			ParameterManager pm = manager.getParameterManager(this.paramManager.getIndex());
			ParameterPart other = new ParameterPart();
			other.paramManager = pm;
			other.templateIndex = this.templateIndex;
			return other;
		}

		public int getLength()
				throws EternaException
		{
			try
			{
				String temp = this.paramManager.getParameterTemplate(this.templateIndex);
				return this.paramManager.isParameterSetted() ? temp.length() : 0;
			}
			catch (Exception ex)
			{
				throw new EternaException(ex);
			}
		}

		public String getSQL()
				throws EternaException
		{
			String temp = this.paramManager.getParameterTemplate(this.templateIndex);
			return this.paramManager.isParameterSetted() ? temp : "";
		}
	}

	protected static class SubFlagPart extends PartSQL
	{
		private String insertString = null;

		public void setSubPart(String subPart)
		{
			this.insertString = subPart;
		}

		public PartSQL copy(boolean clear, SQLManager manager)
		{
			SubFlagPart other = new SubFlagPart();
			other.insertString = clear ? null : this.insertString;
			return other;
		}

		public int getLength()
		{
			return this.insertString == null ? 1 : this.insertString.length();
		}

		public String getSQL()
		{
			return this.insertString == null ? SQLManager.SUBPART_FLAG + "" : this.insertString;
		}
	}

	protected static class SubPart extends PartSQL
	{
		private int flagPartIndex = -1;
		private PartSQL[] parts = null;
		private String template;
		private int aheadParamCount;
		private int subIndex = 0;

		private String insertString = null;
		private String backupString = null;

		public SubPart(String template, int aheadParamCount)
		{
			if (template == null)
			{
				throw new NullPointerException();
			}
			this.template = template;
			this.aheadParamCount = aheadParamCount;
		}

		public PartSQL copy(boolean clear, SQLManager manager)
		{
			SubPart other = new SubPart(this.template, this.aheadParamCount);
			other.subIndex = this.subIndex;
			other.insertString = clear ? null : this.insertString;
			other.backupString = clear ? null : this.backupString;
			if (this.parts != null)
			{
				other.flagPartIndex = this.flagPartIndex;
				other.parts = new PartSQL[this.parts.length];
				for (int i = 0; i < this.parts.length; i++)
				{
					other.parts[i] = this.parts[i].copy(clear, manager);
				}
			}
			return other;
		}

		public void initialize(EternaFactory factory)
				throws EternaException
		{
			if (this.parts == null)
			{
				super.initialize(factory);
				ArrayList partList = new ArrayList();
				ArrayList paramList = new ArrayList();
				ArrayList subSQLList = new ArrayList();
				ArrayList subList = new ArrayList();
				SQLManager.parse(this.template, true, partList, paramList, subSQLList, subList);

				if (paramList.size() > 0)
				{
					throw new EternaException(
							"The parameter flag '?' can't int the sub SQL tamplet:"
							+ this.template + ".");
				}
				if (subList.size() != 1)
				{
					throw new EternaException(
							"Error sub SQL flag in template:" + this.template + ".");
				}
				this.parts = new PartSQL[partList.size()];
				Iterator itr = partList.iterator();
				for (int i = 0; i < this.parts.length; i++)
				{
					PartSQL ps = (PartSQL) itr.next();
					ps.initialize(factory);
					this.parts[i] = ps;
				}
				SubFlagPart flagPart = (SubFlagPart) subList.get(0);

				for (int i = 0; i < this.parts.length; i++)
				{
					if (this.parts[i] == flagPart)
					{
						this.flagPartIndex = i;
						break;
					}
				}
			}
		}

		public int getAheadParamCount()
		{
			return this.aheadParamCount;
		}

		public void setSubPart(String subPart)
		{
			this.insertString = subPart;
			((SubFlagPart) this.parts[this.flagPartIndex]).setSubPart(subPart);
		}

		public boolean checkSubPartSame(String subPart)
		{
			return this.insertString == subPart;
		}

		public int getLength()
				throws EternaException
		{
			if (this.insertString == null)
			{
				throw new EternaException("Sub SQL unsetted, subsql:[" + this.template + "].");
			}

			if (this.insertString.length() == 0)
			{
				return 0;
			}
			else
			{
				int size = 0;
				for (int i = 0; i < this.parts.length; i++)
				{
					size += this.parts[i].getLength();
				}
				return size;
			}
		}

		public String getSQL() throws EternaException
		{
			if (this.insertString == null)
			{
				throw new EternaException("Sub SQL unsetted, index:[" + this.subIndex
						+ "], subsql:[" + this.template + "].");
			}

			if (this.insertString.length() == 0)
			{
				return "";
			}

			StringAppender temp = StringTool.createStringAppender(this.getLength());
			for (int i = 0; i < this.parts.length; i++)
			{
				temp.append(this.parts[i].getSQL());
			}
			return temp.toString();
		}

		public int getSubIndex()
		{
			return this.subIndex;
		}

		public void setSubIndex(int subIndex)
		{
			this.subIndex = subIndex;
		}

		public void backup()
		{
			this.backupString = this.insertString;
		}

		public void recover()
		{
			if (this.backupString != null)
			{
				this.insertString = this.backupString;
				this.backupString = null;
			}
		}

	}

	protected static class ConstantSQL extends PartSQL
	{
		private String name;

		private String value = null;

		public ConstantSQL(String name)
		{
			if (name == null)
			{
				throw new NullPointerException();
			}
			this.name = StringTool.intern(name);
		}

		public void initialize(EternaFactory factory)
				throws EternaException
		{
			if (this.value == null)
			{
				super.initialize(factory);
				String temp = factory.getConstantValue(this.name);
				if (temp == null)
				{
					throw new EternaException("The constant '" + this.name + "' not found.");
				}
				this.value = temp;

				ArrayList partList = new ArrayList();
				ArrayList paramList = new ArrayList();
				ArrayList subSQLList = new ArrayList();
				ArrayList subList = new ArrayList();
				SQLManager.parse(this.value, true, partList, paramList, subSQLList, subList);

				if (paramList.size() > 0)
				{
					throw new EternaException(
							"The parameter flag '?' can't int the sub SQL tamplet:"
							+ this.value + ".");
				}
				StringAppender buf = StringTool.createStringAppender(this.value.length() + 16);
				Iterator itr = partList.iterator();
				for (int i = 0; i < partList.size(); i++)
				{
					PartSQL ps = (PartSQL) itr.next();
					ps.initialize(factory);
					buf.append(ps.getSQL());
				}
				this.value = StringTool.intern(buf.toString(), true);
			}
		}

		public PartSQL copy(boolean clear, SQLManager manager)
		{
			return this;
		}

		public String getName()
		{
			return this.name;
		}

		public void setValue(String value)
		{
			if (value != null)
			{
				this.value = value;
			}
		}

		public int getLength()
		{
			return this.value.length();
		}

		public String getSQL()
		{
			if (this.value == null)
			{
				throw new NullPointerException();
			}
			return this.value;
		}

	}

	protected static class NormalSQL extends PartSQL
	{
		private String sql;

		public NormalSQL(String sql)
		{
			if (sql == null)
			{
				throw new NullPointerException();
			}
			// 这里的sql大部分是intern处理过的字符串的字串
			this.sql = sql;
		}

		public PartSQL copy(boolean clear, SQLManager manager)
		{
			return this;
		}

		public int getLength()
		{
			return this.sql.length();
		}

		public String getSQL()
		{
			return this.sql;
		}

	}

}