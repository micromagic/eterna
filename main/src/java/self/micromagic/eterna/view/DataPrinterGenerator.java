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

package self.micromagic.eterna.view;

import self.micromagic.eterna.share.EternaFactory;
import self.micromagic.eterna.share.Generator;
import self.micromagic.eterna.digester.ConfigurationException;

/**
 * 数据集输出器的构造者.
 */
public interface DataPrinterGenerator extends Generator
{
	/**
	 * 初始化此构造者.
	 */
	void initialize(EternaFactory factory) throws ConfigurationException;

	/**
	 * 创建一个数据集输出器.
	 *
	 * @return    数据集输出器
	 */
	DataPrinter createDataPrinter() throws ConfigurationException;

}