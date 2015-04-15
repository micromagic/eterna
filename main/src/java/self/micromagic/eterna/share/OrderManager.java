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

package self.micromagic.eterna.share;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import self.micromagic.eterna.share.EternaException;
import self.micromagic.util.ObjectRef;

public class OrderManager
{
	private String containerName = "$parent";

	public OrderManager()
	{
	}

	public OrderManager(String containerName)
	{
		this.containerName = "$" + containerName;
	}

	public List getOrder(OrderItem item, Object[] containers, String orderStr,
			List srcList, Map srcMap)
			throws EternaException
	{
		ObjectRef[] tmpContainers = null;
		if (containers != null)
		{
			tmpContainers = new ObjectRef[containers.length];
		}
		else
		{
			tmpContainers = new ObjectRef[0];
		}

		HashMap tmpMap = new HashMap();           // 存放已设置的对象
		LinkedList tmpList = new LinkedList();    // 存放临时排序的对象
		LinkedList leftList = new LinkedList();   // 存放未排序的对象
		HashMap leftMap = new HashMap();          // 存放未排序的对象
		OrderItem tmpItem;
		if (orderStr != null)
		{
			leftList.addAll(srcList);
			leftMap.putAll(srcMap);
			StringTokenizer st = new StringTokenizer(orderStr, ",");
			while (st.hasMoreTokens())
			{
				String str = st.nextToken().trim();
				if (str.length() > 0 && str.charAt(0) == '$')
				{
					int index = str.lastIndexOf(':');
					if (str.startsWith(this.containerName))
					{
						int id = 0;
						if (index != -1)
						{
							id = Integer.parseInt(str.substring(index + 1)) - 1;
						}
						if (tmpContainers[id] != null)
						{
							throw new EternaException("Multi [" + containerName + ":"
									+ (id + 1) + "], the order is:[" + orderStr + "].");
						}
						tmpContainers[id] = new ObjectRef();
						tmpList.add(tmpContainers[id]);
					}
					else if (str.startsWith("$gap"))
					{
						int count = 1;
						if (index != -1)
						{
							count = Integer.parseInt(str.substring(index + 1));
						}
						tmpList.add(new Integer(count));
					}
				}
				else
				{
					tmpItem = item.create(leftMap.remove(str));
					if (tmpItem != null)
					{
						leftList.remove(tmpItem.obj);
						tmpList.add(tmpItem.obj);
						tmpMap.put(str, tmpItem.obj);
					}
					else
					{
						ObjectRef objR = new ObjectRef(str);
						tmpList.add(objR);
						tmpMap.put(str, objR);
					}
				}
			}
		}
		else
		{
			tmpList.addAll(srcList);
			tmpMap.putAll(srcMap);
		}

		for (int i = 0; i < tmpContainers.length; i++)
		{
			boolean addLeft = false;
			if (tmpContainers[i] == null)
			{
				tmpContainers[i] = new ObjectRef();
				tmpList.add(tmpContainers[i]);
				addLeft = true;
			}
			LinkedList theList = new LinkedList();
			tmpContainers[i].setObject(theList);
			Iterator itr = item.getOrderItemIterator(containers[i]);
			while (itr.hasNext())
			{
				tmpItem = item.create(itr.next());
				Object obj = tmpMap.get(tmpItem.name);
				if (obj == null)
				{
					if (leftMap.get(tmpItem.name) == null)
					{
						srcMap.put(tmpItem.name, tmpItem.obj);
						if (addLeft)
						{
							leftList.add(tmpItem.obj);
							leftMap.put(tmpItem.name, tmpItem.obj);
						}
						else
						{
							theList.add(tmpItem.obj);
							tmpMap.put(tmpItem.name, tmpItem.obj);
						}
					}
					else if (!addLeft)
					{
						tmpItem = item.create(leftMap.remove(tmpItem.name));
						leftList.remove(tmpItem.obj);
						theList.add(tmpItem.obj);
						tmpMap.put(tmpItem.name, tmpItem.obj);
					}
				}
				else
				{
					if (obj instanceof ObjectRef)
					{
						srcMap.put(tmpItem.name, tmpItem.obj);
						((ObjectRef) obj).setObject(tmpItem.obj);
						tmpMap.put(tmpItem.name, tmpItem.obj);
					}
				}
			}
		}

		LinkedList resultList = new LinkedList();
		Iterator itr = tmpList.iterator();
		while (itr.hasNext())
		{
			Object obj = itr.next();
			if (obj instanceof ObjectRef)
			{
				obj = ((ObjectRef) obj).getObject();
				if (obj instanceof List)
				{
					Iterator tmpItr = ((List) obj).iterator();
					while (tmpItr.hasNext())
					{
						tmpItem = item.create(tmpItr.next());
						this.checkIgnore(tmpItem, resultList, srcMap);
					}
				}
				else if (obj instanceof String)
				{
					EternaFactoryImpl.log.warn(
							"Error name:[" + obj + "] in order string:[" + orderStr + "].");
				}
				else
				{
					tmpItem = item.create(obj);
					this.checkIgnore(tmpItem, resultList, srcMap);
				}
			}
			else if (obj instanceof Integer)
			{
				int count = ((Integer) obj).intValue();
				for (int i = 0; i < count; i++)
				{
					if (leftList.size() > 0)
					{
						tmpItem = item.create(leftList.removeFirst());
						this.checkIgnore(tmpItem, resultList, srcMap);
					}
					else
					{
						break;
					}
				}
			}
			else
			{
				tmpItem = item.create(obj);
				this.checkIgnore(tmpItem, resultList, srcMap);
			}
		}

		itr = leftList.iterator();
		while (itr.hasNext())
		{
			tmpItem = item.create(itr.next());
			this.checkIgnore(tmpItem, resultList, srcMap);
		}

		return resultList;
	}

	private void checkIgnore(OrderItem item, List resultList, Map srcMap)
			throws EternaException
	{
		if (item.isIgnore())
		{
			srcMap.remove(item.name);
		}
		else
		{
			resultList.add(item.obj);
		}
	}

	public static abstract class OrderItem
	{
		public final String name;
		public final Object obj;

		protected OrderItem(String name, Object obj)
		{
			this.name = name;
			this.obj = obj;
		}

		public abstract boolean isIgnore()
				throws EternaException;

		public abstract OrderItem create(Object obj)
				throws EternaException;

		public abstract Iterator getOrderItemIterator(Object container)
				throws EternaException;

	}

}