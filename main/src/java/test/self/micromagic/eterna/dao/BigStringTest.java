
package self.micromagic.eterna.dao;

import java.sql.SQLException;
import java.sql.Types;

import junit.framework.TestCase;
import self.micromagic.eterna.dao.preparer.CreaterManager;
import self.micromagic.eterna.dao.preparer.PreparerCreater;
import self.micromagic.eterna.dao.preparer.ValuePreparer;
import self.micromagic.eterna.digester2.ContainerManager;
import self.micromagic.eterna.share.EternaFactory;
import self.micromagic.util.Utility;

public class BigStringTest extends TestCase
{
	public void testBigStringSqlType()
			throws SQLException
	{
		EternaFactory f = (EternaFactory) ContainerManager.getGlobalContainer().getFactory();
		PreparerCreater creater = CreaterManager.createPreparerCreater("BigString", null, f);
		ValuePreparer preparer = creater.createPreparer(null);
		PreparerChecker stmt = new PreparerChecker(Types.CLOB);
		preparer.setValueToStatement(1, stmt);
		Utility.setProperty("eterna.dao.bigString.sqlType", "BigString");
		preparer = creater.createPreparer(null);
		stmt = new PreparerChecker(Types.LONGVARCHAR);
		preparer.setValueToStatement(1, stmt);
	}

}
