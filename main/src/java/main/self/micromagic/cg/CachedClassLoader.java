
package self.micromagic.cg;

/**
 * 带缓存的类加载器.
 */
public interface CachedClassLoader
{
	/**
	 * 添加一个需要保存在类加载器中的缓存对象.
	 */
	void addCache(Object key, Object cache);

}
