package self.micromagic.eterna.base.preparer;

import self.micromagic.eterna.share.AttributeManager;
import self.micromagic.eterna.share.EternaException;
import self.micromagic.eterna.share.EternaFactory;

public abstract class AbstractPreparerCreater
		implements PreparerCreater
{
	public AbstractPreparerCreater(String name)
	{
		this.name = name;
	}
	private final String name;

	public String getName()
	{
		return this.name;
	}

	protected void setAttributes(AttributeManager attributes)
	{
		this.attributes = attributes;
		String fStr = (String) attributes.getAttribute(PATTERN);
		if (fStr != null)
		{
			this.setFormat(fStr);
		}
	}
	private AttributeManager attributes = new AttributeManager();

	public Object getAttribute(String name)
	{
		return this.attributes.getAttribute(name);
	}

	public abstract void setFormat(String format) throws EternaException;

	public boolean initialize(EternaFactory factory)
			throws EternaException
	{
		if (this.factory == null)
		{
			this.factory = factory;
			this.nullCreater = CreaterManager.createNullCreater(factory);
			return false;
		}
		return true;
	}
	private EternaFactory factory;
	private NullCreater nullCreater;

	/**
	 * 构造一个null数据的值准备器.
	 */
	protected ValuePreparer createNull(int type)
	{
		return this.nullCreater.createPreparer(type);
	}

	public EternaFactory getFactory()
			throws EternaException
	{
		return this.factory;
	}

}