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

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.io.Reader;
import java.io.Serializable;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class MemoryChars
		implements Serializable
{
	private static int COUNTER = 0;

	private List blockList;
	private final int blockSize;
	private final int sizeThreshold;
	private long size;

	private boolean inMemory = true;
	private transient RandomAccessFile diskCache = null;
	private transient boolean diskCacheSerialized = false;
	private File diskCacheFile = null;

	private long usedSize = 0L;
	private transient List readers = null;
	private transient Writer writer = null;
	private transient List reWriters = null;

	public MemoryChars()
	{
		this(1, 1024, Utility.MEMORY_CACHE_SIZE_THRESHOLD);
	}

	public MemoryChars(int sizeThreshold)
	{
		this(1, 1024, sizeThreshold);
	}

	public MemoryChars(int blockCount, int blockSize)
	{
		this(blockCount, blockSize, Utility.MEMORY_CACHE_SIZE_THRESHOLD);
	}

	public MemoryChars(int blockCount, int blockSize, int sizeThreshold)
	{
		if (blockCount <= 0)
		{
			throw new IllegalArgumentException("Error block count" + blockCount + ".");
		}
		if (blockSize <= 0)
		{
			throw new IllegalArgumentException("Error block size" + blockSize + ".");
		}
		this.sizeThreshold = sizeThreshold;
		this.blockSize = blockSize;
		this.size = blockCount * (long) blockSize;
		if (this.size > this.sizeThreshold)
		{
			this.dealMemory2Disk(false);
		}
		else
		{
			this.blockList = new ArrayList();
			for (int i = 0; i < blockCount; i++)
			{
				this.blockList.add(new CharsBlock(blockSize, i * (long) blockSize));
			}
		}
	}

	/**
	 * 获取单个存储单元的大小.
	 */
	public int getBlockSize()
	{
		return this.blockSize;
	}

	/**
	 * 获取使用存储单元的个数.
	 * 如果返回-1表示未存储在内存中.
	 */
	public int getBlockCount()
	{
		List list = this.blockList;
		return list == null ? -1 : list.size();
	}

	/**
	 * 数据是否存储在内存中.
	 */
	public boolean isInMemory()
	{
		return this.inMemory;
	}

	public void close()
			throws IOException
	{
		if (this.writer != null)
		{
			this.writer.close();
		}
		if (this.isInMemory())
		{
			return;
		}
		this.diskCache.close();
		if (!this.diskCacheSerialized)
		{
			this.diskCacheFile.delete();
		}
	}

	public void finalize()
			throws IOException
	{
		this.close();
	}

	private File getTempFile()
	{
		File tempDir = new File(System.getProperty("java.io.tmpdir"));
		String fileName = "MemoryChars_" + Utility.getUID() + "_" + getUniqueId() + ".tmp";
		File f = new File(tempDir, fileName);
		return f;
	}

	private static String getUniqueId()
	{
		int current;
		synchronized (MemoryChars.class)
		{
			current = COUNTER++ & 0xffffff;
		}
		return Integer.toString(current);
	}

	private byte[] chars2Bytes(char[] chars, int off, int len)
	{
		byte[] buf = new byte[len * 2];
		for (int i = off; i < len; i++)
		{
			char v = chars[i];
			buf[(i - off) * 2] = (byte) ((v >>> 8) & 0xFF);
			buf[(i - off) * 2 + 1] = (byte) (v & 0xFF);
		}
		return buf;
	}

	private byte[] string2Bytes(String str, int off, int len)
	{
		byte[] buf = new byte[len * 2];
		for (int i = off; i < len; i++)
		{
			char v = str.charAt(i);
			buf[(i - off) * 2] = (byte) ((v >>> 8) & 0xFF);
			buf[(i - off) * 2 + 1] = (byte) (v & 0xFF);
		}
		return buf;
	}

	private char[] bytes2Chars(byte[] buf)
	{
		char[] chars = new char[buf.length / 2];
		for (int i = 0; i < buf.length; i += 2)
		{
			chars[i / 2] = (char) (((buf[i] << 8) | (buf[i + 1] & 0xFF)) & 0xFFFF);
		}
		return chars;
	}

	private void dealMemory2Disk(boolean deal)
	{
		this.diskCacheFile = this.getTempFile();
		try
		{
			this.diskCache = new RandomAccessFile(this.diskCacheFile, "rw");
			if (deal)
			{
				long hasSize = this.usedSize;
				int index = 0;
				while (hasSize > 0)
				{
					CharsBlock block = (CharsBlock) this.blockList.get(index);
					if (hasSize >= this.blockSize)
					{
						this.diskCache.write(this.chars2Bytes(block.chars, 0, this.blockSize));
						hasSize -= this.blockSize;
					}
					else
					{
						this.diskCache.write(this.chars2Bytes(block.chars, 0, (int) hasSize));
						hasSize = 0;
					}
					index++;
				}
			}
		}
		catch (Exception ex)
		{
			Utility.createLog("eterna.memory.io").error("Error in create file:" + this.diskCacheFile + ".", ex);
		}
		this.inMemory = false;
		this.blockList = null;
	}

	private synchronized void addBolck(int size)
	{
		if (!this.isInMemory())
		{
			// 如果不在内存中, 就不用添加块了
			this.size += size;
			return;
		}
		if (this.size + size > this.sizeThreshold)
		{
			this.dealMemory2Disk(true);
		}
		else
		{
			int count = size / this.blockSize;
			if (count * this.blockSize < size)
			{
				count++;
			}
			for (int i = 0; i < count; i++)
			{
				this.blockList.add(new CharsBlock(this.blockSize, this.size));
				this.size += this.blockSize;
			}
		}
	}

	private void setChar(long index, int c)
			throws IOException
	{
		if (!this.isInMemory())
		{
			synchronized (MemoryChars.class)
			{
				this.diskCache.seek(index * 2);
				this.diskCache.write(this.chars2Bytes(new char[]{(char) c}, 0, 1));
				return;
			}
		}
		int blockIndex = (int) (index / this.blockSize);
		int realIndex = (int) (index % this.blockSize);
		CharsBlock block = (CharsBlock) this.blockList.get(blockIndex);
		block.chars[realIndex] = (char) c;
	}

	private void setChars(long index, char cbuf[], int off, int len)
			throws IOException
	{
		if (!this.isInMemory())
		{
			synchronized (MemoryChars.class)
			{
				this.diskCache.seek(index * 2);
				this.diskCache.write(this.chars2Bytes(cbuf, off, len));
				return;
			}
		}
		int blockIndex = (int) (index / this.blockSize);
		int realIndex = (int) (index % this.blockSize);
		while (len > 0)
		{
			CharsBlock block = (CharsBlock) this.blockList.get(blockIndex);
			int leftSize = this.blockSize - realIndex;
			if (leftSize >= len)
			{
				System.arraycopy(cbuf, off, block.chars, realIndex, len);
				len = 0;
			}
			else
			{
				System.arraycopy(cbuf, off, block.chars, realIndex, leftSize);
				off += leftSize;
				len -= leftSize;
				blockIndex++;
				realIndex = 0;
			}
		}
	}

	private void setChars(long index, String str, int off, int len)
			throws IOException
	{
		if (!this.isInMemory())
		{
			synchronized (MemoryChars.class)
			{
				this.diskCache.seek(index * 2);
				this.diskCache.write(this.string2Bytes(str, off, len));
				return;
			}
		}
		int blockIndex = (int) (index / this.blockSize);
		int realIndex = (int) (index % this.blockSize);
		while (len > 0)
		{
			CharsBlock block = (CharsBlock) this.blockList.get(blockIndex);
			int leftSize = this.blockSize - realIndex;
			if (leftSize >= len)
			{
				str.getChars(off, off + len, block.chars, realIndex);
				len = 0;
			}
			else
			{
				str.getChars(off, off + leftSize, block.chars, realIndex);
				off += leftSize;
				len -= leftSize;
				blockIndex++;
				realIndex = 0;
			}
		}
	}

	private int getChar(long index)
			throws IOException
	{
		if (!this.isInMemory())
		{
			synchronized (MemoryChars.class)
			{
				this.diskCache.seek(index * 2);
				int b1 = this.diskCache.read();
				int b2 = this.diskCache.read();
				return (char) (((b1 << 8) | b2) & 0xFFFF);
			}
		}
		int blockIndex = (int) (index / this.blockSize);
		int realIndex = (int) (index % this.blockSize);
		CharsBlock block = (CharsBlock) this.blockList.get(blockIndex);
		return block.chars[realIndex] & 0xff;
	}

	private int getChars(long index, char cbuf[], int off, int len)
			throws IOException
	{
		if (index >= this.usedSize)
		{
			return -1;
		}
		if (!this.isInMemory())
		{
			synchronized (MemoryChars.class)
			{
				this.diskCache.seek(index * 2);
				if (index + len > this.usedSize)
				{
					len = (int) (this.usedSize - index);
				}
				byte[] tmpBuf = new byte[len * 2];
				this.diskCache.read(tmpBuf);
				char[] tmpChars = this.bytes2Chars(tmpBuf);
				System.arraycopy(tmpChars, 0, cbuf, off, len);
				return len;
			}
		}
		int blockIndex = (int) (index / this.blockSize);
		int realIndex = (int) (index % this.blockSize);
		long hasSize = this.usedSize - index;
		int readCount = hasSize > len ? len : (int) hasSize;
		int returnCount = readCount;
		while (readCount > 0)
		{
			CharsBlock block = (CharsBlock) this.blockList.get(blockIndex);
			int nowSize = this.blockSize - realIndex;
			if (nowSize >= readCount)
			{
				System.arraycopy(block.chars, realIndex, cbuf, off, readCount);
				readCount = 0;
			}
			else
			{
				System.arraycopy(block.chars, realIndex, cbuf, off, nowSize);
				off += nowSize;
				readCount -= nowSize;
				blockIndex++;
				realIndex = 0;
			}
		}
		return returnCount;
	}

	public long getUsedSize()
	{
		return this.usedSize;
	}

	public Reader getReader()
	{
		if (this.readers == null)
		{
			this.readers = new LinkedList();
		}
		Reader reader = new MemoryReader();
		this.readers.add(reader);
		return reader;
	}

	private void removeReader(Reader reader)
	{
		if (this.readers != null)
		{
			this.readers.remove(reader);
		}
	}

	public Writer getWriter()
	{
		if (this.writer == null)
		{
			this.writer = new MemoryWriter(0L);
		}
		return this.writer;
	}


	public Writer getRewriter(long position)
	{
		if (position < 0)
		{
			throw new IndexOutOfBoundsException(Long.toString(position));
		}
		if (this.reWriters == null)
		{
			this.reWriters = new LinkedList();
		}
		MemoryRewriter writer = new MemoryRewriter(position);
		this.reWriters.add(writer);
		return writer;
	}

	private void removeRewriter(Writer writer)
	{
		if (this.reWriters != null)
		{
			this.reWriters.remove(writer);
		}
	}

	public void clearMemoryChars()
	{
		if (this.readers != null)
		{
			List temp = this.readers;
			this.readers = null;  // 这里将置空提前, 防止在关闭时再删除一次
			Iterator itr = temp.iterator();
			while (itr.hasNext())
			{
				try
				{
					((Reader) itr.next()).close();
				}
				catch (IOException ex) {}
			}
		}
		if (this.writer != null)
		{
			try
			{
				this.writer.close();
			}
			catch (IOException ex) {}
			this.writer = null;
		}
		if (this.reWriters != null)
		{
			List temp = this.reWriters;
			this.reWriters = null;  // 这里将置空提前, 防止在关闭时再删除一次
			Iterator itr = temp.iterator();
			while (itr.hasNext())
			{
				try
				{
					((Writer) itr.next()).close();
				}
				catch (IOException ex) {}
			}
		}
		this.usedSize = 0L;
	}

	private static final long serialVersionUID = 1L;

	private class MemoryWriter extends Writer
	{
		private long position = 0L;
		private boolean closed = false;

		public MemoryWriter(long position)
		{
			this.position = position;
		}

		public void write(int c)
				throws IOException
		{
			this.checkClosed();
			if (MemoryChars.this.usedSize >= MemoryChars.this.size)
			{
				MemoryChars.this.addBolck(1);
			}
			MemoryChars.this.setChar(this.position++, c);
			MemoryChars.this.usedSize++;

		}

		public void write(char cbuf[], int off, int len)
				throws IOException
		{
			this.checkClosed();
			if (cbuf == null)
			{
				throw new NullPointerException();
			}
			else if ((off < 0) || (off > cbuf.length) || (len < 0) ||
					((off + len) > cbuf.length) || ((off + len) < 0))
			{
				throw new IndexOutOfBoundsException();
			}
			else if (len == 0)
			{
				return;
			}

			if (MemoryChars.this.usedSize + len > MemoryChars.this.size)
			{
				MemoryChars.this.addBolck(
						(int) (MemoryChars.this.usedSize + len - MemoryChars.this.size));
			}
			MemoryChars.this.setChars(this.position, cbuf, off, len);
			this.position += len;
			MemoryChars.this.usedSize += len;
		}

		public void write(String str, int off, int len)
				throws IOException
		{
			this.checkClosed();
			if (str == null)
			{
				throw new NullPointerException();
			}
			else if ((off < 0) || (off > str.length()) || (len < 0) ||
					((off + len) > str.length()) || ((off + len) < 0))
			{
				throw new StringIndexOutOfBoundsException();
			}
			else if (len == 0)
			{
				return;
			}

			if (MemoryChars.this.usedSize + len > MemoryChars.this.size)
			{
				MemoryChars.this.addBolck(
						(int) (MemoryChars.this.usedSize + len - MemoryChars.this.size));
			}
			MemoryChars.this.setChars(this.position, str, off, len);
			this.position += len;
			MemoryChars.this.usedSize += len;
		}

		public void flush()
		{
		}

		private void checkClosed()
				throws IOException
		{
			if (this.closed)
			{
				throw new IOException("This stream is closed.");
			}
		}

		public void close()
		{
			this.closed = true;
		}

	}

	private class MemoryRewriter extends Writer
	{
		private long position = 0L;
		private boolean closed = false;

		public MemoryRewriter(long position)
		{
			this.position = position;
		}

		public void write(int c)
				throws IOException
		{
			this.checkClosed();
			if (this.position >= MemoryChars.this.usedSize)
			{
				throw new IOException("Out of size:" + MemoryChars.this.usedSize);
			}
			MemoryChars.this.setChar(this.position++, c);

		}

		public void write(char cbuf[], int off, int len)
				throws IOException
		{
			this.checkClosed();
			if (cbuf == null)
			{
				throw new NullPointerException();
			}
			else if ((off < 0) || (off > cbuf.length) || (len < 0) ||
					((off + len) > cbuf.length) || ((off + len) < 0))
			{
				throw new IndexOutOfBoundsException();
			}
			else if (len == 0)
			{
				return;
			}

			if (this.position + len > MemoryChars.this.usedSize)
			{
				throw new IOException("Out of size:" + MemoryChars.this.usedSize);
			}
			MemoryChars.this.setChars(this.position, cbuf, off, len);
			this.position += len;
		}

		public void write(String str, int off, int len)
				throws IOException
		{
			this.checkClosed();
			if (str == null)
			{
				throw new NullPointerException();
			}
			else if ((off < 0) || (off > str.length()) || (len < 0) ||
					((off + len) > str.length()) || ((off + len) < 0))
			{
				throw new StringIndexOutOfBoundsException();
			}
			else if (len == 0)
			{
				return;
			}

			if (this.position + len > MemoryChars.this.usedSize)
			{
				throw new IOException("Out of size:" + MemoryChars.this.usedSize);
			}
			MemoryChars.this.setChars(this.position, str, off, len);
			this.position += len;
		}

		public void flush()
		{
		}

		private void checkClosed()
				throws IOException
		{
			if (this.closed)
			{
				throw new IOException("This stream is closed.");
			}
		}

		public void close()
		{
			this.closed = true;
			MemoryChars.this.removeRewriter(this);
		}

	}

	private class MemoryReader extends Reader
	{
		private long position = 0L;
		private long markPos = 0L;
		private boolean closed = false;

		public int read()
				throws IOException
		{
			this.checkClosed();
			if (this.position >= MemoryChars.this.usedSize)
			{
				return -1;
			}
			return MemoryChars.this.getChar(this.position++);
		}

		public int read(char cbuf[], int off, int len)
				throws IOException
		{
			this.checkClosed();
			if (cbuf == null)
			{
				throw new NullPointerException();
			}
			else if ((off < 0) || (off > cbuf.length) || (len < 0) ||
					((off + len) > cbuf.length) || ((off + len) < 0))
			{
				throw new IndexOutOfBoundsException();
			}
			else if (len == 0)
			{
				return 0;
			}

			int count = MemoryChars.this.getChars(this.position, cbuf, off, len);
			if (count >= 0)
			{
				this.position += count;
			}
			return count;
		}

		public long skip(long n)
				throws IOException
		{
			this.checkClosed();
			long changeCount = 0;
			if (n <= 0)
			{
				changeCount = this.position + n > 0 ? n : -this.position;
			}
			else
			{
				long leftCount = MemoryChars.this.usedSize - this.position;
				changeCount = n < leftCount ? n : leftCount;
			}
			this.position += changeCount;
			return changeCount;
		}

		public boolean ready()
				throws IOException
		{
			this.checkClosed();
			return this.position < MemoryChars.this.usedSize;
		}

		public boolean markSupported()
		{
			return true;
		}

		public void mark(int readAheadLimit)
				throws IOException
		{
			this.checkClosed();
			this.markPos = this.position;
		}

		public void reset()
				throws IOException
		{
			this.checkClosed();
			this.position = this.markPos;
		}

		private void checkClosed()
				throws IOException
		{
			if (this.closed)
			{
				throw new IOException("This stream is closed.");
			}
		}

		public void close()
		{
			if (!this.closed)
			{
				this.closed = true;
				MemoryChars.this.removeReader(this);
			}
		}

	}

	private static class CharsBlock
			implements Serializable
	{
		public final char[] chars;
		//public final long start;

		public CharsBlock(int size, long start)
		{
			this.chars = new char[size];
			//this.start = start;
		}

		private static final long serialVersionUID = 1L;

	}

	private void writeObject(ObjectOutputStream out)
			throws IOException
	{
		if (!this.isInMemory())
		{
			this.diskCacheSerialized = true;
		}
		out.defaultWriteObject();
	}

	private void readObject(ObjectInputStream in)
			throws IOException, ClassNotFoundException
	{
		in.defaultReadObject();
		if (!this.isInMemory())
		{
			this.diskCache = new RandomAccessFile(this.diskCacheFile, "rw");
		}
		else
		{
			this.diskCache = null;
		}
		this.diskCacheSerialized = false;
		this.readers = null;
		this.writer = new MemoryWriter(this.usedSize);
		this.reWriters = null;
	}

}