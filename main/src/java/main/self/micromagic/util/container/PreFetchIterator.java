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

package self.micromagic.util.container;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import self.micromagic.util.ref.BooleanRef;

/**
 * 可预取的迭代器.
 *
 * @author micromagic@sina.com
 */
public class PreFetchIterator
		implements Iterator
{
	private List preFetchList;
	private Iterator itr;
	private boolean modifiable;

	/**
	 * 通过一个Iterator创建PreFetchIterator.
	 */
	public PreFetchIterator(Iterator itr)
	{
		this.itr = itr;
		this.modifiable = true;
	}

	/**
	 * 通过一个Iterator创建PreFetchIterator.
	 *
	 * @param modifiable  创建的PreFetchIterator是否可修改
	 */
	public PreFetchIterator(Iterator itr, boolean modifiable)
	{
		this.itr = itr;
		this.modifiable = modifiable;
	}

	/**
	 * 预取迭代器中的对象, 如果没有则返回null.
	 *
	 * @param index    要预取之后的第几个对象, 1为第一个 2为第二个
	 * @param hasValue 出参, 表示是否存在预取的对象
	 */
	public Object prefetch(int index, BooleanRef hasValue)
	{
		if (hasValue != null)
		{
			hasValue.value = true;
		}
		if (this.preFetchList != null && this.preFetchList.size() >= index)
		{
			return this.preFetchList.get(index - 1);
		}
		if (this.preFetchList == null)
		{
			this.preFetchList = new LinkedList();
		}
		for (int i = this.preFetchList.size(); i < index; i++)
		{
			if (this.itr.hasNext())
			{
				this.preFetchList.add(this.itr.next());
			}
			else
			{
				if (hasValue != null)
				{
					hasValue.value = false;
				}
				return null;
			}
		}
		return this.preFetchList.get(index - 1);
	}

	public boolean hasNext()
	{
		if (this.preFetchList != null && this.preFetchList.size() > 0)
		{
			return true;
		}
		return this.itr.hasNext();
	}

	public Object next()
	{
		if (this.preFetchList != null && this.preFetchList.size() > 0)
		{
			return this.preFetchList.remove(0);
		}
		return this.itr.next();
	}

	public void remove()
	{
		if (this.modifiable && (this.preFetchList == null || this.preFetchList.size() == 0))
		{
			this.itr.remove();
		}
		else
		{
			throw new UnsupportedOperationException();
		}
	}

}

