
package self.micromagic.eterna.digester2;

import org.dom4j.Element;
import self.micromagic.eterna.share.EternaFactory;
import self.micromagic.cg.BeanMap;

/**
 * 创建工厂对象的规则.
 */
public class FactoryCreateRule extends ParseRule
{
	public FactoryCreateRule(Digester digester)
	{
		super(digester);
	}

	private String factoryClass;

	public boolean begin(Element element)
	{
		ParseException.setContextInfo("", element);
		EternaFactory factory = ContainerManager.getCurrentFactory();
		if (factory == null)
		{
         BeanMap bm = createBeanMap(this.factoryClass, Thread.currentThread().getContextClassLoader(), true);
			factory = (EternaFactory) bm.getBean();
			ContainerManager.setCurrentFactory(factory);
			if (ContainerManager.log.isDebugEnabled())
			{
				ContainerManager.log.debug("Factory [" + this.factoryClass + "] has created.");
			}
		}
		this.digester.push(factory);
		return true;
	}

	public void end(Element element)
	{
		this.digester.pop();
	}

}