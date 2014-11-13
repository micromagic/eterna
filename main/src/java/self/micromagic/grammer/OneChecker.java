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

package self.micromagic.grammer;

/**
 * 单个的字符检查器.
 */
public abstract class OneChecker
		implements Checker
{

	public boolean verify(ParserData pd)
			throws GrammerException
	{
		pd.pushChecker(this);
		boolean result = this.verify(pd.getCurrentChar());
		pd.popChecker(result);
		return result;
	}

	protected abstract boolean verify(char c);

	public static class SetChecker extends OneChecker
			implements Checker
	{
		private String chars;

		public SetChecker(String chars)
		{
			this.chars = chars == null ? "" : chars;
		}

		public boolean verify(char c)
		{
			return this.chars.indexOf(c) != -1;
		}

		public String toString()
		{
			return this.chars;
		}

	}

	public static class RangeChecker extends OneChecker
			implements Checker
	{
		private char beginChar;
		private char endChar;

		public RangeChecker(char beginChar, char endChar)
		{
			this.beginChar = beginChar;
			this.endChar = endChar;
		}

		public boolean verify(char c)
		{
			return c >= this.beginChar && c <= this.endChar;
		}

		public String toString()
		{
			return "[" + this.beginChar + "-" + this.endChar + "]";
		}

	}
}