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
import java.util.HashMap;
import java.util.Map;

import org.dom4j.Element;
import self.micromagic.eterna.model.AppData;
import self.micromagic.eterna.model.AppDataLogExecute;
import self.micromagic.eterna.model.Execute;
import self.micromagic.eterna.model.ModelAdapter;
import self.micromagic.eterna.model.ModelExport;
import self.micromagic.eterna.model.impl.AbstractExecute;
import self.micromagic.eterna.share.Generator;
import self.micromagic.eterna.share.EternaException;
import self.micromagic.util.StringTool;

/**
 * 将数据设置到数据集中的执行器.
 *
 * 可设置的属性列表
 *
 * needPrepare      是否需要将准备好的数据设置的数据集中, 默认值为true
 *
 * pushPrepare      是否需要将准备好的数据(map)压入堆栈, 默认值为false
 *
 * prepares         需要准备的数据, 键和值的分隔符为"=", 元素项的分隔符为";"
 *
 *
 * @author micromagic@sina.com
 */
public class DataPrepareExecute extends AbstractExecute
		implements Execute, Generator
{
	protected Map prepares;
	protected boolean needPrepare = true;
	protected boolean pushPrepare = false;

	public void initialize(ModelAdapter model)
			throws EternaException
	{
		if (this.initialized)
		{
			return;
		}
		String tmp;

		tmp = (String) this.getAttribute("needPrepare");
		if (tmp != null)
		{
			this.needPrepare = "true".equalsIgnoreCase(tmp);
		}
		tmp = (String) this.getAttribute("pushPrepare");
		if (tmp != null)
		{
			this.pushPrepare = "true".equalsIgnoreCase(tmp);
		}

		tmp = (String) this.getAttribute("prepares");
		if (tmp != null)
		{
			this.prepares = StringTool.string2Map(tmp, ";", '=');
		}
		else
		{
			this.prepares = new HashMap();
		}
	}

	public String getExecuteType()
	{
		return "dataPrepare";
	}

	public ModelExport execute(AppData data, Connection conn)
			throws EternaException, SQLException, IOException
	{
		if (data.getLogType() > 0)
		{
			Element nowNode = data.getCurrentNode();
			if (nowNode != null)
			{
				AppDataLogExecute.printObject(nowNode.addElement("prepares"), this.prepares);
			}
		}
		if (this.needPrepare)
		{
			data.dataMap.putAll(this.prepares);
		}
		if (this.pushPrepare)
		{
			data.push(this.prepares);
		}
		return null;
	}

}