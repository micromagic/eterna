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
import java.util.Map;

import org.dom4j.Element;

import self.micromagic.cg.BeanMap;
import self.micromagic.util.IntegerRef;
import self.micromagic.util.StringRef;
import self.micromagic.util.StringTool;

/**
 * 通过名称来的设置来绑定配置与对象的属性.
 */
public class AttrBinder
		implements ElementProcessor
{
	/**
	 * 获取整个元素节点的标识.
	 */
	static final String ELEMENT_FLAG = "$element";

	static final String __FLAG = "attr:";
	public static AttrBinder getInstance()
	{
		return instance;
	}
	private static AttrBinder instance = new AttrBinder();

	private AttrGetter[] attrs;
	private String[] toNames;

	public ElementProcessor parse(Digester digester, ParseRule rule,
			String config, IntegerRef position)
	{
		return parseConfig(config, position);
	}

	/**
	 * 解析配置信息, 生成AttrBind.
	 * 格式: begin:{name1,name2:id2,$body:attr}
	 */
	public static AttrBinder parseConfig(String config, IntegerRef position)
	{
		position.value += 1;
		List attrs = new ArrayList();
		List toNames = new ArrayList();
		while (config.charAt(position.value - 1) != ParseRule.BLOCK_END)
		{
			StringRef name = new StringRef();
			AttrGetter ag = parseGetter(config, position, name);
			attrs.add(ag);
			toNames.add(name.getString());
		}
		AttrBinder attrBind = new AttrBinder();
		int size = attrs.size();
		attrBind.attrs = new AttrGetter[size];
		attrBind.toNames = new String[size];
		attrs.toArray(attrBind.attrs);
		toNames.toArray(attrBind.toNames);
		return attrBind;
	}

	/**
	 * 解析配置信息, 生成AttrBind单元.
	 * 格式: name1 或 name2:id2 或 $body:attr
	 */
	static AttrGetter parseGetter(String config, IntegerRef position, StringRef name)
	{
		int beginPos = position.value;
		int endPos = ParseRule.findItemEnd(config, position);
		String tmpName = config.substring(beginPos, endPos).trim();
		if (tmpName.length() == 0)
		{
			// 没有任何数据, 无法构造AttrBind
			throw new ParseException("Error config [" + config + "] for AttrBinder.");
		}
		int sFlag = name == null ? -1 : config.indexOf(':', beginPos);  // 属性与绑定名称的分隔符
		int pFlag = config.indexOf('(', beginPos);  // 参数的起始符
		if (pFlag != -1 && pFlag < endPos)
		{
			tmpName = config.substring(beginPos, pFlag).trim();
		}
		else if (sFlag != -1 && sFlag < endPos)
		{
			tmpName = config.substring(beginPos, sFlag).trim();
		}
		AttrGetter ag;
		if (BodyAttrGetter.BODY_FLAG.equals(tmpName))
		{
			// $body
			ag = BodyAttrGetter.parseConfig(config, position);
			endPos = ParseRule.findItemEnd(config, position);
			if (name != null && (sFlag == -1 || sFlag > endPos))
			{
				// $body必须设置绑定的属性名
				throw new ParseException("Error config [" + config + "] for AttrBinder.");
			}
			else if (name != null)
			{
				name.setString(config.substring(sFlag + 1, endPos).trim());
			}
		}
		else if (ELEMENT_FLAG.equals(tmpName))
		{
			// $element
			ag = new ElementGetter();
			endPos = ParseRule.findItemEnd(config, position);
			if (name != null && (sFlag == -1 || sFlag > endPos))
			{
				// $element必须设置绑定的属性名
				throw new ParseException("Error config [" + config + "] for AttrBinder.");
			}
			else if (name != null)
			{
				name.setString(config.substring(sFlag + 1, endPos).trim());
			}
		}
		else
		{
			ag = new StandardAttrGetter(tmpName);
			if (pFlag != -1 && pFlag < endPos)
			{
				StandardAttrGetter sag = (StandardAttrGetter) ag;
				position.value = pFlag;
				Map params = ParseRule.parseParam(config, position);
				String bStr;
				if ((bStr = (String) params.get("i")) != null)
				{
					sag.setIntern(ParseRule.booleanConverter.convertToBoolean(bStr));
				}
				if ((bStr = (String) params.get("m")) != null)
				{
					sag.setMustExists(ParseRule.booleanConverter.convertToBoolean(bStr));
				}
				if ((bStr = (String) params.get("d")) != null)
				{
					sag.setDefaultValue(bStr);
				}
				endPos = ParseRule.findItemEnd(config, position);
			}
			if (sFlag != -1 && sFlag < endPos)
			{
				name.setString(config.substring(sFlag + 1, endPos).trim());
			}
			else if (name != null)
			{
				name.setString(tmpName);
			}
		}
		position.value = endPos + 1;
		return ag;
	}

	public boolean begin(Digester digester, Element element)
	{
		this.bind((BeanMap) digester.peek(0), element);
		return true;
	}

	public void end(Digester digester, Element element)
	{
	}

	public void bind(BeanMap bean, Element element)
	{
		int count = this.attrs.length;
		for (int i = 0; i < count; i++)
		{
			Object value = this.attrs[i].get(element);
			if (value != null)
			{
				bean.put(this.toNames[i], value);
			}
		}
	}

}

/**
 * 一个标准的属性获取者.
 */
class StandardAttrGetter
		implements AttrGetter
{
	StandardAttrGetter(String flag)
	{
		this.flag = flag;
	}
	private final String flag;

	public void setMustExists(boolean b)
	{
		this.mustExists = b;
	}
	private boolean mustExists = true;

	public void setDefaultValue(String d)
	{
		this.defaultValue = d;
		if (d != null)
		{
			this.mustExists = false;
		}
	}
	private String defaultValue;

	public void setIntern(boolean b)
	{
		this.intern = b;
	}
	private boolean intern = true;

	public Object get(Element el)
	{
		String r = el.attributeValue(this.flag);
		if (r == null && (this.mustExists || this.defaultValue != null))
		{
			if (this.defaultValue != null)
			{
				return this.defaultValue;
			}
			throw new ParseException("Not found attribute [" + this.flag
					+ "] at tag [" + el.getName() + "].");
		}
		return this.intern ? StringTool.intern(r) : r;
	}

}

/**
 * 元素节点的获取者.
 */
class ElementGetter
		implements AttrGetter
{
	public Object get(Element el)
	{
		return el;
	}

}
