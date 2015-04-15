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
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import jxl.Workbook;
import jxl.write.DateFormat;
import jxl.write.NumberFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import self.micromagic.eterna.model.AppData;
import self.micromagic.eterna.model.ModelExport;
import self.micromagic.eterna.sql.ResultIterator;
import self.micromagic.eterna.sql.ResultMetaData;
import self.micromagic.eterna.sql.ResultReader;
import self.micromagic.eterna.sql.ResultRow;
import self.micromagic.eterna.share.EternaException;
import self.micromagic.util.MemoryStream;

/**
 * 注: 使用此类进行导出excel时, 请将model的transactionType设为hold
 */
public class ExportExcelExecute extends AbstractExportExecute
{
	public static final int PRINT_TYPE_NUMBER = 1;
	public static final int PRINT_TYPE_DATE = 2;

	public String getExecuteType()
			throws EternaException
	{
		return "exportExcel";
	}

	public ModelExport execute(AppData data, Connection conn)
			throws EternaException, SQLException, IOException
	{
		ResultIterator ritr = null;
		OutputStream out = null;
		try
		{
			ritr = this.getResultIterator(data, conn);
			HttpServletResponse response = data.getHttpServletResponse();
			WritableWorkbook workbook;
			if (!saveExport && response != null)
			{
				response.setContentType("application/vnd.ms-excel");
				response.setHeader("Content-disposition", "attachment; "
						+ getFileNameParam(data, this.getFileName(data) + ".xls", this.fileNameEncode));
				out = data.getOutputStream();
				workbook = this.createWorkBook(data, out);
			}
			else
			{
				MemoryStream ms = new MemoryStream(1, 1024 * 4);
				out = ms.getOutputStream();
				workbook = this.createWorkBook(data, out);
				Map raMap = data.getRequestAttributeMap();
				raMap.put(DOWNLOAD_CONTENTTYPE, "application/vnd.ms-excel");
				raMap.put(DOWNLOAD_FILENAME, this.getFileName(data) + ".xls");
				raMap.put(DOWNLOAD_STREAM, ms.getInputStream());
			}
			this.dealExportExcel(workbook, ritr, data, conn);
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

	protected void dealExportExcel(WritableWorkbook workbook, ResultIterator ritr,
			AppData data, Connection conn)
			throws Exception
	{
		WritableSheet sheet = workbook.createSheet(this.getFileName(data), 0);
		ResultMetaData meta = ritr.getMetaData();
		int count = meta.getColumnCount();
		jxl.write.WritableCellFormat[] formats = new jxl.write.WritableCellFormat[count];
		int[] types = new int[count];
		boolean[] notPrint = new boolean[count];
		Map formatsMap = new HashMap();
		WritableFont title = new WritableFont(WritableFont.createFont("宋体"),
				12, WritableFont.BOLD, false);
		WritableFont normal = new WritableFont(WritableFont.createFont("宋体"),
				12, WritableFont.NO_BOLD, false);
		jxl.write.WritableCellFormat wcfTitle = new jxl.write.WritableCellFormat(title);
		jxl.write.WritableCellFormat wcfNormal = new jxl.write.WritableCellFormat(normal);
		wcfTitle.setBorder(jxl.format.Border.ALL, jxl.format.BorderLineStyle.THIN);
		wcfTitle.setVerticalAlignment(jxl.format.VerticalAlignment.CENTRE);
		wcfNormal.setBorder(jxl.format.Border.ALL, jxl.format.BorderLineStyle.THIN);
		wcfNormal.setVerticalAlignment(jxl.format.VerticalAlignment.CENTRE);

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
			String format = (String) reader.getAttribute(PRINT_FORMAT);
			if (format != null)
			{
				String type = (String) reader.getAttribute(PRINT_COLTYPE);
				formats[i] = this.createFormat(format, type, formatsMap, wcfNormal, normal);
				if ("number".equals(type))
				{
					types[i] = PRINT_TYPE_NUMBER;
				}
				else if ("date".equals(type))
				{
					types[i] = PRINT_TYPE_DATE;
				}
			}
			else
			{
				formats[i] = wcfNormal;
			}

			int width = -1;
			try
			{
				String tempW = (String) reader.getAttribute(PRINT_WIDTH);
				if (tempW != null)
				{
					width = Integer.parseInt(tempW);
				}
			}
			catch (Exception ex) {}
			if (width == -1) width = meta.getColumnWidth(i + 1);
			if (width != -1) sheet.setColumnView(i - skipColumnCount, width);
			String caption = (String) reader.getAttribute(PRINT_CAPTION);
			if (caption == null)
			{
				caption = meta.getColumnCaption(i + 1);
			}
			jxl.write.Label label = new jxl.write.Label(i - skipColumnCount, 0, caption, wcfTitle);
			sheet.addCell(label);
		}
		int rowIndex = 1;
		while (ritr.hasMoreRow())
		{
			ResultRow row = ritr.nextRow();
			skipColumnCount = 0;
			for (int i = 0; i < count; i++)
			{
				if (notPrint[i])
				{
					skipColumnCount++;
					continue;
				}
				if (types[i] == PRINT_TYPE_NUMBER)
				{
					jxl.write.Number tmp = new jxl.write.Number(i - skipColumnCount, rowIndex,
							row.getDouble(i + 1), formats[i]);
					sheet.addCell(tmp);
				}
				else if (types[i] == PRINT_TYPE_DATE)
				{
					java.sql.Timestamp date = row.getTimestamp(i + 1);
					if (date != null)
					{
						jxl.write.DateTime tmp = new jxl.write.DateTime(i - skipColumnCount, rowIndex, date, formats[i]);
						sheet.addCell(tmp);
					}
					else
					{
						jxl.write.Label label = new jxl.write.Label(i - skipColumnCount, rowIndex, "", wcfNormal);
						sheet.addCell(label);
					}
				}
				else
				{
					jxl.write.Label label = new jxl.write.Label(i - skipColumnCount, rowIndex,
							row.getFormated(i + 1).toString(), formats[i]);
					sheet.addCell(label);
				}
			}
			rowIndex++;
		}

		workbook.write();
		workbook.close();
	}

	protected WritableWorkbook createWorkBook(AppData data, OutputStream out)
			throws IOException
	{
		return Workbook.createWorkbook(out);
	}

	protected jxl.write.WritableCellFormat createFormat(String format, String type, Map formatsMap,
			jxl.write.WritableCellFormat defaultCF, WritableFont font)
			throws WriteException
	{
		jxl.write.WritableCellFormat tmp = (jxl.write.WritableCellFormat) formatsMap.get(format);
		if (tmp != null)
		{
			return tmp;
		}
		if ("number".equals(type))
		{
			NumberFormat nf = new NumberFormat(format);
			tmp = new jxl.write.WritableCellFormat(font, nf);
			tmp.setBorder(jxl.format.Border.ALL, jxl.format.BorderLineStyle.THIN);
			tmp.setVerticalAlignment(jxl.format.VerticalAlignment.CENTRE);
			formatsMap.put(format, tmp);
			return tmp;
		}
		else if ("date".equals(type))
		{
			DateFormat df = new DateFormat(format);
			tmp = new jxl.write.WritableCellFormat(font, df);
			tmp.setBorder(jxl.format.Border.ALL, jxl.format.BorderLineStyle.THIN);
			tmp.setVerticalAlignment(jxl.format.VerticalAlignment.CENTRE);
			formatsMap.put(format, tmp);
			return tmp;
		}
		return defaultCF;
	}

	static
	{
		// 载入ReadExcelExecute, 初始化jxl的unicode.encoding
		new ReadExcelExecute();
	}

}