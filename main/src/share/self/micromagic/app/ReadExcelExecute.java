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
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.Format;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import jxl.BooleanCell;
import jxl.Cell;
import jxl.CellType;
import jxl.DateCell;
import jxl.NumberCell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.biff.StringHelper;
import jxl.read.biff.BiffException;
import org.apache.commons.fileupload.FileItem;
import self.micromagic.eterna.model.AppData;
import self.micromagic.eterna.model.Execute;
import self.micromagic.eterna.model.ModelAdapter;
import self.micromagic.eterna.model.ModelExport;
import self.micromagic.eterna.model.impl.AbstractExecute;
import self.micromagic.eterna.share.Generator;
import self.micromagic.eterna.share.TypeManager;
import self.micromagic.eterna.share.EternaException;
import self.micromagic.eterna.sql.ResultReader;
import self.micromagic.eterna.sql.ResultReaderManager;
import self.micromagic.eterna.sql.impl.ResultReaders;
import self.micromagic.util.Utility;

public class ReadExcelExecute extends AbstractExecute
		implements Execute, Generator
{
	private int cacheIndex = 0;
	private String errorRows_name = "errorRows";
	private String errorRowFlags_name = "errorRowFlags";
	private int sheetIndex = 0;
	private int titleRowCount = 1;
	private boolean skipEmptyRow = true;
	private boolean needRowIndex = true;
	private boolean[] needTrim;
	private ResultReader[] readers;
	private ResultReaderManager readerManager;
	private String excelCharset = null;

	public void initialize(ModelAdapter model)
			throws EternaException
	{
		if (this.initialized)
		{
			return;
		}
		super.initialize(model);
		String temp = (String) this.getAttribute("titleRowCount");
		if (temp != null)
		{
			this.titleRowCount = Integer.parseInt(temp);
		}
		temp = (String) this.getAttribute("errorRows_name");
		if (temp != null)
		{
			this.errorRows_name = temp;
		}
		temp = (String) this.getAttribute("errorRowFlags_name");
		if (temp != null)
		{
			this.errorRowFlags_name = temp;
		}
		temp = (String) this.getAttribute("cacheIndex");
		if (temp != null)
		{
			this.cacheIndex = Integer.parseInt(temp);
		}
		temp = (String) this.getAttribute("sheetIndex");
		if (temp != null)
		{
			this.sheetIndex = Integer.parseInt(temp);
		}
		temp = (String) this.getAttribute("skipEmptyRow");
		if (temp != null)
		{
			this.skipEmptyRow = "true".equalsIgnoreCase(temp);
		}
		temp = (String) this.getAttribute("needRowIndex");
		if (temp != null)
		{
			this.needRowIndex = "true".equalsIgnoreCase(temp);
		}
		temp = (String) this.getAttribute("excelCharset");
		if (temp != null)
		{
			this.excelCharset = temp;
		}
		temp = (String) this.getAttribute("readerManagerName");
		if (temp != null)
		{
			this.readerManager = model.getFactory().getReaderManager(temp);
			if (this.readerManager == null)
			{
				throw new EternaException("Not found the reader manager [" + temp + "].");
			}
			if (this.needRowIndex)
			{
				this.readerManager = this.readerManager.copy("withRowIndex");
				this.readerManager.setReaderOrder("rowIndex");
				this.readerManager.addReader(ResultReaders.createReader("int", "rowIndex"));
				this.readerManager.lock();
			}
			List tmpList = this.readerManager.getReaderList();
			this.needTrim = new boolean[tmpList.size()];
			this.readers = new ResultReader[tmpList.size()];
			Iterator itr = tmpList.iterator();
			for (int i = 0; i < this.readers.length; i++)
			{
				ResultReader reader = (ResultReader) itr.next();
				if (!reader.isUseColumnIndex() && !this.needRowIndex && i == 0)
				{
					throw new EternaException(
							"In the reader manager [" + temp + "], all reader must set colIndex.");
				}
				this.readers[i] = reader;
				this.needTrim[i] = true;
				if (TypeManager.isTypeString(reader.getType()))
				{
					if ("false".equals(reader.getAttribute("read.trim")))
					{
						this.needTrim[i] = false;
					}
				}
			}
		}
		else
		{
			throw new EternaException("At ReadExcelExecute you must set readerManagerName attribute.");
		}
	}

	public String getExecuteType()
			throws EternaException
	{
		return "readExcel";
	}

	public ModelExport execute(AppData data, Connection conn)
			throws EternaException, SQLException, IOException
	{
		Object obj = data.caches[this.cacheIndex];
		if (obj == null)
		{
			throw new EternaException("Not found the stream in cache:" + this.cacheIndex + ".");
		}
		InputStream in = null;
		if (obj instanceof InputStream)
		{
			in = (InputStream) obj;
		}
		else if (obj instanceof FileItem)
		{
			in = ((FileItem) obj).getInputStream();
		}
		else
		{
			throw new EternaException("Error stream type " + obj.getClass() + ".");
		}

		try
		{
			Workbook book;
			if (this.excelCharset == null)
			{
				book = Workbook.getWorkbook(in);
			}
			else
			{
				WorkbookSettings ws = new WorkbookSettings();
				ws.setEncoding(this.excelCharset);
				book = Workbook.getWorkbook(in, ws);
			}
			Sheet sheet = book.getSheet(this.sheetIndex);
			self.micromagic.util.CustomResultIterator eri
					= new self.micromagic.util.CustomResultIterator(this.readerManager, null);
			self.micromagic.util.CustomResultIterator errorList = null;
			int errorCount = 0;
			Map errorMap = null;
			for (int i = this.titleRowCount; i < sheet.getRows(); i++)
			{
				Cell[] row = sheet.getRow(i);
				Object[] values = new Object[this.readers.length];
				boolean allEmpty = true;
				boolean hasError = false;
				for (int j = 0; j < this.readers.length; j++)
				{
					if (this.needRowIndex && j == 0)
					{
						values[0] = new Integer(i + 1);
					}
					else if (row.length > this.readers[j].getColumnIndex() - 1)
					{
						Cell cell = row[this.readers[j].getColumnIndex() - 1];
						CellType ct = cell.getType();
						Object valueObj = null;
						Format tmpF = null;
						if (this.needTrim[j])
						{
							// 需要trim时, 可能不是字符串，需要按类型读取
							if (ct == CellType.DATE || ct == CellType.DATE_FORMULA)
							{
								DateCell tmpCell = (DateCell) cell;
								valueObj = tmpCell.getDate();
								tmpF = tmpCell.getDateFormat();
							}
							else if (ct == CellType.NUMBER || ct == CellType.NUMBER_FORMULA)
							{
								NumberCell tmpCell = (NumberCell) cell;
								valueObj = new Double(tmpCell.getValue());
								tmpF = tmpCell.getNumberFormat();
							}
							else if (ct == CellType.BOOLEAN || ct == CellType.BOOLEAN_FORMULA)
							{
								BooleanCell tmpCell = (BooleanCell) cell;
								valueObj = tmpCell.getValue() ? Boolean.TRUE : Boolean.FALSE;
							}
						}
						if (valueObj == null)
						{
							String str = cell.getContents();
							String strTrim = str.trim();
							valueObj = this.needTrim[j] ? strTrim.length() == 0 ? null : strTrim : str;
						}

						if (valueObj != null)
						{
							try
							{
								if (tmpF != null && TypeManager.isTypeString(this.readers[j].getType()))
								{
									values[j] = tmpF.format(valueObj);
								}
								else
								{
									values[j] = this.needTrim[j] ?
											this.readers[j].readObject(valueObj) : valueObj;
								}
							}
							catch (Exception ex)
							{
								values[j] = cell.getContents();
								if (errorList == null)
								{
									errorList = new self.micromagic.util.CustomResultIterator(this.readerManager, null);
									errorMap = new HashMap();
								}
								hasError = true;
								errorMap.put(errorCount + ":" + this.readers[j].getName(), ex.getMessage());
							}
							if (valueObj instanceof String)
							{
								if (((String) valueObj).trim().length() > 0)
								{
									allEmpty = false;
								}
							}
							else
							{
								allEmpty = false;
							}
						}
					}
				}
				if (!allEmpty || !this.skipEmptyRow)
				{
					eri.createRow(values);;
					if (hasError)
					{
						errorList.createRow(values);
						errorCount++;
					}
				}
			}
			eri.finishCreateRow();
			data.push(eri);
			if (errorList != null)
			{
				errorList.finishCreateRow();
				data.dataMap.put(this.errorRows_name, errorList);
				data.dataMap.put(this.errorRowFlags_name, errorMap);
			}
			book.close();
		}
		catch (BiffException ex)
		{
			throw new EternaException(ex);
		}
		if (in != null)
		{
			in.close();
		}
		return null;
	}

	static
	{
		setUnicodeEncoding();
	}

	/**
	 * 设置jxl的unicode.encoding, 用于解决某些jdk下乱码的问题
	 */
	public static void setUnicodeEncoding()
	{
		String encoding = Utility.getProperty("jxl.unicode.encoding");
		if (encoding != null)
		{
			StringHelper.UNICODE_ENCODING = encoding;
			return;
		}
		/*
		不读取sun.io.unicode.encoding的设置，这个值不一定正确
		encoding = System.getProperty("sun.io.unicode.encoding");
		if (encoding != null)
		{
			StringHelper.UNICODE_ENCODING = encoding;
		}
		*/
	}

}