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

package self.micromagic.eterna.dao.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;

import self.micromagic.eterna.dao.Dao;
import self.micromagic.eterna.dao.Parameter;
import self.micromagic.eterna.dao.Query;
import self.micromagic.eterna.dao.ResultReader;
import self.micromagic.eterna.dao.reader.InvalidReader;
import self.micromagic.eterna.dao.reader.ReaderWrapper;
import self.micromagic.eterna.share.EternaException;
import self.micromagic.eterna.share.EternaFactory;
import self.micromagic.eterna.share.Tool;
import self.micromagic.util.StringAppender;
import self.micromagic.util.StringTool;
import self.micromagic.util.converter.BooleanConverter;
import self.micromagic.util.ref.BooleanRef;
import self.micromagic.util.ref.IntegerRef;

/**
 * 数据库操作语句的管理者.
 */
public class DaoManager
{
	public static final char EXTEND_FLAG = '#';
	public static final String EXTEND_FLAG_STR = "#";
	public static final char PARAMETER_FLAG = '?';
	public static final char SUBPART_FLAG = '$';

	public static final char EXTEND_NAME_BEGIN = '(';
	public static final char EXTEND_NAME_END = ')';

	public static final String AUTO_NAME = "auto";
	public static final String CHECK_NAME = "check";
	public static final String SUB_SCRIPT_NAME = "sub";
	public static final String PARAMETER_NAME = "param";
	public static final String CONSTANT_NAME = "const";

	public static final String AUTO_TYPE_AND = "and";
	public static final String AUTO_TYPE_OR = "or";
	public static final String AUTO_TYPE_UPDATE = "update";
	public static final String AUTO_TYPE_INSERT_N = "insertN";
	public static final String AUTO_TYPE_INSERT_V = "insertV";
	public static final String AUTO_TYPE_SELECT = "select";

	public static final String AUTO_OPT_DYNAMIC = "dynamic";
	public static final String AUTO_OPT_DYNAMIC_S= "d";

	public static final char TEMPLATE_BEGIN = '[';
	public static final char TEMPLATE_END = ']';

	// 动态生成时, 不需要名称的标志
	private static final int AUTO_NAME_NONE = 0;
	// 动态生成时, 需要名称的标志
	private static final int AUTO_NAME_NEED = 1;
	// 动态生成时, 需要名称但不需要表名的标志
	private static final int AUTO_NAME_SKIPTABLE = 2;

	protected static final Log log = Tool.log;

	/**
	 * 在attribute中设置别名外使用怎样的引号的键值.
	 */
	public static final String NAME_QUOTE_FLAG = "dao.name.qoute";

	/**
	 * 生成的select部分的别名外需要使用的引号.
	 */
	private String nameQuote = "\"";

	private PartScript[] partScripts = new PartScript[0];

	private ParameterManager[] parameterManagers = new ParameterManager[0];
	private int[] subPartIndexs = new int[0];

	private boolean changed = true;
	private String cacheScript;
	private Dao dao;

	public void initialize(Dao dao)
			throws EternaException
	{
		this.dao = dao;
		EternaFactory factory = dao.getFactory();
		// 设置别名使用的引号
		String tmpQuote;
		if ((tmpQuote = (String) dao.getAttribute(NAME_QUOTE_FLAG)) != null
				|| (tmpQuote = (String) factory.getAttribute(NAME_QUOTE_FLAG)) != null)
		{
			this.nameQuote = tmpQuote;
		}
		int subIndex = 1;
		for (int i = 0; i < this.partScripts.length; i++)
		{
			this.partScripts[i].initialize(factory);
			if (this.partScripts[i] instanceof SubPart)
			{
				((SubPart) this.partScripts[i]).setSubIndex(subIndex++);
			}
		}
		for (int i = 0; i < this.parameterManagers.length; i++)
		{
			this.parameterManagers[i].check(factory);
		}
	}

	public String getPreparedScript()
			throws EternaException
	{
		if (this.changed)
		{
			IntegerRef size = new IntegerRef();
			try
			{
				this.cacheScript = this.buildPreparedScript(size);
			}
			catch (Exception ex)
			{
				String msg = "Error in build " + this.dao.getType() + " ["
						+ this.dao.getName() + "]'s script.";
				throw new EternaException(msg, ex);
			}
			this.changed = false;
			if (log.isDebugEnabled())
			{
				log.debug("size:" + size + " bufSize:" + this.cacheScript.length());
			}
		}
		return this.cacheScript;
	}
	public String getTempPreparedScript(int[] indexs, String[] subParts)
			throws EternaException
	{
		this.backupSubParts();
		try
		{
			this.setSubParts(indexs, subParts);
			return this.buildPreparedScript(null);
		}
		catch (Exception ex)
		{
			String msg = "Error in build " + this.dao.getType() + " ["
					+ this.dao.getName() + "]'s script.";
			throw new EternaException(msg, ex);
		}
		finally
		{
			this.recoverSubParts();
		}
	}
	private String buildPreparedScript(IntegerRef bufSize)
	{
		int size = 0;
		for (int i = 0; i < this.partScripts.length; i++)
		{
			size += this.partScripts[i].getLength();
		}
		if (bufSize != null)
		{
			bufSize.value = size;
		}
		StringAppender temp = StringTool.createStringAppender(size);
		CheckContainer currentCheck = new CheckContainer();
		for (int i = 0; i < this.partScripts.length; i++)
		{
			CheckPart check = currentCheck.checkPart;
			if (check == null)
			{
				if (this.partScripts[i] instanceof CheckPart)
				{
					currentCheck.checkPart = (CheckPart) this.partScripts[i];
				}
				else
				{
					temp.append(this.partScripts[i].getScript());
				}
			}
			else
			{
				temp.append(check.doCheck(this.partScripts[i], currentCheck));
			}
		}
		if (currentCheck.checkPart != null)
		{
			temp.append(currentCheck.checkPart.doCheck(null, currentCheck));
		}
		return temp.toString().trim();
	}

	public DaoManager copy(boolean clear)
	{
		DaoManager other = new DaoManager();
		other.dao = this.dao;
		other.parameterManagers = new ParameterManager[this.parameterManagers.length];
		for (int i = 0; i < this.parameterManagers.length; i++)
		{
			other.parameterManagers[i] = this.parameterManagers[i].copy(clear);
		}
		other.subPartIndexs = this.subPartIndexs;

		other.partScripts = new PartScript[this.partScripts.length];
		for (int i = 0; i < this.partScripts.length; i++)
		{
			other.partScripts[i] = this.partScripts[i].copy(clear, other);
		}

		if (!clear)
		{
			other.cacheScript = this.cacheScript;
			other.changed = this.changed;
		}
		return other;
	}

	/**
	 * 对数据操作脚本进行预解析.
	 *
	 * @param script  要预解析的数据操作脚本
	 * @param dao     脚本所在的数据操作对象
	 * @return  解析后的数据操作脚本
	 */
	public String preParse(String script, Dao dao)
			throws EternaException
	{
		ResultReader[] readerArray = null;
		Parameter[] paramArray = null;
		StringAppender buf = StringTool.createStringAppender(script.length() + 16);
		String dealedScript = script;
		int index = dealedScript.indexOf(EXTEND_FLAG + AUTO_NAME);
		while (index != -1)
		{
			// 不是个转义标记, 处理自动生成
			if (!isExtendBefore(dealedScript, index))
			{
				buf.append(dealedScript.substring(0, index));
				dealedScript = dealedScript.substring(index);
				if (dealedScript.length() <= 1 + AUTO_NAME.length()
						|| dealedScript.charAt(1 + AUTO_NAME.length()) != TEMPLATE_BEGIN)
				{
					throw new EternaException("After #auto must with a \"[\".");
				}
				int endI = dealedScript.indexOf(TEMPLATE_END);
				if (endI == -1)
				{
					throw new EternaException("After #auto not found \"]\".");
				}
				String autoConfig = dealedScript.substring(1 + AUTO_NAME.length() + 1, endI);
				int sIndex = autoConfig.indexOf(';');
				String beginParam = "1";
				String endParam = "-1";
				String optParam = autoConfig;
				if (sIndex != -1)
				{
					optParam = autoConfig.substring(0, sIndex);
					String[] arr = StringTool.separateString(
							autoConfig.substring(sIndex + 1), ",", true);
					beginParam = arr[0];
					if (arr.length > 1)
					{
						endParam = arr[1];
						if (arr.length > 2)
						{
							throw new EternaException("Error #auto config [" + autoConfig + "].");
						}
					}
				}
				int itemCount = 0;
				try
				{
					BooleanRef dynamic = new BooleanRef();
					String type = this.getAutoType(optParam, dynamic);
					boolean isSelect = AUTO_TYPE_SELECT.equals(type);
					int begin, end;
					if (isSelect)
					{
						if (dynamic.value)
						{
							String msg = "Can't use dynamic in #auto[" + AUTO_TYPE_SELECT
									+ "...], config [" + autoConfig + "]";
							throw new EternaException(msg);
						}
						if (readerArray == null)
						{
							if (!(dao instanceof Query))
							{
								String msg = "The " + dao.getType() +" [" + dao.getName()
										+ "] isn't a Query, can't use #auto[" + AUTO_TYPE_SELECT + "...].";
								throw new EternaException(msg);
							}
							// 生成reader列表
							readerArray = this.makeReaderArray((Query) dao);
							itemCount = readerArray.length;
						}
						begin = this.getAutoParamIndex(beginParam, null, readerArray) - 1;
						end = this.getAutoParamIndex(endParam, null, readerArray);
					}
					else
					{
						if (paramArray == null)
						{
							// 生成parameter列表, 这里不能用getParameterCount, 因为DaoManager还没初始化
							Iterator itr = dao.getParameterIterator();
							List tmp = new ArrayList();
							while (itr.hasNext())
							{
								tmp.add(itr.next());
								itemCount++;
							}
							paramArray = new Parameter[itemCount];
							tmp.toArray(paramArray);
						}
						begin = this.getAutoParamIndex(beginParam, paramArray, null) - 1;
						end = this.getAutoParamIndex(endParam, paramArray, null);
					}
					if (begin > end || begin < 0)
					{
						throw new EternaException("Error #auto range [" + autoConfig + "].");
					}

					if (isSelect)
					{
						boolean first = true;
						Map aliasSet = new HashMap();
						for (int i = begin; i < end; i++)
						{
							ResultReader reader = readerArray[i] ;
							if (reader instanceof InvalidReader)
							{
								continue;
							}
							String upName = reader.getAlias().toUpperCase();
							if (!aliasSet.containsKey(upName))
							{
								// 如果别名已生成过, 则不出现在select列表中
								aliasSet.put(upName, Boolean.TRUE);
								if (!first)
								{
									buf.append(", ");
								}
								else
								{
									first = false;
								}
								buf.append(readerArray[i].getColumnName()).append(" as ")
										.append(this.nameQuote).append(readerArray[i].getAlias())
										.append(this.nameQuote);
							}
						}
					}
					else
					{
						if (AUTO_TYPE_UPDATE.equals(type))
						{
							this.dealAuto(" = ?", ", ", buf, paramArray, begin, end,
									AUTO_NAME_SKIPTABLE, dynamic.value);
						}
						else if (AUTO_TYPE_INSERT_N.equals(type))
						{
							this.dealAuto("", ", ", buf, paramArray, begin, end,
									AUTO_NAME_SKIPTABLE, dynamic.value);
						}
						else if (AUTO_TYPE_INSERT_V.equals(type))
						{
							this.dealAuto("?", ", ", buf, paramArray, begin, end,
									AUTO_NAME_NONE, dynamic.value);
						}
						else if (AUTO_TYPE_OR.equals(type))
						{
							this.dealAuto(" = ?", " or ", buf, paramArray, begin, end,
									AUTO_NAME_NEED, dynamic.value);
						}
						else if (AUTO_TYPE_AND.equals(type))
						{
							this.dealAuto(" = ?", " and ", buf, paramArray, begin, end,
									AUTO_NAME_NEED, dynamic.value);
						}
						else
						{
							throw new EternaException("Error #auto type [" + autoConfig + "].");
						}
					}
				}
				catch (Exception ex)
				{
					String msg = "Error #auto config [" + autoConfig
							+ "], item count:" + itemCount + ".";
					if (ex instanceof EternaException)
					{
						log.error(msg);
						throw (EternaException) ex;
					}
					throw new EternaException(msg, ex);
				}
				dealedScript = dealedScript.substring(endI + 1);
			}
			else
			{
				buf.append(script.substring(0, index + 1 + AUTO_NAME.length()));
				dealedScript = script.substring(index + 1 + AUTO_NAME.length());
			}
			index = dealedScript.indexOf(EXTEND_FLAG + AUTO_NAME);
		}
		buf.append(dealedScript);
		return new String(buf.toString());
	}

	private ResultReader[] makeReaderArray(Query query)
	{
		List tmp = query.getReaderManager().getReaderList();
		int itemCount = tmp.size();
		ResultReader[] readerArray = new ResultReader[itemCount];
		Iterator itr = tmp.iterator();
		int realCount = 0;
		for (int i = 0; i < itemCount; i++)
		{
			ResultReader reader = (ResultReader) itr.next();
			if (reader instanceof ReaderWrapper && ((ReaderWrapper) reader).isHidden())
			{
				// 自动添加的reader, 不占用自动生成列表的位置
				continue;
			}
			realCount++;
			if (reader.isUseColumnIndex())
			{
				// 通过索引值定位列的不应该出现在自动生成里, 但需要占位
				readerArray[i] = new InvalidReader(reader.getName());
			}
			else
			{
				readerArray[i] = reader;
			}
		}
		if (realCount < readerArray.length)
		{
			ResultReader[] tmpArr = new ResultReader[realCount];
			System.arraycopy(readerArray, 0, tmpArr, 0, realCount);
			return tmpArr;
		}
		return readerArray;
	}

	/**
	 * 解析自动生成的类型.
	 *
	 * @param dynamic     是否生成动态参数
	 */
	private String getAutoType(String optParam, BooleanRef dynamic)
	{
		String type = null;
		String[] arr = StringTool.separateString(optParam, ",", true);
		for (int i = 0; i < arr.length; i++)
		{
			if (AUTO_OPT_DYNAMIC.equals(arr[i]))
			{
				dynamic.value = true;
			}
			else if (AUTO_OPT_DYNAMIC_S.equals(arr[i]))
			{
				dynamic.value = true;
			}
			else if (type != null)
			{
				throw new EternaException("Error #auto type [" + optParam + "].");
			}
			else
			{
				type = arr[i];
			}
		}
		return type;
	}

	/**
	 * 获取自动代码生成的索引值.
	 */
	private int getAutoParamIndex(String indexExp, Parameter[] paramArray,
			ResultReader[] readerArray)
			throws EternaException
	{
		if (indexExp.charAt(0) == 'i')
		{
			// i+name, i-name, i=name
			char flag = indexExp.charAt(1);
			String name = indexExp.substring(2);
			int count = paramArray != null ? paramArray.length : readerArray.length;
			for (int i = 0; i < count; i++)
			{
				String tmpName = paramArray != null ? paramArray[i].getName()
						: readerArray[i].getName();
				if (name.equals(tmpName))
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
				index = paramArray != null ? paramArray.length + index + 1
						: readerArray.length + index + 1;
			}
			return index;
		}
	}

	/**
	 * 处理非select的动态语句.
	 */
	private void dealAuto(String plus, String separator, StringAppender buf,
			Parameter[] paramArray, int begin, int end, int nameType, boolean dynamic)
			throws EternaException
	{
		boolean first = true;
		for (int i = begin; i < end; i++)
		{
			if (dynamic)
			{
				buf.append("#param(dAuto_").append(i).append(")[");
			}
			if (!first || dynamic)
			{
				buf.append(separator);
			}
			first = false;
			if (nameType > AUTO_NAME_NONE)
			{
				if (nameType == AUTO_NAME_SKIPTABLE)
				{
					String cName = paramArray[i].getColumnName();
					int tmpI = cName.indexOf('.');
					buf.append(tmpI == -1 ? cName : cName.substring(tmpI + 1));
				}
				else
				{
					buf.append(paramArray[i].getColumnName());
				}
			}
			buf.append(plus);
			if (dynamic)
			{
				buf.append(']');
			}
		}
	}

	public void parse(String script)
			throws EternaException
	{
		this.changed = true;
		ArrayList partList = new ArrayList();
		ArrayList paramList = new ArrayList();
		ArrayList subScriptList = new ArrayList();
		ArrayList subList = new ArrayList();
		parse(script, false, partList, paramList, subScriptList, subList);

		this.partScripts = (PartScript[]) partList.toArray(new PartScript[0]);
		this.parameterManagers = (ParameterManager[]) paramList.toArray(new ParameterManager[0]);
		for (int i = 0; i < this.parameterManagers.length; i++)
		{
			this.parameterManagers[i].setIndex(i);
			this.parameterManagers[i].preCheck();
		}
		this.subPartIndexs = new int[subScriptList.size()];
		Iterator itr = subScriptList.iterator();
		for (int i = 0; i < this.subPartIndexs.length; i++)
		{
			this.subPartIndexs[i] = ((Integer) itr.next()).intValue();
		}
	}

	/**
	 * @param onlyC   是否只能有常量
	 */
	public static void parse(String script, boolean onlyC, List partList, List paramList,
			List subScriptList, List subList)
			throws EternaException
	{
		HashMap paramMap = new HashMap();
		String dealedScript = script;
		int index = dealedScript.indexOf(EXTEND_FLAG);
		PartScript partScript;
		while (index != -1)
		{
			if (index == dealedScript.length() - 1)
			{
				// EXTEND_FLAG 在最后, 所以作为normal 脚本处理
				addNormalPart(partList, paramList, subList, index, dealedScript);
				partList.add(new NormalScript(EXTEND_FLAG_STR));
				dealedScript = "";
			}
			else if (dealedScript.charAt(index + 1) == EXTEND_FLAG)
			{
				// 连续两个EXTEND_FLAG, 合并成一个处理
				dealedScript = addNormalPart(partList, paramList, subList, index + 1, dealedScript);
			}
			else if (dealedScript.charAt(index + 1) == PARAMETER_FLAG || dealedScript.charAt(index + 1) == SUBPART_FLAG
					|| dealedScript.charAt(index + 1) == EXTEND_NAME_BEGIN || dealedScript.charAt(index + 1) == EXTEND_NAME_END
					|| dealedScript.charAt(index + 1) == TEMPLATE_BEGIN || dealedScript.charAt(index + 1) == TEMPLATE_END)
			{
				// 接着的其他特殊标记, 作为普通的脚本处理
				dealedScript = addNormalPart(partList, paramList, subList, index, dealedScript);
				partList.add(new NormalScript(dealedScript.substring(0, 1)));
				dealedScript = dealedScript.substring(1);
			}
			else
			{
				String checked = checkExtendType(dealedScript, index);
				if (checked == SUB_SCRIPT_NAME)
				{
					if (onlyC)
					{
						throw new EternaException("In template can't use sub, script:" + script + ".");
					}
					// 是一个sub脚本
					dealedScript = addNormalPart(partList, paramList, subList, index, dealedScript);
					dealedScript = dealedScript.substring(SUB_SCRIPT_NAME.length());
					if (dealedScript.length() > 0 && dealedScript.charAt(0) == TEMPLATE_BEGIN)
					{
						index = getEndTemplateIndex(dealedScript);
						partScript = new SubPart(dealedScript.substring(1, index), paramList.size());
						dealedScript = dealedScript.substring(index + 1);
					}
					else
					{
						partScript = new SubPart(SUBPART_FLAG + "",  paramList.size());
					}
					Integer tempIndex = new Integer(partList.size());
					partList.add(partScript);
					subScriptList.add(tempIndex);
				}
				else if (checked == PARAMETER_NAME)
				{
					if (onlyC)
					{
						throw new EternaException("In template can't use parameter, script:" + script + ".");
					}
					// 是一个动态参数
					dealedScript = addNormalPart(partList, paramList, subList, index, dealedScript);
					dealedScript = dealedScript.substring(PARAMETER_NAME.length());
					if (dealedScript.length() == 0 || dealedScript.charAt(0) != EXTEND_NAME_BEGIN)
					{
						throw new EternaException("Not found dynamic parameter group, script:" + script + ".");
					}
					dealedScript = addParamPart(partList, paramList, paramMap, dealedScript);
				}
				else if (checked == CONSTANT_NAME)
				{
					// 是一个常量
					dealedScript = addNormalPart(partList, paramList, subList, index, dealedScript);
					dealedScript = dealedScript.substring(CONSTANT_NAME.length());
					if (dealedScript.length() == 0 || dealedScript.charAt(0) != EXTEND_NAME_BEGIN)
					{
						throw new EternaException("Not found constant name [" + script + "].");
					}
					index = dealedScript.indexOf(EXTEND_NAME_END);
					if (index == -1)
					{
						throw new EternaException("Not end constant name [" + script + "].");
					}
					partScript = new ConstantScript(dealedScript.substring(1, index));
					partList.add(partScript);
					dealedScript = dealedScript.substring(index + 1);
				}
				else if (checked == CHECK_NAME)
				{
					// 是一个检测标识
					dealedScript = addNormalPart(partList, paramList, subList, index, dealedScript);
					dealedScript = dealedScript.substring(CHECK_NAME.length());
					if (dealedScript.length() > 0 && dealedScript.charAt(0) == TEMPLATE_BEGIN)
					{
						index = getEndTemplateIndex(dealedScript);
						partScript = new CheckPart(dealedScript.substring(1, index));
						dealedScript = dealedScript.substring(index + 1);
					}
					else
					{
						// 这里不需要对dealedScript截取子串
						partScript = new CheckPart(null);
					}
					partList.add(partScript);
				}
				else
				{
					// 没有发现匹配的EXTEND类型, 所以作为normal 脚本处理, 扩展标志作为普通字符
					addNormalPart(partList, paramList, subList, index + 1, dealedScript);
					dealedScript = dealedScript.substring(index + 1);
				}
			}
			index = dealedScript.indexOf(EXTEND_FLAG);
		}
		if (dealedScript.length() > 0)
		{
			addNormalPart(partList, paramList, subList, dealedScript.length(), dealedScript);
		}
	}

	/**
	 * @return 此sub脚本前面的参数个数
	 */
	public int setSubPart(int index, String subPart)
			throws EternaException
	{
		if (index < 0 || index >= this.subPartIndexs.length)
		{
			throw new EternaException("The position [" + (index + 1) + "] hasn't sub.");
		}
		if (subPart == null)
		{
			throw new EternaException("The position [" + (index + 1)
					+ "]'s sub can't be setted null.");
		}
		int temp = this.subPartIndexs[index];
		SubPart sp = (SubPart) this.partScripts[temp];
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
				throw new EternaException("The position [" + (index + 1) + "] hasn't sub.");
			}
			if (subParts[i] == null)
			{
				throw new EternaException("The position [" + (index + 1)
						+ "]'s sub can't be setted null.");
			}
			int temp = this.subPartIndexs[index];
			SubPart sp = (SubPart) this.partScripts[temp];
			sp.setSubPart(subParts[i]);
		}
	}

	private void backupSubParts()
	{
		for (int i = 0; i < this.subPartIndexs.length; i++)
		{
			SubPart sp = (SubPart) this.partScripts[this.subPartIndexs[i]];
			sp.backup();
		}
	}

	private void recoverSubParts()
	{
		for (int i = 0; i < this.subPartIndexs.length; i++)
		{
			SubPart sp = (SubPart) this.partScripts[this.subPartIndexs[i]];
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

	public boolean isParamSetted(int index)
			throws EternaException
	{
		ParameterManager pm = this.parameterManagers[index];
		return pm.isParameterSetted();
	}

	public boolean isDynamicParameter(int index)
			throws EternaException
	{
		ParameterManager pm = this.parameterManagers[index];
		return pm.getType() == ParameterManager.DYNAMIC_PARAMETER;
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

	private static String checkExtendType(String dealedScript, int extendFlagIndex)
	{
		int size = dealedScript.length();
		int tempLimit = SUB_SCRIPT_NAME.length() + 1;
		String tempStr;
		if (extendFlagIndex + tempLimit <= size)
		{
			tempStr = dealedScript.substring(extendFlagIndex + 1, extendFlagIndex + tempLimit);
			if (SUB_SCRIPT_NAME.equals(tempStr))
			{
				return SUB_SCRIPT_NAME;
			}
		}
		tempLimit = PARAMETER_NAME.length() + 1;
		if (extendFlagIndex + tempLimit <= size)
		{
			tempStr = dealedScript.substring(extendFlagIndex + 1, extendFlagIndex + tempLimit);
			if (PARAMETER_NAME.equals(tempStr))
			{
				return PARAMETER_NAME;
			}
		}
		tempLimit = CONSTANT_NAME.length() + 1;
		if (extendFlagIndex + tempLimit <= size)
		{
			tempStr = dealedScript.substring(extendFlagIndex + 1, extendFlagIndex + tempLimit);
			if (CONSTANT_NAME.equals(tempStr))
			{
				return CONSTANT_NAME;
			}
		}
		tempLimit = CHECK_NAME.length() + 1;
		if (extendFlagIndex + tempLimit <= size)
		{
			tempStr = dealedScript.substring(extendFlagIndex + 1, extendFlagIndex + tempLimit);
			if (CHECK_NAME.equals(tempStr))
			{
				return CHECK_NAME;
			}
		}
		return null;
	}

	/**
	 * 将结束位置前面部分的脚本作为normal部分, 添加到PartScript列表中. <p>
	 * 添加时, 还将解析脚本中的普通参数和子句标志.
	 *
	 * @return   返回结束位置之后的脚本
	 */
	private static String addNormalPart(List partList, List paramList, List subList, int endIndex,
			String dealedScript)
	{
		String temp = dealedScript.substring(0, endIndex);
		paramList.addAll(getNormalParameters(temp));
		int index = temp.indexOf(SUBPART_FLAG);
		while (index != -1)
		{
			partList.add(new NormalScript(temp.substring(0, index)));
			SubFlagPart sfp = new SubFlagPart();
			partList.add(sfp);
			subList.add(sfp);
			temp = temp.substring(index + 1);
			index = temp.indexOf(SUBPART_FLAG);
		}
		if (temp.length() > 0)
		{
			partList.add(new NormalScript(temp));
		}
		return endIndex < dealedScript.length() ? dealedScript.substring(endIndex + 1) : "";
	}

	private static int getEndTemplateIndex(String dealedScript)
			throws EternaException
	{
		int index = 0;
		do
		{
			index = dealedScript.indexOf(TEMPLATE_END, index + 1);
			if (index == -1)
			{
				throw new EternaException("Not found the template end, dealed script:"
						+ dealedScript + ".");
			}
		// 如果模板结束标记前是个扩展标记, 则继续寻找下一个结束标记
		} while (isExtendBefore(dealedScript, index));
		return index;
	}

	/**
	 * 判断给定位置前是否存在有效的扩展标记. <p>
	 * 出现连续的奇数个扩展标记就是有效的扩展标记.
	 */
	private static boolean isExtendBefore(String dealedScript, int index)
	{
		int count = 0;
		for (int i = index - 1; i >= 0; i--)
		{
			if (dealedScript.charAt(i) == EXTEND_FLAG)
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

	private static String addParamPart(List partList, List paramList, Map paramMap, String dealedScript)
			throws EternaException
	{
		int index = dealedScript.indexOf(EXTEND_NAME_END);
		if (index == -1)
		{
			throw new EternaException("Not end dynamic parameter group name, dealed script:"
					+ dealedScript + ".");
		}

		// 根据组名 归类动态参数
		String group = dealedScript.substring(1, index);
		ParameterManager pm = (ParameterManager) paramMap.get(group);
		if (pm == null)
		{
			pm = new ParameterManager(ParameterManager.DYNAMIC_PARAMETER);
			pm.setGroupName(group);
			paramMap.put(group, pm);
		}

		// 获取动态参数的template
		dealedScript = dealedScript.substring(index + 1);
		if (dealedScript.charAt(0) != TEMPLATE_BEGIN)
		{
			throw new EternaException("Not found dynamic parameter template, dealed script:"
					+ dealedScript + ".");
		}
		index = getEndTemplateIndex(dealedScript);

		String temp = dealedScript.substring(1, index);
		pm.addParameterTemplate(temp);
		PartScript partScript = new ParameterPart(pm, pm.getParameterTemplateCount() - 1);
		partList.add(partScript);
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
		return dealedScript.substring(index + 1);
	}

	private static List getNormalParameters(String script)
	{
		ArrayList paramList = new ArrayList();
		int index = script.indexOf(PARAMETER_FLAG);
		while (index != -1)
		{
			paramList.add(new ParameterManager(ParameterManager.NORMAL_PARAMETER));
			index = script.indexOf(PARAMETER_FLAG, index + 1);
		}
		return paramList;
	}

}


/**
 * SQL脚本的片断, 可以是一部分普通的脚本, 或一个子语句脚本,
 * 或可选参数, 或一个常量.
 */
abstract class PartScript
{
	public void initialize(EternaFactory factory)
			throws EternaException
	{
	}

	public abstract PartScript copy(boolean clear, DaoManager manager);

	public abstract int getLength() throws EternaException;

	public abstract String getScript() throws EternaException;

}

class ParameterPart extends PartScript
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

	public PartScript copy(boolean clear, DaoManager manager)
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

	public String getScript()
			throws EternaException
	{
		String temp = this.paramManager.getParameterTemplate(this.templateIndex);
		return this.paramManager.isParameterSetted() ? temp : "";
	}
}

class SubFlagPart extends PartScript
{
	private String insertString = null;

	public void setSubPart(String subPart)
	{
		this.insertString = subPart;
	}

	public PartScript copy(boolean clear, DaoManager manager)
	{
		SubFlagPart other = new SubFlagPart();
		other.insertString = clear ? null : this.insertString;
		return other;
	}

	public int getLength()
	{
		return this.insertString == null ? 1 : this.insertString.length();
	}

	public String getScript()
	{
		return this.insertString == null ? DaoManager.SUBPART_FLAG + "" : this.insertString;
	}
}

class SubPart extends PartScript
{
	private int flagPartIndex = -1;
	private PartScript[] parts = null;
	private final String template;
	private final int aheadParamCount;
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

	public PartScript copy(boolean clear, DaoManager manager)
	{
		SubPart other = new SubPart(this.template, this.aheadParamCount);
		other.subIndex = this.subIndex;
		other.insertString = clear ? null : this.insertString;
		other.backupString = clear ? null : this.backupString;
		if (this.parts != null)
		{
			other.flagPartIndex = this.flagPartIndex;
			other.parts = new PartScript[this.parts.length];
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
			ArrayList subScriptList = new ArrayList();
			ArrayList subList = new ArrayList();
			DaoManager.parse(this.template, true, partList, paramList, subScriptList, subList);

			if (paramList.size() > 0)
			{
				throw new EternaException( "The parameter flag '?' can't int the sub tamplet:"
						+ this.template + ".");
			}
			if (subList.size() != 1)
			{
				throw new EternaException("Error sub flag count in template ["
						+ this.template + "].");
			}
			this.parts = new PartScript[partList.size()];
			Iterator itr = partList.iterator();
			for (int i = 0; i < this.parts.length; i++)
			{
				PartScript ps = (PartScript) itr.next();
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
			throw new EternaException("Sub part unsetted, template [" + this.template + "].");
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

	public String getScript() throws EternaException
	{
		if (this.insertString == null)
		{
			throw new EternaException("Sub part unsetted, index [" + this.subIndex
					+ "], template [" + this.template + "].");
		}

		if (this.insertString.length() == 0)
		{
			return "";
		}

		StringAppender temp = StringTool.createStringAppender(this.getLength());
		for (int i = 0; i < this.parts.length; i++)
		{
			temp.append(this.parts[i].getScript());
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

class ConstantScript extends PartScript
{
	private final String name;

	private String value = null;

	public ConstantScript(String name)
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
				throw new EternaException("The constant [" + this.name + "] not found.");
			}
			this.value = temp;

			ArrayList partList = new ArrayList();
			ArrayList paramList = new ArrayList();
			ArrayList subScriptList = new ArrayList();
			ArrayList subList = new ArrayList();
			DaoManager.parse(this.value, true, partList, paramList, subScriptList, subList);

			if (paramList.size() > 0)
			{
				throw new EternaException(
						"The parameter flag [?] can't int the constant ["
						+ this.value + "].");
			}
			StringAppender buf = StringTool.createStringAppender(this.value.length() + 16);
			Iterator itr = partList.iterator();
			for (int i = 0; i < partList.size(); i++)
			{
				PartScript ps = (PartScript) itr.next();
				ps.initialize(factory);
				buf.append(ps.getScript());
			}
			this.value = StringTool.intern(buf.toString(), true);
		}
	}

	public PartScript copy(boolean clear, DaoManager manager)
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

	public String getScript()
	{
		if (this.value == null)
		{
			throw new NullPointerException();
		}
		return this.value;
	}

}

class CheckPart extends PartScript
{
	private String hasSubStr;
	private String noneSubStr;
	private boolean checkNormal;
	private boolean end;

	public CheckPart(String params)
	{
		this.paramStr = params;
	}
	private String paramStr;

	private CheckPart(boolean waitEnd)
	{
		this.waitEnd = waitEnd;
	}
	private boolean waitEnd;

	public void initialize(EternaFactory factory)
			throws EternaException
	{
		if (this.paramStr != null)
		{
			String tmpStr = this.paramStr;
			this.paramStr = null;
			super.initialize(factory);
			ArrayList partList = new ArrayList();
			ArrayList paramList = new ArrayList();
			ArrayList subScriptList = new ArrayList();
			ArrayList subList = new ArrayList();
			DaoManager.parse(tmpStr, true, partList, paramList, subScriptList, subList);

			if (paramList.size() > 0)
			{
				throw new EternaException(
						"The parameter flag '?' can't int the check param ["
						+ tmpStr + "].");
			}
			StringAppender buf = StringTool.createStringAppender();
			int count = partList.size();
			Iterator itr = partList.iterator();
			for (int i = 0; i < count; i++)
			{
				PartScript ps = (PartScript) itr.next();
				ps.initialize(factory);
				buf.append(ps.getScript());
			}

			Map map = StringTool.string2Map(buf.toString(), ";", '=', true, false, null, null);
			String tmp;
			if ((tmp = (String) map.get("hasSub")) != null || (tmp = (String) map.get("append")) != null)
			{
				this.hasSubStr = tmp;
			}
			if ((tmp = (String) map.get("noneSub")) != null)
			{
				this.noneSubStr = tmp;
			}
			if ((tmp = (String) map.get("checkNormal")) != null)
			{
				this.checkNormal = BooleanConverter.toBoolean(tmp);
			}
			if ((tmp = (String) map.get("end")) != null)
			{
				this.end = BooleanConverter.toBoolean(tmp);
			}
		}
	}

	/**
	 * 对脚本片段进行检查.
	 *
	 * @param part          需要检查的脚本片段
	 * @param currentCheck  当前的检查片段的引用
	 * @return  经过检查后需要输出的脚本片段
	 */
	public String doCheck(PartScript part, CheckContainer currentCheck)
	{
		if (this.end)
		{
			// 不能直接设置一个结束
			throw new EternaException("Error nesting check.");
		}
		StringAppender currentBuf = currentCheck.buf;
		if (part instanceof CheckPart)
		{
			CheckPart other = (CheckPart) part;
			if (!other.end)
			{
				// 不是结束标记, 作为新的检查, 当前检查保存至parent
				currentCheck.setCheckPart(other, true);
				return "";
			}
			currentCheck.setCheckPart(null, true);
			if (this.waitEnd)
			{
				String result = other.hasSubStr;
				return result != null ? result : "";
			}
			else
			{
				// 不是等待结束标记, 那就是无子句
				String partStr = this.buildResult(
						this.noneSubStr, other.noneSubStr, currentBuf);
				return this.buildParentCheck(partStr, currentCheck);
			}
		}
		if (this.waitEnd)
		{
			String partStr = part == null ? "" : part.getScript();
			if (partStr.trim().length() == 0)
			{
				// 等待结束标记时, 空白片段原样输出
				return partStr;
			}
			if (!(part instanceof SubPart || part instanceof ParameterPart))
			{
				// 遇到非空的非子句, 取消waitEnd
				currentCheck.setCheckPart(null, false);
			}
			return partStr;
		}
		if (part instanceof SubPart || part instanceof ParameterPart)
		{
			String partStr = part.getScript();
			if (partStr.length() == 0)
			{
				return "";
			}
			// 存在子句, 结束当前的检查
			currentCheck.setCheckPart(new CheckPart(true), false);
			String resultStr = this.buildResult(
					this.hasSubStr, this.removeFirst(partStr), currentBuf);
			return this.buildParentCheck(resultStr, currentCheck.parent);
		}
		String partStr = part == null ? "" : part.getScript();
		if (part != null && partStr.trim().length() == 0)
		{
			if (currentCheck.buf == null)
			{
				currentCheck.buf = StringTool.createStringAppender();
			}
			currentCheck.buf.append(partStr);
			return "";
		}
		String beginStr = this.noneSubStr;
		if (part != null && this.checkNormal)
		{
			beginStr = this.hasSubStr;
			partStr = this.removeFirst(partStr);
		}
		// 遇到Normal部分就不需要等待结束标记了
		currentCheck.setCheckPart(null, false);
		return this.buildResult(beginStr, partStr, currentBuf);
	}

	/**
	 * 检查CheckContainer的parent属性并构造子语句.
	 */
	private String buildParentCheck(String part, CheckContainer pCheck)
	{
		if (pCheck == null)
		{
			return part;
		}
		CheckPart cPart = pCheck.checkPart;
		if (cPart == null || cPart.waitEnd)
		{
			// 没有parent或parent为等待, 则返回子语句
			return part;
		}
		if (part.trim().length() > 0)
		{
			String partStr = this.buildResult(
					cPart.hasSubStr, this.removeFirst(part), pCheck.buf);
			pCheck.checkPart = new CheckPart(true);
			pCheck.buf = null;
			return this.buildParentCheck(partStr, pCheck.parent);
		}
		else
		{
			if (pCheck.buf == null)
			{
				pCheck.buf = StringTool.createStringAppender();
			}
			pCheck.buf.append(part);
			return "";
		}
	}

	private String buildResult(String begin, String part, StringAppender currentBuf)
	{
		StringAppender buf = StringTool.createStringAppender();
		if (begin != null)
		{
			buf.append(begin);
		}
		if (currentBuf != null)
		{
			// 如果中间有缓存的字符, 需要在中间输出
			buf.append(currentBuf.toString());
		}
		if (part != null)
		{
			buf.append(part);
		}
		return buf.toString();
	}

	private String removeFirst(String part)
	{
		int count = part.length();
		int begin = 0;
		for (; begin < count; begin++)
		{
			// 忽略起始部分的空格
			if (part.charAt(begin) > ' ')
			{
				break;
			}
		}
		if (begin < count && part.charAt(begin) == ',')
		{
			// 是","就作为第一个字符去除
			return part.substring(begin + 1);
		}
		for (; begin < count; begin++)
		{
			char c = part.charAt(begin);
			if (c <= ' ' || c == ',')
			{
				// 遇到第一个特殊字符, 将前面的字符去除
				break;
			}
		}
		return part.substring(begin);
	}

	public PartScript copy(boolean clear, DaoManager manager)
	{
		return this;
	}

	public int getLength()
	{
		return 0;
	}

	public String getScript()
	{
		return "";
	}

}
/**
 * 存放CheckPart的容器.
 */
class CheckContainer
{
	/**
	 * 存放中间输出的字符的缓存.
	 */
	public StringAppender buf;
	public CheckPart checkPart;

	public void setCheckPart(CheckPart other, boolean dealStack)
	{
		if (dealStack)
		{
			if (other == null)
			{
				if (this.parent != null)
				{
					this.checkPart = this.parent.checkPart;
					this.buf = this.parent.buf;
					this.parent = this.parent.parent;
				}
				else
				{
					this.checkPart = null;
					this.buf = null;
				}
			}
			else
			{
				CheckContainer tmp = new CheckContainer();
				tmp.checkPart = this.checkPart;
				tmp.buf = this.buf;
				tmp.parent = this.parent;
				this.parent = tmp;
				this.checkPart = other;
				this.buf = null;
			}
		}
		else
		{
			if (other == null && this.parent != null)
			{
				// 遇到清空当前CheckPart且不需要处理堆栈时, 堆栈必须是空的
				throw new EternaException("Error nesting check.");
			}
			this.checkPart = other;
			this.buf = other == null ? null : this.buf;
		}
	}
	CheckContainer parent = null;

}

class NormalScript extends PartScript
{
	private final String script;

	public NormalScript(String script)
	{
		if (script == null)
		{
			throw new NullPointerException();
		}
		// 这里的脚本大部分是intern处理过的字符串的字串
		this.script = script;
	}

	public PartScript copy(boolean clear, DaoManager manager)
	{
		return this;
	}

	public int getLength()
	{
		return this.script.length();
	}

	public String getScript()
	{
		return this.script;
	}

}
