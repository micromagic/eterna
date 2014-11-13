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

package self.micromagic.dc;

import self.micromagic.eterna.digester.ConfigurationException;
import self.micromagic.eterna.security.Permission;
import self.micromagic.eterna.share.AbstractGenerator;
import self.micromagic.eterna.share.EternaFactory;
import self.micromagic.eterna.sql.ResultFormat;
import self.micromagic.eterna.sql.ResultFormatGenerator;
import self.micromagic.eterna.sql.ResultRow;
import self.micromagic.eterna.sql.ResultReader;
import self.micromagic.dc.CodeClassTool;
import self.micromagic.util.StringTool;

/**
 * 动态编译java代码来构造一个Format.
 *
 * 需设置的属性
 * code                  处理格式化的java代码                                                     2选1
 * attrCode              从factory的属性中获取处理格式化的java代码                                2选1
 *
 * imports               需要引入的包, 如：java.lang, 只需给出包路径, 以","分隔                   可选
 * extends               继承的类                                                                 可选
 * throwCompileError     是否需要将编译的错误抛出, 抛出错误会打断初始化的执行                     默认为false
 *
 * otherParams
 * 预编译处理条件生成代码的参数, 名称需要与代码中的参数名称匹配, 值为参数名对应的代码
 *
 *
 * @author micromagic@sina.com
 */
public class JavaCodeFormat extends AbstractGenerator
		implements ResultFormat, ResultFormatGenerator
{
	private String type;
	private String pattern;
	private FormatCode formatCode;

	public void initialize(EternaFactory factory)
			throws ConfigurationException
	{
		if (this.formatCode != null)
		{
			return;
		}
		String code = CodeClassTool.getCode(this, factory, "code", "attrCode");
		try
		{
			Class codeClass = this.createCodeClass(code);
			this.formatCode = (FormatCode) codeClass.newInstance();
			this.formatCode.setGenerator(this, factory);
		}
		catch (Exception ex)
		{
			if ("true".equalsIgnoreCase((String) this.getAttribute("throwCompileError")))
			{
				if (ex instanceof ConfigurationException)
				{
					throw (ConfigurationException) ex;
				}
				throw new ConfigurationException(ex);
			}
			else
			{
				String pos = "format:[" + this.getName() + "]";
				CodeClassTool.logCodeError(code, pos, ex);
			}
		}
	}

	public Object format(Object obj, ResultRow row, ResultReader reader, Permission permission)
			throws ConfigurationException
	{
		try
		{
			if (this.formatCode != null)
			{
				return this.formatCode.invoke(obj, row, reader, permission);
			}
		}
		catch (Exception ex)
		{
			if (ex instanceof ConfigurationException)
			{
				throw (ConfigurationException) ex;
			}
			throw new ConfigurationException(ex);
		}
		return "";
	}

	public boolean useEmptyString()
	{
		return true;
	}

	public String getType()
	{
		return this.type;
	}

	public void setType(String type)
	{
		this.type = type;
	}

	public String getPattern()
	{
		return this.pattern;
	}

	public void setPattern(String pattern)
	{
		this.pattern = pattern;
	}

	private Class createCodeClass(String code)
			throws Exception
	{
		String extendsStr = (String) this.getAttribute("extends");
		Class extendsClass = FormatCodeImpl.class;
		if (extendsStr != null)
		{
			extendsClass = Class.forName(extendsStr, true, Thread.currentThread().getContextClassLoader());
		}
		String methodHead = "public Object invoke(Object obj, ResultRow row, ResultReader reader, Permission permission)"
				+ "\n      throws Exception";
		String[] iArr = null;
		String imports = (String) this.getAttribute("imports");
		if (imports != null)
		{
			iArr = StringTool.separateString(imports, ",", true);
		}
		return CodeClassTool.createJavaCodeClass(extendsClass, FormatCode.class, methodHead, code, iArr);
	}

	public ResultFormat createFormat()
			throws ConfigurationException
	{
		return this;
	}

	public Object create()
			throws ConfigurationException
	{
		return this.createFormat();
	}

	public interface FormatCode
	{
		public void setGenerator(JavaCodeFormat generator, EternaFactory factory)
				throws ConfigurationException;

		public Object invoke(Object obj, ResultRow row, ResultReader reader, Permission permission)
				throws Exception;

	}

	public static abstract class FormatCodeImpl
			implements FormatCode
	{
		protected JavaCodeFormat generator;
		protected EternaFactory factory;

		public void setGenerator(JavaCodeFormat generator, EternaFactory factory)
		{
			this.factory = factory;
			this.generator = generator;
		}

	}

}