/*
 * Copyright 2015 xinjunli (micromagic@sina.com).
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

package self.micromagic.eterna.dao.reader;

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.logging.Log;

import self.micromagic.eterna.dao.ResultFormat;
import self.micromagic.eterna.dao.ResultReader;
import self.micromagic.eterna.dao.impl.FormatGenerator;
import self.micromagic.eterna.dao.impl.ScriptParser;
import self.micromagic.eterna.security.PermissionSet;
import self.micromagic.eterna.security.PermissionSetHolder;
import self.micromagic.eterna.share.AttributeManager;
import self.micromagic.eterna.share.EternaException;
import self.micromagic.eterna.share.EternaFactory;
import self.micromagic.eterna.share.Tool;
import self.micromagic.eterna.share.TypeManager;
import self.micromagic.util.StringTool;
import self.micromagic.util.converter.BooleanConverter;
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

	/**
	 * reader的状态位, 是否已初始化.
	 */
	protected static final int RS_INITIALIZED = 0x1;
	/**
	 * reader的状态位, 是否使用索引值, false则使用列名.
	 */
	protected static final int RS_USE_INDEX_OR_ALIAS = 0x10;

	protected String name;
	private String caption;
	protected String columnName;
	protected String orderCol;

	protected ResultFormat format;
	protected PermissionSet permissionSet;

	protected String alias;
	protected String realAlias;
	protected int columnIndex = -1;
	protected boolean checkIndex;

	// 当前reader的状态, 是否初始化及是否使用列索引
	protected int readerStatus;

	protected ValueConverter converter;
	// reader的attr会在创建时被替换掉, 所以这里不创建, 使用默认的空attr
	protected AttributeManager attributes = EMPTY_ATTRIBUTES;

	public ObjectReader(String name)
	{
		this.name = this.realAlias = this.alias = name;
	}

	public void initialize(EternaFactory factory)
			throws EternaException
	{
		if ((this.readerStatus & RS_INITIALIZED) != 0)
		{
			return;
		}
		this.readerStatus |= RS_INITIALIZED;
		if (this.columnName == null)
		{
			this.columnName = this.realAlias == null ? this.name : this.realAlias;
		}
		this.columnName = ScriptParser.checkNameWithKey(this.columnName);
		if (this.orderCol != null)
		{
			this.orderCol = ScriptParser.checkNameWithKey(this.orderCol);
		}
		ResultFormat f = this.format;
		String formatName = f instanceof ResultFormatHolder ? f.getName() : null;
		if (formatName != null)
		{
			String checkStr = Tool.PATTERN_PREFIX;
			if (formatName.startsWith(checkStr))
			{
				this.format = FormatGenerator.createFormat(this.getType(),
						formatName.substring(checkStr.length()), this, factory);
			}
			else
			{
				this.format = factory.getFormat(formatName);
			}
		}

		this.permissionSet = PermissionSetHolder.getRealPermissionSet(
				factory, this.permissionSet);
		if (this.caption == null)
		{
			this.caption = Tool.translateCaption(factory, this.getName());
		}
		if (this.columnIndex == -1)
		{
			Object disableFlag = factory.getAttribute(DISABLE_CHECK_INDEX_FLAG);
			this.checkIndex = !BooleanConverter.toBoolean(disableFlag);
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
		this.attributes.convertType(factory, "reader");
	}

	public int getType()
	{
		return TypeManager.TYPE_OBJECT;
	}

	public ResultFormat getFormat()
	{
		return this.format;
	}

	public void setFormatName(String format)
	{
		this.format = format == null ? null : new ResultFormatHolder(format);
	}

	public String getFormatName()
	{
		ResultFormat f = this.format;
		return f == null ? null : f.getName();
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

	public String getOrderCol()
	{
		return this.orderCol == null ? this.getColumnName() : this.orderCol;
	}

	public void setOrderCol(String name)
	{
		this.orderCol = name;
	}

	public String getAlias()
	{
		return this.alias;
	}

	public void setAlias(String alias)
	{
		this.realAlias = this.alias = alias;
		this.columnIndex = -1;
		this.readerStatus &= ~RS_USE_INDEX_OR_ALIAS;
	}

	public String getRealAlias()
	{
		return this.realAlias;
	}

	public void setRealAlias(String alias)
	{
		this.realAlias = alias == null ? this.alias : alias;
	}

	public boolean isUseAlias()
	{
		return (this.readerStatus & RS_USE_INDEX_OR_ALIAS) == 0;
	}

	public int getColumnIndex()
	{
		return this.columnIndex;
	}

	public void setColumnIndex(int columnIndex)
	{
		this.realAlias = this.alias = null;
		this.columnIndex = columnIndex;
		this.readerStatus |= RS_USE_INDEX_OR_ALIAS;
	}

	public boolean isUseColumnIndex()
	{
		return (this.readerStatus & RS_USE_INDEX_OR_ALIAS) != 0;
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
		this.permissionSet = StringTool.isEmpty(permission) ? null
				: new PermissionSetHolder(permission);
	}

	public void setPermissionSet(PermissionSet permissionSet)
	{
		this.permissionSet = permissionSet;
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
		this.attributes = attributes == null ? EMPTY_ATTRIBUTES : attributes;
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
		return this.isUseColumnIndex() || this.transIndex(rs) ?
				rs.getObject(this.columnIndex) : rs.getObject(this.realAlias);
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
				this.columnIndex = rs.findColumn(this.realAlias);
				this.readerStatus |= RS_USE_INDEX_OR_ALIAS;
				return true;
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
