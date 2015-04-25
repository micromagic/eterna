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

package self.micromagic.eterna.digester2.dom;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import junit.framework.TestCase;
import self.micromagic.eterna.digester2.DigesterTest;

public class EternaSAXReaderTest extends TestCase
{
	public void testGetEncoding()
			throws Exception
	{
		InputStream in = DigesterTest.class.getResourceAsStream("d_test1.xml");
		String encoding = EternaSAXReader.getEncoding(in);
		assertEquals("gbk", encoding);
		assertEquals('<', in.read());
		assertEquals('?', in.read());

		String tmp = "<?xml version  1.0 encoding  'utf-8'?>";
		encoding = EternaSAXReader.getEncoding(
				new ByteArrayInputStream(tmp.getBytes("8859_1")));
		assertNull(encoding);
		tmp = "<?xml version='1.0' encoding = 'utf-8'?>";
		encoding = EternaSAXReader.getEncoding(
				new ByteArrayInputStream(tmp.getBytes("8859_1")));
		assertEquals("utf-8", encoding);
	}

}
