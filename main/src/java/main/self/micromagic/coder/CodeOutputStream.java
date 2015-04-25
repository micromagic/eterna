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

import java.io.IOException;
import java.io.OutputStream;

public class CodeOutputStream extends OutputStream
{
	private final int BLOCK_SIZE = 64;

	private OutputStream out;
	private Coder coder;

	private byte[] oneByteBuf = new byte[1];

	/**
	 * 读取时，coders使用的顺序是从前到后
	 */
	public CodeOutputStream(Coder[] coders, OutputStream out)
	{
		this.out = out;
		this.coder = new MultiCoder(coders);
	}

	public CodeOutputStream(Coder coder, OutputStream out)
	{
		this.out = out;
		this.coder = coder;
	}

	public void write(int b) throws IOException
	{
		this.oneByteBuf[0] = (byte) b;
		this.write(this.oneByteBuf);
	}

	public void write(byte b[]) throws IOException
	{
		if (b == null)
		{
			throw new NullPointerException();
		}

		byte[] result = null;
		result = this.coder.encode(b, false);
		if (result.length > 0)
		{
			this.out.write(result);
		}
	}

	public void write(byte b[], int off, int len) throws IOException
	{
		if (b == null)
		{
			throw new NullPointerException();
		}
		else if ((off < 0) || (off > b.length) || (len < 0) ||
				((off + len) > b.length) || ((off + len) < 0))
		{
			throw new IndexOutOfBoundsException();
		}
		else if (len == 0)
		{
			return;
		}

		if (off == 0 && len == b.length)
		{
			this.write(b);
			return;
		}

		int leftCount = len;
		int copyPos = off;
		byte[] temp = new byte[BLOCK_SIZE];
		while (leftCount > 0)
		{
			if (leftCount > BLOCK_SIZE)
			{
				System.arraycopy(b, copyPos, temp, 0, BLOCK_SIZE);
				this.write(temp);
				leftCount -= BLOCK_SIZE;
				copyPos += BLOCK_SIZE;
			}
			else
			{
				temp = new byte[leftCount];
				System.arraycopy(b, copyPos, temp, 0, leftCount);
				this.write(temp);
				copyPos += leftCount;
				leftCount = 0;
			}
		}
	}

	public void flush() throws IOException
	{
		this.out.flush();
	}

	public void close() throws IOException
	{
		byte[] result = null;
		result = this.coder.encode(Coder.EMPTY_BYTE_ARRAY, true);
		if (result.length > 0)
		{
			this.out.write(result);
		}
		this.out.close();
	}

}