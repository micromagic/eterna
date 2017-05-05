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
import java.util.List;

import org.dom4j.Element;

import self.micromagic.eterna.dao.Update;
import self.micromagic.eterna.dao.impl.ScriptParser;
import self.micromagic.eterna.share.EternaException;
import self.micromagic.eterna.share.EternaFactory;
import self.micromagic.eterna.share.EternaObject;
import self.micromagic.util.StringTool;

/**
 * 数据库索引定义的描述.
 */
public class IndexDesc extends AbstractObject
		implements ConstantDef, EternaObject, OptDesc
{
	/**
	 * 索引的名称.
	 */
	public String indexName;

	/**
	 * 索引对应的表名.
	 */
	public String tableName;

	/**
	 * 是否为主键.
	 */
	public boolean key;

	/**
	 * 是否为唯一键.
	 */
	public boolean unique;

	/**
	 * 是否为外键.
	 */
	public boolean foreign;

	/**
	 * 设置索引的类型.
	 */
	public void setType(String type)
	{
		if ("key".equalsIgnoreCase(type))
		{
			this.key = true;
		}
		else if ("unique".equalsIgnoreCase(type))
		{
			this.unique = true;
		}
		else if ("foreign".equalsIgnoreCase(type))
		{
			this.foreign = true;
		}
	}

	/**
	 * 相关列名列表.
	 */
	public List columns = new ArrayList();

	/**
	 * 添加一个列名.
	 */
	public void addColumn(String colName)
	{
		this.columns.add(ScriptParser.checkNameForQuote(colName));
	}

	/**
	 * 外键关联的表名.
	 */
	public String refName;

	/**
	 * 关联外键的列名列表.
	 */
	public List refColumns = new ArrayList();

	/**
	 * 添加一个列名.
	 */
	public void addRefColumn(String colName)
	{
		this.refColumns.add(ScriptParser.checkNameForQuote(colName));
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
	}

	public boolean initialize(EternaFactory factory)
			throws EternaException
	{
		if (super.initialize(factory))
		{
			return true;
		}
		if (this.foreign && this.columns.size() != this.refColumns.size())
		{
			String msg = "The foreign key [" + this.indexName + "]'s column isn't same, "
					+ this.columns.size() + " != " + this.refColumns.size() + " .";
			throw new EternaException(msg);
		}
		this.indexName = ScriptParser.checkNameForQuote(
				resolveConst(this.indexName, factory));
		this.tableName = ScriptParser.checkNameForQuote(
				resolveConst(this.tableName, factory));
		if (!StringTool.isEmpty(this.refName))
		{
			this.refName = ScriptParser.checkNameForQuote(
					resolveConst(this.refName, factory));
		}
		return false;
	}

	/**
	 * 执行索引描述中所有定义的操作.
	 */
	public void exec(Connection conn)
			throws SQLException
	{
		Update u = this.factory.createUpdate(COMMON_EXEC);
		u.setSubScript(1, this.indexDefiner.getIndexDefine(this, null));
		u.execute(conn);
	}

	public Element getElement()
	{
		return this.element;
	}

	public void setElement(Element element)
	{
		this.element = element;
	}
	private Element element;

	public boolean isIgnoreError(Throwable error)
	{
		return false;
	}

}
