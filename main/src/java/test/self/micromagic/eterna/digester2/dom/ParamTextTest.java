package self.micromagic.eterna.digester2.dom;

import java.io.StringReader;

import junit.framework.TestCase;

public class ParamTextTest extends TestCase
{
	public void testParse()
			throws Exception
	{
		String str1 = "start\n$define${a:1;b:2;cc:{{{3}}};\n d : {{{\na\nb{\n\n}}}e:5}"
				+ ",$param${p0,$x,$d},ab$dc$define${x:100;}123$param${p1},$param${p2,xx,a}";
		String str2 = "start\n"
				+ ", \na\nb{\n,ab$dc123VP1,a";
		ParamText pt = new ParamText();
		pt.parse(new StringReader(str1));
		assertEquals("3", pt.getDefine("cc"));
		assertEquals("100", pt.getParameter("p0").getDescribe());
		pt.setParameter("p1", "VP1");
		String str3 = pt.getResultString();
		//System.out.println(str3);
		//System.out.println(str2);
		assertEquals(str2, str3);
	}

}
