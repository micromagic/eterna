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

package self.micromagic.eterna.digester;

import org.xml.sax.Attributes;
import self.micromagic.util.StringAppender;
import self.micromagic.util.StringTool;

/**
 * 进行初始化情况记录的初始化规则.
 * 主要记录当前正在生成的是什么对象, 这样在发生异常时可以准确地报告
 * 哪个对象出错了.
 *
 * @author micromagic@sina.com
 */
public class ObjectLogRule extends MyRule
{
	private String attributeName;
	private String objType;

	/**
	 * @param attributeName   配置中指定对象名称的属性
	 * @param objType         对象类型
	 */
	public ObjectLogRule(String attributeName, String objType)
	{
		this.attributeName = attributeName;
		this.objType = objType;
	}

	public void myBegin(String namespace, String name, Attributes attributes)
			throws Exception
	{
		String theName = attributes.getValue(this.attributeName);
		if (theName == null)
		{
			theName = "null";
		}
		StringAppender temp = StringTool.createStringAppender(
				this.objType.length() + theName.length() + 2);
		temp.append(this.objType).append('[').append(theName).append(']');
		ConfigurationException.objName = temp.toString();
	}

	/**
	 * 设置当前正在初始化的配置.
	 */
	public static void setConfigName(String name)
	{
		ConfigurationException.config = name;
	}

	/**
	 * 设置当前正在初始化的对象名称.
	 */
	public static void setObjName(String name)
	{
		ConfigurationException.objName = name;
	}

	/**
	 * 设置当前正在初始化的对象的类型及名称.
	 */
	public static void setObjName(String type, String name)
	{
		if (name == null)
		{
			setObjName(type);
		}
		StringAppender temp = StringTool.createStringAppender(
				type.length() + name.length() + 2);
		temp.append(type).append('[').append(name).append(']');
		ConfigurationException.objName = temp.toString();
	}

}