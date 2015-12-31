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

import java.io.StringReader;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.dom4j.Element;

import self.micromagic.cg.BeanTool;
import self.micromagic.cg.ClassGenerator;
import self.micromagic.eterna.dao.ResultRow;
import self.micromagic.eterna.share.EternaException;
import self.micromagic.expression.DynamicObject;
import self.micromagic.expression.ExprTool;
import self.micromagic.expression.Expression;
import self.micromagic.expression.antlr.ExprLexer;
import self.micromagic.expression.antlr.ExprParser;
import self.micromagic.expression.antlr.ExprTokenTypes;
import self.micromagic.util.StringAppender;
import self.micromagic.util.StringTool;
import self.micromagic.util.Utility;
import self.micromagic.util.container.ValueContainerMap;
import self.micromagic.util.ref.BooleanRef;
import self.micromagic.util.ref.StringRef;
import antlr.collections.AST;

/**
 * 数据处理者.
 */
public class DataHandler
		implements ExprTokenTypes, DynamicObject
{
	/**
	 * 配置中设置是否需要在日志中记录原始值的标识.
	 */
	public static final String LOG_OLD_VALUE_FLAG = "eterna.data.logOldValue";
	/**
	 * 配置中设置是否需要在日志中记录变量值变更信息的标识.
	 */
	public static final String LOG_VAR_CHANGED_FLAG = "eterna.data.logVarChanged";

	/**
	 * 无操作.
	 */
	private static final int OPT_TYPE_NONE = 0;
	/**
	 * 修改操作.
	 */
	private static final int OPT_TYPE_MODIFY = 1;
	/**
	 * 移除操作.
	 */
	private static final int OPT_TYPE_DELETE = 3;

	/**
	 * null的标识对象.
	 */
	public static final Object NULL_FLAG = new Object();

	/**
	 * 读取数据的配置.
	 */
	private String config;


	/**
	 * AppData中的map的获取工具.
	 */
	private MapGetter mapGetter;

	/**
	 * 读取/设置map中数据的名称.
	 */
	private Object[] subs;

	/**
	 * 从定义的变量中读取/设置数据.
	 */
	private VarCache.VarInfo varInfo;

	/**
	 * 在一个表达式对象中读取/设置数据.
	 */
	private Object exprObj;


	private boolean needMapDataName = true;
	private boolean readOnly = true;
	private String caption = "config";

	/**
	 * 是否需要在日志中记录原始值.
	 */
	private static boolean logOldValue;
	/**
	 * 是否需要在日志中记录变量值的变更.
	 */
	private static boolean logVarChanged;

	static
	{
		try
		{
			Utility.addFieldPropertyManager(LOG_OLD_VALUE_FLAG,
					DataHandler.class, "logOldValue");
			Utility.addFieldPropertyManager(LOG_VAR_CHANGED_FLAG,
					DataHandler.class, "logVarChanged");
		}
		catch (Exception ex) {}
	}

	/**
	 * @param caption           显示错误信息是使用的标题
	 * @param needMapDataName   当读取map数据时, 是否需要给出数据的名称
	 *                          当readOnly的值为false时, 此值会被强制设为true
	 * @param readOnly          是否为只读方式
	 */
	public DataHandler(String caption, boolean needMapDataName, boolean readOnly)
	{
		this.caption = caption;
		this.readOnly = readOnly;
		this.needMapDataName = !readOnly || needMapDataName;
	}

	/**
	 * 清空已有的配置
	 */
	private void clearConfig()
	{
		this.config = null;
		this.mapGetter = null;
		this.subs = null;
		this.exprObj = null;
		this.varInfo = null;
	}

	/**
	 * 获取处理配置.
	 */
	public String getConfig()
	{
		return this.config;
	}

	/**
	 * 判断给出的名称是否为有效的变量名主名称.
	 */
	public static boolean isValidMainName(String name)
	{
		if (name == null)
		{
			return false;
		}
		if (mapNameIndex.containsKey(name))
		{
			return true;
		}
		return name.startsWith("$");
	}

	/**
	 * 通过语法分析的节点设置配置.
	 */
	public void setConfig(AST node)
	{
		if (node.getType() != VAR)
		{
			throw new EternaException("The node's type isn't [VAR].");
		}
		if (this.config == null)
		{
			this.clearConfig();
			this.config = node.toStringTree();
		}
		List subs = new ArrayList();
		String mainName = this.parseConfig(node, subs);

		if (this.exprObj != null)
		{
			// 使用表达式对象
			if (!ExprTool.isConstObject(this.exprObj))
			{
				// 不是静态对象, 需要有个变量暂存结果
				this.varInfo = this.getVarCache().getVarInfo("$", false, null);
			}
			this.subs = subs.toArray(new Object[subs.size()]);
			return;
		}
		Object tObj = mapNameIndex.get(mainName);
		if (tObj != null)
		{
			this.mapGetter = (MapGetter) tObj;
		}
		if (this.mapGetter != null)
		{
			if (!subs.isEmpty())
			{
				this.subs = subs.toArray(new Object[subs.size()]);
				return;
			}
			if (!this.needMapDataName)
			{
				// 子名称未设置并且不是必需时, 则退出不用抛出异常.
				return;
			}
		}
		else if (mainName.startsWith("$"))
		{
			// 变量定义
			StringRef err = new StringRef(null);
			this.varInfo = this.getVarCache().getVarInfo(mainName, !this.readOnly, err);
			if (err.getString() != null)
			{
				AppData.log.error("Error " + this.caption + " [" + this.config + "], msg: "
						+ err.getString());
			}
			if (!subs.isEmpty())
			{
				this.subs = subs.toArray(new Object[subs.size()]);
			}
			return;
		}
		throw new EternaException("Error " + this.caption + " main name [" + mainName + "].");
	}

	/**
	 * 设置处理配置.
	 */
	public void setConfig(String config)
			throws EternaException
	{
		this.clearConfig();
		this.config = config;
		ExprLexer lex = new ExprLexer(new StringReader(config));
		ExprParser parser = new ExprParser(lex);
		try
		{
			parser.identVar();
		}
		catch (RuntimeException ex)
		{
			throw ex;
		}
		catch (Exception ex)
		{
			throw new EternaException(ex);
		}
		this.setConfig(parser.getAST());
	}

	private VarCache getVarCache()
	{
		AppData data = AppData.getCurrentData();
		if (data.varCache == null)
		{
			data.varCache = new VarCache();
		}
		return data.varCache;
	}

	public Object getResult(AppData data)
	{
		return this.getData(data, false);
	}

	/**
	 * 根据处理配置读取数据.
	 *
	 * @param data       AppData对象, 可从中获取数据
	 * @param remove     获取数据后, 是否将源头的数据移除
	 */
	public Object getData(AppData data, boolean remove)
			throws EternaException
	{
		int optType = remove ? OPT_TYPE_DELETE : OPT_TYPE_NONE;
		Object value = null;
		if (this.mapGetter != null)
		{
			Map tmpMap = this.mapGetter.getMap(data);
			if (this.subs != null)
			{
				if (this.subs.length == 1)
				{
					Object sub = getExprValue(this.subs[0], data, null);
					value = tmpMap.get(sub);
					if (remove)
					{
						tmpMap.remove(sub);
					}
				}
				else
				{
					value = this.dealValue(tmpMap, this.subs, data, optType, null);
				}
			}
			else
			{
				value = tmpMap;
			}
		}
		else if (this.exprObj != null)
		{
			Object tmp = this.getExprObj(data);
			value = this.dealValue(tmp, this.subs, data, optType, null);
		}
		else if (this.varInfo != null)
		{
			if (this.subs != null)
			{
				Object tmp = this.varInfo.getValue(data);
				value = this.dealValue(tmp, this.subs, data, optType, null);
			}
			else
			{
				value = this.varInfo.getValue(data);
				if (remove)
				{
					this.varInfo.setValue(data, null);
				}
			}
		}
		return value;
	}

	/**
	 * 根据处理配置设置数据.
	 *
	 * @return 原始数据
	 */
	public Object setData(AppData data, Object value)
			throws EternaException
	{
		if (this.readOnly)
		{
			throw new EternaException("The [" + this.caption + "] is read only, can't be setted.");
		}
		if (this.mapGetter != null)
		{
			Map tmpMap = this.mapGetter.getMap(data);
			if (this.subs.length == 1)
			{
				Object sub = getExprValue(this.subs[0], data, null);
				return tmpMap.put(sub, value);
			}
			else
			{
				return this.dealValue(tmpMap, this.subs, data, OPT_TYPE_MODIFY, value);
			}
		}
		else if (this.exprObj != null)
		{
			Object tmp = this.getExprObj(data);
			return this.dealValue(tmp, this.subs, data, OPT_TYPE_MODIFY, value);
		}
		else if (this.varInfo != null)
		{
			if (this.subs != null)
			{
				Object tmp = this.varInfo.getValue(data);
				return this.dealValue(tmp, this.subs, data, OPT_TYPE_MODIFY, value);
			}
			else
			{
				Object old = this.varInfo.getValue(data);
				this.varInfo.setValue(data, value);
				if (logVarChanged && data.getLogType() > 0)
				{
					Element nowNode = data.getCurrentNode();
					if (nowNode != null)
					{
						Element vNode = nowNode.addElement("set-var-value");
						vNode.addAttribute("name", this.varInfo.getName());
						if (logOldValue)
						{
							AppDataLogExecute.printObject(vNode.addElement("new-value"), value);
							AppDataLogExecute.printObject(vNode.addElement("old-value"), old);
						}
						else
						{
							AppDataLogExecute.printObject(vNode, value);
						}
					}
				}
				return old;
			}
		}
		else
		{
			return null;
		}
	}

	/**
	 * 深度处理map/array/list类型对象中的值.
	 *
	 * @param obj     需要处理的对象
	 * @param subs    需要处理的子名称列表
	 * @param remove  是否需要将原来的值移除
	 * @param newVal  需要设置的新值
	 * @return  obj中的原始值
	 */
	private Object dealValue(Object obj, Object[] subs, AppData data, int optType, Object newVal)
	{
		int dCount = subs.length;
		if (newVal != null || optType > OPT_TYPE_NONE)
		{
			dCount--;
		}
		Object currentVal = obj;
		for (int i = 0; i < dCount; i++)
		{
			if (currentVal == null)
			{
				return null;
			}
			BooleanRef isInt = new BooleanRef();
			Object sub = getExprValue(subs[i], data, isInt);
			Object nextValue;
			if (isInt.value)
			{
				if (currentVal instanceof List)
				{
					int index = ((Number) sub).intValue();
					List list = (List) currentVal;
					nextValue = list.size() > index ? list.get(index) : null;
				}
				else if (currentVal instanceof Collection)
				{
					int count = ((Number) sub).intValue();
					Iterator itr = ((Collection) currentVal).iterator();
					for (int j = 1; j < count; j++)
					{
						itr.next();
					}
					nextValue = itr.next();
				}
				else if (currentVal instanceof CharSequence)
				{
					int index = ((Number) sub).intValue();
					nextValue = new Character(((CharSequence) currentVal).charAt(index));
				}
				else if (ClassGenerator.isArray(currentVal.getClass()))
				{
					nextValue = Array.get(currentVal, ((Number) sub).intValue());
				}
				else
				{
					// 当前值不是list或array无法处理
					throw new EternaException(
							"The value type [" + obj.getClass() + "] can't trans to list or array.");
				}
			}
			else
			{
				nextValue = tran2Map(currentVal).get(sub);
			}
			if (nextValue == null && optType == OPT_TYPE_MODIFY)
			{
				BooleanRef nextInt = new BooleanRef();
				// 判断下一个对象的类型
				getExprValue(subs[i + 1], data, nextInt);
				// 如果是赋值操作, 需要生成对象
				if (nextInt.value)
				{
					nextValue = new ArrayList();
				}
				else
				{
					nextValue = new HashMap();
				}
				this.modifyValue(currentVal, sub, isInt.value, optType, nextValue);
			}
			currentVal = nextValue;
		}
		if (dCount == subs.length || currentVal == null)
		{
			// 如果没有特殊处理, 将当前值返回
			return currentVal;
		}
		BooleanRef isInt = new BooleanRef();
		Object sub = getExprValue(subs[subs.length - 1], data, isInt);
		return this.modifyValue(currentVal, sub, isInt.value, optType, newVal);
	}

	/**
	 * 修改对象中的值.
	 */
	private Object modifyValue(Object obj, Object sub, boolean isInt, int optType, Object newVal)
	{
		if (isInt)
		{
			int index = ((Number) sub).intValue();
			if (obj instanceof List)
			{
				List list = (List) obj;
				int size = list.size();
				if (size < 0 || size <= index)
				{
					if (size >= 0)
					{
						int count = index - list.size();
						for (int i = 0; i < count; i++)
						{
							list.add(null);
						}
					}
					list.add(newVal);
					return null;
				}
				else if (optType == OPT_TYPE_DELETE)
				{
					return list.remove(index);
				}
				else
				{
					return list.set(index, newVal);
				}
			}
			else if (ClassGenerator.isArray(obj.getClass()))
			{
				Object old = Array.get(obj, index);
				Array.set(obj, index, newVal);
				return old;
			}
			else
			{
				// 当前值不是list或array无法处理
				throw new EternaException(
						"The value type [" + obj.getClass() + "] can't trans to list or array.");
			}
		}
		else
		{
			Map tmpMap = tran2Map(obj);
			// 进行特殊处理, 移除或设置新值.
			if (optType == OPT_TYPE_DELETE)
			{
				return tmpMap.remove(sub);
			}
			else
			{
				return tmpMap.put(sub, newVal);
			}
		}
	}

	/**
	 * 将对象转换为map.
	 */
	private static Map tran2Map(Object obj)
	{
		if (obj instanceof Map)
		{
			return (Map) obj;
		}
		if (obj instanceof ResultRow)
		{
			return ValueContainerMap.createResultRowMap((ResultRow) obj);
		}
		if (BeanTool.checkBean(obj.getClass()))
		{
			return BeanTool.getBeanMap(obj);
		}
		throw new EternaException("The value type [" + obj.getClass() + "] can't trans to map.");
	}

	/**
	 * 从表达式对象中获取数据.
	 */
	private Object getExprObj(AppData data)
	{
		if (this.varInfo == null)
		{
			// 没有定义变量, 说明是个常数
			return this.exprObj;
		}
		Object tmp = this.varInfo.getValue(data);
		if (tmp == null)
		{
			tmp = getExprValue(this.exprObj, data, null);
			if (tmp == null)
			{
				tmp = NULL_FLAG;
			}
			this.varInfo.setValue(data, tmp);
		}
		return tmp == NULL_FLAG ? null : tmp;
	}

	/**
	 * 获取表达式对象的值.
	 */
	private static Object getExprValue(Object expr, AppData data, BooleanRef isInt)
	{
		Object r = null;
		if (expr instanceof DynamicObject)
		{
			r = ((DynamicObject) expr).getResult(data);
		}
		else
		{
			r = expr;
		}
		if (isInt != null)
		{
			// isInt不为null, 表示需要整型或字符串
			if (r instanceof Number)
			{
				isInt.value = true;
			}
			else if (r != null)
			{
				r = r.toString();
			}
		}
		return r;
	}

	/**
	 * 解析配置的语法分析节点.
	 */
	protected String parseConfig(AST node, List subs)
			throws EternaException
	{
		AST first = node.getFirstChild();
		String mainName = null;
		if (first.getType() == EXPR)
		{
			if (first.getNextSibling() == null)
			{
				throw new EternaException("Not found sub after expression.");
			}
			this.exprObj = this.parseNode(first);
		}
		else
		{
			mainName = first.getText();
		}
		AST tmp = first.getNextSibling();
		while (tmp != null)
		{
			if (tmp.getType() == DOT)
			{
				tmp = tmp.getNextSibling();
				subs.add(tmp.getText());
			}
			else if (tmp.getType() == LBRACK)
			{
				tmp = tmp.getNextSibling();
				subs.add(this.parseNode(tmp));
			}
			else
			{
				throw new EternaException("Error node type in data handler [" + node.getText() + "].");
			}
			tmp = tmp.getNextSibling();
		}
		return mainName;
	}
	/**
	 * 解析表达式节点对象.
	 */
	protected Object parseNode(AST node)
	{
		Object obj = ExprTool.parseExpNode(node);
		if (obj != null)
		{
			if (obj instanceof Number)
			{
				if (!(obj instanceof Integer))
				{
					obj = Utility.createInteger(((Number) obj).intValue());
				}
			}
			else if (obj instanceof Expression)
			{
				obj = ((Expression) obj).tryGetResult(null);
			}
			else if (!(obj instanceof DataHandler))
			{
				obj = obj.toString();
			}
		}
		return obj;
	}

	/**
	 * 将语法节点转换为字符串.
	 */
	private String transNode2Str(AST node)
	{
		StringAppender buf = StringTool.createStringAppender(32);
		AST first = node.getFirstChild();
		if (first.getType() == EXPR)
		{
			buf.append(first.toStringTree());
		}
		else
		{
			buf.append(first.getText());
		}
		AST tmp = first.getNextSibling();
		while (tmp != null)
		{
			if (tmp.getType() == DOT)
			{
				tmp = tmp.getNextSibling();
				buf.append('.');
				this.appendSub(tmp, buf);
			}
			else if (tmp.getType() == LBRACK)
			{
				tmp = tmp.getNextSibling();
				buf.append('[');
				this.appendSub(tmp, buf);
				buf.append(']');
			}
			tmp = tmp.getNextSibling();
		}
		return buf.toString();
	}
	private void appendSub(AST sub, StringAppender buf)
	{
		if (sub.getType() == VAR)
		{
			buf.append(this.transNode2Str(sub));
		}
		else
		{
			AST tmp = sub.getFirstChild();
			if (tmp != null)
			{
				buf.append(tmp.toStringList());
			}
		}
	}

	/**
	 * 注册一个AppData中各类map的获取工具.
	 *
	 * @param name    map的标识
	 * @param getter  map的获取工具
	 * @return  是否覆盖了现有的注册, 如果已有注册则返回true
	 */
	public static boolean registerMapGetter(String name, MapGetter getter)
	{
		if (name != null && getter != null)
		{
			return mapNameIndex.put(name, getter) != null;
		}
		return false;
	}

	/**
	 * 存放名称与AppData中map索引值的对应表.
	 */
	private static final Map mapNameIndex = new HashMap();

	static
	{
		mapNameIndex.put(AppData.REQUEST_PARAMETER_MAP_NAME, new BaseMapGetter(AppData.REQUEST_PARAMETER_MAP));
		mapNameIndex.put(AppData.REQUEST_ATTRIBUTE_MAP_NAME, new BaseMapGetter(AppData.REQUEST_ATTRIBUTE_MAP));
		mapNameIndex.put(AppData.SESSION_ATTRIBUTE_MAP_NAME, new BaseMapGetter(AppData.SESSION_ATTRIBUTE_MAP));
		mapNameIndex.put(AppData.DATA_MAP_NAME, new BaseMapGetter(AppData.DATA_MAP));
		mapNameIndex.put("param", new BaseMapGetter(AppData.REQUEST_PARAMETER_MAP));
		mapNameIndex.put("attr", new BaseMapGetter(AppData.REQUEST_ATTRIBUTE_MAP));
		mapNameIndex.put("session", new BaseMapGetter(AppData.SESSION_ATTRIBUTE_MAP));
		mapNameIndex.put("RP", new BaseMapGetter(AppData.REQUEST_PARAMETER_MAP));
		mapNameIndex.put("RA", new BaseMapGetter(AppData.REQUEST_ATTRIBUTE_MAP));
		mapNameIndex.put("SA", new BaseMapGetter(AppData.SESSION_ATTRIBUTE_MAP));
		mapNameIndex.put("header", new HeaderGetter());
		mapNameIndex.put("cookie", new CookieGetter());
	}

}