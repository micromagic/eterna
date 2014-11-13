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

package self.micromagic.eterna.digester;

import java.util.Arrays;

import org.apache.commons.digester.Digester;
import org.apache.commons.logging.Log;
import org.xml.sax.Attributes;
import self.micromagic.eterna.share.Generator;
import self.micromagic.eterna.share.Tool;
import self.micromagic.util.StringTool;
import self.micromagic.util.Utility;

/**
 * 各类属性的设置, 通过PropertySetRule来调用.
 *
 * @author micromagic@sina.com
 */
public abstract class PropertySetter
{
	protected static final Log log = FactoryManager.log;

	protected Digester digester;

	protected String methodName;
	protected int objectIndex = 0;

	/**
	 * @param methodName     设置属性调用的方法
	 */
	public PropertySetter(String methodName)
	{
		this.methodName = methodName;
	}

	/**
	 * @param methodName     设置属性调用的方法
	 * @param objectIndex    被设置属性对象在堆栈中的索引值
	 */
	public PropertySetter(String methodName, int objectIndex)
	{
		this.methodName = methodName;
		this.objectIndex = objectIndex;
	}

	/**
	 * 设置所属的解析器.
	 */
	public void setDigester(Digester digester)
	{
		this.digester = digester;
	}

	/**
	 * 设置属性对象在堆栈中的索引值.
	 */
	public void setObjectIndex(int index)
	{
		this.objectIndex = index;
	}

	/**
	 * 设置的属性是否必须存在.
	 */
	public abstract boolean isMustExist();

	/**
	 * 准备设置的属性.
	 *
	 * @param namespace      xml配置文件的命名空间
	 * @param name           当前xml元素的名称
	 * @param attributes     当前xml元素的属性集
	 * @return               准备好的属性对象
	 */
	public abstract Object prepareProperty(String namespace, String name, Attributes attributes)
			throws Exception;

	/**
	 * 根据文本内容准备设置的属性.
	 *
	 * @param namespace      xml配置文件的命名空间
	 * @param name           当前xml元素的名称
	 * @param text           当前xml元素的文本对象
	 * @return               准备好的属性对象
	 */
	public Object prepareProperty(String namespace, String name, BodyText text)
			throws Exception
	{
		return null;
	}

	/**
	 * 是否需要xml元素的文本内容.
	 */
	public boolean requireBodyValue()
	{
		return false;
	}

	/**
	 * 将准备好的属性设置到对象中.
	 */
	public abstract void setProperty()
			throws Exception;

	/**
	 * 准备属性并将其设置到对象中. <p>
	 * 其调用的代码如下:
	 * <blockquote><pre>
	 * this.prepareProperty(namespace, name, attributes);
	 * this.setProperty();
	 * </pre></blockquote>
	 *
	 * @param namespace      xml配置文件的命名空间
	 * @param name           当前xml元素的名称
	 * @param attributes     当前xml元素的属性集
	 */
	public void setProperty(String namespace, String name, Attributes attributes)
			throws Exception
	{
		this.prepareProperty(namespace, name, attributes);
		this.setProperty();
	}

}

/**
 * 单属性设置的公共类.
 *
 * @author micromagic@sina.com
 */
abstract class SinglePropertySetter extends PropertySetter
{
	public static final Class[] STRING_TYPE = new Class[]{String.class};
	public static final Class[] INTEGER_TYPE = new Class[]{int.class};
	public static final Class[] BOOLEAN_TYPE = new Class[]{boolean.class};

	protected String attributeName;
	protected String defaultValue;
	protected boolean mustExist;

	protected Object[] value;
	protected Class[] type;

	/**
	 * 是否要对获取的字符串进行intern处理. <p>
	 * 对一些频繁出现的字符错intern处理后可以节省内存使用的空间。
	 */
	boolean needIntern = true;

	/**
	 * @param attributeName   从xml属性集中获取值的名称
	 * @param methodName      设置属性调用的方法
	 * @param mustExist       xml属性集中是否必须存在需要的值
	 */
	public SinglePropertySetter(String attributeName, String methodName, boolean mustExist)
	{
		super(methodName);
		this.attributeName = attributeName;
		this.mustExist = mustExist;
	}

	/**
	 * @param attributeName   从xml属性集中获取值的名称
	 * @param methodName      设置属性调用的方法
	 * @param defaultValue    xml属性集中不存在在需要的值是使用的默认值
	 */
	public SinglePropertySetter(String attributeName, String methodName, String defaultValue)
	{
		super(methodName);
		this.attributeName = attributeName;
		this.defaultValue = defaultValue;
		this.mustExist = false;
	}

	/**
	 * 是否要对获取的字符串进行intern处理.
	 */
	public boolean isNeedIntern()
	{
		return this.needIntern;
	}

	/**
	 * 设置是否要对获取的字符串进行intern处理.
	 */
	public void setNeedIntern(boolean needIntern)
	{
		this.needIntern = needIntern;
	}

	/**
	 * 设置的属性是否必须存在.
	 */
	public boolean isMustExist()
	{
		return this.mustExist;
	}

	/**
	 * 从xml属性集中获取值.
	 *
	 * @param namespace      xml配置文件的命名空间
	 * @param name           当前xml元素的名称
	 * @param attributes     当前xml元素的属性集
	 */
	protected String getValue(String namespace, String name, Attributes attributes)
			throws InvalidAttributesException
	{
		if (this.attributeName == null)
		{
			return this.mustExist ? null : this.defaultValue;
		}
		String value = attributes.getValue(this.attributeName);
		if (value == null)
		{
			if (this.isMustExist())
			{
				throw new InvalidAttributesException("Not fount the attribute '"
						+ this.attributeName + "' in " + name + ".");
			}
			return this.needIntern ? StringTool.intern(this.defaultValue) : this.defaultValue;
		}
		return this.needIntern ? StringTool.intern(value) : value;
	}

	/**
	 * 将准备好的属性设置到对象中.
	 */
	public void setProperty()
			throws Exception
	{
		if (this.value == null)
		{
			return;
		}
		Object obj = this.digester.peek(this.objectIndex);
		try
		{
			Tool.invokeExactMethod(obj, this.methodName, this.value, this.type);
		}
		catch (Exception ex)
		{
			log.error("Method invoke error. method:" + this.methodName + "  param:"
					+ Arrays.asList(this.type) + "  obj:" + (obj == null ? null : obj.getClass())
					+ "  value:" + Arrays.asList(this.value));
			throw ex;
		}
	}

}

/**
 * 根据设置的类名构造对象, 并将其设置属性.
 *
 * @author micromagic@sina.com
 */
class ObjectPropertySetter extends SinglePropertySetter
{
	protected Class classType;
	protected Object theObject;
	protected boolean objectClassMustExist = true;

	public ObjectPropertySetter(String attributeName, String methodName,
			String className, Class classType)
	{
		super(attributeName, methodName, className);
		this.objectClassMustExist = className == null;
		this.classType = classType;
		this.type = new Class[]{this.classType};
	}

	/**
	 * 从xml属性集中(或默认值)获取类名并生成对象.
	 */
	protected void setMyObject(String namespace, String name, Attributes attributes)
			throws Exception
	{
		String cName = ObjectCreateRule.getClassName(
				this.attributeName, this.defaultValue, attributes);
		if (cName == null)
		{
			throw new InvalidAttributesException("Not fount the attribute '"
					+ this.attributeName + "' in " + name + ".");
		}

		this.digester.getLogger().debug("New " + cName);
		this.theObject = ObjectCreateRule.createObject(cName, this.objectClassMustExist);
	}

	public Object prepareProperty(String namespace, String name, Attributes attributes)
			throws Exception
	{
		this.setMyObject(namespace, name, attributes);
		ObjectCreateRule.checkType(this.classType, this.theObject);
		return this.theObject;
	}

	/**
	 * 设置value属性, 在setProperty方法中会调用此方法生成设置的值.
	 */
	protected void setMyValue()
			throws Exception
	{
		this.value = new Object[]{this.theObject};
	}

	public void setProperty()
			throws Exception
	{
		if (this.theObject == null)
		{
			return;
		}
		Object obj = this.digester.peek(this.objectIndex);
		this.setMyValue();
		try
		{
			Tool.invokeExactMethod(obj, this.methodName, this.value, this.type);
		}
		catch (Exception ex)
		{
			log.error("Method invoke error. method:" + this.methodName + "  param:"
					+ Arrays.asList(this.type) + "  obj:" + obj.getClass()
					+ "  value:" + Arrays.asList(this.value));
			throw ex;
		}
	}
}

/**
 * 设置Generator生成的属性.
 *
 * @author micromagic@sina.com
 */
class GeneratorPropertySetter extends ObjectPropertySetter
{
	protected Generator generator;
	protected boolean withName;

	public GeneratorPropertySetter(String attributeName, String methodName,
			String className, Class classType)
	{
		this(attributeName, methodName, className, classType, false);
	}

	public GeneratorPropertySetter(String attributeName, String methodName,
			String className, Class classType, boolean withName)
	{
		super(attributeName, methodName, className, classType);
		if (withName)
		{
			// withName 为false的情况, 在父类中设置过了.
			this.type = new Class[]{String.class, this.classType};
		}
		this.withName = withName;
	}

	public Object prepareProperty(String namespace, String name, Attributes attributes)
			throws Exception
	{
		this.setMyObject(namespace, name, attributes);
		ObjectCreateRule.checkType(Generator.class, this.theObject);
		this.generator = (Generator) this.theObject;
		return this.generator;
	}

	protected void setMyValue()
			throws Exception
	{
		this.generator.setFactory(FactoryManager.getCurrentFactory());
		if (this.withName)
		{
			this.value = new Object[]{this.generator.getName(), this.generator.create()};
		}
		else
		{
			this.value = new Object[]{this.generator.create()};
		}
	}

}

/**
 * 设置从堆栈中获取的属性.
 *
 * @author micromagic@sina.com
 */
class StackPropertySetter extends SinglePropertySetter
{
	protected Class classType;
	protected int stackObjectIndex = 0;

	public StackPropertySetter(String methodName, Class classType, int objectIndex)
	{
		super(null, methodName, true);
		this.objectIndex = objectIndex;
		this.classType = classType;
		this.type = new Class[]{this.classType};
	}

	public StackPropertySetter(String methodName, Class classType,
			int objectIndex, int stackIndex)
	{
		super(null, methodName, true);
		this.objectIndex = objectIndex;
		this.stackObjectIndex = stackIndex;
		this.classType = classType;
		this.type = new Class[]{this.classType};
	}

	public Object prepareProperty(String namespace, String name, Attributes attributes)
			throws Exception
	{
		Object obj = this.digester.peek(this.stackObjectIndex);
		if (obj instanceof Generator)
		{
			((Generator) obj).setFactory(FactoryManager.getCurrentFactory());
		}
		this.value = new Object[]{obj};
		return obj;
	}

}

/**
 * 设置String类型的属性.
 *
 * @author micromagic@sina.com
 */
class StringPropertySetter extends SinglePropertySetter
{
	public StringPropertySetter(String attributeName, String methodName, boolean mustExist)
	{
		super(attributeName, methodName, mustExist);
		this.type = STRING_TYPE;
	}

	public StringPropertySetter(String attributeName, String methodName, boolean mustExist,
			boolean needIntern)
	{
		super(attributeName, methodName, mustExist);
		this.type = STRING_TYPE;
		this.needIntern = needIntern;
	}

	public StringPropertySetter(String attributeName, String methodName, String defaultValue)
	{
		super(attributeName, methodName, defaultValue);
		this.type = STRING_TYPE;
	}

	public Object prepareProperty(String namespace, String name, Attributes attributes)
			throws Exception
	{
		String strValue = this.getValue(namespace, name, attributes);
		if (strValue == null)
		{
			this.value = null;
			return null;
		}

		this.value = new Object[]{strValue};
		return strValue;
	}

}

/**
 * 设置从body中获取的String类型的属性.
 */
class BodyPropertySetter extends StringPropertySetter
{
	private boolean trimLines = true;
	private boolean bodyTextTrimLines;

	private String noLineAttributeName = "noLine";
	private boolean noLine = false;
	private boolean bodyTextNoLine;

	private String needResolveAttributeName;
	private boolean needResolve;
	private boolean bodyTextNeedResolve;

	public BodyPropertySetter(String attributeName, String methodName)
	{
		super(attributeName, methodName, false);
		this.needIntern = false;
	}

	public BodyPropertySetter(String attributeName, String methodName, boolean trimLines)
	{
		super(attributeName, methodName, false);
		this.trimLines = trimLines;
		this.needIntern = false;
	}

	public BodyPropertySetter(String attributeName, String methodName, boolean trimLines,
			boolean needIntern)
	{
		this(attributeName, methodName, trimLines);
		this.needIntern = needIntern;
	}

	public void setNoLine(String attributeName, boolean noLine)
	{
		this.noLineAttributeName = attributeName;
		this.noLine = noLine;
	}

	/**
	 * 设置是否需要处理文本中"${...}"的动态属性.
	 *
	 * @param attributeName  设置是否需要处理的属性名
	 * @param defaultValue   默认值, 如果未调用此方法, 此值为false
	 */
	public void setNeedResolve(String attributeName, boolean defaultValue)
	{
		this.needResolveAttributeName = attributeName;
		this.needResolve = defaultValue;
	}

	public Object prepareProperty(String namespace, String name, Attributes attributes)
			throws Exception
	{
		// 将bodyText的获取设置 置为初始值
		this.bodyTextTrimLines = this.trimLines;
		this.bodyTextNoLine = this.noLine;
		String strValue = this.getValue(namespace, name, attributes);
		this.bodyTextTrimLines = strValue != null ? "true".equalsIgnoreCase(strValue) : this.trimLines;
		strValue = attributes.getValue(this.noLineAttributeName);
		this.bodyTextNoLine = strValue != null ? "true".equalsIgnoreCase(strValue) : this.noLine;
		if (this.needResolveAttributeName != null)
		{
			strValue = attributes.getValue(this.needResolveAttributeName);
			this.bodyTextNeedResolve = strValue != null ? "true".equalsIgnoreCase(strValue) : this.needResolve;
		}
		return "";
	}

	public boolean requireBodyValue()
	{
		return true;
	}

	public Object prepareProperty(String namespace, String name, BodyText text)
			throws Exception
	{
		String bodyStr = this.bodyTextTrimLines ?
				text.trimEveryLineSpace(this.bodyTextNoLine) : text.toString();
		bodyStr = this.bodyTextNeedResolve ? Utility.resolveDynamicPropnames(bodyStr) : bodyStr;
		if (this.needIntern)
		{
			bodyStr = StringTool.intern(bodyStr, true);
		}
		this.value = new Object[]{bodyStr};
		return this.value;
	}

}

/**
 * 设置boolean类型的属性.
 *
 * @author micromagic@sina.com
 */
class BooleanPropertySetter extends SinglePropertySetter
{
	public BooleanPropertySetter(String attributeName, String methodName, boolean mustExist)
	{
		super(attributeName, methodName, mustExist);
		this.type = BOOLEAN_TYPE;
		this.needIntern = false;
	}

	public BooleanPropertySetter(String attributeName, String methodName, String defaultValue)
	{
		super(attributeName, methodName, defaultValue);
		this.type = BOOLEAN_TYPE;
		this.needIntern = false;
	}

	public Object prepareProperty(String namespace, String name, Attributes attributes)
			throws Exception
	{
		String strValue = this.getValue(namespace, name, attributes);
		if (strValue == null)
		{
			this.value = null;
			return null;
		}

		this.value = new Object[]{"true".equalsIgnoreCase(strValue) ? Boolean.TRUE : Boolean.FALSE};
		return this.value[0];
	}

}

/**
 * 设置int类型的属性.
 *
 * @author micromagic@sina.com
 */
class IntegerPropertySetter extends SinglePropertySetter
{
	public IntegerPropertySetter(String attributeName, String methodName, boolean mustExist)
	{
		super(attributeName, methodName, mustExist);
		this.type = INTEGER_TYPE;
		this.needIntern = false;
	}

	public IntegerPropertySetter(String attributeName, String methodName, String defaultValue)
	{
		super(attributeName, methodName, defaultValue);
		this.type = INTEGER_TYPE;
		this.needIntern = false;
	}

	public Object prepareProperty(String namespace, String name, Attributes attributes)
			throws Exception
	{
		String strValue = this.getValue(namespace, name, attributes);
		if (strValue == null)
		{
			this.value = null;
			return null;
		}

		int intValue;
		try
		{
			if (strValue.length() > 2 && "0x".equalsIgnoreCase(strValue.substring(0, 2)))
			{
				// 如果有16进制的起始标记，则进行16进制转换
				intValue = Integer.parseInt(strValue.substring(2), 16);
			}
			else
			{
				intValue = Integer.parseInt(strValue);
			}
		}
		catch (NumberFormatException ex)
		{
			throw new ConfigurationException(ex.getMessage()
					+ "(name:" + name + ", attribute:" + this.attributeName + ")");
		}
		this.value = new Object[]{Utility.createInteger(intValue)};
		return this.value[0];
	}

}

/**
 * 不设置任何属性, 只调用一个无参的方法.
 *
 * @author micromagic@sina.com
 */
class EmptyPropertySetter extends PropertySetter
{
	public static final Class[] EMPTY_TYPE = new Class[0];
	public static final Object[] EMPTY_VALUE = new Object[0];

	public EmptyPropertySetter(String methodName)
	{
		super(methodName);
	}

	public boolean isMustExist()
	{
		return false;
	}

	public Object prepareProperty(String namespace, String name, Attributes attributes)
			throws Exception
	{
		return null;
	}

	public void setProperty()
			throws Exception
	{
		Object obj = this.digester.peek(this.objectIndex);
		Tool.invokeExactMethod(obj, this.methodName, EMPTY_VALUE, EMPTY_TYPE);
	}

}

