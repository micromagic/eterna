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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import self.micromagic.util.Utility;
import self.micromagic.util.ref.StringRef;

/**
 * 用于存放自定义变量的缓存.
 */
public class VarCache
{
	/**
	 * 配置中设置是否需要警报变量未初始化的标识.
	 */
	public static final String WARN_NOT_SETTED_FLAG = "var.warnNotInit";

	/**
	 * 是否需要警报变量未设置.
	 */
	private static boolean warnNotInit = true;

	private static final VarCache global = new VarCache();

	/**
	 * 是否为全局的自定义变量的缓存.
	 */
	private boolean globalCache;

	static
	{
		global.globalCache = true;
		try
		{
			Utility.addFieldPropertyManager(WARN_NOT_SETTED_FLAG,
					VarCache.class, "warnNotInit");
		}
		catch (Exception ex) {}
	}

	/**
	 * 获取全局的自定义变量的缓存.
	 */
	public static VarCache getGlobalCache()
	{
		return global;
	}


	/**
	 * 创建一个变量的存储空间.
	 */
	public Object[] createCache()
	{
		return new Object[this.varCount];
	}

	/**
	 * 获取定义的变量信息列表.
	 */
	public VarInfo[] getInfos()
	{
		VarInfo[] arr = new VarInfo[this.varCount];
		this.varList.toArray(arr);
		return arr;
	}

	/**
	 * 获取一个变量定义的信息.
	 * 如果变量不存在, 则创建一个变量定义.
	 *
	 * @param name        变量的名称
	 * @param statusType  变量的状态类型, 读取 写入等
	 * @param err         定义变量时的出错信息
	 * @return
	 */
	public VarInfo getVarInfo(String name, int statusType, StringRef err)
	{
		if (name == null)
		{
			throw new NullPointerException("Param name can't be null.");
		}
		if (!this.globalCache && name.startsWith("$$"))
		{
			// 以"$$"起始的变量定义到全局
			return global.getVarInfo(name, statusType, null);
		}
		VarInfo info = null;
		if (!"$".equals(name))
		{
			// 单个$表示临时变量, 每次都要创建
			Iterator itr = this.varList.iterator();
			for (int i = 0; i < this.varCount; i++)
			{
				AbstractVarInfo tmp = (AbstractVarInfo) itr.next();
				if (name.equals(tmp.getName()))
				{
					tmp.addStatusType(statusType);
					info = tmp;
					break;
				}
			}
		}
		if (info == null)
		{
			if (this.globalCache)
			{
				info = new GlobalVarInfo(name, statusType, this.varCount++);
			}
			else
			{
				info = new ModelVarInfo(name, statusType, this.varCount++);
			}
			this.varList.add(info);
			if (warnNotInit && err != null && !info.isSetted())
			{
				err.setString("The var [" + name + "] hasn't setted value.");
			}
		}
		return info;
	}
	private int varCount = 0;;
	private final List varList = new ArrayList();

	public interface VarInfo
	{
		/**
		 * 变量的状态类型, 读取.
		 */
		int STATUS_TYPE_READ = 0x01;
		/**
		 * 变量的状态类型, 写入.
		 */
		int STATUS_TYPE_WRITE = 0x02;
		/**
		 * 变量的状态类型, 初始化或外部使用.
		 */
		int STATUS_TYPE_INIT = 0x1;

		/**
		 * 变量的名称.
		 */
		String getName();

		/**
		 * 获取变量的值.
		 */
		Object getValue(AppData data);

		/**
		 * 设置变量的值.
		 */
		void setValue(AppData data, Object v);

		/**
		 * 变量是否在表达式中被使用过.
		 * 即是否被读取过.
		 */
		boolean isUsed();

		/**
		 * 变量是否在表达式中被赋过值.
		 */
		boolean isWrite();

		/**
		 * 变量是否被设置过值.
		 */
		boolean isSetted();

		/**
		 * 变量是否需要赋值.
		 * 即在使用时还未设置过值.
		 */
		boolean isNeedSet();

	}

}

/**
 * 抽象的变量信息.
 */
abstract class AbstractVarInfo
		implements VarCache.VarInfo
{
	/**
	 * 用于检测变量是否被设过值的状态.
	 */
	static final int STATUS_CHECK_SETTED = STATUS_TYPE_INIT | STATUS_TYPE_WRITE;

	/**
	 * 变量的状态类型, 需要赋值, 即在使用时还未设置过值.
	 */
	static final int STATUS_TYPE_NEEDSET = 0x002;

	AbstractVarInfo(String name, int statusType)
	{
		this.name = name;
		this.statusType = statusType;
		if (statusType == STATUS_TYPE_READ)
		{
			this.statusType |= STATUS_TYPE_NEEDSET;
		}
	}
	private int statusType;
	private final String name;

	public String getName()
	{
		return this.name;
	}

	/**
	 * 添加一个状态类型.
	 */
	void addStatusType(int type)
	{
		this.statusType |= type;
	}

	public boolean isUsed()
	{
		return (this.statusType & STATUS_TYPE_READ) != 0;
	}

	public boolean isWrite()
	{
		return (this.statusType & STATUS_TYPE_WRITE) != 0;
	}

	public boolean isSetted()
	{
		return (this.statusType & STATUS_CHECK_SETTED) != 0;
	}

	public boolean isNeedSet()
	{
		return (this.statusType & STATUS_TYPE_NEEDSET) != 0
				&& (this.statusType & STATUS_TYPE_INIT) == 0;
	}

}

/**
 * model作用域下的变量信息.
 */
class ModelVarInfo extends AbstractVarInfo
		implements VarCache.VarInfo
{
	public ModelVarInfo(String name, int statusType, int pos)
	{
		super(name, statusType);
		this.pos = pos;
	}
	private final int pos;

	public Object getValue(AppData data)
	{
		return data.modelVars[this.pos];
	}

	public void setValue(AppData data, Object v)
	{
		data.modelVars[this.pos] = v;
	}

}

/**
 * 全局作用域下的变量信息.
 */
class GlobalVarInfo extends AbstractVarInfo
		implements VarCache.VarInfo
{
	public GlobalVarInfo(String name, int statusType, int pos)
	{
		super(name, statusType);
		this.pos = pos;
	}
	private final int pos;

	public Object getValue(AppData data)
	{
		return data.globalVars[this.pos];
	}

	public void setValue(AppData data, Object v)
	{
		data.globalVars[this.pos] = v;
	}

}
