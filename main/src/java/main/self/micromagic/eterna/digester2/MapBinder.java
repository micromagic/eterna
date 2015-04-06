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

import java.util.ArrayList;
import java.util.List;

import org.dom4j.Element;

import self.micromagic.cg.BeanMap;
import self.micromagic.eterna.share.Tool;
import self.micromagic.util.ref.IntegerRef;
import self.micromagic.util.ref.StringRef;

/**
 * 通过名称及其值来设置一个类似map的对象.
 */
public class MapBinder
		implements ElementProcessor
{
	static final String __FLAG = "map:";
	public static MapBinder getInstance()
	{
		return instance;
	}
	private static MapBinder instance = new MapBinder();

	private String methodName;
	private AttrGetter[] attrs;
	private String[] toNames;

	public ElementProcessor parse(Digester digester, ParseRule rule,
			String config, IntegerRef position)
	{
		return parseConfig(config, position);
	}

	/**
	 * 解析配置信息, 生成MapBinder.
	 * 格式: map:{name1,name2:id2,$body:attr}
	 */
	public static MapBinder parseConfig(String config, IntegerRef position)
	{
		int mBegin = position.value += 1;
		int mEnd = ParseRule.findItemEnd(config, position);
		String mName = config.substring(mBegin, mEnd).trim();
		if (mName.length() == 0)
		{
			// 没有方法名不能解析为MethodBinder
			throw new ParseException("Error config [" + config + "] for MapBinder.");
		}
		List attrs = new ArrayList();
		List toNames = new ArrayList();
		position.value = mEnd + 1;
		while (config.charAt(position.value - 1) != ParseRule.BLOCK_END)
		{
			StringRef name = new StringRef();
			AttrGetter ag = AttrBinder.parseGetter(config, position, name, "MapBinder");
			attrs.add(ag);
			toNames.add(name.getString());
		}
		MapBinder mapBind = new MapBinder();
		mapBind.methodName = mName;
		int size = attrs.size();
		mapBind.attrs = new AttrGetter[size];
		mapBind.toNames = new String[size];
		attrs.toArray(mapBind.attrs);
		toNames.toArray(mapBind.toNames);
		return mapBind;
	}

	public boolean begin(Digester digester, Element element)
	{
		Object obj = digester.peek(0);
		if (obj instanceof BeanMap)
		{
			obj = ((BeanMap) obj).getBean();
		}
		this.bind(obj, element);
		return true;
	}

	public void end(Digester digester, Element element)
	{
	}

	public void bind(Object obj, Element element)
	{
		try
		{
			int count = this.attrs.length;
			for (int i = 0; i < count; i++)
			{
				Object value = this.attrs[i].get(element);
				if (value != null)
				{
					Object[] args = new Object[2];
					args[0] = this.toNames[i]; // name;
					args[1] = value; // value;
					Tool.invokeExactMethod(obj, this.methodName, args);
				}
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

}

