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

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import self.micromagic.eterna.share.EternaException;
import self.micromagic.eterna.share.EternaFactory;
import self.micromagic.eterna.view.BaseManager;
import self.micromagic.eterna.view.Function;
import self.micromagic.eterna.view.ViewAdapter;
import self.micromagic.eterna.view.ViewAdapterGenerator;

public class ModifiableViewResImpl
		implements ViewAdapterGenerator.ModifiableViewRes
{
	protected Map functionMap = null;
	protected Set typicalComponentNames = null;
	protected Set resourceNames = null;

	public String addFunction(Function fn)
			throws EternaException
	{
		if (this.functionMap == null) this.functionMap = new HashMap(2);
		EternaFactory shareFactory = fn.getFactory().getShareFactory();
		String fName = fn.getName();
		if (shareFactory != null)
		{
			try
			{
				boolean sameFn = true;
				EternaFactory tmp = shareFactory;
				while (tmp != null)
				{
					// 循环判断此方法是否被重定义过
					Function otherFn = tmp.getFunction(fName);
					if (otherFn != fn)
					{
						sameFn = false;
						break;
					}
					tmp = tmp.getShareFactory();
				}
				if (!sameFn)
				{
					// 重定义过的方法需要在名字后加后缀来区分
					int fnId = 1;
					tmp = shareFactory.getShareFactory();
					while (tmp != null)
					{
						tmp = tmp.getShareFactory();
						fnId++;
					}
					// 后缀根据所在的EternaFactory的share层级来定
					fName += "_EFID_" + fnId;
				}
			}
			catch (EternaException ex)
			{
				// 出现异常, 说明shareFactory中没有找到同名的方法
			}
		}
		Function oldFn = (Function) this.functionMap.get(fName);
		if (oldFn != null && oldFn != fn)
		{
			ViewTool.log.error("Duplicate function name:[" + fName + "] when add it.");
		}
		if (oldFn == null)
		{
			this.functionMap.put(fName, fn);
			this.addAll(fn.getViewRes());
		}
		return fName;
	}

	/**
	 * 向方法的map中添加一组方法.
	 */
	public void addAllFunction(Map fnMap)
	{
		if (this.functionMap == null)
		{
			if (fnMap == null || fnMap.size() == 0)
			{
				return;
			}
			this.functionMap = new HashMap(2);
		}
		ViewTool.putAllFunction(this.functionMap, fnMap);
	}

	public void addTypicalComponentNames(String name)
	{
		if (this.typicalComponentNames == null) this.typicalComponentNames = new HashSet(1);
		this.typicalComponentNames.add(name);
	}

	public void addResourceNames(String name)
	{
		if (this.resourceNames == null) this.resourceNames = new HashSet(1);
		this.resourceNames.add(name);
	}

	public void addAll(ViewAdapter.ViewRes res)
			throws EternaException
	{
		this.addAllFunction(res.getFunctionMap());
		if (this.typicalComponentNames == null)
		{
			Set temp = res.getTypicalComponentNames();
			if (temp.size() > 0)
			{
				this.typicalComponentNames = new HashSet(2);
				this.typicalComponentNames.addAll(temp);
			}
		}
		else
		{
			this.typicalComponentNames.addAll(res.getTypicalComponentNames());
		}
		if (this.resourceNames == null)
		{
			Set temp = res.getResourceNames();
			if (temp.size() > 0)
			{
				this.resourceNames = new HashSet(2);
				this.resourceNames.addAll(temp);
			}
		}
		else
		{
			this.resourceNames.addAll(res.getResourceNames());
		}
	}

	public Map getFunctionMap()
	{
		if (this.functionMap == null) return Collections.EMPTY_MAP;
		return Collections.unmodifiableMap(this.functionMap);
	}

	public Set getTypicalComponentNames()
	{
		if (this.typicalComponentNames == null) return Collections.EMPTY_SET;
		return Collections.unmodifiableSet(this.typicalComponentNames);
	}

	public Set getResourceNames()
	{
		if (this.resourceNames == null) return Collections.EMPTY_SET;
		return Collections.unmodifiableSet(this.resourceNames);
	}

}