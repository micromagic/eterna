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

package self.micromagic.app;

import java.sql.Connection;
import java.sql.SQLException;
import java.io.IOException;
import java.util.List;

import self.micromagic.eterna.model.impl.AbstractExecute;
import self.micromagic.eterna.model.Execute;
import self.micromagic.eterna.model.ModelAdapter;
import self.micromagic.eterna.model.ModelExport;
import self.micromagic.eterna.model.AppData;
import self.micromagic.eterna.share.Generator;
import self.micromagic.eterna.digester.ConfigurationException;
import self.micromagic.util.CodeItemManager;

public class CodeItemExecute extends AbstractExecute
		implements Execute, Generator
{
	private String codeName;
	private boolean useCName = false;
	private String valueName;

	public void initialize(ModelAdapter model)
			throws ConfigurationException
	{
		if (this.initialized)
		{
			return;
		}
		super.initialize(model);
		this.codeName = (String) this.getAttribute("codeName");
		if (this.codeName == null)
		{
			this.codeName = this.getName();
		}
		String tmpBool = (String) this.getAttribute("useCName");
		if (tmpBool != null)
		{
			this.useCName = "true".equalsIgnoreCase(tmpBool);
		}
		this.valueName = (String) this.getAttribute("valueName");
	}

	public String getExecuteType() throws ConfigurationException
	{
		return "codeItem";
	}

	public ModelExport execute(AppData data, Connection conn)
			throws ConfigurationException, SQLException, IOException
	{
		List item;
		if (this.useCName)
		{
			CodeItemManager.CodeItemProperty cp = CodeItemManager.getPropertyByCName(this.codeName);
			item = CodeItemManager.getCodeItems(conn, cp);
		}
		else
		{
			item = CodeItemManager.getCodeItems(conn, this.codeName);
		}
		if (this.valueName != null)
		{
			data.dataMap.put(this.valueName, item);
		}
		else
		{
			data.push(item);
		}
		return null;
	}

}