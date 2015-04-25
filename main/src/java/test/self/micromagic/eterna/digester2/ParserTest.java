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

package self.micromagic.eterna.digester2;

import java.util.Iterator;

import junit.framework.TestCase;
import self.micromagic.eterna.digester2.dom.EternaElement;
import self.micromagic.util.ref.IntegerRef;
import tool.PrivateAccessor;

public class ParserTest extends TestCase
{
	public void testBodyAttr()
			throws Exception
	{
		BodyAttrGetter bag;
		String config;
		IntegerRef pos = new IntegerRef(0);

		bag = BodyAttrGetter.parseConfig(" $body  ", pos);
		assertNull(PrivateAccessor.get(bag, "bodyTag"));
		assertNull(PrivateAccessor.get(bag, "attrTag"));

		pos.value = 2;
		try
		{
			bag = BodyAttrGetter.parseConfig("a,b$body", pos);
			fail();
		}
		catch (ParseException ex) {}
		pos.value = 6;
		try
		{
			bag = BodyAttrGetter.parseConfig("$body,$body (", pos);
			fail();
		}
		catch (ParseException ex) {}

		pos.value = 0;
		config = "$body (attr=a),";
		bag = BodyAttrGetter.parseConfig(config, pos);
		assertNull(PrivateAccessor.get(bag, "bodyTag"));
		assertEquals("a", PrivateAccessor.get(bag, "attrTag"));
		assertEquals(config.indexOf(')') + 1, pos.value);
		pos.value = 0;
		config = "$body (body=a)  ,";
		bag = BodyAttrGetter.parseConfig(config, pos);
		assertNull(PrivateAccessor.get(bag, "attrTag"));
		assertEquals("a", PrivateAccessor.get(bag, "bodyTag"));
		assertEquals(config.indexOf(')') + 1, pos.value);

		pos.value = 0;
		config = "$body (body=a, attr=b) , ";
		bag = BodyAttrGetter.parseConfig(config, pos);
		assertEquals("a", PrivateAccessor.get(bag, "bodyTag"));
		assertEquals("b", PrivateAccessor.get(bag, "attrTag"));
		assertEquals(config.indexOf(')') + 1, pos.value);

		pos.value = 0;
		config = "$body (body=a,attr=b,t=0,rFlag=r)";
		bag = BodyAttrGetter.parseConfig(config, pos);
		assertEquals("a", PrivateAccessor.get(bag, "bodyTag"));
		assertEquals("b", PrivateAccessor.get(bag, "attrTag"));
		assertEquals("trimLine", PrivateAccessor.get(bag, "trimLinesAttr"));
		assertEquals("resolve", PrivateAccessor.get(bag, "resolveAttr"));
		assertEquals(Boolean.FALSE, PrivateAccessor.get(bag, "trimLines"));
		assertEquals(config.indexOf(')') + 1, pos.value);

		pos.value = 0;
		config = "$body (body=a,attr=b,t=0,rFlag=r,r=1),$body(t=0),";
		bag = BodyAttrGetter.parseConfig(config, pos);
		assertEquals("a", PrivateAccessor.get(bag, "bodyTag"));
		assertEquals("b", PrivateAccessor.get(bag, "attrTag"));
		assertEquals("trimLine", PrivateAccessor.get(bag, "trimLinesAttr"));
		assertEquals("r", PrivateAccessor.get(bag, "resolveAttr"));
		assertEquals(Boolean.FALSE, PrivateAccessor.get(bag, "trimLines"));
		assertEquals(Boolean.TRUE, PrivateAccessor.get(bag, "resolve"));
		assertEquals(config.indexOf(')') + 1, pos.value);
	}

	public void testAttrBinder()
			throws Exception
	{
		AttrBinder ab;
		String config;
		IntegerRef pos = new IntegerRef(0);

		config = "a,  attr: {x}";
		pos.value = 10;
		ab = AttrBinder.parseConfig(config, pos);
		assertEquals(1, ((Object[]) PrivateAccessor.get(ab, "attrs")).length);
		assertEquals(1, ((Object[]) PrivateAccessor.get(ab, "toNames")).length);
		assertEquals(config.length(), pos.value);

		pos.value = 9;
		try
		{
			ab = AttrBinder.parseConfig("a, attr:   ", pos);
			fail();
		}
		catch (ParseException ex) {}
		pos.value = 6;
		try
		{
			ab = AttrBinder.parseConfig(" attr:{$body}", pos);
			fail();
		}
		catch (ParseException ex) {}

		pos.value = 5;
		config = "attr:{name0,$body (attr=a,m=1):a1,name2:a2,name3 ( m=1\n,i=1),name4(m=0):a4 }";
		ab = AttrBinder.parseConfig(config, pos);
		assertEquals("name0", ((Object[]) PrivateAccessor.get(ab, "toNames"))[0]);
		assertEquals("a1", ((Object[]) PrivateAccessor.get(ab, "toNames"))[1]);
		assertEquals("a2", ((Object[]) PrivateAccessor.get(ab, "toNames"))[2]);
		assertEquals("name3", ((Object[]) PrivateAccessor.get(ab, "toNames"))[3]);
		assertEquals("a4", ((Object[]) PrivateAccessor.get(ab, "toNames"))[4]);
		assertEquals(BodyAttrGetter.class, ((Object[]) PrivateAccessor.get(ab, "attrs"))[1].getClass());
		assertEquals(StandardAttrGetter.class, ((Object[]) PrivateAccessor.get(ab, "attrs"))[3].getClass());
		assertEquals(config.length(), pos.value);
	}

	public void testMethodBinder()
			throws Exception
	{
		MethodBinder mb;
		String config;
		IntegerRef pos = new IntegerRef(0);

		config = "a,  method: {a   }";
		pos.value = 12;
		mb = MethodBinder.parseConfig(config, pos);
		assertEquals(0, ((Object[]) PrivateAccessor.get(mb, "attrs")).length);
		assertEquals("a", PrivateAccessor.get(mb, "methodName"));
		assertEquals(config.length(), pos.value);

		pos.value = 11;
		try
		{
			mb = MethodBinder.parseConfig("a, method:  ", pos);
			fail();
		}
		catch (ParseException ex) {}

		pos.value = 7;
		config = "method:{name0,$body (attr=a,m=1):a1,name2:\na2,name3 ( m=1,i=1),name4(m=0):a4 }";
		mb = MethodBinder.parseConfig(config, pos);
		assertEquals("name0", PrivateAccessor.get(mb, "methodName"));
		assertEquals(BodyAttrGetter.class, ((Object[]) PrivateAccessor.get(mb, "attrs"))[0].getClass());
		assertEquals(StandardAttrGetter.class, ((Object[]) PrivateAccessor.get(mb, "attrs"))[3].getClass());
		assertEquals(4, ((Object[]) PrivateAccessor.get(mb, "attrs")).length);
		assertEquals(config.length(), pos.value);
	}

	void printElement(EternaElement el)
	{
		System.out.println("e:" + el.getName() + ", l:" + el.getLineNumber() + ", c:" + el.getColumnNumber());
		Iterator itr = el.elementIterator();
		while (itr.hasNext())
		{
			this.printElement((EternaElement) itr.next());
		}
	}

}