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
import java.util.List;
import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;
import java.util.StringTokenizer;
import java.util.Map;
import java.util.HashMap;

import self.micromagic.eterna.model.impl.AbstractExecute;
import self.micromagic.eterna.model.Execute;
import self.micromagic.eterna.model.ModelAdapter;
import self.micromagic.eterna.model.ModelExport;
import self.micromagic.eterna.model.AppData;
import self.micromagic.eterna.share.Generator;
import self.micromagic.eterna.share.EternaException;
import self.micromagic.util.Utility;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.portlet.PortletFileUpload;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;

public class UploadExecute extends AbstractExecute
		implements Execute, Generator
{
	private String charset = "UTF-8";
	private String storeNames;
	private Set namesSet = new HashSet();

	public void initialize(ModelAdapter model)
			throws EternaException
	{
		if (this.initialized)
		{
			return;
		}
		super.initialize(model);
		this.storeNames = (String) this.getAttribute("storeNames");
		if (this.storeNames == null)
		{
			this.storeNames = this.getName();
		}
		StringTokenizer st = new StringTokenizer(this.storeNames, ",");
		while (st.hasMoreTokens())
		{
			String tmp = st.nextToken().trim();
			this.namesSet.add(tmp);
		}

		String temp = (String) this.getAttribute("charset");
		if (temp != null)
		{
			this.charset = temp;
		}
		else
		{
			temp = Utility.getProperty(Utility.CHARSET_TAG);
			if (temp != null && !temp.equals(this.charset))
			{
				this.charset = temp;
			}
		}
	}

	public String getExecuteType()
			throws EternaException
	{
		return "upload";
	}

	public ModelExport execute(AppData data, Connection conn)
			throws EternaException, SQLException, IOException
	{
		try
		{
			FileItemFactory factory = new DiskFileItemFactory();
			List items = null;
			if (data.getHttpServletRequest() != null)
			{
				ServletFileUpload upload = new ServletFileUpload(factory);
				upload.setHeaderEncoding(this.charset);
				items = upload.parseRequest(data.getHttpServletRequest());
			}
			else if (data.actionRequest != null)
			{
				PortletFileUpload upload = new PortletFileUpload(factory);
				upload.setHeaderEncoding(this.charset);
				items = upload.parseRequest(data.actionRequest);
			}

			Map result = new HashMap();
			if (items != null)
			{
				Iterator iter = items.iterator();
				while (iter.hasNext())
				{
					FileItem item = (FileItem) iter.next();
					if (this.namesSet.contains(item.getFieldName()))
					{
						Object oldValue = result.get(item.getFieldName());
						Object nowValue;
						if (item.isFormField())
						{
							nowValue = item.getString(this.charset);
						}
						else
						{
							nowValue = item;
						}
						if (oldValue != null)
						{
							if (oldValue instanceof Object[])
							{
								Object[] oldArr = (String[]) oldValue;
								Object[] newArr = new String[oldArr.length + 1];
								System.arraycopy(oldArr, 0, newArr, 0, oldArr.length);
								newArr[oldArr.length] = nowValue;
								result.put(item.getFieldName(), newArr);
							}
							else
							{
								result.put(item.getFieldName(), new Object[]{oldValue, nowValue});
							}
						}
						else
						{
							result.put(item.getFieldName(), nowValue);
						}
					}
				}
			}
			data.push(result);
		}
		catch (FileUploadException ex)
		{
			log.error("Upload error.", ex);
			throw new EternaException(ex);
		}
		return null;
	}

}