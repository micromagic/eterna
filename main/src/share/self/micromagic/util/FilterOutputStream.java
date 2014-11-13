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

import java.io.IOException;
import java.io.OutputStream;

public class FilterOutputStream extends OutputStream
{
	private byte[] beginFlag;
	private byte[] endFlag;
	private byte[] flagBuf;
	private OutputStream out;
	private int bufPos = 0;
	private boolean canOut = false;

	public FilterOutputStream(byte[] beginFlag, byte[] endFlag, OutputStream out)
	{
		this.beginFlag = beginFlag;
		this.endFlag = endFlag;
		this.flagBuf = new byte[Math.max(beginFlag.length, endFlag.length)];
		this.out = out;
	}

	public void write(int b)
			throws IOException
	{
		byte[] flag = this.canOut ? this.endFlag : this.beginFlag;
		if (flag[this.bufPos] == (byte) b)
		{
			this.flagBuf[this.bufPos++] = (byte) b;
			if (this.bufPos == flag.length)
			{
				this.canOut = !this.canOut;
				this.bufPos = 0;
			}
		}
		else
		{
			this.setSubPos(flag);
		}
	}

	private void setSubPos(byte[] flag)
			throws IOException
	{
		for (int i = 1; i < this.bufPos; i++)
		{
			if (this.flagBuf[i] == flag[0])
			{
				boolean allSame = true;
				for (int j = i + 1; j < this.bufPos; j++)
				{
					if (this.flagBuf[j] != flag[j - i])
					{
						allSame = false;
						break;
					}
				}
				if (allSame)
				{
					if (this.canOut)
					{
						this.out.write(this.flagBuf, 0, i);
					}
					System.arraycopy(this.flagBuf, i, this.flagBuf, 0, this.bufPos - i);
					this.bufPos -= i;
					return;
				}
			}
		}
		if (this.canOut && this.bufPos > 0)
		{
			this.out.write(this.flagBuf, 0, this.bufPos);
		}
		this.bufPos = 0;
	}

	public void flush()
			throws IOException
	{
		this.out.flush();
	}

	public void close()
			throws IOException
	{
		this.out.close();
	}

	public static void main(String[] args)
			throws IOException
	{
		FilterOutputStream fos = new FilterOutputStream("(".getBytes(), ")".getBytes(), System.out);
		fos.write("ab(cd)efg1(234)5".getBytes());
		fos.flush();
	}

}