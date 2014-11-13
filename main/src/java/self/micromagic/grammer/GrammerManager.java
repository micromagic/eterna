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

import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import self.micromagic.util.Utility;
import self.micromagic.util.Utils;

public class GrammerManager
{
	public static String ROOT_ELEMENT = "grammer-config";
	public static String NODE_ELEMENT = "node";
	public static String GROUP_ELEMENT = "group";
	public static String LIST_ELEMENT = "list";
	public static String QUEUE_ELEMENT = "queue";

	public static int id = 1;

	protected static final Log log = Utility.createLog("eterna.grammer");

	private Map grammerElementMap = new HashMap();

	public void init(InputStream is)
			throws GrammerException
	{
		try
		{
			SAXReader reader = new SAXReader();
			Document doc = reader.read(is);
			Element root = doc.getRootElement();
			if (!ROOT_ELEMENT.equals(root.getName()))
			{
				throw new GrammerException("Not found the root element [" + ROOT_ELEMENT + "].");
			}
			Iterator itr = root.elementIterator();
			while (itr.hasNext())
			{
				Element el = (Element) itr.next();
				GrammerElement ge = this.getElement(el, true);
				if (this.grammerElementMap.put(ge.getName(), ge) != null)
				{
					throw new GrammerException("Duplicated name:" + ge.getName() + ".");
				}
			}
			itr = this.grammerElementMap.values().iterator();
			while (itr.hasNext())
			{
				GrammerElement ge = (GrammerElement) itr.next();
				ge.initialize(this.grammerElementMap);
			}
		}
		catch (DocumentException ex)
		{
			log.error("Error in init GrammerManager.", ex);
			throw new GrammerException(ex);
		}
	}

	public GrammerElement getGrammerElement(String name)
	{
		return (GrammerElement) this.grammerElementMap.get(name);
	}

	protected GrammerElement getElement(Element el, boolean needName)
			throws GrammerException
	{
		String name = el.getName();
		String attrName = el.attributeValue("name");
		if (needName)
		{
			if (attrName == null)
			{
				throw new GrammerException("Not found the name attribute in the element:[" + el.toString() + "].");
			}
		}
		else if (attrName == null)
		{
			synchronized (GrammerManager.class)
			{
				attrName = "grammer_" + Integer.toString(id++, 32);
			}
		}
		AbstractElement ae = null;
		if (NODE_ELEMENT.equals(name))
		{
			ae = this.initNode(el);
		}
		else if (GROUP_ELEMENT.equals(name))
		{
			ae = this.initGroup(el);
		}
		else if (LIST_ELEMENT.equals(name))
		{
			ae = this.initList(el);
		}
		else if (QUEUE_ELEMENT.equals(name))
		{
			ae = this.initQueue(el);
		}
		if (ae != null)
		{
			ae.setName(attrName);
			String type = el.attributeValue("type");
			ae.setType(parseGrammerElementType(type));
			String not = el.attributeValue("not");
			if (not != null)
			{
				ae.setNot("true".equals(not));
			}
			return ae;
		}
		throw new GrammerException("Error element name [" + name + "].");
	}

	protected GrammerList initList(Element el)
			throws GrammerException
	{
		GrammerList list = new GrammerList();
		Iterator itr = el.elementIterator("list-cell");
		while (itr.hasNext())
		{
			Element tmp = (Element) itr.next();
			String refName = tmp.attributeValue("refName");
			String opt = tmp.attributeValue("opt");
			char optC = ' ';
			if (opt != null)
			{
				if (opt.length() != 1)
				{
					throw new GrammerException("Error GrammerList cell opt:" + opt + ":[" + el.toString() + "].");
				}
				optC = opt.charAt(0);
				if (optC != GrammerList.LIST_TYPE_ONE && optC != GrammerList.LIST_TYPE_ONE_P
						&& optC != GrammerList.LIST_TYPE_MORE && optC != GrammerList.LIST_TYPE_MORE_P)
				{
					throw new GrammerException("Error GrammerList cell opt:" + opt + ":[" + el.toString() + "].");
				}
			}
			boolean hasRange = false;
			String min = tmp.attributeValue("min");
			String max = tmp.attributeValue("max");
			int minI = -1;
			int maxI = -1;
			if (min != null || max != null)
			{
				hasRange = true;
				if (min != null)
				{
					minI = Utils.parseInt(min, -1);
				}
				if (max != null)
				{
					maxI = Utils.parseInt(max, -1);
				}
			}
			if (refName != null)
			{
				if (hasRange)
				{
					list.addElement(refName, minI, maxI);
				}
				else
				{
					list.addElement(refName, optC);
				}
			}
			else
			{
				List subs = tmp.elements();
				if (subs.size() != 1)
				{
					throw new GrammerException("Only one element can in list-cell:[" + el.toString() + "].");
				}
				Element sub = (Element) subs.get(0);
				if (hasRange)
				{
					list.addElement(this.getElement(sub, false), minI, maxI);
				}
				else
				{
					list.addElement(this.getElement(sub, false), optC);
				}
			}
		}
		return list;
	}

	protected GrammerGroup initGroup(Element el)
			throws GrammerException
	{
		GrammerGroup group = new GrammerGroup();
		Iterator itr = el.elementIterator("group-cell");
		while (itr.hasNext())
		{
			Element tmp = (Element) itr.next();
			String refName = tmp.attributeValue("refName");
			if (refName != null)
			{
				group.addElement(refName);
			}
			else
			{
				List subs = tmp.elements();
				if (subs.size() != 1)
				{
					throw new GrammerException("Only one element can in group-cell:[" + el.toString() + "].");
				}
				Element sub = (Element) subs.get(0);
				group.addElement(this.getElement(sub, false));
			}
		}
		return group;
	}

	protected GrammerQueue initQueue(Element el)
			throws GrammerException
	{
		GrammerQueue queue = new GrammerQueue();
		String attrTmp = el.attributeValue("chars");
		if (attrTmp != null)
		{
			queue.setQueue(attrTmp);
		}
		return queue;
	}

	protected GrammerNode initNode(Element el)
			throws GrammerException
	{
		GrammerNode node = new GrammerNode();
		String attrTmp = el.attributeValue("other");
		if (attrTmp != null)
		{
			node.setOtherCharType("true".equals(attrTmp));
		}
		attrTmp = el.attributeValue("end");
		if (attrTmp != null)
		{
			node.setEndType("true".equals(attrTmp));
		}
		Iterator itr = el.elementIterator();
		while (itr.hasNext())
		{
			Element tmp = (Element) itr.next();
			if ("true-set".equals(tmp.getName()))
			{
				node.addTrueChecker(this.initSetChecker(tmp));
			}
			else if ("false-set".equals(tmp.getName()))
			{
				node.addFalseChecker(this.initSetChecker(tmp));
			}
			else if ("true-range".equals(tmp.getName()))
			{
				node.addTrueChecker(this.initRangeChecker(tmp));
			}
			else if ("false-range".equals(tmp.getName()))
			{
				node.addFalseChecker(this.initRangeChecker(tmp));
			}
		}
		return node;
	}

	protected OneChecker initSetChecker(Element el)
			throws GrammerException
	{
		String chars = el.attributeValue("chars");
		if (chars == null)
		{
			throw new GrammerException("Not found chars attribute in the element:[" + el.toString() + "].");
		}
		return new OneChecker.SetChecker(chars);
	}

	protected OneChecker initRangeChecker(Element el)
			throws GrammerException
	{
		String begin = el.attributeValue("begin");
		if (begin == null)
		{
			throw new GrammerException("Not found begin attribute in the element:[" + el.toString() + "].");
		}
		if (begin.length() != 1)
		{
			throw new GrammerException("The begin attribute must be one char in the element:[" + el.toString() + "].");
		}
		String end = el.attributeValue("end");
		if (end == null)
		{
			throw new GrammerException("Not found end attribute in the element:[" + el.toString() + "].");
		}
		if (end.length() != 1)
		{
			throw new GrammerException("The end attribute must be one char in the element:[" + el.toString() + "].");
		}
		return new OneChecker.RangeChecker(begin.charAt(0), end.charAt(0));
	}

	public static String getGrammerElementTypeName(int type)
	{
		return GrammerElement.TYPE_NAMES[type];
	}

	public static int parseGrammerElementType(String type)
			throws GrammerException
	{
		if (type == null || type.length() == 0)
		{
			return GrammerElement.TYPE_NONE;
		}
		for (int i = 0; i < GrammerElement.TYPE_NAMES.length; i++)
		{
			String typeName = GrammerElement.TYPE_NAMES[i];
			if (typeName.equalsIgnoreCase(type))
			{
				return i;
			}
		}
		throw new GrammerException("Error GrammerElement type:" + type + ".");
	}

}