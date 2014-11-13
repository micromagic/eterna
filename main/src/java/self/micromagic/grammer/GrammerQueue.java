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

import java.util.Map;

import self.micromagic.util.Utils;

public class GrammerQueue extends AbstractElement
		implements GrammerElement
{
	private String queue = "";

	public void initialize(Map elements) {}

	public boolean isTypeNone()
	{
		return this.getType() == TYPE_NONE;
	}

	public void setQueue(String queue)
	{
		this.queue = queue;
	}

	public boolean doVerify(ParserData pd)
			throws GrammerException
	{
		for (int i = 0; i < this.queue.length(); i++)
		{
			if (pd.isEnd())
			{
				return false;
			}
			char c = 0;
			try
			{
				c = pd.getNextChar();
			}
			catch (GrammerException ex)
			{
				if (pd.isEnd())
				{
					return false;
				}
				throw ex;
			}
			if (c != this.queue.charAt(i))
			{
				return false;
			}
		}
		return true;
	}

	public String toString()
	{
		return "Queue:" + this.getName() + ":" + GrammerManager.getGrammerElementTypeName(this.getType())
				+ ":Q[" + Utils.dealString2EditCode(this.queue) + "]";
	}


}