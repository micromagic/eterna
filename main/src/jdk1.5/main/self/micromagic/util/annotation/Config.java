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

package self.micromagic.util.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 将类中的一个静态变量或静态方法与配置中的值进行
 * 绑定的标注.
 * 
 * @see self.micromagic.util.PropertiesManager#autoBind(Class)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface Config
{
	/**
	 * 对应的配置的名称.
	 */
	String name();

	/**
	 * 配置信息的描述.
	 */
	String description() default "";

	/**
	 * 当配置未设置时使用的默认值.
	 */
	String defaultValue() default "";

}