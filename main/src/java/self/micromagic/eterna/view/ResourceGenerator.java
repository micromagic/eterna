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

import self.micromagic.eterna.share.Generator;
import self.micromagic.eterna.share.EternaException;

/**
 * 文本资源构造器.
 */
public interface ResourceGenerator extends Generator
{
	/**
	 * 设置此文本资源的文本.
	 */
	void setResourceText(String text) throws EternaException;

	Resource createResource() throws EternaException;

}