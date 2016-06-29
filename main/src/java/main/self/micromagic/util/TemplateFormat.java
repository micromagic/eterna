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

package self.micromagic.util;

import java.util.ArrayList;

import self.micromagic.eterna.dao.ResultFormat;
import self.micromagic.eterna.dao.ResultReader;
import self.micromagic.eterna.dao.ResultRow;
import self.micromagic.eterna.security.Permission;
import self.micromagic.eterna.share.AbstractGenerator;
import self.micromagic.eterna.share.EternaException;
import self.micromagic.eterna.share.EternaFactory;

/**
 * 增加一个format的例子：
 * <format name="link.format" type="String" generator="self.micromagic.util.TemplateFormat"/>
 *    <pattern> <![CDATA[
 *       <a href="/projectManager.dow?projectId=[v]&action=load">[v]</a>
 *    ]]> </pattern>
 * </format>
 */
public class TemplateFormat extends AbstractGenerator
		implements ResultFormat
{
	public static final String DEFAULT_INSERT_VALUE_TAG = "[v]";

	protected String pattern;
	protected String[] patterns;

	/**
	 * 进行格式化输出需要的权限, 如果没有权限, 则不格式化, 直接输出
	 */
	protected String needPermission = null;

	public boolean initialize(EternaFactory factory)
			throws EternaException
	{
		if (this.factory == null)
		{
			this.factory = factory;
			return false;
		}
		return true;
	}
	protected EternaFactory factory;

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
		String temp = obj == null ? "" : obj.toString();
		int count = this.pattern.length() + (this.patterns.length - 1) * temp.length();
		StringAppender buf = StringTool.createStringAppender(count);
		for (int i = 0; i < this.patterns.length; i++)
		{
			if (i > 0)
			{
				buf.append(temp);
			}
			buf.append(this.patterns[i]);
		}
		return buf.toString();
	}

	public boolean useEmptyString()
	{
		return true;
	}

	protected void parseTemplate()
	{
		if (this.pattern == null)
		{
			this.pattern = "";
			this.patterns = StringTool.EMPTY_STRING_ARRAY;
		}
		String valueTag = (String) this.getAttribute("insert_value_tag");
		valueTag = valueTag == null ? DEFAULT_INSERT_VALUE_TAG : valueTag;
		this.needPermission = (String) this.getAttribute("format_permission");
		ArrayList temp = new ArrayList();
		int vtLength = valueTag.length();
		String str = this.pattern;
		int index = str.indexOf(valueTag);
		while (index != -1)
		{
			temp.add(str.substring(0, index));
			str = str.substring(index + vtLength);
			index = str.indexOf(valueTag);
		}
		temp.add(str);
		this.patterns = (String[]) temp.toArray(new String[temp.size()]);
	}

	public Object create()
			throws EternaException
	{
		this.parseTemplate();
		return this;
	}

	public void setType(String type)
	{
	}

	public void setPattern(String pattern)
	{
		this.pattern = pattern;
	}

	public static void main(String[] args)
			throws EternaException
	{
	}

}
