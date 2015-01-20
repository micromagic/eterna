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

import java.io.InputStream;

import self.micromagic.eterna.share.FactoryContainer;

/**
 * 配置资源的对象.
 */
public interface ConfigResource
{
	/**
	 * 创建一个当前类型的配置资源.
	 *
	 * @param config     配置信息
	 * @param container  当前的工厂容器
	 */
	ConfigResource create(String config, FactoryContainer container);

	/**
	 * 获取最后更新的时间(毫秒数).
	 * 如果无法获取最后更新时间则返回-1.
	 */
	long getLastModified();

	/**
	 * 获取资源的定义信息.
	 */
	String getURI();

	/**
	 * 获取资源的数据流.
	 * 如果不存在则返回null.
	 */
	InputStream getAsStream();

	/**
	 * 根据给出的路径获取新的配置资源的对象.
	 * 如果不存在则返回null.
	 */
	ConfigResource getResource(String path);

}
