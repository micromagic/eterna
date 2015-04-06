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

package self.micromagic.eterna.search;

import self.micromagic.eterna.dao.preparer.ValuePreparer;
import self.micromagic.eterna.share.EternaException;
import self.micromagic.eterna.share.EternaFactory;
import self.micromagic.eterna.share.EternaObject;

/**
 * SQL语句条件的生成器. <p>
 * {@link self.micromagic.eterna.search.Condition}将通过该生成器来生成SQL条件。
 *
 * @author  micromagic@sina.com
 */
public interface ConditionBuilder extends EternaObject
{
	public static final String[] OPERATOR_NAMES = {
		"isNull", "notNull", "checkNull",
		"equal", "notEqual", "large", "below", "notLarge", "notBelow",
		"beginWith", "endWith", "include", "match"
	};

	public static ValuePreparer[] EMPTY_PREPARERS = new ValuePreparer[0];

	boolean initialize(EternaFactory factory) throws EternaException;

	public String getName() throws EternaException;

	public String getCaption() throws EternaException;

	/**
	 * 生成一个SQL条件.
	 *
	 * @param colName  要生成的条件的名称
	 * @param value    要生成的条件的值
	 * @param cp       与此条件生成器的相对应的ConditionProperty
	 * @return         所生成的条件, 及相关参数
	 */
	public Condition buildeCondition(String colName, Object value, ConditionProperty cp)
			throws EternaException;

}