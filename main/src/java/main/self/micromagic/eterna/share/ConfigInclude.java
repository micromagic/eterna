package self.micromagic.eterna.share;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import self.micromagic.eterna.digester2.ConfigResource;
import self.micromagic.eterna.digester2.ContainerManager;
import self.micromagic.eterna.digester2.dom.ParamText;
import self.micromagic.eterna.digester2.dom.Parameter;

/**
 * 引用一个配置的描述对象.
 */
public class ConfigInclude
{
	/**
	 * 在工厂容器的属性中存放需要引用的列表的键值.
	 */
	public static final String INCLUDE_LIST_FLAG = "includes";

	/**
	 * 获取应用的资源配置.
	 */
	public String getSrc()
	{
		return this.src;
	}
	private String src;
	public void setSrc(String src)
	{
		this.src = src;
	}

	public void addParam(String name, String value)
	{
		this.params.setAttribute(name, value);
	}
	private final AttributeManager params = new AttributeManager();

	/**
	 * 获取引用的资源对象, 可以是一个InputStream或Reader.
	 */
	public Object getIncludeRes(ConfigResource res)
			throws IOException
	{
		ConfigResource tmp = res.getResource(this.src);
		ContainerManager.setCurrentResource(tmp);
		InputStream in = tmp.getAsStream();
		if (in == null)
		{
			return null;
		}
		ParamText pt = new ParamText();
		pt.parse(in);
		Parameter[] params = pt.getParams();
		for (int i = 0; i < params.length; i++)
		{
			String v = (String) this.params.getAttribute(params[i].getName());
			if (v != null)
			{
				params[i].setValue(v);
			}
			else if (params[i].getDefaultValue() == null)
			{
				throw new EternaException("The param [" + params[i].getName()
						+ "] hasn'e setted.");
			}
		}
		return new StringReader(pt.getResultString());
	}

	/**
	 * 将这个引用注册到当前的工厂容器中.
	 */
	public void registerInclude()
	{
		if (this.src == null)
		{
			throw new EternaException("Attribute src is null.");
		}
		FactoryContainer container = ContainerManager.getCurrentContainer();
		List includes = (List) container.getAttribute(INCLUDE_LIST_FLAG);
		if (includes == null)
		{
			includes = new ArrayList();
			container.setAttribute(INCLUDE_LIST_FLAG, includes);
		}
		includes.add(this);
	}

}
