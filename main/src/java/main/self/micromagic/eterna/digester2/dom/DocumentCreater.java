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

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;

/**
 * dom4j文档对象的创建者.
 */
public class DocumentCreater
{
	private DocumentCreater()
	{
	}

	/**
	 * 根据输入流创建一个dom4j的文档对象.
	 *
	 * @param in  输入流
	 */
	public static Document createDoc(InputStream in)
			throws DocumentException, IOException
	{
		return createDoc(in, true, false);
	}

	/**
	 * 根据输入流创建一个dom4j的文档对象.
	 *
	 * @param in         输入流
	 * @param needClose  执行后是否需要关闭输入流
	 */
	public static Document createDoc(InputStream in, boolean needClose)
			throws DocumentException, IOException
	{
		return createDoc(in, needClose, false);
	}

	/**
	 * 根据输入流创建一个dom4j的文档对象.
	 *
	 * @param in         输入流
	 * @param needClose  执行后是否需要关闭输入流
	 * @param useEterna  是否使用eterna的SAXReader
	 */
	public static Document createDoc(InputStream in, boolean needClose, boolean useEterna)
			throws DocumentException, IOException
	{
		if (in == null)
		{
			return null;
		}
		try
		{
			SAXReader reader;
			if (useEterna)
			{
				reader = new EternaSAXReader(new EternaDocumentFactory());
			}
			else
			{
				reader = new SAXReader();
			}
			return reader.read(in);
		}
		finally
		{
			if (needClose)
			{
				in.close();
			}
		}
	}

	/**
	 * 根据字符流创建一个dom4j的文档对象.
	 *
	 * @param reader  字符流
	 */
	public static Document createDoc(Reader reader)
			throws DocumentException, IOException
	{
		return createDoc(reader, true, false);
	}

	/**
	 * 根据字符流创建一个dom4j的文档对象.
	 *
	 * @param reader     字符流
	 * @param needClose  执行后是否需要关闭字符流
	 */
	public static Document createDoc(Reader reader, boolean needClose)
			throws DocumentException, IOException
	{
		return createDoc(reader, needClose, false);
	}

	/**
	 * 根据字符流创建一个dom4j的文档对象.
	 *
	 * @param reader     字符流
	 * @param needClose  执行后是否需要关闭字符流
	 * @param useEterna  是否使用eterna的SAXReader
	 */
	public static Document createDoc(Reader reader, boolean needClose, boolean useEterna)
			throws DocumentException, IOException
	{
		if (reader == null)
		{
			return null;
		}
		try
		{
			SAXReader saxReader;
			if (useEterna)
			{
				saxReader = new EternaSAXReader(new EternaDocumentFactory());
			}
			else
			{
				saxReader = new SAXReader();
			}
			return saxReader.read(reader);
		}
		finally
		{
			if (needClose)
			{
				reader.close();
			}
		}
	}

}
