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

import self.micromagic.eterna.model.impl.AbstractExecute;
import self.micromagic.eterna.model.Execute;
import self.micromagic.eterna.model.ModelExport;
import self.micromagic.eterna.model.AppData;
import self.micromagic.eterna.model.ModelAdapter;
import self.micromagic.eterna.share.Generator;
import self.micromagic.eterna.digester.ConfigurationException;
import self.micromagic.coder.SpellCoder;
import self.micromagic.util.Utils;

/**
 * 将指定cache中的中文转换成拼音.
 * 转换后的拼音会放到stack中.
 */
public class SpellCoderExecute extends AbstractExecute
		implements Execute, Generator
{
	protected SpellCoder spellCoder = new SpellCoder();
	protected int srcIndex = 0;

	public void initialize(ModelAdapter model)
			throws ConfigurationException
	{
		if (this.initialized)
		{
			return;
		}
		super.initialize(model);
		String tmp;

		tmp = (String) this.getAttribute("firstLetterOnly");
		if (tmp != null)
		{
			this.spellCoder.setFirstLetterOnly("true".equalsIgnoreCase(tmp));
		}
		tmp = (String) this.getAttribute("unknowWordToInterrogation");
		if (tmp != null)
		{
			this.spellCoder.setUnknowWordToInterrogation("true".equalsIgnoreCase(tmp));
		}
		tmp = (String) this.getAttribute("enableUnicodeLetter");
		if (tmp != null)
		{
			this.spellCoder.setEnableUnicodeLetter("true".equalsIgnoreCase(tmp));
		}
		tmp = (String) this.getAttribute("ignoreOtherLetter");
		if (tmp != null)
		{
			this.spellCoder.setIgnoreOtherLetter("true".equalsIgnoreCase(tmp));
		}
		tmp = (String) this.getAttribute("srcIndex");
		if (tmp != null)
		{
			this.srcIndex = Utils.parseInt(tmp);
		}
	}

	public String getExecuteType()
	{
		return "spellCoder";
	}

	public ModelExport execute(AppData data, Connection conn)
			throws ConfigurationException, SQLException, IOException
	{
		Object obj = data.caches[this.srcIndex];
		if (obj != null && obj instanceof String)
		{
			data.push(this.spellCoder.makeSpellCode((String) obj));
		}
		else
		{
			data.push(null);
		}
		return null;
	}

}