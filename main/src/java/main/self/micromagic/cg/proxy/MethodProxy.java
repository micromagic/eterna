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
 * 一个方法调用的代理, 通过动态生成的代码进行调用, 不使用反射.
 */
public interface MethodProxy
{
	/**
	 * 调用目标对象的方法.
	 *
	 * @param target  被调用的目标对象, 如果此方法为静态方法, 则可以给null
	 * @param args    方法的参数, 需要与目标方法需要的一致
	 * @return  方法调用的返回值
	 */
	Object invoke(Object target, Object[] args) throws Exception;

}