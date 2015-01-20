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

package self.micromagic.eterna.base.preparer;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;

import self.micromagic.eterna.share.Tool;
import self.micromagic.util.StringRef;

abstract class AbstractNumberCreater extends AbstractPreparerCreater
{
	public AbstractNumberCreater(String name)
	{
		super(name);
	}

	protected NumberFormat format = null;

	public void setFormat(String formatStr)
	{
		StringRef realPattern = new StringRef();
		Locale locale = Tool.parseLocal(formatStr, realPattern);
		if (locale == null)
		{
			format = new DecimalFormat(formatStr);
		}
		else
		{
			format = new DecimalFormat(realPattern.getString(),
					new DecimalFormatSymbols(locale));
		}
	}

}
