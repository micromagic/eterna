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

import self.micromagic.eterna.share.Generator;
import self.micromagic.eterna.share.AbstractGenerator;
import self.micromagic.eterna.model.Execute;
import self.micromagic.eterna.model.ModelAdapter;
import self.micromagic.eterna.share.EternaException;

public abstract class AbstractExecute extends AbstractGenerator
		implements Execute, Generator
{
	protected ModelAdapter model;
	protected boolean initialized = false;

	public void initialize(ModelAdapter model)
			throws EternaException
	{
		this.initialized = true;
		this.model = model;
	}

	public Object create()
			throws EternaException
	{
		return this.createExecute();
	}

	public Execute createExecute()
			throws EternaException
	{
		return this;
	}

	public ModelAdapter getModelAdapter()
			throws EternaException
	{
		return this.model;
	}

	public void destroy()
	{
	}

}