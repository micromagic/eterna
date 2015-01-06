/*
 * Copyright 2009-2015 xinjunli (micromagic@sina.com).
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

import org.dom4j.io.SAXContentHandler;
import org.dom4j.DocumentFactory;
import org.dom4j.ElementHandler;
import org.xml.sax.Locator;

public class EternaSAXContentHandler extends SAXContentHandler
{
	public EternaSAXContentHandler(DocumentFactory documentFactory, ElementHandler elementHandler)
	{
		super(documentFactory, elementHandler);
		if (documentFactory instanceof EternaDocumentFactory)
		{
			this.documentFactory = (EternaDocumentFactory) documentFactory;
		}
	}
	private EternaDocumentFactory documentFactory;

	public void setDocumentLocator(Locator documentLocator)
	{
		super.setDocumentLocator(documentLocator);
		if (this.documentFactory != null)
		{
			this.documentFactory.setDocumentLocator(documentLocator);
		}
	}

}