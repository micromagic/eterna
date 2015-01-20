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

package self.micromagic.eterna.digester2.dom;

import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.QName;
import org.xml.sax.Locator;

public class EternaDocumentFactory extends DocumentFactory
{
	public Element createElement(String qualifiedName, String namespaceURI)
	{
		if (this.documentLocator != null)
		{
			EternaElement e = new EternaElement(createQName(qualifiedName, namespaceURI));
			e.setLocation(this.documentLocator.getLineNumber(), this.documentLocator.getColumnNumber());
			return e;
		}
		return super.createElement(qualifiedName, namespaceURI);
	}

	public Element createElement(QName qname)
	{
		if (this.documentLocator != null)
		{
			EternaElement e = new EternaElement(qname);
			e.setLocation(this.documentLocator.getLineNumber(), this.documentLocator.getColumnNumber());
			return e;
		}
		return super.createElement(qname);
	}

	public Element createElement(String name)
	{
		if (this.documentLocator != null)
		{
			EternaElement e = new EternaElement(createQName(name));
			e.setLocation(this.documentLocator.getLineNumber(), this.documentLocator.getColumnNumber());
			return e;
		}
		return super.createElement(name);
	}

	public void setDocumentLocator(Locator documentLocator)
	{
		this.documentLocator = documentLocator;
	}
	private Locator documentLocator;

	private static final long serialVersionUID = 1L;

}