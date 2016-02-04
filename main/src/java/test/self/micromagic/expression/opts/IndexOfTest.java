
package self.micromagic.expression.opts;

import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;
import self.micromagic.util.Utility;

public class IndexOfTest extends TestCase
{
	public void testList()
	{
		IndexOf iOf = new IndexOf();
		List list = Arrays.asList(new Object[]{"a", "b", "c", "c", "d", "b", "a"});
		assertEquals(Utility.INTEGER_1, iOf.exec(new Object[]{list, "b"}));
		assertEquals(Utility.INTEGER_5, iOf.exec(new Object[]{list, "b", Boolean.TRUE}));
	}

	public void testLast()
	{
		IndexOf iOf = new IndexOf();
		String str = "1,234,,5";
		assertEquals(Utility.INTEGER_6, iOf.exec(new Object[]{str, ",", Boolean.TRUE}));
		assertEquals(Utility.INTEGER_5, iOf.exec(new Object[]{str, ",,", Boolean.TRUE}));
		assertEquals(Utility.INTEGER_1, iOf.exec(new Object[]{str, ",", Utility.INTEGER_3, Boolean.TRUE}));
	}

	public void testFirst()
	{
		IndexOf iOf = new IndexOf();
		String str = "12,,34,5";
		assertEquals(Utility.INTEGER_2, iOf.exec(new Object[]{str, ","}));
		assertEquals(Utility.INTEGER_3, iOf.exec(new Object[]{str, ",3"}));
		assertEquals(Utility.INTEGER_6, iOf.exec(new Object[]{str, ",", Utility.INTEGER_5}));
	}

}
