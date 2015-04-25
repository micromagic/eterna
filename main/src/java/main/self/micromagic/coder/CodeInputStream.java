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

import java.io.InputStream;
import java.io.IOException;

public class CodeInputStream extends InputStream
{
	private final int BLOCK_SIZE = 64;

	private InputStream in;
	private Coder coder;

	private byte[] oneByteBuf = new byte[1];
	private byte[] outBuf;
	private int outByteCount = 0;
	private int outByteOffset = 0;
	private byte[] readBuf = new byte[128];
	private boolean readOver = false;

	/**
	 * 读取时，coders使用的顺序是从后到前
	 */
	public CodeInputStream(Coder[] coders, InputStream in)
	{
		this.coder = new MultiCoder(coders);
		this.in = in;
	}

	public CodeInputStream(Coder coder, InputStream in)
	{
		this.coder = coder;
		this.in = in;
	}

	public int read()
			throws IOException
	{
		int count = this.read(this.oneByteBuf);
		if (count != 1)
		{
			return -1;
		}
		return this.oneByteBuf[0] & 0xff;
	}

	public int read(byte b[])
			throws IOException
	{
		if (b == null)
		{
			throw new NullPointerException();
		}
		if (this.outByteCount == -1)
		{
			return -1;
		}
		int hasCount = this.outByteCount - this.outByteOffset;

		if (hasCount > b.length)
		{
			System.arraycopy(this.outBuf, this.outByteOffset, b, 0, b.length);
			this.outByteOffset += b.length;
			return b.length;
		}

		int sum = 0;
		int leftCount = b.length;
		int copyPos = 0;
		while (leftCount > 0)
		{
			if (hasCount == 0)
			{
				if (!this.readToBuf())
				{
					return sum == 0 ? -1 : sum;
				}
			}
			hasCount = this.outByteCount - this.outByteOffset;

			if (hasCount >= leftCount)
			{
				System.arraycopy(this.outBuf, this.outByteOffset, b, copyPos, leftCount);
				this.outByteOffset += leftCount;
				sum += leftCount;
				return sum;
			}
			else
			{
				System.arraycopy(this.outBuf, this.outByteOffset, b, copyPos, hasCount);
				leftCount -= hasCount;
				this.outByteOffset += hasCount;
				sum += hasCount;
				copyPos += hasCount;
				hasCount = 0;
			}
		}
		return sum;
	}

	private boolean readToBuf()
			throws IOException
	{
		int count = this.in.read(this.readBuf);
		if (count == -1)
		{
			if (!this.readOver)
			{
				byte[] result = null;
				result = this.coder.decode(Coder.EMPTY_BYTE_ARRAY, true);
				this.outByteOffset = 0;
				this.outByteCount = result.length;
				this.outBuf = result;
				this.readOver = true;
				return result.length > 0;
			}
			this.outByteCount = -1;
			this.outByteOffset = 0;
			this.outBuf = null;
			return false;
		}

		byte[] rbuf;
		boolean over = count < this.readBuf.length;
		if (!over)
		{
			rbuf = this.readBuf;
		}
		else
		{
			rbuf = new byte[count];
			System.arraycopy(this.readBuf, 0, rbuf, 0, count);
			this.readOver = true;
		}
		byte[] result = null;
		result = this.coder.decode(rbuf, over);
		this.outByteOffset = 0;
		this.outByteCount = result.length;
		this.outBuf = result;
		return true;
	}

	public int read(byte b[], int off, int len)
			throws IOException
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
			return 0;
		}

		if (off == 0 && len == b.length)
		{
			return this.read(b);
		}

		int sum = 0;
		int leftCount = len;
		int nowCount = 0;
		int copyPos = off;
		byte[] temp = new byte[BLOCK_SIZE];
		while (leftCount > 0)
		{
			if (leftCount > BLOCK_SIZE)
			{
				leftCount -= BLOCK_SIZE;
				nowCount = BLOCK_SIZE;
			}
			else
			{
				temp = new byte[leftCount];
				nowCount = leftCount;
				leftCount = 0;
			}
			int count = this.read(temp);
			if (count == -1)
			{
				return sum == 0 ? -1 : sum;
			}
			else if (count < nowCount)
			{
				sum += count;
				System.arraycopy(temp, 0, b, copyPos, count);
				return sum;
			}
			System.arraycopy(temp, 0, b, copyPos, count);
			sum += count;
			copyPos += count;
		}
		return sum;
	}

	public int available()
			throws IOException
	{
		return this.in.available();
	}

	public void close()
			throws IOException
	{
		this.coder.decode(Coder.EMPTY_BYTE_ARRAY, true);
		this.in.close();
	}

}