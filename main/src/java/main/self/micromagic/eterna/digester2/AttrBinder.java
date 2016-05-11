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

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.dom4j.Element;

import self.micromagic.cg.BeanMap;
import self.micromagic.cg.CellAccessInfo;
import self.micromagic.eterna.share.EternaException;
import self.micromagic.util.FormatTool;
import self.micromagic.util.StringTool;
import self.micromagic.util.Utility;
import self.micromagic.util.converter.BooleanConverter;
import self.micromagic.util.ref.IntegerRef;
import self.micromagic.util.ref.StringRef;

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

	/**
	 * 以后面的文字作为值的前缀.
	 */
	static final String TEXT_PREFIX = "$text.";

	/**
	 * 生成一个序号的标识.
	 */
	static final String SERIAL_FLAG = "$serial";

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
	 * 格式: attr:{name1,name2:id2,$body:attr}
	 */
	public static AttrBinder parseConfig(String config, IntegerRef position)
	{
		position.value += 1;
		List attrs = new ArrayList();
		List toNames = new ArrayList();
		while (config.charAt(position.value - 1) != ParseRule.BLOCK_END)
		{
			StringRef name = new StringRef();
			AttrGetter ag = parseGetter(config, position, name, "AttrBinder");
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
	static AttrGetter parseGetter(String config, IntegerRef position, StringRef name,
			String caption)
	{
		int beginPos = position.value;
		int endPos = ParseRule.findItemEnd(config, position);
		String tmpName = config.substring(beginPos, endPos).trim();
		if (tmpName.length() == 0)
		{
			// 没有任何数据, 无法构造AttrBind
			throw new ParseException("Error config [" + config + "] for " + caption + ".");
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
		if (checkSpecialName(tmpName))
		{
			if (BodyAttrGetter.BODY_FLAG.equals(tmpName))
			{
				// $body
				ag = BodyAttrGetter.parseConfig(config, position);
				endPos = ParseRule.findItemEnd(config, position);
			}
			else if (ELEMENT_FLAG.equals(tmpName))
			{
				// $element
				ag = new ElementGetter();
				endPos = ParseRule.findItemEnd(config, position);
			}
			else if (SERIAL_FLAG.equals(tmpName))
			{
				// $serial
				NumberFormat format = null;
				if (pFlag != -1 && pFlag < endPos)
				{
					position.value = pFlag;
					Map params = ParseRule.parseParam(config, position);
					String pattern;
					if ((pattern = (String) params.get("pattern")) != null)
					{
						format = new DecimalFormat(pattern);
					}
				}
				ag = new SerialGetter(format);
				endPos = ParseRule.findItemEnd(config, position);
			}
			else if (tmpName.startsWith(TEXT_PREFIX))
			{
				// $text.xxx
				ag = new TextGetter(tmpName.substring(TEXT_PREFIX.length()));
				endPos = ParseRule.findItemEnd(config, position);
			}
			else
			{
				throw new EternaException("Error name [" + tmpName +"].");
			}
			if (name != null && (sFlag == -1 || sFlag > endPos))
			{
				// 特殊标识必须设置绑定的属性名
				throw new ParseException("Error config [" + config + "] for " + caption + ".");
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
					sag.setIntern(BooleanConverter.toBoolean(bStr));
				}
				if ((bStr = (String) params.get("r")) != null)
				{
					sag.setResolve(BooleanConverter.toBoolean(bStr));
				}
				if ((bStr = (String) params.get("m")) != null)
				{
					sag.setMustExists(BooleanConverter.toBoolean(bStr));
				}
				if ((bStr = (String) params.get("d")) != null)
				{
					sag.setDefaultValue(bStr);
				}
				if ((bStr = (String) params.get("c")) != null)
				{
					sag.setCheckEmpty(BooleanConverter.toBoolean(bStr));
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

	/**
	 * 检查当前名称是否为特殊标识.
	 */
	static boolean checkSpecialName(String name)
	{
		if (BodyAttrGetter.BODY_FLAG.equals(name) || ELEMENT_FLAG.equals(name)
				|| SERIAL_FLAG.equals(name) || name.startsWith(TEXT_PREFIX))
		{
			return true;
		}
		return false;
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
				CellAccessInfo cai = bean.getCellAccessInfo(this.toNames[i]);
				if (cai == null || cai.cellDescriptor.getWriteProcesser() == null)
				{
					String msg = "Can't set property [" + this.toNames[i]
							+ "] for class [" + bean.getBean().getClass().getName() + "].";
					throw new ParseException(msg);
				}
				cai.setValue(value);
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

	public void setCheckEmpty(boolean b)
	{
		this.checkEmpty = b;
	}
	private boolean checkEmpty;

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

	public void setResolve(boolean b)
	{
		this.resolve = b;
	}
	private boolean resolve;

	public Object get(Element el)
	{
		String r = el.attributeValue(this.flag);
		boolean emptyStr = false;
		if (this.checkEmpty && StringTool.isEmpty(r))
		{
			emptyStr = r != null;
			r = null;
		}
		if (r == null && (this.mustExists || this.defaultValue != null))
		{
			if (this.defaultValue != null)
			{
				r = this.defaultValue;
			}
			else
			{
				if (emptyStr)
				{
					throw new ParseException("The attribute [" + this.flag
							+ "] at tag [" + el.getName() + "] is empty.");
				}
				else
				{
					throw new ParseException("Not found attribute [" + this.flag
							+ "] at tag [" + el.getName() + "].");
				}
			}
		}
		if (this.resolve)
		{
			r = Utility.resolveDynamicPropnames(r);
		}
		r = this.intern ? StringTool.intern(r) : r;
		return this.checkEmpty ? StringTool.isEmpty(r) ? null : r : r;
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

/**
 * 文本值的获取者.
 */
class TextGetter
		implements AttrGetter
{
	public TextGetter(String text)
	{
		this.text = text;
	}
	private final String text;

	public Object get(Element el)
	{
		return this.text;
	}

}

/**
 * 序列号的获取者.
 */
class SerialGetter
		implements AttrGetter
{
	public SerialGetter(NumberFormat format)
	{
		this.format = format;
	}
	private final NumberFormat format;

	public Object get(Element el)
	{
		int s = ContainerManager.getNextSerial();
		if (this.format == null)
		{
			return Integer.toString(s);
		}
		return FormatTool.getThreadFormat(format).format(s);
	}

}
