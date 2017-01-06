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

import java.io.IOException;
import java.io.Serializable;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.AbstractCollection;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import self.micromagic.util.Utility;

/**
 * 一个键值基于相同引用来判断的Map, 并且键值使用了弱引用. <p>
 * 大部分代码参考SynHashMap来编写的, 因为键值使用弱引用, 无法按IdentityHashMap来编写.
 * 另外, 这样在多线程环境下也能使用这个Map.
 */
public class WeakIdentityMap extends AbstractMap
		implements Map, Cloneable, Serializable
{
	private static final long serialVersionUID = 1L;

	/**
	 * 默认的初始容量 - 必须是2的乘方.
	 */
	public static final int DEFAULT_INITIAL_CAPACITY = 16;

	/**
	 * 最大容量.
	 */
	protected static final int MAXIMUM_CAPACITY = 1 << 30;

	/**
	 * 默认的负载因子.
	 */
	protected static final float DEFAULT_LOAD_FACTOR = 0.75f;

	/**
	 * hash表, 长度必须是2的乘方.
	 */
	protected transient WeakEntry[] table;

	/**
	 * 保存的内容的个数.
	 */
	protected transient int size;

	/**
	 * 下一次需要重构hash表的容量.
	 */
	protected int threshold;

	/**
	 * hash表的负载因子.
	 */
	protected final float loadFactor;

	/**
	 * SynHashMap被修改的次数.
	 */
	protected transient volatile int modCount;

	/**
	 * 用于清除的引用的队列.
	 */
	protected final ReferenceQueue queue = new ReferenceQueue();

	/**
	 * 在调用get和containsKey操作时是否要检查引用的对象是否已释放.
	 */
	protected boolean checkRefWhenGet;

	/**
	 * 创建一个空的<tt>SynHashMap</tt>.
	 *
	 * @param initialCapacity  初始的存储空间
	 * @param loadFactor       负荷因子
	 */
	public WeakIdentityMap(int initialCapacity, float loadFactor)
	{
		if (initialCapacity < 0)
		{
			throw new IllegalArgumentException("Illegal initial capacity: " + initialCapacity);
		}
		if (initialCapacity > MAXIMUM_CAPACITY)
		{
			initialCapacity = MAXIMUM_CAPACITY;
		}
		if (loadFactor <= 0 || Float.isNaN(loadFactor))
		{
			throw new IllegalArgumentException("Illegal load factor: " + loadFactor);
		}

		// Find a power of 2 >= initialCapacity
		int capacity = 1;
		while (capacity < initialCapacity)
		{
			capacity <<= 1;
		}

		this.loadFactor = loadFactor;
		threshold = (int) (capacity * loadFactor);
		table = new WeakEntry[capacity];
		init();
	}

	/**
	 * 创建一个空的<tt>SynHashMap</tt>.
	 *
	 * @param initialCapacity  初始的存储空间
	 */
	public WeakIdentityMap(int initialCapacity)
	{
		this(initialCapacity, DEFAULT_LOAD_FACTOR);
	}

	/**
	 * 创建一个空的<tt>SynHashMap</tt>.
	 */
	public WeakIdentityMap()
	{
		this.loadFactor = DEFAULT_LOAD_FACTOR;
		threshold = (int) (DEFAULT_INITIAL_CAPACITY * DEFAULT_LOAD_FACTOR);
		table = new WeakEntry[DEFAULT_INITIAL_CAPACITY];
		init();
	}

	/**
	 * 通过一个现有的<tt>Map</tt>创建一个新的<tt>SynHashMap</tt>.
	 */
	public WeakIdentityMap(Map m)
	{
		this(Math.max((int) (m.size() / DEFAULT_LOAD_FACTOR) + 1, DEFAULT_INITIAL_CAPACITY),
				DEFAULT_LOAD_FACTOR);
		putAllForCreate(m);
	}

	/**
	 * 在调用get和containsKey操作时是否要检查引用的对象是否已释放.
	 */
	public boolean isCheckRefWhenGet()
	{
		return this.checkRefWhenGet;
	}

	/**
	 * 设置在调用get和containsKey操作时是否要检查引用的对象是否已释放. <p>
	 * 因为引用的释放大部分情况是应用的重新加载, 所以之后都会伴随put操作,
	 * 因此, get操作时就可以不用检查引用的释放, 这样能够加快get的效率.
	 */
	public void setCheckRefWhenGet(boolean checkRefWhenGet)
	{
		this.checkRefWhenGet = checkRefWhenGet;
	}

	// internal utilities

	protected void init()
	{
	}

	protected WeakEntry[] getTable()
	{
		this.expungeStaleEntries();
		return this.table;
	}

	/**
	 * 检查过期的引用.
	 */
	protected void expungeStaleEntries()
	{
		WeakEntry[] tmpTable = this.table;
		Object r;
		int hash;
		while ((r = this.queue.poll()) != null)
		{
			WeakEntry entry = ((WeakRef) r).getEntry();
			hash = entry.hash;
			int i = indexFor(hash, tmpTable.length);
			// 这里需要修改size, 所以需要加上同步锁
			synchronized (this)
			{
				WeakEntry prev = tmpTable[i];
				WeakEntry e = prev;
				while (e != null)
				{
					WeakEntry next = e.next;
					if (entry == e || entry.sameEntry(e))
					{
						if (prev == e)
						{
							tmpTable[i] = next;
						}
						else
						{
							prev.next = next;
						}
						this.size--;
						break;
					}
					prev = e;
					e = next;
				}
			}
		}
	}

	protected static int hash(Object obj)
	{
		int h = System.identityHashCode(obj);
		h ^= (h >>> 20) ^ (h >>> 12);
		return h ^ (h >>> 7) ^ (h >>> 4);
	}

	protected static int indexFor(int h, int length)
	{
		return h & (length - 1);
	}

	/**
	 * 放回当前map的容量.
	 */
	public int size()
	{
		if (this.size == 0)
		{
			return this.size;
		}
		this.expungeStaleEntries();
		return this.size;
	}

	/**
	 * 返回当前map是否为空.
	 */
	public boolean isEmpty()
	{
		return this.size() == 0;
	}

	/**
	 * 根据key获取对应的值.
	 */
	public Object get(Object key)
	{
		if (key == null)
		{
			return null;
		}
		int hash = hash(key);
		WeakEntry[] tmpTable = this.checkRefWhenGet ? this.getTable() : this.table;
		for (WeakEntry e = tmpTable[indexFor(hash, tmpTable.length)]; e != null; e = e.next)
		{
			if (e.hash == hash && e.refKey.get() == key)
			{
				return e.value;
			}
		}
		return null;
	}

	/**
	 * 判断是否包含指定的可以值.
	 */
	public boolean containsKey(Object key)
	{
		return this.getEntry(key) != null;
	}

	/**
	 * 根据key获取Entry.
	 */
	protected WeakEntry getEntry(Object key)
	{
		if (key == null)
		{
			return null;
		}
		int hash = hash(key);
		WeakEntry[] tmpTable = this.checkRefWhenGet ? this.getTable() : this.table;
		for (WeakEntry e = tmpTable[indexFor(hash, tmpTable.length)]; e != null; e = e.next)
		{
			if (e.hash == hash && e.refKey.get() == key)
			{
				return e;
			}
		}
		return null;
	}

	/**
	 * 添加一个key value对.
	 */
	public synchronized Object put(Object key, Object value)
	{
		if (key == null)
		{
			throw new NullPointerException("The parameter [key] can be null.");
		}
		int hash = hash(key);
		WeakEntry[] tmpTable = this.getTable();
		int i = indexFor(hash, tmpTable.length);
		for (WeakEntry e = tmpTable[i]; e != null; e = e.next)
		{
			if (e.hash == hash && e.getKey() == key)
			{
				Object oldValue = e.value;
				e.value = value;
				return oldValue;
			}
		}
		modCount++;
		this.addEntry(hash, key, value, i);
		return null;
	}

	/**
	 * 用于内部使用的添加key value对.
	 */
	protected final void putForCreate(Object key, Object value)
	{
		if (key == null)
		{
			throw new NullPointerException("The parameter [key] can be null.");
		}
		int hash = hash(key);
		WeakEntry[] tmpTable = this.table;
		int i = indexFor(hash, tmpTable.length);
		for (WeakEntry e = tmpTable[i]; e != null; e = e.next)
		{
			if (e.hash == hash && e.getKey() == key)
			{
				e.value = value;
				return;
			}
		}
		this.createEntry(hash, key, value, i);
	}

	/**
	 * 用于内部使用, 添加map中的key value对.
	 */
	protected synchronized void putAllForCreate(Map m)
	{
		for (Iterator i = m.entrySet().iterator(); i.hasNext();)
		{
			Map.Entry e = (Map.Entry) i.next();
			this.putForCreate(e.getKey(), e.getValue());
		}
	}

	/**
	 * 根据新的容量, 重新构造hash表.
	 */
	protected synchronized void resize(int newCapacity)
	{
		WeakEntry[] oldTable = this.table;
		int oldCapacity = oldTable.length;
		// 如果新容量没有现在的大, 则直接退出
		if (newCapacity <= oldCapacity)
		{
			return;
		}
		if (oldCapacity == MAXIMUM_CAPACITY)
		{
			this.threshold = Integer.MAX_VALUE;
			return;
		}
		WeakEntry[] newTable = new WeakEntry[newCapacity];
		this.transfer(newTable);
		this.threshold = (int) (newCapacity * this.loadFactor);
		this.table = newTable;
	}

	/**
	 * 将所有的Entry移到新的hash表中.
	 */
	protected final void transfer(WeakEntry[] newTable)
	{
		WeakEntry[] src = this.table;
		int newCapacity = newTable.length;
		for (int j = 0; j < src.length; j++)
		{
			WeakEntry e = src[j];
			if (e != null)
			{
				do
				{
					e = e.copy();
					WeakEntry next = e.next;
					int i = indexFor(e.hash, newCapacity);
					e.next = newTable[i];
					newTable[i] = e;
					e = next;
				} while (e != null);
			}
		}
	}

	/**
	 * 添加map中的key value对.
	 */
	public void putAll(Map m)
	{
		int numKeysToBeAdded = m.size();
		if (numKeysToBeAdded == 0)
		{
			return;
		}
		/*
		 * 计算新的容量
		 */
		if (numKeysToBeAdded > threshold)
		{
			int targetCapacity = (int) (numKeysToBeAdded / loadFactor + 1);
			if (targetCapacity > MAXIMUM_CAPACITY)
			{
				targetCapacity = MAXIMUM_CAPACITY;
			}
			int newCapacity = this.table.length;
			while (newCapacity < targetCapacity)
			{
				newCapacity <<= 1;
			}
			if (newCapacity > this.table.length)
			{
				this.resize(newCapacity);
			}
		}
		for (Iterator i = m.entrySet().iterator(); i.hasNext();)
		{
			Map.Entry e = (Map.Entry) i.next();
			this.put(e.getKey(), e.getValue());
		}
	}

	/**
	 * 根据key移除一个value.
	 */
	public Object remove(Object key)
	{
		WeakEntry e = this.removeEntryForKey(key);
		return (e == null ? null : e.value);
	}

	/**
	 * 根据key移除一个Entry, 并将其返回.
	 */
	protected synchronized WeakEntry removeEntryForKey(Object key)
	{
		if (key == null)
		{
			return null;
		}
		int hash = hash(key);
		WeakEntry[] tmpTable = this.getTable();
		int i = indexFor(hash, tmpTable.length);
		WeakEntry prev = tmpTable[i];
		WeakEntry e = prev;
		while (e != null)
		{
			WeakEntry next = e.next;
			if (e.hash == hash && e.getKey() == key)
			{
				this.modCount++;
				this.size--;
				if (prev == e)
				{
					tmpTable[i] = next;
				}
				else
				{
					prev.next = next;
				}
				return e;
			}
			prev = e;
			e = next;
		}
		return e;
	}

	/**
	 * 给EntrySet使用的移除.
	 */
	protected synchronized WeakEntry removeMapping(Object o)
	{
		if (!(o instanceof Map.Entry))
		{
			return null;
		}

		Map.Entry entry = (Map.Entry) o;
		Object key = entry.getKey();
		if (key == null)
		{
			return null;
		}
		int hash = hash(key);
		WeakEntry[] tmpTable = this.getTable();
		int i = indexFor(hash, tmpTable.length);
		WeakEntry prev = tmpTable[i];
		WeakEntry e = prev;
		while (e != null)
		{
			WeakEntry next = e.next;
			if (e.hash == hash && e.equals(entry))
			{
				this.modCount++;
				this.size--;
				if (prev == e)
				{
					tmpTable[i] = next;
				}
				else
				{
					prev.next = next;
				}
				return e;
			}
			prev = e;
			e = next;
		}
		return e;
	}

	/**
	 * 移除所有的内容.
	 */
	public synchronized void clear()
	{
		this.modCount++;
		Map.Entry[] tab = this.table;
		for (int i = 0; i < tab.length; i++)
		{
			tab[i] = null;
		}
		this.size = 0;
		// 移除队列中所有失去引用的对象
		while (this.queue.poll() != null);
	}

	/**
	 * 是否包含某个value值.
	 */
	public boolean containsValue(Object value)
	{
		if (value == null)
		{
			return containsNullValue();
		}
		WeakEntry[] tmpTable = this.getTable();
		for (int i = 0; i < tmpTable.length; i++)
		{
			for (WeakEntry e = tmpTable[i]; e != null; e = e.next)
			{
				if (value.equals(e.value))
				{
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * 判断是否包含value为null.
	 */
	protected boolean containsNullValue()
	{
		WeakEntry[] tmpTable = this.getTable();
		for (int i = 0; i < tmpTable.length; i++)
		{
			for (WeakEntry e = tmpTable[i]; e != null; e = e.next)
			{
				if (e.value == null)
				{
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * clone此map.
	 */
	public Object clone()
	{
		WeakIdentityMap result = null;
		try
		{
			result = (WeakIdentityMap) super.clone();
		}
		catch (CloneNotSupportedException e)
		{
			// assert false;
		}
		WeakEntry[] tmpTable = this.getTable();
		result.table = new WeakEntry[tmpTable.length];
		result.entrySet = null;
		result.keySet = null;
		result.values = null;
		result.modCount = 0;
		result.size = 0;
		result.init();
		result.putAllForCreate(this);
		return result;
	}

	private static class WeakRef extends WeakReference
	{
		protected WeakEntry entry;

		public WeakRef(Object r, WeakEntry entry, ReferenceQueue q)
		{
			super(r, q);
			this.entry = entry;
		}

		public WeakEntry getEntry()
		{
			return this.entry;
		}

	}

	protected static class WeakEntry implements Map.Entry
	{
		protected final WeakRef refKey;
		protected Object value;
		protected WeakEntry next;
		protected final int hash;

		/**
		 * Creates new entry.
		 */
		protected WeakEntry(int h, Object k, Object v, WeakEntry n, ReferenceQueue q)
		{
			this.refKey = new WeakRef(k, this, q);
			this.value = v;
			this.next = n;
			this.hash = h;
		}

		private WeakEntry(int h, WeakRef refKey)
		{
			this.hash = h;
			this.refKey = refKey;
		}

		public Object getKey()
		{
			return this.refKey.get();
		}

		public Object getValue()
		{
			return this.value;
		}

		public Object setValue(Object newValue)
		{
			Object oldValue = this.value;
			this.value = newValue;
			return oldValue;
		}

		public boolean sameEntry(WeakEntry e)
		{
			return this.refKey == e.refKey && this.hash == e.hash;
		}

		public boolean equals(Object o)
		{
			if (!(o instanceof Map.Entry))
			{
				return false;
			}
			Map.Entry e = (Map.Entry) o;
			return this.getKey() == e.getKey() && Utility.objectEquals(this.getValue(), e.getValue());
		}

		public final int hashCode()
		{
			return this.hash;
		}

		public final String toString()
		{
			return this.getKey() + "=" + this.getValue();
		}

		protected WeakEntry copy()
		{
			WeakEntry result = new WeakEntry(this.hash, this.refKey);
			result.value = this.value;
			result.next = this.next;
			return result;
		}

	}

	/**
	 * 添加一个Entry.
	 */
	protected final void addEntry(int hash, Object key, Object value, int bucketIndex)
	{
		WeakEntry e = this.table[bucketIndex];
		this.table[bucketIndex] = new WeakEntry(hash, key, value, e, this.queue);
		if (this.size++ >= this.threshold)
		{
			this.resize(2 * this.table.length);
		}
	}

	/**
	 * 创建一个Entry, 在初始化 反序列化 clone时使用.
	 */
	protected final void createEntry(int hash, Object key, Object value, int bucketIndex)
	{
		WeakEntry e = this.table[bucketIndex];
		this.table[bucketIndex] = new WeakEntry(hash, key, value, e, this.queue);
		this.size++;
	}

	protected static class EntryIterator implements Iterator
	{
		protected WeakEntry next; // next entry to return
		protected int expectedModCount; // For fast-fail
		protected int index; // current slot
		protected WeakEntry current; // current entry
		protected WeakIdentityMap map; // map

		protected EntryIterator(WeakIdentityMap map)
		{
			this.map = map;
			this.expectedModCount = this.map.modCount;
			WeakEntry[] t = this.map.getTable();
			if (this.map.size > 0)
			{
				// advance to first entry
				while (this.index < t.length && (this.next = t[this.index++]) == null);
			}
		}

		public boolean hasNext()
		{
			return this.next != null;
		}

		protected WeakEntry nextEntry()
		{
			if (this.map.modCount != this.expectedModCount)
			{
				throw new ConcurrentModificationException();
			}
			WeakEntry e = this.next;
			if (e == null)
			{
				throw new NoSuchElementException();
			}

			if ((this.next = e.next) == null)
			{
				WeakEntry[] t = this.map.getTable();
				while (this.index < t.length && (this.next = t[this.index++]) == null);
			}
			this.current = e;
			return e;
		}

		public Object next()
		{
			return this.nextEntry();
		}

		public void remove()
		{
			if (this.current == null)
			{
				throw new IllegalStateException();
			}
			if (this.map.modCount != expectedModCount)
			{
				throw new ConcurrentModificationException();
			}
			Object k = this.current.getKey();
			this.current = null;
			this.map.removeEntryForKey(k);
			this.expectedModCount = this.map.modCount;
		}

	}

	protected static class ValueIterator extends EntryIterator
	{
		public ValueIterator(WeakIdentityMap map)
		{
			super(map);
		}

		public Object next()
		{
			return this.nextEntry().value;
		}

	}

	protected static class KeyIterator extends EntryIterator
	{
		public KeyIterator(WeakIdentityMap map)
		{
			super(map);
		}

		public Object next()
		{
			return this.nextEntry().getKey();
		}

	}

	// Subclass overrides these to alter behavior of views' iterator() method
	protected Iterator newKeyIterator()
	{
		return new KeyIterator(this);
	}

	protected Iterator newValueIterator()
	{
		return new ValueIterator(this);
	}

	protected Iterator newEntryIterator()
	{
		return new EntryIterator(this);
	}

	// Views

	protected transient Set entrySet = null;
	protected transient Set keySet = null;
	protected transient Collection values = null;

	/**
	 * 获取key的集合.
	 */
	public Set keySet()
	{
		this.expungeStaleEntries();
		Set ks = this.keySet;
		return (ks != null ? ks : (this.keySet = new KeySet(this)));
	}

	protected static class KeySet extends AbstractSet
	{
		protected WeakIdentityMap map; // map

		public KeySet(WeakIdentityMap map)
		{
			this.map = map;
		}

		public Iterator iterator()
		{
			return this.map.newKeyIterator();
		}

		public int size()
		{
			return this.map.size();
		}

		public boolean contains(Object o)
		{
			return this.map.containsKey(o);
		}

		public boolean remove(Object o)
		{
			return this.map.removeEntryForKey(o) != null;
		}

		public void clear()
		{
			this.map.clear();
		}

	}

	/**
	 * 获取value的集合.
	 */
	public Collection values()
	{
		this.expungeStaleEntries();
		Collection vs = this.values;
		return (vs != null ? vs : (this.values = new Values(this)));
	}

	protected static class Values extends AbstractCollection
	{
		protected WeakIdentityMap map; // map

		public Values(WeakIdentityMap map)
		{
			this.map = map;
		}

		public Iterator iterator()
		{
			return this.map.newValueIterator();
		}

		public int size()
		{
			return this.map.size();
		}

		public boolean contains(Object o)
		{
			return this.map.containsValue(o);
		}

		public void clear()
		{
			this.map.clear();
		}

	}

	/**
	 * 获取Entry集合.
	 */
	public Set entrySet()
	{
		this.expungeStaleEntries();
		Set es = this.entrySet;
		return es != null ? es : (this.entrySet = new EntrySet(this));
	}

	protected static class EntrySet extends AbstractSet
	{
		protected WeakIdentityMap map; // map

		public EntrySet(WeakIdentityMap map)
		{
			this.map = map;
		}

		public Iterator iterator()
		{
			return this.map.newEntryIterator();
		}

		public boolean contains(Object o)
		{
			if (!(o instanceof Map.Entry))
			{
				return false;
			}
			Map.Entry e = (Map.Entry) o;
			Map.Entry candidate = this.map.getEntry(e.getKey());
			return candidate != null && candidate.equals(e);
		}

		public boolean remove(Object o)
		{
			return this.map.removeMapping(o) != null;
		}

		public int size()
		{
			return this.map.size();
		}

		public void clear()
		{
			this.map.clear();
		}

	}

	/**
	 * 将此对象序列化.
	 */
	private synchronized void writeObject(java.io.ObjectOutputStream s) throws IOException
	{
		this.expungeStaleEntries();
		Iterator i = (this.size > 0) ? this.entrySet().iterator() : null;
		// 输出一些默认的值
		s.defaultWriteObject();
		// 输出hash表的大小
		s.writeInt(this.table.length);
		// 输出当前的容量
		s.writeInt(this.size);
		// 输出每个Entry
		if (i != null)
		{
			while (i.hasNext())
			{
				Map.Entry e = (Map.Entry) i.next();
				s.writeObject(e.getKey());
				s.writeObject(e.getValue());
			}
		}
	}

	/**
	 * 将此对象反序列化.
	 */
	private synchronized void readObject(java.io.ObjectInputStream s) throws IOException, ClassNotFoundException
	{
		// 读入默认值
		s.defaultReadObject();
		// 读入hash表的大小
		int numBuckets = s.readInt();
		this.table = new WeakEntry[numBuckets];
		init(); // 执行初始化
		// 读入容量
		int size = s.readInt();
		// 设置每个Entry
		for (int i = 0; i < size; i++)
		{
			Object key = s.readObject();
			Object value = s.readObject();
			this.putForCreate(key, value);
		}
	}

}
