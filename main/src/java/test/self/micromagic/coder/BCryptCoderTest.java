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

import junit.framework.TestCase;

public class BCryptCoderTest extends TestCase
{
	public void testHash()
	{
		String h1 = BCryptCoder.hashPassword("test0001", BCryptCoder.generateSalt());
		assertEquals("pwd1", true, BCryptCoder.checkPassword("test0001", h1));
		String h2 = BCryptCoder.hashPassword("test0002", BCryptCoder.generateSalt(12));
		assertEquals("pwd2", true, BCryptCoder.checkPassword("test0002", h2));
		String h3 = BCryptCoder.hashPassword("test0003", BCryptCoder.generateSalt(7));
		assertEquals("pwd3", true, BCryptCoder.checkPassword("test0003", h3));
		String h4 = BCryptCoder.hashPassword("test0004", BCryptCoder.generateSalt(8));
		assertEquals("pwd4", false, BCryptCoder.checkPassword("test0003", h4));
		String h5 = BCryptCoder.hashPassword("test0004", h4);
		assertEquals("pwd5", h4, h5);
		System.out.println("h1:" + h1);
		System.out.println("h2:" + h2);
		System.out.println("h3:" + h3);
		System.out.println("h4:" + h4);
	}

	public void testTime()
	{
		long begin = System.currentTimeMillis();
		BCryptCoder.hashPassword("test0001", BCryptCoder.generateSalt(11));
		long end = System.currentTimeMillis();
		long l1 = end - begin;

		begin = System.currentTimeMillis();
		BCryptCoder.hashPassword("test0002", BCryptCoder.generateSalt(12));
		end = System.currentTimeMillis();
		long l2 = end - begin;

		assertEquals("time2 > time1", true, l2 > l1);
		assertEquals("time2 > 0.5", true, l2 > 500);
		System.out.println("time1:" + l1);
		System.out.println("time2:" + l2);
	}

}