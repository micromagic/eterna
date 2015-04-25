/*
 * Copyright 2015 xinjunli (micromagic@sina.com).
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

package self.micromagic.util;

import java.util.List;

import junit.framework.TestCase;
import self.micromagic.eterna.dao.preparer.ValuePreparer;
import self.micromagic.eterna.digester2.ContainerManager;
import self.micromagic.eterna.search.BuildeResult;
import self.micromagic.eterna.search.ConditionBuilder;
import self.micromagic.eterna.search.ConditionProperty;
import self.micromagic.eterna.security.PermissionSet;
import self.micromagic.eterna.share.EternaFactory;
import self.micromagic.eterna.share.TypeManager;
import tool.PrivateAccessor;

public class BuilderTest extends TestCase
{
	public void testSub1()
			throws Exception
	{
		TemplateBuilder t = new TemplateBuilder();
		t.setAttribute("template", "[C1].[C0].[C1] IN ($)");
		PrivateAccessor.invoke(t, "parseTemplate", new Object[0]);
		int[] indexs = (int[]) PrivateAccessor.get(t, "indexs");
		assertEquals(3, indexs.length);
		BuildeResult con = t.buildeCondition(
				"a,b", "1,2,3, 4, 5", new EmptyConditionProperty("a,b"));
		assertEquals(5, con.preparers.length);
		assertEquals("b.a.b IN (?, ?, ?, ?, ?)", con.scriptPart);

		t = new TemplateBuilder();
		t.setAttribute("template", "[C1] x ($)");
		t.setAttribute("sub_cell", "[C0] like ?");
		t.setAttribute("sub_link", " or ");
		PrivateAccessor.invoke(t, "parseTemplate", new Object[0]);
		con = t.buildeCondition(
				"a,b", "1,2,3", new EmptyConditionProperty("a,b"));
		assertEquals(3, con.preparers.length);
		assertEquals("b x (a like ? or a like ? or a like ?)", con.scriptPart);
	}

	public void testTemplate1()
			throws Exception
	{
		TemplateBuilder t = new TemplateBuilder();
		t.setAttribute("template", "[C1] IN (SELECT tcode FROM TTI_SCLASS WHERE [C0] = ?)");
		PrivateAccessor.invoke(t, "parseTemplate", new Object[0]);
		int[] indexs = (int[]) PrivateAccessor.get(t, "indexs");
		assertEquals(2, indexs.length);
		assertEquals(1, indexs[0]);
		assertEquals(0, indexs[1]);
		assertEquals(Utility.createInteger(1), PrivateAccessor.get(t, "paramCount"));

		t = new TemplateBuilder();
		t.setAttribute("template", "x");
		t.setAttribute("param_count", "3");
		PrivateAccessor.invoke(t, "parseTemplate", new Object[0]);
		assertEquals(Utility.createInteger(3), PrivateAccessor.get(t, "paramCount"));

		t = new TemplateBuilder();
		t.setAttribute("template", "123 456");
		PrivateAccessor.invoke(t, "parseTemplate", new Object[0]);
		BuildeResult con = t.buildeCondition("a", "1", null);
		assertEquals("123 456", con.scriptPart);

		t = new TemplateBuilder();
		t.setAttribute("template", "a[C2]b c[C1]d");
		PrivateAccessor.invoke(t, "parseTemplate", new Object[0]);
		con = t.buildeCondition("1,2,3", "a", null);
		assertEquals("a3b c2d", con.scriptPart);
		con = t.buildeCondition("x,y", "a", null);
		assertEquals("ab cyd", con.scriptPart);
	}

	public void testTemplate2()
			throws Exception
	{
		EternaFactory f = (EternaFactory) ContainerManager.getGlobalContainer().getFactory();
		TemplateBuilder t = new TemplateBuilder();
		t.setPrepare("strInclude");
		t.setAttribute("template", "([C0] like ? or [C1] like ?)");
		PrivateAccessor.invoke(t, "parseTemplate", new Object[0]);
		t.initialize(f);
		BuildeResult condition = t.buildeCondition("a,b", "str", null);
		assertEquals("(a like ? or b like ?)", condition.scriptPart);
		assertEquals(2, condition.preparers.length);
		assertEquals("%str%", PrivateAccessor.get(condition.preparers[0], "value"));
	}

	public void testBase()
			throws Exception
	{
		EternaFactory f = (EternaFactory) ContainerManager.getGlobalContainer().getFactory();
		ConditionBuilder b;
		BuildeResult condition;
		EmptyConditionProperty cp = new EmptyConditionProperty("a");

		b = f.getConditionBuilder("isNull");
		condition = b.buildeCondition("a", "x", cp);
		assertEquals("a IS NULL", condition.scriptPart);
		b = f.getConditionBuilder("notNull");
		condition = b.buildeCondition("a", "x", cp);
		assertEquals("a IS NOT NULL", condition.scriptPart);
		b = f.getConditionBuilder("equal");
		condition = b.buildeCondition("a", "x", cp);
		assertEquals("a = ?", condition.scriptPart);
		assertNull(condition.preparers[0]);
		b = f.getConditionBuilder("notEqual");
		condition = b.buildeCondition("a", "x", cp);
		assertEquals("a <> ?", condition.scriptPart);
		assertNull(condition.preparers[0]);

		b = f.getConditionBuilder("include");
		condition = b.buildeCondition("a", "x", cp);
		assertEquals("a LIKE ?", condition.scriptPart);
		assertEquals("%x%", PrivateAccessor.get(condition.preparers[0], "value"));
		condition = b.buildeCondition("a", "x%", cp);
		assertEquals("a LIKE ? escape '\\'", condition.scriptPart);
		assertEquals("%x\\%%", PrivateAccessor.get(condition.preparers[0], "value"));
		b = f.getConditionBuilder("beginWith");
		condition = b.buildeCondition("a", "x", cp);
		assertEquals("a LIKE ?", condition.scriptPart);
		assertEquals("x%", PrivateAccessor.get(condition.preparers[0], "value"));
		b = f.getConditionBuilder("endWith");
		condition = b.buildeCondition("a", "x", cp);
		assertEquals("a LIKE ?", condition.scriptPart);
		assertEquals("%x", PrivateAccessor.get(condition.preparers[0], "value"));
		b = f.getConditionBuilder("match");
		condition = b.buildeCondition("a", "x", cp);
		assertEquals("a LIKE ? escape '\\'", condition.scriptPart);
		assertNull(condition.preparers[0]);

		b = f.getConditionBuilder("more");
		condition = b.buildeCondition("a", "x", cp);
		assertEquals("a > ?", condition.scriptPart);
		assertNull(condition.preparers[0]);
		b = f.getConditionBuilder("less");
		condition = b.buildeCondition("a", "x", cp);
		assertEquals("a < ?", condition.scriptPart);
		assertNull(condition.preparers[0]);
		b = f.getConditionBuilder("moreEqual");
		condition = b.buildeCondition("a", "x", cp);
		assertEquals("a >= ?", condition.scriptPart);
		assertNull(condition.preparers[0]);
		b = f.getConditionBuilder("lessEqual");
		condition = b.buildeCondition("a", "x", cp);
		assertEquals("a <= ?", condition.scriptPart);
		assertNull(condition.preparers[0]);
	}

}

class EmptyConditionProperty
		implements ConditionProperty
{
	public EmptyConditionProperty(String colName)
	{
		this.colName = colName;
	}
	private final String colName;

	public void initialize(EternaFactory factory)
	{
	}

	public String getName()
	{
		return "builderTest";
	}

	public String getColumnName()
	{
		return this.colName;
	}

	public String getColumnCaption()
	{
		return null;
	}

	public int getColumnType()
	{
		return TypeManager.TYPE_STRING;
	}

	public String getColumnTypeName()
	{
		return TypeManager.getTypeName(TypeManager.TYPE_STRING);
	}

	public ValuePreparer createValuePreparer(String value)
	{
		return null;
	}

	public ValuePreparer createValuePreparer(Object value)
	{
		return null;
	}

	public boolean isIgnore()
	{
		return false;
	}

	public boolean isVisible()
	{
		return false;
	}

	public String getConditionInputType()
	{
		return null;
	}

	public String getDefaultValue()
	{
		return null;
	}

	public Object getAttribute(String name)
	{
		return null;
	}

	public String[] getAttributeNames()
	{
		return null;
	}

	public PermissionSet getPermissionSet()
	{
		return null;
	}

	public String getConditionBuilderListName()
	{
		return null;
	}

	public List getConditionBuilderList()
	{
		return null;
	}

	public boolean isUseDefaultConditionBuilder()
	{
		return false;
	}

	public ConditionBuilder getDefaultConditionBuilder()
	{
		return null;
	}

}
