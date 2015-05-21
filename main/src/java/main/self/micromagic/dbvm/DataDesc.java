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

import self.micromagic.eterna.dao.Update;
import self.micromagic.eterna.share.EternaException;
import self.micromagic.eterna.share.EternaFactory;
import self.micromagic.eterna.share.EternaObject;
import self.micromagic.util.StringTool;


/**
 * 数据库操作定义的描述.
 */
public class DataDesc extends AbstractObject
		implements OptDesc, EternaObject, ConstantDef
{
	/**
	 * 数据操作的脚本.
	 */
	public String script;

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

}
