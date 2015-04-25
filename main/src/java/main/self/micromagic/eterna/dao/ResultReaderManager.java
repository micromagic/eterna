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

package self.micromagic.eterna.dao;

import java.util.List;

import self.micromagic.eterna.security.Permission;
import self.micromagic.eterna.share.EternaException;
import self.micromagic.eterna.share.EternaFactory;

public interface ResultReaderManager
{
	/**
	 * 在item的arrtibute中设置使用format的名称.
	 */
	String FORMAT_FLAG = "format";

	/**
	 * 在ResultReader的arrtibute中设置格式化值只显示列的名称.
	 * 如果设置了此属性, 将会以此属性值作为名称添加一个相同的
	 * ResultReader到最后, 并将当前ResultReader的format设为
	 * 无效.
	 */
	String SHOW_NAME_FLAG = "showName";

	/**
	 * 在item的arrtibute中设置读取的列表别名的名称.
	 */
	String ALIAS_FLAG = "alias";

	/**
	 * 在reader的名称前设置降序排序的标记.
	 */
	char ORDER_FLAG_DESC = '-';

	/**
	 * 在reader的名称前设置升序排序的标记.
	 */
	char ORDER_FLAG_ASC = '+';

	/**
	 * 获取本ResultReaderManager的名称.
	 */
	String getName() throws EternaException;

	/**
	 * 获取本ResultReaderManager所属的工厂.
	 */
	EternaFactory getFactory() throws EternaException;

	/**
	 * 获取列名是否大小写敏感的. <p>
	 *
	 * @return  true为大小写敏感的, false为不区分大小写
	 */
	boolean isColNameSensitive() throws EternaException;

	/**
	 * 获得ResultReaderManager中的ResultReader总个数.
	 */
	int getReaderCount() throws EternaException;

	/**
	 * 通过reader的名称获取一个ResultReader.
	 */
	ResultReader getReader(String name) throws EternaException;

	/**
	 * 根据索引值, 从reader列表中获取一个ResultReader.
	 * reader列表会随着列设置而改变.
	 * 第一个值为0, 第二个值为1, ...
	 */
	ResultReader getReader(int index) throws EternaException;

	/**
	 * 设置需要的<code>ResultReader</code>, 及排列顺序和查询的排序规则.
	 *
	 * @param names     存放<code>ResultReader</code>的名称及排序的数组,
	 *                  <code>ResultReader</code>将按这个数组所指定的顺序排列,
	 *                  并根据排序标记来设置排序.
	 *                  排序标记有:
	 *                  {@link #ORDER_FLAG_DESC}"-name" 表示此列降序
	 *                  {@link #ORDER_FLAG_ASC}"+name" 表示此列升序
	 *                  没有任何排序标记, 如"name" 表示此列不排序
	 */
	void setReaderList(String[] names) throws EternaException;

	/**
	 * 通过reader的名称获取该reader对象所在的索引值.
	 *
	 * @param name      reader的名称
	 * @param notThrow  设为<code>true<code>时, 当对应名称的reader不存在时
	 *                  不会抛出异常, 而只是返回-1
	 * @return  reader所在的索引值, 第一个值为0, 第二个值为1, ...
	 *          或-1(当对应名称的reader不存在时)
	 *
	 */
	int getReaderIndex(String name, boolean notThrow) throws EternaException;

	/**
	 * 通过reader的名称获取该reader对象所在的索引值.
	 *
	 * @param name      reader的名称
	 * @return  reader所在的索引值, 第一个为0, 第二个为1, ...
	 * @throws EternaException  当对应名称的reader不存在时
	 */
	int getReaderIndex(String name) throws EternaException;

	/**
	 * 获取用于排序的SQL子语句.
	 */
	String getOrderByString() throws EternaException;

	/**
	 * 获得一个<code>ResultReader</code>的列表.
	 * 此方法列出的是所有的<code>ResultReader</code>.
	 * 无论setReaderList设置了怎样的值, 都是返回所有的.
	 *
	 * @return  用于读取数据的所有<code>ResultReader</code>的列表.
	 * @throws EternaException  当相关配置出错时
	 * @see #setReaderList
	 */
	List getReaderList() throws EternaException;

	/**
	 * 根据权限, 获得一个<code>ResultReader</code>的列表.
	 * 如果setReaderList设置了显示的<code>ResultReader</code>, 那返回的列表只会在
	 * 此范围内.
	 * 如果某个列没有读取权限的话, 那相应的列会替换为<code>NullResultReader</code>
	 * 的实例.
	 *
	 * @return  正式用于读取数据的<code>ResultReader</code>的列表.
	 * @throws EternaException  当相关配置出错时
	 * @see #setReaderList
	 */
	List getReaderList(Permission permission) throws EternaException;

	/**
	 * 判断是否已锁住所有属性, 这样使用者只能读取, 而不能修改. <p>
	 * 被复制后的ResultReaderManager将会解除锁定.
	 *
	 * @return  true表示已锁, false表示未锁
	 * @see #copy()
	 */
	boolean isLocked();

	/**
	 * 复制自身的所有属性, 并返回.
	 */
	ResultReaderManager copy() throws EternaException;

}