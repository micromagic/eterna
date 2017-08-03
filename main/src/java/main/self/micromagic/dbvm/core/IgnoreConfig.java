
package self.micromagic.dbvm.core;

import self.micromagic.util.container.ThreadCache;

/**
 * 忽略的配置对象.
 */
public class IgnoreConfig
{
	/**
	 * 线程缓存中放置此对象的名称.
	 */
	private static final String THREAD_CACHE_FLAG = "eterna.dbvm.ignoreConfig";

	private boolean ignoreSameKey;

	/**
	 * 设置在执行插入时是否需要忽略主键冲突的错误.
	 */
	public void setIgnoreSameKey(boolean b)
	{
		this.ignoreSameKey = b;
		getCurrentConfig().ignoreSameKey = b;
	}

	/**
	 * 执行插入时是否需要忽略主键冲突的错误.
	 */
	public boolean isIgnoreSameKey()
	{
		return getCurrentConfig().ignoreSameKey;
	}

	/**
	 * 获取当前的忽略配置对象.
	 */
	public static IgnoreConfig getCurrentConfig()
	{
		ThreadCache cache = ThreadCache.getInstance();
		IgnoreConfig tmp = (IgnoreConfig) cache.getProperty(THREAD_CACHE_FLAG);
		if (tmp == null)
		{
			tmp = new IgnoreConfig();
			cache.setProperty(THREAD_CACHE_FLAG, tmp);
		}
		return tmp;
	}

	/**
	 * 清除当前的忽略配置对象.
	 * 在版本初始化前使用.
	 */
	public static void clearCurrentConfig()
	{
		ThreadCache.getInstance().removeProperty(THREAD_CACHE_FLAG);
	}

}
