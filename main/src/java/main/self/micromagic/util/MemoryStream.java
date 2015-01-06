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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * 内存数据流. <p>
 * 可以通过{@link #getOutputStream()}方法获得输出流,
 * 通过{@link #getInputStream()}方法获得输入流,
 * 通过{@link #getRewriteStream(long)}方法获得重写输出流.
 */
public class MemoryStream
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
	private transient List inStreams = null;
	private transient OutputStream outStream = null;
	private transient List reOutStreams = null;

	public MemoryStream()
	{
		this(1, 1024, Utility.MEMORY_CACHE_SIZE_THRESHOLD);
	}

	public MemoryStream(int sizeThreshold)
	{
		this(1, 1024, sizeThreshold);
	}

	public MemoryStream(int blockCount, int blockSize)
	{
		this(blockCount, blockSize, Utility.MEMORY_CACHE_SIZE_THRESHOLD);
	}

	public MemoryStream(int blockCount, int blockSize, int sizeThreshold)
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
				this.blockList.add(new MemoryBlock(blockSize, i * (long) blockSize));
			}
		}
	}

	public boolean isInMemory()
	{
		return this.inMemory;
	}

	public void close()
			throws IOException
	{
		if (this.outStream != null)
		{
			this.outStream.close();
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
		String fileName = "MemoryStream_" + Utility.getUID() + "_" + getUniqueId() + ".tmp";
		File f = new File(tempDir, fileName);
		return f;
	}

	private static String getUniqueId()
	{
		int current;
		synchronized (MemoryStream.class)
		{
			current = COUNTER++ & 0xffffff;
		}
		return Integer.toString(current);
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
					MemoryBlock block = (MemoryBlock) this.blockList.get(index);
					if (hasSize >= this.blockSize)
					{
						this.diskCache.write(block.memory);
						hasSize -= this.blockSize;
					}
					else
					{
						this.diskCache.write(block.memory, 0, (int) hasSize);
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
				this.blockList.add(new MemoryBlock(this.blockSize, this.size));
				this.size += this.blockSize;
			}
		}
	}

	private void setByte(long index, int b)
			throws IOException
	{
		if (!this.isInMemory())
		{
			synchronized (MemoryStream.class)
			{
				this.diskCache.seek(index);
				this.diskCache.write(b);
				return;
			}
		}
		int blockIndex = (int) (index / this.blockSize);
		int realIndex = (int) (index % this.blockSize);
		MemoryBlock block = (MemoryBlock) this.blockList.get(blockIndex);
		block.memory[realIndex] = (byte) b;
	}

	private void setBytes(long index, byte b[], int off, int len)
			throws IOException
	{
		if (!this.isInMemory())
		{
			synchronized (MemoryStream.class)
			{
				this.diskCache.seek(index);
				this.diskCache.write(b, off, len);
				return;
			}
		}
		int blockIndex = (int) (index / this.blockSize);
		int realIndex = (int) (index % this.blockSize);
		while (len > 0)
		{
			MemoryBlock block = (MemoryBlock) this.blockList.get(blockIndex);
			int leftSize = this.blockSize - realIndex;
			if (leftSize >= len)
			{
				System.arraycopy(b, off, block.memory, realIndex, len);
				len = 0;
			}
			else
			{
				System.arraycopy(b, off, block.memory, realIndex, leftSize);
				off += leftSize;
				len -= leftSize;
				blockIndex++;
				realIndex = 0;
			}
		}
	}

	private int getByte(long index)
			throws IOException
	{
		if (!this.isInMemory())
		{
			synchronized (MemoryStream.class)
			{
				this.diskCache.seek(index);
				return this.diskCache.read();
			}
		}
		int blockIndex = (int) (index / this.blockSize);
		int realIndex = (int) (index % this.blockSize);
		MemoryBlock block = (MemoryBlock) this.blockList.get(blockIndex);
		return block.memory[realIndex] & 0xff;
	}

	private int getBytes(long index, byte b[], int off, int len)
			throws IOException
	{
		if (index >= this.usedSize)
		{
			return -1;
		}
		if (!this.isInMemory())
		{
			synchronized (MemoryStream.class)
			{
				this.diskCache.seek(index);
				if (index + len > this.usedSize)
				{
					len = (int) (this.usedSize - index);
				}
				return this.diskCache.read(b, off, len);
			}
		}
		int blockIndex = (int) (index / this.blockSize);
		int realIndex = (int) (index % this.blockSize);
		long hasSize = this.usedSize - index;
		int readCount = hasSize > len ? len : (int) hasSize;
		int returnCount = readCount;
		while (readCount > 0)
		{
			MemoryBlock block = (MemoryBlock) this.blockList.get(blockIndex);
			int nowSize = this.blockSize - realIndex;
			if (nowSize >= readCount)
			{
				System.arraycopy(block.memory, realIndex, b, off, readCount);
				readCount = 0;
			}
			else
			{
				System.arraycopy(block.memory, realIndex, b, off, nowSize);
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

	public InputStream getInputStream()
	{
		if (this.inStreams == null)
		{
			this.inStreams = new LinkedList();
		}
		InputStream ins = new MemoryInputStream();
		this.inStreams.add(ins);
		return ins;
	}

	private void removeInputStream(InputStream ins)
	{
		if (this.inStreams != null)
		{
			this.inStreams.remove(ins);
		}
	}

	public OutputStream getOutputStream()
	{
		if (this.outStream == null)
		{
			this.outStream = new MemoryOutputStream(0L);
		}
		return this.outStream;
	}

	public OutputStream getRewriteStream(long position)
	{
		if (position < 0)
		{
			throw new IndexOutOfBoundsException(Long.toString(position));
		}
		if (this.reOutStreams == null)
		{
			this.reOutStreams = new LinkedList();
		}
		OutputStream outs = new MemoryRewriteStream(position);
		this.reOutStreams.add(outs);
		return outs;
	}

	private void removeRewriteStream(OutputStream outs)
	{
		if (this.reOutStreams != null)
		{
			this.reOutStreams.remove(outs);
		}
	}

	public void clearMemoryStream()
	{
		if (this.inStreams != null)
		{
			List temp = this.inStreams;
			this.inStreams = null;  // 这里将置空提前, 防止在关闭时再删除一次
			Iterator itr = temp.iterator();
			while (itr.hasNext())
			{
				try
				{
					((InputStream) itr.next()).close();
				}
				catch (IOException ex) {}
			}
		}
		if (this.outStream != null)
		{
			try
			{
				this.outStream.close();
			}
			catch (IOException ex) {}
			this.outStream = null;
		}
		if (this.reOutStreams != null)
		{
			List temp = this.reOutStreams;
			this.reOutStreams = null;  // 这里将置空提前, 防止在关闭时再删除一次
			Iterator itr = temp.iterator();
			while (itr.hasNext())
			{
				try
				{
					((OutputStream) itr.next()).close();
				}
				catch (IOException ex) {}
			}
		}
		this.usedSize = 0L;
	}

	private class MemoryOutputStream extends OutputStream
	{
		private long position = 0L;
		private boolean closed = false;

		public MemoryOutputStream(long position)
		{
			this.position = position;
		}

		public void write(int b)
				throws IOException
		{
			this.checkClosed();
			if (MemoryStream.this.usedSize >= MemoryStream.this.size)
			{
				MemoryStream.this.addBolck(1);
			}
			MemoryStream.this.setByte(this.position++, b);
			MemoryStream.this.usedSize++;
		}

		public void write(byte b[], int off, int len)
				throws IOException
		{
			this.checkClosed();
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

			if (MemoryStream.this.usedSize + len > MemoryStream.this.size)
			{
				MemoryStream.this.addBolck(
						(int) (MemoryStream.this.usedSize + len - MemoryStream.this.size));
			}
			MemoryStream.this.setBytes(this.position, b, off, len);
			this.position += len;
			MemoryStream.this.usedSize += len;
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

	private static final long serialVersionUID = 1L;

	private class MemoryRewriteStream extends OutputStream
	{
		private long position = 0L;
		private boolean closed = false;

		public MemoryRewriteStream(long position)
		{
			this.position = position;
		}

		public void write(int b)
				throws IOException
		{
			this.checkClosed();
			if (this.position >= MemoryStream.this.usedSize)
			{
				throw new IOException("Out of size:" + MemoryStream.this.usedSize);
			}
			MemoryStream.this.setByte(this.position++, b);
		}

		public void write(byte b[], int off, int len)
				throws IOException
		{
			this.checkClosed();
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

			if (this.position + len > MemoryStream.this.usedSize)
			{
				throw new IOException("Out of size:" + MemoryStream.this.usedSize);
			}
			MemoryStream.this.setBytes(this.position, b, off, len);
			this.position += len;
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
			MemoryStream.this.removeRewriteStream(this);
		}

	}

	private class MemoryInputStream extends InputStream
	{
		private long position = 0L;
		private long markPos = 0L;
		private boolean closed = false;

		public int read()
				throws IOException
		{
			this.checkClosed();
			if (this.position >= MemoryStream.this.usedSize)
			{
				return -1;
			}
			return MemoryStream.this.getByte(this.position++);
		}

		public int read(byte b[], int off, int len)
				throws IOException
		{
			this.checkClosed();
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

			int count = MemoryStream.this.getBytes(this.position, b, off, len);
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
				long leftCount = MemoryStream.this.usedSize - this.position;
				changeCount = n < leftCount ? n : leftCount;
			}
			this.position += changeCount;
			return changeCount;
		}

		public int available()
				throws IOException
		{
			this.checkClosed();
			int leftCount = (int) (MemoryStream.this.usedSize - this.position);
			return leftCount < 0 ? Integer.MAX_VALUE : leftCount;
		}

		public void mark(int readlimit)
		{
			this.markPos = this.position;
		}

		public boolean markSupported()
		{
			return true;
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
				MemoryStream.this.removeInputStream(this);
			}
		}

	}

	private static class MemoryBlock
			implements Serializable
	{
		//public final long start;
		public final byte[] memory;

		public MemoryBlock(int size, long start)
		{
			this.memory = new byte[size];
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
		this.inStreams = null;
		this.outStream = new MemoryOutputStream(this.usedSize);
		this.reOutStreams = null;
	}

}