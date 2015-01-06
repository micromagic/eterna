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

import org.dom4j.Namespace;
import org.dom4j.QName;
import org.dom4j.tree.DefaultElement;

public class EternaElement extends DefaultElement
{
	public EternaElement(String name)
	{
		super(name);
	}

	public EternaElement(QName qname)
	{
		super(qname);
	}

	public EternaElement(QName qname, int attributeCount)
	{
		super(qname, attributeCount);
	}

	public EternaElement(String name, Namespace namespace)
	{
		super(name, namespace);
	}

	public int getColumnNumber()
	{
		return this.colNum;
	}
	private int colNum = 0;

	public int getLineNumber()
	{
		return this.lineNum;
	}
	private int lineNum = 0;

	public void setLocation(int lineNum, int colNum)
	{
		this.lineNum = lineNum;
		this.colNum = colNum;
	}

	private static final long serialVersionUID = 1L;

}