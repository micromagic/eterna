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

package self.micromagic.eterna.base.reader;

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.logging.Log;

import self.micromagic.eterna.base.ResultFormat;
import self.micromagic.eterna.base.ResultReader;
import self.micromagic.eterna.base.impl.FormatGenerator;
import self.micromagic.eterna.security.PermissionSet;
import self.micromagic.eterna.share.AttributeManager;
import self.micromagic.eterna.share.EternaException;
import self.micromagic.eterna.share.EternaFactory;
import self.micromagic.eterna.share.Tool;
import self.micromagic.eterna.share.TypeManager;
import self.micromagic.util.StringTool;
import self.micromagic.util.converter.ValueConverter;

public class ObjectReader
		implements ResultReader, Cloneable
{
	/**
	 * 在arrtibute中设置导出时使用的标题.
	 */
	public static final String PRINT_CAPTION = "print.caption";

	/**
	 * ResultReader默认的空属性集.
	 */
	protected static final AttributeManager EMPTY_ATTRIBUTES = new AttributeManager();

	protected String name;
	protected String columnName;
	protected String formatName;
	protected ResultFormat format;
	private String caption;

	protected PermissionSet permissionSet;

	protected boolean useIndexOrAlias;
	protected String alias;
	protected int columnIndex = -1;
	protected boolean checkIndex;
	protected boolean initialized;

	protected ValueConverter converter;
	protected AttributeManager attributes = EMPTY_ATTRIBUTES;

	public int getType()
	{
		return TypeManager.TYPE_OBJECT;
	}

	public ObjectReader(String name)
	{
		this.name = this.alias = name;
	}

	public void initialize(EternaFactory factory)
			throws EternaException
	{
		if (this.initialized)
		{
			return;
		}
		this.initialized = true;
		if (this.columnName == null)
		{
			this.columnName = this.alias == null ? this.name : this.alias;
		}
		if (this.formatName != null)
		{
			String checkStr = "pattern:";
			if (this.formatName.startsWith(checkStr))
			{
				this.format = FormatGenerator.createFormat(
						this.getType(), this.formatName.substring(checkStr.length()));
			}
			else
			{
				this.format = factory.getFormat(this.formatName);
				if (this.format == null)
				{
					log.warn("The format [" + this.formatName + "] not found.");
				}
			}
		}
		if (this.permissionSet != null)
		{
			this.permissionSet.initialize(factory);
		}
		if (this.caption == null)
		{
			this.caption = Tool.translateCaption(factory, this.getName());
		}
		if (this.columnIndex == -1)
		{
			this.checkIndex = true;
		}
		// 检查有没有设置导出时使用的标题, 如果设置了则要尝试翻译
		String tmpStr = (String) this.getAttribute(PRINT_CAPTION);
		if (tmpStr != null)
		{
			String pCaption = Tool.translateCaption(factory, tmpStr);
			if (pCaption != null)
			{
				this.attributes.setAttribute(PRINT_CAPTION, pCaption);
			}
		}
	}

	public boolean isIgnore()
	{
		return this.getType() == TypeManager.TYPE_NULL;
	}

	public ResultFormat getFormat()
	{
		return this.format;
	}

	public void setFormatName(String format)
	{
		this.formatName = format;
	}

	public String getFormatName()
	{
		return this.formatName;
	}

	public String getName()
	{
		return this.name;
	}

	public String getColumnName()
	{
		return this.columnName;
	}

	public void setColumnName(String name)
	{
		this.columnName = name;
	}

	public String getAlias()
	{
		return this.alias;
	}

	public void setAlias(String alias)
	{
		this.alias = alias;
		this.columnIndex = -1;
		this.useIndexOrAlias = false;
	}

	public boolean isUseAlias()
	{
		return !this.useIndexOrAlias;
	}

	public int getColumnIndex()
	{
		return this.columnIndex;
	}

	public void setColumnIndex(int columnIndex)
	{
		this.alias = null;
		this.columnIndex = columnIndex;
		this.useIndexOrAlias = true;
	}

	public boolean isUseColumnIndex()
	{
		return this.useIndexOrAlias;
	}

	/**
	 * 设置在读取时是否需要获取列的索引值, 之后对数据的读取都是通过这个列的索引值.
	 */
	public void setCheckIndex(boolean checkIndex)
	{
		this.checkIndex = checkIndex;
	}

	/**
	 * 在以列名方式读取数据时, 是否要获取此列的索引值.
	 */
	public boolean isCheckIndex()
	{
		return this.checkIndex;
	}

	public boolean isValid()
	{
		return true;
	}

	public void setPermission(String permission)
	{
		if (permission == null || permission.trim().length() == 0)
		{
			return;
		}
		this.permissionSet = new PermissionSet(
				StringTool.separateString(permission, ",", true));
	}

	public PermissionSet getPermissionSet()
			throws EternaException
	{
		return this.permissionSet;
	}

	public String getCaption()
	{
		return this.caption;
	}

	public String getFilledCaption()
	{
		if (this.caption == null)
		{
			return this.isUseColumnIndex() ?
					"col_" + this.getColumnIndex() : this.getAlias();
		}
		return this.caption;
	}

	public void setCaption(String caption)
	{
		this.caption = caption;
	}

	public void setAttribute(String name, Object value)
	{
		if (this.attributes == EMPTY_ATTRIBUTES)
		{
			this.attributes = new AttributeManager();
		}
		this.attributes.setAttribute(name, value);
	}

	public void setAttributes(AttributeManager attributes)
	{
		this.attributes = attributes;
	}

	public Object getAttribute(String name)
	{
		return this.attributes.getAttribute(name);
	}

	public String[] getAttributeNames()
	{
		return this.attributes.getAttributeNames();
	}

	public Object readResult(ResultSet rs)
			throws SQLException
	{
		return this.useIndexOrAlias || this.transIndex(rs) ?
				rs.getObject(this.columnIndex) : rs.getObject(this.alias);
	}

	/**
	 * 尝试将列名转换成索引值.
	 */
	protected boolean transIndex(ResultSet rs)
	{
		if (this.checkIndex)
		{
			try
			{
				this.columnIndex = rs.findColumn(this.alias);
				return this.useIndexOrAlias = true;
			}
			catch (SQLException ex)
			{
				this.checkIndex = false;
			}
		}
		return false;
	}

	public Object readCall(CallableStatement call, int index)
			throws SQLException
	{
		return call.getObject(index);
	}

	public Object readObject(Object obj)
			throws EternaException
	{
		if (obj == null)
		{
			return null;
		}
		if (obj instanceof ResultSet)
		{
			try
			{
				return this.readResult((ResultSet) obj);
			}
			catch (SQLException ex)
			{
				throw new EternaException(ex);
			}
		}
		return this.converter == null ? obj : this.converter.convert(obj);
	}

	protected static final Log log = Tool.log;

}