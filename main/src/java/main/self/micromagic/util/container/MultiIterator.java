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

import java.util.Iterator;

import self.micromagic.util.container.UnmodifiableIterator;

public class MultiIterator
		implements Iterator
{
	private Iterator firstItr;
	private Iterator secondItr;

	private boolean readOnly;

	public MultiIterator(Iterator firstItr, Iterator secondItr)
	{
		this(firstItr, secondItr, false);
	}

	public MultiIterator(Iterator firstItr, Iterator secondItr, boolean readOnly)
	{
		this.firstItr = firstItr == null ? UnmodifiableIterator.EMPTY_ITERATOR : firstItr;
		this.secondItr = secondItr == null ? UnmodifiableIterator.EMPTY_ITERATOR : secondItr;
		this.readOnly = readOnly;
	}

	public boolean hasNext()
	{
		return this.firstItr.hasNext() ? true : this.secondItr.hasNext();
	}

	public Object next()
	{
		return this.firstItr.hasNext() ? this.firstItr.next() : this.secondItr.next();
	}

	public void remove()
	{
		if (this.readOnly)
		{
			throw new UnsupportedOperationException();
		}
		if (this.firstItr.hasNext())
		{
			this.firstItr.remove();
		}
		else
		{
			this.secondItr.remove();
		}
	}

}

