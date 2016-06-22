
package self.micromagic.coder;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.util.BitSet;

import self.micromagic.util.StringTool;

/**
 * 对字符串的特殊字符进行编码处理的工具.
 */
public class StringCoder
{
	/**
	 * 16进制的字符.
	 */
	protected static final char[] HEX_NUMS = {
		'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
		'A', 'B', 'C', 'D', 'E', 'F'
	};

	/**
	 * 标识编/解码中断的异常.
	 */
	protected static final RuntimeException BREAK_FLAG = new RuntimeException("break");

	/**
	 * 可预取或向前读取字符的个数.
	 */
	protected final int bufSize;

	/**
	 * 全局读取到的索引值.
	 */
	protected int totalIndex;

	/**
	 * 已读取字符的缓存.
	 */
	protected final char[] charBuf;
	/**
	 * 当前字符缓存中存放的字符个数.
	 */
	protected int charCount;
	/**
	 * 当前读取到的字符索引值.
	 */
	protected int charIndex;

	/**
	 * 字符的输出缓存.
	 */
	protected final char[] outBuf;
	/**
	 * 输出缓存使用到的索引值.
	 */
	protected int outIndex;

	/**
	 * 有效的, 不需要进行转换的字符集.
	 */
	protected final BitSet validChars;
	/**
	 * 转义字符.
	 */
	protected final char escapeChar;

	/**
	 * 默认的有效字符集.
	 */
	protected static final BitSet DEFAULT_VALID_CHARS = new BitSet(128);

	static
	{
		DEFAULT_VALID_CHARS.set('_');
		DEFAULT_VALID_CHARS.set('$');
		for (int i = 'A'; i <= 'Z'; i++)
		{
			DEFAULT_VALID_CHARS.set(i);
		}
		for (int i = 'a'; i <= 'z'; i++)
		{
			DEFAULT_VALID_CHARS.set(i);
		}
		for (int i = '0'; i <= '9'; i++)
		{
			DEFAULT_VALID_CHARS.set(i);
		}
	}

	/**
	 * 默认的构造函数. <p>
	 * 转义字符为"$".
	 */
	public StringCoder()
	{
		this(8, '$', null);
	}

	/**
	 * 仅设置转义字符进行构造.
	 *
	 * @param escapeChar  转义字符
	 */
	public StringCoder(char escapeChar)
	{
		this(8, escapeChar, null);
	}

	/**
	 * 通过设置转义字符等参数进行构造.
	 *
	 * @param bufSize     可预取或向前读取字符的个数
	 * @param escapeChar  转义字符
	 * @param validChars  不需要转义的字符集, 如果为<code>null</code>将使用默认的字符集
	 */
	public StringCoder(int bufSize, char escapeChar, BitSet validChars)
	{
		this.escapeChar = escapeChar;
		if (bufSize > 0)
		{
			if (bufSize > 128)
			{
				throw new IllegalArgumentException("Too large buf size [" + bufSize + "].");
			}
			this.bufSize = bufSize;
		}
		else
		{
			this.bufSize = 0;
		}
		if (validChars == null)
		{
			validChars = DEFAULT_VALID_CHARS;
		}
		this.validChars = validChars;
		this.charBuf = new char[512];
		this.outBuf = new char[128];
	}

	/**
	 * 对一个字符串进行编码处理, 并返回编码后的字符串.
	 */
	public String encodeString(String str)
	{
		return this.encodeString(str, 0);
	}

	/**
	 * 从指定位置开始, 对一个字符串进行编码处理, 并返回编码后的字符串.
	 */
	public String encodeString(String str, int begin)
	{
		if (begin > 0)
		{
			str = str.substring(begin);
			this.totalIndex = begin;
		}
		else
		{
			this.totalIndex = 0;
		}
		Reader in = new StringReader(str);
		Writer out = StringTool.createWriter(str.length() + 32);
		try
		{
			this.encode(in, out);
		}
		catch (IOException ex)
		{
			// 这里不会抛出这个异常
			throw new IllegalStateException(ex.getMessage());
		}
		return out.toString();
	}

	/**
	 * 对一个字符串进行解码处理, 并返回编码后的字符串.
	 */
	public String decodeString(String str)
	{
		return this.decodeString(str, 0);
	}

	/**
	 * 从指定位置开始, 对一个字符串进行解码处理, 并返回编码后的字符串.
	 */
	public String decodeString(String str, int begin)
	{
		if (begin > 0)
		{
			str = str.substring(begin);
			this.totalIndex = begin;
		}
		else
		{
			this.totalIndex = 0;
		}
		Reader in = new StringReader(str);
		Writer out = StringTool.createWriter(str.length());
		try
		{
			this.decode(in, out);
		}
		catch (IOException ex)
		{
			// 这里不会抛出这个异常
			throw new IllegalStateException(ex.getMessage());
		}
		return out.toString();
	}

	/**
	 * 对字符流进行编码处理.
	 */
	public void encode(Reader in, Writer out)
			throws IOException
	{
		try
		{
			this.dealChars(in, out, false);
		}
		catch (RuntimeException ex)
		{
			if (ex == BREAK_FLAG)
			{
				this.flush(out);
			}
			else
			{
				throw ex;
			}
		}
	}

	/**
	 * 对字符流进行解码处理.
	 */
	public void decode(Reader in, Writer out)
			throws IOException
	{
		try
		{
			this.dealChars(in, out, true);
		}
		catch (RuntimeException ex)
		{
			if (ex == BREAK_FLAG)
			{
				this.flush(out);
			}
			else
			{
				throw ex;
			}
		}
	}

	/**
	 * 对字符流进行处理.
	 */
	protected void dealChars(Reader in, Writer out, boolean doDecode)
			throws IOException
	{
		int tmpCount = in.read(this.charBuf);
		this.charCount = tmpCount;
		this.charIndex = 0;
		while (tmpCount >= 0)
		{
			if (tmpCount > 0)
			{
				if (doDecode)
				{
					this.decodeChar(out, this.charBuf[this.charIndex]);
				}
				else
				{
					this.encodeChar(out, this.charBuf[this.charIndex]);
				}
				this.totalIndex++;
				this.charIndex++;
			}
			if (this.charIndex >= this.charCount - this.bufSize)
			{
				int beginIndex = this.charIndex - this.bufSize;
				if (beginIndex > 0)
				{
					this.charIndex = this.bufSize;
				}
				else
				{
					// 前部缓存的个数不够, 不更新字符索引
					beginIndex = 0;
				}
				int length = this.charCount - beginIndex;
				if (length > 0)
				{
					System.arraycopy(this.charBuf, beginIndex, this.charBuf, 0, length);
				}
				tmpCount = in.read(this.charBuf, length, this.charBuf.length - length);
				this.charCount = (tmpCount > 0 ? tmpCount : 0) + length;
			}
		}
		for (; this.charIndex < this.charCount; this.charIndex++, this.totalIndex++)
		{
			if (doDecode)
			{
				this.decodeChar(out, this.charBuf[this.charIndex]);
			}
			else
			{
				this.encodeChar(out, this.charBuf[this.charIndex]);
			}
		}
		this.flush(out);
	}

	/**
	 * 对一个字符进行编码处理.
	 */
	protected void encodeChar(Writer out, char c)
			throws IOException
	{
		if (c == this.escapeChar || !this.validChars.get(c))
		{
			this.writeChars(this.encodeChar(c), out, false);
		}
		else
		{
			this.writeChar(c, out, false);
		}
	}

	/**
	 * 对一个字符进行解码处理.
	 */
	protected void decodeChar(Writer out, char c)
			throws IOException
	{
		if (c == this.escapeChar)
		{
			this.writeChar(this.decodeChar(c), out, false);
		}
		else
		{
			this.writeChar(c, out, false);
		}
	}

	/**
	 * 对一个字符使用转义符进行编码.
	 */
	protected char[] encodeChar(char c)
	{
		if (c <= 0xff)
		{
			char[] buf = new char[4];
			buf[0] = this.escapeChar;
			buf[1] = HEX_NUMS[(c >> 6) & 0x3];
			buf[2] = HEX_NUMS[(c >> 3) & 0x7];
			buf[3] = HEX_NUMS[c & 0x7];
			return buf;
		}
		else
		{
			char[] buf = new char[6];
			buf[0] = this.escapeChar;
			buf[1] = 'u';
			buf[2] = HEX_NUMS[(c >> 12) & 0xf];
			buf[3] = HEX_NUMS[(c >> 8) & 0xf];
			buf[4] = HEX_NUMS[(c >> 4) & 0xf];
			buf[5] = HEX_NUMS[c & 0xf];
			return buf;
		}
	}

	/**
	 * 对一个转义符进行解码.
	 */
	protected char decodeChar(char c)
	{
		int next = this.getChar(1, true);
		if (next == 'u')
		{
			int num = this.hexChar2Num(this.getChar(2, true)) << 12;
			num |= this.hexChar2Num(this.getChar(3, true)) << 8;
			num |= this.hexChar2Num(this.getChar(4, true)) << 4;
			num |= this.hexChar2Num(this.getChar(5, true));
			// 外层循环还会加1, 所以这里跳过的字符需要减1
			this.skipChars(6 - 1);
			return (char) num;
		}
		else if (next >= '0' && next <= '7')
		{
			int num = this.hexChar2Num(next);
			int leftCount = 1;
			if (next <= '3' && next >= '0')
			{
				leftCount = 2;
			}
			int index = 2;
			while (leftCount > 0)
			{
				int tmp = this.getChar(index, false);
				if (tmp >= '0' && tmp <= '7')
				{
					num = (num << 3) | this.hexChar2Num(tmp);
					leftCount--;
					index++;
				}
				else
				{
					break;
				}
			}
			// 外层循环还会加1, 所以这里跳过的字符需要减1
			this.skipChars(index - 1);
			return (char) num;
		}
		else
		{
			this.skipChars(1);
			switch (next)
			{
				case 't':
					return '\t';
				case 'f':
					return '\f';
				case 'r':
					return '\r';
				case 'n':
					return '\n';
				case 'b':
					return '\b';
				case '\\':
					return '\\';
				case '\'':
					return '\'';
				case '\"':
					return '\"';
				default:
					return (char) next;
			}
		}
	}

	/**
	 * 将16进制字符转换成数字.
	 */
	protected int hexChar2Num(int c)
	{
		if (c >= '0' && c <= '9')
		{
			return c - '0';
		}
		if (c >= 'A' && c <= 'F')
		{
			return c - ('A' - 10);
		}
		if (c >= 'a' && c <= 'f')
		{
			return c - ('a' - 10);
		}
		throw new IllegalArgumentException("Error hex char [" + c + "].");
	}

	/**
	 * 跳过指定个数的字符.
	 */
	protected boolean skipChars(int count)
	{
		if (count < 0 || count + this.charIndex >= this.charCount)
		{
			throw new IllegalArgumentException("Error skip count [" + count + "].");
		}
		if (count > 0)
		{
			this.charIndex += count;
			this.totalIndex += count;
		}
		return true;
	}

	/**
	 * 获取一个字符.
	 *
	 * @param index       字符所在的索引值
	 *                    0 表示获取当前字符
	 *                    正数 表示获取之后的字符
	 *                    负数 表示获取之前的字符
	 * @param checkBound  是否需要检查越界
	 * @return  指定位置的字符, -1 表示已超出范围
	 * @throws IllegalArgumentException  索引值超出缓存的大小
	 */
	protected int getChar(int index, boolean checkBound)
			throws IllegalArgumentException
	{
		if (Math.abs(index) > this.bufSize)
		{
			throw new IllegalArgumentException(
					"Error index [" + index + "] for buf size [" + this.bufSize + "].");
		}
		int pos = index + this.charIndex;
		if (pos < 0 || pos >= this.charCount)
		{
			if (checkBound)
			{
				throw new IllegalStateException("Not found char in [" + index + "].");
			}
			return -1;
		}
		return this.charBuf[pos];
	}

	/**
	 * 写入一个字符.
	 *
	 * @param c      需要写入的字符
	 * @param out    字符的输出流
	 * @param flush  是否需要将缓存的数据全部刷到输出流中
	 */
	protected void writeChar(char c, Writer out, boolean flush)
			throws IOException
	{
		if (1 + this.outIndex > this.outBuf.length)
		{
			out.write(this.outBuf, 0, this.outIndex);
			this.outIndex = 0;
		}
		this.outBuf[this.outIndex++] = c;
		if (flush)
		{
			this.flush(out);
		}
	}

	/**
	 * 写入一批字符.
	 *
	 * @param chars  需要写入的字符数组
	 * @param out    字符的输出流
	 * @param flush  是否需要将缓存的数据全部刷到输出流中
	 */
	protected void writeChars(char[] chars, Writer out, boolean flush)
			throws IOException
	{
		if (chars.length > this.outBuf.length)
		{
			if (this.outIndex > 0)
			{
				out.write(this.outBuf, 0, this.outIndex);
				this.outIndex = 0;
			}
			out.write(chars);
			if (flush)
			{
				out.flush();
			}
			return;
		}
		if (chars.length + this.outIndex > this.outBuf.length)
		{
			out.write(this.outBuf, 0, this.outIndex);
			this.outIndex = 0;
		}
		System.arraycopy(chars, 0, this.outBuf, this.outIndex, chars.length);
		this.outIndex += chars.length;
		if (flush)
		{
			this.flush(out);
		}
	}

	protected void flush(Writer out)
			throws IOException
	{
		if (this.outIndex > 0)
		{
			out.write(this.outBuf, 0, this.outIndex);
			this.outIndex = 0;
		}
		out.flush();
	}

}
