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

package self.micromagic.dbvm.core;

import java.sql.Connection;

import org.dom4j.Element;

import self.micromagic.dbvm.OptDesc;
import self.micromagic.eterna.share.EternaException;
import self.micromagic.eterna.share.EternaFactory;
import self.micromagic.eterna.share.EternaObject;
import self.micromagic.util.StringAppender;
import self.micromagic.util.StringTool;
import self.micromagic.util.ref.BooleanRef;
import self.micromagic.util.ref.StringRef;

/**
 * 抽象的操作描述.
 */
public abstract class AbstractOptDesc extends AbstractObject
		implements EternaObject, OptDesc
{
	private Element element;
	private final CheckHandler checkHandler;

	public AbstractOptDesc()
	{
		this.checkHandler = CheckHandler.getCurrentCheck();
	}

	public boolean checkNeedExec(Connection conn, int step, StringRef firstMsg)
	{
		if (this.checkHandler == null)
		{
			return true;
		}
		BooleanRef first = new BooleanRef();
		boolean result = this.checkHandler.doCheck(conn, first);
		if (!result && first.value)
		{
			StringAppender buf = StringTool.createStringAppender(56);
			buf.append('[').append(this.checkHandler.tableName);
			if (!StringTool.isEmpty(this.checkHandler.columnName))
			{
				buf.append('.').append(this.checkHandler.columnName);
			}
			if (!StringTool.isEmpty(this.checkHandler.indexName))
			{
				buf.append(" index:").append(this.checkHandler.indexName);
			}
			buf.append("] ").append(this.checkHandler.existsFlag ? "isn't" : "is")
					.append(" exists and ignore step [").append(step);
			int optCount = this.checkHandler.getOptCount();
			if (optCount > 1)
			{
				buf.append('-').append(step + optCount - 1);
			}
			buf.append(']');
			firstMsg.setString(buf.toString());
		}
		return result;
	}

	public void initCheckFlag()
	{
		if (this.checkHandler != null)
		{
			this.checkHandler.setCheckResult(Boolean.TRUE);
		}
	}

	public boolean initialize(EternaFactory factory)
			throws EternaException
	{
		if (super.initialize(factory))
		{
			return true;
		}
		if (this.checkHandler != null)
		{
			this.checkHandler.initialize(factory, this);
		}
		return false;
	}

	public Element getElement()
	{
		return this.element;
	}

	public void setElement(Element element)
	{
		this.element = element;
	}

	public boolean isIgnoreError(Throwable error)
	{
		return false;
	}

}
