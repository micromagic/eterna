
package self.micromagic.eterna.share.sub;

public class TestClass
{
	public abstract static class TestClass1
	{
		public abstract String getValue(String str);

	}

	private static class TestClass2 extends TestClass1
	{
		public String getValue(String str)
		{
			return "123" + str;
		}

	}

	public static class TestClass3 extends TestClass2
	{
		public String getNextValue(String str)
		{
			return "a:" + str;
		}

	}

	public static class TestClass4 extends TestClass2
	{
		public String getValue(String str)
		{
			return "a:" + str;
		}

	}

}
