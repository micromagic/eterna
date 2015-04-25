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

package self.micromagic.cg;

import java.lang.reflect.Modifier;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.io.InputStream;
import java.io.Reader;
import java.io.OutputStream;
import java.io.Writer;
import java.sql.Connection;
import java.sql.ResultSet;

import self.micromagic.util.Utility;
import self.micromagic.util.StringTool;

/**
 * 默认的bean检测器.
 *
 * @author micromagic@sina.com
 */
class DefaultBeanChecker
		implements BeanChecker
{
	public int check(Class beanClass)
	{
		if (beanClass == null)
		{
			return CHECK_RESULT_NO;
		}
		String beanClassName = beanClass.getName();
		// java, javax 及 org 包下的类不是bean
		if (beanClassName.startsWith("java.") || beanClassName.startsWith("javax.")
				|| beanClassName.startsWith("org."))
		{
			return CHECK_RESULT_NO;
		}
		// 基本类型 数组 接口 不是bean
		if (beanClass.isPrimitive() || beanClass.isArray() || beanClass.isInterface())
		{
			return CHECK_RESULT_NO;
		}
		// 非public的类不是bean
		if (!Modifier.isPublic(beanClass.getModifiers()))
		{
			BeanTool.beanClassNameCheckMap.put(beanClassName, Boolean.FALSE);
			return CHECK_RESULT_NO;
		}
		// 实现了基本接口的类不是bean
		if (isBaseType(beanClass))
		{
			BeanTool.beanClassNameCheckMap.put(beanClassName, Boolean.FALSE);
			return CHECK_RESULT_NO;
		}
		try
		{
			BeanMethodInfo[] arr = BeanMethodInfo.getBeanMethods(beanClass);
			// 不存在属性信息的类或公共属性的不是bean
			if (arr == null || arr.length == 0)
			{
				boolean hasPublicField = false;
				Field[] fields = beanClass.getFields();
				for (int i = 0; i < fields.length; i++)
				{
					if (!Modifier.isStatic(fields[i].getModifiers()))
					{
						hasPublicField = true;
						break;
					}
				}
				if (!hasPublicField)
				{
					BeanTool.beanClassNameCheckMap.put(beanClassName, Boolean.FALSE);
					return CHECK_RESULT_NO;
				}
			}
			// 不存在无参的构造函数的类不是bean
			if (beanClass.getConstructor(new Class[0]) == null)
			{
				BeanTool.beanClassNameCheckMap.put(beanClassName, Boolean.FALSE);
				return CHECK_RESULT_NO;
			}
		}
		catch (Throwable ex)
		{
			// 解析bean的过程中出现异常, 则判定为不是bean
			BeanTool.beanClassNameCheckMap.put(beanClassName, Boolean.FALSE);
			return CHECK_RESULT_NO;
		}
		return CHECK_RESULT_YES;
	}

	/**
	 * 检查目标类型是否为一些基础类型.
	 */
	private boolean isBaseType(Class beanClass)
	{
		for (int i = 0; i < baseTypeArr.length; i++)
		{
			if (baseTypeArr[i].isAssignableFrom(beanClass))
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * 在配置中设置基础类型列表的键值.
	 */
	public static final String BASE_TYPE_PROPERTY = "self.micromagic.cg.baseTypes";

	private static Class[] baseTypeArr;
	static
	{
		// 初始化基础类型列表
		List tmp = new ArrayList();
		tmp.add(Collection.class);
		tmp.add(Map.class);
		tmp.add(Iterator.class);
		tmp.add(InputStream.class);
		tmp.add(Reader.class);
		tmp.add(OutputStream.class);
		tmp.add(Writer.class);
		tmp.add(Connection.class);
		tmp.add(ResultSet.class);
		String[] tNames = {
			"javax.servlet.ServletRequest", "javax.servlet.ServletResponse",
			"javax.servlet.http.HttpSession"
		};
		for (int i = 0; i < tNames.length; i++)
		{
			try
			{
				tmp.add(Class.forName(tNames[i]));
			}
			catch (Throwable ex) {}
		}
		String s = Utility.getProperty(BASE_TYPE_PROPERTY);
		if (s != null)
		{
			tNames = StringTool.separateString(s, ";,", true);
			for (int i = 0; i < tNames.length; i++)
			{
				try
				{
					tmp.add(Class.forName(tNames[i]));
				}
				catch (Throwable ex)
				{
					CG.log.warn("Error in read base type:[" + tNames[i] + "].", ex);
				}
			}
		}
		baseTypeArr = (Class[]) tmp.toArray(new Class[0]);
	}

}