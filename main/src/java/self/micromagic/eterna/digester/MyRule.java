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

import org.apache.commons.digester.Rule;
import org.xml.sax.Attributes;

/**
 * 抽象的初始化规则, 提供了公用的方法及是否可初始化的控制.
 *
 * @author micromagic@sina.com
 */
public abstract class MyRule extends Rule
{
	/**
	 * 是否可执行初始化.
	 * 这是个静态量, 由于初始化时只能在一个线程中进行, 所以这里不会有多线程的问题.
	 */
	static boolean dealRule = true;

	/**
	 * 是否使用body文本的数据.
	 */
	protected boolean useBodyText = false;

	public MyRule()
	{
	}

	/**
	 * 继承父类的处理body文本.
	 * 会判断是否可执行及是否使用body文本, 通过的话才调用myBody.
	 */
	public void body(String namespace, String name, String text)
			throws Exception
	{
		if (dealRule && this.useBodyText)
		{
			BodyText temp = new BodyText();
			temp.append(text.toCharArray(), 0, text.length());
			this.myBody(namespace, name, temp);
		}
	}

	/**
	 * 实际处理body文本的方法, 继承类需重写此方法实现真正的处理.
	 */
	public void myBody(String namespace, String name, BodyText text)
			throws Exception
	{
	}

	/**
	 * 继承父类的处理节点开始.
	 * 会判断是否可执行, 通过的话才调用myBegin.
	 */
	public void begin(String namespace, String name, Attributes attributes)
			throws Exception
	{
		if (dealRule)
		{
			this.myBegin(namespace, name, attributes);
		}
	}

	/**
	 * 实际处理节点开始的方法, 继承类需重写此方法实现真正的处理.
	 */
	public void myBegin(String namespace, String name, Attributes attributes)
			throws Exception
	{
	}

	/**
	 * 继承父类的处理节点结束.
	 * 会判断是否可执行, 通过的话才调用myEnd.
	 */
	public void end(String namespace, String name)
			throws Exception
	{
		if (dealRule)
		{
			this.myEnd(namespace, name);
		}
	}

	/**
	 * 实际处理节点结束的方法, 继承类需重写此方法实现真正的处理.
	 */
	public void myEnd(String namespace, String name)
			throws Exception
	{
	}

}