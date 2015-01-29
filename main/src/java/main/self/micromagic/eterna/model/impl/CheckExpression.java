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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import self.micromagic.eterna.model.AppData;
import self.micromagic.eterna.model.DataHandler;
import self.micromagic.eterna.model.Expression;
import self.micromagic.eterna.share.EternaException;
import self.micromagic.grammer.GrammerElement;
import self.micromagic.grammer.GrammerException;
import self.micromagic.grammer.GrammerManager;
import self.micromagic.grammer.ParserData;
import self.micromagic.grammer.ParserData.GrammerCell;
import self.micromagic.util.BooleanRef;
import self.micromagic.util.ObjectRef;
import self.micromagic.util.StringTool;
import self.micromagic.util.Utility;
import self.micromagic.util.converter.ConverterFinder;
import self.micromagic.util.converter.ValueConverter;

/**
 * 用于判断的表达式对象.
 */
public class CheckExpression
{
	private CheckExpression(ExpCell cell)
	{
		this.expCell = cell;
	}
	private final ExpCell expCell;

	/**
	 * 执行判断表达式.
	 */
	public boolean execCheck(AppData data)
	{
		return this.expCell.getResult(data);
	}

	/**
	 * 解析一个判断表达式.
	 */
	public static CheckExpression parseExpression(String exp)
			throws EternaException
	{
		GrammerElement ge = rootGE;
		ParserData pd = new ParserData(exp);
		try
		{
			if (!ge.verify(pd))
			{
				throw new EternaException("Error expression [" + exp + "].");
			}
		}
		catch (GrammerException ex)
		{
			throw new EternaException(ex);
		}
		List stack = new ArrayList();
		parseCell(exp, pd.getGrammerCellLst(), stack);
		if (stack.size() == 0)
		{
			throw new EternaException("Error expression [" + exp + "].");
		}
		// 处理需要延迟连接的判断表达式
		parseObj(exp, tryConvertExpCell(pop(stack, null)), null, stack);
		if (stack.size() != 1)
		{
			throw new EternaException("Error expression [" + exp + "].");
		}
		return new CheckExpression((ExpCell) stack.get(0));
	}

	/**
	 * 解析每个语法单元.
	 */
	private static void parseCell(String exp, List cells, List stack)
	{
		Iterator itr = cells.iterator();
		String preOpt = null;
		BooleanRef stringBegin = new BooleanRef(false);
		GrammerCell cell = null;
		while (itr.hasNext())
		{
			cell = (GrammerCell) itr.next();
			int type = cell.grammerElement.getType();
			if (type == GrammerElement.TYPE_OPERATOR)
			{
				if (preOpt == null)
				{
					preOpt = cell.textBuf;
				}
				else
				{
					preOpt += cell.textBuf;
				}
			}
			else if (preOpt != null)
			{
				parsePreOpt(exp, preOpt, cell, stack, stringBegin);
				preOpt = null;
			}
			if (type == GrammerElement.TYPE_NAME)
			{
				if ("refName".equals(cell.grammerElement.getName()))
				{
					DataHandler handler = new DataHandler("var", true, true);
					handler.setConfig(cell.textBuf);
					parseObj(exp, handler, cell, stack);
				}
				else
				{
					Object tOpt = optCache.get(cell.textBuf);
					if (tOpt != null)
					{
						// 如果当前是一个操作, 则按操作处理
						parseObj(exp, tOpt, cell, stack);
					}
					else
					{
						// 其他类型的名称也作为变量处理
						DataHandler handler = new DataHandler("var", true, true);
						handler.setConfig(cell.textBuf);
						parseObj(exp, handler, cell, stack);
					}
				}
			}
			else if (type == GrammerElement.TYPE_EGROUP)
			{
				// 需要处理子语法单元
				parseCell(exp, cell.subCells, stack);
			}
			else if (type != GrammerElement.TYPE_BLANK
					&& type != GrammerElement.TYPE_OPERATOR)
			{
				Object tValue = Expression.getValue(cell);
				parseObj(exp, tValue, cell, stack);
			}
		}
		if (preOpt != null)
		{
			parsePreOpt(exp, preOpt, cell, stack, stringBegin);
		}
	}

	/**
	 * 解析之前保留的操作符.
	 */
	private static void parsePreOpt(String exp, String preOpt, GrammerCell cell,
			List stack, BooleanRef stringBegin)
	{
		if (StringTool.isEmpty(preOpt))
		{
			return;
		}
		if ("(".equals(preOpt))
		{
			stack.add(BLOCK_BEGIN);
		}
		else if (")".equals(preOpt))
		{
			parseObj(exp, BLOCK_END, cell, stack);
		}
		else if ("\"".equals(preOpt))
		{
			if (stringBegin.value)
			{
				parseObj(exp, BLOCK_END, cell, stack);
				stringBegin.value = false;
			}
			else
			{
				stringBegin.value = true;
			}
		}
		else
		{
			Object opt = optCache.get(preOpt);
			if (opt == null)
			{
				if (preOpt.length() > 1)
				{
					// 无法识别的操作, 尝试识别子操作
					for (int i = preOpt.length() - 1; i > 0; i++)
					{
						if (checkSubOpt(preOpt.substring(0, i)))
						{
							parsePreOpt(exp, preOpt.substring(0, i), cell, stack, stringBegin);
							parsePreOpt(exp, preOpt.substring(i), cell, stack, stringBegin);
							return;
						}
					}
				}
				throw new EternaException("Error opt [" + preOpt
						+ "] in expression [" + exp + "].");
			}
			parseObj(exp, opt, cell, stack);
		}
	}
	/**
	 * 检查拆分后的子操作是否合法.
	 */
	private static boolean checkSubOpt(String subOpt)
	{
		if ("(".equals(subOpt) || ")".equals(subOpt) || "\"".equals(subOpt))
		{
			return true;
		}
		return optCache.containsKey(subOpt);
	}

	/**
	 * 对一个操作对象或值进行解析.
	 */
	private static void parseObj(String exp, Object obj, GrammerCell cell, List stack)
	{
		if (obj instanceof CheckOpt)
		{
			CheckOpt cOpt = (CheckOpt) obj;
			if (cOpt.needArgBefore())
			{
				// 操作需要前置参数, 获取前一个堆栈值
				BooleanRef hasValue = new BooleanRef(false);
				Object pObj = pop(stack, hasValue);
				if (!hasValue.value || !isValue(pObj))
				{
					throw new EternaException("The opt [" + cell.textBuf
							+ "] need after a value, expression [" + exp + "].");
				}
				if (cOpt.needArgAfter())
				{
					// 需要第二个操作符的操作, 在获取第二个操作符后再构造
					stack.add(pObj);
					stack.add(obj);
				}
				else
				{
					parseCheckOpt(exp, cOpt.create(pObj, null), stack);
				}
			}
		}
		else if (obj instanceof String)
		{
			BooleanRef hasValue = new BooleanRef(false);
			Object pObj = pop(stack, hasValue);
			if (hasValue.value)
			{
				if (pObj instanceof String)
				{
					stack.add((String) pObj + (String) obj);
				}
				else
				{
					// 前面的值不是String, 放回堆栈
					stack.add(pObj);
					stack.add(obj);
				}
			}
			else
			{
				// 前面没有表达式了, 将当前表达式放入堆栈
				stack.add(obj);
			}
		}
		else if (obj == BLOCK_END)
		{
			// 是块结束符, 解析前一个堆栈元素
			BooleanRef hasValue = new BooleanRef(false);
			Object pObj = pop(stack, hasValue);
			if (!hasValue.value)
			{
				throw new EternaException("Error expression [" + exp + "].");
			}
			if (pObj instanceof String)
			{
				// 如果是String类型, 要包装一下, 以免仍旧被判断为要拼接的字符串
				pObj = new ObjectRef(pObj);
			}
			parseObj(exp, pObj, cell, stack);
		}
		else if (obj instanceof ExpCell)
		{
			// 解析表达式前的堆栈, 只能是ExpLink或not
			BooleanRef hasValue = new BooleanRef(false);
			Object pObj = pop(stack, hasValue);
			if (hasValue.value)
			{
				if (pObj == NOT_FLAG)
				{
					((ExpCell) obj).setNot();
					parseObj(exp, obj, cell, stack);
				}
				else if (pObj instanceof ExpLink)
				{
					ExpLink link = (ExpLink) pObj;
					// 处理判断表达式时不需要判断是否要延迟连接
					pObj = tryConvertExpCell(pop(stack, null));
					if (!(pObj instanceof ExpCell))
					{
						throw new EternaException("Error expression [" + exp + "].");
					}
					((ExpCell) pObj).addNext((ExpCell) obj, link);
					parseObj(exp, pObj, cell, stack);
				}
				else if (pObj == BLOCK_BEGIN)
				{
					ExpCell ec = (ExpCell) obj;
					if (ec.hasNext())
					{
						ec = new ExpCell(new WrapperOpt((ExpCell) obj));
					}
					parseObj(exp, ec, cell, stack);
				}
				else
				{
					throw new EternaException("Error expression [" + exp + "].");
				}
			}
			else
			{
				// 前面没有表达式了, 将当前表达式放回去
				stack.add(obj);
			}
		}
		else if (isValue(obj))
		{
			parseValue(exp, obj, cell, stack);
		}
		else
		{
			// 其它类型直接压入堆栈
			stack.add(obj);
		}
	}

	/**
	 * 对一个value进行解析.
	 */
	private static void parseValue(String exp, Object obj, GrammerCell cell, List stack)
	{
		BooleanRef hasValue = new BooleanRef(false);
		Object pObj = pop(stack, hasValue);
		if (hasValue.value)
		{
			if (pObj instanceof ValueConverter)
			{
				stack.add(new DataOpt(changeObj(obj), (ValueConverter) pObj));
			}
			else if (pObj instanceof CheckOpt)
			{
				CheckOpt cOpt = (CheckOpt) pObj;
				if (cOpt.needArgAfter())
				{
					if (cOpt.needArgBefore())
					{
						Object pValue = changeObj(pop(stack, null));
						parseCheckOpt(exp, cOpt.create(pValue, changeObj(obj)), stack);
					}
					else
					{
						parseCheckOpt(exp, cOpt.create(changeObj(obj), null), stack);
					}
				}
				else
				{
					// 前一个操作不需要的后值, 放回堆栈
					stack.add(pObj);
					stack.add(obj);
				}
			}
			else
			{
				// 前一个值不是需要的类型, 放回堆栈
				stack.add(pObj);
				stack.add(obj);
			}
		}
		else
		{
			// 前面没有表达式了, 将当前表达式放回去
			stack.add(obj);
		}
	}
	/**
	 * 检查是否为ObjectRef, 如果是获取其引用的值.
	 */
	private static Object changeObj(Object value)
	{
		if (value instanceof ObjectRef)
		{
			return ((ObjectRef) value).getObject();
		}
		return value;
	}

	/**
	 * 对一个判断操作进行解析.
	 */
	private static void parseCheckOpt(String exp, CheckOpt opt, List stack)
	{
		BooleanRef hasValue = new BooleanRef(false);
		Object pObj = pop(stack, hasValue);
		if (hasValue.value)
		{
			// 操作前的堆栈, 只能是 块起始符, ExpLink或not
			if (pObj == BLOCK_BEGIN)
			{
				stack.add(BLOCK_BEGIN);
				stack.add(new ExpCell(opt));
			}
			else if (pObj == NOT_FLAG)
			{
				ExpCell e = new ExpCell(opt);
				e.setNot();
				stack.add(e);
			}
			else if (pObj instanceof ExpLink)
			{
				ExpLink link = (ExpLink) pObj;
				if (link.delayLink())
				{
					// 需要延迟连接, 放回堆栈
					stack.add(pObj);
					stack.add(new ExpCell(opt));
				}
				else
				{
					pObj = tryConvertExpCell(pop(stack, null));
					if (!(pObj instanceof ExpCell))
					{
						throw new EternaException("Error expression [" + exp + "].");
					}
					((ExpCell) pObj).addNext(opt, link);
					// 将前面的表达式单元加回堆栈
					stack.add(pObj);
				}
			}
			else
			{
				throw new EternaException("Error expression [" + exp + "].");
			}
		}
		else
		{
			stack.add(new ExpCell(opt));
		}
	}

	/**
	 * 判断对象是否是一个值, 而不是操作.
	 */
	private static boolean isValue(Object obj)
	{
		if (obj == null || obj instanceof DataHandler)
		{
			return true;
		}
		return !(obj instanceof CheckOpt) && !(obj instanceof ExpLink)
				&& !(obj instanceof ExpCell) && !(obj instanceof ValueConverter);
	}

	/**
	 * 尝试转换成判断单元.
	 */
	private static Object tryConvertExpCell(Object obj)
	{
		if (obj instanceof ExpCell)
		{
			return obj;
		}
		else if (obj instanceof BooleanRef)
		{
			return new ExpCell(new WrapperOpt(((BooleanRef) obj).value));
		}
		return obj;
	}

	/**
	 * 弹出堆栈中的值.
	 */
	private static Object pop(List stack, BooleanRef hasValue)
	{
		if (stack.size() > 0)
		{
			if (hasValue != null)
			{
				hasValue.value = true;
			}
			return stack.remove(stack.size() - 1);
		}
		return null;
	}

	/**
	 * 左括号.
	 */
	private static final Object BLOCK_BEGIN = new Object();
	/**
	 * 一个模块的结束标记.
	 */
	private static final Object BLOCK_END = new Object();
	/**
	 * 一个非操作的标记.
	 */
	private static final Object NOT_FLAG = new Object();

	/**
	 * 操作的缓存.
	 */
	private static Map optCache = new HashMap();

	private static GrammerManager grammerManager;
	private static GrammerElement rootGE;
	static
	{
		optCache.put("int", ConverterFinder.findConverter(int.class, false));
		optCache.put("long", ConverterFinder.findConverter(long.class, false));
		optCache.put("double", ConverterFinder.findConverter(double.class, false));
		optCache.put("float", ConverterFinder.findConverter(float.class, false));
		optCache.put("String", ConverterFinder.findConverter(String.class, false));
		optCache.put("string", ConverterFinder.findConverter(String.class, false));
		optCache.put("boolean", ConverterFinder.findConverter(boolean.class, false));
		optCache.put("bool", ConverterFinder.findConverter(boolean.class, false));

		optCache.put("null", new ObjectRef(null));
		optCache.put("true", new BooleanRef(true));
		optCache.put("false", new BooleanRef(false));
		optCache.put("!", NOT_FLAG);
		optCache.put("not", NOT_FLAG);

		optCache.put("|", new ExpLink(false, false, false));
		optCache.put("&", new ExpLink(false, false, true));
		ExpLink el;
		el = new ExpLink(true, false, false); // or
		optCache.put("||", el);
		optCache.put("or", el);
		el = new ExpLink(false, true, true); // and
		optCache.put("&&", el);
		optCache.put("and", el);

		optCache.put("isNull", new NullCheck(false));
		optCache.put("notNull", new NullCheck(true));

		EqualCheck ce = new EqualCheck(false);
		optCache.put("=", ce);
		optCache.put("==", ce);
		ce = new EqualCheck(true);
		optCache.put("!=", ce);
		optCache.put("<>", ce);

		optCache.put(">=", new CompareCheck(CompareCheck.LARGE_EQUAL));
		optCache.put(">", new CompareCheck(CompareCheck.LARGE_THEN));
		optCache.put("<=", new CompareCheck(CompareCheck.LOEWR_EQUAL));
		optCache.put("<", new CompareCheck(CompareCheck.LOEWR_THEN));

		optCache.put("hasNext", new HasNextCheck());

		try
		{
			grammerManager = new GrammerManager();
			grammerManager.init(CheckExpression.class.getClassLoader().getResource(
					"self/micromagic/eterna/model/grammer.xml").openStream());
			rootGE = grammerManager.getGrammerElement("expression_checker");
			if (rootGE == null)
			{
				AppData.log.error("Init check expression error, "
						+ "not found GrammerElement [expression_checker]");
			}
		}
		catch (Exception ex)
		{
			AppData.log.error("Init check expression error.", ex);
		}
	}

	/**
	 * 一个判断的操作.
	 */
	public interface CheckOpt
	{
		/**
		 * 执行判断操作.
		 */
		boolean doCheck(AppData data);

		/**
		 * 创建一个判断操作对象.
		 *
		 * @param arg1  第一个参数
		 * @param arg2  第二个参数
		 */
		CheckOpt create(Object arg1, Object arg2);

		/**
		 * 操作符前是否需要参数.
		 */
		boolean needArgBefore();

		/**
		 * 操作符后是否需要参数.
		 */
		boolean needArgAfter();

	}

}

/**
 * 一个判断表达式的判断单元.
 */
class ExpCell
{
	public ExpCell(CheckExpression.CheckOpt opt)
	{
		this.opt = opt;
	}
	private final CheckExpression.CheckOpt opt;

	public void setNot()
	{
		this.needNot = !this.needNot;
	}
	private boolean needNot;

	/**
	 * 获取此单元的判断结果.
	 */
	public boolean getResult(AppData data)
	{
		boolean r = this.opt.doCheck(data);
		if (this.next != null)
		{
			r = this.link.doLink(r, this.next, data);
		}
		return this.needNot ? !r : r;
	}

	/**
	 * 是否有下一个表达式.
	 */
	public boolean hasNext()
	{
		return this.next != null;
	}

	public void addNext(CheckExpression.CheckOpt opt, ExpLink link)
	{
		if (this.next == null)
		{
			this.next = new ExpCell(opt);
			this.link = link;
		}
		else
		{
			this.next.addNext(opt, link);
		}
	}
	public void addNext(ExpCell exp, ExpLink link)
	{
		if (this.next == null)
		{
			this.next = exp;
			this.link = link;
		}
		else
		{
			this.next.addNext(exp, link);
		}
	}
	private ExpLink link;
	private ExpCell next;

}

/**
 * 将一个ExpCell或boolean等包装成CheckOpt
 */
class WrapperOpt
		implements CheckExpression.CheckOpt
{
	public WrapperOpt(ExpCell cell)
	{
		this.cell = cell;
	}
	private ExpCell cell;

	public WrapperOpt(boolean result)
	{
		this.result = result;
	}
	private boolean result;

	public boolean doCheck(AppData data)
	{
		if (this.cell == null)
		{
			return this.result;
		}
		return this.cell.getResult(data);
	}

	public CheckExpression.CheckOpt create(Object arg1, Object arg2)
	{
		return this;
	}

	public boolean needArgBefore()
	{
		return false;
	}
	public boolean needArgAfter()
	{
		return false;
	}

}

/**
 * 表达式判断单元的连接操作.
 */
class ExpLink
{
	public ExpLink(boolean tr, boolean fr, boolean and_or)
	{
		this.trueReturn = tr;
		this.falseReturn = fr;
		this.and_or = and_or;
	}
	private final boolean trueReturn;
	private final boolean falseReturn;
	private final boolean and_or;

	/**
	 * 是否需要延迟连接.
	 */
	public boolean delayLink()
	{
		return !this.and_or;
	}

	public boolean doLink(boolean preResult, ExpCell next, AppData data)
	{
		if (preResult)
		{
			if (trueReturn)
			{
				return true;
			}
			return this.and_or ? preResult & next.getResult(data)
					: preResult | next.getResult(data);
		}
		else
		{
			if (falseReturn)
			{
				return false;
			}
			return this.and_or ? preResult & next.getResult(data)
					: preResult | next.getResult(data);
		}
	}

}

abstract class OneArgCheck
		implements CheckExpression.CheckOpt
{
	public boolean needArgBefore()
	{
		return true;
	}

	public boolean needArgAfter()
	{
		return false;
	}

	protected abstract OneArgCheck createCheck();

	public CheckExpression.CheckOpt create(Object arg1, Object arg2)
	{
		OneArgCheck check = this.createCheck();
		check.dataOpt = DataOpt.createDataOpt(arg1);
		return check;
	}
	protected DataOpt dataOpt;

	public boolean doCheck(AppData data)
	{
		return this.doCheck(this.dataOpt.getData(data));
	}

	public abstract boolean doCheck(Object v);
}

abstract class TwoArgCheck
		implements CheckExpression.CheckOpt
{
	public boolean needArgBefore()
	{
		return true;
	}

	public boolean needArgAfter()
	{
		return true;
	}

	protected abstract TwoArgCheck createCheck();

	public CheckExpression.CheckOpt create(Object arg1, Object arg2)
	{
		TwoArgCheck check = this.createCheck();
		check.dataOpt1 = DataOpt.createDataOpt(arg1);
		check.dataOpt2 = DataOpt.createDataOpt(arg2);
		return check;
	}
	protected DataOpt dataOpt1;
	protected DataOpt dataOpt2;

	public boolean doCheck(AppData data)
	{
		return this.doCheck(this.dataOpt1.getData(data), this.dataOpt2.getData(data));
	}

	public abstract boolean doCheck(Object v1, Object v2);

}

class HasNextCheck extends OneArgCheck
{
	protected OneArgCheck createCheck()
	{
		return new HasNextCheck();
	}

	public boolean doCheck(Object v)
	{
		if (v != null && v instanceof Iterator)
		{
			return ((Iterator) v).hasNext();
		}
		return false;
	}

}

class EqualCheck extends TwoArgCheck
{
	public EqualCheck(boolean not)
	{
		this.needNot = not;
	}
	private final boolean needNot;

	protected TwoArgCheck createCheck()
	{
		return new EqualCheck(this.needNot);
	}

	public boolean doCheck(Object v1, Object v2)
	{
		boolean r = Utility.objectEquals(v1, v2);
		return this.needNot ? !r : r;
	}

}

class CompareCheck extends TwoArgCheck
{
	public static final int LARGE_THEN = 1;
	public static final int LARGE_EQUAL = 2;
	public static final int LOEWR_THEN = -1;
	public static final int LOEWR_EQUAL = -2;
	public CompareCheck(int type)
	{
		this.type = type;
	}
	private final int type;

	protected TwoArgCheck createCheck()
	{
		return new CompareCheck(this.type);
	}

	public boolean doCheck(Object v1, Object v2)
	{
		if (v1 == null || v2 == null)
		{
			return false;
		}
		if (v1 instanceof Comparable)
		{
			int r = ((Comparable) v1).compareTo(v2);
			switch (this.type)
			{
				case LOEWR_EQUAL:
					return r <= 0;
				case LOEWR_THEN:
					return r < 0;
				case LARGE_THEN:
					return r > 0;
				case LARGE_EQUAL:
					return r >= 0;
			}
		}
		return false;
	}

}

class NullCheck extends OneArgCheck
{
	public NullCheck(boolean type)
	{
		this.notNull = type;
	}
	/**
	 * true 判断是否不为null, false 判断是否为null
	 */
	private final boolean notNull;

	protected OneArgCheck createCheck()
	{
		return new NullCheck(this.notNull);
	}

	public boolean doCheck(Object v)
	{
		return this.notNull ? v != null : v == null;
	}

}

/**
 * 一个数据处理的操作.
 */
class DataOpt
{
	public DataOpt(Object value, ValueConverter converter)
	{
		this.converter = converter;
		if (value instanceof DataHandler)
		{
			this.handler = (DataHandler) value;
		}
		else
		{
			this.value = value;
		}
	}
	public static DataOpt createDataOpt(Object value)
	{
		if (value instanceof DataOpt)
		{
			return (DataOpt) value;
		}
		return new DataOpt(value, null);
	}
	private DataHandler handler;
	private Object value;
	ValueConverter converter;

	/**
	 * 获取处理后的数据.
	 */
	public Object getData(AppData data)
	{
		Object v = this.value;
		if (this.handler != null)
		{
			v = this.handler.getData(data, false);
		}
		return this.converter == null ? v : this.converter.convert(v);
	}

}
