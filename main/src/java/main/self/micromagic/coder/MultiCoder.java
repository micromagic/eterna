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

public class MultiCoder extends AbstractCoder
		implements Coder
{
	private Coder[] coders;

	public MultiCoder(Coder[] coders)
	{
		this(coders, false);
	}

	private MultiCoder(Coder[] coders, boolean copyNew)
	{
		if (coders.length == 0)
		{
			throw new IllegalArgumentException("At least get one Coder.");
		}
		this.coders = new Coder[coders.length];
		if (copyNew)
		{
			for (int i = 0; i < coders.length; i++)
			{
				this.coders[i] = coders[i].createNew();
			}
		}
		else
		{
			System.arraycopy(coders, 0, this.coders, 0, coders.length);
		}
	}

	public Coder createNew()
	{
		return new MultiCoder(this.coders, true);
	}

	public void clear()
	{
		for (int i = 0; i < this.coders.length; i++)
		{
			this.coders[i].clear();
		}
	}

	public byte[] encode(byte[] buf, boolean over)
	{
		byte[] result = null;
		for (int i = 0; i < this.coders.length; i++)
		{
			result = this.coders[i].encode(buf, over);
			buf = result;
		}
		return result;
	}

	public byte[] decode(byte[] buf, boolean over)
	{
		byte[] result = null;
		for (int i = this.coders.length - 1; i >= 0; i--)
		{
			result = this.coders[i].decode(buf, over);
			buf = result;
		}
		return result;
	}

}