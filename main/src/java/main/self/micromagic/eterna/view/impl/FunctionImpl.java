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

package self.micromagic.eterna.view.impl;

import self.micromagic.eterna.share.AbstractGenerator;
import self.micromagic.eterna.share.EternaException;
import self.micromagic.eterna.share.EternaFactory;
import self.micromagic.eterna.view.Function;
import self.micromagic.eterna.view.ModifiableViewRes;
import self.micromagic.eterna.view.View;

/**
 * @author micromagic@sina.com
 */
public class FunctionImpl extends AbstractGenerator
			implements Function
{
	private String param = "";
	private String scriptBody = "";

	private ModifiableViewRes viewRes = null;

	public String getParam()
	{
		return this.param;
	}

	public void setParam(String param)
	{
		this.param = param;
	}

	public String getBody()
	{
		return this.scriptBody;
	}

	public void setBody(String body)
	{
		this.scriptBody = body;
	}

	public EternaFactory getFactory()
	{
		return (EternaFactory) this.factory;
	}

	public View.ViewRes getViewRes()
			throws EternaException
	{
		if (this.viewRes == null)
		{
			this.viewRes = new ModifiableViewResImpl();
			this.scriptBody = ViewTool.dealScriptPart(
					this.viewRes, this.scriptBody, ViewTool.GRAMMER_TYPE_EXPRESSION, this.getFactory());
		}
		return this.viewRes;
	}

	public Object create()
	{
		return this;
	}

	public boolean initialize(EternaFactory factory)
	{
		return false;
	}


}