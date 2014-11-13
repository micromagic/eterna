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
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import self.micromagic.eterna.digester.ConfigurationException;
import self.micromagic.eterna.model.AppData;
import self.micromagic.eterna.model.ModelExport;
import self.micromagic.eterna.sql.ResultIterator;
import self.micromagic.eterna.sql.ResultMetaData;
import self.micromagic.eterna.sql.ResultReader;
import self.micromagic.eterna.sql.ResultRow;
import self.micromagic.util.MemoryStream;
import self.micromagic.util.Utility;

/**
 * 将ResultIterator导出成csv格式
 */
public class ExportCSV extends AbstractExportExecute
{
	public String getExecuteType()
			throws ConfigurationException
	{
		return "exportCSV";
	}

	public ModelExport execute(AppData data, Connection conn)
			throws ConfigurationException, SQLException, IOException
	{
		ResultIterator ritr = null;
		Writer out = null;
		try
		{
			ritr = this.getResultIterator(data, conn);
			HttpServletResponse response = data.getHttpServletResponse();
			if (!saveExport && response != null)
			{
				response.setContentType("text/csv; charset=" + this.encodeName);
				response.setHeader("Content-disposition", "attachment; "
						+ getFileNameParam(data, this.getFileName(data) + ".csv", this.fileNameEncode));
				out = new OutputStreamWriter(data.getOutputStream(), this.encodeName);
			}
			else
			{
				MemoryStream ms = new MemoryStream(1, 1024 * 4);
				out = new OutputStreamWriter(ms.getOutputStream(), this.encodeName);
				Map raMap = data.getRequestAttributeMap();
				raMap.put(DOWNLOAD_CONTENTTYPE, "text/csv; charset=" + this.encodeName);
				raMap.put(DOWNLOAD_FILENAME, this.getFileName(data) + ".csv");
				raMap.put(DOWNLOAD_STREAM, ms.getInputStream());
			}
			this.dealExportCSV(out, ritr, data, conn);
		}
		catch (Exception ex)
		{
			log.error("Write excel error.", ex);
		}
		finally
		{
			if (ritr != null)
			{
				// 这里可能是需要接管数据库链接, 所以使用完后需要自行释放
				ritr.close();
			}
			this.closeOutput(out);
		}
		return null;
	}

	protected void dealExportCSV(Writer out, ResultIterator ritr, AppData data, Connection conn)
			throws Exception
	{
		ResultMetaData meta = ritr.getMetaData();
		int count = meta.getColumnCount();
		boolean[] notPrint = new boolean[count];

		int skipColumnCount = 0;
		for (int i = 0; i < count; i++)
		{
			ResultReader reader = meta.getColumnReader(i + 1);
			notPrint[i] = "true".equalsIgnoreCase((String) reader.getAttribute(PRINT_EXCLUDE));
			if (notPrint[i])
			{
				skipColumnCount++;
				continue;
			}
			if (i - skipColumnCount > 0)
			{
				out.write(',');
			}
			String caption = (String) reader.getAttribute(PRINT_CAPTION);
			if (caption == null)
			{
				caption = meta.getColumnCaption(i + 1);
			}
			this.writeString(out, caption);
		}
		while (ritr.hasMoreRow())
		{
			out.write(Utility.LINE_SEPARATOR);
			ResultRow row = ritr.nextRow();
			skipColumnCount = 0;
			for (int i = 0; i < count; i++)
			{
				if (notPrint[i])
				{
					skipColumnCount++;
					continue;
				}
				if (i - skipColumnCount > 0)
				{
					out.write(',');
				}
				this.writeString(out, row.getFormated(i + 1).toString());
			}
		}
	}

	protected void writeString(Writer out, String str)
			throws IOException
	{
		if (str == null)
		{
			return;
		}
		int modifyCount = 0;
		for (int i = 0; i < str.length(); i++)
		{
			char c = str.charAt(i);
			String appendStr = null;
			if (c == ',' || c < ' ')
			{
				modifyCount++;
			}
			else if (c == '"')
			{
				appendStr = "\"\"";
				modifyCount++;
			}
			if (modifyCount == 1)
			{
				out.write('"');
				out.write(str.substring(0, i));
				//这里将modifyCount的个数增加, 防止下一次调用使他继续进入这个初始化
				modifyCount++;
			}
			if (modifyCount > 0)
			{
				if (appendStr == null)
				{
					out.write(c);
				}
				else
				{
					out.write(appendStr);
				}
			}
		}
		if (modifyCount > 0)
		{
			out.write('"');
		}
		else
		{
			out.write(str);
		}
	}

}