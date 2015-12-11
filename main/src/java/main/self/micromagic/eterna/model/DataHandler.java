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

import self.micromagic.cg.BeanTool;
import self.micromagic.cg.ClassGenerator;
import self.micromagic.eterna.share.EternaException;
import self.micromagic.expression.ExpTool;
import self.micromagic.expression.Expression;
import self.micromagic.expression.antlr.ExpLexer;
import self.micromagic.expression.antlr.ExpParser;
import self.micromagic.expression.antlr.ExpTokenTypes;
import self.micromagic.util.StringAppender;
import self.micromagic.util.StringTool;
import self.micromagic.util.Utility;
import self.micromagic.util.ref.BooleanRef;
import self.micromagic.util.ref.StringRef;
import antlr.collections.AST;

/**
 * 数据处理者.
 */
public class DataHandler
		implements ExpTokenTypes
{
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


	private boolean needMapDataName = true;
	private boolean readOnly = true;
	private String caption = "config";

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
		ExpLexer lex = new ExpLexer(new StringReader(config));
		ExpParser parser = new ExpParser(lex);
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

	/**
	 * 根据处理配置读取数据.
	 *
	 * @param data       AppData对象, 可从中获取数据
	 * @param remove     获取数据后, 是否将源头的数据移除
	 */
	public Object getData(AppData data, boolean remove)
			throws EternaException
	{
		Object value = null;
		if (this.mapGetter != null)
		{
			Map tmpMap = this.mapGetter.getMap(data);
			if (this.subs != null)
			{
				if (this.subs.length == 1)
				{
					value = tmpMap.get(this.subs[0]);
					if (remove)
					{
						tmpMap.remove(this.subs[0]);
					}
				}
				else
				{
					value = this.dealValue(tmpMap, this.subs, data, remove, null);
				}
			}
			else
			{
				value = tmpMap;
			}
		}
		else if (this.varInfo != null)
		{
			if (this.subs != null)
			{
				value = this.dealValue(this.varInfo.getValue(data),
						this.subs, data, remove, null);
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
	 * 根据处理配置设置数据
	 */
	public void setData(AppData data, Object value)
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
				if (value == null)
				{
					tmpMap.remove(this.subs[0]);
				}
				else
				{
					tmpMap.put(this.subs[0], value);
				}
			}
			else
			{
				this.dealValue(tmpMap, this.subs, data, true, value);
			}
		}
		else if (this.varInfo != null)
		{
			if (this.subs != null)
			{
				this.dealValue(this.varInfo.getValue(data), this.subs, data, true, value);
			}
			else
			{
				this.varInfo.setValue(data, value);
			}
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
	private Object dealValue(Object obj, Object[] subs, AppData data, boolean remove, Object newVal)
	{
		int dCount = subs.length;
		if (newVal != null || remove)
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
			Object sub = this.getSubValue(subs[i], data, isInt);
			if (isInt.value)
			{
				if (currentVal instanceof List)
				{
					currentVal = ((List) currentVal).get(((Number) sub).intValue());
				}
				else if (currentVal instanceof Collection)
				{
					int count = ((Number) sub).intValue();
					Iterator itr = ((Collection) currentVal).iterator();
					for (int j = 1; j < count; j++)
					{
						itr.next();
					}
					currentVal = itr.next();
				}
				else if (ClassGenerator.isArray(currentVal.getClass()))
				{
					currentVal = Array.get(currentVal, ((Number) sub).intValue());
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
				currentVal = tran2Map(currentVal);
			}
		}
		if (dCount == subs.length)
		{
			// 如果没有特殊处理, 将当前值返回
			return currentVal;
		}
		BooleanRef isInt = new BooleanRef();
		Object sub = this.getSubValue(subs[subs.length - 1], data, isInt);
		return this.modifyValue(currentVal, sub, isInt.value, newVal);
	}

	/**
	 * 修改对象中的值.
	 */
	private Object modifyValue(Object obj, Object sub, boolean isInt, Object newVal)
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
			if (newVal != null)
			{
				return tmpMap.put(sub, newVal);
			}
			else
			{
				return tmpMap.remove(sub);
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
		if (BeanTool.checkBean(obj.getClass()))
		{
			return BeanTool.getBeanMap(obj);
		}
		throw new EternaException("The value type [" + obj.getClass() + "] can't trans to map.");
	}

	/**
	 * 获取子名称列表的值.
	 */
	private Object getSubValue(Object sub, AppData data, BooleanRef isInt)
	{
		Object r = null;
		if (sub instanceof DataHandler)
		{
			r = ((DataHandler) sub).getData(data, false);
		}
		else if (sub instanceof Expression)
		{
			r = ((Expression) sub).getResult(data);
		}
		else
		{
			r = sub;
		}
		if (r instanceof Number)
		{
			isInt.value = true;
			if (!(r instanceof Integer))
			{
				r = Utility.createInteger(((Number) r).intValue());
			}
		}
		else if (r != null)
		{
			r = r.toString();
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
		String mainName = first.getText();
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
				Object obj = ExpTool.parseExpNode(tmp);
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
				subs.add(obj);
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
	 * 将语法节点转换为字符串.
	 */
	private String transNode2Str(AST node)
	{
		StringAppender buf = StringTool.createStringAppender(32);
		AST first = node.getFirstChild();
		buf.append(first.getText());
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