
package self.micromagic.eterna.security;

import self.micromagic.eterna.share.EternaException;
import self.micromagic.eterna.share.EternaFactory;

public class TestPermissionSetCreater
		implements PermissionSetCreater
{
	public boolean initialize(EternaFactory factory)
			throws EternaException
	{
		return false;
	}

	public EternaFactory getFactory()
			throws EternaException
	{
		return null;
	}

	public PermissionSet createPermissionSet(String permission)
	{
		System.out.println("p:" + permission);
		return new TestPermissionSet(permission);
	}

}

class TestPermissionSet
		implements PermissionSet
{
	public TestPermissionSet(String config)
	{
		this.config = config;
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
