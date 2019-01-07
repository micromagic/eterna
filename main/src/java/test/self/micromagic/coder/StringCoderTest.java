
package self.micromagic.coder;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Writer;

import junit.framework.TestCase;
import self.micromagic.util.StringTool;

public class StringCoderTest extends TestCase
{
	public void testNormal()
	{
		StringCoder coder = new StringCoder();
		String src, dest;

		src = "ab\n;cd测试?";
		dest = coder.encodeString(src);
		System.out.println(dest);
		assertEquals(src, coder.decodeString(dest));

		src = "ab\n;c\7d测试";
		dest = coder.encodeString(src);
		assertEquals(src, coder.decodeString(dest));

		src = "123";
		dest = coder.encodeString(src);
		assertEquals(src, coder.decodeString(dest));
	}

	public void testBreak()
	{
		MyStrCoder coder = new MyStrCoder();
		String src, dest;

		dest = "ab\"123\\n5\\\"67\\7x\"xd";
		src = coder.decodeString(dest, 3);
		assertEquals('\"', dest.charAt(coder.getTotalIndex()));
		assertEquals("123\n5\"67\7x", src);

		dest = "ab\"123\\n5\\\"67\\7x\\\"xd";
		src = coder.decodeString(dest, 3);
		assertEquals(dest.length(), coder.getTotalIndex());
		assertEquals("123\n5\"67\7x\"xd", src);
	}

	public void testBase64()
			throws Exception
	{
		byte[] buf = {1, 2, 3, 4, 5, 6, 7};
		CodeInputStream in = new CodeInputStream(
				new Base64(), new ByteArrayInputStream(buf), false);
		System.out.println(StringTool.toString(in, "8859_1"));
		in.close();
	}

}

class MyStrCoder extends StringCoder
{
	public MyStrCoder()
	{
		super('\\');
	}

	public int getTotalIndex()
	{
		return this.totalIndex;
	}

	protected void decodeChar(Writer out, char c)
			throws IOException
	{
		if (c == '\"')
		{
			throw BREAK_FLAG;
		}
		super.decodeChar(out, c);
	}

}
