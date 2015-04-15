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

import java.beans.PropertyEditor;
import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import self.micromagic.eterna.sql.ResultRow;
import self.micromagic.util.ResManager;
import self.micromagic.util.StringAppender;
import self.micromagic.util.StringTool;
import self.micromagic.util.Utility;
import self.micromagic.util.container.SynHashMap;
import self.micromagic.util.converter.BigIntegerConverter;
import self.micromagic.util.converter.BooleanConverter;
import self.micromagic.util.converter.ByteConverter;
import self.micromagic.util.converter.BytesConverter;
import self.micromagic.util.converter.CalendarConverter;
import self.micromagic.util.converter.CharacterConverter;
import self.micromagic.util.converter.DateConverter;
import self.micromagic.util.converter.DecimalConverter;
import self.micromagic.util.converter.DoubleConverter;
import self.micromagic.util.converter.FloatConverter;
import self.micromagic.util.converter.IntegerConverter;
import self.micromagic.util.converter.LongConverter;
import self.micromagic.util.converter.MapConverter;
import self.micromagic.util.converter.ReaderConverter;
import self.micromagic.util.converter.ShortConverter;
import self.micromagic.util.converter.StreamConverter;
import self.micromagic.util.converter.StringConverter;
import self.micromagic.util.converter.TimeConverter;
import self.micromagic.util.converter.TimestampConverter;
import self.micromagic.util.converter.UtilDateConverter;
import self.micromagic.util.converter.ValueConverter;

/**
 * 对bean进行操作的工具.
 *
 * @author micromagic@sina.com
 */
public class BeanTool
{
	/**
	 * 设置是否要使用默认的bean检查器.
	 */
	public static final String CG_USE_DBC_PROPERTY = "self.micromagic.cg.use.defaultBeanChecker";

	/**
	 * 获取对象类型中的某个属性.
	 *
	 * @param c     对象类型
	 * @param name  属性名称
	 * @return  获取到的属性对象, 如果不存在则返回null
	 */
	public static Field getField(Class c, String name)
	{
		if (c.isInterface())
		{
			return null;
		}
		Field f = null;
		try
		{
			f = c.getDeclaredField(name);
		}
		catch (Exception ex) {}
		if (f == null)
		{
			Class p = c.getSuperclass();
			if (p != null && p != Object.class)
			{
				f = getField(p, name);
			}
		}
		return f;
	}

	/**
	 * 通过map来对bean对象设置属性.
	 *
	 * @param bean    需要被设置属性的bean
	 * @param values  数据来源
	 * @return  被设置了的属性个数
	 */
	public static int setBeanValues(Object bean, Map values)
	{
		return setBeanValues(bean, values, "");
	}

	/**
	 * 通过map来对bean对象设置属性.
	 *
	 * @param bean    需要被设置属性的bean
	 * @param values	数据来源
	 * @param prefix	属性名词前缀
	 * @return  被设置了的属性个数
	 */
	public static int setBeanValues(Object bean, Map values, String prefix)
	{
		if (bean == null || values == null)
		{
			return 0;
		}
		MapToBean p = getMapToBean(bean.getClass());
		if (p == null)
		{
			return 0;
		}
		if (prefix == null)
		{
			prefix = "";
		}
		try
		{
			return p.setBeanValues(bean, values, prefix);
		}
		catch (RuntimeException ex)
		{
			throw ex;
		}
		catch (Exception ex)
		{
			throw new CGException(ex);
		}
	}

	/**
	 * 获得bean和map的转换工具.
	 *
	 * @param bean  要和map进行转换的bean
	 */
	public static BeanMap getBeanMap(Object bean)
	{
		return getBeanMap(bean, "");
	}

	/**
	 * 获得bean和map的转换工具.
	 *
	 * @param bean    要和map进行转换的bean
	 * @param prefix  属性名词前缀
	 */
	public static BeanMap getBeanMap(Object bean, String prefix)
	{
		if (bean == null)
		{
			return null;
		}
		Class beanClass = bean.getClass();
		BeanDescriptor bd = getBeanDescriptor(beanClass);
		if (bd == null)
		{
			return null;
		}
		if (prefix == null)
		{
			prefix = "";
		}
		return new BeanMap(bean, prefix, bd);
	}

	/**
	 * 获得bean和map的转换工具.
	 *
	 * @param beanType  要和map进行转换的bean的类型
	 * @param prefix    属性名词前缀
	 */
	public static BeanMap getBeanMap(Class beanType, String prefix)
	{
		if (beanType == null)
		{
			return null;
		}
		BeanDescriptor bd = getBeanDescriptor(beanType);
		if (bd == null)
		{
			return null;
		}
		if (prefix == null)
		{
			prefix = "";
		}
		return new BeanMap(beanType, prefix, bd);
	}

	/**
	 * 获得BeanMap中对bean属性的操作单元.
	 */
	public static CellDescriptor getBeanMapCell(Class type, String name)
	{
		BeanDescriptor db = getBeanDescriptor(type);
		if (db != null)
		{
			return db.getCell(name);
		}
		return null;
	}

	/**
	 * 获得BeanMap中对bean的构造单元.
	 */
	public static CellDescriptor getBeanMapInitCell(Class type)
	{
		BeanDescriptor db = getBeanDescriptor(type);
		if (db != null)
		{
			return db.getInitCell();
		}
		return null;
	}

	/**
	 * 移除BeanMap的结构信息, 移除后再次使用时就能够重新构造结构信息.
	 */
	public static void removeBeanDescriptor(Class type)
	{
		synchronized (beanDescriptorCache)
		{
			beanDescriptorCache.removeProperty(type);
		}
	}

	private static ClassKeyCache beanDescriptorCache = ClassKeyCache.getInstance();

	/**
	 * 获得对bean类的描述信息.
	 */
	public static BeanDescriptor getBeanDescriptor(Class beanClass)
	{
		if (beanClass == null)
		{
			return null;
		}
		BeanDescriptor bd = (BeanDescriptor) beanDescriptorCache.getProperty(beanClass);
		if (bd == null)
		{
			synchronized (beanDescriptorCache)
			{
				bd = getBeanDescriptor0(beanClass);
			}
		}
		return bd;
	}
	private static BeanDescriptor getBeanDescriptor0(Class beanClass)
	{
		BeanDescriptor bd = (BeanDescriptor) beanDescriptorCache.getProperty(beanClass);
		if (bd == null)
		{
			Map psInfo = new HashMap();
			Map tmp;
			Iterator tmpItr;
			try
			{
				String fnName = "public int setBeanValue(CellDescriptor cd, int[] indexs, Object bean, "
						+ "Object value, String prefix, BeanMap beanMap, Object originObj, Object oldValue)";
				String mh = StringTool.createStringAppender().append(fnName).appendln()
						.append("		throws Throwable").toString();
				String beginCode = StringTool.createStringAppender()
						.append("	int ").append(SETTED_COUNT_NAME).append(" = 0;").appendln()
						.toString();
				String endCode =  StringTool.createStringAppender()
						.append("	return ").append(SETTED_COUNT_NAME).append(';').toString();
				String[] imports = {
					ClassGenerator.getPackageString(Map.class),
					ClassGenerator.getPackageString(BeanTool.class),
					ClassGenerator.getPackageString(ResultRow.class),
					ClassGenerator.getPackageString(beanClass)
				};
				BeanPropertyWriteProcesser wp = new BeanPropertyWriteProcesser(
						"value", "beanMap", "originObj", "oldValue");
				tmp = createPropertyProcessers(beanClass, BeanPropertyWriter.class,
						mh, "bean", beginCode, endCode, wp, imports, BEAN_PROCESSER_TYPE_W);
				tmpItr = tmp.entrySet().iterator();
				while (tmpItr.hasNext())
				{
					Map.Entry entry = (Map.Entry) tmpItr.next();
					CellDescriptor bmc = (CellDescriptor) psInfo.get(entry.getKey());
					if (bmc == null)
					{
						bmc = new CellDescriptor();
						psInfo.put(entry.getKey(), bmc);
						bmc.setName((String) entry.getKey());
					}
					ProcesserInfo pi = (ProcesserInfo) entry.getValue();
					bmc.setWriteProcesser((BeanPropertyWriter) pi.processer);
					bmc.setCellType(pi.type);
					if (pi.type.isArray())
					{
						bmc.setArrayType(true);
					}
					else if (Collection.class.isAssignableFrom(pi.type))
					{
						bmc.setReadOldValue(true);
					}
					else if (checkBean(pi.type))
					{
						bmc.setBeanType(true);
					}
				}

				fnName = "public Object getBeanValue(CellDescriptor cd, int[] indexs, Object bean, "
						+ "String prefix, BeanMap beanMap)";
				mh = StringTool.createStringAppender().append(fnName).appendln()
						.append("		throws Throwable").toString();
				beginCode = endCode = "";
				BeanPropertyReadProcesser rp = new BeanPropertyReadProcesser(beanClass);
				tmp = createPropertyProcessers(beanClass, BeanPropertyReader.class,
						mh, "bean", beginCode, endCode, rp, imports, BEAN_PROCESSER_TYPE_R);
				tmpItr = tmp.entrySet().iterator();
				while (tmpItr.hasNext())
				{
					Map.Entry entry = (Map.Entry) tmpItr.next();
					CellDescriptor bmc = (CellDescriptor) psInfo.get(entry.getKey());
					if (bmc == null)
					{
						bmc = new CellDescriptor();
						psInfo.put(entry.getKey(), bmc);
						bmc.setName((String) entry.getKey());
					}
					ProcesserInfo pi = (ProcesserInfo) entry.getValue();
					if (bmc.getCellType() != null && bmc.getCellType() != pi.type)
					{
						CG.log.error("Error cell [" + ClassGenerator.getClassName(beanClass)
								+ "#" + entry.getKey() + "] type in create MapToBean, write:["
								+ bmc.getCellType() + "], read:[" + pi.type + "]");
						continue;
					}
					bmc.setReadProcesser((BeanPropertyReader) pi.processer);
					if (bmc.getCellType() == null)
					{
						bmc.setCellType(pi.type);
						if (pi.type.isArray())
						{
							bmc.setArrayType(true);
						}
						else if (Collection.class.isAssignableFrom(pi.type))
						{
							bmc.setReadOldValue(true);
						}
						else if (checkBean(pi.type))
						{
							bmc.setBeanType(true);
						}
					}
				}

				// 这里的fnName和前面的相同, 就不用重新赋值了
				beginCode = StringTool.createStringAppender().append(fnName).appendln()
						.append("		throws Throwable").appendln().append('{').toString();
				endCode = "}";
				String bodyCode = "return new " + ClassGenerator.getClassName(beanClass) + "();";
				CellDescriptor tmpBMC = null;
				try
				{
					BeanPropertyReader tmpBPR = (BeanPropertyReader) createPropertyProcesser("P_init",
							beanClass, UnitProcesser.BeanProperty.class, BeanPropertyReader.class,
							beginCode, bodyCode, endCode, imports);
					((UnitProcesser.BeanProperty) tmpBPR).setMember(beanClass.getConstructor(new Class[0]));
					tmpBMC = new CellDescriptor();
					tmpBMC.setName("<init>");
					tmpBMC.setReadProcesser(tmpBPR);
					tmpBMC.setCellType(beanClass);
					tmpBMC.setBeanType(true);
				}
				catch (RuntimeException ex)
				{
					throw ex;
				}
				catch (Throwable ex) {}

				bd = new BeanDescriptor(beanClass, psInfo, tmpBMC);
			}
			catch (Throwable ex)
			{
				CG.log.error("Error in create MapToBean.", ex);
				if (ex instanceof RuntimeException)
				{
					throw (RuntimeException) ex;
				}
				else if (ex instanceof Error)
				{
					throw (Error) ex;
				}
				throw new CGException(ex);
			}
		}
		beanDescriptorCache.setProperty(beanClass, bd);
		return bd;
	}

	private static ClassKeyCache mapToBeanCache = ClassKeyCache.getInstance();
	/**
	 * 获得将map的值设置到bean属性中的处理类.
	 */
	private static MapToBean getMapToBean(Class beanClass)
	{
		Object obj = mapToBeanCache.getProperty(beanClass);
		if (obj == null)
		{
			synchronized (mapToBeanCache)
			{
				obj = getMapToBean0(beanClass);
			}
		}
		return (MapToBean) obj;
	}
	private static MapToBean getMapToBean0(Class beanClass)
	{
		Object obj = mapToBeanCache.getProperty(beanClass);
		if (obj == null)
		{
			try
			{
				String mh = StringTool.createStringAppender()
						.append("public int setBeanValues(Object bean, Map values, String prefix)").appendln()
						.append("		throws Throwable").toString();
				String beginCode = StringTool.createStringAppender()
						.append("	Object ").append(TMP_OBJ_NAME).append(';').appendln()
						.append("	int ").append(SETTED_COUNT_NAME).append(" = 0;").appendln()
						.append("	String ").append(TMP_STR_NAME).append(';').appendln()
						.toString();
				String endCode =  StringTool.createStringAppender()
						.append("	return ").append(SETTED_COUNT_NAME).append(';').toString();
				String[] imports = {
					ClassGenerator.getPackageString(Map.class),
					ClassGenerator.getPackageString(BeanTool.class),
					ClassGenerator.getPackageString(beanClass)
				};
				MapToBeanProcesser p = new MapToBeanProcesser("values");
				obj = createBeanProcesser(beanClass, MapToBean.class, mh,
						"bean", beginCode, endCode, p, imports, BEAN_PROCESSER_TYPE_W);
			}
			catch (Throwable ex)
			{
				CG.log.error("Error in create MapToBean.", ex);
				if (ex instanceof RuntimeException)
				{
					throw (RuntimeException) ex;
				}
				else if (ex instanceof Error)
				{
					throw (Error) ex;
				}
				throw new CGException(ex);
			}
		}
		if (obj != null)
		{
			mapToBeanCache.setProperty(beanClass, obj);
		}
		return (MapToBean) obj;
	}


	/**
	 * 获得bean类中的所有公有非静态的属性
	 */
	public static Field[] getBeanFields(Class c)
	{
		List result = new ArrayList();
		Field[] fs = c.getFields();
		for (int i = 0; i < fs.length; i++)
		{
			Field f = fs[i];
			if (!Modifier.isStatic(f.getModifiers()))
			{
				result.add(f);
			}
		}
		return (Field[]) result.toArray(new Field[result.size()]);
	}

	/**
	 * 获得bean类中的所有公开的get方法
	 */
	public static BeanMethodInfo[] getBeanReadMethods(Class c)
	{
		List result = new ArrayList();
		BeanMethodInfo[] infos = BeanMethodInfo.getBeanMethods(c);
		for (int i = 0; i < infos.length; i++)
		{
			BeanMethodInfo info = infos[i];
			if (info.doGet)
			{
				result.add(info);
			}
		}
		return (BeanMethodInfo[]) result.toArray(new BeanMethodInfo[result.size()]);
	}

	/**
	 * 获得bean类中的所有公开的set方法
	 */
	public static BeanMethodInfo[] getBeanWriteMethods(Class c)
	{
		List result = new ArrayList();
		BeanMethodInfo[] infos = BeanMethodInfo.getBeanMethods(c);
		for (int i = 0; i < infos.length; i++)
		{
			BeanMethodInfo info = infos[i];
			if (!info.doGet)
			{
				result.add(info);
			}
		}
		return (BeanMethodInfo[]) result.toArray(new BeanMethodInfo[result.size()]);
	}

	/**
	 * 处理读取的过程.
	 */
	public static int BEAN_PROCESSER_TYPE_R = 0;

	/**
	 * 处理写入的过程.
	 */
	public static int BEAN_PROCESSER_TYPE_W = 1;

	/**
	 * 生成一个bean的处理类.
	 *
	 * @param suffix             生成类名的后缀
	 * @param beanClass          bean类
	 * @param interfaceClass     处理接口
	 * @param methodHead         方法头部
	 * @param beanParamName      bean参数的名称
	 * @param unitTemplate       单元代码模板
	 * @param primitiveTemplate  基本类型单元代码模板
	 * @param linkTemplate       两个类型单元之间的连接模板
	 * @param imports            要引入的包
	 * @param processerType      处理的是设置的过程还是读取的过程
	 * @return  返回相应的处理类
	 */
	public static Object createBeanProcesser(String suffix, Class beanClass, Class interfaceClass, String methodHead,
			String beanParamName, String unitTemplate, String primitiveTemplate, String linkTemplate,
			String[] imports, int processerType)
	{
		ClassGenerator cg = ClassGenerator.createClassGenerator(suffix, beanClass, interfaceClass, imports);
		StringAppender function = StringTool.createStringAppender(256);
		function.append(methodHead).appendln().append('{').appendln();
		function.append("	").append(ClassGenerator.getClassName(beanClass)).append(' ').append(BeanTool.BEAN_NAME)
				.append(" = (").append(ClassGenerator.getClassName(beanClass)).append(") ").append(beanParamName)
				.append(';').appendln();
		boolean first = true;

		Map dataMap = new HashMap();

		Field[] fields = BeanTool.getBeanFields(beanClass);
		for (int i = 0; i < fields.length; i++)
		{
			if (processerType == BEAN_PROCESSER_TYPE_W && Modifier.isFinal(fields[i].getModifiers()))
			{
				continue;
			}
			if (!first)
			{
				function.append(Utility.resolveDynamicPropnames(linkTemplate, dataMap, true)).appendln();
			}
			dataMap.clear();
			Field f = fields[i];
			dataMap.put("first", String.valueOf(first));
			dataMap.put("name", f.getName());
			dataMap.put("type", "field");
			if (f.getType().isPrimitive())
			{
				String pType = ClassGenerator.getClassName(f.getType());
				dataMap.put("primitive", pType);
				dataMap.put("value", "String.valueOf(" + BeanTool.BEAN_NAME + "." + f.getName() + ")");
				dataMap.put("o_value", BeanTool.BEAN_NAME + "." + f.getName());
				dataMap.put("wrapName", BeanTool.getPrimitiveWrapClassName(pType));
				function.append(Utility.resolveDynamicPropnames(primitiveTemplate, dataMap, true))
						.appendln();
			}
			else
			{
				dataMap.put("value", BeanTool.BEAN_NAME + "." + f.getName());
				function.append(Utility.resolveDynamicPropnames(unitTemplate, dataMap, true)).appendln();
			}
			first = false;
		}
		BeanMethodInfo[] methods = processerType == BEAN_PROCESSER_TYPE_W ?
				BeanTool.getBeanWriteMethods(beanClass) : BeanTool.getBeanReadMethods(beanClass);
		for (int i = 0; i < methods.length; i++)
		{
			BeanMethodInfo m = methods[i];
			if (m.method != null)
			{
				if (!first)
				{
					function.append(Utility.resolveDynamicPropnames(linkTemplate, dataMap, true))
							.appendln();
				}
				dataMap.put("first", String.valueOf(first));
				dataMap.put("name", m.name);
				dataMap.put("type", "method");
				if (m.type.isPrimitive())
				{
					String pType = ClassGenerator.getClassName(m.type);
					dataMap.put("primitive", pType);
					dataMap.put("value",
							"String.valueOf(" + BeanTool.BEAN_NAME + "." + m.method.getName() + "())");
					dataMap.put("o_value", BeanTool.BEAN_NAME + "." + m.method.getName() + "()");
					dataMap.put("wrapName", BeanTool.getPrimitiveWrapClassName(pType));
					function.append(Utility.resolveDynamicPropnames(primitiveTemplate, dataMap, true))
							.appendln();
				}
				else
				{
					dataMap.put("value", BeanTool.BEAN_NAME + "." + m.method.getName() + "()");
					function.append(Utility.resolveDynamicPropnames(unitTemplate, dataMap, true))
							.appendln();
				}
				first = false;
			}
		}
		function.append('}');

		cg.addMethod(function.toString());
		cg.setClassLoader(beanClass.getClassLoader());
		try
		{
			return cg.createClass().newInstance();
		}
		catch (Throwable ex)
		{
			if (ClassGenerator.COMPILE_LOG_TYPE > CG.COMPILE_LOG_TYPE_ERROR)
			{
				CG.log.error("Error in create bean processer.", ex);
			}
			if (ex instanceof RuntimeException)
			{
				throw (RuntimeException) ex;
			}
			else if (ex instanceof Error)
			{
				throw (Error) ex;
			}
			throw new CGException(ex);
		}
	}

	/**
	 * 生成一个bean的处理类.
	 *
	 * @param beanClass       bean类
	 * @param interfaceClass  处理接口
	 * @param methodHead      方法头部
	 * @param beanParamName   bean参数的名称
	 * @param beginCode       方法开始部分的代码
	 * @param endCode         方法结束部分的代码
	 * @param unitProcesser   属性单元的处理器
	 * @param imports         要引入的包
	 * @param processerType   处理的是设置的过程还是读取的过程
	 * @return  返回相应的处理类
	 */
	static Object createBeanProcesser(Class beanClass, Class interfaceClass, String methodHead, String beanParamName,
			String beginCode, String endCode, UnitProcesser unitProcesser, String[] imports, int processerType)
	{
		ClassGenerator cg = ClassGenerator.createClassGenerator("Processer", beanClass, interfaceClass, imports);
		StringAppender function = StringTool.createStringAppender(256);
		function.append(methodHead).appendln().append('{').appendln();
		function.append("	").append(ClassGenerator.getClassName(beanClass)).append(' ').append(BeanTool.BEAN_NAME)
				.append(" = (").append(ClassGenerator.getClassName(beanClass)).append(") ").append(beanParamName)
				.append(';').appendln();
		function.append(beginCode).appendln();

		Field[] fields = getBeanFields(beanClass);
		for (int i = 0; i < fields.length; i++)
		{
			if (processerType == BEAN_PROCESSER_TYPE_W && Modifier.isFinal(fields[i].getModifiers()))
			{
				continue;
			}
			String code;
			Field f = fields[i];
			if (f.getType().isPrimitive())
			{
				String wrapName = (String) BeanTool.primitiveWrapClass.get(
						ClassGenerator.getClassName(f.getType()));
				code = unitProcesser.getFieldCode(f, f.getType(), wrapName, processerType, cg);
			}
			else
			{
				code = unitProcesser.getFieldCode(f, f.getType(), null, processerType, cg);
			}
			function.append(code).appendln();
		}
		BeanMethodInfo[] methods = processerType == BEAN_PROCESSER_TYPE_W ?
				getBeanWriteMethods(beanClass) : getBeanReadMethods(beanClass);
		for (int i = 0; i < methods.length; i++)
		{
			String code;
			BeanMethodInfo m = methods[i];
			if (m.type.isPrimitive())
			{
				String wrapName = (String) BeanTool.primitiveWrapClass.get(ClassGenerator.getClassName(m.type));
				code = unitProcesser.getMethodCode(m, m.type, wrapName, processerType, cg);
			}
			else
			{
				code = unitProcesser.getMethodCode(m, m.type, null, processerType, cg);
			}
			if (!StringTool.isEmpty(code))
			{
				function.append(code).appendln();
			}
		}
		function.append(endCode).appendln().append('}');

		cg.addMethod(function.toString());
		cg.setClassLoader(beanClass.getClassLoader());
		try
		{
			return cg.createClass().newInstance();
		}
		catch (Throwable ex)
		{
			if (ClassGenerator.COMPILE_LOG_TYPE > CG.COMPILE_LOG_TYPE_ERROR)
			{
				CG.log.error("Error in create bean processer.", ex);
			}
			if (ex instanceof RuntimeException)
			{
				throw (RuntimeException) ex;
			}
			else if (ex instanceof Error)
			{
				throw (Error) ex;
			}
			throw new CGException(ex);
		}
	}

	/**
	 * 对一个bean生成一组属性的处理类.
	 *
	 * @param beanClass       bean类
	 * @param interfaceClass  处理接口
	 * @param methodHead      方法头部
	 * @param beanParamName   bean参数的名称
	 * @param beginCode       方法开始部分的代码
	 * @param endCode         方法结束部分的代码
	 * @param unitProcesser   属性单元的处理器
	 * @param imports         要引入的包
	 * @param processerType   处理的是设置的过程还是读取的过程
	 * @return  返回相应的处理类
	 */
	static Map createPropertyProcessers(Class beanClass, Class interfaceClass, String methodHead, String beanParamName,
			String beginCode, String endCode, UnitProcesser unitProcesser, String[] imports, int processerType)
	{
		Map result = new HashMap();
		String beginCode0 = StringTool.createStringAppender(256)
				.append(methodHead).appendln().append('{').appendln()
				.append("	").append(ClassGenerator.getClassName(beanClass)).append(' ').append(BeanTool.BEAN_NAME)
				.append(" = (").append(ClassGenerator.getClassName(beanClass)).append(") ").append(beanParamName)
				.append(';').appendln().append(beginCode).toString();
		String endCode0 = StringTool.isEmpty(endCode) ? "}"
				: StringTool.createStringAppender(endCode, 5, false).appendln().append('}').toString();

		Field[] fields = getBeanFields(beanClass);
		for (int i = 0; i < fields.length; i++)
		{
			if (processerType == BEAN_PROCESSER_TYPE_W && Modifier.isFinal(fields[i].getModifiers()))
			{
				continue;
			}
			String code;
			Field f = fields[i];
			String wrapName = null;
			if (f.getType().isPrimitive())
			{
				wrapName = (String) BeanTool.primitiveWrapClass.get(ClassGenerator.getClassName(f.getType()));
			}
			ClassGenerator cg = ClassGenerator.createClassGenerator("P_" + f.getName(),
					beanClass, UnitProcesser.BeanProperty.class, interfaceClass, imports);
			code = unitProcesser.getFieldCode(f, f.getType(), wrapName, processerType, cg);
			Object p = createPropertyProcesser(cg, beanClass, beginCode0, code, endCode0);
			((UnitProcesser.BeanProperty) p).setMember(f);
			result.put(f.getName(), new ProcesserInfo(f.getName(), f.getType(), p));
		}
		BeanMethodInfo[] methods = processerType == BEAN_PROCESSER_TYPE_W ?
				getBeanWriteMethods(beanClass) : getBeanReadMethods(beanClass);
		for (int i = 0; i < methods.length; i++)
		{
			String code;
			BeanMethodInfo m = methods[i];
			String wrapName = null;
			if (m.type.isPrimitive())
			{
				wrapName = (String) BeanTool.primitiveWrapClass.get(ClassGenerator.getClassName(m.type));
			}
			ClassGenerator cg = ClassGenerator.createClassGenerator("P_" + m.name,
					beanClass, UnitProcesser.BeanProperty.class, interfaceClass, imports);
			code = unitProcesser.getMethodCode(m, m.type, wrapName, processerType, cg);
			if (!StringTool.isEmpty(code))
			{
				Object p = createPropertyProcesser(cg, beanClass, beginCode0, code, endCode0);
				((UnitProcesser.BeanProperty) p).setMember(m.method == null ? m.indexedMethod : m.method);
				result.put(m.name, new ProcesserInfo(m.name, m.type, p));
			}
		}

		return result;
	}

	/**
	 * 生成一个属性的处理类.
	 *
	 * @param cg         生成处理类的代码生成器
	 * @param beanClass  bean类
	 * @param beginCode  方法起始部分的代码
	 * @param bodyCode   方法主题部分的代码
	 * @param endCode    方法结束部分的代码
	 * @return  返回相应的处理类
	 */
	static Object createPropertyProcesser(ClassGenerator cg, Class beanClass, String beginCode,
			String bodyCode, String endCode)
	{
		StringAppender function = StringTool.createStringAppender(256);
		function.append(beginCode).appendln()
				.append(bodyCode).appendln()
				.append(endCode).appendln();
		cg.addMethod(function.toString());
		cg.setClassLoader(beanClass.getClassLoader());
		try
		{
			return cg.createClass().newInstance();
		}
		catch (Throwable ex)
		{
			if (ClassGenerator.COMPILE_LOG_TYPE > CG.COMPILE_LOG_TYPE_ERROR)
			{
				CG.log.error("Error in create bean processer.", ex);
			}
			if (ex instanceof RuntimeException)
			{
				throw (RuntimeException) ex;
			}
			else if (ex instanceof Error)
			{
				throw (Error) ex;
			}
			throw new CGException(ex);
		}
	}

	/**
	 * 生成一个属性的处理类.
	 *
	 * @param suffix          生成类名的后缀
	 * @param beanClass       bean类
	 * @param interfaceClass  处理接口
	 * @param beginCode       方法起始部分的代码
	 * @param bodyCode        方法主题部分的代码
	 * @param endCode         方法结束部分的代码
	 * @param imports         要引入的包
	 * @return  返回相应的处理类
	 */
	static Object createPropertyProcesser(String suffix, Class beanClass, Class interfaceClass,
			String beginCode, String bodyCode, String endCode, String[] imports)
	{
		return createPropertyProcesser(suffix, beanClass, null, interfaceClass,
				beginCode, bodyCode, endCode, imports);
	}

	/**
	 * 生成一个属性的处理类.
	 *
	 * @param suffix          生成类名的后缀
	 * @param beanClass       bean类
	 * @param superClass      需要继承的类
	 * @param interfaceClass  处理类的接口
	 * @param beginCode       方法起始部分的代码
	 * @param bodyCode        方法主题部分的代码
	 * @param endCode         方法结束部分的代码
	 * @param imports         要引入的包
	 * @return  返回相应的处理类
	 */
	static Object createPropertyProcesser(String suffix, Class beanClass, Class superClass,
			Class interfaceClass, String beginCode, String bodyCode, String endCode, String[] imports)
	{
		ClassGenerator cg = ClassGenerator.createClassGenerator(
				suffix, beanClass, superClass, interfaceClass, imports);
		StringAppender function = StringTool.createStringAppender(256);
		function.append(beginCode).appendln()
				.append(bodyCode).appendln()
				.append(endCode).appendln();
		cg.addMethod(function.toString());
		cg.setClassLoader(beanClass.getClassLoader());
		try
		{
			return cg.createClass().newInstance();
		}
		catch (Throwable ex)
		{
			if (ClassGenerator.COMPILE_LOG_TYPE > CG.COMPILE_LOG_TYPE_ERROR)
			{
				CG.log.error("Error in create bean processer.", ex);
			}
			if (ex instanceof RuntimeException)
			{
				throw (RuntimeException) ex;
			}
			else if (ex instanceof Error)
			{
				throw (Error) ex;
			}
			throw new CGException(ex);
		}
	}

	/**
	 * 针对某个bean, 注册一个类型转换器.
	 */
	public static void registerConverter(Class beanClass, Class type, ValueConverter converter)
	{
		synchronized (converterManager)
		{
			BeanDescriptor bd = getBeanDescriptor(beanClass);
			if (bd.getConverterManager() == converterManager)
			{
				bd.setConverterManager(converterManager.copy());
			}
			bd.getConverterManager().registerConverter(type, converter);
		}
	}

	/**
	 * 针对某个bean, 注册一个类型转换时使用的<code>PropertyEditor</code>.
	 */
	public static void registerPropertyEditor(Class beanClass, Class type, PropertyEditor pe)
	{
		synchronized (converterManager)
		{
			BeanDescriptor bd = getBeanDescriptor(beanClass);
			if (bd.getConverterManager() == converterManager)
			{
				bd.setConverterManager(converterManager.copy());
			}
			bd.getConverterManager().registerPropertyEditor(type, pe);
		}
	}

	static final ConverterManager converterManager = new ConverterManager();

	/**
	 * 注册一个类型转换器.
	 */
	public static void registerConverter(Class type, ValueConverter converter)
	{
		converterManager.registerConverter(type, converter);
	}

	/**
	 * 注册一个类型转换时使用的<code>PropertyEditor</code>.
	 */
	public static void registerPropertyEditor(Class type, PropertyEditor pe)
	{
		converterManager.registerPropertyEditor(type, pe);
	}

	/**
	 * 根据转换器的索引值获取对应的转换器.
	 *
	 * @param type  转换目标的类型
	 */
	public static ValueConverter getConverter(Class type)
	{
		int index = converterManager.getConverterIndex(type);
		return index == -1 ? null : converterManager.getConverter(index);
	}

	/**
	 * 根据转换器的索引值获取对应的转换器.
	 *
	 * @param index  转换器的索引值
	 */
	public static ValueConverter getConverter(int index)
	{
		return converterManager.getConverter(index);
	}

	/**
	 * 获取对基础类型设置的代码.
	 */
	static StringAppender getPrimitiveSetCode(String wrapName, Class type, String converterBase,
			String resName, Map paramCache, StringAppender sa)
	{
		int vcIndex = converterManager.getConverterIndex(type);
		// 基本类型已全部注册, 这里不会有-1
		codeRes.printRes(GET_FIRST_VALUE_RES, paramCache, 1, sa).appendln();
		String typeName = ClassGenerator.getClassName(type);
		paramCache.put("declareType", typeName);
		paramCache.put("converterType", "self.micromagic.util.converter."
				+ wrapName + "Converter");
		paramCache.put("converterName", converterBase + ".getConverter(" + vcIndex + ")");
		paramCache.put("converterMethod",
				"convertTo" + Character.toUpperCase(typeName.charAt(0)) + typeName.substring(1));
		codeRes.printRes(resName, paramCache, 1, sa).appendln();
		return sa;
	}

	/**
	 * 基本类型对应的外覆类.
	 */
	static Map primitiveWrapClass = new HashMap();

	/**
	 * 如果给出的类名是一个基本类型, 则给出他的外覆类的类名.
	 * 否则返回<code>null</code>.
	 */
	public static String getPrimitiveWrapClassName(String className)
	{
		return (String) primitiveWrapClass.get(className);
	}

	/**
	 * 用于判断一个类名是否为bean的map.
	 */
	static Map beanClassNameCheckMap = new SynHashMap();

	/**
	 * 存放bean的类名模板的集合.
	 */
	static Set beanClassNamePatternSet = new HashSet();

	/**
	 * 注册作为bean的类, 多个类名之间用","或";"隔开.
	 */
	public static void registerBean(String classNames)
	{
		String[] names = StringTool.separateString(
				Utility.resolveDynamicPropnames(classNames), ",;", true);
		if (names == null)
		{
			return;
		}
		for (int i = 0; i < names.length; i++)
		{
			if (names[i].indexOf('*') != -1)
			{
				// 存在"*"号, 表示是一个名称模板
				beanClassNamePatternSet.add(names[i]);
			}
			else
			{
				beanClassNameCheckMap.put(names[i], Boolean.TRUE);
			}
		}
	}

	private static boolean CG_USE_DEFAULT_BEAN_CHECKER = true;
	/**
	 * 判断所给出的类名是否是bean.
	 */
	public static boolean checkBean(Class type)
	{
		String className = type.getName();
		Boolean beanType = (Boolean) beanClassNameCheckMap.get(className);
		if (beanType != null)
		{
			return beanType.booleanValue();
		}
		synchronized (beanCheckers)
		{
			if (beanCheckers.size() > 0)
			{
				Iterator itr = beanCheckers.iterator();
				while (itr.hasNext())
				{
					BeanChecker bc = (BeanChecker) itr.next();
					if (bc.check(type) == BeanChecker.CHECK_RESULT_YES)
					{
						beanClassNameCheckMap.put(className, Boolean.TRUE);
						return true;
					}
					else if (bc.check(type) == BeanChecker.CHECK_RESULT_NO)
					{
						return false;
					}
				}
			}
		}
		if (CG_USE_DEFAULT_BEAN_CHECKER)
		{
			if (defaultBeanChecker.check(type) == BeanChecker.CHECK_RESULT_YES)
			{
				beanClassNameCheckMap.put(className, Boolean.TRUE);
				return true;
			}
		}
		return false;
	}

	/**
	 * 一个用于检查给出的类名是否是一个bean的检测器列表.
	 */
	private static List beanCheckers = new LinkedList();

	private static BeanChecker defaultBeanChecker = new DefaultBeanChecker();

	/**
	 * 注册一般bean的检测器.
	 */
	public static void registerBeanChecker(BeanChecker bc)
	{
		synchronized (beanCheckers)
		{
			if (!beanCheckers.contains(bc))
			{
				beanCheckers.add(bc);
			}
		}
	}

	/**
	 * 去除一个bean的检测器.
	 */
	public static void removeBeanChecker(BeanChecker bc)
	{
		synchronized (beanCheckers)
		{
			beanCheckers.remove(bc);
		}
	}

	/**
	 * 各种类型转换器
	 */
	public static final BooleanConverter booleanConverter = new BooleanConverter();
	public static final ByteConverter byteConverter = new ByteConverter();
	public static final BytesConverter bytesConverter = new BytesConverter();
	public static final ShortConverter shortConverter = new ShortConverter();
	public static final IntegerConverter intConverter = new IntegerConverter();
	public static final LongConverter longConverter = new LongConverter();
	public static final FloatConverter floatConverter = new FloatConverter();
	public static final DoubleConverter doubleConverter = new DoubleConverter();
	public static final TimeConverter timeConverter = new TimeConverter();
	public static final DateConverter dateConverter = new DateConverter();
	public static final TimestampConverter timestampConverter = new TimestampConverter();
	public static final StringConverter stringConverter = new StringConverter();
	public static final StreamConverter streamConverter = new StreamConverter();
	public static final ReaderConverter readerConverter = new ReaderConverter();
	public static final BigIntegerConverter bigIntegerConverter = new BigIntegerConverter();
	public static final DecimalConverter decimalConverter = new DecimalConverter();
	public static final UtilDateConverter utilDateConverter = new UtilDateConverter();
	public static final CalendarConverter calendarConverter = new CalendarConverter();
	public static final CharacterConverter charConverter = new CharacterConverter();

	/**
	 * 代码段资源.
	 */
	static ResManager codeRes = new ResManager();

	/**
	 * 初始化代码资源及各种类型对应的转换器.
	 */
	static
	{
		try
		{
			codeRes.load(BeanTool.class.getResourceAsStream("BeanTool.res"));
			Utility.addFieldPropertyManager(CG_USE_DBC_PROPERTY, BeanTool.class,
					"CG_USE_DEFAULT_BEAN_CHECKER", "true");
		}
		catch (Exception ex)
		{
			CG.log.error("Error in get code res.", ex);
		}

		registerConverter(Boolean.class, booleanConverter);
		registerConverter(boolean.class, booleanConverter);
		registerConverter(Character.class, charConverter);
		registerConverter(char.class, charConverter);
		registerConverter(Byte.class, byteConverter);
		registerConverter(byte.class, byteConverter);
		registerConverter(Short.class, shortConverter);
		registerConverter(short.class, shortConverter);
		registerConverter(Integer.class, intConverter);
		registerConverter(int.class, intConverter);
		registerConverter(Long.class, longConverter);
		registerConverter(long.class, longConverter);
		registerConverter(Float.class, floatConverter);
		registerConverter(float.class, floatConverter);
		registerConverter(Double.class, doubleConverter);
		registerConverter(double.class, doubleConverter);
		registerConverter(String.class, stringConverter);
		registerConverter(byte[].class, bytesConverter);
		registerConverter(java.util.Date.class, utilDateConverter);
		registerConverter(java.sql.Time.class, timeConverter);
		registerConverter(java.sql.Date.class, dateConverter);
		registerConverter(java.sql.Timestamp.class, timestampConverter);
		registerConverter(Calendar.class, calendarConverter);
		registerConverter(InputStream.class, streamConverter);
		registerConverter(Reader.class, readerConverter);
		registerConverter(BigInteger.class, bigIntegerConverter);
		registerConverter(BigDecimal.class, decimalConverter);
		registerConverter(Map.class, new MapConverter());

		primitiveWrapClass.put("boolean", "Boolean");
		primitiveWrapClass.put("char", "Character");
		primitiveWrapClass.put("byte", "Byte");
		primitiveWrapClass.put("short", "Short");
		primitiveWrapClass.put("int", "Integer");
		primitiveWrapClass.put("long", "Long");
		primitiveWrapClass.put("float", "Float");
		primitiveWrapClass.put("double", "Double");
	}

	/**
	 * 存放bean对象的变量名.
	 */
	public static final String BEAN_NAME = "beanObj";

	/**
	 * 获取字符串数组第一个元素的代码资源名称
	 */
	static final String GET_FIRST_VALUE_RES = "getFirstValue";

	/**
	 * 存放读取的临时对象的变量名.
	 */
	static final String TMP_OBJ_NAME = "tmpObj";

	/**
	 * 属性描述类的变量名.
	 */
	static final String CELL_DESCRIPTOR_NAME = "cd";

	/**
	 * 当前的BeanMap对象的变量名.
	 */
	static final String BEAN_MAP_NAME = "beanMap";

	/**
	 * 存放设置的属性个数的变量名.
	 */
	static final String SETTED_COUNT_NAME = "settedCount";

	/**
	 * 存放需要读取的名称前缀的变量名.
	 */
	static final String PREFIX_NAME = "prefix";

	/**
	 * 存放需要读取的名称(即拼接上前缀后的名称)的变量名.
	 */
	static final String TMP_STR_NAME = "tmpStr";

	/**
	 * 数组或Collection使用的索引值列表.
	 */
	static final String INDEXS_NAME = "indexs";

	/**
	 * 定义的数组的名称.
	 */
	static final String DEF_ARRAY_NAME = "tmpArr";

	/**
	 * 处理者数组的名称.
	 */
	static final String PROCESSER_ARRAY_NAME = "processerArr";

}