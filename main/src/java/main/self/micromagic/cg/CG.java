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

package self.micromagic.cg;

import org.apache.commons.logging.Log;
import self.micromagic.util.Utility;

/**
 * 将代码生成类的接口, 用于实现不同的生成方式.
 *
 * @author micromagic@sina.com
 */
public interface CG
{
	/**
	 * 类的名称变量名. 在代码内部, 需要构造类本身或写构造函数时, 需要用到的类名. <p>
	 * 如：
	 * 构造函数   public ${thisName}()
	 * 定义本类   ${thisName} value = new ${thisName}();
	 */
	public static final String THIS_NAME = "thisName";

	/**
	 * 配置对代码编译的类型.
	 */
	public static final String COMPILE_TYPE_PROPERTY = "self.micromagic.compile.type";

	/**
	 * 用ant作为编译类型时使用的名称.
	 */
	public static final String COMPILE_TYPE_ANT = AntCG.COMPILE_TYPE;

	/**
	 * 用javassist作为编译类型时使用的名称.
	 */
	public static final String COMPILE_TYPE_JAVASSIST = JavassistCG.COMPILE_TYPE;

	/**
	 * 设置是否要输出代码动态编译相关的日志信息.
	 * 可设置的值如下:
	 * 1. 只记录出错信息
	 * 2. 记录编译过程中的一些信息
	 * 3. 记录生成的代码信息
	 */
	public static final String COMPILE_LOG_PROPERTY = "self.micromagic.compile.log";

	/**
	 * 1 (> 0). 只记录出错信息
	 */
	public static final int COMPILE_LOG_TYPE_ERROR = 0;

	/**
	 * 2 (> 1). 记录编译过程中的一些信息
	 */
	public static final int COMPILE_LOG_TYPE_INFO = 1;

	/**
	 * 3 (> 2). 记录生成的代码信息
	 */
	public static final int COMPILE_LOG_TYPE_DEBUG = 2;

	/**
	 * 用于记录日志.
	 */
	static final Log log = Utility.createLog("eterna.cg");

	/**
	 * 创建一个类.
	 *
	 * @param cg   创建类时需要使用到的类构造器.
	 */
	Class createClass(ClassGenerator cg) throws Exception;

}