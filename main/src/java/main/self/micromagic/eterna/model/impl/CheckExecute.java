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

import java.sql.Connection;
import java.sql.SQLException;
import java.io.IOException;
import java.util.Iterator;

import self.micromagic.eterna.model.CheckExecuteGenerator;
import self.micromagic.eterna.model.Execute;
import self.micromagic.eterna.model.ModelExport;
import self.micromagic.eterna.model.AppData;
import self.micromagic.eterna.model.ModelAdapter;
import self.micromagic.eterna.model.CheckOperator;
import self.micromagic.eterna.model.AppDataLogExecute;
import self.micromagic.eterna.share.EternaException;
import self.micromagic.eterna.digester.ObjectCreateRule;
import self.micromagic.eterna.share.EternaFactory;
import self.micromagic.util.StringTool;
import self.micromagic.util.ObjectRef;
import self.micromagic.util.Utility;
import self.micromagic.util.StringAppender;
import org.dom4j.Element;

public class CheckExecute extends AbstractExecute
		implements Execute, CheckExecuteGenerator
{
	public static int MAX_LOOP_COUNT = 1024 * 32;

	static
	{
		try
		{
			Utility.addFieldPropertyManager(MAX_LOOP_COUNT_PROPERTY, CheckExecute.class, "MAX_LOOP_COUNT");
		}
		catch (Throwable ex)
		{
			log.warn("Error in init CheckExecute max loop count.", ex);
		}
	}

	private String trueExportName = null;
	private ModelExport trueExport = null;
	private String trueModelName = null;
	private int trueModelIndex = -1;
	private int trueTransactionType = -1;

	private String falseExportName = null;
	private ModelExport falseExport = null;
	private String falseModelName = null;
	private int falseModelIndex = -1;
	private int falseTransactionType = -1;

	private int loopType = 0;

	private int obj1Index = -1;
	private int obj2Index = -1;
	private CheckOperator checkOpt = null;

	public void initialize(ModelAdapter model)
			throws EternaException
	{
		if (this.initialized)
		{
			return;
		}
		super.initialize(model);

		if (this.checkOpt == null)
		{
			throw new EternaException("The attribute checkPattern must be setted.");
		}

		StringAppender nameBuf = StringTool.createStringAppender(64);
		nameBuf.append('[');
		if (this.trueExportName != null)
		{
			nameBuf.append("trueExport:").append(this.trueExportName);
			this.trueExport = model.getFactory().getModelExport(this.trueExportName);
			if (this.trueExport == null)
			{
				log.warn("The Model Export [" + this.trueExportName + "] not found.");
			}
		}
		if (this.trueModelName != null)
		{
			if (nameBuf.length() > 0) nameBuf.append(',');
			nameBuf.append("trueModel:").append(this.trueModelName);
			this.trueModelIndex = model.getFactory().getModelAdapterId(this.trueModelName);
		}

		if (this.falseExportName != null)
		{
			if (nameBuf.length() > 0) nameBuf.append(',');
			nameBuf.append("falseExport:").append(this.falseExportName);
			this.falseExport = model.getFactory().getModelExport(this.falseExportName);
			if (this.falseExport == null)
			{
				log.warn("The Model Export [" + this.falseExportName + "] not found.");
			}
		}
		if (this.falseModelName != null)
		{
			if (nameBuf.length() > 0) nameBuf.append(',');
			nameBuf.append("falseModel:").append(this.falseModelName);
			this.falseModelIndex = model.getFactory().getModelAdapterId(this.falseModelName);
		}
		if (nameBuf.length() > 0) nameBuf.append(',');
		nameBuf.append("loopType:").append(this.loopType).append(']');

		if (this.name == null)
		{
			this.name = StringTool.intern(nameBuf.toString());
		}
	}

	public String getExecuteType()
			throws EternaException
	{
		return "check";
	}

	public ModelExport execute(AppData data, Connection conn)
			throws EternaException, SQLException, IOException
	{
		boolean checkResult;
		ModelExport export;
		ObjectRef preConn = (ObjectRef) data.getSpcialData(ModelAdapter.MODEL_CACHE, ModelAdapter.PRE_CONN);
		int loopCount = 0;
		EternaFactory f = this.getModelAdapter().getFactory();
		do
		{
			Object obj1 = this.obj1Index != -1 ? data.caches[this.obj1Index] : null;
			Object obj2 = this.obj2Index != -1 ? data.caches[this.obj2Index] : null;
			checkResult = this.checkOpt.check(obj1, obj2);
			if (data.getLogType() > 0)
			{
				Element nowNode = data.getCurrentNode();
				if (nowNode != null)
				{
					Element rNode = nowNode.addElement("check-result");
					rNode.addAttribute("value", String.valueOf(checkResult));
					rNode.addAttribute("checkPattern", this.checkOpt.toString());
					if (obj1 != null)
					{
						Element vNode = rNode.addElement("obj1");
						vNode.addAttribute("cache", Integer.toString(this.obj1Index));
						AppDataLogExecute.printObject(vNode, obj1);
					}
					if (obj2 != null)
					{
						Element vNode = rNode.addElement("obj2");
						vNode.addAttribute("cache", Integer.toString(this.obj2Index));
						AppDataLogExecute.printObject(vNode, obj2);
					}
				}
			}
			if (checkResult)
			{
				if (this.trueModelIndex != -1)
				{
					ModelAdapter tmpModel = f.createModelAdapter(this.trueModelIndex);
					int tType = this.trueTransactionType == -1 ? tmpModel.getTransactionType() : this.trueTransactionType;
					export = f.getModelCaller().callModel(data, tmpModel, this.trueExport, tType, preConn);
				}
				else
				{
					export = this.trueExport;
				}
			}
			else
			{
				if (this.falseModelIndex != -1)
				{
					ModelAdapter tmpModel = f.createModelAdapter(this.falseModelIndex);
					int tType = this.falseTransactionType == -1 ? tmpModel.getTransactionType() : this.falseTransactionType;
					export = f.getModelCaller().callModel(data, tmpModel, this.falseExport, tType, preConn);
				}
				else
				{
					export = this.falseExport;
				}
			}

			// 这里判断循环的最大数, 防止死循环
			loopCount++;
			if (loopCount > MAX_LOOP_COUNT)
			{
				log.warn("The execute [" + this.getName() + "] is breaked.");
				break;
			}

			if (export != null)
			{
				return export;
			}

		} while (checkResult ? this.loopType > 0 : this.loopType < 0);
		return export;
	}

	public void setCheckPattern(String pattern)
			throws EternaException
	{
		String[] params = StringTool.separateString(pattern, ";", true);
		if (params.length > 3)
		{
			throw new EternaException("Too many type params:[" + pattern + "].");
		}
		if (params.length > 0)
		{
			try
			{
				this.obj1Index = Integer.parseInt(params[0]);
			}
			catch (NumberFormatException ex)
			{
				throw new EternaException("Error check obj1:" + params[0] + ".");
			}
		}
		if (params.length == 3)
		{
			try
			{
				this.obj2Index = Integer.parseInt(params[2]);
			}
			catch (NumberFormatException ex)
			{
				throw new EternaException("Error check obj2:" + params[2] + ".");
			}
		}
		if (params.length > 1)
		{
			if ("null".equals(params[1]))
			{
				this.checkOpt = NullCheck.instance;
			}
			else if ("array".equals(params[1]))
			{
				this.checkOpt = ArrayCheck.instance;
			}
			else if ("hasNext".equals(params[1]))
			{
				this.checkOpt = HasNextCheck.instance;
			}
			else if ("empty".equals(params[1]))
			{
				this.checkOpt = EmptyCheck.instance;
			}
			else if ("<".equals(params[1]))
			{
				this.checkOpt = CompareCheck.instance[0];
			}
			else if ("=".equals(params[1]))
			{
				this.checkOpt = CompareCheck.instance[1];
			}
			else if (">".equals(params[1]))
			{
				this.checkOpt = CompareCheck.instance[2];
			}
			else if (params[1].startsWith("class:"))
			{
				try
				{
					this.checkOpt = (CheckOperator) ObjectCreateRule.createObject(
							params[1].substring(6));
				}
				catch (Exception ex)
				{
					throw new EternaException(ex);
				}
			}
			else
			{
				try
				{
					this.checkOpt = new ClassTypeCheck(Class.forName(params[1]));
				}
				catch (Exception ex)
				{
					throw new EternaException(ex);
				}
			}
		}
	}

	public void setLoopType(int type)
	{
		this.loopType = type;
	}

	public void setTrueExportName(String name)
	{
		this.trueExportName = name;
	}

	public void setFalseExportName(String name)
	{
		this.falseExportName = name;
	}

	public void setTrueTransactionType(String tType)
			throws EternaException
	{
		this.trueTransactionType = ModelAdapterImpl.parseTransactionType(tType);
	}

	public void setFalseTransactionType(String tType)
			throws EternaException
	{
		this.falseTransactionType = ModelAdapterImpl.parseTransactionType(tType);
	}

	public void setTrueModelName(String name)
			throws EternaException
	{
		this.trueModelName = name;
		this.setName(name);
	}

	public void setFalseModelName(String name)
			throws EternaException
	{
		this.falseModelName = name;
	}

	private static class HasNextCheck
			implements CheckOperator
	{
		static HasNextCheck instance = new HasNextCheck();

		public boolean check(Object value1, Object value2)
				throws EternaException
		{
			if (value1 != null && value1 instanceof Iterator)
			{
				return ((Iterator) value1).hasNext();
			}
			return false;
		}

		public String toString()
		{
			return "hasNext";
		}

	}

	private static class NullCheck
			implements CheckOperator
	{
		static NullCheck instance = new NullCheck();

		public boolean check(Object value1, Object value2)
				throws EternaException
		{
			return value1 == null;
		}

		public String toString()
		{
			return "nullCheck";
		}

	}

	private static class EmptyCheck
			implements CheckOperator
	{
		static EmptyCheck instance = new EmptyCheck();

		public boolean check(Object value1, Object value2)
				throws EternaException
		{
			return value1 == null || "".equals(value1);
		}

		public String toString()
		{
			return "emptyCheck";
		}

	}

	private static class ArrayCheck
			implements CheckOperator
	{
		static ArrayCheck instance = new ArrayCheck();

		public boolean check(Object value1, Object value2)
				throws EternaException
		{
			if (value1 == null)
			{
				return false;
			}
			return value1.getClass().isArray();
		}

		public String toString()
		{
			return "arrayCheck";
		}

	}

	private static class ClassTypeCheck
			implements CheckOperator
	{
		private Class checkClass;

		public ClassTypeCheck(Class checkClass)
		{
			this.checkClass = checkClass;
		}

		public boolean check(Object value1, Object value2)
				throws EternaException
		{
			if (value1 == null)
			{
				return false;
			}
			return this.checkClass.isInstance(value1);
		}

		public String toString()
		{
			return "typeCheck";
		}

	}

	private static class CompareCheck
			implements CheckOperator
	{
		static CompareCheck[] instance = {
			new CompareCheck(-1), new CompareCheck(0), new CompareCheck(1)
		};

		private int compareResult;

		public CompareCheck(int compareResult)
		{
			this.compareResult = compareResult;
		}

		public boolean check(Object value1, Object value2)
				throws EternaException
		{
			if (value1 == null)
			{
				// 如果是等于比较，则value2为空则为true
				return value2 == null && this.compareResult == 0;
			}
			else if (value2 == null)
			{
				// 如果是大于比较，则value2为空则为true
				return this.compareResult == 1;
			}
			if (value1 instanceof Comparable && value2 instanceof Comparable)
			{
				int tmpResult = ((Comparable) value1).compareTo(value2);
				tmpResult = tmpResult < 0 ? -1 : tmpResult > 0 ? 1 : 0;
				return tmpResult == this.compareResult;
			}
			else
			{
				throw new EternaException("The two value can not compare, 1:"
						+ value1.getClass() + ", 2:" + value2.getClass() + ".");
			}
		}

		public String toString()
		{
			return this.compareResult == 0 ? "=" : this.compareResult < 0 ? "<" : ">";
		}

	}

}