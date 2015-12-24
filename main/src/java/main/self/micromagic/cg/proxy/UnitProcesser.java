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

import java.lang.reflect.Field;
import java.lang.reflect.Member;

import self.micromagic.cg.BeanMethodInfo;
import self.micromagic.cg.ClassGenerator;

/**
 * 对一个属性单元的代码生成器.
 *
 * @author micromagic@sina.com
 */
public interface UnitProcesser
{
	/**
	 * 获得对属性的处理代码.
	 *
	 * @param f              属性对象
	 * @param type           属性的类型
	 * @param wrapName       如果是基本类型的话, 外覆类的名称
	 * @param processerType  处理类型, 写或读
	 * @param cg             生成处理类的代码生成器
	 * @return   对这个属性的处理代码
	 */
	String getFieldCode(Field f, Class type, String wrapName, int processerType, ClassGenerator cg);

	/**
	 * 获得对方法的处理代码.
	 *
	 * @param m              方法信息
	 * @param type           属性的类型
	 * @param wrapName       如果是基本类型的话, 外覆类的名称
	 * @param processerType  处理类型, 写或读
	 * @param cg             生成处理类的代码生成器
	 * @return   对这个方法的处理代码
	 */
	String getMethodCode(BeanMethodInfo m, Class type, String wrapName, int processerType,
			ClassGenerator cg);

	/**
	 * 属性对应的成员.
	 */
	public static abstract class BeanProperty
	{
		/**
		 * 获取属性对应的成员.
		 */
		public Member getMember()
		{
			return this.member;
		}
		/**
		 * 设置属性对应的成员.
		 */
		public void setMember(Member member)
		{
			this.member = member;
		}
		private Member member;

	}

}