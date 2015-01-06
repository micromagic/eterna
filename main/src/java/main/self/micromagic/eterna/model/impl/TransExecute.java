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

package self.micromagic.eterna.model.impl;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.Map;
import java.util.Iterator;
import java.util.Collection;
import java.util.Arrays;
import java.util.Enumeration;

import self.micromagic.eterna.share.EternaException;
import self.micromagic.eterna.digester.ObjectCreateRule;
import self.micromagic.eterna.model.AppData;
import self.micromagic.eterna.model.Execute;
import self.micromagic.eterna.model.ModelAdapter;
import self.micromagic.eterna.model.ModelExport;
import self.micromagic.eterna.model.TransExecuteGenerator;
import self.micromagic.eterna.model.TransOperator;
import self.micromagic.eterna.model.AppDataLogExecute;
import self.micromagic.eterna.sql.ResultIterator;
import self.micromagic.eterna.sql.ResultRow;
import self.micromagic.util.converter.StringConverter;
import self.micromagic.util.converter.ValueConverter;
import self.micromagic.util.converter.IntegerConverter;
import self.micromagic.util.converter.LongConverter;
import self.micromagic.util.converter.DoubleConverter;
import self.micromagic.util.converter.TimeConverter;
import self.micromagic.util.converter.DateConverter;
import self.micromagic.util.converter.TimestampConverter;
import self.micromagic.eterna.search.SearchAdapter;
import self.micromagic.eterna.search.SearchManager;
import self.micromagic.util.StringTool;
import self.micromagic.util.StringRef;
import self.micromagic.util.container.PreFetchIterator;
import org.dom4j.Element;
import org.apache.commons.collections.iterators.EnumerationIterator;

public class TransExecute extends AbstractExecute
		implements Execute, TransExecuteGenerator
{
	protected DataHandler fromHandler = new DataHandler("from", true, true);
	protected boolean removeFrom = false;
	protected boolean mustExist = true;

	protected DataHandler toHandler = null;

	protected TransOperator opt;
	protected boolean pushResult = false;

	public void initialize(ModelAdapter model)
			throws EternaException
	{
		if (this.initialized)
		{
			return;
		}
		super.initialize(model);
	}

	public String getExecuteType()
	{
		return "trans";
	}

	public String getName()
			throws EternaException
	{
		return this.toHandler == null ? this.pushResult ? "#stack" : "#none"
				: this.toHandler.getConfig();
	}

	public boolean isPushResult()
	{
		return this.pushResult;
	}

	public void setPushResult(boolean push)
	{
		this.pushResult = push;
	}

	public void setFrom(String theFrom)
			throws EternaException
	{
		this.fromHandler.setConfig(theFrom);
	}

	public void setRemoveFrom(boolean remove)
	{
		this.removeFrom = remove;
	}

	public void setMustExist(boolean mustExist)
	{
		this.mustExist = mustExist;
	}

	public void setOpt(String opt)
			throws EternaException
	{
		if (opt.startsWith("class:"))
		{
			try
			{
				this.opt = (TransOperator) ObjectCreateRule.createObject(opt.substring(6));
			}
			catch (Exception ex)
			{
				throw new EternaException(ex);
			}
		}
		else
		{
			int index = opt.indexOf(':');
			String optName = opt;
			String optParam = null;
			if (index != -1)
			{
				optName = opt.substring(0, index);
				optParam = opt.substring(index + 1);
			}
			if ("getFirstRow".equals(optName))
			{
				this.opt = TransResultRow.instance;
			}
			else if ("getNext".equals(optName))
			{
				this.opt = TransNext.instance;
			}
			else if ("getFirstString".equals(optName))
			{
				this.opt = TransStrings.instance;
			}
			else if ("getFormated".equals(optName))
			{
				this.opt = new TransResultValue(optParam);
			}
			else if ("getObject".equals(optName))
			{
				this.opt = new TransResultValue(optParam, false);
			}
			else if ("getMapValue".equals(optName))
			{
				this.opt = new TransMapValue(optParam);
			}
			else if ("toResultIterator".equals(optName))
			{
				this.opt = TransToResultIterator.instance;
			}
			else if ("toIterator".equals(optName))
			{
				this.opt = TransToIterator.instance;
			}
			else if ("beforeFirst".equals(optName))
			{
				this.opt = TransBeforeFirst.instance;
			}
			else if ("toArray".equals(optName))
			{
				this.opt = optParam != null ? new TransToArray(optParam) : TransToArray.instance;
			}
			else if ("toString".equals(optName))
			{
				this.opt = new TransToWatendType(new StringConverter());
			}
			else if ("toInteger".equals(optName))
			{
				this.opt = new TransToWatendType(new IntegerConverter());
			}
			else if ("toLong".equals(optName))
			{
				this.opt = new TransToWatendType(new LongConverter());
			}
			else if ("toDouble".equals(optName))
			{
				this.opt = new TransToWatendType(new DoubleConverter());
			}
			else if ("toTime".equals(optName))
			{
				this.opt = new TransToWatendType(new TimeConverter());
			}
			else if ("toDate".equals(optName))
			{
				this.opt = new TransToWatendType(new DateConverter());
			}
			else if ("toDatetime".equals(optName))
			{
				this.opt = new TransToWatendType(new TimestampConverter());
			}
			else
			{
				throw new EternaException("Error opt:[" + opt + "].");
			}
		}
	}

	public void setTo(String theTo)
			throws EternaException
	{
		if (this.toHandler == null)
		{
			this.toHandler = new DataHandler("to", true, false);
		}
		this.toHandler.setConfig(theTo);
	}

	public ModelExport execute(AppData data, Connection conn)
			throws EternaException, SQLException, IOException
	{
		Object value = this.fromHandler.getData(data, this.removeFrom);
		if (value == null)
		{
			if (this.mustExist)
			{
				throw new EternaException("There is no value in [" + this.fromHandler.getConfig() + "].");
			}
		}
		Element nowNode = null;
		Element vToNode = null;
		if (data.getLogType() > 0)
		{
			nowNode = data.getCurrentNode();
		}
		if (nowNode != null)
		{
			Element vNode = nowNode.addElement("value-from");
			if (this.removeFrom)
			{
				vNode.addAttribute("removeFrom", "true");
			}
			vNode.addAttribute("config", this.fromHandler.getConfig());
			AppDataLogExecute.printObject(vNode, value);
			vToNode = nowNode.addElement("value-to");
			if (this.isPushResult())
			{
				vToNode.addAttribute("pushResult", "true");
			}
			if (this.toHandler != null)
			{
				vToNode.addAttribute("config", this.toHandler.getConfig());
			}
		}
		if (this.opt != null && value != null)
		{
			value = this.opt.change(value);
			if (vToNode != null)
			{
				vToNode.addAttribute("opt", this.opt.toString());
				AppDataLogExecute.printObject(vToNode, value);
			}
		}

		if (this.toHandler != null)
		{
			this.toHandler.setData(data, value);
		}
		if (this.isPushResult())
		{
			data.push(value);
		}
		return null;
	}

	private static class TransResultValue
			implements TransOperator
	{
		private String param;
		private boolean useFormated = true;

		public TransResultValue(String param)
		{
			this.param = param;
		}

		public TransResultValue(String param, boolean useFormated)
		{
			this.param = param;
			this.useFormated = useFormated;
		}

		public Object change(Object value)
				throws EternaException
		{
			if (value == null)
			{
				return null;
			}
			ResultRow row = (ResultRow) TransResultRow.instance.change(value);
			if (row != null)
			{
				try
				{
					return this.useFormated ? row.getFormated(this.param) : row.getObject(this.param);
				}
				catch (SQLException ex)
				{
					throw new EternaException(ex);
				}
			}
			throw new EternaException("Error Object type:" + value.getClass() + ".");
		}

		public String toString()
		{
			return (this.useFormated ? "getFormated:" : "getObject:") + this.param;
		}

	}

	private static class TransMapValue
			implements TransOperator
	{
		private String param;

		public TransMapValue(String param)
		{
			this.param = param;
		}

		public Object change(Object value)
				throws EternaException
		{
			if (value == null)
			{
				return null;
			}
			if (value instanceof Map)
			{
				Map map = (Map) value;
				return map.get(this.param);
			}
			else if (value instanceof SearchManager)
			{
				SearchManager sm = (SearchManager) value;
				SearchManager.Condition condition = sm.getCondition(this.param);
				return condition != null ? condition.value : null;
			}
			throw new EternaException("Error Object type:" + value.getClass() + ".");
		}

		public String toString()
		{
			return "getMapValue:" + this.param;
		}

	}

	private static class TransStrings
			implements TransOperator
	{
		static TransStrings instance = new TransStrings();

		public Object change(Object value)
				throws EternaException
		{
			if (value == null)
			{
				return null;
			}
			if (value instanceof String[])
			{
				String[] strs = (String[]) value;
				if (strs.length > 0)
				{
					return strs[0];
				}
				return null;
			}
			if (value instanceof String)
			{
				return value;
			}
			throw new EternaException("Error Object type:" + value.getClass() + ".");
		}

		public String toString()
		{
			return "getFirstString";
		}

	}

	private static class TransToIterator
			implements TransOperator
	{
		static TransToIterator instance = new TransToIterator();

		public Object change(Object value)
				throws EternaException
		{
			if (value == null)
			{
				return null;
			}
			if (value instanceof Iterator)
			{
				if (!(value instanceof PreFetchIterator))
				{
					return new PreFetchIterator((Iterator) value);
				}
				return value;
			}
			else if (value instanceof Collection)
			{
				return new PreFetchIterator(((Collection) value).iterator());
			}
			else if (value instanceof Object[])
			{
				return new PreFetchIterator(Arrays.asList((Object[]) value).iterator());
			}
			else if (value instanceof Map)
			{
				return new PreFetchIterator(((Map) value).entrySet().iterator());
			}
			else if (value instanceof Enumeration)
			{
				return new PreFetchIterator(new EnumerationIterator((Enumeration) value));
			}
			throw new EternaException("Error Object type:" + value.getClass() + ".");
		}

		public String toString()
		{
			return "toIterator";
		}

	}

	private static class TransNext
			implements TransOperator
	{
		static TransNext instance = new TransNext();

		public Object change(Object value)
				throws EternaException
		{
			if (value == null)
			{
				return null;
			}
			if (value instanceof Iterator)
			{
				Iterator itr = (Iterator) value;
				if (itr.hasNext())
				{
					return itr.next();
				}
				return null;
			}
			throw new EternaException("Error Object type:" + value.getClass() + ".");
		}

		public String toString()
		{
			return "getNext";
		}

	}

	private static class TransToResultIterator
			implements TransOperator
	{
		static TransToResultIterator instance = new TransToResultIterator();

		public Object change(Object value)
				throws EternaException
		{
			if (value == null)
			{
				return null;
			}
			if (value instanceof ResultIterator)
			{
				return value;
			}
			if (value instanceof SearchAdapter.Result)
			{
				return ((SearchAdapter.Result) value).queryResult;
			}
			throw new EternaException("Error Object type:" + value.getClass() + ".");
		}

		public String toString()
		{
			return "toResultIterator";
		}

	}

	private static class TransBeforeFirst
			implements TransOperator
	{
		static TransBeforeFirst instance = new TransBeforeFirst();

		public Object change(Object value)
				throws EternaException
		{
			if (value == null)
			{
				return null;
			}
			if (value instanceof ResultIterator)
			{
				ResultIterator ritr = (ResultIterator) value;
				try
				{
					ritr.beforeFirst();
				}
				catch (SQLException ex)
				{
					throw new EternaException(ex);
				}
				return ritr;
			}
			if (value instanceof SearchAdapter.Result)
			{
				ResultIterator ritr = ((SearchAdapter.Result) value).queryResult;
				try
				{
					ritr.beforeFirst();
				}
				catch (SQLException ex)
				{
					throw new EternaException(ex);
				}
				return value;
			}
			throw new EternaException("Error Object type:" + value.getClass() + ".");
		}

		public String toString()
		{
			return "beforeFirst";
		}

	}

	private static class TransResultRow
			implements TransOperator
	{
		static TransResultRow instance = new TransResultRow();

		public Object change(Object value)
				throws EternaException
		{
			if (value == null)
			{
				return null;
			}
			if (value instanceof ResultIterator)
			{
				ResultIterator ritr = (ResultIterator) value;
				if (ritr.hasNext())
				{
					try
					{
						return ritr.nextRow();
					}
					catch (SQLException ex)
					{
						throw new EternaException(ex);
					}
				}
				return null;
			}
			if (value instanceof ResultRow)
			{
				return value;
			}
			if (value instanceof SearchAdapter.Result)
			{
				ResultIterator ritr = ((SearchAdapter.Result) value).queryResult;
				if (ritr.hasNext())
				{
					try
					{
						return ritr.nextRow();
					}
					catch (SQLException ex)
					{
						throw new EternaException(ex);
					}
				}
				return null;
			}
			throw new EternaException("Error Object type:" + value.getClass() + ".");
		}

		public String toString()
		{
			return "getFirstRow";
		}

	}

	private static class TransToArray
			implements TransOperator
	{
		static TransToArray instance = new TransToArray();

		private String param;

		public TransToArray(String param)
		{
			this.param = param;
		}

		public TransToArray()
		{
			this.param = ",";
		}

		public Object change(Object value)
				throws EternaException
		{
			if (value == null)
			{
				return null;
			}
			if (value instanceof ResultIterator)
			{
				LinkedList list = new LinkedList();
				ResultIterator ritr = (ResultIterator) value;
				try
				{
					while (ritr.hasNext())
					{
						list.add(ritr.nextRow().getFormated(this.param));
					}
				}
				catch (SQLException ex)
				{
					throw new EternaException(ex);
				}
				String[] temp = new String[list.size()];
				list.toArray(temp);
				return temp;
			}
			else if (value instanceof String)
			{
				return StringTool.separateString((String) value, this.param, true);
			}
			throw new EternaException("Error Object type:" + value.getClass() + ".");
		}

		public String toString()
		{
			return "toArray:" + this.param;
		}

	}

	private static class TransToWatendType
			implements TransOperator
	{
		private ValueConverter converter;

		public TransToWatendType(ValueConverter converter)
		{
			this.converter = converter;
		}

		public Object change(Object value)
				throws EternaException
		{
			return this.converter.convert(value);
		}

		public String toString()
		{
			StringRef type = new StringRef();
			this.converter.getConvertType(type);
			String typeName = "Unkown";
			if (type.getString() != null && type.getString().length() > 0)
			{
				typeName = type.getString();
			}
			return "convert:" + typeName;
		}

	}

}