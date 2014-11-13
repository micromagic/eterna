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

package self.micromagic.coder;

import java.util.Arrays;

public class Gost extends AbstractCoder
		implements Coder
{
	private static final int GROUP_SIZE = 8;
	private static final int ENCODE_BYTES_SIZE = 8;
	private static final int DECODE_BYTES_SIZE = 9;

	private int[] enKey = new int[8];

	private byte[] encodeLeftByte = new byte[ENCODE_BYTES_SIZE];
	private int encodeLeftCount = 0;
	private byte[] decodeLeftByte = new byte[DECODE_BYTES_SIZE];
	private int decodeLeftCount = 0;

	public Gost(byte[] key)
	{
		if (key.length > 0)
		{
			byte[] temp = new byte[32];
			int count = 32;
			int copyCount = Math.min(key.length, count);
			System.arraycopy(key, 0, temp, 0, copyCount);
			int[] tempBuf = new int[2];
			for (int i = 0; i < 4; i++)
			{
				dataToBuf(temp, i * 8, tempBuf);
				this.enKey[i * 2] = tempBuf[0];
				this.enKey[i * 2 + 1] = tempBuf[1];
			}
		}
	}

	private Gost(Gost src)
	{
		this.enKey = src.enKey;
	}

	public Coder createNew()
	{
		return new Gost(this);
	}

	public void clear()
	{
		this.encodeLeftCount = 0;
		this.decodeLeftCount = 0;
	}

	public byte[] decode(byte[] des)
	{
		if (des.length % GROUP_SIZE != 1)
		{
			throw new IllegalArgumentException("The byte array must be a (length MOD 8 = 1).");
		}
		return this.decode(des, true);
	}

	public byte[] encode(byte[] buf, boolean over)
	{
		int aLen = buf.length + this.encodeLeftCount;
		int numFullGroups = aLen / GROUP_SIZE;
		int numBytesLeft = aLen - GROUP_SIZE * numFullGroups;
		// 如果是要结束的话则要增加一组加一个字节, 一组是结束用的, 一个字节是对剩余字节描述
		int resultLen = over ?
				GROUP_SIZE * (numFullGroups + (numBytesLeft > 0 ? 1 : 0)) + 1 :
				GROUP_SIZE * numFullGroups;
		byte[] result = null;
		if (resultLen == 0)
		{
			if (buf.length > 0)
			{
				System.arraycopy(buf, 0, this.encodeLeftByte,
						this.encodeLeftCount, buf.length);
				this.encodeLeftCount += buf.length;
			}
			result = EMPTY_BYTE_ARRAY;
		}
		else
		{
			result = new byte[resultLen];
			int[] tempBuf = new int[2];
			int srcOffset = 0;
			int desOffset = 0;

			// 处理前面留下来的一些字节
			if (this.encodeLeftCount > 0 && numFullGroups >= 1)
			{
				srcOffset = ENCODE_BYTES_SIZE - this.encodeLeftCount;
				System.arraycopy(buf, 0, this.encodeLeftByte,
						this.encodeLeftCount, srcOffset);
				encry_data(this.encodeLeftByte, result, 0, 0, this.enKey, tempBuf);
				numFullGroups--;
				desOffset += GROUP_SIZE;
				this.encodeLeftCount = 0;
			}

			for (int i = 0; i < numFullGroups; i++)
			{
				encry_data(buf, result, srcOffset, desOffset, this.enKey, tempBuf);
				srcOffset += GROUP_SIZE;
				desOffset += GROUP_SIZE;
			}
			if (numBytesLeft != 0)
			{
				System.arraycopy(buf, srcOffset, this.encodeLeftByte,
						this.encodeLeftCount, numBytesLeft - this.encodeLeftCount);
				this.encodeLeftCount = numBytesLeft;
				if (over)
				{
					Arrays.fill(this.encodeLeftByte,
							this.encodeLeftCount, ENCODE_BYTES_SIZE, (byte) 0);
					encry_data(this.encodeLeftByte, result, 0, desOffset,
							this.enKey, tempBuf);
					desOffset += GROUP_SIZE;
					result[desOffset++] = (byte) this.encodeLeftCount;
					this.encodeLeftCount = 0;
				}
			}
			else
			{
				if (over)
				{
					result[desOffset++] = (byte) GROUP_SIZE;
				}
				this.encodeLeftCount = 0;
			}
		}
		if (over)
		{
			this.encodeLeftCount = 0;
		}
		return result;
	}

	public byte[] decode(byte[] buf, boolean over)
	{
		int aLen = buf.length + this.decodeLeftCount;
		byte[] result = null;
		if (aLen < DECODE_BYTES_SIZE)
		{
			if (buf.length != 0)
			{
				System.arraycopy(buf, 0, this.decodeLeftByte,
						this.decodeLeftCount, buf.length);
				this.decodeLeftCount += buf.length;
			}
			result = EMPTY_BYTE_ARRAY;
		}
		else
		{
			int resultLen;
			int numFullGroups;
			int lastGroupSize = 0;
			int[] tempBuf = new int[2];
			byte[] tempBytes = null;
			if (over)
			{
				tempBytes = new byte[GROUP_SIZE];
				if (aLen % GROUP_SIZE != 1)
				{
					numFullGroups = aLen / GROUP_SIZE;
					resultLen = (numFullGroups) * GROUP_SIZE;
					lastGroupSize = GROUP_SIZE;
				}
				else
				{
					if (aLen > 8)
					{
						numFullGroups = aLen / GROUP_SIZE - 1;
						lastGroupSize = buf.length > 0 ? buf[buf.length - 1] :
								this.decodeLeftByte[this.decodeLeftCount - 1];
						resultLen = (numFullGroups) * GROUP_SIZE
								+ (aLen > 0 ? lastGroupSize : 0);
					}
					else
					{
						numFullGroups = 0;
						lastGroupSize = 0;
						resultLen = 0;
					}
				}
			}
			else
			{
				numFullGroups = (aLen - 1) / GROUP_SIZE;
				resultLen = numFullGroups * GROUP_SIZE;
			}
			result = new byte[resultLen];
			int numBytesLeft = aLen - GROUP_SIZE * numFullGroups;
			int desOffset = 0;
			int srcOffset = 0;

			// 处理前面留下来的一些字节
			if (this.decodeLeftCount > 0 && numFullGroups >= 1)
			{
				if (this.decodeLeftCount > GROUP_SIZE)
				{
					decry_data(this.decodeLeftByte, result, 0, 0, this.enKey, tempBuf);
					srcOffset += GROUP_SIZE;
					numFullGroups--;
					this.decodeLeftByte[0] = this.decodeLeftByte[GROUP_SIZE + 1];
					this.decodeLeftCount = 1;
				}
				if (numFullGroups >= 1)
				{
					desOffset = GROUP_SIZE - this.decodeLeftCount;
					System.arraycopy(buf, 0, this.decodeLeftByte,
							this.decodeLeftCount, desOffset);
					decry_data(this.decodeLeftByte, result, 0, srcOffset, this.enKey, tempBuf);
					numFullGroups--;
					srcOffset += GROUP_SIZE;
					this.decodeLeftCount = 0;
				}
			}

			for (int i = 0; i < numFullGroups; i++)
			{
				decry_data(buf, result, desOffset, srcOffset, this.enKey, tempBuf);
				desOffset += GROUP_SIZE;
				srcOffset += GROUP_SIZE;
			}
			if (this.decodeLeftCount < numBytesLeft)
			{
				System.arraycopy(buf, desOffset, this.decodeLeftByte,
						this.decodeLeftCount, numBytesLeft - this.decodeLeftCount);
				this.decodeLeftCount = numBytesLeft;
			}
			if (over && this.decodeLeftCount == DECODE_BYTES_SIZE)
			{
				decry_data(this.decodeLeftByte, tempBytes, 0, 0, this.enKey, tempBuf);
				System.arraycopy(tempBytes, 0, result, srcOffset, lastGroupSize);
				this.decodeLeftCount = 0;
			}
		}
		if (over)
		{
			this.decodeLeftCount = 0;
		}
		return result;
	}

	/**
	 * 32轮加密操作. <p>
	 * 明文的字节流和加密后的字节流的个数都为8.
	 *
	 * @param src         明文字节流
	 * @param des         加密后的密文字节流
	 * @param srcOffset   本次解密的明文字节流起始位置
	 * @param desOffset   本次解密的密文字节流起始位置
	 * @param key         密钥-长度为8
	 * @param lrBuf       用于存储中间变量的缓存-长度为2
	 */
	private static void encry_data(byte[] src, byte[] des, int srcOffset,
			int desOffset, int[] key, int[] lrBuf)
	{
		dataToBuf(src, srcOffset, lrBuf);
		for (int i = 0; i < 32; i++)
		{
			lrBuf[1] ^= f_32_11(lrBuf[0] + key[WZ_SPKEY[i]]);
			gost_swap(lrBuf);   // 左右值交换
		}
		gost_swap(lrBuf);   // 左右值交换
		bufToData(lrBuf, des, desOffset);
	}

	/**
	 * 32轮解密操作. <p>
	 * 密文的字节流和解密后的字节流的个数都为8.
	 *
	 * @param des         密文字节流
	 * @param src         解密后的明文字节流
	 * @param desOffset   本次解密的密文字节流起始位置
	 * @param srcOffset   本次解密的明文字节流起始位置
	 * @param key         密钥-长度为8
	 * @param lrBuf       用于存储中间变量的缓存-长度为2
	 */
	private static void decry_data(byte[] des, byte[] src, int desOffset,
			int srcOffset, int[] key, int[] lrBuf)
	{
		dataToBuf(des, desOffset, lrBuf);
		for (int i = 0; i < 32; i++)
		{
			lrBuf[1] ^= f_32_11(lrBuf[0] + key[WZ_SPKEY[31 - i]]);
			gost_swap(lrBuf);   // 左右值交换
		}
		gost_swap(lrBuf);   // 左右值交换
		bufToData(lrBuf, src, srcOffset);
	}

	/**
	 * s-盒替换、循环左移11位操作
	 */
	private static int f_32_11(int x)
	{
		x = (WZ_SP[7][(x >>> 28) & 0xf] << 28) | (WZ_SP[6][(x >>> 24) & 0xf] << 24)
				| (WZ_SP[5][(x >>> 20) & 0xf] << 20) | (WZ_SP[4][(x >>> 16) & 0xf] << 16)
				| (WZ_SP[3][(x >>> 12) & 0xf] << 12) | (WZ_SP[2][(x >>> 8) & 0xf] << 8)
				| (WZ_SP[1][(x >>> 4) & 0xf] << 4) | WZ_SP[0][x & 0xf];
		return x << 11 | x >>> 21;
	}

	/**
	 * 左右值交换. <p>
	 * 即数组第一个值和第二个值交换.
	 */
	private static void gost_swap(int[] buf)
	{
		int tempbuf;
		tempbuf = buf[1];
		buf[1] = buf[0];
		buf[0] = tempbuf;
	}

	/**
	 * 将从offset开始的8个data的值转换到2个buf中.
	 */
	private static void dataToBuf(byte[] data, int offset, int[] buf)
	{
		buf[0] = (data[offset] & 0xff) | ((data[offset + 1] & 0xff) << 8)
				| ((data[offset + 2] & 0xff) << 16) | ((data[offset + 3] & 0xff) << 24);
		buf[1] = (data[offset + 4] & 0xff) | ((data[offset + 5] & 0xff) << 8)
				| ((data[offset + 6] & 0xff) << 16) | ((data[offset + 7] & 0xff) << 24);
	}

	/**
	 * 将2个buf中的值转换到offset开始的8个data中.
	 */
	private static void bufToData(int[] buf, byte[] data, int offset)
	{
		data[offset] = (byte) (buf[0] & 0xff);
		data[offset + 1] = (byte) ((buf[0] >>> 8) & 0xff);
		data[offset + 2] = (byte) ((buf[0] >>> 16) & 0xff);
		data[offset + 3] = (byte) ((buf[0] >>> 24) & 0xff);
		data[offset + 4] = (byte) (buf[1] & 0xff);
		data[offset + 5] = (byte) ((buf[1] >>> 8) & 0xff);
		data[offset + 6] = (byte) ((buf[1] >>> 16) & 0xff);
		data[offset + 7] = (byte) ((buf[1] >>> 24) & 0xff);
	}

	// Gost的s-盒: [8][16]
	private static final byte WZ_SP[][] = {
		{0x4, 0xa, 0x9, 0x2, 0xd, 0x8, 0x0, 0xe, 0x6, 0xb, 0x1, 0xc, 0x7, 0xf, 0x5, 0x3},
		{0xe, 0xb, 0x4, 0xc, 0x6, 0xd, 0xf, 0xa, 0x2, 0x3, 0x8, 0x1, 0x0, 0x7, 0x5, 0x9},
		{0x5, 0x8, 0x1, 0xd, 0xa, 0x3, 0x4, 0x2, 0xe, 0xf, 0xc, 0x7, 0x6, 0x0, 0x9, 0xb},
		{0x7, 0xd, 0xa, 0x1, 0x0, 0x8, 0x9, 0xf, 0xe, 0x4, 0x6, 0xc, 0xb, 0x2, 0x5, 0x3},
		{0x6, 0xc, 0x7, 0x1, 0x5, 0xf, 0xd, 0x8, 0x4, 0xa, 0x9, 0xe, 0x0, 0x3, 0xb, 0x2},
		{0x4, 0xb, 0xa, 0x0, 0x7, 0x2, 0x1, 0xd, 0x3, 0x6, 0x8, 0x5, 0x9, 0xc, 0xf, 0xe},
		{0xd, 0xb, 0x4, 0x1, 0x3, 0x6, 0x5, 0x9, 0x0, 0xa, 0xe, 0x7, 0xf, 0x8, 0x2, 0xc},
		{0x1, 0xf, 0xd, 0x0, 0x5, 0x7, 0xa, 0x4, 0x9, 0x2, 0x3, 0xe, 0x6, 0xb, 0x8, 0xc}
	};

	// 加密密钥使用顺序表
	private static final int WZ_SPKEY[] = {
		0, 1, 2, 3, 4, 5, 6, 7,
		0, 2, 6, 1, 3, 7, 5, 6,
		2, 3, 7, 4, 0, 1, 5, 4,
		7, 6, 5, 4, 3, 2, 1, 0
	};

}