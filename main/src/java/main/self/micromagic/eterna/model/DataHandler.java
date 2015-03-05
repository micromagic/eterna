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

package self.micromagic.eterna.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import self.micromagic.cg.BeanTool;
import self.micromagic.eterna.share.EternaException;
import self.micromagic.grammer.GrammerElement;
import self.micromagic.grammer.GrammerException;
import self.micromagic.grammer.GrammerManager;
import self.micromagic.grammer.ParserData;
import self.micromagic.grammer.ParserData.GrammerCell;
import self.micromagic.util.StringAppender;
import self.micromagic.util.StringTool;
import self.micromagic.util.ref.StringRef;

/**
 * 数据处理者.
 */
public class DataHandler
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
	private String[] subNames;

	/**
	 * 从定义的变量中读取/设置数据.
	 */
	private VarCache.VarInfo varInfo;

	/**
	 * 读取的常量数据的值.
	 */
	private String constValue = null;

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
		this.subNames = null;
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
	 * 设置处理配置.
	 */
	public void setConfig(String config)
			throws EternaException
	{
		this.clearConfig();
		this.config = config;
		List subs = new ArrayList();
		String mainName = this.parseConfig(config, subs);

		String[] subNames = null;
		int subCount = subs.size();
		if (subCount > 0)
		{
			subNames = new String[subCount];
			subs.toArray(subNames);
		}

		Object tObj = mapNameIndex.get(mainName);
		if (tObj != null)
		{
			this.mapGetter = (MapGetter) tObj;
		}
		if (this.mapGetter != null)
		{
			if (subNames != null)
			{
				this.subNames = subNames;
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
				AppData.log.error("Error " + this.caption + " [" + config + "], msg: "
						+ err.getString());
			}
			if (subNames != null)
			{
				this.subNames = subNames;
			}
			return;
		}
		else if (subNames == null)
		{
			// 没有子名称, 默认是对data数据集的操作
			this.mapGetter = (MapGetter) mapNameIndex.get("data");
			this.subNames = new String[]{mainName};
		}
		if (this.readOnly)
		{
			if ("value".equals(mainName))
			{
				if (subNames != null && subNames.length == 1)
				{
					this.constValue = subNames[0];
					return;
				}
			}
		}

		throw new EternaException("Error " + this.caption + " [" + config + "].");
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
		if (this.constValue != null)
		{
			value = this.constValue;
		}
		else if (this.mapGetter != null)
		{
			Map tmpMap = this.mapGetter.getMap(data);
			if (this.subNames != null)
			{
				if (this.subNames.length == 1)
				{
					value = tmpMap.get(this.subNames[0]);
					if (remove)
					{
						tmpMap.remove(this.subNames[0]);
					}
				}
				else
				{
					value = this.dealMap(tmpMap, this.subNames, remove, null);
				}
			}
			else
			{
				value = tmpMap;
			}
		}
		else if (this.varInfo != null)
		{
			if (this.subNames != null)
			{
				value = this.dealMap(this.varInfo.getValue(data),
						this.subNames, remove, null);
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
			if (this.subNames.length == 1)
			{
				if (value == null)
				{
					tmpMap.remove(this.subNames[0]);
				}
				else
				{
					tmpMap.put(this.subNames[0], value);
				}
			}
			else
			{
				this.dealMap(tmpMap, this.subNames, true, value);
			}
		}
		else if (this.varInfo != null)
		{
			if (this.subNames != null)
			{
				this.dealMap(this.varInfo.getValue(data), this.subNames, true, value);
			}
			else
			{
				this.varInfo.setValue(data, value);
			}
		}
	}

	/**
	 * 深度处理map类型对象中的值.
	 *
	 * @param map     需要处理的map类型的对象
	 * @param subs    需要处理的子名称列表
	 * @param remove  是否需要将原来的值移除
	 * @param newVal  需要设置的新值
	 * @return  map中的原始值
	 */
	private Object dealMap(Object map, String[] subs, boolean remove, Object newVal)
	{
		int dCount = subs.length;
		if (newVal != null || remove)
		{
			dCount--;
		}
		Object currentVal = map;
		for (int i = 0; i < dCount; i++)
		{
			if (currentVal == null)
			{
				return null;
			}
			if (currentVal instanceof Map)
			{
				currentVal = ((Map) currentVal).get(subs[i]);
			}
			else if (BeanTool.checkBean(currentVal.getClass()))
			{
				currentVal = BeanTool.getBeanMap(currentVal).get(subs[i]);
			}
			else
			{
				// 当前值不是map无法处理
				return null;
			}
		}
		if (dCount == subs.length)
		{
			// 如果没有特殊处理, 将当前值返回
			return currentVal;
		}
		Map tmpMap;
		if (currentVal instanceof Map)
		{
			tmpMap = (Map) currentVal;
		}
		else if (BeanTool.checkBean(currentVal.getClass()))
		{
			tmpMap = BeanTool.getBeanMap(currentVal);
		}
		else
		{
			// 当前值不是map无法处理
			return null;
		}
		// 进行特殊处理, 移除或设置新值.
		if (newVal != null)
		{
			return tmpMap.put(subs[subs.length - 1], newVal);
		}
		else
		{
			return tmpMap.remove(subs[subs.length - 1]);
		}
	}

	/**
	 * 解析一个配置表达式, 并将主名称返回.
	 *
	 * @param subs  添加子名称的列表
	 */
	public String parseConfig(String config, List subs)
			throws EternaException
	{
		GrammerElement ge = rootGE;
		ParserData pd = new ParserData(config);
		try
		{
			if (!ge.verify(pd))
			{
				throw new EternaException("Error " + this.caption + " config [" + config + "].");
			}
		}
		catch (GrammerException ex)
		{
			throw new EternaException(ex);
		}
		List cells = pd.getGrammerCellLst();
		// refName的词法结构会被解析在子节点中
		Iterator itr = ((GrammerCell) cells.get(0)).subCells.iterator();
		GrammerCell cell = (GrammerCell) itr.next();
		String mainName = cell.textBuf;
		while (itr.hasNext())
		{
			cell = (GrammerCell) itr.next();
			// 第二个词法节点为名称节点, 可直接作为子名称
			GrammerCell secondCell = (GrammerCell) cell.subCells.get(1);
			if (secondCell.grammerElement.getType() == GrammerElement.TYPE_NAME)
			{
				subs.add(secondCell.textBuf);
			}
			else
			{
				subs.add(this.getStrValue(cell));
			}
		}
		return mainName;
	}

	/**
	 * 从词法列表中获取定义的字符串.
	 */
	public String getStrValue(GrammerCell subCell)
	{
		StringAppender buf = null;
		Iterator itr = subCell.subCells.iterator();
		while (itr.hasNext())
		{
			GrammerCell cell = (GrammerCell) itr.next();
			if ("\"".equals(cell.textBuf))
			{
				if (buf == null)
				{
					buf = StringTool.createStringAppender();
				}
				else
				{
					// 如果buf不为空, 说明是第二个引号, 返回字符串
					return buf.toString();
				}
			}
			else if (buf != null)
			{
				// 如果buf已存在说明是处理字符串中间的部分
				buf.append((String) Expression.getValue(cell));
			}
		}
		throw new EternaException("Error " + this.caption + " sub "
				+ subCell.textBuf + ".");
	}

	/**
	 * 存放名称与AppData中map索引值的对应表.
	 */
	private static final Map mapNameIndex = new HashMap();

	private static GrammerManager grammerManager;
	private static GrammerElement rootGE;

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

		try
		{
			grammerManager = new GrammerManager();
			grammerManager.init(DataHandler.class.getClassLoader().getResource(
					"self/micromagic/eterna/model/grammer.xml").openStream());
			rootGE = grammerManager.getGrammerElement("refName");
			if (rootGE == null)
			{
				AppData.log.error("Init check expression error, "
						+ "not found GrammerElement [refName]");
			}
		}
		catch (Exception ex)
		{
			AppData.log.error("Init data config expression error.", ex);
		}
	}

}