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

package self.micromagic.grammer;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

public class GrammerGroup extends AbstractElement
		implements GrammerElement
{
	private List groupElements = new ArrayList();
	private boolean initialized = false;
	private boolean initOver = false;
	private boolean allSubNone = false;

	public void initialize(Map elements)
			throws GrammerException
	{
		if (!this.initialized)
		{
			this.initialized = true;
			int hasType = 0;
			Iterator itr = this.groupElements.iterator();
			while (itr.hasNext())
			{
				GrammerGroupCell cell = (GrammerGroupCell) itr.next();
				cell.initialize(elements);
				int nowType;
				try
				{
					nowType = cell.grammerElement.isTypeNone() ? -1 : 1;
				}
				catch (GrammerException ex)
				{
					// 如果出现异常, 那表示此元素被循环引用了, 默认被循环引用的元素有类型
					nowType = 1;
				}
				if (hasType == 0)
				{
					hasType = nowType;
				}
				else
				{
					if (hasType != nowType)
					{
						throw new GrammerException("In a group[" + this.getName()
								+ "], all cell type must TYPE_NONE or not TYPE_NONE." + this.groupElements);
					}
				}
			}
			this.allSubNone = hasType == 1 ? false : true;
			this.initOver = true;
		}
	}

	public boolean isTypeNone()
			throws GrammerException
	{
		if (!this.initOver)
		{
			throw new GrammerException("In a group[" + this.getName()
					+ "], hasn't initialized.");
		}
		return this.getType() == TYPE_NONE ? this.allSubNone : false;
	}

	public void addElement(String name)
	{
		this.groupElements.add(new GrammerGroupCell(name));
	}

	public void addElement(GrammerElement element)
	{
		this.groupElements.add(new GrammerGroupCell(element.getName(), element));
	}

	public boolean doVerify(ParserData pd)
			throws GrammerException
	{
		Iterator itr = this.groupElements.iterator();
		int preIndex = -1;
		GrammerGroupCell trueCell = null;
		while (itr.hasNext())
		{
			pd.addResetPoint();
			GrammerGroupCell cell = (GrammerGroupCell) itr.next();
			if (cell.grammerElement.verify(pd))
			{
				if (preIndex == -1 || preIndex < pd.getCurrentIndex())
				{
					preIndex = pd.getCurrentIndex();
					trueCell = cell;
				}
			}
			pd.reset();
		}
		if (trueCell == null)
		{
			return false;
		}
		else
		{
			trueCell.grammerElement.verify(pd);
			return true;
		}
	}

	public String toString()
	{
		return "Group:" + this.getName() + ":" + GrammerManager.getGrammerElementTypeName(this.getType());
	}

	private static class GrammerGroupCell
	{
		public final String name;
		private GrammerElement grammerElement = null;

		public GrammerGroupCell(String name)
		{
			this.name = name;
		}

		public GrammerGroupCell(String name, GrammerElement grammerElement)
		{
			this.name = name;
			this.grammerElement = grammerElement;
		}

		public void initialize(Map elements)
				throws GrammerException
		{
			if (this.grammerElement == null)
			{
				GrammerElement e = (GrammerElement) elements.get(this.name);
				if (e == null)
				{
					throw new GrammerException("Not found the GrammerElement:" + this.name + ".");
				}
				e.initialize(elements);
				this.grammerElement = e;
			}
			else
			{
				this.grammerElement.initialize(elements);
			}
		}

		public String toString()
		{
			if (this.grammerElement == null)
			{
				return this.name;
			}
			return this.grammerElement.toString();
		}

	}

}