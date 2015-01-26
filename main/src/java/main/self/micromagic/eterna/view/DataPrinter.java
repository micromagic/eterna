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

import java.io.IOException;
import java.io.Writer;
import java.sql.SQLException;
import java.text.DateFormat;
import java.util.Iterator;
import java.util.Map;

import self.micromagic.eterna.dao.ResultIterator;
import self.micromagic.eterna.dao.ResultRow;
import self.micromagic.eterna.share.EternaException;
import self.micromagic.eterna.share.EternaObject;

/**
 * 数据集输出器, 用于将数据集按照一定的格式进行输出.
 * 注: 数据集中不可有自引用的数据.
 */
public interface DataPrinter extends EternaObject
{
	/**
	 * 获取此数据集输出器的名称.
	 */
	public String getName() throws EternaException;

	/**
	 * 输出数据集.
	 *
	 * @param out          输出流
	 * @param data         数据集
	 * @param hasPreData   输出的数据集前是否有其他数据
	 */
	void printData(Writer out, Map data, boolean hasPreData) throws IOException, EternaException;

	/**
	 * 输出布尔类型值.
	 *
	 * @param out         输出流
	 * @param b           布尔值
	 */
	void print(Writer out, boolean b) throws IOException, EternaException;

	/**
	 * 输出字符类型值.
	 *
	 * @param out         输出流
	 * @param c           字符值
	 */
	void print(Writer out, char c) throws IOException, EternaException;

	/**
	 * 输出整型值.
	 *
	 * @param out         输出流
	 * @param i           整型值
	 */
	void print(Writer out, int i) throws IOException, EternaException;

	/**
	 * 输出长整型值.
	 *
	 * @param out         输出流
	 * @param l           长整型值
	 */
	void print(Writer out, long l) throws IOException, EternaException;

	/**
	 * 输出浮点型值.
	 *
	 * @param out         输出流
	 * @param f           浮点型值
	 */
	void print(Writer out, float f) throws IOException, EternaException;

	/**
	 * 输出双精度浮点型值.
	 *
	 * @param out         输出流
	 * @param d           双精度浮点型值
	 */
	void print(Writer out, double d) throws IOException, EternaException;

	/**
	 * 输出字符串类型值.
	 *
	 * @param out         输出流
	 * @param s           字符串类型值
	 */
	void print(Writer out, String s) throws IOException, EternaException;

	/**
	 * 输出一个Object对象.
	 *
	 * @param out          输出流
	 * @param value        要输出的Object对象
	 */
	void print(Writer out, Object value) throws IOException, EternaException;

	/**
	 * 输出一个Object对象数组.
	 *
	 * @param out          输出流
	 * @param values       要输出的Object对象数组
	 */
	void print(Writer out, Object[] values) throws IOException, EternaException;

	/**
	 * 输出一个Map对象.
	 *
	 * @param out          输出流
	 * @param map          要输出的Map对象
	 */
	void printMap(Writer out, Map map) throws IOException, EternaException;

	/**
	 * 输出结果集迭代器的内容.
	 *
	 * @param out          输出流
	 * @param ritr         结果集迭代器
	 */
	void printResultIterator(Writer out, ResultIterator ritr)
			throws IOException, EternaException, SQLException;

	/**
	 * 输出行结果集的内容.
	 *
	 * @param out          输出流
	 * @param row          行结果集
	 */
	void printResultRow(Writer out, ResultRow row)
			throws IOException, EternaException, SQLException;

	/**
	 * 输出迭代器的内容.
	 *
	 * @param out          输出流
	 * @param itr          迭代器
	 */
	void printIterator(Writer out, Iterator itr) throws IOException, EternaException;

	/**
	 * 输出对象的起始符.
	 *
	 * @param out          输出流
	 */
	void printObjectBegin(Writer out) throws IOException, EternaException;

	/**
	 * 输出对象的结束符.
	 *
	 * @param out          输出流
	 */
	void printObjectEnd(Writer out) throws IOException, EternaException;

	/**
	 * 输出一组key-value对.
	 *
	 * @param out          输出流
	 * @param key          名称
	 * @param value        布尔值
	 * @param first        是否为对象中的第一组值
	 */
	void printPair(Writer out, String key, boolean value, boolean first)
			throws IOException, EternaException;

	/**
	 * 输出一组key-value对.
	 *
	 * @param out          输出流
	 * @param key          名称
	 * @param value        字符值
	 * @param first        是否为对象中的第一组值
	 */
	void printPair(Writer out, String key, char value, boolean first)
			throws IOException, EternaException;

	/**
	 * 输出一组key-value对.
	 *
	 * @param out          输出流
	 * @param key          名称
	 * @param value        整型值
	 * @param first        是否为对象中的第一组值
	 */
	void printPair(Writer out, String key, int value, boolean first)
			throws IOException, EternaException;

	/**
	 * 输出一组key-value对, 不检查参数key是否为null, 是否包含特殊字符.
	 *
	 * @param out          输出流
	 * @param key          名称
	 * @param value        整型值
	 * @param first        是否为对象中的第一组值
	 */
	void printPairWithoutCheck(Writer out, String key, int value, boolean first)
			throws IOException, EternaException;

	/**
	 * 输出一组key-value对.
	 *
	 * @param out          输出流
	 * @param key          名称
	 * @param value        长整型值
	 * @param first        是否为对象中的第一组值
	 */
	void printPair(Writer out, String key, long value, boolean first)
			throws IOException, EternaException;

	/**
	 * 输出一组key-value对, 不检查参数key是否为null, 是否包含特殊字符.
	 *
	 * @param out          输出流
	 * @param key          名称
	 * @param value        长整型值
	 * @param first        是否为对象中的第一组值
	 */
	void printPairWithoutCheck(Writer out, String key, long value, boolean first)
			throws IOException, EternaException;

	/**
	 * 输出一组key-value对.
	 *
	 * @param out          输出流
	 * @param key          名称
	 * @param value        浮点型值
	 * @param first        是否为对象中的第一组值
	 */
	void printPair(Writer out, String key, float value, boolean first)
			throws IOException, EternaException;

	/**
	 * 输出一组key-value对.
	 *
	 * @param out          输出流
	 * @param key          名称
	 * @param value        双精度浮点型值
	 * @param first        是否为对象中的第一组值
	 */
	void printPair(Writer out, String key, double value, boolean first)
			throws IOException, EternaException;

	/**
	 * 输出一组key-value对, 不检查参数key是否为null, 是否包含特殊字符.
	 *
	 * @param out          输出流
	 * @param key          名称
	 * @param value        双精度浮点型值
	 * @param first        是否为对象中的第一组值
	 */
	void printPairWithoutCheck(Writer out, String key, double value, boolean first)
			throws IOException, EternaException;

	/**
	 * 输出一组key-value对.
	 *
	 * @param out          输出流
	 * @param key          名称
	 * @param value        字符串值
	 * @param first        是否为对象中的第一组值
	 */
	void printPair(Writer out, String key, String value, boolean first)
			throws IOException, EternaException;

	/**
	 * 输出一组key-value对, 不检查参数key是否为null, 是否包含特殊字符.
	 *
	 * @param out          输出流
	 * @param key          名称
	 * @param value        字符串值
	 * @param first        是否为对象中的第一组值
	 */
	void printPairWithoutCheck(Writer out, String key, String value, boolean first)
			throws IOException, EternaException;

	/**
	 * 输出一组key-value对.
	 *
	 * @param out          输出流
	 * @param key          名称
	 * @param value        对象值
	 * @param first        是否为对象中的第一组值
	 */
	void printPair(Writer out, String key, Object value, boolean first)
			throws IOException, EternaException;

	/**
	 * 输出一组key-value对, 不检查参数key是否为null, 是否包含特殊字符.
	 *
	 * @param out          输出流
	 * @param key          名称
	 * @param value        对象值
	 * @param first        是否为对象中的第一组值
	 */
	void printPairWithoutCheck(Writer out, String key, Object value, boolean first)
			throws IOException, EternaException;

	/**
	 * 设置日期类型的数据使用的格式化方式.
	 */
	void setDateFormat(DateFormat format);

	/**
	 * 获取一个bean实例的输出器.
	 *
	 * @param beanClass     bean的Class
	 * @return      bean实例的输出器
	 */
	BeanPrinter getBeanPrinter(Class beanClass) throws EternaException;

	/**
	 * bean实例的输出器.
	 */
	interface BeanPrinter
	{
		/**
		 * 输出bean中的属性.
		 *
		 * @param p         数据集输出器
		 * @param out       输出流
		 * @param bean      bean的实例
		 */
		void print(DataPrinter p, Writer out, Object bean) throws IOException, EternaException;

	}

}