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

package self.micromagic.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections.ReferenceMap;

import self.micromagic.eterna.dao.ResultFormat;
import self.micromagic.eterna.dao.ResultReader;
import self.micromagic.eterna.dao.ResultRow;
import self.micromagic.eterna.security.Permission;
import self.micromagic.eterna.share.AbstractGenerator;
import self.micromagic.eterna.share.EternaException;
import self.micromagic.eterna.share.EternaFactory;

/**
 * 可设置属性的说明.
 *
 * start    起始数值
 * gap      数值的递增值
 */
public class DictionaryFormat extends AbstractGenerator
		implements ResultFormat
{
	public static final String DEFAULT_WORD_SPLIT = ",";
	private static final Map dictionaryCache = new ReferenceMap(ReferenceMap.HARD, ReferenceMap.WEAK);

	protected boolean codeTrans = false;
	protected Map transMap = null;

	protected String pattern;
	protected String type;
	protected String[] words;
	protected String wordSplit;
	protected String elseValue = null;
	protected int start;
	protected int gap;

	/**
	 * 进行格式化输出需要的权限, 如果没有权限, 则不格式化, 直接输出
	 */
	protected String needPermission = null;

	public boolean initialize(EternaFactory factory)
			throws EternaException
	{
		return false;
	}

	public Object format(Object obj, ResultRow row, ResultReader reader, Permission permission)
			throws EternaException
	{
		if (this.needPermission != null && permission != null)
		{
			if (!permission.hasPermission(this.needPermission))
			{
				return obj == null ? "" : obj.toString();
			}
		}
		if (obj != null)
		{
			if (this.codeTrans)
			{
				String tmp = (String) this.transMap.get(String.valueOf(obj));
				if (tmp != null)
				{
					return tmp;
				}
			}
			else if (obj instanceof Number)
			{
				int code = ((Number) obj).intValue();
				int index = (code - this.start) / this.gap;
				if (index < 0 || index >= this.words.length)
				{
					return this.elseValue;
				}
				return this.words[index];
			}
		}
		return this.elseValue;
	}

	public boolean useEmptyString()
	{
		return true;
	}

	protected void parseWords()
			throws EternaException
	{
		if (this.pattern == null)
		{
			this.pattern = "";
		}
		if ("String".equals(this.type))
		{
			this.codeTrans = true;
		}
		else if ("Number".equals(this.type) || "int".equals(this.type))
		{
			this.codeTrans = false;
		}
		else
		{
			throw new EternaException("The type must be [String] or [Number] or [int], but it's ["
					+ this.type + "].");
		}
		this.needPermission = (String) this.getAttribute("format_permission");
		String elseValue = (String) this.getAttribute("else_value");
		this.wordSplit = (String) this.getAttribute("value_split");
		this.wordSplit = this.wordSplit == null || this.wordSplit.length() == 0 ?
				DEFAULT_WORD_SPLIT : this.wordSplit;
		this.elseValue = elseValue == null ? "" : elseValue;
		if (!this.codeTrans)
		{
			this.start = Integer.parseInt((String) this.getAttribute("start"), 0);
			this.gap = Integer.parseInt((String) this.getAttribute("gap"), 1);
		}
		synchronized (dictionaryCache)
		{
			this.words = (String[]) dictionaryCache.get(this.pattern);
			if (this.words == null)
			{
				String str = this.pattern;
				ArrayList temp = new ArrayList();
				int wsLength = this.wordSplit.length();
				int index = str.indexOf(this.wordSplit);
				while (index != -1)
				{
					temp.add(str.substring(0, index));
					str = str.substring(index + wsLength);
					index = str.indexOf(this.wordSplit);
				}
				temp.add(str);
				this.words = (String[]) temp.toArray(new String[temp.size()]);
				dictionaryCache.put(this.pattern, this.words);
			}
			if (this.codeTrans)
			{
				this.transMap = (Map) dictionaryCache.get(this.words);
				if (this.transMap == null)
				{
					if (this.words.length % 2 != 0)
					{
						String msg = "The words count must be an even number, "
								+ "but the count is:" + this.words.length + ".";
						throw new EternaException(msg);
					}
					this.transMap = new HashMap();
					for (int i = 0; i < this.words.length; i += 2)
					{
						String key = this.words[i];
						String value = this.words[i + 1];
						if (this.transMap.put(key, value) != null)
						{
							throw new EternaException("The key words:[" + key + "] appeared more than once.");
						}
					}
					dictionaryCache.put(this.words, this.transMap);
				}
			}
		}
	}

	public Object create()
			throws EternaException
	{
		this.parseWords();
		return this;
	}

	public void setType(String type)
	{
		this.type = type;
	}

	public void setPattern(String pattern)
	{
		this.pattern = pattern;
	}

}