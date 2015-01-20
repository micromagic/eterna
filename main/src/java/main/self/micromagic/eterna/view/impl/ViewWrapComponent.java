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

import java.util.Iterator;

import self.micromagic.eterna.share.EternaException;
import self.micromagic.eterna.share.EternaFactory;
import self.micromagic.eterna.view.Component;
import self.micromagic.eterna.view.View;
import self.micromagic.util.container.UnmodifiableIterator;
import self.micromagic.util.StringAppender;
import self.micromagic.util.StringTool;

public class ViewWrapComponent extends ComponentImpl
		implements Component
{
	protected View view;
	protected boolean needScript = true;

	public ViewWrapComponent(View view)
	{
		this.view = view;
	}

	public void initialize(EternaFactory factory, Component parent)
			throws EternaException
	{
		if (this.initialized)
		{
			return;
		}
		this.eternaFactory = factory;
		this.initialized = true;
		this.stringCoder = factory.getStringCoder();
		String tmp;

		tmp = (String) factory.getAttribute(VIEW_WRPA_NEED_SCRIPT_FLAG);
		if (tmp != null)
		{
			this.needScript = "true".equalsIgnoreCase(tmp);
		}
		tmp = (String) factory.getAttribute(VIEW_WRPA_TYPE_FLAG);
		if (tmp == null || SPECIAL_TYPE_NONE.equals(tmp))
		{
			this.type = SPECIAL_TYPE_NONE;
		}
		else if (NORMAL_TYPE_DIV.equalsIgnoreCase(tmp))
		{
			this.type = tmp;
		}
		else
		{
			log.error("Error view wrap type:[" + tmp + "].");
			this.type = SPECIAL_TYPE_NONE;
		}
		if (!SPECIAL_TYPE_NONE.equals(this.type))
		{
			if (this.view.getWidth() != null && this.view.getHeight() != null)
			{
				StringAppender buf = StringTool.createStringAppender();
				buf.append("css:{");
				if (this.view.getWidth() != null)
				{
					buf.append("width:\"").append(this.view.getWidth()).append('"');
				}
				if (this.view.getHeight() != null)
				{
					if (this.view.getWidth() != null)
					{
						buf.append(',');
					}
					buf.append("height:\"").append(this.view.getHeight()).append('"');
				}
				buf.append('}');
				this.componentParam = new String(buf.toString());
			}
		}

		Iterator subComponentItr = this.getSubComponents();
		while (subComponentItr.hasNext())
		{
			Component sub = (Component) subComponentItr.next();
			sub.initialize(factory, null);
		}
	}

	public String getName()
			throws EternaException
	{
		return this.view.getName();
	}

	public Component getParent()
	{
		return null;
	}

	public Iterator getSubComponents()
			throws EternaException
	{
		return this.view.getComponents();
	}

	public Iterator getEvents()
	{
		return UnmodifiableIterator.EMPTY_ITERATOR;
	}

	public String getBeforeInit()
			throws EternaException
	{
		return this.needScript ? this.view.getBeforeInit() : null;
	}

	public String getInitScript()
			throws EternaException
	{
		return this.needScript ? this.view.getInitScript() : null;
	}

}