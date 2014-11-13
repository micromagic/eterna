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

package self.micromagic.eterna.view.impl;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import self.micromagic.eterna.digester.ConfigurationException;
import self.micromagic.eterna.model.AppData;
import self.micromagic.eterna.share.EternaFactory;
import self.micromagic.eterna.share.OrderManager;
import self.micromagic.eterna.share.Tool;
import self.micromagic.eterna.sql.QueryAdapter;
import self.micromagic.eterna.sql.ResultReaderManager;
import self.micromagic.eterna.view.BaseManager;
import self.micromagic.eterna.view.Component;
import self.micromagic.eterna.view.TableList;
import self.micromagic.eterna.view.TableListGenerator;
import self.micromagic.eterna.view.ViewAdapter;
import self.micromagic.eterna.view.ViewAdapterGenerator;
import self.micromagic.util.container.PreFetchIterator;

/**
 * @author micromagic@sina.com
 */
public class TableListImpl extends AbstractTable
		implements TableList, TableListGenerator
{
	private ViewAdapterGenerator.ModifiableViewRes viewRes = null;

	private String columnOrder = null;
	private List columns = new LinkedList();
	private Map columnMap = new HashMap();

	private Column typicalColumn = null;

	protected void initSubs(EternaFactory factory)
			throws ConfigurationException
	{
		Iterator columnItr = this.getColumns();
		while (columnItr.hasNext())
		{
			Column cell = (Column) columnItr.next();
			cell.initialize(factory, this);
		}
		if (this.typicalColumn != null)
		{
			this.typicalColumn.initialize(factory, this);
		}

		String trName = (String) factory.getAttribute(TR.DEFAULT_TABLELIST_TR_ATTRIBUTE);
		if (trName != null && trName.length() > 0 && this.getTR() == null)
		{
			this.setTR(new TR(TableList.TR_NAME_PERFIX));
		}
	}

	protected void initBaseManager(String baseManagerName, EternaFactory factory)
			throws ConfigurationException
	{
		ResultReaderManager readerManager = null;
		if (baseManagerName.startsWith("reader:"))
		{
			readerManager = factory.getReaderManager(baseManagerName.substring(7));
		}
		else
		{
			String queryName;
			if (baseManagerName.startsWith("query:"))
			{
				queryName = baseManagerName.substring(6);
			}
			else
			{
				queryName = baseManagerName;
			}
			QueryAdapter query = factory.createQueryAdapter(queryName);
			readerManager = query.getReaderManager();
		}
		if (readerManager != null)
		{
			this.baseManager = new BaseManager(factory);
			this.baseManager.setItems(readerManager);
		}
		if (this.baseManager == null)
		{
			log.info("The base manager [" + baseManagerName + "] not found.");
		}

		if (this.autoArrange && this.baseManager != null)
		{
			Iterator itr = this.baseManager.getItems().iterator();
			List temp = new LinkedList();
			while (itr.hasNext())
			{
				Object obj = itr.next();
				ColumnImpl column = this.createColumnByItem((BaseManager.Item) obj, factory);
				column.setFactory(factory);
				column.initialize(factory, this);
				temp.add(column);
			}

			OrderManager om = new OrderManager();
			List resultList = om.getOrder(new InitColumnOrderItem(), new List[]{temp}, "$parent",
					this.columns, this.columnMap);
			this.columns = resultList;
			if (this.columnOrder != null)
			{
				resultList = om.getOrder(new InitColumnOrderItem(), null, this.columnOrder,
						this.columns, this.columnMap);
				this.columns = resultList;
			}
		}
	}

	protected void fillSubs(EternaFactory factory)
			throws ConfigurationException
	{
		ListIterator itr = this.columns.listIterator();
		while (itr.hasNext())
		{
			Column tmp = (Column) itr.next();
			if (tmp.getParent() != this)
			{
				ColumnImpl newColumn = new ColumnImpl();
				newColumn.setName(tmp.getName());
				this.fillEmptyAttr(newColumn, tmp, tmp.getTypicalComponent());
				newColumn.setIgnoreGlobalParam(tmp.isIgnoreGlobalContainerParam());
				newColumn.setIgnoreGlobalTitleParam(tmp.isIgnoreGlobalTitleParam());
				newColumn.setFactory(factory);
				newColumn.initialize(factory, this);
				itr.set(newColumn);
				this.columnMap.put(newColumn.getName(), newColumn);
				tmp = newColumn;
			}
			boolean settedTypical = false;
			if (this.typicalColumn != null)
			{
				if (!tmp.isIgnoreGlobalParam() && tmp instanceof ColumnImpl)
				{
					this.fillEmptyAttr((ColumnImpl) tmp, this.typicalColumn, this.typicalColumn.getTypicalComponent());
					settedTypical = true;
				}
			}
			if (!settedTypical && tmp instanceof ColumnGenerator)
			{
				ColumnGenerator col = (ColumnGenerator) tmp;
				col.setBeforeInit(ViewTool.addParentScript(tmp.getBeforeInit(), null));
				col.setInitScript(ViewTool.addParentScript(tmp.getInitScript(), null));
			}

			if (tmp.getCaption() == null && tmp instanceof ColumnGenerator)
			{
				String caption = Tool.translateCaption(factory, tmp.getName());
				if (caption != null)
				{
					((ColumnGenerator) tmp).setCaption(caption);
				}
				else
				{
					((ColumnGenerator) tmp).setCaption(tmp.getName());
				}
			}
		}
	}

	private ColumnImpl createColumnByItem(BaseManager.Item item, EternaFactory factory)
			throws ConfigurationException
	{
		ColumnImpl column = new ColumnImpl();
		this.fillSubByItem(column, item);
		column.setWidth(item.getWidth());

		Column tempColumn = (Column) this.columnMap.get(item.getName());
		if (tempColumn != null && tempColumn instanceof ColumnImpl)
		{
			ColumnImpl tempColumnImpl = (ColumnImpl) tempColumn;
			Component typicalComponent = null;
			if (column.typicalComponentName != null)
			{
				typicalComponent = factory.getTypicalComponent(column.typicalComponentName);
			}
			this.fillEmptyAttr(tempColumnImpl, column, typicalComponent);
		}

		return column;
	}

	private void fillEmptyAttr(ColumnImpl nowColumn, Column typical, Component typicalComponent)
			throws ConfigurationException
	{
		if (nowColumn.getWidth() == -1 && typical.getWidth() != -1)
		{
			nowColumn.setWidth(typical.getWidth());
		}
		this.fillEmptySubAttr(nowColumn, new ColumnSub(typical), typicalComponent,
				nowColumn.defaultBeforeInit);
		nowColumn.defaultBeforeInit = ColumnImpl.DEFAULT_BINIT.equals(nowColumn.getBeforeInit());
	}

	public void printSpecialBody(Writer out, AppData data, ViewAdapter view)
			throws IOException, ConfigurationException
	{
		super.printSpecialBody(out, data, view);

		out.write(",columns:[");
		Iterator columnItr = this.getColumns();
		boolean hasColumn = false;
		while (columnItr.hasNext())
		{
			if (hasColumn)
			{
				out.write(',');
			}
			else
			{
				hasColumn = true;
			}
			TableList.Column column = (TableList.Column) columnItr.next();
			column.print(out, data, view);
		}
		out.write(']');
	}

	public String getType()
	{
		return "tableList";
	}

	public void setType(String type) {}

	public void setColumnOrder(String order)
	{
		this.columnOrder = order;
	}

	public Iterator getColumns()
	{
		return new PreFetchIterator(this.columns.iterator(), false);
	}

	public void addColumn(Column column)
			throws ConfigurationException
	{
		if (ViewTool.TYPICAL_NAME.equals(column.getName()))
		{
			this.typicalColumn = column;
		}
		else
		{
			Object tmp = this.columnMap.put(column.getName(), column);
			if (tmp != null)
			{
				throw new ConfigurationException("The Column [" + column.getName()
						+ "] allready in this TableList [" + this.getName() + "].");
			}
			this.columns.add(column);
		}
	}

	public void deleteColumn(Column column)
			throws ConfigurationException
	{
		this.columnMap.remove(column.getName());
		this.columns.remove(column);
	}

	public void clearColumns()
	{
		this.columnMap.clear();
		this.columns.clear();
	}

	protected ViewAdapterGenerator.ModifiableViewRes getModifiableViewRes()
			throws ConfigurationException
	{
		if (this.viewRes == null)
		{
			this.viewRes = super.getModifiableViewRes();
			Iterator columnItr = this.getColumns();
			while (columnItr.hasNext())
			{
				Column column = (Column) columnItr.next();
				this.viewRes.addAll(column.getViewRes());
			}
			if (this.typicalColumn != null)
			{
				this.viewRes.addAll(this.typicalColumn.getViewRes());
			}
		}
		return this.viewRes;
	}

	public Component createComponent()
	{
		return this.createTableList();
	}

	public TableList createTableList()
	{
		return this;
	}

	public static class ColumnImpl extends Sub
			implements Column, ColumnGenerator
	{
		private static final String DEFAULT_BINIT = "/*DBI*/ if(typeof $E.D[eg_temp.dataName].rowCount==\"number\"){checkResult=false;checkResult=$E.D[eg_temp.dataName].names[eg_temp.srcName]!=null;}";
		private int width = -1;
		private boolean defaultBeforeInit = false;

		public void initialize(EternaFactory factory, Component parent)
				throws ConfigurationException
		{
			if (this.initialized)
			{
				return;
			}
			super.initialize(factory, parent);
			if (this.beforeInit == null)
			{
				this.defaultBeforeInit = true;
				this.beforeInit = DEFAULT_BINIT;
			}
		}

		protected void printSpecialTitle(Writer out, AppData data)
		{
		}

		protected void printSpecialContainer(Writer out, AppData data)
		{
		}

		protected void printSpecialElse(Writer out, AppData data)
				throws IOException, ConfigurationException
		{
			if (this.getWidth() != -1)
			{
				out.write(",width:");
				out.write(String.valueOf(this.getWidth()));
			}
			if (this.defaultBeforeInit)
			{
				out.write(",DBI:1");
			}
		}

		public String getType()
		{
			return "tableListColumn";
		}

		public void setType(String type) {}

		public String getDataName()
				throws ConfigurationException
		{
			return this.isOtherData() ? this.otherDataName : this.getTableList().getDataName();
		}

		private TableList getTableList()
		{
			return (TableList) this.parent;
		}

		public int getWidth()
		{
			return this.width;
		}

		public void setWidth(int width)
		{
			this.width = width;
		}

		public Component createComponent()
		{
			return this.createColumn();
		}

		public TableList.Column createColumn()
		{
			return this;
		}

	}

	private static class InitColumnOrderItem extends OrderManager.OrderItem
	{
		private TableList.Column column;

		public InitColumnOrderItem()
		{
			super("", null);
		}

		protected InitColumnOrderItem(String name, Object obj)
		{
			super(name, obj);
			this.column = (TableList.Column) obj;
		}

		public boolean isIgnore()
				throws ConfigurationException
		{
			return this.column.isIgnore();
		}

		public OrderManager.OrderItem create(Object obj)
				throws ConfigurationException
		{
			if (obj == null)
			{
				return null;
			}
			TableList.Column column = (TableList.Column) obj;
			return new InitColumnOrderItem(column.getName(), column);
		}

		public Iterator getOrderItemIterator(Object container)
				throws ConfigurationException
		{
			if (container instanceof List)
			{
				List temp = (List) container;
				return temp.iterator();
			}
			else
			{
				TableList temp = (TableList) container;
				return temp.getColumns();
			}
		}

	}

}