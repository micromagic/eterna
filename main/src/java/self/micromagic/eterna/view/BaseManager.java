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

package self.micromagic.eterna.view;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import self.micromagic.eterna.digester.ConfigurationException;
import self.micromagic.eterna.search.ConditionProperty;
import self.micromagic.eterna.search.SearchAdapter;
import self.micromagic.eterna.share.EternaFactory;
import self.micromagic.eterna.sql.ResultReader;
import self.micromagic.eterna.sql.ResultReaderManager;
import self.micromagic.eterna.sql.SQLParameterGroup;
import self.micromagic.eterna.sql.impl.ResultReaders;
import self.micromagic.eterna.view.impl.ViewTool;
import self.micromagic.util.StringTool;
import self.micromagic.util.StringAppender;

public class BaseManager
{
	public static final String NEW_ROW = "newRow";
	public static final String REQUIRED = "required";
	public static final String CELL_SIZE = "cellSize";
	public static final String CONTAINER_PARAM = "containerParam";
	public static final String TITLE_PARAM = "titleParam";
	public static final String DATA_SRC = "dataSrc";
	public static final String INIT_PARAM = "initParam";
	public static final String BEFORE_INIT = "beforeInit";
	public static final String INIT_SCRIPT = "initScript";
	public static final char SPECIAL_FLAG = '.';

	public static final String ETERNA_INITIALIZED_FLAG = "eterna_initialized";

	private static Set definedNameSet = new HashSet();

	static
	{
		definedNameSet.add("value");
		definedNameSet.add(SQLParameterGroup.READER_VPC_FLAG);
		definedNameSet.add(SQLParameterGroup.READER_COLNAME_FLAG);
		definedNameSet.add(NEW_ROW);
		definedNameSet.add(REQUIRED);
		definedNameSet.add(CELL_SIZE);
		definedNameSet.add(CONTAINER_PARAM);
		definedNameSet.add(TITLE_PARAM);
		definedNameSet.add(DATA_SRC);
		definedNameSet.add(INIT_PARAM);
		definedNameSet.add(BEFORE_INIT);
		definedNameSet.add(INIT_SCRIPT);
		definedNameSet.add(ResultReader.INPUT_TYPE_FLAG);
		definedNameSet.add(ResultReaders.CHECK_INDEX_FLAG);
	}

	/**
	 * @deprecated
	 * @see ViewTool#createEternaId
	 */
	public static int createEternaId()
	{
		return ViewTool.createEternaId();
	}

	private StringCoder stringCoder;
	List items = new LinkedList();

	public BaseManager(EternaFactory factory)
			throws ConfigurationException
	{
		this.stringCoder = factory.getStringCoder();
	}

	public int getCount()
	{
		return this.items.size();
	}

	public List getItems()
	{
		return this.items;
	}

	public void setItems(ResultReaderManager readerManager)
			throws ConfigurationException
	{
		Iterator itr = readerManager.getReaderList().iterator();
		while (itr.hasNext())
		{
			ResultReader temp = (ResultReader) itr.next();
			if (temp.isVisible())
			{
				this.items.add(new ReaderItem(this.stringCoder, temp));
			}
		}
	}

	public void setItems(SearchAdapter search)
			throws ConfigurationException
	{
		int count = search.getConditionPropertyCount();
		for (int i = 0; i < count; i++)
		{
			ConditionProperty temp = search.getConditionProperty(i);
			if (temp.isVisible())
			{
				this.items.add(new PropertyItem(this.stringCoder, temp));
			}
		}
	}

	private static int[] parseCellSize(String cellSizeStr)
	{
		if (cellSizeStr != null)
		{
			int index = cellSizeStr.indexOf(',');
			try
			{
				return new int[]{
					Integer.parseInt(cellSizeStr.substring(0, index)),
					Integer.parseInt(cellSizeStr.substring(index + 1))
				};
			}
			catch (Exception ex)
			{
				ViewTool.log.warn("Error cell size string:[" + cellSizeStr + "].", ex);
				return null;
			}
		}
		return null;
	}

	public interface Item
	{
		public String getName() throws ConfigurationException;

		public String getDataSrc() throws ConfigurationException;

		String getCaption() throws ConfigurationException;

		int getWidth() throws ConfigurationException;

		int[] getCellSize() throws ConfigurationException;

		int getType() throws ConfigurationException;

		boolean isNewRow() throws ConfigurationException;

		boolean isRequired() throws ConfigurationException;

		String getInputType() throws ConfigurationException;

		String getInitParam() throws ConfigurationException;

		String getContainerParam() throws ConfigurationException;

		String getTitleParam() throws ConfigurationException;

		String getBeforeInit() throws ConfigurationException;

		String getInitScript() throws ConfigurationException;

		boolean isVisible() throws ConfigurationException;

	}

	abstract static class AbstractItem
			implements Item
	{
		protected StringCoder stringCoder;
		private String dataSrc;
		private boolean newRow;
		private boolean required;
		private String containerParam;
		private String titleParam;
		private String beforeInit;
		private String initScript;
		private int[] cellSize;
		protected String initParam;

		public AbstractItem(StringCoder stringCoder)
		{
			this.stringCoder = stringCoder;
		}

		protected void initItem()
				throws ConfigurationException
		{
			this.dataSrc = this.getAttribute(DATA_SRC);
			this.beforeInit = this.getAttribute(BEFORE_INIT);
			this.initScript = this.getAttribute(INIT_SCRIPT);
			this.containerParam = this.getAttribute(CONTAINER_PARAM);
			this.titleParam = this.getAttribute(TITLE_PARAM);
			this.cellSize = parseCellSize(this.getAttribute(CELL_SIZE));
			this.newRow = "true".equals(this.getAttribute(NEW_ROW));
			this.required = "true".equals(this.getAttribute(REQUIRED));
		}

		protected abstract String getAttribute(String name) throws ConfigurationException;

		public String getDataSrc()
				throws ConfigurationException
		{
			return this.dataSrc;
		}

		public boolean isNewRow()
				throws ConfigurationException
		{
			return this.newRow;
		}

		public boolean isRequired()
				throws ConfigurationException
		{
			return this.required;
		}

		public int[] getCellSize()
				throws ConfigurationException
		{
			return this.cellSize;
		}

		public String getInitParam()
		{
			return this.initParam;
		}

		public String getContainerParam()
		{
			return this.containerParam;
		}

		public String getTitleParam()
		{
			return this.titleParam;
		}

		public String getBeforeInit()
		{
			return this.beforeInit;
		}

		public String getInitScript()
		{
			return this.initScript;
		}

	}

	static class ReaderItem extends AbstractItem
			implements Item
	{
		private ResultReader reader;
		private String inputType;

		public ReaderItem(StringCoder stringCoder, ResultReader reader)
				throws ConfigurationException
		{
			super(stringCoder);
			this.reader = reader;

			this.initItem();
			this.inputType = (String) this.reader.getAttribute(ResultReader.INPUT_TYPE_FLAG);
			String tmp = (String) this.reader.getAttribute(INIT_PARAM);
			if (tmp != null)
			{
				this.initParam = tmp;
			}
			else
			{
				String[] names = reader.getAttributeNames();
				if (names != null && names.length > 0)
				{
					StringAppender buf = StringTool.createStringAppender();
					for (int i = 0; i < names.length; i++)
					{
						String name = names[i];
						if (name != null && name.indexOf(SPECIAL_FLAG) > 0)
						{
							// 名称中包含SPECIAL_FLAG, 且不是以SPECIAL_FLAG开始的
							// 不能作为init param的属性
							continue;
						}
						if (!definedNameSet.contains(name))
						{
							if (buf.length() > 0)
							{
								buf.append(',');
							}
							buf.append(name).append(":\"");
							buf.append(this.stringCoder.toJsonString((String) reader.getAttribute(name)));
							buf.append('"');
						}
					}
					if (buf.length() > 0)
					{
						this.initParam = StringTool.intern(buf.toString(), true);
					}
				}
			}
		}

		protected String getAttribute(String name)
				throws ConfigurationException
		{
			return (String) this.reader.getAttribute(name);
		}

		public String getName()
				throws ConfigurationException
		{
			return this.reader.getName();
		}

		public String getCaption()
				throws ConfigurationException
		{
			return this.reader.getCaption();
		}

		public int getWidth()
				throws ConfigurationException
		{
			return this.reader.getWidth();
		}

		public int getType()
				throws ConfigurationException
		{
			return this.reader.getType();
		}

		public String getInputType()
				throws ConfigurationException
		{
			return this.inputType;
		}

		public boolean isVisible()
				throws ConfigurationException
		{
			return this.reader.isVisible();
		}

	}

	class PropertyItem extends AbstractItem
			implements Item
	{
		private ConditionProperty property;

		public PropertyItem(StringCoder stringCoder, ConditionProperty property)
				throws ConfigurationException
		{
			super(stringCoder);
			this.property = property;

			this.initItem();
			String tmp = this.property.getAttribute(INIT_PARAM);
			if (tmp != null)
			{
				this.initParam = tmp;
			}
			else
			{
				String[] names = property.getAttributeNames();
				if (names != null && names.length > 0)
				{
					StringAppender buf = StringTool.createStringAppender();
					for (int i = 0; i < names.length; i++)
					{
						String name = names[i];
						if (!definedNameSet.contains(name))
						{
							if (buf.length() > 0)
							{
								buf.append(',');
							}
							buf.append(name).append(":\"");
							buf.append(this.stringCoder.toJsonString(property.getAttribute(name)));
							buf.append('"');
						}
					}
					if (buf.length() > 0)
					{
						this.initParam = StringTool.intern(buf.toString(), true);
					}
				}
			}
		}

		protected String getAttribute(String name)
				throws ConfigurationException
		{
			return this.property.getAttribute(name);
		}

		public String getName()
				throws ConfigurationException
		{
			return this.property.getName();
		}

		public String getCaption()
				throws ConfigurationException
		{
			return this.property.getColumnCaption();
		}

		public int getWidth()
		{
			return -1;
		}

		public int getType()
				throws ConfigurationException
		{
			return this.property.getColumnType();
		}

		public String getInputType()
				throws ConfigurationException
		{
			return this.property.getConditionInputType();
		}

		public boolean isVisible()
				throws ConfigurationException
		{
			return this.property.isVisible();
		}

	}

}