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

package self.micromagic.eterna.dao.impl;

import java.sql.Connection;
import java.sql.SQLException;

import org.dom4j.Element;

import self.micromagic.eterna.dao.Dao;
import self.micromagic.eterna.dao.DaoLogger;
import self.micromagic.eterna.share.EternaException;
import self.micromagic.eterna.share.EternaFactory;
import self.micromagic.util.logging.TimeLogger;

public class TestDaoLogger
		implements DaoLogger
{
	public boolean initialize(EternaFactory factory)
			throws EternaException
	{
		return false;
	}

	public String getName()
			throws EternaException
	{
		return this.name;
	}
	public void setName(String name)
	{
		this.name = name;
	}
	private String name;

	public void log(Dao base, Element node, TimeLogger usedTime, Throwable error, Connection conn)
			throws EternaException, SQLException
	{
		System.out.println("-------------------------------------");
		System.out.println("log name " + this.name);
		if (error == null)
		{
			System.out.println(node.element("script").asXML());
			System.out.println(node.element("parameters").asXML());
		}
		else
		{
			System.out.println(node.element("script").asXML());
			System.out.println("error: " + error);
		}
	}

}
