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

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import self.micromagic.app.BaseExecute;
import self.micromagic.eterna.model.AppData;
import self.micromagic.eterna.model.ModelExport;
import self.micromagic.eterna.share.EternaFactory;
import self.micromagic.eterna.share.EternaException;
import self.micromagic.util.StringTool;
import self.micromagic.cg.ClassGenerator;

/**
 * 动态编译java代码来构造一个执行器.
 *
 * 需设置的属性
 * code                  执行的java代码                                                           2选1
 * attrCode              从factory的属性中获取执行的java代码                                      2选1
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
public class JavaCodeExecute extends BaseExecute
{
	private ExecuteCode executeCode;

	protected void plusInit()
			throws EternaException
	{
		this.executeType = "javaCode";
		String code = CodeClassTool.getCode(this, this.factory, "code", "attrCode");
		try
		{
			Class codeClass = this.createCodeClass(code);
			this.executeCode = (ExecuteCode) codeClass.newInstance();
			this.executeCode.setGenerator(this, this.factory);
			this.executeType = "javaCode:" + ClassGenerator.getClassName(this.executeCode.getClass());
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
				String pos = "model:[" + this.getModelAdapter().getName() + "], execute:["
						+ this.getName() + "]";
				CodeClassTool.logCodeError(code, pos, ex);
			}
		}
	}

	private Class createCodeClass(String code)
			throws Exception
	{
		String extendsStr = (String) this.getAttribute("extends");
		Class extendsClass = ExecuteCodeImpl.class;
		if (extendsStr != null)
		{
			extendsClass = Class.forName(extendsStr, true, Thread.currentThread().getContextClassLoader());
		}
		String methodHead = "public Object invoke(AppData data, Connection conn)\n      throws Exception";
		String[] iArr = null;
		String imports = (String) this.getAttribute("imports");
		if (imports != null)
		{
			iArr = StringTool.separateString(imports, ",", true);
		}
		return CodeClassTool.createJavaCodeClass(extendsClass, ExecuteCode.class, methodHead, code, iArr);
	}

	protected ModelExport dealProcess(AppData data, Connection conn)
			throws EternaException, SQLException, IOException, InnerExport
	{
		if (this.executeCode == null)
		{
			return null;
		}
		try
		{
			Object obj = this.executeCode.invoke(data, conn);
			if (obj instanceof ModelExport)
			{
				return (ModelExport) obj;
			}
			return null;
		}
		catch (Exception ex)
		{
			if (ex instanceof EternaException)
			{
				throw (EternaException) ex;
			}
			if (ex instanceof SQLException)
			{
				throw (SQLException) ex;
			}
			if (ex instanceof IOException)
			{
				throw (IOException) ex;
			}
			if (ex instanceof InnerExport)
			{
				throw (InnerExport) ex;
			}
			throw new EternaException(ex);
		}
	}

	public interface ExecuteCode
	{
		public void setGenerator(JavaCodeExecute generator, EternaFactory factory);

		public Object invoke(AppData data, Connection conn) throws Exception;

	}

	public static abstract class ExecuteCodeImpl extends BaseExecute
			implements ExecuteCode
	{
		protected JavaCodeExecute generator;

		public void setGenerator(JavaCodeExecute generator, EternaFactory factory)
		{
			this.factory = factory;
			this.generator = generator;
		}

	}

}