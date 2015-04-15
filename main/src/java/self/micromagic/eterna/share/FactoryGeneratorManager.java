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

package self.micromagic.eterna.share;

import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

import self.micromagic.eterna.share.EternaException;
import self.micromagic.eterna.digester.FactoryManager;
import self.micromagic.eterna.digester.ObjectLogRule;

public class FactoryGeneratorManager
{
	private Map generatorMap;
	private List generatorList;

	private String managerName;
	protected EternaFactory factory;

	private FactoryGeneratorManager shareFGM;
	private boolean initialized;
	private boolean inInit;

	public FactoryGeneratorManager(String managerName, EternaFactory factory)
	{
		this.managerName = managerName;
		this.factory = factory;
		this.generatorMap = new HashMap();
		this.generatorList = new ArrayList();
	}

	protected FactoryGeneratorManager(EternaFactory otherFactory)
	{
		this.managerName = "otherShare";
		this.factory = otherFactory;
		this.initialized = true;
	}

	public void initialize(FactoryGeneratorManager shareFGM)
			throws EternaException
	{
		if (this.initialized)
		{
			return;
		}
		this.initialized = true;
		this.inInit = true;
		this.shareFGM = shareFGM;

		List temp = new ArrayList(this.generatorList.size() + (this.shareFGM == null ? 32 : 2));
		temp.addAll(this.generatorList);
		this.generatorList = temp;

		int size = temp.size();
		GeneratorContainer container;
		for (int i = 0; i < size; i++)
		{
			container = (GeneratorContainer) this.generatorList.get(i);
			ObjectLogRule.setObjName(this.managerName, container.generator.getName());
			container.generator.initialize(this.factory);
		}
		this.inInit = false;
	}

	public void destroy()
	{
		Iterator itr = this.generatorList.iterator();
		while (itr.hasNext())
		{
			GeneratorContainer container = (GeneratorContainer) itr.next();
			container.generator.destroy();
		}
	}

	public Object create(String name)
			throws EternaException
	{
		GeneratorContainer container = (GeneratorContainer) this.generatorMap.get(name);
		if (container == null)
		{
			if (this.shareFGM != null)
			{
				return this.shareFGM.create(name);
			}
			throw new EternaException(
					"Not found [" + this.managerName + "] name:" + name + ".");
		}
		if (this.inInit || !this.initialized)
		{
			container.generator.initialize(this.factory);
		}
		return container.generator.create();
	}

	public Object create(int id)
			throws EternaException
	{
		if (id < 0 || id >= this.generatorList.size())
		{
			if (this.shareFGM != null && id >= Factory.MAX_ADAPTER_COUNT)
			{
				return this.shareFGM.create(id - Factory.MAX_ADAPTER_COUNT);
			}
			throw new EternaException(
					"Not found [" + this.managerName + "] id:" + id + ".");
		}

		GeneratorContainer container = (GeneratorContainer) this.generatorList.get(id);
		if (container == null)
		{
			throw new EternaException(
					"Not found [" + this.managerName + "] id:" + id + ".");
		}
		if (this.inInit || !this.initialized)
		{
			container.generator.initialize(this.factory);
		}
		return container.generator.create();
	}

	public int getIdByName(String name)
			throws EternaException
	{
		GeneratorContainer container = (GeneratorContainer) this.generatorMap.get(name);
		if (container == null)
		{
			if (this.shareFGM != null)
			{
				return this.shareFGM.getIdByName(name) + Factory.MAX_ADAPTER_COUNT;
			}
			throw new EternaException(
					"Not found [" + this.managerName + "] name:" + name + ".");
		}
		return container.id;
	}

	public void register(AdapterGenerator generator)
			throws EternaException
	{
		if (generator == null)
		{
			throw new NullPointerException();
		}
		String name = generator.getName();
		if (this.generatorMap.containsKey(name))
		{
			if (!FactoryManager.isSuperInit())
			{
				throw new EternaException(
						"Duplicate [" + this.managerName + "] name:" + name + ".");
			}
			else
			{
				return;
			}
		}
		if (this.initialized)
		{
			generator.initialize(this.factory);
		}
		GeneratorContainer container;
		int id = this.generatorList.size();
		if (id >= Factory.MAX_ADAPTER_COUNT)
		{
			throw new EternaException("Max adapter count:" + id + "," + Factory.MAX_ADAPTER_COUNT + ".");
		}
		container = new GeneratorContainer(id, generator);
		this.generatorList.add(container);

		this.generatorMap.put(name, container);
	}

	public void deregister(String name)
			throws EternaException
	{
		GeneratorContainer container = (GeneratorContainer) this.generatorMap.get(name);
		if (container == null)
		{
			throw new EternaException(
					"Not found [" + this.managerName + "] name:" + name + ".");
		}
		this.generatorList.set(container.id, null);
		this.generatorMap.remove(name);
		container.generator = null;
	}

	private static class GeneratorContainer
	{
		public final int id;
		public AdapterGenerator generator;

		public GeneratorContainer(int id, AdapterGenerator generator)
		{
			this.id = id;
			this.generator = generator;
		}

	}

	/**
	 * 对于其它类型的ShareFactory的抽象实现.
	 */
	static abstract class AbstractOtherShare extends FactoryGeneratorManager
	{
		public AbstractOtherShare(EternaFactory factory)
		{
			super(factory);
		}

		public void initialize(FactoryGeneratorManager shareFGM) {}
		public void destroy() {}
		public void register(AdapterGenerator generator) {}
		public void deregister(String name) {}

	}

	static class QueryOtherShare extends AbstractOtherShare
	{
		public QueryOtherShare(EternaFactory factory)
		{
			super(factory);
		}

		public Object create(String name)
			throws EternaException
		{
			return this.factory.createQueryAdapter(name);
		}

		public Object create(int id)
			throws EternaException
		{
			return this.factory.createQueryAdapter(id);
		}

		public int getIdByName(String name)
			throws EternaException
		{
			return this.factory.getQueryAdapterId(name);
		}

	}
	static FactoryGeneratorManager createQueryFGM(EternaFactory factory)
	{
		return factory == null ? null : new QueryOtherShare(factory);
	}

	static class UpdateOtherShare extends AbstractOtherShare
	{
		public UpdateOtherShare(EternaFactory factory)
		{
			super(factory);
		}

		public Object create(String name)
			throws EternaException
		{
			return this.factory.createUpdateAdapter(name);
		}

		public Object create(int id)
			throws EternaException
		{
			return this.factory.createUpdateAdapter(id);
		}

		public int getIdByName(String name)
			throws EternaException
		{
			return this.factory.getUpdateAdapterId(name);
		}

	}
	static FactoryGeneratorManager createUpdateFGM(EternaFactory factory)
	{
		return factory == null ? null : new UpdateOtherShare(factory);
	}

	static class SearchOtherShare extends AbstractOtherShare
	{
		public SearchOtherShare(EternaFactory factory)
		{
			super(factory);
		}

		public Object create(String name)
			throws EternaException
		{
			return this.factory.createSearchAdapter(name);
		}

		public Object create(int id)
			throws EternaException
		{
			return this.factory.createSearchAdapter(id);
		}

		public int getIdByName(String name)
			throws EternaException
		{
			return this.factory.getSearchAdapterId(name);
		}

	}
	static FactoryGeneratorManager createSearchFGM(EternaFactory factory)
	{
		return factory == null ? null : new SearchOtherShare(factory);
	}

	static class ModelOtherShare extends AbstractOtherShare
	{
		public ModelOtherShare(EternaFactory factory)
		{
			super(factory);
		}

		public Object create(String name)
			throws EternaException
		{
			return this.factory.createModelAdapter(name);
		}

		public Object create(int id)
			throws EternaException
		{
			return this.factory.createModelAdapter(id);
		}

		public int getIdByName(String name)
			throws EternaException
		{
			return this.factory.getModelAdapterId(name);
		}

	}
	static FactoryGeneratorManager createModelFGM(EternaFactory factory)
	{
		return factory == null ? null : new ModelOtherShare(factory);
	}

	static class ViewOtherShare extends AbstractOtherShare
	{
		public ViewOtherShare(EternaFactory factory)
		{
			super(factory);
		}

		public Object create(String name)
			throws EternaException
		{
			return this.factory.createViewAdapter(name);
		}

		public Object create(int id)
			throws EternaException
		{
			return this.factory.createViewAdapter(id);
		}

		public int getIdByName(String name)
			throws EternaException
		{
			return this.factory.getViewAdapterId(name);
		}

	}
	static FactoryGeneratorManager createViewFGM(EternaFactory factory)
	{
		return factory == null ? null : new ViewOtherShare(factory);
	}

}