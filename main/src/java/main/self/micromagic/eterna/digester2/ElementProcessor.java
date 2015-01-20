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

import org.dom4j.Element;

import self.micromagic.util.IntegerRef;

/**
 * xml元素节点的处理器.
 */
public interface ElementProcessor
{
	/**
	 * 执行节点起始时的处理.
	 *
	 * @param digester  文档解析工具
	 * @param element   当前节点
	 *
	 * @return  是否需要继续执行后面的处理
	 */
	boolean begin(Digester digester, Element element);

	/**
	 * 执行节点结束时的处理.
	 *
	 * @param digester  文档解析工具
	 * @param element   当前节点
	 */
	void end(Digester digester, Element element);

	/**
	 * 根据配置解析元素节点的处理器.
	 *
	 * @param digester  文档解析工具
	 * @param rule      所属的处理规则
	 * @param config    配置信息
	 * @param position  读取的起始位置
	 * @return  解析出来的元素节点的处理器
	 */
	ElementProcessor parse(Digester digester, ParseRule rule,
			String config, IntegerRef position);

}