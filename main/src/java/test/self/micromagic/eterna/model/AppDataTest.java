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

package self.micromagic.eterna.model;

import java.util.Iterator;

import junit.framework.TestCase;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;

public class AppDataTest extends TestCase
{
	public void testAddNode()
	{
		Document doc = DocumentHelper.createDocument();
		Element root = doc.addElement("root");
		Element logs = root.addElement("logs");
		Element log1 = DocumentHelper.createElement("log");
		logs.add(log1.addAttribute("name", "l1"));
		Element log2 = DocumentHelper.createElement("log");
		logs.add(log2.addAttribute("name", "l2"));
		System.out.println(doc.asXML());
		Element newLogs = DocumentHelper.createElement("logs");
		Iterator itr = logs.nodeIterator();
		itr.next();
		while (itr.hasNext())
		{
			Node node = (Node) itr.next();
			node.setParent(null);
			newLogs.add(node);
		}
		root = logs.getParent();
		root.remove(logs);
		root.add(newLogs);
		System.out.println(doc.asXML());
	}

}
