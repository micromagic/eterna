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

package self.micromagic.cg;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import self.micromagic.util.StringAppender;
import self.micromagic.util.StringTool;
import self.micromagic.util.IntegerRef;

/**
 * 生成对bean属性设置的属性单元处理者.
 *
 * @author micromagic@sina.com
 */
class BeanPropertyWriteProcesser
		implements UnitProcesser
{
	protected Map paramCache = new HashMap();
	protected String beanMapName;

	public BeanPropertyWriteProcesser(String valueName, String beanMapName, String originName,
			String oldValueName)
	{
		this.paramCache.put("beanName", BeanTool.BEAN_NAME);
		this.paramCache.put("originObjName", originName);
		this.paramCache.put("tmpObjName", valueName);
		this.paramCache.put("settedCountName", BeanTool.SETTED_COUNT_NAME);
		this.paramCache.put("prefixName", BeanTool.PREFIX_NAME);
		this.paramCache.put("oldValueName", oldValueName);
		this.beanMapName = beanMapName;
	}

	public String getFieldCode(Field f, Class type, String wrapName, int processerType, ClassGenerator cg)
	{
		this.paramCache.put("pName", f.getName());
		this.paramCache.put("fieldName", f.getName());
		String[] resNames = new String[] {
			"beanMap.primitiveFieldSet", "convertTypeFieldSet",
			"beanMap.beanTypeFieldSet", "otherTypeFieldSet", "beanMap.arrayTypeFieldSet"
		};
		return this.getProcesserCode(type, f.getName(), wrapName, resNames, cg);
	}

	public String getMethodCode(BeanMethodInfo m, Class type, String wrapName, int processerType,
			ClassGenerator cg)
	{
		if (m.method == null)
		{
			return null;
		}
		this.paramCache.put("pName", m.name);
		this.paramCache.put("methodName", m.method.getName());
		String[] resNames = new String[] {
			"beanMap.primitiveMethodSet", "convertTypeMethodSet",
			"beanMap.beanTypeMethodSet", "otherTypeMethodSet", "beanMap.arrayTypeMethodSet"
		};
		return this.getProcesserCode(type, m.name, wrapName, resNames, cg);
	}

	protected String getProcesserCode(Class type, String pName, String wrapName, String[] resNames,
			ClassGenerator cg)
	{
		StringAppender sa = StringTool.createStringAppender(128);
		if (wrapName != null)
		{
			sa = BeanTool.getPrimitiveSetCode(wrapName, type, this.beanMapName, resNames[0],
					this.paramCache, sa);
		}
		else
		{
			int vcIndex = BeanTool.converterManager.getConverterIndex(type);
			if (vcIndex != -1)
			{
				BeanTool.codeRes.printRes(BeanTool.GET_FIRST_VALUE_RES, this.paramCache, 1, sa).appendln();
				this.paramCache.put("converterName", this.beanMapName + ".getConverter(" + vcIndex + ")");
				this.paramCache.put("className", ClassGenerator.getClassName(type));
				BeanTool.codeRes.printRes(resNames[1], this.paramCache, 1, sa).appendln();
			}
			else if (BeanTool.checkBean(type))
			{
				this.paramCache.put("className", ClassGenerator.getClassName(type));
				this.paramCache.put("tempItemName", "_tmpItem");
				BeanTool.codeRes.printRes("beanMap.convertBeanType", this.paramCache, 1, sa).appendln();
				BeanTool.codeRes.printRes(resNames[2], this.paramCache, 1, sa).appendln();
			}
			else if (type.isArray())
			{
				IntegerRef level = new IntegerRef();
				Class eType = ClassGenerator.getArrayElementType(type, level);
				this.paramCache.put("className", ClassGenerator.getClassName(type));
				this.paramCache.put("cellClass", ClassGenerator.getClassName(eType));
				this.paramCache.put("arrayLevel", level);
				this.paramCache.put("beanMapName", this.beanMapName);
				BeanTool.codeRes.printRes(resNames[4], this.paramCache, 1, sa).appendln();
			}
			else
			{
				this.paramCache.put("className", ClassGenerator.getClassName(type));
				BeanTool.codeRes.printRes(resNames[3], this.paramCache, 1, sa).appendln();
			}
		}
		return sa.toString();
	}

}