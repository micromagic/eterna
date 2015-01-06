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

package self.micromagic.eterna.digester2;

import self.micromagic.eterna.share.FactoryContainer;
import self.micromagic.eterna.share.EternaFactory;
import self.micromagic.eterna.share.Tool;
import self.micromagic.util.container.ThreadCache;
import org.apache.commons.logging.Log;

/**
 */
public class ContainerManager
{
	public static final Log log = Tool.log;

	public static FactoryContainer getCurrentContainer()
	{
		return null;
	}

	public static EternaFactory getCurrentFactory()
	{
		return (EternaFactory) ThreadCache.getInstance().getProperty(THREAD_FACTORY_KEY);
	}

	public static void setCurrentFactory(EternaFactory factory)
	{
		ThreadCache.getInstance().setProperty(THREAD_FACTORY_KEY, factory);
	}

	public static int getSuperInitLevel()
	{
		return 0;
	}

	/**
	 * 在线程中存储当前EternaFactory的键值.
	 */
	private static final String THREAD_FACTORY_KEY = "eterna.current.factory";

}