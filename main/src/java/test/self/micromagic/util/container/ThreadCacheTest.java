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

package self.micromagic.util.container;

import java.util.Map;

import junit.framework.TestCase;
import self.micromagic.util.Utility;
import tool.PrivateAccessor;

public class ThreadCacheTest extends TestCase
{
	public void testRelease()
			throws Exception
	{
		tmpValue = null;
		Map tCaches = (Map) PrivateAccessor.get(ThreadCache.class, "threadCaches");
		TmpThread tmp = new TmpThread();
		loadThreadValue();
		int nowSize = tCaches.size();
		assertEquals(null, tmpValue);
		tmp.start();
		Thread.sleep(10L);
		assertEquals(Utility.INTEGER_1, tmpValue);
		assertEquals(nowSize + 1, tCaches.size());
		tmp = null;
		Thread.sleep(1000L);
		System.gc();
		Thread.sleep(1000L);
		System.gc();
		assertEquals(nowSize, tCaches.size());
		System.getProperties().list(System.out);
		System.out.println(java.util.Arrays.asList(".\t1|.\t1\t1|.\t1\t2|".split("|")));
		System.out.println(java.util.Arrays.asList(".\t1|.\t1\t1|.\t1\t2|".split("\\|")));
	}

	static Object tmpValue;
	static void loadThreadValue()
	{
		tmpValue = ThreadCache.getInstance().getProperty("1");
	}

	static class TmpThread extends Thread
	{
		public void run()
		{
			ThreadCache cache = ThreadCache.getInstance();
			cache.setProperty("1", new Integer(1));
			loadThreadValue();
		}

	}

}

