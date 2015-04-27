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

package self.micromagic.eterna.digester2;

import org.dom4j.Element;

import self.micromagic.eterna.share.EternaFactory;
import self.micromagic.eterna.share.Tool;
import self.micromagic.util.StringTool;
import self.micromagic.util.ref.IntegerRef;

/**
 * 创建一个对象.
 */
public class ObjectCreater
		implements ElementProcessor
{
	public static final String __FLAG = "create:";
	public static ObjectCreater getInstance()
	{
		return instance;
	}
	private static ObjectCreater instance = new ObjectCreater();

	private String attrName;
	private String defaultValue;
	private boolean registerFactory;

	public ElementProcessor parse(Digester digester, ParseRule rule,
			String config, IntegerRef position)
	{
		int nBegin = position.value += 1;
		int nEnd = ParseRule.findItemEnd(config, position);
		String aName = config.substring(nBegin, nEnd).trim();
		int paramIndex = 0;
		String dValue = null;
		boolean registerFactory = false;
		position.value = nEnd + 1;
		while (config.charAt(position.value - 1) != ParseRule.BLOCK_END)
		{
			int tmpBegin = position.value;
			int tmpEnd = ParseRule.findItemEnd(config, position);
			String tmpStr = config.substring(tmpBegin, tmpEnd).trim();
			if (paramIndex == 0)
			{
				dValue = tmpStr;
			}
			else if (paramIndex == 1)
			{
				registerFactory = ParseRule.booleanConverter.convertToBoolean(tmpStr);
			}
			paramIndex++;
			position.value = tmpEnd + 1;
		}
		if (StringTool.isEmpty(dValue) && StringTool.isEmpty(aName))
		{
			throw new ParseException("Error config [" + config + "] for ObjectCreater.");
		}
		ObjectCreater r = new ObjectCreater();
		r.attrName = StringTool.isEmpty(aName) ? null : aName;
		r.defaultValue = StringTool.isEmpty(dValue) ? null : dValue;
		r.registerFactory = registerFactory;
		return r;
	}

	public boolean begin(Digester digester, Element element)
	{
		String cName = null;
		boolean noAttr = false;
		if (this.attrName != null)
		{
			cName = element.attributeValue(this.attrName);
		}
		if (StringTool.isEmpty(cName))
		{
			noAttr = true;
			if (this.defaultValue == null)
			{
				throw new ParseException("Not found attribute [" + this.attrName
						+ "] at tag [" + element.getName() + "].");
			}
			cName = this.defaultValue;
		}
		Object obj;
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		if (this.registerFactory)
		{
			try
			{
				obj = ContainerManager.getCurrentFactory();
				Class c = Tool.getClass(cName, loader);
				if (obj == null)
				{
					obj = c.newInstance();
					ContainerManager.setCurrentFactory((EternaFactory) obj);
					if (Digester.log.isDebugEnabled())
					{
						Digester.log.debug("Factory [" + cName + "] has created.");
					}
				}
				else if (!noAttr && c != obj.getClass())
				{
					String msg = "The factory isn't same, current [" + obj.getClass().getName()
							+ "], wanted [" + cName + "].";
					Digester.log.error(msg, new ParseException());
				}
			}
			catch (RuntimeException ex)
			{
				throw ex;
			}
			catch (Exception ex)
			{
				throw new ParseException(ex);
			}
		}
		else
		{
			obj = Tool.createBeanMap(cName, loader, true);
		}
		digester.push(obj);
		return true;
	}

	public void end(Digester digester, Element element)
	{
		digester.pop();
	}

}