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

package self.micromagic.eterna.security;

import self.micromagic.eterna.share.EternaFactory;
import self.micromagic.util.StringTool;

/**
 * 权限的占位对象.
 * 在设置权限集合的配置时使用.
 */
public class PermissionSetHolder
		implements PermissionSet
{
	private final String config;

	public PermissionSetHolder(String config)
	{
		this.config = config;;
	}

	/**
	 * 如果是占位对象, 则通过工厂创建真正的权限集合.
	 */
	public static PermissionSet getRealPermissionSet(EternaFactory factory,
			PermissionSet permissionSet)
	{
		if (permissionSet == null)
		{
			return null;
		}
		if (permissionSet instanceof PermissionSetHolder)
		{
			String config = permissionSet.toString();
			return StringTool.isEmpty(config) ? null : factory.createPermissionSet(config);
		}
		else
		{
			return permissionSet;
		}
	}

	public String getConfig()
	{
		return this.config;
	}

	public String toString()
	{
		return this.getConfig();
	}

	public boolean checkPermission(Permission permission)
	{
		return false;
	}

}
