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
import java.util.Enumeration;

import org.apache.commons.collections.iterators.EmptyIterator;
import org.apache.commons.collections.iterators.IteratorEnumeration;

/**
 * @author micromagic@sina.com
 */
public class UnmodifiableIterator
		implements Iterator
{
	public static final Iterator EMPTY_ITERATOR = EmptyIterator.INSTANCE;
	public static final Enumeration EMPTY_ENUMERATION = new IteratorEnumeration(EmptyIterator.INSTANCE);

	private Iterator itr;

	public UnmodifiableIterator(Iterator itr)
	{
		this.itr = itr == null ? EMPTY_ITERATOR : itr;
	}

	public boolean hasNext()
	{
		return this.itr.hasNext();
	}

	public Object next()
	{
		return this.itr.next();
	}

	public void remove()
	{
		throw new UnsupportedOperationException();
	}

}