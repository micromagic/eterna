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

package self.micromagic.eterna.digester2;

import java.util.Map;
import java.util.HashMap;

import org.dom4j.Element;
import self.micromagic.util.container.ThreadCache;
import self.micromagic.util.IntegerRef;

/**
 * 检查父配置中的对象是否与子配置中相同.
 */
public class SameCheck
		implements ElementProcessor
{
	public static final String __FLAG = "same:";
	public static SameCheck getInstance()
	{
		return instance;
	}
	private static SameCheck instance = new SameCheck();

	public ElementProcessor parse(Digester digester, String config, IntegerRef position)
	{
		int nBegin = position.value += 1;
		int nEnd = ParseRule.findItemEnd(config, position);
		String aName = config.substring(nBegin, nEnd).trim();
		if (config.charAt(nEnd) != ParseRule.BLOCK_END)
		{
			// 没有块结束符
			throw new ParseException("Error config [" + config + "] for SameCheck.");
		}
		position.value = nEnd + 1;
		SameCheck r = new SameCheck();
		r.attrName = aName.length() > 0 ? aName : null;
		return r;
	}
	private String attrName;

	public boolean begin(Digester digester, Element element)
	{
		String objFlag = element.getName();
		if (this.attrName != null)
		{
			objFlag = objFlag + ":" + element.attributeValue(this.attrName);
		}
		ThreadCache cache = ThreadCache.getInstance();
		Map dealedObjMap = (Map) cache.getProperty(DEALED_OBJECT_MAP);
		if (dealedObjMap == null)
		{
			dealedObjMap = new HashMap();
			cache.setProperty(DEALED_OBJECT_MAP, dealedObjMap);
		}
		boolean hasObj = dealedObjMap.put(objFlag, Boolean.TRUE) != null;
		if (hasObj && ContainerManager.getSuperInitLevel() > 0)
		{
			if (ContainerManager.log.isDebugEnabled())
			{
				ContainerManager.log.debug(objFlag + " has bean overwrited.");
			}
			return false;
		}
		return true;
	}
	static final String DEALED_OBJECT_MAP = "_eterna.parse.dealedObj_";

	public void end(Digester digester, Element element)
	{
	}

}