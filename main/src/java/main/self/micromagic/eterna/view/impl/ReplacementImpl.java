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

package self.micromagic.eterna.view.impl;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import self.micromagic.eterna.share.EternaException;
import self.micromagic.eterna.model.AppData;
import self.micromagic.eterna.share.EternaFactory;
import self.micromagic.eterna.view.Component;
import self.micromagic.eterna.view.ModifiableViewRes;
import self.micromagic.eterna.view.Replacement;
import self.micromagic.eterna.view.ReplacementGenerator;
import self.micromagic.eterna.view.View;
import self.micromagic.util.container.MultiIterator;
import self.micromagic.util.container.UnmodifiableIterator;
import self.micromagic.util.container.PreFetchIterator;
import self.micromagic.util.StringRef;
import self.micromagic.util.IntegerRef;
import self.micromagic.util.StringTool;

/**
 * @author micromagic@sina.com
 */
public class ReplacementImpl extends ComponentImpl
		implements Replacement, ReplacementGenerator
{
	protected boolean ignoreGlobalSetted = false;
	protected String baseComponentName;
	protected Component baseComponent;

	/**
	 * 得直接匹配控件的映射表.
	 */
	protected Map directMatchMap;

	/**
	 * 是否直接引用baseComponent.
	 */
	protected boolean linkTypical = true;

	/**
	 * 是否检查需要直接引用baseComponent.
	 */
	protected boolean checkLinkTypical;

	/**
	 * 是否定义了特殊的事件.
	 */
	protected boolean hasSpecialEvent;

	/**
	 * 是否是个外覆的Replacement.
	 */
	protected boolean wrap;

	/**
	 * 基础控件是否为Replacement.
	 */
	protected boolean baseReplacement;

	private ModifiableViewRes viewRes;
	private List replacedList;

	public void initialize(EternaFactory factory, Component parent)
			throws EternaException
	{
		if (this.initialized)
		{
			return;
		}
		super.initialize(factory, parent);
		boolean topReplace = true;
		String tmpComName = this.baseComponentName;
		if (tmpComName != null)
		{
			this.baseComponent = this.findBaseComponent(tmpComName, factory);
			if (this.directMatchMap != null)
			{
				Iterator subComponentItr = this.componentList.iterator();
				while (subComponentItr.hasNext())
				{
					Component sub = (Component) subComponentItr.next();
					ReplacementInfo rInfo = (ReplacementInfo) this.directMatchMap.get(sub.getName());
					if (rInfo != null)
					{
						if (rInfo.base == null)
						{
							rInfo.base = sub;
							subComponentItr.remove();
						}
						else
						{
							throw new EternaException("The direct match component \""
									+ sub.getName() + "\" appeared more than once.");
						}
					}
				}
			}
		}
		else
		{
			if (parent == null || !(parent instanceof Replacement))
			{
				throw new EternaException("Top replacement must set base component.");
			}
			topReplace = false;
			this.directMatchMap = ((Replacement) parent).getDirectMatchMap();
		}
		if (topReplace)
		{
			this.initReplace(factory, this.baseComponent, null);
		}
	}

	public void initReplace(EternaFactory factory, Component base, Replacement parent)
			throws EternaException
	{
		if (base != null)
		{
			this.initBase(factory, base);
		}
		else
		{
			// parent不为null, 表示这是一个占位节点, 先要将这个占位节点替换到相应位置
			parent.replaceComponent(factory, this);
		}
		if (this.baseReplacement)
		{
			// 当base为一个Replacement时
			if (this.linkTypical)
			{
				// 如果是直接引用baseComponent, 清空replacedList
				this.replacedList = null;
			}
			return;
		}

		if (!this.linkTypical || this.checkLinkTypical)
		{
			// 替换需要替换的Component
			Iterator subComponentItr = this.componentList.iterator();
			while (subComponentItr.hasNext())
			{
				Component sub = (Component) subComponentItr.next();
				if (sub instanceof Replacement)
				{
					Replacement myReplace = (Replacement) sub;
					if (myReplace.getBaseComponent() == null)
					{
						myReplace.initReplace(factory, null, this);
					}
					else
					{
						this.replaceComponent(factory, sub);
					}
				}
				else
				{
					this.replaceComponent(factory, sub);
				}
			}

			// 将其它的节点替换成replacement
			this.dealAutoWrap(factory);
		}
	}

	public void initBase(EternaFactory factory, Component base)
			throws EternaException
	{
		if (this.baseComponent == null)
		{
			if (this.isReplacement(base))
			{
				// base为一个Replacement时, 需要标识出来
				this.baseReplacement = true;
			}
			else
			{
				this.wrap = true;
			}
			this.baseComponent = this.unWrapReplacement(base);
		}

		this.hasSpecialEvent = this.eventList.size() > 0;
		this.linkTypical = this.componentParam == null && this.beforeInit == null
				&& this.initScript == null && this.componentList.size() == 0
				&& !this.ignoreGlobalSetted;
		this.checkLinkTypical = this.linkTypical && this.directMatchMap != null;

		base = this.getPrimaryComponent(this.baseComponent, null);
		if ((!this.linkTypical || this.checkLinkTypical) && this.replacedList == null)
		{
			Iterator itr = base.getSubComponents();
			this.replacedList = new LinkedList();
			while (itr.hasNext())
			{
				this.replacedList.add(itr.next());
			}
		}
		if (this.linkTypical)
		{
			this.ignoreGlobalParam = base.isIgnoreGlobalParam();
			return;
		}

		if (this.componentParam == null)
		{
			this.componentParam = base.getComponentParam();
		}
		if (!this.ignoreGlobalSetted)
		{
			this.ignoreGlobalParam = base.isIgnoreGlobalParam();
		}

		String parentScript = base.getBeforeInit();
		this.beforeInit = ViewTool.addParentScript(this.beforeInit, parentScript);

		parentScript = base.getInitScript();
		this.initScript = ViewTool.addParentScript(this.initScript, parentScript);
	}

	public void replaceComponent(EternaFactory factory, Component newReplace)
			throws EternaException
	{
		ListIterator itr = this.replacedList.listIterator();
		while (itr.hasNext())
		{
			Component com = (Component) itr.next();
			if (com.getParent() != this && com.getName().equals(newReplace.getName()))
			{
				// 如果parent不为本控件, 且名称相同表示是需要替换的
				if (newReplace instanceof Replacement)
				{
					Replacement myReplace = (Replacement) newReplace;
					if (myReplace.getBaseComponent() == null)
					{
						myReplace.initBase(factory, com);
					}
					itr.set(newReplace);
				}
				else
				{
					itr.set(newReplace);
				}
				break;
			}
		}
	}

	public Map getDirectMatchMap()
	{
		return this.directMatchMap;
	}

	public Component getBaseComponent()
	{
		return this.baseComponent;
	}

	/**
	 * 对剩余节点自动打包
	 */
	private void dealAutoWrap(EternaFactory factory)
			throws EternaException
	{
		// 将其它的节点替换掉
		ListIterator itr = this.replacedList.listIterator();
		while (itr.hasNext())
		{
			Component sub = (Component) itr.next();
			String name = sub.getName();
			if (sub.getParent() != this)
			{
				if (this.directMatchMap != null)
				{
					// 根据直接替换节点来替换
					ReplacementInfo rInfo = (ReplacementInfo) this.directMatchMap.get(name);
					if (rInfo != null && rInfo.base != null && rInfo.canReplace())
					{
						boolean replaced = false;
						if (rInfo.base instanceof Replacement)
						{
							Replacement myReplace = (Replacement) rInfo.base;
							if (myReplace.getBaseComponent() == null)
							{
								myReplace.initReplace(factory, sub, null);
								itr.set(myReplace);
								replaced = true;
							}
						}
						if (!replaced)
						{
							ReplacementImpl ri = new ReplacementImpl();
							ri.setName(name);
							ri.initialize(factory, this);
							ri.initBase(factory, rInfo.base);
							if (ri.linkTypical)
							{
								ri.replacedList = null;
							}
							itr.set(ri);
						}
						// 有节点替换, 需要将linkTypical设为false
						this.changeLinkTypical();
						continue;
					}
				}
				ReplacementImpl ri = new ReplacementImpl();
				ri.setName(name);
				ri.initialize(factory, this);
				ri.initReplace(factory, sub, null);
				itr.set(ri);
			}
		}

		if (this.linkTypical)
		{
			// linkTypical为true, 说明无节点替换, 清空replacedList
			this.replacedList = null;
		}
		else
		{
			// linkTypical为false, 说明有节点替换, 需要复制BaseComponent的属性
			if (this.getParent() instanceof ReplacementImpl)
			{
				// 有节点替换, 且父节点为ReplacementImpl, 则将父节点的linkTypical设为false
				ReplacementImpl pri = (ReplacementImpl) this.getParent();
				if (pri.linkTypical)
				{
					pri.changeLinkTypical();
				}
			}
		}
	}

	/**
	 * 解开autoWrap(自动包裹的)控件, 对于typical-replacement会碰到已经打包的
	 */
	private Component unWrapReplacement(Component com)
	{
		while (com instanceof ReplacementImpl)
		{
			ReplacementImpl tmp = (ReplacementImpl) com;
			if (!tmp.linkTypical || tmp.hasSpecialEvent || tmp.getBaseComponent() == null)
			{
				break;
			}
			com = tmp.getBaseComponent();
		}
		return com;
	}

	/**
	 * 检查一个控件是否为Replacement.
	 * 对于直接外覆的控件, 需要解包后再判断.
	 */
	private boolean isReplacement(Component com)
	{
		while (com instanceof ReplacementImpl)
		{
			ReplacementImpl tmp = (ReplacementImpl) com;
			if (!tmp.wrap)
			{
				return true;
			}
			com = tmp.getBaseComponent();
		}
		return com instanceof Replacement;
	}

	/**
	 * 当直接引用baseComponent时, 将其转换为非直接引用, 并初始化变量.
	 */
	private void changeLinkTypical()
			throws EternaException
	{
		Component base = this.getBaseComponent();
		if (this.linkTypical && base != null)
		{
			this.linkTypical = false;
			this.componentParam = base.getComponentParam();
			this.beforeInit = base.getBeforeInit();
			this.initScript = base.getInitScript();
		}
	}

	/**
	 * 获得原始的控件，如果给了eventList参数，也会把event添加进去
	 */
	private Component getPrimaryComponent(Component com, List eventList)
	{
		while (com instanceof ReplacementImpl)
		{
			ReplacementImpl tmp = (ReplacementImpl) com;
			if (!tmp.linkTypical || tmp.getBaseComponent() == null)
			{
				break;
			}
			if (eventList != null && tmp.hasSpecialEvent)
			{
				ListIterator litr = tmp.eventList.listIterator(tmp.eventList.size());
				while (litr.hasPrevious())
				{
					eventList.add(0, litr.previous());
				}
			}
			com = tmp.getBaseComponent();
		}
		return com;
	}

	public void printBody(Writer out, AppData data, View view)
			throws IOException, EternaException
	{
		super.printBody(out, data, view);
	}

	public void printSpecialBody(Writer out, AppData data, View view)
			throws IOException, EternaException
	{
		if (this.linkTypical)
		{
			String idName = ViewTool.createTypicalComponentName(data, this.baseComponent);
			out.write(",typicalComponent:\"");
			this.stringCoder.toJsonString(out, idName);
			out.write('"');
		}
		else if (this.baseComponent != null)
		{
			this.getPrimaryComponent(this.baseComponent, null).printSpecialBody(out, data, view);
		}
	}

	public void setBaseComponentName(String name)
			throws EternaException
	{
		this.baseComponentName = name;
		if (name != null)
		{
			int index = name.indexOf(';');
			if (index == -1)
			{
				return;
			}
			this.baseComponentName = name.substring(0, index);
			String temp = name.substring(index + 1);
			List tmpList = new LinkedList();
			String[] names = StringTool.separateString(temp, ",", true);
			for (int i = 0; i < names.length; i++)
			{
				if (names[i].length() == 0)
				{
					continue;
				}
				tmpList.add(names[i]);
			}
			if (tmpList.size() > 0)
			{
				this.directMatchMap = new HashMap();
				Iterator itr = tmpList.iterator();
				while (itr.hasNext())
				{
					String tmpName = (String) itr.next();
					int tmpI = tmpName.indexOf(':');
					ReplacementInfo rInfo;
					int tmpIndex = -1;
					if (tmpI == -1)
					{
						rInfo = new ReplacementInfo(tmpName);
					}
					else
					{
						tmpIndex = Integer.parseInt(tmpName.substring(tmpI + 1));
						rInfo = new ReplacementInfo(tmpName.substring(0, tmpI),tmpIndex);
					}
					rInfo = (ReplacementInfo) this.directMatchMap.put(rInfo.name, rInfo);
					if (rInfo != null && rInfo.addIndex(tmpIndex))
					{
						throw new EternaException("The name \"" + tmpName
								+ "\" appeared more than once in config:[" + name + "].");
					}
				}
			}
		}
	}

	public void setIgnoreGlobalParam(boolean ignore)
			throws EternaException
	{
		this.ignoreGlobalSetted = true;
		super.setIgnoreGlobalParam(ignore);
	}

	public String getType()
			throws EternaException
	{
		if (!this.linkTypical && this.baseComponent != null)
		{
			return this.getPrimaryComponent(this.baseComponent, null).getType();
		}
		return "replacement";
	}

	public void setType(String type) {}

	public Iterator getSubComponents()
	{
		if (this.replacedList == null)
		{
			return UnmodifiableIterator.EMPTY_ITERATOR;
		}
		return new PreFetchIterator(this.replacedList.iterator(), false);
	}

	public Iterator getEvents()
			throws EternaException
	{
		if (this.baseComponent == null || this.linkTypical)
		{
			return super.getEvents();
		}
		List tmp = new LinkedList();
		Component tmpCom = this.getPrimaryComponent(this.baseComponent, tmp);
		Iterator tmpItr = new MultiIterator(tmpCom.getEvents(), tmp.iterator());
		return new MultiIterator(tmpItr, super.getEvents());
	}

	/**
	 * 根据名称表达式, 查找基础控件对象.
	 *
	 * @param nameExp  要查找控件的名称表达式
	 */
	protected Component findBaseComponent(String nameExp, EternaFactory factory)
			throws EternaException
	{
		if (nameExp == null)
		{
			return null;
		}
		StringRef subName = new StringRef();
		IntegerRef subIndex = new IntegerRef(1);
		Component tmpCom;
		if (nameExp.startsWith("view:"))
		{
			String bName = this.parseBaseName(nameExp.substring(5), subName, subIndex);
			if (bName == null)
			{
				throw new EternaException("Error base name expression [" + nameExp + "].");
			}
			View view = factory.createView(bName);
			tmpCom = new ViewWrapComponent(view);
		}
		else
		{
			String bName;
			if (nameExp.startsWith("typical:"))
			{
				bName = this.parseBaseName(nameExp.substring(8), subName, subIndex);
			}
			else
			{
				bName = this.parseBaseName(nameExp, subName, subIndex);
			}
			if (bName == null)
			{
				throw new EternaException("Error base name expression [" + nameExp + "].");
			}
			tmpCom = this.unWrapReplacement(factory.getTypicalComponent(bName));
			if (tmpCom == null)
			{
				throw new EternaException("The Typical Component [" + nameExp + "] not found.");
			}
		}
		// 这里要先对baseComponent初始化, 因为后面需要初始化好的baseComponent
		// 因此要求Component的初始化方法要有已初始化标记, 判断是否需要执行初始化
		tmpCom.initialize(factory, null);
		if (subName.getString() != null && subName.getString().length() > 0)
		{
			tmpCom = this.findSubComponent(tmpCom, subName.getString(), subIndex.value, new IntegerRef());
			if (tmpCom == null)
			{
				throw new EternaException("The Typical Component [" + nameExp + "] not found.");
			}
		}
		return tmpCom;
	}

	/**
	 * 根据名称及索引值查找一个子控件对象.
	 */
	private Component findSubComponent(Component root, String name, int index, IntegerRef nowIndex)
			throws EternaException
	{
		Iterator itr = root.getSubComponents();
		while (itr.hasNext())
		{
			Component sub = (Component) itr.next();
			if (!this.isReplacement(sub))
			{
				// 当不是一个Replacement控件(即非外覆控件), 需要解包
				sub = this.unWrapReplacement(sub);
			}
			if (name.equals(sub.getName()))
			{
				nowIndex.value++;
				if (nowIndex.value == index)
				{
					return this.unWrapReplacement(sub);
				}
			}
			Component tmp = this.findSubComponent(sub, name, index, nowIndex);
			if (tmp != null)
			{
				return tmp;
			}
		}
		return null;
	}

	/**
	 * 解析基础控件的名称.
	 */
	private String parseBaseName(String baseName, StringRef subName, IntegerRef subIndex)
	{
		String[] names = StringTool.separateString(baseName, ":", true);
		if (names.length == 2)
		{
			subName.setString(names[1]);
		}
		else if (names.length == 3)
		{
			subName.setString(names[1]);
			subIndex.value = Integer.parseInt(names[2]);
		}
		else if (names.length > 3)
		{
			return null;
		}
		return names[0];
	}

	protected ModifiableViewRes getModifiableViewRes()
			throws EternaException
	{
		if (this.viewRes == null)
		{
			this.viewRes = super.getModifiableViewRes();
			if (this.baseComponent != null)
			{
				this.viewRes.addAll(this.baseComponent.getViewRes());
			}
		}
		return this.viewRes;
	}

	protected static class ReplacementInfo
	{
		public final String name;
		private int appearenCount = 0;
		public int[] indexs = null;
		public Component base = null;

		public ReplacementInfo(String name)
		{
			this.name = name;
		}

		public ReplacementInfo(String name, int index)
		{
			this(name);
			this.indexs = new int[]{index};
		}

		public boolean addIndex(int index)
		{
			if (this.indexs == null)
			{
				return false;
			}
			if (this.hasIndex(index))
			{
				return false;
			}
			if (index <= 0)
			{
				return false;
			}
			int[] tmpArr = this.indexs;
			this.indexs = new int[tmpArr.length + 1];
			System.arraycopy(tmpArr, 0, this.indexs, 0, tmpArr.length);
			this.indexs[this.indexs.length - 1] = index;
			return true;
		}

		public boolean canReplace()
		{
			this.appearenCount++;
			return this.indexs == null || this.hasIndex(this.appearenCount);
		}

		private boolean hasIndex(int index)
		{
			for (int i = 0; i < this.indexs.length; i++)
			{
				if (this.indexs[i] == index)
				{
					return true;
				}
			}
			return false;
		}

	}

}