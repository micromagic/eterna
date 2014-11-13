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

package self.micromagic.eterna.search;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.sql.Connection;
import java.sql.SQLException;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import self.micromagic.eterna.digester.ConfigurationException;
import self.micromagic.eterna.model.AppData;
import self.micromagic.eterna.model.Execute;
import self.micromagic.eterna.model.ModelAdapter;
import self.micromagic.eterna.model.ModelExport;
import self.micromagic.eterna.model.impl.AbstractExecute;
import self.micromagic.eterna.security.EmptyPermission;
import self.micromagic.eterna.security.Permission;
import self.micromagic.eterna.security.User;
import self.micromagic.eterna.share.EternaFactory;
import self.micromagic.eterna.share.Generator;
import self.micromagic.eterna.sql.QueryAdapter;
import self.micromagic.eterna.sql.ResultIterator;
import self.micromagic.eterna.sql.UpdateAdapter;
import self.micromagic.util.MemoryChars;

/**
 * 查询条件配置信息获取及保存的execute.
 *
 * @author micromagic@sina.com
 */
public class SearchExecute extends AbstractExecute
		implements Execute, Generator
{
	private static Document DEFAULT_SETTING = DocumentHelper.createDocument();
	public static final String DEFAULT_USERID = "[default]";

	private EternaFactory factory;

	private int sql_get_setting_id;
	private int sql_delete_setting_id;
	private int sql_add_setting_id;

	private int userCacheIndex = 0;
	private int resultCacheIndex = 1;

	static
	{
		DEFAULT_SETTING.addElement("eterna").addElement("groups").addElement("group")
				.addAttribute("caption", "group 1").addAttribute("name", "0");
	}

	public void initialize(ModelAdapter model)
			throws ConfigurationException
	{
		if (this.initialized)
		{
			return;
		}
		super.initialize(model);
		this.factory = model.getFactory();

		String temp = (String) this.getAttribute("userCacheIndex");
		if (temp != null)
		{
			this.userCacheIndex = Integer.parseInt(temp);
		}
		temp = (String) this.getAttribute("resultCacheIndex");
		if (temp != null)
		{
			this.resultCacheIndex = Integer.parseInt(temp);
		}

		this.sql_get_setting_id = this.factory.getQueryAdapterId("eterna.search.get.search_setting");
		this.sql_delete_setting_id = this.factory.getUpdateAdapterId("eterna.search.delete.search_setting");
		this.sql_add_setting_id = this.factory.getUpdateAdapterId("eterna.search.insert.search_setting");
	}

	public String getExecuteType() throws ConfigurationException
	{
		return "search setting";
	}

	public ModelExport execute(AppData data, Connection conn)
			throws ConfigurationException, SQLException, IOException
	{
		String search = data.getRequestParameter("search");
		String action = data.getRequestParameter("action");
		String id = data.getRequestParameter("id");
		User user = (User) data.caches[this.userCacheIndex];
		String username;
		Permission permission;
		if (user == null)
		{
			username = DEFAULT_USERID;
			permission = EmptyPermission.getInstance();
		}
		else
		{
			username = user.getUserId();
			permission = user.getPermission();
		}

		if ("load".equals(action))
		{
			QueryAdapter query = this.factory.createQueryAdapter(this.sql_get_setting_id);
			query.setString(1, username);
			query.setString(2, search);
			query.setString(3, id);
			ResultIterator ritr = query.executeQuery(conn);
			if (ritr.getRecordCount() == 0)
			{
				MemoryChars mc = new MemoryChars();
				Writer out = mc.getWriter();
				out.write(DEFAULT_SETTING.asXML());
				data.caches[this.resultCacheIndex] = mc.getReader();
			}
			else
			{
				MemoryChars mc = new MemoryChars();
				Writer out = mc.getWriter();
				while (ritr.hasMoreRow())
				{
					out.write(ritr.nextRow().getString("settingXML"));
				}
				data.caches[this.resultCacheIndex] = mc.getReader();
			}
		}
		else if ("save".equals(action))
		{
			UpdateAdapter update = this.factory.createUpdateAdapter(this.sql_delete_setting_id);
			update.setString(1, username);
			update.setString(2, search);
			update.setString(3, id);
			update.executeUpdate(conn);
			update = this.factory.createUpdateAdapter(this.sql_add_setting_id);
			update.setString(1, username);
			update.setString(2, search);
			update.setString(3, id);
			String settingXML = data.getRequestParameter("settingXML");

			int index = 1;
			while (settingXML.length() > 0)
			{
				String temp;
				if (log.isDebugEnabled())
				{
					log.debug("Left setting xml:" + settingXML);
					log.debug("Left xml length:" + settingXML.length());
				}
				if (settingXML.length() > 1000)
				{
					temp = settingXML.substring(0, 1000);
					settingXML = settingXML.substring(1000);
				}
				else
				{
					temp = settingXML;
					settingXML = "";

				}
				update.setInt(4, index);
				update.setString(5, temp);
				update.executeUpdate(conn);
				index++;
			}
			data.caches[this.resultCacheIndex] = new StringReader((index - 1) + "");
		}
		else // default  get ConditionProperty
		{
			SearchAdapter sa = this.factory.createSearchAdapter(search);
			Reader reader = sa.getConditionDocument(permission);
			data.caches[this.resultCacheIndex] = reader;
		}
		return null;
	}

}