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

package self.micromagic.util;

/**
 * @author micromagic.sina.com
 */
class StringTool$StringBufferCreater
		implements StringAppender, StringTool.StringAppenderCreater
{
	private StringBuffer buf;

	StringTool$StringBufferCreater()
	{
	}

	private StringTool$StringBufferCreater(int initSize)
	{
		this.buf = new StringBuffer(initSize);
	}

	public StringAppender create(int initSize)
	{
		return new StringTool$StringBufferCreater(initSize);
	}

	public StringAppender append(Object obj)
	{
		this.buf.append(obj);
		return this;
	}

	public StringAppender append(String str)
	{
		this.buf.append(str);
		return this;
	}

	public StringAppender append(String str, int startIndex, int length)
	{
		this.buf.append(str.substring(startIndex, startIndex + length));
		return this;
	}

	public StringAppender append(char[] chars)
	{
		this.buf.append(chars);
		return this;
	}

	public StringAppender append(char[] chars, int startIndex, int length)
	{
		this.buf.append(chars, startIndex, length);
		return this;
	}

	public StringAppender append(boolean value)
	{
		this.buf.append(value);
		return this;
	}

	public StringAppender append(char ch)
	{
		this.buf.append(ch);
		return this;
	}

	public StringAppender append(CharSequence s)
	{
		this.buf.append(s);
		return this;
	}

	public StringAppender append(CharSequence s, int start, int end)
	{
		this.buf.append(s, start, end);
		return this;
	}

	public StringAppender append(int value)
	{
		this.buf.append(value);
		return this;
	}

	public StringAppender append(long value)
	{
		this.buf.append(value);
		return this;
	}

	public StringAppender append(float value)
	{
		this.buf.append(value);
		return this;
	}

	public StringAppender append(double value)
	{
		this.buf.append(value);
		return this;
	}

	public StringAppender appendln()
	{
		this.buf.append(Utility.LINE_SEPARATOR);
		return this;
	}

	public String substring(int beginIndex, int endIndex)
	{
		return this.buf.substring(beginIndex, endIndex);
	}

	public String toString()
	{
		return this.buf.toString();
	}

	public int length()
	{
		return this.buf.length();
	}

	public char charAt(int index)
	{
		return this.buf.charAt(index);
	}

	public CharSequence subSequence(int start, int end)
	{
		return this.buf.subSequence(start, end);
	}

}