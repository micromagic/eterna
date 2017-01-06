
package self.micromagic.util;

import junit.framework.TestCase;

public class MapTest extends TestCase
{
	public void testHashCode()
	{
		int count = 5000000;
		int code = 0;
		Object obj = new MapTest();

		long begin = System.currentTimeMillis();
		for (int i = 0; i < count; i++)
		{
			code = obj.hashCode();
		}
		System.out.println("h1.1: " + (System.currentTimeMillis() - begin));
		System.out.println("code: " + code);

		begin = System.currentTimeMillis();
		for (int i = 0; i < count; i++)
		{
			code = System.identityHashCode(obj);
		}
		System.out.println("h1.2: " + (System.currentTimeMillis() - begin));
		System.out.println("code: " + code);

		obj = new Object();

		begin = System.currentTimeMillis();
		for (int i = 0; i < count; i++)
		{
			code = obj.hashCode();
		}
		System.out.println("h2.1: " + (System.currentTimeMillis() - begin));
		System.out.println("code: " + code);

		begin = System.currentTimeMillis();
		for (int i = 0; i < count; i++)
		{
			code = System.identityHashCode(obj);
		}
		System.out.println("h2.2: " + (System.currentTimeMillis() - begin));
		System.out.println("code: " + code);
	}

	public int hashCode()
	{
		return 1;
	}

}
