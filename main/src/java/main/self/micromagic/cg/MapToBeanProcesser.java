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

import self.micromagic.eterna.share.Tool;
import self.micromagic.util.StringAppender;
import self.micromagic.util.StringTool;

/**
 * 生成map对bean设置类的属性单元处理者.
 *
 * @author micromagic@sina.com
 */
class MapToBeanProcesser
		implements UnitProcesser
{
	private static final String GET_MAP_VALUE_RES = "mapSet.getMapValue";

	protected Map paramCache = new HashMap();

	public MapToBeanProcesser(String mapName)
	{
		this.paramCache.put("beanName", BeanTool.BEAN_NAME);
		this.paramCache.put("mapName", mapName);
		this.paramCache.put("tmpObjName", BeanTool.TMP_OBJ_NAME);
		this.paramCache.put("settedCountName", BeanTool.SETTED_COUNT_NAME);
		this.paramCache.put("prefixName", BeanTool.PREFIX_NAME);
		this.paramCache.put("tmpStr", BeanTool.TMP_STR_NAME);
	}

	public String getFieldCode(Field f, Class type, String wrapName, int processerType, ClassGenerator cg)
	{
		this.paramCache.put("pName", f.getName());
		this.paramCache.put("fieldName", f.getName());
		String[] resNames = new String[] {
			"mapSet.primitiveFieldSet", "convertTypeFieldSet",
			"mapSet.beanTypeFieldSet", "otherTypeFieldSet"
		};
		return this.getProcesserCode(type, f.getName(), wrapName, resNames);
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
			"mapSet.primitiveMethodSet", "convertTypeMethodSet",
			"mapSet.beanTypeMethodSet", "otherTypeMethodSet"
		};
		return this.getProcesserCode(type, m.name, wrapName, resNames);
	}

	protected String getProcesserCode(Class type, String pName, String wrapName, String[] resNames)
	{
		StringAppender sa = StringTool.createStringAppender(128);
		BeanTool.codeRes.printRes(GET_MAP_VALUE_RES, this.paramCache, 1, sa).appendln();
		if (wrapName != null)
		{
			sa = BeanTool.getPrimitiveSetCode(wrapName, type, "BeanTool", resNames[0],
					this.paramCache, sa);
		}
		else
		{
			int vcIndex = BeanTool.converterManager.getConverterIndex(type);
			if (vcIndex != -1)
			{
				BeanTool.codeRes.printRes(BeanTool.GET_FIRST_VALUE_RES, this.paramCache, 1, sa).appendln();
				this.paramCache.put("converterName", "BeanTool.getConverter(" + vcIndex + ")");
				this.paramCache.put("className", ClassGenerator.getClassName(type));
				BeanTool.codeRes.printRes(resNames[1], this.paramCache, 1, sa).appendln();
			}
			else if (Tool.isBean(type))
			{
				this.paramCache.put("className", ClassGenerator.getClassName(type));
				BeanTool.codeRes.printRes(resNames[2], this.paramCache, 1, sa).appendln();
			}
			else
			{
				this.paramCache.put("className", ClassGenerator.getClassName(type));
				BeanTool.codeRes.printRes(resNames[3], this.paramCache, 1, sa).appendln();
			}
		}
		sa.appendln();
		return sa.toString();
	}

}