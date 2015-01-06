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

import org.dom4j.Element;

import self.micromagic.eterna.share.EternaException;
import self.micromagic.util.StringAppender;
import self.micromagic.util.StringTool;
import self.micromagic.util.container.ThreadCache;

/**
 * 当适配器<code>Adapter</code>相关的配置文件不正确，或不正确的
 * 使用适配器的时候就会抛出该异常.
 *
 * @author micromagic@sina.com
 */
public class ParseException extends EternaException
{
	static String CONTEXT_INFO_TAG = "eterna.context.info";

	/**
	 * 构造一个<code>ParseException</code>.
	 */
	public ParseException()
	{
		super();
		this.initContextInfo();
	}

	/**
	 * 通过参数<code>message</code>来构造一个<code>ParseException</code>.
	 *
	 * @param message   出错信息
	 */
	public ParseException(String message)
	{
		super(message);
		this.initContextInfo();
	}

	/**
	 * 通过通过一个抛出的对象来构造一个<code>ParseException</code>.
	 *
	 * @param origin    异常或错误
	 */
	public ParseException(Throwable origin)
	{
		super(origin);
		this.initContextInfo();
	}

	/**
	 * 通过参数<code>message</code>和一个抛出的对象来构造一个<code>ParseException</code>.
	 *
	 * @param message   出错信息
	 * @param origin    异常或错误
	 */
	public ParseException(String message, Throwable origin)
	{
		super(message, origin);
		this.initContextInfo();
	}

	/**
	 * 初始化当前的环境信息.
	 */
	private void initContextInfo()
	{
		this.contextInfo = (ContextInfo) ThreadCache.getInstance().getProperty(CONTEXT_INFO_TAG);
	}
	private ContextInfo contextInfo;

	public String getMessage()
	{
		String msg = super.getMessage();
		msg = msg == null ? "" : msg;
		ContextInfo ci = this.contextInfo;
		if (ci == null)
		{
			return msg;
		}
		StringAppender temp = StringTool.createStringAppender(msg.length() + 32);
		if (!StringTool.isEmpty(ci.config))
		{
			temp.append("Config:").append(ci.config).append("; ");
		}
		if (!StringTool.isEmpty(ci.uri))
		{
			temp.append("URI:").append(ci.uri).append("; ");
		}
		if (!StringTool.isEmpty(ci.objName))
		{
			temp.append("Object:").append(ci.objName).append("; ");
		}
		if (ci.element != null)
		{
			temp.append("Tag:").append(ci.element.getName());
			if (ci.element instanceof EternaElement)
			{
				int lineNum = ((EternaElement) ci.element).getLineNumber();
				if (lineNum != -1)
				{
					temp.append('(').append(lineNum).append(')');
				}
			}
			temp.append("; ");
		}
		temp.append("Message:").append(msg);
		return temp.toString();
	}

	/**
	 * 获取上下文环境信息.
	 */
	static ContextInfo getContextInfo()
	{
		ThreadCache cache = ThreadCache.getInstance();
		ContextInfo ci = (ContextInfo) cache.getProperty(CONTEXT_INFO_TAG);
		if (ci == null)
		{
			ci = new ContextInfo();
			cache.setProperty(CONTEXT_INFO_TAG, ci);
		}
		return ci;
	}

	/**
	 * 设置上下文环境信息.
	 */
	static void setContextInfo(String uri, String config)
	{
		ContextInfo ci = getContextInfo();
		if (uri != null)
		{
			ci.uri = uri;
		}
		if (config != null)
		{
			ci.config = config;
		}
	}

	/**
	 * 设置上下文环境信息.
	 */
	static void setContextInfo(String objName, Element element)
	{
		ContextInfo ci = getContextInfo();
		if (objName != null)
		{
			ci.objName = objName;
		}
		if (element != null)
		{
			ci.element = element;
		}
	}

	private static final long serialVersionUID = 1L;

}

class ContextInfo
{
	public Element element;
	public String objName;
	public String uri;
	public String config;

}