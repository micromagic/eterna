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

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;

import org.apache.commons.logging.Log;
import self.micromagic.util.Utility;
import self.micromagic.util.StringAppender;
import self.micromagic.util.StringTool;

public abstract class Encoder
{
	protected static final Log log = Utility.createLog("eterna.coder");

	/*
	private static final char[] hexDigits = {
		'0', '1', '2', '3', '4', '5', '6', '7',
		'8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
	};
	*/
	private static final char[] hexDigits = {
		'0', '1', '2', '3', '4', '5', '6', '7',
		'8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
	};

	private static final Encoder pureMd5 = new PureMd5();
	private static final Encoder defaultPSD = new Password();

	public abstract byte[] encode(byte[] buf);

	public String enString(String str, String charset)
	{
		byte[] buf = Coder.EMPTY_BYTE_ARRAY;
		if (str != null)
		{
			try
			{
				buf = str.getBytes(charset);
			}
			catch (UnsupportedEncodingException ex) {}
		}
		byte[] result = this.encode(buf);
		int count = result.length;
		StringAppender sbuf = StringTool.createStringAppender(count * 2);
		for (int i = 0; i < count; i++)
		{
			byte byte0 = result[i];
			sbuf.append(hexDigits[byte0 >>> 4 & 0xf]).append(hexDigits[byte0 & 0xf]);
		}
		return sbuf.toString();
	}

	public String enString(String str)
	{
		return this.enString(str, "UTF-8");
	}

	public static Encoder getInstance()
	{
		return pureMd5;
	}

	public static Encoder getPasswordInstance()
	{
		return defaultPSD;
	}

	public static String encodeString(String str)
	{
		return defaultPSD.enString(str);
	}

	private static class PureMd5 extends Encoder
	{
		private MessageDigest md5;

		public PureMd5()
		{
			try
			{
				this.md5 = MessageDigest.getInstance("MD5");
			}
			catch (Exception ex)
			{
				log.warn("When init encoder.", ex);
			}
		}

		public synchronized byte[] encode(byte[] buf)
		{
			this.md5.update(buf);
			return this.md5.digest();
		}

	}

	private static class Password extends Encoder
	{
		private Encoder firstCoder = pureMd5;
		private Base64 strCoder;

		public Password()
		{
			char[] codeTable = new char[64];
			int index = 0;
			for (int i = 'i'; i <= 'r'; i++) codeTable[index++] = (char) i;
			for (int i = 's'; i <= 'z'; i++) codeTable[index++] = (char) i;
			for (int i = 'R'; i <= 'Z'; i++) codeTable[index++] = (char) i;
			codeTable[index++] = '-';
			for (int i = 'L'; i <= 'Q'; i++) codeTable[index++] = (char) i;
			for (int i = '7'; i <= '9'; i++) codeTable[index++] = (char) i;
			for (int i = 'A'; i <= 'F'; i++) codeTable[index++] = (char) i;
			codeTable[index++] = '_';
			for (int i = 'a'; i <= 'd'; i++) codeTable[index++] = (char) i;
			for (int i = 'e'; i <= 'h'; i++) codeTable[index++] = (char) i;
			for (int i = '0'; i <= '6'; i++) codeTable[index++] = (char) i;
			for (int i = 'G'; i <= 'K'; i++) codeTable[index++] = (char) i;
			this.strCoder = new Base64(codeTable);
			//this.strCoder.printCodeTable(System.out, 10);
		}

		public synchronized byte[] encode(byte[] buf)
		{
			byte[] result = this.firstCoder.encode(buf);
			return this.strCoder.encode(result);
		}

		public synchronized String enString(String str, String charset)
		{
			byte[] buf = Coder.EMPTY_BYTE_ARRAY;
			if (str != null)
			{
				try
				{
					buf = str.getBytes(charset);
				}
				catch (UnsupportedEncodingException ex) {}
			}
			byte[] result = this.firstCoder.encode(buf);
			return this.strCoder.byteArrayToBase64(result);
		}

	}

}