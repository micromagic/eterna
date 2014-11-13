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

import self.micromagic.eterna.digester.ConfigurationException;
import self.micromagic.eterna.digester.FactoryManager;

public interface Factory
{
	static final int MAX_ADAPTER_COUNT = 1024 * 16;

	void initialize(FactoryManager.Instance factoryManager, Factory shareFactory)
			throws ConfigurationException;

	void setName(String name) throws ConfigurationException;

	String getName() throws ConfigurationException;

	FactoryManager.Instance getFactoryManager() throws ConfigurationException;

	Object getAttribute(String name) throws ConfigurationException;

	String[] getAttributeNames() throws ConfigurationException;

	Object setAttribute(String name, Object value) throws ConfigurationException;

	Object removeAttribute(String name) throws ConfigurationException;

	void destroy();

}