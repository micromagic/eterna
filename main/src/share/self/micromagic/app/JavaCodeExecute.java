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



/**
 * 动态编译java代码来构造一个执行器.
 *
 * 需设置的属性
 * code                  执行的java代码                                                           2选1
 * attrCode              从factory的属性中获取执行的java代码                                      2选1
 *
 * imports               需要引入的包, 如：java.lang, 只需给出包路径, 以","分隔                   可选
 * extends               继承的类                                                                 可选
 * codeParam             预编译执行代码的参数, 格式为: key1=value1;key2=value2                    可选
 * throwCompileError     是否需要将编译的错误抛出, 抛出错误会打断初始化的执行                     默认为false
 *
 * @deprecated
 * @see self.micromagic.dc.JavaCodeExecute
 */
public class JavaCodeExecute extends self.micromagic.dc.JavaCodeExecute
{
}