
package self.micromagic.cg;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;
import self.micromagic.util.Utility;

public class ArrayToolTest extends TestCase
{
	public void testCollection()
	{
		List list = new ArrayList();
		list.add(new Object[]{"1"});
		list.add(Arrays.asList(new Object[]{"2", "3"}));
		Object arr = ArrayTool.convertArray(1, Integer[].class, list, true);
		assertEquals(Integer[][].class, arr.getClass());
		assertEquals(2, ((Object[]) arr).length);
		Integer[][] real = (Integer[][]) arr;
		assertEquals(1, real[0].length);
		assertEquals(2, real[1].length);
		assertEquals(Utility.INTEGER_1, real[0][0]);
		assertEquals(Utility.INTEGER_2, real[1][0]);
		assertEquals(Utility.INTEGER_3, real[1][1]);
	}

	public void testConvertArray()
	{
		//self.micromagic.util.Utility.setProperty(CG.COMPILE_TYPE_PROPERTY, "ant");

		Object[] arr = new Object[1];
		arr[0] = new Integer[2];
		assertEquals(Integer[][].class, ArrayTool.convertArray(1, Integer[].class, arr).getClass());
		arr = new Integer[1][2][3];
		assertEquals(Integer[][][].class, ArrayTool.convertArray(1, Integer[][].class, arr).getClass());
		arr = new Object[1][2];

		Object[][] arr2 = new Object[1][2];
		arr2[0][0] = new Integer[1];
		arr2[0][1] = new Integer[2];
		assertEquals(Integer[][][].class, ArrayTool.convertArray(1, Integer[][].class, arr2).getClass());

		Object[][] arr3 = new Object[1][2];
		arr3[0][0] = new Integer[1][2];
		arr3[0][1] = new Integer[2][3];
		assertEquals(Integer[][][][].class, ArrayTool.convertArray(2, Integer[][].class, arr3).getClass());

		Object[][] arr4 = new Object[1][2];
		arr4[0][0] = new TestSubBean[1][2];
		arr4[0][1] = new TestSubBean[2][3];
		assertEquals(TestSubBean[][][][].class, ArrayTool.convertArray(2, TestSubBean[][].class, arr4).getClass());

		Object[] arr5 = new Object[3];
		arr5[2] = new TestSubBean();
		assertEquals(TestSubBean[].class, ArrayTool.convertArray(1, TestSubBean.class, arr5).getClass());

		Object[][] arr6 = new Object[3][];
		arr6[2] = new TestSubBean[2];
		arr6[2][0] = new TestSubBean();
		assertEquals(TestSubBean[][].class, ArrayTool.convertArray(2, TestSubBean.class, arr6).getClass());
	}

	public void testPrimitiveWrap()
	{
		assertEquals(Integer[][].class, ArrayTool.wrapPrimitiveArray(2, new int[0][0]).getClass());
		assertEquals(Boolean[].class, ArrayTool.wrapPrimitiveArray(1, new boolean[0]).getClass());
		assertEquals(Character[][][].class, ArrayTool.wrapPrimitiveArray(3, new char[0][0][0]).getClass());

		assertEquals(Integer[].class, ArrayTool.wrapPrimitiveArray(1, new int[1]).getClass());
		assertEquals(Byte[].class, ArrayTool.wrapPrimitiveArray(1, new byte[1]).getClass());
		assertEquals(Short[].class, ArrayTool.wrapPrimitiveArray(1, new short[1]).getClass());
		assertEquals(Long[].class, ArrayTool.wrapPrimitiveArray(1, new long[1]).getClass());
		assertEquals(Float[].class, ArrayTool.wrapPrimitiveArray(1, new float[1]).getClass());
		assertEquals(Double[].class, ArrayTool.wrapPrimitiveArray(1, new double[1]).getClass());
		assertEquals(Character[].class, ArrayTool.wrapPrimitiveArray(1, new char[1]).getClass());
		assertEquals(Boolean[].class, ArrayTool.wrapPrimitiveArray(1, new boolean[1]).getClass());

		assertNull(ArrayTool.wrapPrimitiveArray(1, new String[1]));
	}

	public void testConvertThrow()
	{
		//self.micromagic.util.Utility.setProperty(CG.COMPILE_TYPE_PROPERTY, "ant");
		try
		{
			ArrayTool.wrapPrimitiveArray(3, new String[0][0][0], true);
			fail();
		}
		catch (Exception ex)
		{
			System.out.println(ex);
		}
		assertNull(ArrayTool.wrapPrimitiveArray(3, null, false));
		Object[] arr = new Object[1];
		try
		{
			ArrayTool.convertArray(2, Integer[].class, arr, true);
			fail();
		}
		catch (Exception ex)
		{
			System.out.println(ex);
		}
		assertNull(ArrayTool.convertArray(2, Integer[].class, arr, false));
		Integer[][][][] dest = new Integer[1][2][3][];
		try
		{
			ArrayTool.convertArray(2, Integer[].class, arr, dest, true);
			fail();
		}
		catch (Exception ex)
		{
			System.out.println(ex);
		}
		assertEquals(dest, ArrayTool.convertArray(2, Integer[].class, arr, dest, false));
	}

}