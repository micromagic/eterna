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

import org.dom4j.Element;

import self.micromagic.eterna.dao.Update;
import self.micromagic.eterna.dao.impl.ScriptParser;
import self.micromagic.eterna.share.EternaException;
import self.micromagic.eterna.share.EternaFactory;
import self.micromagic.eterna.share.EternaObject;
import self.micromagic.util.StringTool;


/**
 * 执行数据库脚本操作定义的描述.
 */
public class ScriptDesc extends AbstractObject
		implements OptDesc, EternaObject, ConstantDef
{
	// 定义主键重复的常量标识
	private static final String SAME_KEY_FLAG = "EC_sameKey";
	// 操作符: INSERT
	private static final String OPT_INSERT = "insert";
	// 操作符: INTO
	private static final String OPT_INTO = "into";

	/**
	 * 数据操作的脚本.
	 */
	public String script;

	public ScriptDesc()
	{
		this.ignoreSameKey = IgnoreConfig.getCurrentConfig().isIgnoreSameKey();
	}

	public void exec(Connection conn)
			throws SQLException
	{
		Update u = this.factory.createUpdate(COMMON_EXEC);
		u.setSubScript(1, this.script);
		u.execute(conn);
	}

	public boolean initialize(EternaFactory factory)
			throws EternaException
	{
		if (this.factory == null)
		{
			this.factory = factory;
			if (!StringTool.isEmpty(this.script))
			{
				this.script = resolveConst(this.script, factory);
				this.checkIgnore();
			}
			return false;
		}
		return true;
	}
	private EternaFactory factory;

	public EternaFactory getFactory()
	{
		return this.factory;
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

	/**
	 * 检查是否有需要忽略的.
	 */
	private void checkIgnore()
	{
		if (this.ignoreSameKey)
		{
			ScriptParser p = new ScriptParser();
			ScriptParser.Element[] elements = p.parseScript(this.script, 1);
			if (elements.length > 2
					&& OPT_INSERT.equalsIgnoreCase(elements[0].getText())
					&& OPT_INTO.equalsIgnoreCase(elements[1].getText()))
			{
				this.sameKeyCode = Integer.parseInt(
						this.factory.getConstantValue(SAME_KEY_FLAG));
			}
			else
			{
				this.ignoreSameKey = false;
			}
		}
	}
	private boolean ignoreSameKey;
	private int sameKeyCode;

	public boolean isIgnoreError(Throwable error)
	{
		if (this.ignoreSameKey && error instanceof SQLException)
		{
			return this.sameKeyCode == ((SQLException) error).getErrorCode();
		}
		return false;
	}

}
