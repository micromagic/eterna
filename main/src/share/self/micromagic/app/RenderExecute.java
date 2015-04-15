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

import javax.portlet.PortletMode;
import javax.portlet.WindowState;

import self.micromagic.eterna.model.AppData;
import self.micromagic.eterna.model.Execute;
import self.micromagic.eterna.model.ModelAdapter;
import self.micromagic.eterna.model.ModelExport;
import self.micromagic.eterna.model.impl.AbstractExecute;
import self.micromagic.eterna.share.Generator;
import self.micromagic.eterna.share.EternaException;

public class RenderExecute extends AbstractExecute
		implements Execute, Generator
{
	protected ModelExport view = null;
	protected ModelExport edit = null;
	protected ModelExport help = null;

	public void initialize(ModelAdapter model)
				throws EternaException
	{
		if (this.initialized)
		{
			return;
		}
		super.initialize(model);
		String tmp = (String) this.getAttribute("view");
		if (tmp != null)
		{
			this.view = model.getFactory().getModelExport(tmp);
			if (this.view == null)
			{
				log.warn("The Model Export [" + tmp + "] not found.");
			}
		}
		tmp = (String) this.getAttribute("edit");
		if (tmp != null)
		{
			this.edit = model.getFactory().getModelExport(tmp);
			if (this.edit == null)
			{
				log.warn("The Model Export [" + tmp + "] not found.");
			}
		}
		tmp = (String) this.getAttribute("help");
		if (tmp != null)
		{
			this.help = model.getFactory().getModelExport(tmp);
			if (this.help == null)
			{
				log.warn("The Model Export [" + tmp + "] not found.");
			}
		}
	}

	public String getExecuteType()
			throws EternaException
	{
		return "doRender";
	}

	public ModelExport execute(AppData data, Connection conn)
			throws EternaException, SQLException, IOException
	{
		WindowState state = data.renderRequest.getWindowState();
		if (!state.equals(WindowState.MINIMIZED))
		{
			PortletMode mode = data.renderRequest.getPortletMode();
			if (PortletMode.VIEW.equals(mode))
			{
				return this.view;
			}
			else if (PortletMode.EDIT.equals(mode))
			{
				return this.edit;
			}
			else if (PortletMode.HELP.equals(mode))
			{
				return this.help;
			}
			else
			{
				log.error("Unknown portlet mode: " + mode + ".");
			}
		}
		return null;
	}

}