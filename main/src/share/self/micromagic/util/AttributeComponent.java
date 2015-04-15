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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.util.Map;

import self.micromagic.eterna.model.AppData;
import self.micromagic.eterna.share.EternaFactory;
import self.micromagic.eterna.share.EternaException;
import self.micromagic.eterna.view.Component;
import self.micromagic.eterna.view.ComponentGenerator;
import self.micromagic.eterna.view.ViewAdapter;
import self.micromagic.eterna.view.impl.ComponentImpl;

/**
 * 通过factory中attribute的值来生成这个component. <p>
 * component的类型必须设为none, 必须在component中设置名称为attrName的属性,
 * 该值为所使用的factory中attribute的名称.
 * 子component将会替换swapId相同的子节点.
 */
public final class AttributeComponent extends ComponentImpl
		implements Component, ComponentGenerator
{
	public static final String FACTORY_ATTRIBUTE_NAME = "attrName";
	public static final String FILE_SOURCE_NAME = "htmlSource";
	public static final String FILE_ROOT_ATTRIBUTE_NAME = "htmlSource.root";

	private String bodyHTML;
	private String swapFlag;
	private String charset = "UTF-8";

	public AttributeComponent()
	{
		this.type = NORMAL_TYPE_DIV;
	}

	public void initialize(EternaFactory factory, Component parent)
			throws EternaException
	{
		if (this.initialized)
		{
			return;
		}
		super.initialize(factory, parent);

		if (NORMAL_TYPE_DIV.equals(this.type))
		{
			this.initbodyHTML();
		}
		boolean autoSet = true;
		String autoSetStr = (String) this.getAttribute("autoSet");
		if (autoSetStr != null)
		{
			autoSet = "true".equalsIgnoreCase(autoSetStr);
		}
		this.swapFlag = (String) this.getAttribute("swapFlag");

		if (autoSet)
		{
			StringAppender buf = StringTool.createStringAppender();
			buf.appendln().append("if (objConfig.bodyString != null)").appendln().append('{').appendln()
					.append("webObj.html(objConfig.bodyString);").appendln().append('}').appendln();
			if (this.swapFlag == null)
			{
				buf.append("{$ef:swapAttributeComponentSubs}(webObj, objConfig);").appendln();
			}
			else
			{
				buf.append("{$ef:swapAttributeComponentSubs}(webObj, objConfig, \"").append(this.swapFlag)
						.append("\");").appendln();
			}
			String myScript = buf.toString();
			if (this.initScript != null)
			{
				this.initScript = this.initScript + myScript;
			}
			else
			{
				this.initScript = myScript;
			}
		}
	}

	private void initbodyHTML()
			throws EternaException
	{
		String tmp;
		Map bindRes = null;
		tmp = (String) this.getAttribute("bindRes");
		if (tmp != null)
		{
			bindRes = StringTool.string2Map(tmp, ",", ':');
		}

		tmp = (String) this.getAttribute(FACTORY_ATTRIBUTE_NAME);
		if (tmp == null)
		{
			tmp = (String) this.getAttribute(FILE_SOURCE_NAME);
			if (tmp == null)
			{
				throw new EternaException("Must set attribute [" + FACTORY_ATTRIBUTE_NAME
						+ "] or [" + FILE_SOURCE_NAME + "] in AttributeComponent.");
			}
			String fileRoot = (String) factory.getAttribute(FILE_ROOT_ATTRIBUTE_NAME);
			if (fileRoot == null)
			{
				fileRoot = ".";
			}
			else
			{
				fileRoot = Utility.resolveDynamicPropnames(fileRoot);
			}
			File htmlFile = new File(fileRoot, tmp);
			if (!htmlFile.isFile())
			{
				throw new EternaException("Not found html source [" + htmlFile.getPath() + "].");
			}
			tmp = (String) this.getAttribute("charset");
			if (tmp != null)
			{
				this.charset = tmp;
			}
			try
			{
				int size = (int) htmlFile.length();
				StringTool.StringAppenderWriter sw = new StringTool.StringAppenderWriter(size);
				FileInputStream fis = new FileInputStream(htmlFile);
				InputStreamReader isr = new InputStreamReader(fis, this.charset);
				Utility.copyChars(isr, sw);
				isr.close();
				fis.close();
				String htmlStr = sw.toString();
				if (htmlStr.length() < size)
				{
					// 因为bodyHTML是要长期保存的, 所以有多余字符的话就重新生成一个字符串
					this.bodyHTML = new String(htmlStr);
				}
				else
				{
					this.bodyHTML = htmlStr;
				}
			}
			catch (IOException ex)
			{
				throw new EternaException(ex);
			}
		}
		else
		{
			this.bodyHTML = (String) factory.getAttribute(tmp);
			if (this.bodyHTML == null)
			{
				throw new EternaException("Not found attribute [" + tmp + "] in factory.");
			}
		}
		this.bodyHTML = Utility.resolveDynamicPropnames(this.bodyHTML, bindRes);
	}

	public void setType(String type)
			throws EternaException
	{
		if (NORMAL_TYPE_DIV.equals(type) || SPECIAL_TYPE_INHERIT.equals(type))
		{
			this.type = type;
		}
		else
		{
			throw new EternaException("Must set type as [" + NORMAL_TYPE_DIV + "] or ["
					+ SPECIAL_TYPE_INHERIT + "] in AttributeComponent.");
		}
	}

	public void printSpecialBody(Writer out, AppData data, ViewAdapter view)
			throws IOException, EternaException
	{
		super.printSpecialBody(out, data, view);
		out.write(',');
		out.write(NO_SUB_FLAG);
		out.write(":1");
		if (!StringTool.isEmpty(this.bodyHTML))
		{
			out.write(",bodyString:\"");
			this.stringCoder.toJsonStringWithoutCheck(out, this.bodyHTML);
			out.write('"');
		}
		if (SPECIAL_TYPE_INHERIT.equals(this.type))
		{
			out.write(',');
			out.write(INHERIT_GLOBAL_SEARCH);
			out.write(":{gSearch:1");
			if (this.swapFlag != null)
			{
				out.write(',');
				out.write(FLAG_TAG);
				out.write(":\"");
				this.stringCoder.toJsonStringWithoutCheck(out, this.swapFlag);
				out.write('"');
			}
			out.write('}');
		}
	}

}