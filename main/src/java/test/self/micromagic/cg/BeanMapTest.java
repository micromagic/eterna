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

package self.micromagic.cg;

import java.util.Arrays;
import java.util.Collection;
import java.util.ArrayList;
import java.util.TreeSet;
import java.util.Map;
import java.util.HashMap;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.net.URL;

import junit.framework.TestCase;
import self.micromagic.util.Utility;
import self.micromagic.util.FormatTool;
import self.micromagic.util.converter.DateConverter;
import self.micromagic.util.converter.UtilDateConverter;
import self.micromagic.util.ref.StringRef;

public class BeanMapTest extends TestCase
{
	public void testArrayBeanCollection()
	{
		//Utility.setProperty(CG.COMPILE_TYPE_PROPERTY, "ant");
		TestArrayBean ab = new TestArrayBean();
		BeanMap bm = BeanTool.getBeanMap(ab);
		Collection c;

		c = new ArrayList();
		c.add("123");
		c.add(getMainBean("1", "2"));
		c.add(getSubBean("3"));
		ab.setCollection(c);
		assertEquals("123", bm.get("collection[0]"));
		assertEquals("2", bm.get("collection[1].subInfo.id"));
		assertEquals("1", bm.get("collection[1].id"));
		assertEquals("3", bm.get("collection[2].id"));

		c = new TreeSet();
		c.add(getMainBean("5", "6"));
		ab.setCollection(c);
		assertEquals("6", bm.get("collection[0].subInfo.id"));
		assertEquals("5", bm.get("collection[0].id"));
	}

	public void testBean2Map()
			throws Exception
	{
		//Utility.setProperty(CG.COMPILE_TYPE_PROPERTY, "ant");
		TestMainBean mb = new TestMainBean();
		TestSubBean sb = new TestSubBean();
		sb.setAddress("addr");
		sb.setAmount(1.02);
		mb.setSubInfo(sb);
		BeanMap bm = BeanTool.getBeanMap(mb);
		bm.setBean2Map(true);
		Object obj = bm.get("subInfo");
		assertTrue(obj instanceof Map);

		TestArrayBean ab = this.getArrayBean(null);
		bm = BeanTool.getBeanMap(ab);
		bm.setBean2Map(true);
		obj = bm.get("mainBean1");
		assertTrue(obj instanceof Map[]);
		obj = ((Map[]) obj)[0].get("subInfo");
		assertTrue(obj instanceof Map);
		obj = bm.get("mainBean2");
		assertTrue(obj instanceof Map[][]);
		obj = bm.get("subBean2");
		assertTrue(obj instanceof Map[][][]);
		assertNull(bm.get("urls"));
		bm.put("urls", new URL[]{new URL("http://a.com")});
		assertNotNull(bm.get("urls"));

		bm.put("subBean2", getSubBean3Map());
		assertEquals("02", ab.getSubBean2()[0][1][1].getId());
		assertEquals(100.0, ab.getSubBean2()[1][0][1].getAmount(), 0.001);

		assertEquals(2, ab.getArrStr()[1].length);
		bm.put("arrStr", new int[][]{{1, 2}, {3, 4, 5}});
		assertEquals("1", ab.getArrStr()[0][0]);
		assertEquals("13", ab.getArrStr()[0][2]);
		assertEquals(3, ab.getArrStr()[1].length);
		assertEquals("5", ab.getArrStr()[1][2]);

		bm.put("arrInt", new int[]{9, 10});
		assertEquals(9, ab.getArrInt()[0]);
		assertEquals(3, ab.getArrInt()[2]);

		MainBean2 m2 = new MainBean2();
		assertTrue(BeanTool.checkBean(MainBean2.class));
		m2.id = "001";
		m2.name = "测试1";
		m2.gradeYear = 2008;
		m2.comeDate = "2003-3-5";
		bm.put("mainB", m2);
		assertNotNull(ab.mainB);
		assertEquals("001", ab.mainB.getId());
		assertEquals("测试1", ab.mainB.getName());
		assertEquals(2008, ab.mainB.getGradeYear());
		assertEquals(FormatTool.parseDate("2003-3-5").getTime(), ab.mainB.getComeDate().getTimeInMillis());

		bm = BeanTool.getBeanMap(mb);
		UtilDateConverter dc = new UtilDateConverter();
		dc.setDateFormats(new DateFormat[]{new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"), new SimpleDateFormat("yyyy-MM-dd HH:mm")});
		bm.registerConverter(java.util.Date.class, dc);
		bm.put("birth", "2003-3-5 2:3");
		assertEquals(FormatTool.parseDatetime("2003-3-5 2:3:0").getTime(), mb.getBirth().getTime());
		bm.put("birth", "2003-3-5 2:3:12");
		assertEquals(FormatTool.parseDatetime("2003-3-5 2:3:12").getTime(), mb.getBirth().getTime());
		bm.put("birth", "2003-3-5");
		assertEquals(FormatTool.parseDatetime("2003-3-5 2:3:12").getTime(), mb.getBirth().getTime());
	}

	public void testPrimitiveArray()
	{
		Utility.setProperty(CG.COMPILE_TYPE_PROPERTY, "ant");
		Object obj1 = ArrayTool.wrapPrimitiveArray(2, new int[][]{{1, 2, 3}, {0, 9}});
		assertTrue(obj1 instanceof Integer[][]);
		Integer[][] arr1 = (Integer[][]) obj1;
		assertEquals(new Integer(1), arr1[0][0]);
		assertEquals(new Integer(3), arr1[0][2]);
		assertEquals(new Integer(9), arr1[1][1]);

		Object obj2 = ArrayTool.wrapPrimitiveArray(3, new boolean[][][]{{{true}, {false}}, {{false}}});
		assertTrue(obj2 instanceof Boolean[][][]);
	}

	public void testArrayBean()
	{
		Map tMap = new HashMap();
		TestArrayBean ab = this.getArrayBean(tMap);
		BeanMap bm = BeanTool.getBeanMap(ab);
		assertEquals("113", bm.get("subBean2[1][0][2].id"));
		assertEquals("1", bm.get("mainBean2[1][0].subInfo.id"));
		assertEquals("3", bm.get("mainBean2[1][2].id"));
		assertEquals(new Integer(5), bm.get("arrInt[4]"));
		assertEquals("22", bm.get("arrStr[1][1]"));
		assertTrue(Arrays.equals(new String[]{"31"}, (String[]) bm.get("arrStr[2]")));

		long l1, l2;
		int count = 100000;
		int tmpI;
		Object tmp;

		l1 = System.currentTimeMillis();
		for (int i = 0; i < count; i++)
		{
			tmp = bm.get("subBean2[1][0][2].id");
			tmp = bm.get("arrStr[1]");
			tmp = bm.get("mainBean2[1][0].subInfo.id");
			tmp = bm.get("arrInt[2]");
		}
		l2 = System.currentTimeMillis();
		System.out.println("t1:" + (l2 - l1));

		l1 = System.currentTimeMillis();
		for (int i = 0; i < count * 100; i++)
		{
			tmp = ab.getSubBean2()[1][0][2].getId();
			tmp = ab.getArrStr()[1];
			tmp = ab.getMainBean2()[1][0].getSubInfo().getId();
			tmpI = ab.getArrInt()[2];
		}
		l2 = System.currentTimeMillis();
		System.out.println("t2:" + (l2 - l1));

		CellAccessInfo cai1 = bm.getCellAccessInfo("subBean2[1][0][2].id");
		CellAccessInfo cai2 = bm.getCellAccessInfo("arrStr[1]");
		CellAccessInfo cai3 = bm.getCellAccessInfo("mainBean2[1][0].subInfo.id");
		CellAccessInfo cai4 = bm.getCellAccessInfo("arrInt[2]");
		l1 = System.currentTimeMillis();
		for (int i = 0; i < count * 10; i++)
		{
			tmp = cai1.getValue();
			tmp = cai2.getValue();
			tmp = cai3.getValue();
			tmp = cai4.getValue();
		}
		l2 = System.currentTimeMillis();
		System.out.println("t3:" + (l2 - l1));

		l1 = System.currentTimeMillis();
		for (int i = 0; i < count * 10; i++)
		{
			tmp = tMap.get("subBean2[1][0][2].id");
			tmp = tMap.get("arrStr[1][1]");
			tmp = tMap.get("mainBean2[1][0].subInfo.id");
			tmp = tMap.get("arrInt[2]");
		}
		l2 = System.currentTimeMillis();
		System.out.println("t4:" + (l2 - l1));

		assertEquals("113", tMap.get("subBean2[1][0][2].id"));
		assertEquals("1", tMap.get("mainBean2[1][0].subInfo.id"));
		assertEquals(new Integer(3), tMap.get("arrInt[2]"));
		assertEquals("22", tMap.get("arrStr[1][1]"));
	}

	public void testParseArrayName()
	{
		int[] indexs;
		StringRef refName;

		refName = new StringRef();
		indexs = null;
		indexs = BeanMap.parseArrayName("tmpName", refName);
		assertNull(indexs);
		assertEquals("tmpName", refName.getString());

		refName = new StringRef();
		indexs = null;
		indexs = BeanMap.parseArrayName("t[2]", refName);
		assertTrue(Arrays.equals(new int[]{2}, indexs));
		assertEquals("t", refName.getString());

		refName = new StringRef();
		indexs = null;
		indexs = BeanMap.parseArrayName("t1[1][3]", refName);
		assertTrue(Arrays.equals(new int[]{1, 3}, indexs));
		assertEquals("t1", refName.getString());

		refName = new StringRef();
		indexs = null;
		indexs = BeanMap.parseArrayName("t2[1][3][2]", refName);
		assertTrue(Arrays.equals(new int[]{1, 3, 2}, indexs));
		assertEquals("t2", refName.getString());
	}

	public static TestArrayBean getArrayBean(Map resultMap)
	{
		TestArrayBean ab = new TestArrayBean();

		ab.setArrInt(new int[]{1, 2, 3, 4, 5, 6});
		if (resultMap != null)
		{
			resultMap.put("arrInt[0]", new Integer(1));
			resultMap.put("arrInt[1]", new Integer(2));
			resultMap.put("arrInt[2]", new Integer(3));
			resultMap.put("arrInt[3]", new Integer(4));
			resultMap.put("arrInt[4]", new Integer(5));
			resultMap.put("arrInt[5]", new Integer(6));
		}

		ab.setArrStr(new String[][]{{"11", "12", "13"}, {"21", "22"}, {"31"}});
		if (resultMap != null)
		{
			resultMap.put("arrStr[0][0]", "11");
			resultMap.put("arrStr[0][1]", "12");
			resultMap.put("arrStr[0][2]", "13");
			resultMap.put("arrStr[1][0]", "21");
			resultMap.put("arrStr[1][1]", "22");
			resultMap.put("arrStr[2][0]", "31");
		}

		TestMainBean[] mbArr1 = {
			getMainBean("1", "1"), getMainBean("1", "2"), getMainBean("1", "3")
		};
		ab.setMainBean1(mbArr1);
		if (resultMap != null)
		{
			resultMap.put("mainBean1[0].id", "1");
			resultMap.put("mainBean1[0].subInfo.id", "1");
			resultMap.put("mainBean1[1].id", "1");
			resultMap.put("mainBean1[1].subInfo.id", "2");
			resultMap.put("mainBean1[2].id", "1");
			resultMap.put("mainBean1[2].subInfo.id", "3");
		}

		TestMainBean[][] mbArr2 = {
			{getMainBean("2", "1"), getMainBean("2", "2"), getMainBean("2", "3")},
			{getMainBean("3", "1"), getMainBean("3", "2"), getMainBean("3", "3")}
		};
		ab.setMainBean2(mbArr2);
		if (resultMap != null)
		{
			resultMap.put("mainBean2[0][0].id", "2");
			resultMap.put("mainBean2[0][0].subInfo.id", "1");
			resultMap.put("mainBean2[0][1].id", "2");
			resultMap.put("mainBean2[0][1].subInfo.id", "2");
			resultMap.put("mainBean2[0][2].id", "2");
			resultMap.put("mainBean2[0][2].subInfo.id", "3");
			resultMap.put("mainBean2[1][0].id", "3");
			resultMap.put("mainBean2[1][0].subInfo.id", "1");
			resultMap.put("mainBean2[1][1].id", "3");
			resultMap.put("mainBean2[1][1].subInfo.id", "2");
			resultMap.put("mainBean2[1][2].id", "3");
			resultMap.put("mainBean2[1][2].subInfo.id", "3");
		}

		TestSubBean[] sbArr1 = {
			getSubBean("11"), getSubBean("12"), getSubBean("13")
		};
		ab.setSubBean1(sbArr1);
		if (resultMap != null)
		{
			resultMap.put("subBean1[0].id", "11");
			resultMap.put("subBean1[1].id", "12");
			resultMap.put("subBean1[2].id", "13");
		}

		TestSubBean[][][] sbArr2 = {
			{
				{getSubBean("211"), getSubBean("212")},
				{getSubBean("221")}
			},
			{
				{getSubBean("111"), getSubBean("112"), getSubBean("113")},
				{getSubBean("121"), getSubBean("122")}
			}
		};
		ab.setSubBean2(sbArr2);
		if (resultMap != null)
		{
			resultMap.put("subBean2[0][0][0].id", "211");
			resultMap.put("subBean2[0][0][1].id", "212");
			resultMap.put("subBean2[0][1][0].id", "221");
			resultMap.put("subBean2[1][0][0].id", "111");
			resultMap.put("subBean2[1][0][1].id", "112");
			resultMap.put("subBean2[1][0][2].id", "113");
			resultMap.put("subBean2[1][1][0].id", "121");
			resultMap.put("subBean2[1][1][1].id", "122");
		}

		return ab;
	}

	public static TestMainBean getMainBean(String mainId, String subId)
	{
		TestMainBean mb = new TestMainBean();
		mb.setId(mainId);
		TestSubBean sb = new TestSubBean();
		sb.setId(subId);
		mb.setSubInfo(sb);
		return mb;
	}

	public static TestSubBean getSubBean(String subId)
	{
		TestSubBean sb = new TestSubBean();
		sb.setId(subId);
		return sb;
	}

	public static Map[][][] getSubBean3Map()
	{
		Map[][][] tmpMap = new Map[2][2][];
		Map m1 = new HashMap();
		m1.put("id", "01");
		m1.put("address", "tmp");
		Map m2 = new HashMap();
		m2.put("id", "02");
		m2.put("amount", "34.6");
		Map m3 = new HashMap();
		m3.put("id", "03");
		m3.put("phone", new Integer(23456));
		tmpMap[0][1] = new Map[]{m1, m2, m3};

		Map m4 = new HashMap();
		m4.put("id", "04");
		m4.put("address", "tttttt");
		Map m5 = new HashMap();
		m5.put("id", "02");
		m5.put("amount", new Long(100));
		tmpMap[1][0] = new Map[]{m4, m5};

		return tmpMap;
	}

	public static class MainBean2
	{
		public String id;
		public String name;
		public java.util.Date birth;
		public String comeDate;
		public int gradeYear;

	}

}