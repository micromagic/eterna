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

import self.micromagic.eterna.share.EternaException;
import self.micromagic.eterna.share.EternaFactory;
import self.micromagic.eterna.share.Generator;

public interface ComponentGenerator extends Generator
{
	void addComponent(Component com) throws EternaException;

	void deleteComponent(Component com) throws EternaException;

	void clearComponents() throws EternaException;

	void addEvent(Event event) throws EternaException;

	void deleteEvent(Event event) throws EternaException;

	void clearEvent() throws EternaException;

	void setType(String type) throws EternaException;

	void setIgnoreGlobalParam(boolean ignore) throws EternaException;

	void setComponentParam(String param) throws EternaException;

	void setBeforeInit(String condition) throws EternaException;

	void setInitScript(String body) throws EternaException;

	void setAttributes(String attributes) throws EternaException;

	void initAttributes(EternaFactory factory, String attributes) throws EternaException;

	Component createComponent() throws EternaException;

	interface EventGenerator extends Generator
	{
		void setName(String name) throws EternaException;

		void setScriptParam(String param) throws EternaException;

		void setScriptBody(String body) throws EternaException;

		Event createEvent() throws EternaException;

	}

}