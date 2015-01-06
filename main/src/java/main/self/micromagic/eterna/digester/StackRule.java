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

package self.micromagic.eterna.digester;

import java.util.ArrayList;

import org.xml.sax.Attributes;
import org.apache.commons.digester.Rule;

/**
 * 需要解决多个节点使用同一个初始化规则造成冲突的问题，可以使用这个初始化规则. <p>
 *
 * 使用此规则的样例代码如下:
 * <blockquote><pre>
 * new StackRule() {
 *    public Rule createRule() throws Exception
 *    {
 *       PropertySetter setter = new BodyPropertySetter("trimLine",
 *             "setBeforeInit", true, false);
 *       return new PropertySetRule(setter, false);
 *    }
 * };
 * </pre></blockquote>
 * 通过构造一个匿名类, 实现createRule方法, 在此方法中生成一个真正需要的初始化
 * 规则. 因为每一个节点需要不同的初始化规则实例, 这个初始化规则就是处理在需要
 * 的地方生成新的实例.
 *
 * @author micromagic@sina.com
 */
public abstract class StackRule extends Rule
{
	static final String[] VIEW_ROOT_PATHS = {
		"eterna-config/factory/objs/view",
		"eterna-config/factory/objs/typical-replacement",
		"eterna-config/factory/objs/typical-component",
	};
	static final String[] ALL_ROOT_PATHS = VIEW_ROOT_PATHS;

	private int stackIndex = 0;
	private ArrayList stack = new ArrayList();

	/**
	 * 检查当前节点的路径是否符合此初始化规则.
	 *
	 * @param path      被检查的路径
	 * @param paths     符合条件的路径集
	 */
	static boolean checkRoot(String path, String[] paths)
	{
		for (int i = 0; i < paths.length; i++)
		{
			if (path.startsWith(paths[i]))
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * 生成需要的初始化规则的实例, 实现的方法必须要构造一个新的初始化规则,
	 * 否则就会发送冲突.
	 */
	public abstract Rule createRule() throws Exception;

	private Rule getRule(int stackIndex)
			throws Exception
	{
		if (this.stack.size() == stackIndex)
		{
			Rule rule = this.createRule();
			rule.setDigester(this.getDigester());
			rule.setNamespaceURI(this.getNamespaceURI());
			this.stack.add(rule);
		}
		return (Rule) this.stack.get(stackIndex);
	}

	public void begin(String namespace, String name, Attributes attributes)
			throws Exception
	{
		if (!checkRoot(this.digester.getMatch(), ALL_ROOT_PATHS))
		{
			this.digester.getLogger().error("Error component path:" + this.digester.getMatch() + ".");
			return;
		}
		this.stackIndex++;
		this.getRule(this.stackIndex - 1).begin(namespace, name, attributes);
	}

	public void body(String namespace, String name, String text)
			throws Exception
	{
		if (!checkRoot(this.digester.getMatch(), ALL_ROOT_PATHS))
		{
			return;
		}
		this.getRule(this.stackIndex - 1).body(namespace, name, text);
	}

	public void end(String namespace, String name)
			throws Exception
	{
		if (!checkRoot(this.digester.getMatch(), ALL_ROOT_PATHS))
		{
			return;
		}
		this.stackIndex--;
		this.getRule(this.stackIndex).end(namespace, name);
	}

	public void finish() throws Exception
	{
		this.getRule(0).finish();
	}

}