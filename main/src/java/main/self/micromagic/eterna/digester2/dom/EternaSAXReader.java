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
import java.io.IOException;
import java.io.InputStream;

import org.dom4j.DocumentFactory;
import org.dom4j.io.SAXContentHandler;
import org.dom4j.io.SAXReader;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

public class EternaSAXReader extends SAXReader
{
	public EternaSAXReader(DocumentFactory factory)
	{
		super(factory);
		this.setValidation(false);
		this.setEntityResolver(new EmptyEntityResolver());
	}

	protected SAXContentHandler createContentHandler(XMLReader reader)
	{
		return new EternaSAXContentHandler(this.getDocumentFactory(), this.getDispatchHandler());
	}

	/**
	 * 获取一个xml数据流的编码.
	 */
	public static String getEncoding(InputStream in)
			throws IOException
	{
		in.mark(MAX_MARK_COUNT);
		byte[] buf = new byte[MAX_MARK_COUNT];
		int count = in.read(buf);
		in.reset();

		int c;
		boolean sawQuestion = false;
		String key = null;
		int keyBegin = -1;
		int valueBegin = -1;
		boolean sawEq = false;
		char quoteChar = 0;
		for (int i = 0; i < count; ++i)
		{
			c = buf[i] & 0xff;
			// ignore whitespace before/between "key = 'value'"
			if (c == ' ' || c == '\t' || c == '\n' || c == '\r')
			{
				if (key == null && keyBegin != -1)
				{
					key = new String(buf, keyBegin, i - keyBegin, "8859_1");
					// to get next key
					keyBegin = -1;
				}
				continue;
			}
			// terminate the loop ASAP
			if (c == '?')
			{
				sawQuestion = true;
			}
			else if (sawQuestion)
			{
				if (c == '>')
				{
					break;
				}
				sawQuestion = false;
			}

			// did we get the "key =" bit yet?
			if (key == null || !sawEq)
			{
				if (!sawEq && c == '=')
				{
					if (key == null)
					{
						key = new String(buf, keyBegin, i - keyBegin, "8859_1");
					}
					sawEq = true;
					keyBegin = -1;
					quoteChar = 0;
					continue;
				}
				if (keyBegin == -1)
				{
					if (Character.isWhitespace((char) c))
					{
						continue;
					}
					keyBegin = i;
					key = null;
					sawEq = false;
				}
				else if (Character.isWhitespace((char) c))
				{
					if (key == null)
					{
						key = new String(buf, keyBegin, i - keyBegin, "8859_1");
					}
					// to get next key
					keyBegin = -1;
				}
				continue;
			}

			// space before quoted value
			if (Character.isWhitespace((char) c))
			{
				continue;
			}
			if (c == '"' || c == '\'')
			{
				if (quoteChar == 0)
				{
					quoteChar = (char) c;
					valueBegin = i + 1;
					continue;
				}
				else if (c == quoteChar)
				{
					if ("encoding".equals(key))
					{
						return new String(buf, valueBegin, i - valueBegin, "8859_1");
					}
					else
					{
						key = null;
						valueBegin = -1;
					}
				}
			}
		}
		return null;
	}
	private static final int MAX_MARK_COUNT = 128;

}

class EmptyEntityResolver
		implements EntityResolver
{
	public InputSource resolveEntity(String publicId, String systemId)
	{
		return new InputSource(new ByteArrayInputStream(new byte[0]));
	}

}