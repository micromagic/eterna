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

package self.micromagic.eterna.dao.preparer;

import java.sql.SQLException;
import java.util.Arrays;

import self.micromagic.eterna.dao.Dao;
import self.micromagic.eterna.dao.Parameter;
import self.micromagic.eterna.dao.PreparedStatementWrap;
import self.micromagic.eterna.share.EternaException;
import self.micromagic.util.StringAppender;
import self.micromagic.util.StringTool;

public class PreparerManager
{
	public static final ValuePreparer IGNORE_PREPARER = new IgnorePreparer();

	private Dao dao;
	private final ValuePreparer[] preparers;
	private Parameter[] parameterArray = null;
	private PreparerManagerList[] insertedPM = null;

	/**
	 * 构造一个PreparerManager, 需要指定preparers的个数
	 */
	public PreparerManager(int count)
	{
		this.preparers = new ValuePreparer[count];
	}

	/**
	 * 通过指定Parameter数组来构造一个PreparerManager
	 */
	public PreparerManager(Dao dao, Parameter[] parameterArray)
	{
		this(parameterArray.length);
		this.parameterArray = parameterArray;
		this.dao = dao;
	}

	/**
	 * 获取preparers的个数
	 */
	public int getCount()
	{
		return this.preparers.length;
	}

	/**
	 * 获取未设置成"忽略"的参数个数
	 */
	public int getParamCount()
	{
		int count = 0;
		for (int i = 0; i < this.preparers.length; i++)
		{
			if (this.preparers[i] != IGNORE_PREPARER)
			{
				count++;
			}
		}
		if (this.insertedPM != null)
		{
			PreparerManagerList tpml;
			for (int i = 0; i < this.insertedPM.length; i++)
			{
				if (this.insertedPM[i] != null)
				{
					tpml = this.insertedPM[i];
					do
					{
						count += tpml.preparerManager.getParamCount();
						tpml = tpml.next;
					} while (tpml != null);
				}
			}
		}
		return count;
	}

	/**
	 * 判断是否有未"忽略"的参数
	 */
	public boolean hasActiveParam()
	{
		for (int i = 0; i < this.preparers.length; i++)
		{
			if (this.preparers[i] != IGNORE_PREPARER)
			{
				return true;
			}
		}
		if (this.insertedPM != null)
		{
			PreparerManagerList tpml;
			for (int i = 0; i < this.insertedPM.length; i++)
			{
				if (this.insertedPM[i] != null)
				{
					tpml = this.insertedPM[i];
					do
					{
						if (tpml.preparerManager.hasActiveParam())
						{
							return true;
						}
						tpml = tpml.next;
					} while (tpml != null);
				}
			}
		}
		return false;
	}

	/**
	 * 设置一个preparer
	 */
	public void setValuePreparer(ValuePreparer preparer)
			throws EternaException
	{
		try
		{
			this.preparers[preparer.getRelativeIndex() - 1] = preparer;
		}
		catch (Exception ex)
		{
			throw new EternaException(
					"Invalid parameter index:" + (preparer.getRelativeIndex()) + ".");
		}
	}

	/**
	 * 将某个preparer设置为忽略的参数
	 */
	public void setIgnore(int index)
			throws EternaException
	{
		try
		{
			this.preparers[index - 1] = IGNORE_PREPARER;
		}
		catch (Exception ex)
		{
			throw new EternaException(
					"Invalid parameter index:" + (index) + ".");
		}
	}

	/**
	 * 在中间插入一个PreparerManager
	 *
	 * @param pm             要插入的PreparerManager
	 * @param index          插入的参数位置
	 * @param subPartIndex   插入的子sql位置
	 */
	public void inserPreparerManager(PreparerManager pm, int index, int subPartIndex)
	{
		if (pm == null && this.insertedPM == null)
		{
			// 当设置的PreparerManager为null, 且insertedPM未初始化时,
			// 则不作任何操作
			return;
		}
		if (index < 0 || index > this.preparers.length)
		{
			StringAppender buf = StringTool.createStringAppender(18);
			buf.append("[index:").append(index).append(" size:")
					.append(this.preparers.length + 1).append(']');
			throw new IndexOutOfBoundsException(buf.toString());
		}
		if (this.insertedPM == null)
		{
			this.insertedPM = new PreparerManagerList[this.preparers.length + 1];
		}
		this.insertedPM[index] = this.modifyPreparerManagerList(
				this.insertedPM[index], pm, subPartIndex);
	}

	private PreparerManagerList modifyPreparerManagerList(PreparerManagerList pml,
			PreparerManager pm, int subPartIndex)
	{
		if (pml == null)
		{
			// 如果pml为空, pm不为空, 则新建一个pml
			if (pm != null)
			{
				return new PreparerManagerList(subPartIndex, pm);
			}
		}
		else
		{
			PreparerManagerList prepml = pml;
			PreparerManagerList nowpml = pml;
			boolean found = false;
			do
			{
				if (nowpml.subPartIndex == subPartIndex)
				{
					found = true;
					break;
				}
				else if (nowpml.subPartIndex > subPartIndex)
				{
					break;
				}
				prepml = nowpml;
				nowpml = nowpml.next;
			} while (nowpml != null);

			if (found)
			{
				// 如果找到了pml
				if (pm == null)
				{
					// 且pm为空, 则要删除这个pml
					if (prepml == nowpml)
					{
						// prepml == nowpml 则说明是头一个
						return nowpml.next;
					}
					else
					{
						prepml.next = nowpml.next;
					}
				}
				else
				{
					nowpml.preparerManager = pm;
				}
			}
			else if (pm != null)
			{
				// 如果找未到了pml, 且pm不为空
				if (prepml == nowpml)
				{
					// prepml == nowpml 则说明是头一个
					prepml = new PreparerManagerList(subPartIndex, pm);
					prepml.next = nowpml;
					return prepml;
				}
				else
				{
					prepml.next = new PreparerManagerList(subPartIndex, pm);
					prepml.next.next = nowpml;
				}
			}
		}
		return pml;
	}

	/**
	 * 将所有的preparer参数设置到PreparedStatement中
	 */
	public void prepareValues(PreparedStatementWrap stmtWrap)
			throws EternaException, SQLException
	{
		this.prepareValues(stmtWrap, 1, null);
	}

	/**
	 * 将所有的preparer参数设置到PreparedStatement中
	 * 并且会将对配置的参数设置的索引值放到paramIndexs中
	 */
	public void prepareValues(PreparedStatementWrap stmtWrap, int[] paramIndexs)
			throws EternaException, SQLException
	{
		if (paramIndexs != null)
		{
			if (paramIndexs.length < this.preparers.length)
			{
				paramIndexs = null;
			}
			else
			{
				Arrays.fill(paramIndexs, -1);
			}
		}
		this.prepareValues(stmtWrap, 1, paramIndexs);
	}

	/**
	 * 将所有的preparer参数设置到PreparedStatement中
	 *
	 * @return 实际设置的参数个数
	 */
	private int prepareValues(PreparedStatementWrap stmtWrap, int startIndex, int[] paramIndexs)
			throws EternaException, SQLException
	{
		int realIndex = startIndex;
		int settedCount = 0;
		PreparerManagerList tpml;
		for (int i = 0; i < this.preparers.length; i++)
		{
			if (this.insertedPM != null)
			{
				if (this.insertedPM[i] != null)
				{
					tpml = this.insertedPM[i];
					do
					{
						int count = tpml.preparerManager.prepareValues(stmtWrap, realIndex, null);
						realIndex += count;
						settedCount += count;
						tpml = tpml.next;
					} while (tpml != null);
				}
			}
			if (this.preparers[i] == null)
			{
				StringAppender buf = StringTool.createStringAppender(52);
				if (this.dao != null)
				{
					buf.append("In").append(this.dao.getType()).append(" [")
							.append(this.dao.getName()).append(']');
					buf.append(", the parameter");
				}
				else
				{
					buf.append("The parameter");
				}
				if (this.parameterArray != null)
				{
					buf.append(" [").append(this.parameterArray[i].getName()).append(']');
				}
				buf.append(" not setted. real:").append(realIndex);
				buf.append(" relative:").append(i + 1).append('.');
				throw new EternaException(buf.toString());
			}
			else
			{
				if (this.preparers[i] != IGNORE_PREPARER)
				{
					this.preparers[i].setValueToStatement(realIndex, stmtWrap);
					if (paramIndexs != null)
					{
						paramIndexs[i] = realIndex;
					}
					realIndex++;
					settedCount++;
				}
			}
		}
		if (this.insertedPM != null)
		{
			if (this.insertedPM[this.preparers.length] != null)
			{
				tpml = this.insertedPM[this.preparers.length];
				do
				{
					int count = tpml.preparerManager.prepareValues(stmtWrap, realIndex, null);
					realIndex += count;
					settedCount += count;
					tpml = tpml.next;
				} while (tpml != null);
			}
		}
		return settedCount;
	}

	private static class PreparerManagerList
	{
		public int subPartIndex;
		public PreparerManager preparerManager;
		public PreparerManagerList next;

		public PreparerManagerList(int subPartIndex, PreparerManager preparerManager)
		{
			this.subPartIndex = subPartIndex;
			this.preparerManager = preparerManager;
			this.next = null;
		}

	}

	private static class IgnorePreparer
			implements ValuePreparer
	{
		public PreparerCreater getCreater()
				throws EternaException
		{
			return null;
		}

		public void setValueToStatement(int index, PreparedStatementWrap stmtWrap)
				throws SQLException
		{
		}

		public void setName(String name)
		{
		}

		public String getName()
		{
			return null;
		}

		public void setRelativeIndex(int index)
		{
		}

		public int getRelativeIndex()
		{
			return 0;
		}

	}

}