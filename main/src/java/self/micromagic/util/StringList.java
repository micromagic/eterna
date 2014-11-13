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

package self.micromagic.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

class StringList
{
	private List strList = new ArrayList();

	public synchronized StringList append(char str[], int offset, int len)
	{
		this.strList.add(new String(str, offset, len));
		return this;
	}

	public synchronized String get(int index)
	{
		return (String) this.strList.get(index);
	}

	public synchronized String toString()
	{
		StringAppender temp = StringTool.createStringAppender(this.strList.size() * 16);
		Iterator itr = this.strList.iterator();
		while (itr.hasNext())
		{
			temp.append((String) itr.next());
		}
		return temp.toString();
	}

}