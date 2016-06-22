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
 * 与配置管理器绑定的类成员的信息.
 */
public class BindingInfo
{
	private final Class<?> c;
	private final Member m;
	private final String configName;
	private final String configValue;
	private final String description;
	private final String defaultValue;
	private final boolean fieldType;

	BindingInfo(Class<?> c, Member m, String name, String value, String description,
			String defaultValue)
	{
		this.c = c;
		this.m = m;
		this.configName = name;
		this.configValue = value;
		this.description = description;
		this.fieldType = m instanceof Field;
		this.defaultValue = defaultValue;
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
	 * 获取属性中的值.
	 * 只有当绑定的成员类型为属性时才有效, 如果绑定
	 * 的为方法, 那只会返回null.
	 */
	public Object getFieldValue()
	{
		if (!this.fieldType)
		{
			return null;
		}
		try
		{
			Field f = (Field) this.m;
			Object r;
			if (!f.isAccessible())
			{
				f.setAccessible(true);
				r = f.get(null);
				f.setAccessible(false);
			}
			else
			{
				r = f.get(null);
			}
			return r;
		}
		catch (Throwable ex)
		{
			return null;
		}
	}

	/**
	 * 获取绑定的配置名称.
	 */
	public String getConfigName()
	{
		return this.configName;
	}

	/**
	 * 获取配置的值.
	 */
	public String getConfigValue()
	{
		return this.configValue;
	}

	/**
	 * 获取配置的默认值.
	 */
	public String getDefaultValue()
	{
		return this.defaultValue;
	}

	/**
	 * 获取与配置绑定的描述.
	 */
	public String getDescription()
	{
		return this.description;
	}

	@Override
	public String toString()
	{
		if (this.strValue == null)
		{
			this.strValue = "BindingInfo:{config:[" + this.configName + "], value:["
					+ this.configValue + "], class:" + this.c.getCanonicalName() + ", ";
			if (this.fieldType)
			{
				this.strValue += "field:" + this.m.getName() + ", type:"
						+ ((Field) this.m).getType().getCanonicalName()
						+ ", val:[" + this.getFieldValue() + "]";
			}
			else
			{
				this.strValue += "method:" + this.m.getName();
			}
			if (this.defaultValue != null)
			{
				this.strValue += ", default:[" + this.defaultValue + "]";
			}
			this.strValue += ", description:[" + this.description + "]}";
		}
		return this.strValue;
	}
	private String strValue;

}