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

package self.micromagic.eterna.security;

import java.util.Arrays;

import self.micromagic.eterna.share.EternaException;
import self.micromagic.eterna.share.EternaFactory;
import self.micromagic.util.StringTool;

public class PermissionSetCreaterImpl
		implements PermissionSetCreater
{
	public boolean initialize(EternaFactory factory)
			throws EternaException
	{
		if (this.factory == null)
		{
			this.factory = factory;
			return false;
		}
		return true;
	}
	private EternaFactory factory;

	public EternaFactory getFactory()
			throws EternaException
	{
		return this.factory;
	}

	public PermissionSet createPermissionSet(String permission)
	{
		if (!StringTool.isEmpty(permission))
		{
			return new PermissionSetImpl(permission, this.factory);
		}
		return null;
	}

}

class PermissionSetImpl
		implements PermissionSet
{
	public PermissionSetImpl(String permissionConfig, EternaFactory factory)
	{
		this.permissionConfig = permissionConfig;
		this.permissionNames = StringTool.separateString(permissionConfig, ",", true);
		Arrays.sort(this.permissionNames);
		this.initialize(factory);
	}
	private final String permissionConfig;
	private int[] permissionIds;
	private final String[] permissionNames;

	private void initialize(EternaFactory factory)
			throws EternaException
	{
		UserManager um = factory.getUserManager();
		if (um != null && um.hasPermissionId())
		{
			this.permissionIds = new int[this.permissionNames.length];
			for (int i = 0; i < this.permissionNames.length; i++)
			{
				this.permissionIds[i] = um.getPermissionId(this.permissionNames[i]);
			}
		}
	}

	/**
	 * 检测参数permission中是否包含本权限集合中的某个权限.
	 *
	 * 注:
	 * 如果参数为permission空, 则返回true.
	 * 如果本权限集合为空, 则也返回true.
	 */
	public boolean checkPermission(Permission permission)
	{
		if (permission == null)
		{
			return true;
		}
		String[] pnames = this.permissionNames;
		int[] pids = this.permissionIds;
		if (pnames == null && pids == null)
		{
			return true;
		}
		if (pids != null)
		{
			for (int i = 0; i < pids.length; i++)
			{
				if (permission.hasPermission(pids[i]))
				{
					return true;
				}
			}
		}
		else
		{
			for (int i = 0; i < pnames.length; i++)
			{
				if (permission.hasPermission(pnames[i]))
				{
					return true;
				}
			}
		}
		return false;
	}

	public int hashCode()
	{
		if (this.hashCode == 0)
		{
			if (this.permissionIds != null)
			{
				for (int i = 0; i < this.permissionIds.length; i++)
				{
					this.hashCode += this.permissionIds[i];
				}
			}
			else if (this.permissionNames != null)
			{
				for (int i = 0; i < this.permissionNames.length; i++)
				{
					this.hashCode += this.permissionNames[i].hashCode();
				}
			}
		}
		return this.hashCode;
	}
	private int hashCode = 0;

	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (obj instanceof PermissionSetImpl)
		{
			PermissionSetImpl other = (PermissionSetImpl) obj;
			if (this.permissionIds != null && other.permissionIds != null)
			{
				if (this.permissionIds.length == other.permissionIds.length)
				{
					for (int i = 0; i < this.permissionIds.length; i++)
					{
						if (this.permissionIds[i] != other.permissionIds[i])
						{
							return false;
						}
					}
					return true;
				}
			}
			else if (this.permissionNames != null && other.permissionNames != null)
			{
				if (this.permissionNames.length == other.permissionNames.length)
				{
					for (int i = 0; i < this.permissionNames.length; i++)
					{
						if (!this.permissionNames[i].equals(other.permissionNames[i]))
						{
							return false;
						}
					}
					return true;
				}
			}
		}
		return false;
	}

	public String toString()
	{
		return this.permissionConfig;
	}

}
