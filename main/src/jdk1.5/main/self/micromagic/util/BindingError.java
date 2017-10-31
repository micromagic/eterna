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

import java.lang.reflect.Field;
import java.lang.reflect.Member;

/**
 * 自动配置绑定时的出错信息.
 */
public class BindingError
{
	private final Class<?> c;
	private final Member m;
	private final String code;
	private final String msg;
	private final boolean fieldType;
	
	private String strValue;

	BindingError(Class<?> c, Member m, String code, String msg)
	{
		this.c = c;
		this.m = m;
		this.code = code;
		this.msg = msg;
		this.fieldType = m instanceof Field;
	}

	/**
	 * 获取与配置绑定的类.
	 */
	public Class<?> getType()
	{
		return this.c;
	}

	/**
	 * 获取与配置绑定的成员.
	 */
	public Member getMember()
	{
		return this.m;
	}

	/**
	 * 获取与配置绑定的成员是否为属性.
	 * true表示为属性, false表示为方法.
	 */
	public boolean isField()
	{
		return this.fieldType;
	}

	/**
	 * 获取错误信息的代码.
	 */
	public String getCode()
	{
		return this.code;
	}

	/**
	 * 获取错误的信息.
	 */
	public String getMessage()
	{
		return this.msg;
	}

	@Override
	public String toString()
	{
		if (this.strValue == null)
		{
			this.strValue = "BindingError:{class:" + this.c.getCanonicalName() + ", ";
			if (this.fieldType)
			{
				this.strValue += "field:" + this.m.getName() + ", type:"
						+ ((Field) this.m).getType().getCanonicalName();
			}
			else
			{
				this.strValue += "method:" + this.m.getName();
			}
			this.strValue += ", code:" + this.code + ", message:[" + this.msg + "]}";
		}
		return this.strValue;
	}

}
