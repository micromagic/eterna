
package self.micromagic.eterna.digester2.dom;

import self.micromagic.eterna.digester2.ParseException;

/**
 * 可定义参数的文本中的参数对象.
 */
public class Parameter
{
	private final String name;
	private String describe;
	private String defaultValue;
	private String value;

	Parameter(String name)
	{
		this.name = name;
	}

	void init(ParamText parser)
	{
		if (this.describe != null)
		{
			if (this.describe.startsWith("$"))
			{
				this.describe = parser.getDefine(this.describe.substring(1));
			}
		}
		if (this.defaultValue != null)
		{
			if (this.defaultValue.startsWith("$"))
			{
				this.defaultValue = parser.getDefine(this.defaultValue.substring(1));
			}
			this.value = this.defaultValue;
		}
	}

	void setDescribe(String describe)
	{
		if (this.describe != null)
		{
			throw new ParseException("Too many describe in same param [" + this.name + "].");
		}
		this.describe = describe;
	}

	void setDefaultValue(String defaultValue)
	{
		if (this.defaultValue != null)
		{
			throw new ParseException("Too many default in same param [" + this.name + "].");
		}
		this.defaultValue = defaultValue;
	}

	/**
	 * 获取参数的名称.
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * 获取参数的说明.
	 */
	public String getDescribe()
	{
		return describe;
	}

	/**
	 * 获取参数的默认值.
	 */
	public String getDefaultValue()
	{
		return defaultValue;
	}

	/**
	 * 获取参数的值.
	 */
	public String getValue()
	{
		if (this.value == null)
		{
			throw new ParseException("Param [" + this.name + "] value hasn't setted.");
		}
		return value;
	}

	/**
	 * 设置参数的值.
	 */
	public void setValue(String value)
	{
		this.value = value;
	}

}
