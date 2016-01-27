
package self.micromagic.expression.opts;

import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;
import self.micromagic.util.Utility;

public class SubObjectTest extends TestCase
{
	public void testSub()
	{
		SubObject sub = new SubObject();
		String str = "12345";
		assertEquals("23", sub.exec(new Object[]{str, Utility.INTEGER_1, Utility.INTEGER_3}));
		Object[] arr = new Object[]{"a", "b", "c"};
		Object[] r_arr = (Object[]) sub.exec(new Object[]{arr, Utility.INTEGER_2});
		assertEquals(1, r_arr.length);
		assertEquals("c", r_arr[0]);
		List list = Arrays.asList(arr);
		List r_list = Arrays.asList(new Object[]{"a", "b"});
		assertEquals(r_list, sub.exec(new Object[]{list, Utility.INTEGER_0, Utility.INTEGER_2}));
	}

	public void testSame()
	{
		SubObject sub = new SubObject();
		String str = "12345";
		assertTrue(str == sub.exec(new Object[]{str}));
		assertTrue(str == sub.exec(new Object[]{str, Utility.INTEGER_0, Utility.INTEGER_5}));
		Object[] arr = new Object[]{"1", "2"};
		assertTrue(arr == sub.exec(new Object[]{arr}));
		List list = Arrays.asList(arr);
		assertTrue(list == sub.exec(new Object[]{list}));
	}

}
