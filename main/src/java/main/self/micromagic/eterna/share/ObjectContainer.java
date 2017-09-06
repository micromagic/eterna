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

package self.micromagic.eterna.share;

/**
 * 存放对象的容器.
 */
abstract class ObjectContainer
{
	private int id;

	protected ObjectContainer(int id)
	{
		// 编号的最低位用于保存是否已初始化
		this.id = id << 1;
	}

	/**
	 * 获取对象的编号.
	 */
	public int getId()
	{
		return this.id >> 1;
	}

	/**
	 * 是否已初始化.
	 *
	 * @param chage  是否需要将初始化标志设为true
	 */
	protected boolean isInitialized(boolean change)
	{
		if ((this.id & 1) == 1)
		{
			return true;
		}
		if (change)
		{
			this.id |= 1;
		}
		return false;
	}

	/**
	 * 获取对象的名称.
	 */
	public abstract String getName();

	/**
	 * 所创建的对象是否为单例.
	 */
	public abstract boolean isSingleton();

	/**
	 * 获取对象的类型.
	 */
	public abstract Class getType();

	/**
	 * 执行初始化.
	 */
	public abstract boolean initialize(EternaFactory factory) throws EternaException;

	/**
	 * 创建容器中存放的对象.
	 *
	 * @param needInit  是否需要执行初始化
	 */
	public abstract Object create(boolean needInit, EternaFactory factory);

	/**
	 * 销毁存放的对象.
	 */
	public abstract void destroy();

}

/**
 * 普通Object的容器.
 */
class NormalObjectCon extends ObjectContainer
{
	private final String name;
	private final Object obj;

	NormalObjectCon(int id, String name, Object obj)
	{
		super(id);
		this.obj = obj;
		this.name = name;
	}

	public String getName()
	{
		return this.name;
	}

	public boolean isSingleton()
	{
		return true;
	}

	public Class getType()
	{
		return this.obj.getClass();
	}

	public boolean initialize(EternaFactory factory)
			throws EternaException
	{
		if (this.isInitialized(true))
		{
			return true;
		}
		return false;
	}

	public Object create(boolean needInit, EternaFactory factory)
	{
		return this.obj;
	}

	public void destroy()
	{
	}

}

/**
 * EternaObject的容器.
 */
class EternaObjectCon extends ObjectContainer
{
	private final EternaObject obj;

	EternaObjectCon(int id, EternaObject obj)
	{
		super(id);
		this.obj = obj;
	}

	public String getName()
	{
		return this.obj.getName();
	}

	public boolean isSingleton()
	{
		return true;
	}

	public Class getType()
	{
		return this.obj.getClass();
	}

	public boolean initialize(EternaFactory factory)
			throws EternaException
	{
		if (this.isInitialized(true))
		{
			return true;
		}
		return this.obj.initialize(factory);
	}

	public Object create(boolean needInit, EternaFactory factory)
	{
		if (needInit && !this.isInitialized(false))
		{
			EternaFactoryImpl.initObject(true, factory, this);
		}
		return this.obj;
	}

	public void destroy()
	{
	}

}

/**
 * ObjectCreater的容器.
 */
class EternaCreaterCon extends ObjectContainer
{
	private final EternaCreater obj;

	EternaCreaterCon(int id, EternaCreater obj)
	{
		super(id);
		this.obj = obj;
	}

	public String getName()
	{
		return this.obj.getName();
	}

	public boolean isSingleton()
	{
		return this.obj.isSingleton();
	}

	public Class getType()
	{
		return this.obj.getObjectType();
	}

	public boolean initialize(EternaFactory factory)
			throws EternaException
	{
		if (this.isInitialized(true))
		{
			return true;
		}
		return this.obj.initialize(factory);
	}

	public Object create(boolean needInit, EternaFactory factory)
	{
		if (needInit && !this.isInitialized(false))
		{
			EternaFactoryImpl.initObject(true, factory, this);
		}
		return this.obj.create();
	}

	public void destroy()
	{
		this.obj.destroy();
	}

}
