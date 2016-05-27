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

package self.micromagic.dbvm;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import self.micromagic.dbvm.impl.CommonUpdate;
import self.micromagic.eterna.dao.Update;
import self.micromagic.eterna.dao.preparer.PreparerManager;
import self.micromagic.eterna.dao.preparer.ValuePreparer;
import self.micromagic.eterna.share.EternaException;
import self.micromagic.eterna.share.EternaFactory;
import self.micromagic.eterna.share.EternaObject;
import self.micromagic.util.StringAppender;
import self.micromagic.util.StringTool;

/**
 * 数据库表定义的描述.
 */
public class TableDesc extends AbstractObject
		implements ConstantDef, EternaObject, OptDesc
{
	/**
	 * 表名.
	 */
	public String tableName;

	/**
	 * 修改列时, 新的表名.
	 */
	public String newName;

	/**
	 * 表注释.
	 */
	public String desc;

	/**
	 * 表中的列描述列表.
	 */
	public List columns = new ArrayList();

	/**
	 * 添加一个列.
	 */
	public void addColumn(ColumnDesc colDesc)
	{
		if (this.optType == OPT_TYPE_CREATE && colDesc.optType != OPT_TYPE_CREATE)
		{
			throw new EternaException("Cant't set other column \"optType\" int create table.");
		}
		this.columns.add(colDesc);
	}

	/**
	 * 操作方式.
	 */
	public int optType = OPT_TYPE_CREATE;

	/**
	 * 设置操作的名称.
	 */
	public void setOptName(String optName)
	{
		if ("drop".equalsIgnoreCase(optName))
		{
			this.optType = OPT_TYPE_DROP;
		}
		else if("modify".equalsIgnoreCase(optName))
		{
			this.optType = OPT_TYPE_MODIFY;
		}
	}

	public boolean initialize(EternaFactory factory)
			throws EternaException
	{
		if (super.initialize(factory))
		{
			return true;
		}
		this.comment = (TableComment) factory.createObject(TABLE_COMM_NAME);
		this.renameOpt = factory.getConstantValue("rename");
		this.tableName = resolveConst(this.tableName, factory);
		Iterator itr = this.columns.iterator();
		while (itr.hasNext())
		{
			ColumnDesc colDesc = (ColumnDesc) itr.next();
			if (!StringTool.isEmpty(colDesc.defaultValue))
			{
				colDesc.defaultValue = resolveConst(colDesc.defaultValue, factory);
			}
		}
		return false;
	}
	protected String renameOpt;
	private TableComment comment;

	/**
	 * 执行表描述中所有定义的操作.
	 */
	public void exec(Connection conn)
			throws SQLException
	{
		Update u = this.factory.createUpdate(COMMON_EXEC);
		if (this.optType == OPT_TYPE_DROP)
		{
			u.setSubScript(1, "drop table " + this.tableName);
			u.execute(conn);
			return;
		}
		else if (this.optType == OPT_TYPE_CREATE)
		{
			StringAppender buf = StringTool.createStringAppender(512);
			buf.append("create table ").append(this.tableName).append(" (");
			if (this.mutipleLine)
			{
				buf.appendln();
			}
			boolean first = true;
			List paramList = new LinkedList();
			Iterator itr = this.columns.iterator();
			while (itr.hasNext())
			{
				ColumnDesc cDesc = (ColumnDesc) itr.next();
				if (first)
				{
					first = false;
				}
				else
				{
					buf.append(',');
					if (this.mutipleLine)
					{
						buf.appendln();
					}
				}
				if (this.mutipleLine)
				{
					buf.append("   ");
				}
				buf.append(this.columnDefiner.getColumnDefine(this, cDesc, paramList));
			}
			if (this.mutipleLine)
			{
				buf.appendln();
			}
			buf.append(')');
			PreparerManager m = this.filterPreparer(paramList);
			if (m == null)
			{
				u.setSubScript(1, buf.toString());
			}
			else
			{
				u.setSubScript(1, buf.toString(), m);
			}
			if (!StringTool.isEmpty(this.desc))
			{
				Update tmp = this.factory.createUpdate(COMMON_EXEC);
				tmp.setSubScript(1, this.comment.getComment(this));
				paramList.add(tmp);
			}
			this.execWithList(u, paramList, conn);
		}
		else if (this.optType == OPT_TYPE_MODIFY)
		{
			if (!StringTool.isEmpty(this.newName))
			{
				String s = "alter table " + this.tableName + " " + this.renameOpt
						+ " " + this.newName;
				u.setSubScript(1, s);
				u.execute(conn);
			}
			this.execColumnModify(u, conn);
		}
	}

	/**
	 * 执行表中各列的修改操作.
	 */
	private void execColumnModify(Update update, Connection conn)
			throws SQLException
	{
		Iterator itr = this.columns.iterator();
		while (itr.hasNext())
		{
			ColumnDesc cDesc = (ColumnDesc) itr.next();
			List paramList = new LinkedList();
			StringAppender buf = StringTool.createStringAppender(32);
			buf.append(this.columnDefiner.getColumnDefine(this, cDesc, paramList));

			PreparerManager m = this.filterPreparer(paramList);
			if (m == null)
			{
				update.setSubScript(1, buf.toString());
			}
			else
			{
				update.setSubScript(1, buf.toString(), m);
			}
			if (this.desc != null)
			{
				// 修改时注释需要判空
				Update tmp = this.factory.createUpdate(COMMON_EXEC);
				tmp.setSubScript(1, this.comment.getComment(this));
				paramList.add(tmp);
			}
			this.execWithList(update, paramList, conn);
		}
	}

	/**
	 * 从列表中过滤出参数.
	 */
	private PreparerManager filterPreparer(List list)
	{
		List tmp = new ArrayList();
		Iterator itr = list.iterator();
		while (itr.hasNext())
		{
			Object obj = itr.next();
			if (obj instanceof ValuePreparer)
			{
				ValuePreparer p = (ValuePreparer) obj;
				tmp.add(p);
				p.setRelativeIndex(tmp.size());
				// 如果是个参数, 从列表中移除
				itr.remove();
			}
		}
		int count = tmp.size();
		if (count > 0)
		{
			PreparerManager m = new PreparerManager(count);
			itr = tmp.iterator();
			for (int i = 0; i < count; i++)
			{
				m.setValuePreparer((ValuePreparer) itr.next());
			}
			return m;
		}
		return null;
	}

	/**
	 * 执行列表中的操作.
	 */
	private void execWithList(Update main, List list, Connection conn)
			throws SQLException
	{
		Exception err = null;
		try
		{
			main.execute(conn);
		}
		catch (Exception ex)
		{
			err = ex;
			// 如果出错, 接下来的执行设置为记录模式
			CommonUpdate.setLogScript(true);
		}
		Iterator itr = list.iterator();
		while (itr.hasNext())
		{
			Object obj = itr.next();
			if (obj instanceof Update)
			{
				try
				{
					((Update) obj).execute(conn);
				}
				catch (Exception ex)
				{
					if (err == null)
					{
						err = ex;
					}
					// 如果出错, 接下来的执行设置为记录模式
					CommonUpdate.setLogScript(true);
				}
			}
		}
		if (err != null)
		{
			if (err instanceof SQLException)
			{
				throw (SQLException) err;
			}
			if (err instanceof RuntimeException)
			{
				throw (RuntimeException) err;
			}
			throw new EternaException(err);
		}
	}

}
