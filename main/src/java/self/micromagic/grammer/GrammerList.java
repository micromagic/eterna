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

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Iterator;

public class GrammerList extends AbstractElement
		implements GrammerElement
{
	public static final char LIST_TYPE_ONE = ' ';
	public static final char LIST_TYPE_MORE = '+';
	public static final char LIST_TYPE_ONE_P = '?';
	public static final char LIST_TYPE_MORE_P = '*';

	private List listElements = new ArrayList();
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
			Iterator itr = this.listElements.iterator();
			while (itr.hasNext())
			{
				GrammerListCell cell = (GrammerListCell) itr.next();
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
						throw new GrammerException("In a list[" + this.getName()
								+ "], all cell type must TYPE_NONE or not TYPE_NONE." + this.listElements);
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
			throw new GrammerException("In a list[" + this.getName()
					+ "], hasn't initialized.");
		}
		return this.getType() == TYPE_NONE ? this.allSubNone : false;
	}

	public void addElement(String name, char opt)
	{
		this.listElements.add(new GrammerListCell(name, opt));
	}

	public void addElement(GrammerElement element, char opt)
	{
		this.listElements.add(new GrammerListCell(element.getName(), opt, element));
	}

	public void addElement(String name, int min, int max)
	{
		GrammerListCell cell = new GrammerListCell(name, LIST_TYPE_MORE_P);
		cell.setRangeCount(min, max);
		this.listElements.add(cell);
	}

	public void addElement(GrammerElement element, int min, int max)
	{
		GrammerListCell cell = new GrammerListCell(name, LIST_TYPE_MORE_P, element);
		cell.setRangeCount(min, max);
		this.listElements.add(cell);
	}

	public boolean doVerify(ParserData pd)
			throws GrammerException
	{
		Iterator itr = this.listElements.iterator();
		while (itr.hasNext())
		{
			GrammerListCell cell = (GrammerListCell) itr.next();
			int count = cell.grammerElement.verify(pd) ? 1 : 0;
			if (count == 0)
			{
				if (cell.opt == LIST_TYPE_ONE || cell.opt == LIST_TYPE_MORE)
				{
					return false;
				}
			}
			if (cell.opt == LIST_TYPE_MORE_P || cell.opt == LIST_TYPE_MORE)
			{
				if (count > 0)
				{
					int preIndex = pd.getCurrentIndex();
					while (cell.grammerElement.verify(pd))
					{
						// 当本次位置和前一次位置相同, 表示没有进展, 退出循环
						if (preIndex == pd.getCurrentIndex())
						{
							break;
						}
						preIndex = pd.getCurrentIndex();
						count++;
					}
				}
				if (!cell.checkRange(count))
				{
					return false;
				}
			}
		}
		return true;
	}

	public String toString()
	{
		return "List:" + this.getName() + ":" + GrammerManager.getGrammerElementTypeName(this.getType());
	}

	private static class GrammerListCell
	{
		public final String name;
		public final char opt;
		private GrammerElement grammerElement = null;

		private int minCount = -1;
		private int maxCount = -1;

		public GrammerListCell(String name, char opt)
		{
			this.name = name;
			this.opt = opt;
		}

		public GrammerListCell(String name, char opt, GrammerElement grammerElement)
		{
			this.name = name;
			this.opt = opt;
			this.grammerElement = grammerElement;
		}

		public void setRangeCount(int min, int max)
		{
			this.minCount = min;
			this.maxCount = max;
		}

		public boolean checkRange(int count)
		{
			if (this.minCount != -1)
			{
				if (count < this.minCount)
				{
					return false;
				}
			}
			if (this.maxCount != -1)
			{
				if (count > this.maxCount)
				{
					return false;
				}
			}
			return true;
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