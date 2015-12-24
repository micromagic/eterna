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

package self.micromagic.cg.proxy;

/**
 * 处理的信息类.
 *
 * @author micromagic@sina.com
 */
public class ProcesserInfo
{
	public final String name;
	public final Class type;
	public final Object processer;

	public ProcesserInfo(String name, Class type, Object processer)
	{
		this.name = name;
		this.type = type;
		this.processer = processer;
	}

}