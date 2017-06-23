
package self.micromagic.util.container;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Iterator;

/**
 * 双缓冲列表. <p>
 * 这种列表适合多生产者单消费者的模式.
 * 生产者向列表中添加数据时会加锁, 消费者是通过交换的方式直接获取一个列表,
 * 在列表交换时会加锁, 对列表迭代时不会加锁.
 *
 * @param <T>  列表中元素的类型
 */
public class DoubleBufferList<T>
{
	/**
	 * 用于接收对象的列表.
	 */
	private Collection<T> receiverList;
	/**
	 * 用于导出给消费者的列表.
	 */
	private Collection<T> exporterList;
	/**
	 * 需要触发的监听者.
	 */
	private Listener listener;
	/**
	 * 是否要触发监听的动作.
	 */
	private boolean needFire;

	/**
	 * 构造一个双缓冲列表.
	 */
	public DoubleBufferList()
	{
		this(32, true, null);
	}

	/**
	 * 构造一个双缓冲列表.
	 *
	 * @param initSize   列表初始化的空间
	 * @param listener   需要触发的监听者
	 */
	public DoubleBufferList(int initSize, Listener listener)
	{
		this(initSize, true, listener);
	}

	/**
	 * 构造一个双缓冲列表.
	 *
	 * @param initSize   列表初始化的空间
	 * @param checkSize  是否需要检查列表的使用空间
	 * @param listener   需要触发的监听者
	 */
	public DoubleBufferList(int initSize, boolean checkSize, Listener listener)
	{
		this.exporterList = new OneBufferList<T>(initSize, checkSize);
		this.receiverList = new OneBufferList<T>(initSize, checkSize);
		this.listener = listener;
		this.needFire = listener != null;
	}

	/**
	 * 向列表中添加一个对象.
	 *
	 * @param obj  需要添加的对象
	 */
	public void add(T obj)
	{
		boolean fireFlag = false;
		synchronized (this)
		{
			if (this.listener != null && this.receiverList.isEmpty()
					&& this.listener.needFire(this))
			{
				this.needFire = false;
				fireFlag = true;
			}
			this.receiverList.add(obj);
		}
		if (fireFlag)
		{
			this.fireAction();
		}
	}

	/**
	 * 向列表中添加一批对象.
	 *
	 * @param objs  需要添加的一批对象
	 */
	public void addAll(Collection<T> objs)
	{
		if (objs == null || objs.isEmpty())
		{
			return;
		}
		boolean fireFlag = false;
		synchronized (this)
		{
			if (this.listener != null && this.receiverList.isEmpty()
					&& this.listener.needFire(this))
			{
				this.needFire = false;
				fireFlag = true;
			}
			this.receiverList.addAll(objs);
		}
		if (fireFlag)
		{
			this.fireAction();
		}
	}

	/**
	 * 触发监听的动作.
	 */
	private void fireAction()
	{
		try
		{
			this.listener.action(this);
		}
		catch (RuntimeException ex)
		{
			/// 如果监听动作触发出错, 需要重置触发标识
			this.needFire = true;
			throw ex;
		}
	}

	/**
	 * 获取已添加的对象列表. <p>
	 * 此方法应该由消费者调用, 返回的是一个只读列表.
	 *
	 * @return  给消费者的列表, 如果没有任何对象, 则返回null
	 */
	public Collection<T> getObjects()
	{
		synchronized (this)
		{
			if (this.receiverList.isEmpty())
			{
				if (this.listener != null)
				{
					this.listener.afterEmpty(this);
					this.needFire = true;
				}
				return null;
			}
			Collection<T> tmp = this.exporterList;
			this.exporterList = this.receiverList;
			tmp.clear();
			this.receiverList = tmp;
			return this.exporterList;
		}
	}

	/**
	 * 是否需要触发监听的动作.
	 */
	public boolean needFireAction()
	{
		return this.needFire;
	}

	/**
	 * 添加时如果队列为空时, 需要触发的监听者.
	 */
	public interface Listener
	{
		/**
		 * 是否需要触发监听的动作. <p>
		 * 如果返回为true, 则会立刻出发监听动作.
		 * 此方法是在list的同步锁中被调用的.
		 * 默认可以直接返回list.needFireAction()的值.
		 *
		 * @param list  对应的双缓冲列表
		 *
		 * @see DoubleBufferList#needFireAction()
		 */
		public boolean needFire(DoubleBufferList<?> list);

		/**
		 * 执行监听的动作. <p>
		 * 调用此方法时, 是执行了needFire(DoubleBufferList)方法之后,
		 * 此时list.needFireAction()的值应为false.
		 *
		 * @param list  触发监听动作的双缓冲列表
		 *
		 * @see #needFire(DoubleBufferList)
		 * @see DoubleBufferList#needFireAction()
		 */
		public void action(DoubleBufferList<?> list);

		/**
		 * 获取的数据列表为空时触发的动作. <p>
		 * 此方法是在list的同步锁中被调用的.
		 * 在此方法执行之后list.needFireAction()的值应为true.
		 *
		 * @param list  获取数据的双缓冲列表
		 * @see DoubleBufferList#needFireAction()
		 */
		public void afterEmpty(DoubleBufferList<?> list);

	}

}

/**
 * 单个缓冲列表.
 *
 * @param <T>  列表中元素的类型
 */
class OneBufferList<T>
		implements Collection<T>
{
	/**
	 * 是否需要检查使用的列表空间.
	 */
	private final boolean needCheckSize;
	/**
	 * 连续出现最大使用空间一半的次数.
	 */
	private int halfCount;
	/**
	 * 存储对象的数组.
	 */
	private Object[] objs;
	/**
	 * 以存放对象的数量.
	 */
	private int count;

	public OneBufferList(int initSize, boolean checkSize)
	{
		this.needCheckSize = checkSize;
		this.objs = new Object[initSize <= 0 ? 10 : initSize];
	}

	public boolean add(T o)
	{
		this.ensureCapacity(this.count + 1);
		this.objs[this.count++] = o;
		return true;
	}

	public boolean addAll(Collection<? extends T> c)
	{
		Object[] a = c.toArray();
		int numNew = a.length;
		this.ensureCapacity(this.size() + numNew);
		System.arraycopy(a, 0, this.objs, this.count, numNew);
		this.count += numNew;
		return numNew != 0;
	}

	public void ensureCapacity(int minCapacity)
	{
		int oldCapacity = this.objs.length;
		if (minCapacity > oldCapacity)
		{
			Object oldData[] = this.objs;
			int newCapacity = oldCapacity + (oldCapacity >> 1) + 1;
			if (newCapacity < minCapacity)
			{
				newCapacity = minCapacity;
			}
			this.objs = new Object[newCapacity];
			System.arraycopy(oldData, 0, this.objs, 0, this.count);
		}
	}

	public int size()
	{
		return this.count;
	}

	public boolean isEmpty()
	{
		return this.count == 0;
	}

	public Object[] toArray()
	{
		Object[] arr = new Object[this.count];
		System.arraycopy(this.objs, 0, arr, 0, this.count);
		return arr;
	}

	@SuppressWarnings("unchecked")
	public <E> E[] toArray(E[] a)
	{
		if (a.length < this.count)
		{
			a = (E[]) Array.newInstance(a.getClass().getComponentType(), this.count);
		}
		System.arraycopy(this.objs, 0, a, 0, this.count);
		if (a.length > this.count)
		{
			a[this.count] = null;
		}
		return a;
	}

	public Iterator<T> iterator()
	{
		return new Itr();
	}

	public void clear()
	{
		if (this.needCheckSize && this.objs.length > 128)
		{
			if (this.count < (this.objs.length >> 1))
			{
				if (++this.halfCount > 5)
				{
					this.halfCount = 0;
					int newSize = this.objs.length >> 1;
					this.objs = new Object[newSize];
				}
			}
			else
			{
				this.halfCount = 0;
			}
		}
		this.count = 0;
	}

	@SuppressWarnings({"unchecked"})
	public T get(int index)
	{
		if (index >= this.count)
		{
			throw new IndexOutOfBoundsException(
					"index: " + index + ", count: " + this.count);
		}
		return (T) this.objs[index];
	}

	public boolean contains(Object o)
	{
		throw new UnsupportedOperationException();
	}

	public boolean containsAll(Collection<?> c)
	{
		throw new UnsupportedOperationException();
	}

	public boolean retainAll(Collection<?> c)
	{
		throw new UnsupportedOperationException();
	}

	public boolean removeAll(Collection<?> c)
	{
		throw new UnsupportedOperationException();
	}

	public boolean remove(Object o)
	{
		throw new UnsupportedOperationException();
	}

	private class Itr implements Iterator<T>
	{
		private int cursor = 0;

		public boolean hasNext()
		{
			return this.cursor < OneBufferList.this.count;
		}

		public T next()
		{
			return OneBufferList.this.get(this.cursor++);
		}

		public void remove()
		{
			throw new UnsupportedOperationException();
		}

	}

}
