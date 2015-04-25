/*
 * Copyright 2015 xinjunli (micromagic@sina.com).
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

package self.micromagic.coder;

import self.micromagic.util.StringAppender;
import self.micromagic.util.StringTool;

/**
 * Static methods for translating Coder$Base64 encoded strings to byte arrays
 * and vice-versa.
 */
public class Base64 extends AbstractCoder
		implements Coder
{
	private char[] intToBase64;
	private byte[] base64ToInt;
	private char fillFlag = '=';

	private byte[] encodeLeftByte = new byte[2];
	private int encodeLeftCount = 0;

	private byte[] decodeLeftByte = new byte[3];
	private int decodeLeftCount = 0;

	public Base64()
	{
		this.intToBase64 = DEFAULT_intToBase64;
		this.base64ToInt = DEFAULT_base64ToInt;
	}

	/**
	 * @param base64codes  64个字符的对应表; 或者是65个字符的对应表, 最后个字符为结束的填充符
	 */
	public Base64(char[] base64codes)
	{
		if (base64codes == null || (base64codes.length != 64 && base64codes.length != 65))
		{
			throw new IllegalArgumentException("The code count must be 64 or 65.");
		}
		if (base64codes.length == 65)
		{
			this.fillFlag = base64codes[64];
			if (this.fillFlag < ' ')
			{
				throw new IllegalArgumentException(
						"The fill flag [" + (this.fillFlag & 0xffff) + "] is lower than acsii:[32].");
			}
			if (this.fillFlag >= 128)
			{
				throw new IllegalArgumentException(
						"The fill flag [" + (this.fillFlag & 0xffff) + "] is large than acsii:[127].");
			}
		}
		byte[] temp = new byte[128];
		java.util.Arrays.fill(temp, (byte) -1);
		for (int i = 0; i < 64; i++)
		{
			if (base64codes[i] == this.fillFlag)
			{
				throw new IllegalArgumentException("The code can't contain fill flag [" + this.fillFlag + "].");
			}
			if (base64codes[i] < ' ')
			{
				throw new IllegalArgumentException(
						"The code at [" + i + "] [" + (base64codes[i] & 0xffff) + "] is lower than acsii:[32].");
			}
			if (base64codes[i] >= 128)
			{
				throw new IllegalArgumentException(
						"The code at [" + i + "] [" + (base64codes[i] & 0xffff) + "] is large than acsii:[127].");
			}
			if (temp[base64codes[i]] != -1)
			{
				throw new IllegalArgumentException(
						"The code at [" + i + "] [" + base64codes[i] + "] is duplicated.");
			}
			temp[base64codes[i]] = (byte) i;
		}
		this.intToBase64 = new char[64];
		System.arraycopy(base64codes, 0, this.intToBase64, 0, 64);
		this.base64ToInt = temp;
	}

	private Base64(Base64 src)
	{
		this.intToBase64 = src.intToBase64;
		this.base64ToInt = src.base64ToInt;
		this.fillFlag = src.fillFlag;
	}

	public void printCodeTable(java.io.PrintStream out, int lineSize)
	{
		String indentStr = "      ";
		for (int i = 0; i < this.intToBase64.length; i++)
		{
			if (i % lineSize == 0)
			{
				out.println();
				out.print(indentStr);
			}
			out.print('\'');
			out.print(this.intToBase64[i]);
			out.print("', ");
		}
		out.println();
		out.println();

		for (int i = 0; i < this.base64ToInt.length; i++)
		{
			if (i % lineSize == 0)
			{
				out.println();
				out.print(indentStr);
			}
			int temp = this.base64ToInt[i] & 0xff;
			if (temp == 0xff)
			{
				out.print("-1");
			}
			else
			{
				if (temp < 10)
				{
					out.print(' ');
				}
				out.print(Integer.toString(temp));
			}
			out.print(", ");
		}
		out.println();
		out.println();
	}

	public Coder createNew()
	{
		return new Base64(this);
	}

	public void clear()
	{
		this.encodeLeftCount = 0;
		this.decodeLeftCount = 0;
	}

	public byte[] encode(byte[] buf, boolean over)
	{
		int aLen = buf.length + this.encodeLeftCount;
		int numFullGroups = aLen / 3;
		int numBytesLeft = aLen - 3 * numFullGroups;
		int resultLen = over ? 4 * ((aLen + 2) / 3) : 4 * numFullGroups;
		byte[] result = null;
		if (resultLen == 0)
		{
			if (buf.length > 0)
			{
				this.encodeLeftByte[this.encodeLeftCount++] = buf[0];
				if (buf.length == 2)
				{
					this.encodeLeftByte[this.encodeLeftCount++] = buf[1];
				}
			}
			result = EMPTY_BYTE_ARRAY;
		}
		else
		{
			result = new byte[resultLen];
			char[] intToAlpha = this.intToBase64;

			int byte0;
			int byte1;
			int byte2;
			int inCursor = 0;
			int rCursor = 0;
			// 处理前面留下来的一些字节
			if (this.encodeLeftCount > 0)
			{
				if (numFullGroups >= 1)
				{
					if (this.encodeLeftCount == 1)
					{
						byte0 = this.encodeLeftByte[0] & 0xff;
						byte1 = buf[inCursor++] & 0xff;
						byte2 = buf[inCursor++] & 0xff;
					}
					else
					{
						byte0 = this.encodeLeftByte[0] & 0xff;
						byte1 = this.encodeLeftByte[1] & 0xff;
						byte2 = buf[inCursor++] & 0xff;
					}
					result[rCursor++] = (byte) intToAlpha[byte0 >> 2];
					result[rCursor++] = (byte) intToAlpha[(byte0 << 4) & 0x3f | (byte1 >> 4)];
					result[rCursor++] = (byte) intToAlpha[(byte1 << 2) & 0x3f | (byte2 >> 6)];
					result[rCursor++] = (byte) intToAlpha[byte2 & 0x3f];
					// 应为这里做掉了一组，所以后面减少1组
					numFullGroups--;
				}
				else
				{
					if (buf.length == 0)
					{
						buf = this.encodeLeftByte;
					}
					else
					{
						byte[] tbuf = new byte[2];
						tbuf[0] = this.encodeLeftByte[0];
						tbuf[1] = buf[0];
						buf = tbuf;
					}
				}
				// 将本次剩余的字节数附0，后面再附其它值
				this.encodeLeftCount = 0;
			}
			// Translate all full groups from byte array elements to base64
			for (int i = 0; i < numFullGroups; i++)
			{
				byte0 = buf[inCursor++] & 0xff;
				byte1 = buf[inCursor++] & 0xff;
				byte2 = buf[inCursor++] & 0xff;
				result[rCursor++] = (byte) intToAlpha[byte0 >> 2];
				result[rCursor++] = (byte) intToAlpha[(byte0 << 4) & 0x3f | (byte1 >> 4)];
				result[rCursor++] = (byte) intToAlpha[(byte1 << 2) & 0x3f | (byte2 >> 6)];
				result[rCursor++] = (byte) intToAlpha[byte2 & 0x3f];
			}

			// Translate partial group if present
			if (numBytesLeft != 0)
			{
				if (over)
				{
					byte0 = buf[inCursor++] & 0xff;
					result[rCursor++] = (byte) intToAlpha[byte0 >> 2];
					if (numBytesLeft == 1)
					{
						result[rCursor++] = (byte) intToAlpha[(byte0 << 4) & 0x3f];
						result[rCursor++] = (byte) this.fillFlag;
						result[rCursor++] = (byte) this.fillFlag;
					}
					else
					{
						// assert numBytesLeft == 2;
						byte1 = buf[inCursor++] & 0xff;
						result[rCursor++] = (byte) intToAlpha[(byte0 << 4) & 0x3f | (byte1 >> 4)];
						result[rCursor++] = (byte) intToAlpha[(byte1 << 2) & 0x3f];
						result[rCursor++] = (byte) this.fillFlag;
					}
				}
				else
				{
					this.encodeLeftCount = numBytesLeft;
					this.encodeLeftByte[0] = buf[inCursor++];
					if (numBytesLeft == 2)
					{
						this.encodeLeftByte[1] = buf[inCursor++];
					}
				}
			}
		}
		if (over)
		{
			this.encodeLeftCount = 0;
		}
		return result;
	}

	public byte[] decode(byte[] buf, boolean over)
	{
		byte[] result = null;
		int count = buf.length + this.decodeLeftCount;
		if (count < 4)
		{
			if (buf.length != 0)
			{
				for (int i = this.decodeLeftCount; i < count; i++)
				{
					this.decodeLeftByte[i] = buf[i - this.decodeLeftCount];
				}
				this.decodeLeftCount = count;
			}
			result = EMPTY_BYTE_ARRAY;
		}
		else
		{
			int numGroups = count / 4;
			int leftCount = count - numGroups * 4;
			int missingBytesInLastGroup = 0;
			int bufOffset = 0;
			int resultOffset = 0;
			if (over || leftCount == 0)
			{
				if (buf[buf.length - 1] == this.fillFlag)
				{
					missingBytesInLastGroup++;
				}
				if (buf.length > 1)
				{
					if (buf[buf.length - 2] == this.fillFlag)
					{
						missingBytesInLastGroup++;
					}
				}
				else
				{
					if (this.decodeLeftByte[this.decodeLeftCount - 1] == this.fillFlag)
					{
						missingBytesInLastGroup++;
					}
				}
			}
			result = new byte[3 * numGroups - missingBytesInLastGroup];
			if (this.decodeLeftCount > 0)
			{
				byte[] tempBuf = new byte[4];
				for (int i = 0; i < this.decodeLeftCount; i++)
				{
					tempBuf[i] = this.decodeLeftByte[i];
				}
				for (int i = this.decodeLeftCount; i < 4; i++)
				{
					tempBuf[i] = buf[bufOffset++];
				}
				int tempM = count > 4 ? 0 : missingBytesInLastGroup;
				this.base64ToByteArray(tempBuf, result, 0, 0, 4, tempM);
				resultOffset += 3;
				this.decodeLeftCount = 0;
				numGroups--;
			}
			this.base64ToByteArray(buf, result, bufOffset, resultOffset,
					(numGroups) * 4, missingBytesInLastGroup);
			if (leftCount > 0)
			{
				int start = buf.length - leftCount;
				for (int i = 0; i < leftCount; i++)
				{
					this.decodeLeftByte[i] = buf[start + i];
				}
				this.decodeLeftCount = leftCount;
			}
		}
		if (over)
		{
			this.decodeLeftCount = 0;
		}
		return result;
	}

	public String byteArrayToBase64(byte[] a)
	{
		int aLen = a.length;
		int numFullGroups = aLen / 3;
		int numBytesLeft = aLen - 3 * numFullGroups;
		int resultLen = 4 * ((aLen + 2) / 3);

		StringAppender result = StringTool.createStringAppender(resultLen);
		char[] intToAlpha = this.intToBase64;

		int byte0;
		int byte1;
		int byte2;
		int inCursor = 0;
		// Translate all full groups from byte array elements to base64
		for (int i = 0; i < numFullGroups; i++)
		{
			byte0 = a[inCursor++] & 0xff;
			byte1 = a[inCursor++] & 0xff;
			byte2 = a[inCursor++] & 0xff;
			result.append(intToAlpha[byte0 >> 2]);
			result.append(intToAlpha[(byte0 << 4) & 0x3f | (byte1 >> 4)]);
			result.append(intToAlpha[(byte1 << 2) & 0x3f | (byte2 >> 6)]);
			result.append(intToAlpha[byte2 & 0x3f]);
		}

		// Translate partial group if present
		if (numBytesLeft != 0)
		{
			byte0 = a[inCursor++] & 0xff;
			result.append(intToAlpha[byte0 >> 2]);
			if (numBytesLeft == 1)
			{
				result.append(intToAlpha[(byte0 << 4) & 0x3f]);
				result.append(this.fillFlag).append(this.fillFlag);
			}
			else
			{
				// assert numBytesLeft == 2;
				byte1 = a[inCursor++] & 0xff;
				result.append(intToAlpha[(byte0 << 4) & 0x3f | (byte1 >> 4)]);
				result.append(intToAlpha[(byte1 << 2) & 0x3f]);
				result.append(this.fillFlag);
			}
		}
		// assert inCursor == a.length;
		// assert result.length() == resultLen;
		return result.toString();
	}

	private void base64ToByteArray(byte[] des, byte[] src,
			int desOffset, int srcOffset, int count, int missingBytesInLastGroup)
	{
		if (count == 0)
		{
			return;
		}

		byte[] alphaToInt = this.base64ToInt;
		int numGroups = count / 4;
		int numFullGroups = numGroups - (missingBytesInLastGroup > 0 ? 1 : 0);

		byte ch0;
		byte ch1;
		byte ch2;
		byte ch3;
		// Translate all full groups from base64 to byte array elements
		int inCursor = desOffset, outCursor = srcOffset;
		for (int i = 0; i < numFullGroups; i++)
		{
			ch0 = (byte) base64ToInt((char) (des[inCursor++] & 0xff), alphaToInt);
			ch1 = (byte) base64ToInt((char) (des[inCursor++] & 0xff), alphaToInt);
			ch2 = (byte) base64ToInt((char) (des[inCursor++] & 0xff), alphaToInt);
			ch3 = (byte) base64ToInt((char) (des[inCursor++] & 0xff), alphaToInt);
			src[outCursor++] = (byte) ((ch0 << 2) | (ch1 >> 4));
			src[outCursor++] = (byte) ((ch1 << 4) | (ch2 >> 2));
			src[outCursor++] = (byte) ((ch2 << 6) | ch3);
		}

		// Translate partial group, if present
		if (missingBytesInLastGroup != 0)
		{
			ch0 = (byte) base64ToInt((char) (des[inCursor++] & 0xff), alphaToInt);
			ch1 = (byte) base64ToInt((char) (des[inCursor++] & 0xff), alphaToInt);
			src[outCursor++] = (byte) ((ch0 << 2) | (ch1 >> 4));

			if (missingBytesInLastGroup == 1)
			{
				ch2 = (byte) base64ToInt((char) (des[inCursor++] & 0xff), alphaToInt);
				src[outCursor++] = (byte) ((ch1 << 4) | (ch2 >> 2));
			}
		}
		// assert inCursor == count-missingBytesInLastGroup;
	}

	public byte[] base64ToByteArray(String s)
	{
		byte[] alphaToInt = this.base64ToInt;
		int sLen = s.length();
		int numGroups = sLen / 4;
		if (4 * numGroups != sLen)
		{
			throw new IllegalArgumentException("String length must be a multiple of 4.");
		}
		int missingBytesInLastGroup = 0;
		int numFullGroups = numGroups;
		if (sLen != 0)
		{
			if (s.charAt(sLen - 1) == this.fillFlag)
			{
				missingBytesInLastGroup++;
				numFullGroups--;
			}
			if (s.charAt(sLen - 2) == this.fillFlag)
			{
				missingBytesInLastGroup++;
			}
		}
		byte[] result = new byte[3 * numGroups - missingBytesInLastGroup];

		int ch0;
		int ch1;
		int ch2;
		int ch3;
		// Translate all full groups from base64 to byte array elements
		int inCursor = 0, outCursor = 0;
		for (int i = 0; i < numFullGroups; i++)
		{
			ch0 = base64ToInt(s.charAt(inCursor++), alphaToInt);
			ch1 = base64ToInt(s.charAt(inCursor++), alphaToInt);
			ch2 = base64ToInt(s.charAt(inCursor++), alphaToInt);
			ch3 = base64ToInt(s.charAt(inCursor++), alphaToInt);
			result[outCursor++] = (byte) ((ch0 << 2) | (ch1 >> 4));
			result[outCursor++] = (byte) ((ch1 << 4) | (ch2 >> 2));
			result[outCursor++] = (byte) ((ch2 << 6) | ch3);
		}

		// Translate partial group, if present
		if (missingBytesInLastGroup != 0)
		{
			ch0 = base64ToInt(s.charAt(inCursor++), alphaToInt);
			ch1 = base64ToInt(s.charAt(inCursor++), alphaToInt);
			result[outCursor++] = (byte) ((ch0 << 2) | (ch1 >> 4));

			if (missingBytesInLastGroup == 1)
			{
				ch2 = base64ToInt(s.charAt(inCursor++), alphaToInt);
				result[outCursor++] = (byte) ((ch1 << 4) | (ch2 >> 2));
			}
		}
		// assert inCursor == s.length()-missingBytesInLastGroup;
		// assert outCursor == result.length;
		return result;
	}

	/**
	 * Translates the specified character, which is assumed to be in the
	 * "Base 64 Alphabet" into its equivalent 6-bit positive integer.
	 */
	private static int base64ToInt(char c, byte[] alphaToInt)
	{
		int result = alphaToInt[c];
		if (result < 0)
		{
			throw new IllegalArgumentException("Illegal character [" + c + "].");
		}
		return result;
	}

	public static int base64ToInt(char c)
	{
		return base64ToInt(c, DEFAULT_base64ToInt);
	}

	public static char intToBase64(int code)
	{
		if (code < 0 || code >= 64)
		{
			throw new IllegalArgumentException("Illegal code [" + code + "].");
		}
		return DEFAULT_intToBase64[code];
	}

	/**
	 * This array is a lookup table that translates 6-bit positive integer
	 * index values into their "Coder$Base64 Alphabet" equivalents as specified
	 * in Table 1 of RFC 2045.
	 */
	private static final char[] DEFAULT_intToBase64 = {
		'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J',
		'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T',
		'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd',
		'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n',
		'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x',
		'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7',
		'8', '9', '+', '/'
	};

	/**
	 * This array is a lookup table that translates unicode characters
	 * drawn from the "Coder$Base64 Alphabet" (as specified in Table 1 of RFC 2045)
	 * into their 6-bit positive integer equivalents.  Characters that
	 * are not in the Coder$Base64 alphabet but fall within the bounds of the
	 * array are translated to -1.
	 */
	private static final byte[] DEFAULT_base64ToInt = {
		-1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
		-1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
		-1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
		-1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
		-1, -1, -1, 62, -1, -1, -1, 63, 52, 53,
		54, 55, 56, 57, 58, 59, 60, 61, -1, -1,
		-1, -1, -1, -1, -1,  0,  1,  2,  3,  4,
		 5,  6,  7,  8,  9, 10, 11, 12, 13, 14,
		15, 16, 17, 18, 19, 20, 21, 22, 23, 24,
		25, -1, -1, -1, -1, -1, -1, 26, 27, 28,
		29, 30, 31, 32, 33, 34, 35, 36, 37, 38,
		39, 40, 41, 42, 43, 44, 45, 46, 47, 48,
		49, 50, 51
	};

}