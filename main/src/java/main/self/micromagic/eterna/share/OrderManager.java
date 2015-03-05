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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import self.micromagic.util.ref.ObjectRef;

/**
 * 容器中对象排序的管理器.
 * 可以通过排序字符串的定义对容器中的对象进行排序.
 *
 * 排序的规则是基于关系的排序, 即指定每个关系组中对象的顺序.
 * 关系组的分隔符为";", 关系组中每个对象名的分隔符为",".
 * 第一个关系组表示要排列在起始部分的对象, 最后一个关系组表示要排列在
 * 最后的对象, 中间的关系组表示第二个对象及后面的对象要按顺序排列在
 * 第一个对象出现的位置之后.
 * 第一个关系组和最后个关系组可以不设置对象名称, 如果只设置了一个关系组
 * 其将作为第一个关系组, 而没有最后一个关系组.
 *
 * 样例:
 * 三个关系组
 * n1,n2,n3;n5,n6,n7,n11,n12,n13;n21,n22
 * 第一个和最后个关系组没有设置对象名
 * ;n2,n3,n4;
 * 只有第一个关系组
 * n1,n2
 */
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

	/**
	 * 执行一个排序处理.
	 *
	 * @param list         需要排序的对象列表
	 * @param orderConfig  排序的配置
	 * @param nameHandler  获取对象名称的处理者
	 * @return  排序后的对象列表
	 */
	public static List doOrder(List list, String orderConfig, NameHandler nameHandler)
	{
		if (orderConfig == null || list == null)
		{
			return list;
		}
		LinkedList groupList = new LinkedList();
		Map nameCache = new HashMap();
		parseOrderConfig(orderConfig, groupList, nameCache);

		List objGroup = new ArrayList();
		// 处理第一个分组和最后个分组
		List lastGroup = null;
		if (groupList.size() > 0)
		{
			List firstList = (List) groupList.removeFirst();
			if (firstList.size() > 0)
			{
				ObjContainer oc = (ObjContainer) firstList.get(0);
				oc.groupList = null;
				objGroup.add(firstList);
			}
			// 处理最后一个分组
			if (groupList.size() > 0)
			{
				lastGroup = (List) groupList.removeLast();
				if (lastGroup.size() > 0)
				{
					ObjContainer oc = (ObjContainer) lastGroup.get(0);
					oc.groupList = null;
				}
			}
		}

		// 把对象分组
		List currentObjs = new ArrayList();
		objGroup.add(currentObjs);
		int count = list.size();
		Iterator itr = list.iterator();
		for (int i = 0; i < count; i++)
		{
			Object tmpObj = itr.next();
			String oName = nameHandler.getName(tmpObj);
			ObjContainer oc = (ObjContainer) nameCache.get(oName);
			if (oc != null)
			{
				oc.obj = tmpObj;
				if (oc.groupList != null)
				{
					objGroup.add(oc.groupList);
					currentObjs = new ArrayList();
					objGroup.add(currentObjs);
				}
			}
			else
			{
				currentObjs.add(tmpObj);
			}
		}
		if (lastGroup != null)
		{
			objGroup.add(lastGroup);
		}

		List result = new ArrayList(list.size());
		// 将分组完的对象重新排列
		itr = objGroup.iterator();
		while (itr.hasNext())
		{
			List objList = (List) itr.next();
			Iterator objItr = objList.iterator();
			while (objItr.hasNext())
			{
				Object tObj = objItr.next();
				if (tObj instanceof ObjContainer)
				{
					ObjContainer oc = (ObjContainer) tObj;
					if (oc.obj == null)
					{
						String msg = "The name [" + oc.name + "] not found, config["
								+ orderConfig + "].";
						throw new EternaException(msg);
					}
					result.add(oc.obj);
				}
				else
				{
					result.add(tObj);
				}
			}
		}
		return result;
	}

	/**
	 * 解析排序配置.
	 */
	private static void parseOrderConfig(String orderConfig, List groupList, Map nameCache)
	{
		StringTokenizer token = new StringTokenizer(orderConfig, ";,", true);
		int groupCount = 1;
		List currentObjs = new ArrayList();
		groupList.add(currentObjs);
		while (token.hasMoreTokens())
		{
			String str = token.nextToken();
			if (";".equals(str))
			{
				if (groupCount > 1)
				{
					if (currentObjs.size() < 2)
					{
						String msg = "Error name count [" + currentObjs.size()
								+ "] in midle group(" + groupCount + "), at least 2. config ["
								+ orderConfig + "].";
						throw new EternaException(msg);
					}
				}
				// 需要创建一个新的分组
				currentObjs = new ArrayList();
				groupList.add(currentObjs);
				groupCount++;
			}
			else if (!(",".equals(str)))
			{
				// 不是名称分隔符, 添加对象名称
				ObjContainer oc = new ObjContainer(str.trim(),
						currentObjs.size() == 0 ? currentObjs : null);
				if (nameCache.containsKey(oc.name))
				{
					String msg = "Duplicate name [" + oc.name + "] in config ["
							+ orderConfig + "].";
					throw new EternaException(msg);
				}
				nameCache.put(oc.name, oc);
				currentObjs.add(oc);
			}
		}
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

	/**
	 * 获取对象名称的处理者.
	 */
	public interface NameHandler
	{
		String getName(Object obj);

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

/**
 * 存放对象名称和对象的容器.
 */
class ObjContainer
{
	public ObjContainer(String name, List groupList)
	{
		this.name = name;
		this.groupList = groupList;
	}
	public List groupList;
	public String name;
	public Object obj;

}