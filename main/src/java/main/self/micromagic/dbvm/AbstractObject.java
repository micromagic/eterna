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

package self.micromagic.dbvm;

import self.micromagic.eterna.dao.preparer.CreaterManager;
import self.micromagic.eterna.dao.preparer.PreparerCreater;
import self.micromagic.eterna.share.EternaException;
import self.micromagic.eterna.share.EternaFactory;
import self.micromagic.eterna.share.TypeManager;
import self.micromagic.util.StringAppender;
import self.micromagic.util.StringTool;
import self.micromagic.util.converter.BooleanConverter;
import self.micromagic.util.ref.BooleanRef;

/**
 * 抽象的各类对象.
 */
public abstract class AbstractObject
		implements ConstantDef
{
	public boolean initialize(EternaFactory factory)
		throws EternaException
	{
		if (this.typeDefiner == null)
		{
			this.factory = factory;
			this.typeDefiner = (TypeDefiner) factory.createObject(TYPE_DEF_NAME);
			this.columnDefiner = (ColumnDefiner) factory.createObject(COLUMN_DEF_NAME);
			this.indexDefiner = (IndexDefiner) factory.createObject(INDEX_DEF_NAME);
			this.preparerCreater = CreaterManager.createPreparerCreater(
					TypeManager.TYPE_STRING, null, factory);
			this.mutipleLine = boolConverter.convertToBoolean(
					factory.getAttribute(MUTIPLE_LINE_FLAG));
			return false;
		}
		return true;
	}
	protected boolean mutipleLine;
	protected TypeDefiner typeDefiner;
	protected ColumnDefiner columnDefiner;
	protected IndexDefiner indexDefiner;
	protected EternaFactory factory;
	protected PreparerCreater preparerCreater;

	public String getName()
		throws EternaException
	{
	return this.name;
	}
	public void setName(String name)
	{
	this.name = name;
	}
	protected String name;

	private static BooleanConverter boolConverter = new BooleanConverter();

	/**
	 * 生成默认值的表达式.
	 */
	protected static String makeDefaultExpression(ColumnDesc colDesc, BooleanRef drop)
	{
		if (colDesc.defaultValue == null)
		{
			return null;
		}
		if (colDesc.defaultValue.length() == 0)
		{
			if (drop != null)
			{
				drop.value = true;
			}
			return "default null";
		}
		if (TypeManager.isString(colDesc.typeId))
		{
			String dValue = StringTool.replaceAll(colDesc.defaultValue, "'", "''");
			return "default '".concat(dValue.concat("'"));
		}
		return "default ".concat(colDesc.defaultValue);
	}

	/**
	 * 处理文本中的常量定义.
	 */
	public String resolveConst(String text, EternaFactory factory)
	{
		if (text == null)
		{
			return text;
		}
		String constPrefix = "#const(";
		String constSuffix = ")";
		int startIndex = text.indexOf(constPrefix);
		if (startIndex == -1)
		{
			return text;
		}

		String tempStr = text;
		StringAppender result = StringTool.createStringAppender(text.length() + 32);
		int prefixLength = constPrefix.length();
		while (startIndex != -1)
		{
			result.append(tempStr.substring(0, startIndex));
			int endIndex = tempStr.indexOf(constSuffix, startIndex + prefixLength);
			if (endIndex != -1)
			{
				String cName = tempStr.substring(startIndex + prefixLength, endIndex);
				String tmp = factory.getConstantValue(cName);
				if (!StringTool.isEmpty(tmp))
				{
					result.append(tmp);
					tempStr = tempStr.substring(endIndex + constSuffix.length());
					startIndex = tempStr.indexOf(constPrefix);
				}
				else
				{
					throw new EternaException("Not found constant value [" + cName + "].");
				}
			}
			else
			{
				tempStr = tempStr.substring(startIndex);
				startIndex = -1;
			}
		}
		result.append(tempStr);
		return result.toString();
	}

}
