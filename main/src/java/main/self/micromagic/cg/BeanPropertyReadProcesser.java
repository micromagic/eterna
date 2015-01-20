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
import java.util.Collection;

import self.micromagic.util.StringAppender;
import self.micromagic.util.StringTool;
import self.micromagic.util.IntegerRef;

/**
 * 生成对bean属性读取的属性单元处理者.
 *
 * @author micromagic@sina.com
 */
class BeanPropertyReadProcesser
		implements UnitProcesser
{
	protected Map paramCache = new HashMap();
	protected Class beanClass;

	public BeanPropertyReadProcesser(Class beanClass)
	{
		this.beanClass = beanClass;
		this.paramCache.put("beanName", BeanTool.BEAN_NAME);
		this.paramCache.put("indexs", BeanTool.INDEXS_NAME);
		this.paramCache.put("cellDescriptor", BeanTool.CELL_DESCRIPTOR_NAME);
		this.paramCache.put("beanMap", BeanTool.BEAN_MAP_NAME);
		this.paramCache.put("prefixName", BeanTool.PREFIX_NAME);
		this.paramCache.put("arrayName", BeanTool.DEF_ARRAY_NAME);
	}

	public String getFieldCode(Field f, Class type, String wrapName, int processerType, ClassGenerator cg)
	{
		this.paramCache.put("fieldName", f.getName());
		String[] resNames = new String[] {
			"primitiveFieldGet", "otherTypeFieldGet", "arrayTypeFieldGet", "collectionTypeFieldGet"
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
		this.paramCache.put("methodName", m.method.getName());
		String[] resNames = new String[] {
			"primitiveMethodGet", "otherTypeMethodGet", "arrayTypeMethodGet", "collectionTypeMethodGet"
		};
		return this.getProcesserCode(type, m.name, wrapName, resNames, cg);
	}

	protected String getProcesserCode(Class type, String pName, String wrapName, String[] resNames,
			ClassGenerator cg)
	{
		StringAppender sa = StringTool.createStringAppender(128);
		if (wrapName != null)
		{
			this.paramCache.put("wrapName", wrapName);
			BeanTool.codeRes.printRes(resNames[0], this.paramCache, 1, sa).appendln();
		}
		else if (type.isArray())
		{
			this.appendArrayProcesserCode(pName, type, sa, resNames, cg);
		}
		else if (Collection.class.isAssignableFrom(type))
		{
			StringAppender tmpBuf = StringTool.createStringAppender(64);
			BeanTool.codeRes.printRes("collectionTypeIndexGet", this.paramCache, 0, tmpBuf);
			this.paramCache.put("getCollectionIndexCode", tmpBuf.toString());
			BeanTool.codeRes.printRes(resNames[3], this.paramCache, 1, sa).appendln();
		}
		else
		{
			BeanTool.codeRes.printRes(resNames[1], this.paramCache, 1, sa).appendln();
		}
		return sa.toString();
	}

	/**
	 * 获得对数组的读取处理代码.
	 */
	protected StringAppender appendArrayProcesserCode(String pName, Class type, StringAppender sa,
			String[] resNames, ClassGenerator cg)
	{
		IntegerRef level = new IntegerRef();
		Class eType = ClassGenerator.getArrayElementType(type, level);
		String arrVLStr1 = ClassGenerator.getArrayDefine(level.value);
		this.paramCache.put("arrayLevel", level);
		BeanTool.codeRes.printRes(resNames[2], this.paramCache, 1, sa).appendln();
		String tmpWrapName = null;
		String tmpResName = "arrayTypeOtherGet";
		if (eType.isPrimitive())
		{
			tmpWrapName = BeanTool.getPrimitiveWrapClassName(ClassGenerator.getClassName(eType));
			this.paramCache.put("wrapName", tmpWrapName);
		}
		String[] imports = new String[]{
			ClassGenerator.getPackageString(eType),
			ClassGenerator.getPackageString(BeanTool.class)
		};
		String fnName = "public Object getBeanValue(CellDescriptor cd, int[] indexs, Object arrObj, "
				+ "String prefix, BeanMap beanMap)";
		String beginCode = StringTool.createStringAppender().append(fnName).appendln()
				.append("      throws Throwable").appendln().append('{').appendln()
				.append(ClassGenerator.getClassName(eType)).append(arrVLStr1).append(' ')
				.append(BeanTool.DEF_ARRAY_NAME).append(" = (").append(ClassGenerator.getClassName(eType))
				.append(arrVLStr1).append(") arrObj;").toString();
		String endCode = "}";
		StringAppender arrVL2 = StringTool.createStringAppender();
		StringAppender arrDef = StringTool.createStringAppender();
		arrDef.append("private ").append(ClassGenerator.getClassName(BeanPropertyReader.class))
				.append("[] ").append(BeanTool.PROCESSER_ARRAY_NAME).append(';');
		cg.addField(arrDef.toString());
		StringAppender arrInit = StringTool.createStringAppender();
		arrInit.append("public ${").append(CG.THIS_NAME).append("}()").appendln()
				.append('{').appendln().append("this.").append(BeanTool.PROCESSER_ARRAY_NAME)
				.append(" = new ").append(ClassGenerator.getClassName(BeanPropertyReader.class))
				.append('[').append(level.value).append("];").appendln();
		for (int i = 0; i < level.value; i++)
		{
			arrVL2.append('[').append(BeanTool.INDEXS_NAME).append('[').append(i).append("]]");
			this.paramCache.put("arrayVisitList", arrVL2.toString());
			StringAppender bodyCode = StringTool.createStringAppender();
			if (i == level.value - 1 && tmpWrapName != null)
			{
				tmpResName = "arrayTypePrimitiveGet";
			}
			BeanTool.codeRes.printRes(tmpResName, this.paramCache, 0, bodyCode).appendln();
			Object defObj = BeanTool.createPropertyProcesser("P_arr_" + pName, this.beanClass,
					UnitProcesser.BeanProperty.class, BeanPropertyReader.class,
					beginCode, bodyCode.toString(), endCode, imports);
			arrInit.append("this.").append(BeanTool.PROCESSER_ARRAY_NAME).append('[').append(i)
					.append("] = new ").append(ClassGenerator.getClassName(defObj.getClass()))
					.append("();").appendln();
		}
		arrInit.append('}');
		cg.addConstructor(arrInit.toString());
		return sa;
	}

}