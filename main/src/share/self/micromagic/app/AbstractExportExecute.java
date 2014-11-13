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
import java.io.OutputStream;
import java.io.IOException;
import java.io.Writer;

import javax.servlet.http.HttpServletRequest;

import self.micromagic.eterna.model.ModelAdapter;
import self.micromagic.eterna.model.Execute;
import self.micromagic.eterna.model.AppData;
import self.micromagic.eterna.model.impl.AbstractExecute;
import self.micromagic.eterna.digester.ConfigurationException;
import self.micromagic.eterna.share.Generator;
import self.micromagic.eterna.sql.ResultIterator;
import self.micromagic.eterna.sql.QueryAdapter;
import self.micromagic.eterna.sql.ResultReaderManager;
import self.micromagic.eterna.sql.impl.ResultReaders;
import self.micromagic.eterna.search.SearchAdapter;
import self.micromagic.util.Utils;

public abstract class AbstractExportExecute extends AbstractExecute
		implements Execute, Generator
{
	/**
	 * 标识是否是使用数据集中的指定值来作为文件名.
	 */
	public static final String DATA_FILE_NAME_PREFIX = "$data.";

	/**
	 * 在arrtibute中设置导出时不包含的列.
	 */
	public static final String PRINT_EXCLUDE = "print.notPrint";

	/**
	 * 在arrtibute中设置导出时列的宽度.
	 */
	public static final String PRINT_WIDTH = "print.width";

	/**
	 * 在arrtibute中设置导出时使用的格式化方式.
	 */
	public static final String PRINT_FORMAT = "print.format";

	/**
	 * 在arrtibute中设置导出时对应列的类型, 只有在设置了
	 * <code>PRINT_FORMAT</code>是才需要设置.
	 */
	public static final String PRINT_COLTYPE = "print.colType";

	/**
	 * 在arrtibute中设置导出时使用的标题.
	 */
	public static final String PRINT_CAPTION = ResultReaders.PRINT_CAPTION;


	public static final String DOWNLOAD_CONTENTTYPE = "download.contentType";
	public static final String DOWNLOAD_FILENAME = "download.fileName";
	public static final String DOWNLOAD_STREAM = "download.stream";

	public static final int PRINT_TYPE_NUMBER = 1;
	public static final int PRINT_TYPE_DATE = 2;

	protected boolean holdConnection = true;
	protected int queryCacheIndex = 5;
	protected ResultReaderManager otherReaderManager = null;
	protected String fileName = "export";

	/**
	 * 导出文本形式的文件时使用的编码格式.
	 */
	protected String encodeName = "UTF-8";

	/**
	 * 导出的文件名使用的编码格式.
	 */
	protected String fileNameEncode = "UTF-8";

	/**
	 * 是否需要将导出的数据保存下来.
	 */
	protected boolean saveExport;

	/**
	 * 导出完毕后是否需要关闭输出流.
	 */
	protected boolean needClose;

	public void initialize(ModelAdapter model)
			throws ConfigurationException
	{
		if (this.initialized)
		{
			return;
		}
		super.initialize(model);

		String temp = (String) this.getAttribute("fileName");
		if (temp != null)
		{
			this.fileName = temp;
		}
		temp = (String) this.getAttribute("encodeName");
		if (temp != null)
		{
			this.encodeName = temp;
		}
		temp = (String) this.getAttribute("fileNameEncode");
		if (temp != null)
		{
			this.fileNameEncode = temp;
		}
		temp = (String) this.getAttribute("saveExport");
		if (temp != null)
		{
			this.saveExport = "true".equalsIgnoreCase(temp);
		}
		temp = (String) this.getAttribute("needClose");
		if (temp != null)
		{
			this.needClose = "true".equalsIgnoreCase(temp);
		}
		temp = (String) this.getAttribute("queryCacheIndex");
		if (temp != null)
		{
			this.queryCacheIndex = Utils.parseInt(temp, 5);
		}
		temp = (String) this.getAttribute("otherReaderManager");
		if (temp != null)
		{
			this.otherReaderManager = model.getFactory().getReaderManager(temp);
			if (this.otherReaderManager == null)
			{
				log.error("Not found reader manager [" + temp + "].");
			}
		}
		this.holdConnection = model.getTransactionType() ==  ModelAdapter.T_HOLD;
	}

	/**
	 * 获取文件名称模块的参数.
	 */
	public static String getFileNameParam(AppData data, String fileName, String charset)
	{
		boolean firefox = false;
		HttpServletRequest request = data.getHttpServletRequest();
		if (request != null)
		{
			String agent = request.getHeader("User-Agent");
			firefox = agent.indexOf("Firefox") != -1;
		}
		if (firefox)
		{
			return "filename*=" + charset + "''" + Utils.dealString2URL(fileName, charset);
		}
		else
		{
			return "filename=" + Utils.dealString2URL(fileName, charset);
		}
	}

	/**
	 * 关闭输出流.
	 */
	protected void closeOutput(Object out)
			throws IOException
	{
		if (out != null)
		{
			if (this.needClose)
			{
				if (out instanceof OutputStream)
				{
					((OutputStream) out).close();
				}
				else if (out instanceof Writer)
				{
					((Writer) out).close();
				}
			}
			else
			{
				if (out instanceof OutputStream)
				{
					((OutputStream) out).flush();
				}
				else if (out instanceof Writer)
				{
					((Writer) out).flush();
				}
			}
		}
	}

	protected String getFileName(AppData data)
	{
		if (this.fileName.startsWith(DATA_FILE_NAME_PREFIX))
		{
			return (String) data.dataMap.get(this.fileName.substring(DATA_FILE_NAME_PREFIX.length()));
		}
		return this.fileName;
	}

	protected ResultIterator getResultIterator(AppData data, Connection conn)
			throws ConfigurationException, SQLException
	{
		Object obj = data.caches[this.queryCacheIndex];
		if (obj == null)
		{
			throw new ConfigurationException("There is no value in cache:" + this.queryCacheIndex + ".");
		}
		if (obj instanceof QueryAdapter)
		{
			QueryAdapter query = (QueryAdapter) obj;
			if (this.otherReaderManager != null)
			{
				query.setReaderManager(this.otherReaderManager);
			}
			return this.holdConnection ? query.executeQueryHoldConnection(conn) : query.executeQuery(conn);
		}
		if (obj instanceof ResultIterator)
		{
			return (ResultIterator) obj;
		}
		if (obj instanceof SearchAdapter.Result)
		{
			return ((SearchAdapter.Result) obj).queryResult;
		}
		throw new ConfigurationException("Error value type [" + obj.getClass() + "] in cache:"
				+ this.queryCacheIndex + ".");
	}

}