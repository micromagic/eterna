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

package self.micromagic.app;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import self.micromagic.eterna.digester.FactoryManager;

/**
 * 在servlet初始化完成后, 向全局的工程管理器实例中添加
 * <code>SERVLET_CONTEXT</code>属性.
 *
 * @see FactoryManager#SERVLET_CONTEXT
 */
public class EternaServletContextListener
		implements ServletContextListener
{
	public void contextInitialized(ServletContextEvent sce)
	{
		FactoryManager.getGlobalFactoryManager().setAttribute(
				FactoryManager.SERVLET_CONTEXT, sce.getServletContext());
	}

	public void contextDestroyed(ServletContextEvent sce)
	{
	}

}