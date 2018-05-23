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

package self.micromagic.eterna.search.impl;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import self.micromagic.cg.ClassGenerator;
import self.micromagic.eterna.dao.preparer.PreparerManager;
import self.micromagic.eterna.dao.preparer.ValuePreparer;
import self.micromagic.eterna.model.AppData;
import self.micromagic.eterna.model.DataHandler;
import self.micromagic.eterna.search.BuilderResult;
import self.micromagic.eterna.search.ConditionBuilder;
import self.micromagic.eterna.search.ConditionInfo;
import self.micromagic.eterna.search.ConditionProperty;
import self.micromagic.eterna.search.Search;
import self.micromagic.eterna.search.SearchAttributes;
import self.micromagic.eterna.search.SearchManager;
import self.micromagic.eterna.search.SearchManagerGenerator;
import self.micromagic.eterna.search.SearchParam;
import self.micromagic.eterna.share.AbstractGenerator;
import self.micromagic.eterna.share.EternaException;
import self.micromagic.eterna.share.EternaFactory;
import self.micromagic.eterna.view.DataPrinter;
import self.micromagic.util.StringAppender;
import self.micromagic.util.StringTool;
import self.micromagic.util.ref.StringRef;

/**
 * @author micromagic@sina.com
 */
public class SearchManagerImpl extends AbstractGenerator
		implements SearchManagerGenerator, SearchManager, DataPrinter.BeanPrinter
{
	static final String SEARCHMANAGER_DEALED_PREFIX = "ETERNA_SEARCHMANAGER_DEALED:";

	private transient SearchAttributes searchAttributes;

	private transient String preparedCondition = "";
	private transient PreparerManager generatedPM = null;
	private transient Map conditionMap = null;
	private List conditionList = null;
	private transient Search resetConditionSearch = null;
	private transient Map specialMap = null;

	private int conditionVersion = 1;
	private int pageNum = 0;
	private int pageSize = -1;

	public SearchManagerImpl()
	{
		this.searchAttributes = DEFAULT_ATTRIBUTES;
	}

	protected SearchManagerImpl(SearchAttributes attrs, EternaFactory factory)
	{
		this.searchAttributes = attrs;
		this.factory = factory;
	}

	public boolean initialize(EternaFactory factory)
	{
		if (this.searchAttributes == DEFAULT_ATTRIBUTES)
		{
			String attrs = (String) factory.getAttribute(EternaFactory.SEARCH_ATTRIBUTES_FLAG);
			if (attrs != null)
			{
				Map attrMap = StringTool.string2Map(attrs, ",", ':', true, false, null, null);
				this.searchAttributes = new SearchAttributes(attrMap);
			}
			else
			{
				EternaFactory shareFactory = factory.getShareFactory();
				if (shareFactory != null)
				{
					SearchAttributes tmp = shareFactory.getSearchAttributes();
					if (tmp != null)
					{
						this.searchAttributes = tmp;
					}
				}
			}
			return false;
		}
		return true;
	}

	public int getPageNum()
	{
		return this.pageNum;
	}

	public int getPageSize(int defaultSize)
	{
		return this.pageSize == -1 ? defaultSize : this.pageSize;
	}

	public String getOrderConfig(AppData data)
	{
		return data.getRequestParameter(this.searchAttributes.orderConfigTag);
	}

	public boolean hasQueryType(AppData data)
	{
		String temp;
		Map raMap = data.getRequestAttributeMap();
		if (raMap.get(FORCE_DEAL_CONDITION) != null)
		{
			temp = this.searchAttributes.queryTypeReset;
		}
		else
		{
			temp = (String) raMap.get(FORCE_QUERY_TYPE);
			if (temp == null)
			{
				temp = data.getRequestParameter(this.searchAttributes.queryTypeTag);
			}
			if (temp == null)
			{
				temp = this.searchAttributes.defaultQueryType;
			}
		}
		return this.searchAttributes.queryTypeReset.equals(temp)
				|| this.searchAttributes.queryTypeClear.equals(temp);
	}

	public int getConditionVersion()
	{
		return this.conditionVersion;
	}

	public void setPageNumAndCondition(AppData data, Search search)
			throws EternaException
	{
		this.setPageNumAndCondition(data, search, true);
	}

	private void setPageNumAndCondition(AppData data, Search search, boolean checkDealed)
			throws EternaException
	{
		Map raMap = data.getRequestAttributeMap();
		if (checkDealed)
		{
			String checkName = SEARCHMANAGER_DEALED_PREFIX + search.getSearchManagerName();
			if (raMap.get(checkName) != null)
			{
				if (raMap.get(FORCE_DEAL_CONDITION) == null && raMap.get(FORCE_QUERY_TYPE) == null)
				{
					return;
				}
			}
			raMap.put(checkName, "1");
		}
		SearchParam sParam = (SearchParam) data.getRequestAttributeMap().get(
				Search.ATTR_SEARCH_PARAM);
		if (sParam != null)
		{
			if (sParam.pageNum >= this.searchAttributes.pageStart)
			{
				this.pageNum = sParam.pageNum - this.searchAttributes.pageStart;
			}
			if (sParam.pageSize > 0)
			{
				this.pageSize = sParam.pageSize > MAX_PAGE_SIZE ?
						MAX_PAGE_SIZE : sParam.pageSize;
			}
			if (sParam.allRow)
			{
				raMap.put(Search.READ_ALL_ROW, "1");
			}
			boolean saveCondition = raMap.get(SAVE_CONDITION) != null || search.isSpecialCondition();
			this.setConditionValues(sParam, search, saveCondition);
			// 设置了条件对象, 处理完后直接返回
			return;
		}
		try
		{
			String pn = data.getRequestParameter(this.searchAttributes.pageNumTag);
			if (pn != null)
			{
				this.pageNum = Integer.parseInt(pn) - this.searchAttributes.pageStart;
			}
		}
		catch (Exception ex) {}
		try
		{
			String ps = data.getRequestParameter(this.searchAttributes.pageSizeTag);
			if (ps != null)
			{
				int tmpI = Integer.parseInt(ps);
				this.pageSize = tmpI < 0 ? -1 : tmpI > MAX_PAGE_SIZE ? MAX_PAGE_SIZE : tmpI;
			}
		}
		catch (Exception ex) {}
		try
		{
			String temp;
			if (raMap.get(FORCE_DEAL_CONDITION) != null)
			{
				temp = this.searchAttributes.queryTypeReset;
			}
			else
			{
				temp = (String) raMap.get(FORCE_QUERY_TYPE);
				if (temp == null)
				{
					temp = data.getRequestParameter(this.searchAttributes.queryTypeTag);
				}
				if (temp == null)
				{
					temp = this.searchAttributes.defaultQueryType;
				}
			}
			if (this.searchAttributes.queryTypeReset.equals(temp))
			{
				this.conditionVersion++;
				this.resetConditionSearch = search;
				String xml = data.getRequestParameter(this.searchAttributes.querySettingTag);
				if (log.isDebugEnabled())
				{
					log.debug("The xml:" + xml);
				}
				boolean saveCondition = raMap.get(SAVE_CONDITION) != null || search.isSpecialCondition();
				this.clearCondition();
				if (xml != null)
				{
					Document doc = DocumentHelper.parseText(xml);
					this.setConditionValues(doc, search, saveCondition);
				}
				else
				{
					this.setConditionValues(data, search, saveCondition, false);
				}
			}
			else if (this.searchAttributes.queryTypeClear.equals(temp))
			{
				this.conditionVersion++;
				this.preparedCondition = null;
				this.generatedPM = null;
				this.clearCondition();
			}
			else if (this.conditionVersion == 1)
			{
				// version为1, 表示是第一次进入且未标记要设置条件
				this.conditionVersion++;
				boolean saveCondition = raMap.get(SAVE_CONDITION) != null || search.isSpecialCondition();
				this.setConditionValues(data, search, saveCondition, true);
			}
		}
		catch (Exception ex)
		{
			log.error("When set condition values.", ex);
		}
	}

	/**
	 * 根据参数的名称从<code>doc</code>中的条件配置，设置查询条件。
	 */
	private void setConditionValues(Document doc, Search search, boolean saveCondition)
			throws EternaException
	{
		List conditions = doc.getRootElement().element("conditions").elements();
		this.setConditionValues(this.makeConditionStruct(conditions),
				search, saveCondition);
	}
	private Object[] makeConditionStruct(List conditions)
	{
		List result = new ArrayList();
		Iterator itr = conditions.iterator();
		Element item;
		while (itr.hasNext())
		{
			item = (Element) itr.next();
			if ("conditions".equals(item.getName()))
			{
				result.add(this.makeConditionStruct(item.elements()));
			}
			else
			{
				ConditionInfo info = new ConditionInfo(item.attributeValue("name"),
						item.attributeValue("linkOpt"), item.attributeValue("value"),
						item.attributeValue("builder"));
				result.add(info);
			}
		}
		return result.toArray();
	}

	/**
	 * 构造查询的条件管理者.
	 */
	private void makePreparerManager(List preparerList)
	{
		int pSize = preparerList.size();
		if (pSize > 0)
		{
			PreparerManager tmpPM = new PreparerManager(pSize);
			Iterator itr = preparerList.iterator();
			for (int i = 0; i < pSize; i++)
			{
				ValuePreparer preparer = (ValuePreparer) itr.next();
				preparer.setRelativeIndex(i + 1);
				tmpPM.setValuePreparer(preparer);
			}
			this.generatedPM = tmpPM;
		}
		else
		{
			this.generatedPM = null;
		}
	}

	/**
	 * 根据特殊的条件格式设置查询条件。
	 */
	private void setConditionValues(Object[] conditionStruct, Search search, boolean saveCondition)
			throws EternaException
	{
		StringRef tmpOpt = new StringRef();
		List preparerList = new LinkedList();
		String tmpScript = this.makeConditionScript(conditionStruct,
				saveCondition, search, preparerList, tmpOpt);
		if (tmpScript == null)
		{
			this.preparedCondition = "";
			this.generatedPM = null;
			return;
		}
		if (tmpOpt.getString() != null && tmpOpt.getString().endsWith("NOT"))
		{
			tmpScript = "NOT ".concat(tmpScript);
		}
		this.preparedCondition = tmpScript;
		if (log.isDebugEnabled())
		{
			log.debug("Condition:" + tmpScript);
		}
		this.makePreparerManager(preparerList);
	}
	private String makeConditionScript(Object[] conditionStruct, boolean saveCondition,
			Search search, List preparerList, StringRef firstLink)
	{
		if (conditionStruct == null || conditionStruct.length == 0)
		{
			return null;
		}
		StringAppender buf = StringTool.createStringAppender(128);
		for (int i = 0; i < conditionStruct.length; i++)
		{
			Object obj = conditionStruct[i];
			if (obj == null)
			{
				continue;
			}
			if (obj instanceof ConditionInfo)
			{
				ConditionInfo info = (ConditionInfo) obj;
				ConditionProperty cp = search.getConditionProperty(info.name);
				if (cp != null)
				{
					ConditionBuilder cb;
					if (cp.isUseDefaultConditionBuilder() || StringTool.isEmpty(info.builderName))
					{
						cb = cp.getDefaultConditionBuilder();
					}
					else
					{
						cb = search.getFactory().getConditionBuilder(info.builderName);
					}
					BuilderResult cbCon = null;
					try
					{
						cbCon = cb.buildeCondition(cp.getColumnName(), info.value, cp);
						if (saveCondition)
						{
							this.addCondition(new ConditionInfo(cp.getName(), info.linkOpt, info.value, cb));
						}
					}
					catch (Exception ex)
					{
						String msg = "Error in builde condition [" + cp.getName()
								+ "] at search [" + search.getName() + "].";
						log.error(msg, ex);
					}
					if (log.isDebugEnabled())
					{
						log.debug("OneCondition:" + (cbCon != null ? cbCon.toString() : null));
					}
					if (cbCon != null)
					{
						if (buf.length() == 0)
						{
							firstLink.setString(info.linkOpt);
							buf.append('(');
						}
						else
						{
							buf.append(' ').append(info.linkOpt).append(' ');
						}
						buf.append(cbCon.scriptPart);
						for (int pIndex = 0; pIndex < cbCon.preparers.length; pIndex++)
						{
							cbCon.preparers[pIndex].setName(cp.getName());
						}
						preparerList.addAll(Arrays.asList(cbCon.preparers));
					}
				}
				else
				{
					String msg = "Not found condition [" + info.name
							+ "] in search [" + search.getName() + "].";
					log.error(msg);
				}
			}
			else if (ClassGenerator.isArray(obj.getClass()))
			{
				StringRef tmpOpt = new StringRef();
				String tmpScript = this.makeConditionScript((Object[]) obj,
						saveCondition, search, preparerList, tmpOpt);
				if (tmpScript != null)
				{
					if (buf.length() == 0)
					{
						firstLink.setString(tmpOpt.getString());
						buf.append('(');
					}
					else
					{
						buf.append(' ').append(tmpOpt.getString()).append(' ');
					}
					buf.append(tmpScript);
				}
			}
			else
			{
				throw new EternaException("Error type: " + obj.getClass() + ".");
			}
		}
		if (buf.length() == 0)
		{
			return null;
		}
		else
		{
			buf.append(')');
			return buf.toString();
		}
	}

	/**
	 * 根据参数对象, 设置查询条件.
	 */
	private void setConditionValues(SearchParam param, Search search, boolean saveCondition)
			throws EternaException
	{
		if (param.queryType == null)
		{
			param.queryType = this.searchAttributes.defaultQueryType;
		}
		if (this.searchAttributes.queryTypeReset.equals(param.queryType))
		{
			this.conditionVersion++;
			this.resetConditionSearch = search;
			this.setConditionValues0(param, search, saveCondition);
		}
		else if (this.searchAttributes.queryTypeClear.equals(param.queryType))
		{
			this.conditionVersion++;
			this.preparedCondition = null;
			this.generatedPM = null;
			this.clearCondition();
		}
		else if (this.conditionVersion == 1)
		{
			// version为1, 表示是第一次进入且未标记要设置条件
			this.conditionVersion++;
			this.setConditionValues0(param, search, saveCondition);
		}
	}
	private void setConditionValues0(SearchParam param, Search search, boolean saveCondition)
			throws EternaException
	{
		if (param.condition == null && param.conditionStruct == null)
		{
			// 如果条件都为null, 需要构造一个空的map
			param.condition = Collections.EMPTY_MAP;
		}
		if (param.conditionStruct != null)
		{
			this.setConditionValues(param.conditionStruct, search, saveCondition);
			return;
		}
		StringAppender buf = StringTool.createStringAppender(512);

		List preparerList = new LinkedList();
		int index = 0;
		int count = search.getConditionPropertyCount();
		for (int i = 0; i < count; i++)
		{
			// 这里不捕获异常, 因为在方法外已经捕获了
			ConditionProperty cp = search.getConditionProperty(i);
			Object value = param.condition.get(cp.getName());
			if (!cp.isIgnore() && !(param.skipEmpty ? StringTool.isEmpty(value) : value == null))
			{
				ConditionBuilder cb = cp.getDefaultConditionBuilder();
				BuilderResult cbCon = null;
				try
				{
					cbCon = cb.buildeCondition(cp.getColumnName(), value, cp);
					if (saveCondition)
					{
						this.addCondition(new ConditionInfo(cp.getName(), "and", value, cb));
					}
				}
				catch (Exception ex)
				{
					String msg = "Error in builde condition [" + cp.getName()
							+ "] at search [" + search.getName() + "].";
					log.error(msg, ex);
				}
				if (log.isDebugEnabled())
				{
					log.debug("OneCondition:" + (cbCon != null ? cbCon.toString() : null));
				}
				if (cbCon != null)
				{
					if (index != 0)
					{
						buf.append(" AND ");
					}
					else
					{
						buf.append('(');
					}
					buf.append(cbCon.scriptPart);
					for (int pIndex = 0; pIndex < cbCon.preparers.length; pIndex++)
					{
						cbCon.preparers[pIndex].setName(cp.getName());
					}
					preparerList.addAll(Arrays.asList(cbCon.preparers));
					index++;
				}
			}
		}
		if (buf.length() > 1)
		{
			buf.append(')');
			this.preparedCondition = buf.toString();
		}
		else
		{
			this.preparedCondition = "";
		}
		if (log.isDebugEnabled())
		{
			log.debug("Condition:" + buf.toString());
		}
		this.makePreparerManager(preparerList);
	}

	/**
	 * 根据参数的名称从<code>request</code>中的读取参数，设置查询条件。
	 */
	private void setConditionValues(AppData data, Search search, boolean saveCondition, boolean dealDefault)
			throws EternaException
	{
		StringAppender buf = StringTool.createStringAppender(512);

		List preparerList = new LinkedList();
		int index = 0;
		int count = search.getConditionPropertyCount();
		for (int i = 0; i < count; i++)
		{
			// 这里不捕获异常, 因为在方法外已经捕获了
			ConditionProperty cp = search.getConditionProperty(i);
			Object value;
			if (dealDefault)
			{
				value = cp.getDefaultValue();
				if (value != null)
				{
					if (value instanceof DataHandler)
					{
						value = ((DataHandler) value).getData(data, false);
					}
				}
			}
			else
			{
				value = data.getRequestParameter(cp.getName());
			}
			if (!cp.isIgnore() && !StringTool.isEmpty(value))
			{
				ConditionBuilder cb = cp.getDefaultConditionBuilder();
				BuilderResult cbCon = null;
				try
				{
					cbCon = cb.buildeCondition(cp.getColumnName(), value, cp);
					if (saveCondition)
					{
						this.addCondition(new ConditionInfo(cp.getName(), "and", value, cb));
					}
				}
				catch (Exception ex)
				{
					String msg = "Error in builde condition [" + cp.getName()
							+ "] at search [" + search.getName() + "].";
					log.error(msg, ex);
				}
				if (log.isDebugEnabled())
				{
					log.debug("OneCondition:" + (cbCon != null ? cbCon.toString() : null));
				}
				if (cbCon != null)
				{
					if (index != 0)
					{
						buf.append(" AND ");
					}
					else
					{
						buf.append('(');
					}
					buf.append(cbCon.scriptPart);
					for (int pIndex = 0; pIndex < cbCon.preparers.length; pIndex++)
					{
						cbCon.preparers[pIndex].setName(cp.getName());
					}
					preparerList.addAll(Arrays.asList(cbCon.preparers));
					index++;
				}
			}
		}
		if (buf.length() > 1)
		{
			buf.append(')');
			this.preparedCondition = buf.toString();
		}
		else
		{
			this.preparedCondition = "";
		}
		if (log.isDebugEnabled())
		{
			log.debug("Condition:" + buf.toString());
		}
		this.makePreparerManager(preparerList);
	}

	public PreparerManager getPreparerManager()
	{
		return this.generatedPM;
	}

	public PreparerManager getSpecialPreparerManager(Search search)
			throws EternaException
	{
		if (search == this.resetConditionSearch)
		{
			return this.getPreparerManager();
		}
		if (this.specialMap != null)
		{
			Object[] temp = (Object[]) this.specialMap.get(search.getName());
			if (temp != null)
			{
				return (PreparerManager) temp[1];
			}
		}
		return (PreparerManager) this.createSpecialCondition(search)[1];
	}

	public String getConditionPart()
	{
		if (this.preparedCondition == null)
		{
			this.preparedCondition = "";
		}
		return this.preparedCondition;
	}

	public String getConditionPart(boolean needWrap)
	{
		String r = this.getConditionPart();
		return needWrap || r.length() == 0 ? r : r.substring(1, r.length() - 1);
	}

	public String getSpecialConditionPart(Search search)
			throws EternaException
	{
		if (search == this.resetConditionSearch)
		{
			return this.getConditionPart();
		}
		if (this.specialMap != null)
		{
			Object[] temp = (Object[]) this.specialMap.get(search.getName());
			if (temp != null)
			{
				return (String) temp[0];
			}
		}
		return (String) this.createSpecialCondition(search)[0];
	}

	public String getSpecialConditionPart(Search search, boolean needWrap)
			throws EternaException
	{
		if (search == this.resetConditionSearch)
		{
			return this.getConditionPart(needWrap);
		}
		String r = this.getSpecialConditionPart(search);
		return needWrap || r.length() == 0 ? r : r.substring(1, r.length() - 1);
	}


	private Map prepareCondition(Search search)
			throws EternaException
	{
		List cons = this.getConditions();
		if (cons.isEmpty())
		{
			return null;
		}
		Map result = new HashMap();
		Iterator itr = cons.iterator();
		while (itr.hasNext())
		{
			ConditionInfo con = (ConditionInfo) itr.next();
			if (result.containsKey(con.name))
			{
				continue;
			}
			ConditionProperty cp = search.getConditionProperty(con.name);
			if (cp != null && !cp.isIgnore())
			{
				result.put(con.name, new Object[]{cp, con.value});
			}
		}
		return result;
	}

	/**
	 * 生成条件子集，返回值为2个，第一个为ConditionPart，第二个为PreparerManager.
	 */
	private Object[] createSpecialCondition(Search search)
			throws EternaException
	{
		Map consMap = this.prepareCondition(search);
		if (consMap == null || consMap.isEmpty())
		{
			return new Object[]{"", null};
		}
		StringAppender buf = StringTool.createStringAppender(512);
		List preparerList = new LinkedList();
		Iterator conItr = consMap.values().iterator();
		Object value;
		while (conItr.hasNext())
		{
			int index = 0;
			Object[] pair = (Object[]) conItr.next();
			if (pair != null)
			{
				ConditionProperty cp = (ConditionProperty) pair[0];
				value = pair[1];
				ConditionBuilder cb = cp.getDefaultConditionBuilder();
				BuilderResult cbCon = null;
				try
				{
					cbCon = cb.buildeCondition(cp.getColumnName(), value, cp);
				}
				catch (Exception ex)
				{
					String msg = "Error when builde condition: ConditionProperty["
							+ cp.getName() + "], search[" + search.getName() + "].";
					log.error(msg, ex);
				}
				if (log.isDebugEnabled())
				{
					log.debug("OneCondition:" + cbCon.toString());
				}
				if (cbCon != null)
				{
					if (index != 0)
					{
						buf.append(" AND ");
					}
					else
					{
						buf.append("(");
					}
					buf.append(cbCon.scriptPart);
					for (int pIndex = 0; pIndex < cbCon.preparers.length; pIndex++)
					{
						cbCon.preparers[pIndex].setName(cp.getName());
					}
					preparerList.addAll(Arrays.asList(cbCon.preparers));
					index++;
				}
			}
		}
		if (buf.length() > 0)
		{
			buf.append(" )");
		}
		String pCon = buf.toString();
		if (log.isDebugEnabled())
		{
			log.debug("Condition:" + buf.toString());
		}
		int pSize = preparerList.size();
		PreparerManager gPM = null;
		if (pSize > 0)
		{
			gPM = new PreparerManager(pSize);
			Iterator itr = preparerList.iterator();
			for (int i = 0; i < pSize; i++)
			{
				ValuePreparer preparer = (ValuePreparer) itr.next();
				preparer.setRelativeIndex(i + 1);
				gPM.setValuePreparer(preparer);
			}
		}
		Object[] result = new Object[]{pCon, gPM};
		if (this.specialMap == null)
		{
			this.specialMap = new HashMap();
		}
		this.specialMap.put(search.getName(), result);
		return result;
	}

	public SearchAttributes getSearchAttributes()
	{
		return this.searchAttributes;
	}

	public void setSearchAttributes(SearchAttributes attributes)
	{
		this.searchAttributes = attributes == null ? DEFAULT_ATTRIBUTES : attributes;
	}

	public ConditionInfo getCondition(String name)
	{
		if (this.conditionMap == null)
		{
			return null;
		}
		List list = (List) this.conditionMap.get(name);
		if (list == null)
		{
			return null;
		}
		return (ConditionInfo) list.get(0);
	}

	public List getConditions(String name)
	{
		if (this.conditionMap == null)
		{
			return null;
		}
		List list = (List) this.conditionMap.get(name);
		if (list == null)
		{
			return null;
		}
		return Collections.unmodifiableList(list);
	}

	public List getConditions()
	{
		if (this.conditionList == null)
		{
			return Collections.EMPTY_LIST;
		}
		return Collections.unmodifiableList(this.conditionList);
	}

	private void clearCondition()
	{
		this.specialMap = null;
		if (this.conditionList != null)
		{
			this.conditionList.clear();
			this.conditionMap.clear();
		}
	}

	private void addCondition(ConditionInfo condition)
	{
		if (this.conditionList == null)
		{
			this.conditionList = new LinkedList();
			this.conditionMap = new HashMap();
		}
		this.conditionList.add(condition);
		ArrayList conditions = (ArrayList) this.conditionMap.get(condition.name);
		if (conditions == null)
		{
			conditions = new ArrayList(2);
			this.conditionMap.put(condition.name, conditions);
		}
		conditions.add(condition);
	}

	public Object create()
			throws EternaException
	{
		return this.createSearchManager((EternaFactory) this.factory);
	}

	public SearchManager createSearchManager(EternaFactory factory)
			throws EternaException
	{
		return new SearchManagerImpl(this.searchAttributes, factory);
	}

	public void print(DataPrinter p, Writer out, Object bean)
			throws IOException, EternaException
	{
		Iterator itr = this.getConditions().iterator();
		boolean firstValue = true;
		p.printObjectBegin(out);
		while (itr.hasNext())
		{
			ConditionInfo con = (ConditionInfo) itr.next();
			if (con.value != null)
			{
				p.printPair(out, con.name, con.value, firstValue);
				firstValue = false;
			}
		}
		p.printObjectEnd(out);
	}

}
