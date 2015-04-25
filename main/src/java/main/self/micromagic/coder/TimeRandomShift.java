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

import java.util.Arrays;

import self.micromagic.util.ref.IntegerRef;

public class TimeRandomShift extends AbstractCoder
		implements Coder
{
	private static final long SEED_MASK = (1L << 56) - 1;
	private static final int GROUP_SIZE = 8;
	private static final int HEAD_SIZE = 16;
	private static final int KEY_SIZE = HEAD_SIZE;

	private int infoLocal = 12;
	private byte[] enKey = new byte[KEY_SIZE];
	private byte[] tempBuf = new byte[GROUP_SIZE];

	private byte[] encodeHead = new byte[HEAD_SIZE];
	private Random encodeRandom = null;
	private byte[] encodeLeftByte = new byte[GROUP_SIZE];
	private int encodeLeftCount = 0;

	private byte[] decodeHead = new byte[HEAD_SIZE];
	private Random decodeRandom = null;
	private byte[] decodeLeftByte = new byte[HEAD_SIZE];
	private int decodeLeftCount = 0;

	public TimeRandomShift(byte[] key)
	{
		long tempSeed = 2006L;
		for (int i = 0; i < key.length; i++)
		{
			tempSeed ^= ((long) (key[i] & 0xff)) << ((i % 12) * 5);
		}
		Random r = new Random(tempSeed);
		r.nextBytes(this.enKey);
	}

	public TimeRandomShift(byte[] key, int infoLocal)
	{
		this(key);
		this.infoLocal = infoLocal & 0xf;
	}

	private TimeRandomShift(TimeRandomShift src)
	{
		this.enKey = src.enKey;
		this.infoLocal = src.infoLocal;
	}

	public Coder createNew()
	{
		return new TimeRandomShift(this);
	}

	public void clear()
	{
		this.encodeLeftCount = 0;
		this.encodeRandom = null;
		this.decodeLeftCount = 0;
		this.decodeRandom = null;
	}

	public byte[] decode(byte[] des)
	{
		if (des.length < 16)
		{
			throw new IllegalArgumentException("The byte array length at least be 16.");
		}
		return this.decode(des, true);
	}

	public byte[] encode(byte[] buf, boolean over)
	{
		int aLen = buf.length + this.encodeLeftCount;
		int numFullGroups = aLen / GROUP_SIZE;
		int numBytesLeft = aLen - GROUP_SIZE * numFullGroups;
		int resultLen = !over ? GROUP_SIZE * numFullGroups
				: (this.encodeRandom == null && aLen < GROUP_SIZE ? GROUP_SIZE : aLen);
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
			int srcOffset = 0;
			int desOffset = 0;
			if (this.encodeRandom == null)
			{
				// 对head进行编码
				resultLen += GROUP_SIZE;
				result = new byte[resultLen];
				int realCount = GROUP_SIZE;
				if (aLen < GROUP_SIZE)
				{
					if (buf.length > 0)
					{
						System.arraycopy(buf, 0, this.encodeLeftByte,
								this.encodeLeftCount, buf.length);
						this.encodeLeftCount += buf.length;
						srcOffset += buf.length;
						Arrays.fill(this.encodeLeftByte, this.encodeLeftCount, GROUP_SIZE, (byte) 0);
					}
					// 如果在初始化head时, 长度不足8, 则说明是字节流结束了
					numBytesLeft = 0;
					realCount = aLen;
				}
				else
				{
					srcOffset = GROUP_SIZE - this.encodeLeftCount;
					System.arraycopy(buf, 0, this.encodeLeftByte,
							this.encodeLeftCount, srcOffset);
				}
				this.en_head(this.encodeLeftByte, result, realCount);
				desOffset += HEAD_SIZE;
				this.encodeLeftCount = 0;
				numFullGroups--;
			}
			else
			{
				result = new byte[resultLen];
			}

			IntegerRef ref = new IntegerRef(this.encodeLeftCount);
			this.f_datas(buf, srcOffset, result, desOffset, this.encodeLeftByte, ref,
					true, over, numFullGroups, numBytesLeft);
			this.encodeLeftCount = ref.value;
		}
		if (over)
		{
			this.encodeLeftCount = 0;
			this.encodeRandom = null;
		}
		return result;
	}

	public byte[] decode(byte[] buf, boolean over)
	{
		int aLen = buf.length + this.decodeLeftCount;
		byte[] result = null;
		if ((!over && aLen < GROUP_SIZE)
				|| (this.decodeRandom == null && aLen < HEAD_SIZE))
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
			int numFullGroups = aLen / GROUP_SIZE;
			int numBytesLeft = aLen - GROUP_SIZE * numFullGroups;
			int resultLen = over ? aLen : GROUP_SIZE * numFullGroups;
			int desOffset = 0;
			int srcOffset = 0;
			if (this.decodeRandom == null)
			{
				resultLen -= GROUP_SIZE;
				result = new byte[resultLen];
				// 对head进行解码
				if (this.decodeLeftCount < HEAD_SIZE)
				{
					desOffset = HEAD_SIZE - this.decodeLeftCount;
					System.arraycopy(buf, 0, this.decodeLeftByte,
							this.decodeLeftCount, desOffset);
				}
				int realCount = this.de_head(this.decodeLeftByte, result);
				if (realCount < GROUP_SIZE)
				{
					// 如果在初始化head时, 返回的长度不足8, 则说明是原字节不足8位
					byte[] temp = new byte[realCount];
					System.arraycopy(result, 0, temp, 0, realCount);
					result = temp;
				}
				srcOffset += realCount;
				this.decodeLeftCount = 0;
				numFullGroups -= 2;;
			}
			else
			{
				result = new byte[resultLen];
			}

			IntegerRef ref = new IntegerRef(this.decodeLeftCount);
			this.f_datas(buf, desOffset, result, srcOffset, this.decodeLeftByte, ref,
					false, over, numFullGroups, numBytesLeft);
			this.decodeLeftCount = ref.value;
		}
		if (over)
		{
			this.decodeLeftCount = 0;
			this.decodeRandom = null;
		}
		return result;
	}

	private void f_datas(byte[] buf, int bufOffset, byte[] result, int rOffset,
			byte[] codeBuf, IntegerRef leftCount, boolean en_or_dn, boolean over,
			int numFullGroups, int numBytesLeft)
	{
		int leftNum = leftCount.value;
		// 处理前面留下来的一些字节
		if (leftNum > 0)
		{
			if (numFullGroups >= 1)
			{
				bufOffset = GROUP_SIZE - leftNum;
				System.arraycopy(buf, 0, codeBuf, leftNum, bufOffset);
				this.f_data(codeBuf, 0, result, rOffset, en_or_dn, GROUP_SIZE);
				numFullGroups--;
				rOffset += GROUP_SIZE;
				leftNum = 0;
			}
		}

		for (int i = 0; i < numFullGroups; i++)
		{
			this.f_data(buf, bufOffset, result, rOffset, en_or_dn, GROUP_SIZE);
			bufOffset += GROUP_SIZE;
			rOffset += GROUP_SIZE;
		}
		if (numBytesLeft != 0)
		{
			System.arraycopy(buf, bufOffset, codeBuf, leftNum, numBytesLeft - leftNum);
			leftNum = numBytesLeft;
			if (over)
			{
				this.f_data(codeBuf, 0, result, rOffset, en_or_dn, numBytesLeft);
				rOffset += numBytesLeft;
			}
		}
		leftCount.value = leftNum;
	}

	/**
	 * 对一组8字节以内长的数据进行编码或解码操作.
	 * 如果长度不足8字节. 则说明是最后一组.
	 */
	private void f_data(byte[] src, int srcOffset, byte[] des, int desOffset,
			boolean en_or_dn, int count)
	{
		Random r = en_or_dn ? this.encodeRandom : this.decodeRandom;
		byte[] head = en_or_dn ? this.encodeHead : this.decodeHead;
		boolean swap = r.nextInt(2) != 0;
		int xorIndex = r.nextInt(HEAD_SIZE);
		int keyIndex = r.nextInt(KEY_SIZE);
		int tkey = r.nextInt(); // 生成移位用的密钥
		if (count == GROUP_SIZE)
		{
			if (!en_or_dn)   // 如果是解码, 则先进行异或
			{
				System.arraycopy(src, srcOffset, this.tempBuf, 0, GROUP_SIZE);
				this.f_xor(this.tempBuf, 0, GROUP_SIZE, en_or_dn, head, xorIndex, keyIndex);
				src = this.tempBuf;
				srcOffset = 0;
			}
			if (swap)
			{
				f_32_13(src, srcOffset, des, desOffset + 4, tkey, en_or_dn);
				f_32_13(src, srcOffset + 4, des, desOffset, tkey, en_or_dn);
			}
			else
			{
				f_32_13(src, srcOffset, des, desOffset, tkey, en_or_dn);
				f_32_13(src, srcOffset + 4, des, desOffset + 4, tkey, en_or_dn);
			}
			if (en_or_dn)   // 如果是编码, 则后进行异或
			{
				this.f_xor(des, desOffset, GROUP_SIZE, en_or_dn, head, xorIndex, keyIndex);
			}
		}
		else if (count >= 4)
		{
			if (!en_or_dn)   // 如果是解码, 则先进行异或
			{
				System.arraycopy(src, srcOffset, this.tempBuf, 0, 4);
				this.f_xor(this.tempBuf, 0, 4, en_or_dn, head, xorIndex, keyIndex);
				if (swap)
				{
					this.tempBuf[5] = this.tempBuf[1];
					this.tempBuf[1] = this.tempBuf[2];
					this.tempBuf[2] = this.tempBuf[5];
				}
				f_32_13(this.tempBuf, 0, des, desOffset, tkey, en_or_dn);
			}
			else
			{
				f_32_13(src, srcOffset, des, desOffset, tkey, en_or_dn);
				if (swap)
				{
					this.tempBuf[0] = des[desOffset + 1];
					des[desOffset + 1] = des[desOffset + 2];
					des[desOffset + 2] = this.tempBuf[0];
				}
				this.f_xor(des, desOffset, 4, en_or_dn, head, xorIndex, keyIndex);
			}
			if (count > 4)
			{
				this.f_data(src, srcOffset + 4, des, desOffset + 4, en_or_dn, count - 4);
			}
		}
		else
		{
			System.arraycopy(src, srcOffset, des, desOffset, count);
			if (!en_or_dn)
			{
				this.f_xor(des, desOffset, count, en_or_dn, head, xorIndex, keyIndex);
			}
			if (swap && count > 1)
			{
				this.tempBuf[0] = des[desOffset];
				des[desOffset] = des[desOffset + 1];
				des[desOffset + 1] = this.tempBuf[0];
			}
			if (en_or_dn)
			{
				this.f_xor(des, desOffset, count, en_or_dn, head, xorIndex, keyIndex);
			}
		}
	}

	private void f_xor(byte buf[], int off, int count, boolean en_or_de,
			byte[] head, int headIndex, int keyIndex)
	{
		byte tempb = 0;
		for (int i = 0; i < count; i++)
		{
			if (!en_or_de)
			{
				tempb = buf[off + i];
			}
			buf[off + i] ^= head[headIndex++] ^ this.enKey[keyIndex++];
			if (headIndex == HEAD_SIZE)
			{
				headIndex = 0;
			}
			if (keyIndex == KEY_SIZE)
			{
				keyIndex = 0;
			}
			if (en_or_de)
			{
				tempb = buf[off + i];
			}
			head[headIndex++] = tempb;
			if (headIndex == HEAD_SIZE)
			{
				headIndex = 0;
			}
		}
	}

	/**
	 * 必须保证输入字节流src的长度不少于8, 输出字节流des的长度不小于16.
	 */
	private void en_head(byte[] src, byte[] des, int realCount)
	{
		// 产生random的seed, 只使用56位作为seed
		long seed = System.currentTimeMillis();
		long tempL = ((long) (this.enKey[14] & 0xff)) << 49;
		for (int i = 0; i < GROUP_SIZE; i++)
		{
			tempL ^= (((long) (src[i] & 0xff)) << (i * 8 + 3))
					| (((long) (this.enKey[i * 2 + 1] & 0xff)) << (i * 5));
		}
		seed = (seed ^ tempL) & SEED_MASK;
		// 将seed存入后7个字节, 第一个字节存第一组的字节数和使用密钥的起始位置等
		byte[] tempArr = new byte[GROUP_SIZE];
		for (int i = 0; i < 7; i++)
		{
			tempArr[i + 1] = (byte) ((seed >>> (i * 8)) & 0xff);
		}
		//System.out.println("seed en:" + seed + " " + Long.toHexString(seed));
		Random r = new Random(seed);
		this.encodeRandom = r;
		boolean srcFirst = r.nextInt(2) != 0;
		int keyIndex = r.nextInt(8);
		tempArr[0] = (byte) (realCount | (keyIndex << 4) | (srcFirst ? 0x80 : 0));
		int tkey = r.nextInt(); // 生成移位用的密钥
		for (int i = 4; i < 7; i++)
		{
			tkey ^= (this.enKey[i * 2] & 0xff) << ((i - 4) * 8 + 7);
		}
		// 先进行编码, 编码后再与密钥异或, 最后复制到des字节流中
		f_32_13(src, 4, this.encodeHead, 0, tkey, true);
		if (srcFirst)
		{
			f_32_13(src, 0, this.encodeHead, 4, tkey, true);
			System.arraycopy(tempArr, 4, this.encodeHead, 8, 4);
		}
		else
		{
			System.arraycopy(tempArr, 4, this.encodeHead, 4, 4);
			f_32_13(src, 0, this.encodeHead, 8, tkey, true);
		}
		System.arraycopy(tempArr, 0, this.encodeHead, 12, 4);
		for (int i = 0; i < HEAD_SIZE; i++)
		{
			if (i != 12)
			{
				this.encodeHead[i] ^= this.enKey[keyIndex++];
			}
			if (keyIndex == KEY_SIZE)
			{
				keyIndex = 0;
			}
		}
		// 加密信息字节
		this.encodeHead[12] ^= this.enKey[2] ^ this.enKey[4] ^ this.enKey[6]
				^ this.enKey[8] ^ this.encodeHead[1] ^ this.encodeHead[3];
		//System.out.print("head en:");
		//Tester.printByte(this.encodeHead);

		if (this.infoLocal != 12)
		{
			// 信息字节位置不在12, 进行交换
			byte tmp = this.encodeHead[12];
			this.encodeHead[12] = this.encodeHead[this.infoLocal];
			this.encodeHead[this.infoLocal] = tmp;
		}
		System.arraycopy(this.encodeHead, 0, des, 0, HEAD_SIZE);
	}

	/**
	 * 必须保证输入字节流des的长度不少于16, 输出字节流src的长度不小于8.
	 *
	 * @return    实际有效的字节数
	 */
	private int de_head(byte[] des, byte[] src)
	{
		System.arraycopy(des, 0, this.decodeHead, 0, HEAD_SIZE);
		if (this.infoLocal != 12)
		{
			// 信息字节位置不在12, 将其换回12
			byte tmp = this.decodeHead[12];
			this.decodeHead[12] = this.decodeHead[this.infoLocal];
			this.decodeHead[this.infoLocal] = tmp;
		}

		//System.out.print("head de:");
		//Tester.printByte(this.decodeHead);
		// 从第12个字节中提取信息
		byte info = (byte) (this.decodeHead[12] ^ this.enKey[2] ^ this.enKey[4] ^ this.enKey[6]
				^ this.enKey[8] ^ this.decodeHead[1] ^ this.decodeHead[3]);
		//System.out.println("info:" + Integer.toHexString(info & 0xff));
		boolean srcFirst = info < 0;  // 小于0表示字节的第一位为1
		int keyIndex = (info >>> 4) & 0x7;
		int realCount = info & 0xf;
		// 进行异或解码, 并从解码的数据中获取random的seed
		byte[] tempArr = new byte[HEAD_SIZE];
		for (int i = 0; i < HEAD_SIZE; i++)
		{
			if (i != 12)
			{
				tempArr[i] = (byte) (this.decodeHead[i] ^ this.enKey[keyIndex++]);
			}
			if (keyIndex == KEY_SIZE)
			{
				keyIndex = 0;
			}
		}
		long seed = 0L;
		for (int i = 0; i < 3; i++)
		{
			seed |= (tempArr[13 + i] & 0xff) << (i * 8);
		}
		int start = (srcFirst ? 8 : 4) - 3;
		for (int i = 3; i < 7; i++)
		{
			seed |= ((long) (tempArr[start + i] & 0xff)) << (i * 8);
		}
		//System.out.println("seed de:" + seed + " " + Long.toHexString(seed));
		// 根据seed生成random, 并对数据区进行解码, 放入src字节流中
		Random r = new Random(seed);
		this.decodeRandom = r;
		r.nextInt(2);  // 应该等于 srcFirst (!= 0)
		//System.out.println(r.nextInt(2) + " " + srcFirst);
		r.nextInt(8);  // 应该等于 keyIndex
		//System.out.println(r.nextInt(8) + " " + ((info >>> 4) & 0x7));
		int tkey = r.nextInt(); // 生成移位用的密钥
		for (int i = 4; i < 7; i++)
		{
			tkey ^= (this.enKey[i * 2] & 0xff) << ((i - 4) * 8 + 7);
		}
		f_32_13(tempArr, 0, src, 4, tkey, false);
		if (srcFirst)
		{
			f_32_13(tempArr, 4, src, 0, tkey, false);
		}
		else
		{
			f_32_13(tempArr, 8, src, 0, tkey, false);
		}

		if (this.infoLocal != 12)
		{
			// 信息字节位置不在12, 将head变回交换后的样子
			byte tmp = this.decodeHead[12];
			this.decodeHead[12] = this.decodeHead[this.infoLocal];
			this.decodeHead[this.infoLocal] = tmp;
		}
		return realCount;
	}

	/**
	 * 将src中从srcOffset开始的4个字节的值左移或右移13位,
	 * 放入des中的desOffset处.
	 */
	private static void f_32_13(byte[] src, int srcOffset, byte[] des, int desOffset,
			int randomKey, boolean turnLeft)
	{
		int x = (src[srcOffset] & 0xff) | ((src[srcOffset + 1] & 0xff) << 8)
				| ((src[srcOffset + 2] & 0xff) << 16) | ((src[srcOffset + 3] & 0xff) << 24);
		if (turnLeft)
		{
			x = x << 13 | x >>> 19;
			x ^= randomKey;
		}
		else
		{
			x ^= randomKey;
			x = x << 19 | x >>> 13;
		}
		des[desOffset] = (byte) (x & 0xff);
		des[desOffset + 1] = (byte) ((x >>> 8) & 0xff);
		des[desOffset + 2] = (byte) ((x >>> 16) & 0xff);
		des[desOffset + 3] = (byte) ((x >>> 24) & 0xff);
	}

}