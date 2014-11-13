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

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import self.micromagic.eterna.digester.ConfigurationException;
import self.micromagic.eterna.model.AppData;
import self.micromagic.eterna.model.Execute;
import self.micromagic.eterna.model.ModelAdapter;
import self.micromagic.eterna.model.ModelExport;
import self.micromagic.eterna.model.impl.AbstractExecute;
import self.micromagic.eterna.share.Generator;
import self.micromagic.util.StringTool;
import self.micromagic.util.container.RequestParameterMap;

public class EditParameterMapExecute extends AbstractExecute
		implements Execute, Generator
{
	protected Map initValues = null;

	public void initialize(ModelAdapter model)
				throws ConfigurationException
	{
		if (this.initialized)
		{
			return;
		}
		super.initialize(model);
		String tmp;

		tmp = (String) this.getAttribute("initValues");
		if (tmp != null)
		{
			this.initValues = StringTool.string2Map(tmp, ",;", '=');
		}
	}

	public String getExecuteType() throws ConfigurationException
	{
		return "editParameterMap";
	}

	public ModelExport execute(AppData data, Connection conn)
			throws ConfigurationException, SQLException, IOException
	{
		Map map = data.getRequestParameterMap();
		boolean changeMap = true;
		if (map instanceof RequestParameterMap)
		{
			RequestParameterMap rpm = (RequestParameterMap) map;
			if (!rpm.isReadOnly())
			{
				changeMap = false;
			}
			else
			{
				map = rpm.getOriginParamMap();
			}
		}
		if (changeMap)
		{
			Map tmp = RequestParameterMap.create(map, false);
			data.maps[AppData.REQUEST_PARAMETER_MAP] = tmp;
			map = tmp;
		}
		if (this.initValues != null)
		{
			map.putAll(this.initValues);
		}
		return null;
	}

}