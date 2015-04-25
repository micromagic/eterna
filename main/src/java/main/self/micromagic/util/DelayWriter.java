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

package self.micromagic.util;

import java.io.Writer;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Iterator;

import self.micromagic.util.MemoryChars;
import self.micromagic.util.Utility;

public class DelayWriter extends Writer
{
	private Writer out;
	private Writer realOut;
	private LinkedList delayBlocks = new LinkedList();

	public DelayWriter(Writer out)
	{
		this.out = out;
		this.realOut = out;
	}

	public void write(int c) throws IOException
	{
		this.out.write(c);
	}

	public void write(String str) throws IOException
	{
		this.out.write(str, 0, str.length());
	}

	public void write(String str, int off, int len)
			throws IOException
	{
		this.out.write(str, off, len);
	}

	public void write(char cbuf[])
			throws IOException
	{
		this.out.write(cbuf, 0, cbuf.length);
	}

	public void write(char cbuf[], int off, int len)
			throws IOException
	{
		this.out.write(cbuf, off, len);
	}

	public void flush() throws IOException
	{
		this.realOut.flush();
	}

	public void close()
			throws IOException
	{
		this.realOut.close();
		if (this.out != this.realOut)
		{
			this.out.close();
		}
	}

	public DelayBlock createDelayBlock()
	{
		MemoryChars mc = new MemoryChars();
		DelayBlock db = new DelayBlock(mc);
		this.out = mc.getWriter();
		this.delayBlocks.add(db);
		return db;
	}

	public void flushDelayBlock()
			throws IOException
	{
		this.flushDelayBlock0(false);
	}

	private void flushDelayBlock0(boolean check)
			throws IOException
	{
		Iterator itr = this.delayBlocks.iterator();
		while (itr.hasNext())
		{
			DelayBlock db = (DelayBlock) itr.next();
			if (check)
			{
				if (!db.isSetOver())
				{
					return;
				}
			}
			itr.remove();
			this.realOut.write(db.getValue());
			db.flushBuf(this.realOut);
		}
		this.out = this.realOut;
	}

	public class DelayBlock
	{
		private boolean setOver = false;
		private String value;
		private MemoryChars memoryBuf;

		public DelayBlock(MemoryChars memoryBuf)
		{
			this.memoryBuf = memoryBuf;
		}

		public void flushBuf(Writer out)
				throws IOException
		{
			Utility.copyChars(this.memoryBuf.getReader(), out);
		}

		public void set(String str)
				throws IOException
		{
			this.set(str, false);
		}

		public void set(String str, boolean setOver)
				throws IOException
		{
			this.setOver = setOver;
			this.value = str;
			DelayWriter.this.flushDelayBlock0(true);
		}

		public String getValue()
		{
			return value;
		}

		public void setOver()
		{
			this.setOver = true;
		}

		public boolean isSetOver()
		{
			return setOver;
		}

	}

}