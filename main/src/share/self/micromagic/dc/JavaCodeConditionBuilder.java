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

import self.micromagic.eterna.search.ConditionBuilderGenerator;
import self.micromagic.eterna.search.ConditionBuilder;
import self.micromagic.eterna.search.ConditionProperty;
import self.micromagic.eterna.share.AbstractGenerator;
import self.micromagic.eterna.share.EternaFactory;
import self.micromagic.eterna.share.EternaException;
import self.micromagic.dc.CodeClassTool;
import self.micromagic.util.StringTool;
import self.micromagic.cg.ClassGenerator;

/**
 * 动态编译java代码来构造一个ConditionBuilder.
 *
 * 需设置的属性
 * code                  处理条件生成的java代码                                                   2选1
 * attrCode              从factory的属性中获取处理条件生成的java代码                              2选1
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
public class JavaCodeConditionBuilder extends AbstractGenerator
		implements ConditionBuilder, ConditionBuilderGenerator
{
	private String caption;
	private String operator;
	private ConditionBuilderCode conditionBuilderCode;

	public void initialize(EternaFactory factory)
			throws EternaException
	{
		if (this.conditionBuilderCode != null)
		{
			return;
		}
		String code = CodeClassTool.getCode(this, factory, "code", "attrCode");
		try
		{
			Class codeClass = this.createCodeClass(code);
			this.conditionBuilderCode = (ConditionBuilderCode) codeClass.newInstance();
			this.conditionBuilderCode.setGenerator(this, factory);
		}
		catch (Exception ex)
		{
			if ("true".equalsIgnoreCase((String) this.getAttribute("throwCompileError")))
			{
				if (ex instanceof EternaException)
				{
					throw (EternaException) ex;
				}
				throw new EternaException(ex);
			}
			else
			{
				String pos = "builder:[" + this.getName() + "]";
				CodeClassTool.logCodeError(code, pos, ex);
			}
		}
	}

	public ConditionBuilder.Condition buildeCondition(String colName, String value, ConditionProperty cp)
			throws EternaException
	{
		try
		{
			if (this.conditionBuilderCode != null)
			{
				return this.conditionBuilderCode.invoke(colName, value, cp);
			}
		}
		catch (Exception ex)
		{
			if (ex instanceof EternaException)
			{
				throw (EternaException) ex;
			}
			throw new EternaException(ex);
		}
		return null;
	}

	public String getCaption()
	{
		return this.caption;
	}

	public void setCaption(String caption)
	{
		this.caption = caption;
	}

	public String getOperator()
	{
		return this.operator;
	}

	public void setOperator(String operator)
	{
		this.operator = operator;
	}

	private Class createCodeClass(String code)
			throws Exception
	{
		String extendsStr = (String) this.getAttribute("extends");
		Class extendsClass = ConditionBuilderCodeImpl.class;
		if (extendsStr != null)
		{
			extendsClass = Class.forName(extendsStr, true, Thread.currentThread().getContextClassLoader());
		}
		String rType = ClassGenerator.getClassName(ConditionBuilder.Condition.class);
		String methodHead = "public " + rType + " invoke(String colName, String value, "
				+ "ConditionProperty cp)\n      throws Exception";
		String[] iArr = null;
		String imports = (String) this.getAttribute("imports");
		if (imports != null)
		{
			iArr = StringTool.separateString(imports, ",", true);
		}
		return CodeClassTool.createJavaCodeClass(extendsClass, ConditionBuilderCode.class,
				methodHead, code, iArr);
	}

	public ConditionBuilder createConditionBuilder()
			throws EternaException
	{
		return this;
	}

	public Object create()
			throws EternaException
	{
		return this.createConditionBuilder();
	}

	public interface ConditionBuilderCode
	{
		public void setGenerator(JavaCodeConditionBuilder generator, EternaFactory factory)
				throws EternaException;

		public ConditionBuilder.Condition invoke(String colName, String value, ConditionProperty cp)
				throws Exception;

	}

	public static abstract class ConditionBuilderCodeImpl
			implements ConditionBuilderCode
	{
		protected JavaCodeConditionBuilder generator;
		protected EternaFactory factory;

		public void setGenerator(JavaCodeConditionBuilder generator, EternaFactory factory)
		{
			this.factory = factory;
			this.generator = generator;
		}

	}

}