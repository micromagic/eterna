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

package self.micromagic.eterna.sql.preparer;

import java.io.InputStream;
import java.io.Reader;

import self.micromagic.eterna.share.EternaException;
import self.micromagic.eterna.share.Generator;
import self.micromagic.eterna.share.EternaFactory;

/**
 * 值准备器的创建者的构造者.
 * 用于生成一个值准备器的创建者, 而值准备器的创建者可产生一个值准备器.
 */
public interface ValuePreparerCreaterGenerator extends Generator
{
	/**
	 * 初始化此构造者.
	 */
	void initialize(EternaFactory factory) throws EternaException;

	/**
	 * 获取生成此构造者的工厂.
	 */
	EternaFactory getFactory() throws EternaException;

	/**
	 * 是否要将空字符串变为null.
	 */
	boolean isEmptyStringToNull();

	/**
	 * 根据type类型生成相关的值准备器的创建者.
	 *
	 * @return   值准备器的创建者
	 */
	ValuePreparerCreater createValuePreparerCreater(int pureType) throws EternaException;

	/**
	 * 生成一个null类型的值准备器.
	 *
	 * @param index   对应参数的相对索引值
	 * @param type    在<code>java.sql.Types</code>中定义的SQL型
	 */
	ValuePreparer createNullPreparer(int index, int type) throws EternaException;

	/**
	 * 生成一个boolean类型的值准备器.
	 *
	 * @param index   对应参数的相对索引值
	 * @param v       值
	 */
	ValuePreparer createBooleanPreparer(int index, boolean v) throws EternaException;

	/**
	 * 生成一个byte类型的值准备器.
	 *
	 * @param index   对应参数的相对索引值
	 * @param v       值
	 */
	ValuePreparer createBytePreparer(int index, byte v) throws EternaException;

	/**
	 * 生成一个byte数组类型的值准备器.
	 *
	 * @param index   对应参数的相对索引值
	 * @param v       值
	 */
	ValuePreparer createBytesPreparer(int index, byte[] v) throws EternaException;

	/**
	 * 生成一个short类型的值准备器.
	 *
	 * @param index   对应参数的相对索引值
	 * @param v       值
	 */
	ValuePreparer createShortPreparer(int index, short v) throws EternaException;

	/**
	 * 生成一个int类型的值准备器.
	 *
	 * @param index   对应参数的相对索引值
	 * @param v       值
	 */
	ValuePreparer createIntPreparer(int index, int v) throws EternaException;

	/**
	 * 生成一个long类型的值准备器.
	 *
	 * @param index   对应参数的相对索引值
	 * @param v       值
	 */
	ValuePreparer createLongPreparer(int index, long v) throws EternaException;

	/**
	 * 生成一个float类型的值准备器.
	 *
	 * @param index   对应参数的相对索引值
	 * @param v       值
	 */
	ValuePreparer createFloatPreparer(int index, float v) throws EternaException;

	/**
	 * 生成一个double类型的值准备器.
	 *
	 * @param index   对应参数的相对索引值
	 * @param v       值
	 */
	ValuePreparer createDoublePreparer(int index, double v) throws EternaException;

	/**
	 * 生成一个String类型的值准备器.
	 *
	 * @param index   对应参数的相对索引值
	 * @param v       值
	 */
	ValuePreparer createStringPreparer(int index, String v) throws EternaException;

	/**
	 * 生成一个Stream类型的值准备器.
	 *
	 * @param index   对应参数的相对索引值
	 * @param v       值
	 */
	ValuePreparer createStreamPreparer(int index, InputStream v, int length) throws EternaException;

	/**
	 * 生成一个Reader类型的值准备器.
	 *
	 * @param index   对应参数的相对索引值
	 * @param v       值
	 */
	ValuePreparer createReaderPreparer(int index, Reader v, int length) throws EternaException;

	/**
	 * 生成一个Date类型的值准备器.
	 *
	 * @param index   对应参数的相对索引值
	 * @param v       值
	 */
	ValuePreparer createDatePreparer(int index, java.sql.Date v) throws EternaException;

	/**
	 * 生成一个Time类型的值准备器.
	 *
	 * @param index   对应参数的相对索引值
	 * @param v       值
	 */
	ValuePreparer createTimePreparer(int index, java.sql.Time v) throws EternaException;

	/**
	 * 生成一个Timestamp类型的值准备器.
	 *
	 * @param index   对应参数的相对索引值
	 * @param v       值
	 */
	ValuePreparer createTimestampPreparer(int index, java.sql.Timestamp v) throws EternaException;

	/**
	 * 生成一个Object类型的值准备器.
	 *
	 * @param index   对应参数的相对索引值
	 * @param v       值
	 */
	ValuePreparer createObjectPreparer(int index, Object v) throws EternaException;

}