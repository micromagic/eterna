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

package self.micromagic.eterna.tag;

import self.micromagic.eterna.digester.FactoryManager;
import self.micromagic.eterna.share.EternaException;
import self.micromagic.util.Utility;
import org.apache.commons.logging.Log;

/**
 * 默认的工厂实例查询者.
 *
 * @author micromagic@sina.com
 */
public class DefaultFinder
		implements InstanceFinder
{
	/**
	 * 默认的工厂实例查询者, 如果有自己的实现类, 可以重新对此变量赋值.
	 */
	public static InstanceFinder finder = new DefaultFinder();

	/**
	 * 用于记录日志.
	 */
	static final Log log = Utility.createLog("eterna.tag");


	public FactoryManager.Instance findInstance(String name)
	{
		try
		{
			return FactoryManager.getFactoryManager(name);
		}
		catch (EternaException ex)
		{
			return null;
		}
	}

}