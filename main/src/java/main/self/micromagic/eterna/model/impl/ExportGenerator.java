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

package self.micromagic.eterna.model.impl;

import self.micromagic.eterna.model.ModelExport;
import self.micromagic.eterna.share.AbstractGenerator;
import self.micromagic.eterna.share.EternaException;
import self.micromagic.eterna.share.Generator;

public class ExportGenerator extends AbstractGenerator
		implements Generator
{
	public void setViewName(String viewName)
	{
		this.viewName = viewName;
	}
	private String viewName;

	public void setModelName(String modelName)
	{
		this.modelName = modelName;
	}
	private String modelName;

	public void setPath(String path)
	{
		this.path = path;
	}
	private String path;

	public void setRedirect(boolean redirect)
	{
		this.redirect = redirect;
	}
	private boolean redirect;

	public void setErrorExport(boolean errorExport)
	{
		this.errorExport = errorExport;
	}
	private boolean errorExport;

	public Object create()
	{
		ModelExport export;
		if (this.modelName != null)
		{
			if (this.viewName != null || this.path != null)
			{
				throw new EternaException(
						"Can't set the attribute 'path' or 'viewName' when given the attribute 'modelName'.");
			}
			export = new ModelExport(this.getName(), this.redirect, this.modelName);
		}
		else if (path != null)
		{
			export = this.viewName == null ? new ModelExport(this.getName(), this.path, this.redirect)
					: new ModelExport(this.getName(), this.path, this.viewName, redirect);
		}
		else
		{
			throw new EternaException("Must set the attribute 'path' or 'modelName'.");
		}
		if (this.errorExport)
		{
			export.setErrorExport(this.errorExport);
		}
		return export;
	}

}
