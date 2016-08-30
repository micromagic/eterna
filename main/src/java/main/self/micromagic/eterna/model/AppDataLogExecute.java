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

package self.micromagic.eterna.model;

import java.io.IOException;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.dom4j.Element;

import self.micromagic.cg.ArrayTool;
import self.micromagic.cg.BeanMethodInfo;
import self.micromagic.cg.BeanTool;
import self.micromagic.cg.ClassGenerator;
import self.micromagic.cg.ClassKeyCache;
import self.micromagic.eterna.dao.ResultIterator;
import self.micromagic.eterna.dao.ResultMetaData;
import self.micromagic.eterna.dao.ResultRow;
import self.micromagic.eterna.model.impl.AbstractExecute;
import self.micromagic.eterna.search.ConditionInfo;
import self.micromagic.eterna.search.SearchAttributes;
import self.micromagic.eterna.search.SearchManager;
import self.micromagic.eterna.search.SearchResult;
import self.micromagic.eterna.share.EternaException;
import self.micromagic.eterna.share.Generator;
import self.micromagic.eterna.share.Tool;
import self.micromagic.eterna.share.TypeManager;
import self.micromagic.util.FormatTool;
import self.micromagic.util.container.PreFetchIterator;
import self.micromagic.util.container.SessionCache;
import self.micromagic.util.container.ThreadCache;
import self.micromagic.util.ref.BooleanRef;

/**
 * @author micromagic@sina.com
 */
public class AppDataLogExecute extends AbstractExecute
		implements Execute, Generator
{
	public String getExecuteType()
			throws EternaException
	{
		return "appDataLog";
	}

	public ModelExport execute(AppData data, Connection conn)
			throws EternaException, SQLException, IOException
	{
		printAppData(data);
		return null;
	}

	/**
	 * 用于记录信息打印者的构造器.
	 */
	private static PrinterCreater creater = new PrinterCreater();

	/**
	 * 设置用于记录信息打印者的构造器.
	 */
	public static void setPrinterCreater(PrinterCreater c)
	{
		creater = c;
	}

	/**
	 * 输出AppData对象中的信息.
	 */
	public static void printAppData(AppData data)
	{
		if (data.getLogType() == 0)
		{
			return;
		}
		try
		{
			creater.createPrinter().printAppData(data);
		}
		catch (Throwable ex)
		{
			log.error("Error in print app data.", ex);
		}
	}

	/**
	 * 将对象中的信息添加到父节点中.
	 *
	 * @param parent   被添加信息的父节点
	 * @param value    包含需要添加信息的对象
	 */
	public static void printObject(Element parent, Object value)
	{
		try
		{
			creater.createPrinter().printObject(parent, value);
		}
		catch (Throwable ex)
		{
			log.error("Error in print object:" + value, ex);
			parent.addAttribute("error", ex.getMessage());
		}
	}

	/**
	 * 设置app运行日志记录方式
	 */
	static void setAppLogType(String type)
	{
		try
		{
			APP_LOG_TYPE = Integer.parseInt(type);
			AppData.setAppLogType(APP_LOG_TYPE);
		}
		catch (Exception ex)
		{
			log.error("Error in set app log type.", ex);
		}
	}

	/**
	 * 获取app运行日志记录方式.
	 */
	public static int getAppLogType()
	{
		return APP_LOG_TYPE;
	}
	private static int APP_LOG_TYPE = 0;


	/**
	 * 根据需要打印对象的类型获取一个BeanPrinter对象.
	 */
	public static BeanPrinter getBeanPrinter(Class beanClass)
	{
		BeanPrinter bp = (BeanPrinter) beanPrinterCache.getProperty(beanClass);
		if (bp == null)
		{
			bp = getBeanPrinter0(beanClass);
		}
		return bp;
	}

	/**
	 * 注册一个BeanPrinter对象.
	 */
	public static synchronized void registerBeanPrinter(Class beanClass, BeanPrinter p)
	{
		if (beanClass != null && p != null)
		{
			beanPrinterCache.setProperty(beanClass, p);
		}
	}

	private static ClassKeyCache beanPrinterCache = ClassKeyCache.getInstance();
	/**
	 * BeanPrinter生成.
	 */
	private static synchronized BeanPrinter getBeanPrinter0(Class beanClass)
	{
		BeanPrinter bp = (BeanPrinter) beanPrinterCache.getProperty(beanClass);
		if (bp == null)
		{
			try
			{
				String mh = "public void print(" + ClassGenerator.getClassName(Printer.class)
						+ " p," + " Element parent, Object value) throws Exception";
				String ut = "p.printObject(parent.addElement(\"${type}\")"
						+ ".addAttribute(\"name\", \"${name}\")"
						+ ", ${value});";
				String pt = "parent.addElement(\"${type}\")"
						+ ".addAttribute(\"name\", \"${name}\")"
						+ ".addAttribute(\"type\", \"${primitive}\")"
						+ ".addAttribute(\"value\", ${value});";
				String[] imports = new String[]{
					ClassGenerator.getPackageString(AppDataLogExecute.class),
					ClassGenerator.getPackageString(Element.class),
					ClassGenerator.getPackageString(beanClass)
				};
				bp = (BeanPrinter) Tool.createBeanPrinter(beanClass, BeanPrinter.class, mh,
						"value", ut, pt, "", imports);
				if (bp == null)
				{
					bp = new BeanPrinterImpl(beanClass);
				}
			}
			catch (Throwable ex)
			{
				bp = new BeanPrinterImpl(beanClass);
			}
			beanPrinterCache.setProperty(beanClass, bp);
		}
		return bp;
	}

	public interface BeanPrinter
	{
		public void print(Printer p, Element parent, Object value) throws Exception;

	}

	private static class BeanPrinterImpl
			implements BeanPrinter
	{
		private final Field[] fields;
		private final BeanMethodInfo[] methods;

		public BeanPrinterImpl(Class c)
		{
			this.fields = BeanTool.getBeanFields(c);
			this.methods = BeanTool.getBeanReadMethods(c);
		}

		public void print(Printer p, Element parent, Object value)
				throws Exception
		{
			for (int i = 0; i < this.fields.length; i++)
			{
				Field f = this.fields[i];
				Element fNode = parent.addElement("field");
				fNode.addAttribute("name", f.getName());
				p.printObject(fNode, f.get(value));
			}
			for (int i = 0; i < this.methods.length; i++)
			{
				BeanMethodInfo m = this.methods[i];
				if (m.method != null)
				{
					Element mNode = parent.addElement("method");
					mNode.addAttribute("name", m.name);
					p.printObject(mNode, m.method.invoke(value, new Object[0]));
				}
			}
		}

	}

	/**
	 * 日志信息打印者放置在线程缓存中的标签名.
	 */
	private static final String PRINTER_CACHE_FLAG = "app.data.log.printer";

	/**
	 * 记录日志信息打印者的创建器.
	 */
	public static class PrinterCreater
	{
		public Printer createPrinter()
		{
			ThreadCache cache = ThreadCache.getInstance();
			Printer p = (Printer) cache.getProperty(PRINTER_CACHE_FLAG);
			if (p == null)
			{
				p = new Printer();
				cache.setProperty(PRINTER_CACHE_FLAG, p);
			}
			return p;
		}

	}

	/**
	 * 记录日志信息的打印者.
	 */
	public static class Printer
	{
		private final ArrayList cStack = new ArrayList();
		private int idIndex = 1;

		public void printAppData(AppData data)
				throws Exception
		{
			Element node = data.getCurrentNode();
			if (node == null)
			{
				return;
			}
			Element appData = node.addElement("appData");
			for (int i = 0; i < data.maps.length; i++)
			{
				try
				{
					Element mapNode = appData.addElement(AppData.MAP_NAMES[i]);
					this.printObject(mapNode, data.maps[i]);
				}
				catch (Exception ex)
				{
					log.error("Error in print app data map(" + i + ").", ex);
				}
			}
			Element tmpNode = appData.addElement("cache");
			this.printObject(tmpNode, data.caches);
			tmpNode = appData.addElement("stack");
			this.printObject(tmpNode, data.stack);
		}

		private void printMap(Element parent, Map map)
				throws Exception
		{
			Set entrySet = map.entrySet();
			if (entrySet == null)
			{
				parent.addAttribute("error", "The map enrty set is null.");
				parent.addAttribute("type", map.getClass().getName());
				return;
			}
			parent.addAttribute("count", Integer.toString(entrySet.size()));
			Iterator entrys = entrySet.iterator();
			while (entrys.hasNext())
			{
				Map.Entry entry = (Map.Entry) entrys.next();
				String key = String.valueOf(entry.getKey());
				Object value = entry.getValue();
				Element vNode = parent.addElement("value");
				vNode.addAttribute("key", key);
				this.printObject(vNode, value);
			}
		}

		/**
		 * 输出一个对象数组.
		 */
		private void printObjectArray(Element parent, Object[] array)
				throws Exception
		{
			parent.addAttribute("count", Integer.toString(array.length));
			for (int i = 0; i < array.length; i++)
			{
				Element vNode = parent.addElement("value");
				vNode.addAttribute("index", Integer.toString(i));
				this.printObject(vNode, array[i]);
			}
		}

		/**
		 * 输出一个外覆类数组.
		 */
		private void printWrapperArray(Element parent, Object[] array)
				throws Exception
		{
			parent.addAttribute("count", Integer.toString(array.length));
			String type = null;
			String className = null;
			for (int i = 0; i < array.length; i++)
			{
				Element vNode = parent.addElement("value");
				vNode.addAttribute("index", Integer.toString(i));
				Object value = array[i];
				if (type == null)
				{
					if (value instanceof Boolean)
					{
						type = "boolean";
					}
					else if (value instanceof Number)
					{
						type = "number";
						className = ClassGenerator.getClassName(value.getClass());
					}
					else
					{
						type = "char";
					}
				}
				vNode.addAttribute("type", type);
				if (className != null)
				{
					parent.addAttribute("class", ClassGenerator.getClassName(value.getClass()));
				}
				parent.addAttribute("value", String.valueOf(value));
			}
		}

		private void printCollection(Element parent, Collection collection)
				throws Exception
		{
			parent.addAttribute("count", Integer.toString(collection.size()));
			if (collection.size() > 0)
			{
				int index = 0;
				Iterator itr = collection.iterator();
				while (itr.hasNext())
				{
					Element vNode = parent.addElement("value");
					vNode.addAttribute("index", Integer.toString(index));
					this.printObject(vNode, itr.next());
					index++;
				}
			}
		}

		private void printResultRow(Element parent, ResultRow row)
				throws Exception
		{
			parent.addAttribute("type", "ResultRow");
			ResultMetaData rmd = row.getResultIterator().getMetaData();
			int count = rmd.getColumnCount();
			parent.addAttribute("columnCount", Integer.toString(count));
			Object value;
			for (int i = 1; i <= count; i++)
			{
				if (rmd.getColumnReader(i).isValid())
				{
					Element vNode = parent.addElement("value");
					vNode.addAttribute("columnName", rmd.getColumnName(i));
					vNode.addAttribute("type", TypeManager.getTypeName(rmd.getColumnReader(i).getType()));
					value = row.getFormated(i);
					if (value instanceof String)
					{
						vNode.addAttribute("value", (String) value);
					}
					else
					{
						Element tmp = vNode.addElement("object");
						this.printObject(tmp, value);
					}
				}
			}
		}

		private void printResultIterator(Element parent, ResultIterator ritr)
				throws Exception
		{
			parent.addAttribute("type", "ResultIterator");
			int rowCount = ritr.getCount();
			parent.addAttribute("rowCount", Integer.toString(rowCount));
			int printCount = 5;
			boolean hasMore = true;
			if (rowCount != -1 && rowCount < printCount)
			{
				hasMore = false;
				printCount = rowCount;
			}
			for (int i = 1; i <= printCount; i++)
			{
				ResultRow row = ritr.preFetch(i);
				if (row == null)
				{
					hasMore = false;
					break;
				}
				Element vNode = parent.addElement("value");
				vNode.addAttribute("index", Integer.toString(i));
				this.printResultRow(vNode, row);
			}
			if (rowCount > printCount || (hasMore && ritr.preFetch(printCount + 1) != null))
			{
				Element vNode = parent.addElement("value");
				vNode.addAttribute("type", "more");
				vNode.addAttribute("value", "...");
			}
		}

		private void printIterator(Element parent, PreFetchIterator itr)
				throws Exception
		{
			int index = 1;
			BooleanRef hasNext = new BooleanRef();
			Object obj = itr.prefetch(index, hasNext);
			while (hasNext.value)
			{
				Element vNode = parent.addElement("value");
				vNode.addAttribute("index", Integer.toString(index - 1));
				this.printObject(vNode, obj);
				obj = itr.prefetch(++index, hasNext);
			}
		}

		public void printObject(Element parent, Object value)
				throws Exception
		{
			if (value == null)
			{
				parent.addAttribute("type", "null");
			}
			else if (value instanceof String)
			{
				parent.addAttribute("type", "String");
				parent.addAttribute("value", (String) value);
			}
			else if (value instanceof Number)
			{
				parent.addAttribute("type", "Number");
				parent.addAttribute("class", ClassGenerator.getClassName(value.getClass()));
				parent.addAttribute("value", String.valueOf(value));
			}
			else if (value instanceof Boolean)
			{
				parent.addAttribute("type", "Boolean");
				parent.addAttribute("value", String.valueOf(value));
			}
			else if (value instanceof BeanPrinter)
			{
				((BeanPrinter) value).print(this, parent, value);
			}
			else if (value instanceof ResultRow)
			{
				this.printResultRow(parent, (ResultRow) value);
			}
			else if (value instanceof SearchManager)
			{
				parent.addAttribute("type", "SearchManager");
				SearchManager sm = (SearchManager) value;
				Iterator itr = sm.getConditions().iterator();
				while (itr.hasNext())
				{
					ConditionInfo con = (ConditionInfo) itr.next();
					Element vNode = parent.addElement("value");
					vNode.addAttribute("conditionName", con.name);
					if (con.value == null)
					{
						vNode.addAttribute("type", "null");
					}
					else
					{
						this.printObject(vNode, con.value);
					}
				}
			}
			else if (value instanceof ResultIterator)
			{
				this.printResultIterator(parent, (ResultIterator) value);
			}
			else if (value instanceof Collection)
			{
				if (value instanceof List)
				{
					parent.addAttribute("type", "List");
				}
				else if (value instanceof Set)
				{
					parent.addAttribute("type", "Set");
				}
				else
				{
					parent.addAttribute("type", "Collection");
				}
				if (this.checkAndPush(parent, value))
				{
					try
					{
						this.printCollection(parent, (Collection) value);
					}
					finally
					{
						this.pop();
					}
				}
			}
			else if (value instanceof SearchResult)
			{
				parent.addAttribute("type", "SearchAdapter.Result");
				SearchResult result = (SearchResult) value;
				parent.addAttribute("pageNum", Integer.toString(result.pageNum));
				parent.addAttribute("pageSize", Integer.toString(result.pageSize));
				parent.addAttribute("searchName", result.searchName);
				if (result.queryResult.isTotalCountAvailable())
				{
					parent.addAttribute("totalCount", Integer.toString(result.queryResult.getTotalCount()));
				}
				if (result.orderConfig != null)
				{
					parent.addAttribute("orderConfig", result.orderConfig);
				}
				parent.addAttribute("hasNextPage", String.valueOf(result.queryResult.hasMoreRecord()));
				Element vNode = parent.addElement("value");
				printResultIterator(vNode, result.queryResult);
			}
			else if (value instanceof SearchAttributes)
			{
				parent.addAttribute("type", "SearchManager.Attributes");
				SearchAttributes sma = (SearchAttributes) value;
				parent.addAttribute("pageNumTag", sma.pageNumTag);
				parent.addAttribute("pageSizeTag", sma.pageSizeTag);
				parent.addAttribute("querySettingTag", sma.querySettingTag);
				parent.addAttribute("queryTypeClear", sma.queryTypeClear);
				parent.addAttribute("queryTypeReset", sma.queryTypeReset);
				parent.addAttribute("queryTypeKeep", sma.queryTypeKeep);
				parent.addAttribute("queryTypeTag", sma.queryTypeTag);
			}
			else if (value instanceof Map)
			{
				parent.addAttribute("type", "Map");
				if (this.checkAndPush(parent, value))
				{
					try
					{
						this.printMap(parent, (Map) value);
					}
					finally
					{
						this.pop();
					}
				}
			}
			else if (value instanceof SessionCache.Property)
			{
				this.printObject(parent, ((SessionCache.Property) value).getValue());
			}
			else if (value instanceof Iterator)
			{
				parent.addAttribute("type", "Iterator");
				// 只有类型为PreFetchIterator时才能以预取值的方式记录，否则会将游标移到最后
				if (value instanceof PreFetchIterator)
				{
					if (this.checkAndPush(parent, value))
					{
						try
						{
							this.printIterator(parent, (PreFetchIterator) value);
						}
						finally
						{
							this.pop();
						}
					}
				}
				else
				{
					parent.addAttribute("msg", "Can not read!");
				}
			}
			else if (value instanceof Enumeration)
			{
				// Enumeration迭代显示的话会将游标移到最后, 所以这里只能记录类型
				parent.addAttribute("type", "Enumeration").addAttribute("msg", "Can not read!");
			}
			else if (value instanceof Date)
			{
				parent.addAttribute("type", "Date");
				parent.addAttribute("millis", Long.toString(((Date) value).getTime()));
				parent.addAttribute("value", FormatTool.formatFullDate(value));
			}
			else if (value instanceof Calendar)
			{
				parent.addAttribute("type", "Calendar");
				Date d = ((Calendar) value).getTime();
				parent.addAttribute("value", FormatTool.formatFullDate(d));
			}
			else if (Tool.isBean(value.getClass()))
			{
				parent.addAttribute("type", "bean:" + ClassGenerator.getClassName(value.getClass()));
				if (this.checkAndPush(parent, value))
				{
					try
					{
						this.printBean(parent, value);
					}
					finally
					{
						this.pop();
					}
				}
			}
			else if (value instanceof Map.Entry)
			{
				Map.Entry entry = (Map.Entry) value;
				String tKey = String.valueOf(entry.getKey());
				Object tValue = entry.getValue();
				parent.addAttribute("type", "Entry");
				Element vNode = parent.addElement("value");
				vNode.addAttribute("key", tKey);
				this.printObject(vNode, tValue);
			}
			else if (ClassGenerator.isArray(value.getClass()))
			{
				parent.addAttribute("type", "Array");
				if (this.checkAndPush(parent, value))
				{
					try
					{
						char flag = value.getClass().getName().charAt(1);
						if (flag == 'L' || flag == '[')
						{
							this.printObjectArray(parent, (Object[]) value);
						}
						else
						{
							// 如果是基本类型, 则转成外覆类
							Object[] tmpArr = (Object[]) ArrayTool.wrapPrimitiveArray(1, value);
							this.printWrapperArray(parent, tmpArr);
						}
					}
					finally
					{
						this.pop();
					}
				}
			}
			else if (value instanceof Character)
			{
				parent.addAttribute("type", "Character");
				parent.addAttribute("value", String.valueOf(value));
			}
			else
			{
				parent.addAttribute("type", "class:" + ClassGenerator.getClassName(value.getClass()));
				parent.addAttribute("value", value.toString());
			}
		}

		private void printBean(Element parent, Object value)
				throws Exception
		{
			Class c = value.getClass();
			BeanPrinter bp = getBeanPrinter(c);
			bp.print(this, parent, value);
		}

		private boolean checkAndPush(Element parent, Object value)
		{
			for (int i = this.cStack.size() - 1; i >= 0; i--)
			{
				Object[] tmp = (Object[]) this.cStack.get(i);
				if (tmp[0] == value)
				{
					Element sameE = (Element) tmp[1];
					String id = sameE.attributeValue("containerId");
					if (id == null)
					{
						id = "c_" + this.idIndex;
						this.idIndex++;
						sameE.addAttribute("containerId", id);
					}
					parent.addAttribute("recursion", "true");
					parent.addAttribute("refId", id);
					return false;
				}
			}
			this.cStack.add(new Object[]{value, parent});
			return true;
		}

		private void pop()
		{
			this.cStack.remove(this.cStack.size() - 1);
		}

	}

}