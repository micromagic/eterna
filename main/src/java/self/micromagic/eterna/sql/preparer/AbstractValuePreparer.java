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

package self.micromagic.eterna.sql.preparer;

import org.apache.commons.logging.Log;
import self.micromagic.eterna.share.Tool;
import self.micromagic.eterna.share.EternaFactory;
import self.micromagic.eterna.digester.ConfigurationException;

public abstract class AbstractValuePreparer
		implements ValuePreparer
{
	protected static final Log log = Tool.log;

	protected ValuePreparerCreater vpc;
	protected int index;
	protected String name;

	public AbstractValuePreparer(ValuePreparerCreater vpc)
	{
		this.vpc = vpc;
	}

	public ValuePreparerCreater getCreater()
	{
		return this.vpc;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getName()
	{
		return this.name;
	}

	public void setRelativeIndex(int index)
	{
		this.index = index;
	}

	public int getRelativeIndex()
	{
		return this.index;
	}

	static abstract class AbstractCreater
			implements ValuePreparerCreater
	{
		protected ValuePreparerCreaterGenerator vpcg;

		public AbstractCreater(ValuePreparerCreaterGenerator vpcg)
		{
			this.vpcg = vpcg;
		}

		public EternaFactory getFactory()
				throws ConfigurationException
		{
			return this.vpcg.getFactory();
		}

		public Object getAttribute(String name)
				throws ConfigurationException
		{
			return this.vpcg.getAttribute(name);
		}

		public boolean isEmptyStringToNull()
		{
			return this.vpcg.isEmptyStringToNull();
		}

	}

}