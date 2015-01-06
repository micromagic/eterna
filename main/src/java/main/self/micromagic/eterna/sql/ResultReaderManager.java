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

package self.micromagic.eterna.sql;

import java.util.List;

import self.micromagic.eterna.share.EternaException;
import self.micromagic.eterna.security.Permission;
import self.micromagic.eterna.share.EternaFactory;

public interface ResultReaderManager
{
	/**
	 * 初始化本ResultReaderManager对象, 系统会在初始化时调用此方法. <p>
	 * 该方法的主要作用是初始化每个ResultReader对象, 并根据父对象来组成自己
	 * 自己的reader列表.
	 *
	 * @param factory  EternaFactory的实例, 可以从中获得父对象
	 */
	void initialize(EternaFactory factory) throws EternaException;

	/**
	 * 设置本ResultReaderManager的名称.
	 */
	void setName(String name) throws EternaException;

	/**
	 * 获取本ResultReaderManager的名称.
	 */
	String getName() throws EternaException;

	/**
	 * 设置父ResultReaderManager的名称.
	 */
	void setParentName(String name) throws EternaException;

	/**
	 * 获取父ResultReaderManager的名称.
	 */
	String getParentName() throws EternaException;

	/**
	 * 设置本ResultReaderManager的名称.
	 */
	ResultReaderManager getParent() throws EternaException;

	/**
	 * 获取生成本ResultReaderManager的工厂.
	 */
	EternaFactory getFactory() throws EternaException;

	/**
	 * 设置列名是否是大小写敏感的, 默认的是敏感的. <p>
	 * 注: 只有在没有添加reader的时候才能设置这个属性.
	 */
	void setColNameSensitive(boolean colNameSensitive) throws EternaException;

	/**
	 * 获取列名是否大小写敏感的. <p>
	 *
	 * @return  true为大小写敏感的, false为不区分大小写
	 */
	boolean isColNameSensitive() throws EternaException;

	/**
	 * 获得ResultReader的排序方式字符串.
	 */
	String getReaderOrder() throws EternaException;

	/**
	 * 设置ResultReader的排序方式字符串.
	 */
	void setReaderOrder(String readerOrder) throws EternaException;

	/**
	 * 获得ResultReaderManager中的ResultReader总个数, 这个数字一般等于或大于
	 * ReaderList中的ResultReader个数. <p>
	 *
	 * @see #getReaderList(Permission)
	 * @see #getReaderInList(int)
	 */
	int getReaderCount() throws EternaException;

	/**
	 * 通过reader的名称获取一个ResultReader.
	 */
	ResultReader getReader(String name) throws EternaException;

	/**
	 * 添加一个<code>ResultReader</code>.
	 * <p>如果该<code>ResultReader</code>的名称已经存在, 则会覆盖原来的reader.
	 *
	 * @param reader  要添加的<code>ResultReader</code>
	 * @return     当该<code>ResultReader</code>的名称已经存在时则返回被覆盖掉
	 *             的<code>ResultReader</code>, 否则返回<code>null</code>.
	 * @throws EternaException  当相关配置出错时
	 */
	ResultReader addReader(ResultReader reader) throws EternaException;

	/**
	 * 设置<code>ResultReader</code>的排列顺序以及查询的排序规则.
	 *
	 * @param names     存放<code>ResultReader</code>的名称级排序的数组,
	 *                  <code>ResultReader</code>将按这个数组所指定的顺序排列,
	 *                  并根据他来设置排序.
	 *                  列名及排序的格式为[名称][排序(1个字符)].
	 *                  排序分别为: "-"无, "A"升序, "D"降序.
	 *
	 * @throws EternaException  当相关配置出错时
	 */
	void setReaderList(String[] names) throws EternaException;

	/**
	 * 通过reader的名称获取该reader对象所在的索引值.
	 *
	 * @param name      reader的名称
	 * @param notThrow  设为<code>true<code>时, 当对应名称的reader不存在时
	 *                  不会抛出异常, 而只是返回-1
	 * @return  reader所在的索引值, 或-1(当对应名称的reader不存在时)
	 *          第一个值为1, 第二个值为2, ...
	 */
	int getIndexByName(String name, boolean notThrow) throws EternaException;

	/**
	 * 通过reader的名称获取该reader对象所在的索引值.
	 */
	int getIndexByName(String name) throws EternaException;

	/**
	 * 获取用于排序的sql子语句.
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
	 * 根据索引值, 从reader列表中获取一个ResultReader.
	 * reader列表会随着列设置而改变.
	 * 第一个值为0, 第二个值为1, ...
	 */
	ResultReader getReaderInList(int index) throws EternaException;

	/**
	 * 锁住自己的所有属性, 这样使用者只能读取, 而不能修改. <p>
	 * 一般用在通过xml装载后, 在EternaFactory的初始化中调用此方法.
	 * 注:在调用了copy方法后, 新复制的ResultReaderManager是不被锁住的.
	 *
	 * @see #copy(String)
	 */
	void lock() throws EternaException;

	/**
	 * 判断是否已锁住所有属性, 这样使用者只能读取, 而不能修改. <p>
	 *
	 * @return  true表示已锁, false表示未锁
	 * @see #lock
	 */
	boolean isLocked() throws EternaException;

	/**
	 * 复制自身的所有属性, 并返回.
	 * 当copyName不为null时, 名称将改为:"[原name]+[copyName]".
	 * 反之名称将不会改变.
	 */
	ResultReaderManager copy(String copyName) throws EternaException;

}