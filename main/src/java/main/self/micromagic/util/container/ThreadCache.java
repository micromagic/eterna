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

import java.util.HashMap;
import java.util.Map;

/**
 * 管理线程中缓存的属性.
 */
public class ThreadCache
{
	/**
	 * 存放<code>ThreadCache</code>实例的<code>ThreadLocal</code>的实现.
	 *
	 * 新版本中取消了这种实现方式.
	 * 因为在应用服务器会使用线程缓冲池, 这样线程是不会被释放的, 那缓存在线程
	 * 中的(子ClassLoader载入)对象也不会被释放. 当应用被重新加载的时候, 原来
	 * 的应用就无法被释放, 因为新的应用产生了新的ThreadLocal, 不会覆盖原来的,
	 * 从而造成内存的泄漏.
	 */
	//private static final ThreadLocal localCache = new ThreadLocalCache();

	/**
	 * 保存所有创建的线程缓存.
	 */
	private static final Map threadCaches = new SynHashMap(32, SynHashMap.WEAK);


	private final Map propertys;

	private ThreadCache()
	{
		this.propertys = new HashMap();
	}

	/**
	 * 获得一个<code>ThreadCache</code>的实例.
	 */
	public static ThreadCache getInstance()
	{
		/*
		ThreadCache threadCache = (ThreadCache) localCache.get();
		if (threadCache == null)
		{
			threadCache = new ThreadCache();
			localCache.set(threadCache);
		}
		*/
		Thread t = Thread.currentThread();
		ThreadCache threadCache = (ThreadCache) threadCaches.get(t);
		if (threadCache == null)
		{
			// 这里是以当前线程作为主键, 所以可以直接创建并放入到threadCaches
			// 因为其它线程中不会产生相同的主键
			threadCache = new ThreadCache();
			threadCaches.put(t, threadCache);
			/*
			synchronized (threadCaches)
			{
				// 在同步的环境下再判断是否存在, 不存在的话再生成
				threadCache = (ThreadCache) threadCaches.get(t);
				if (threadCache == null)
				{
					threadCache = new ThreadCache();
					threadCaches.put(t, threadCache);
				}
			}
			*/
		}
		return threadCache;
	}

	/**
	 * 向当前的线程缓存设置一个属性.
	 *
	 * @param name        要设置的属性的名称
	 * @param property    要设置的属性值
	 */
	public void setProperty(String name, Object property)
	{
		this.propertys.put(name, property);
	}

	/**
	 * 获取当前的线程缓存中一个属性的值.
	 *
	 * @param name        要获取的属性的名称
	 */
	public Object getProperty(String name)
	{
		return this.propertys.get(name);
	}

	/**
	 * 移除当前的线程缓存中的一个属性.
	 *
	 * @param name        要移除的属性的名称
	 */
	public void removeProperty(String name)
	{
		this.propertys.remove(name);
	}

	/**
	 * 获得缓存的对象数.
	 */
	public int size()
	{
		return this.propertys.size();
	}

	/**
	 * 清空当前线程缓存中的属性值.
	 */
	public void clearPropertys()
	{
		this.propertys.clear();
	}

	/**
	 * 清空所有线程缓存中的属性值.
	 */
	public static void clearAllPropertys()
	{
		threadCaches.clear();
	}

	/*
	取消了ThreadLocal的方式获取线程缓存
	private static class ThreadLocalCache extends ThreadLocal
	{
		protected Object initialValue()
		{
			return new ThreadCache();
		}

	}
	*/

}