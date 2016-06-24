
package self.micromagic.eterna.security;

import self.micromagic.eterna.share.EternaException;
import self.micromagic.eterna.share.EternaFactory;

public class TestPermissionSetCreater
		implements PermissionSetGenerator
{
	public String getName()
	{
		return this.name;
	}

	public void setName(String name)
	{
		this.name = name;
	}
	private String name;

	public boolean initialize(EternaFactory factory)
			throws EternaException
	{
		return false;
	}

	public PermissionSet createPermissionSet(String permission, EternaFactory factory)
	{
		//System.out.println("Permission:" + permission);
		return new TestPermissionSet(permission);
	}

}

class TestPermissionSet
		implements PermissionSet
{
	public TestPermissionSet(String config)
	{
		this.config = config;
		//System.out.println("config:" + config);
	}
	private final String config;

	public String toString()
	{
		return this.config;
	}

	public boolean checkPermission(Permission permission)
	{
		return true;
	}

}
