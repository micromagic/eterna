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

package self.micromagic.eterna;

import self.micromagic.eterna.digester2.ContainerManager;
import self.micromagic.eterna.share.EternaException;
import self.micromagic.eterna.share.EternaFactory;
import self.micromagic.eterna.share.FactoryContainer;
import self.micromagic.util.Utility;

public class EternaFactoryCreater
{
	public static EternaFactory getEternaFactory(String config)
			throws EternaException
	{
		//Utility.setProperty(FactoryManager.CHECK_GRAMMER_PROPERTY, "false");
		Utility.setProperty(ContainerManager.LOAD_DEFAULT_FLAG, "false");
		Utility.setProperty(ContainerManager.INIT_CONFIG_FLAG, config);
		FactoryContainer fc = ContainerManager.getGlobalContainer();
		//fmi.reInit(null);
		return (EternaFactory) fc.getFactory();
	}

	public static EternaFactory getEternaFactory(Class c)
			throws EternaException
	{
		return getEternaFactory("cp:" + c.getName().replace('.', '/') + ".xml");
	}

}