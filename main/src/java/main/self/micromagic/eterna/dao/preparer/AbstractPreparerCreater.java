package self.micromagic.eterna.dao.preparer;

import self.micromagic.eterna.share.AttributeManager;
import self.micromagic.eterna.share.EternaException;
import self.micromagic.eterna.share.EternaFactory;

public abstract class AbstractPreparerCreater
		implements PreparerCreater
{
	private final String name;
	private AttributeManager attributes = new AttributeManager();
	private EternaFactory factory;
	private NullCreater nullCreater;

	public AbstractPreparerCreater(String name)
	{
		this.name = name;
	}

	public String getName()
	{
		return this.name;
	}

	protected void setAttributes(AttributeManager attributes)
	{
		this.attributes = attributes;
	}

	public Object getAttribute(String name)
	{
		return this.attributes.getAttribute(name);
	}

	public void setPattern(String pattern)
			throws EternaException
	{
	}

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
