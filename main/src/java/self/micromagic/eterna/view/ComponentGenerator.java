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

package self.micromagic.eterna.view;

import self.micromagic.eterna.digester.ConfigurationException;
import self.micromagic.eterna.share.EternaFactory;
import self.micromagic.eterna.share.Generator;

public interface ComponentGenerator extends Generator
{
	void addComponent(Component com) throws ConfigurationException;

	void deleteComponent(Component com) throws ConfigurationException;

	void clearComponents() throws ConfigurationException;

	void addEvent(Component.Event event) throws ConfigurationException;

	void deleteEvent(Component.Event event) throws ConfigurationException;

	void clearEvent() throws ConfigurationException;

	void setType(String type) throws ConfigurationException;

	void setIgnoreGlobalParam(boolean ignore) throws ConfigurationException;

	void setComponentParam(String param) throws ConfigurationException;

	void setBeforeInit(String condition) throws ConfigurationException;

	void setInitScript(String body) throws ConfigurationException;

	void setAttributes(String attributes) throws ConfigurationException;

	void initAttributes(EternaFactory factory, String attributes) throws ConfigurationException;

	Component createComponent() throws ConfigurationException;

	interface EventGenerator extends Generator
	{
		void setName(String name) throws ConfigurationException;

		void setScriptParam(String param) throws ConfigurationException;

		void setScriptBody(String body) throws ConfigurationException;

		Component.Event createEvent() throws ConfigurationException;

	}

}