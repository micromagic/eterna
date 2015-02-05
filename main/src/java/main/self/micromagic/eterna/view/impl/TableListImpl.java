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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import self.micromagic.eterna.dao.EntityItem;
import self.micromagic.eterna.dao.EntityRef;
import self.micromagic.eterna.dao.impl.EntityImpl;
import self.micromagic.eterna.model.AppData;
import self.micromagic.eterna.share.EternaException;
import self.micromagic.eterna.share.EternaFactory;
import self.micromagic.eterna.share.OrderManager;
import self.micromagic.eterna.share.Tool;
import self.micromagic.eterna.view.Component;
import self.micromagic.eterna.view.ModifiableViewRes;
import self.micromagic.eterna.view.TableList;
import self.micromagic.eterna.view.View;
import self.micromagic.util.StringTool;
import self.micromagic.util.container.PreFetchIterator;

/**
 * @author micromagic@sina.com
 */
public class TableListImpl extends AbstractTable
		implements TableList
{
	private ModifiableViewRes viewRes = null;

	private String columnOrder = null;
	private List columns = new LinkedList();
	private final Map columnMap = new HashMap();

	private Column typicalColumn = null;

	protected void initSubs(EternaFactory factory)
			throws EternaException
	{
		List tmpColumns = new ArrayList();
		Iterator itr = this.columns.iterator();
		while (itr.hasNext())
		{
			Object tmp = itr.next();
			if (tmp instanceof Column)
			{
				tmpColumns.add(tmp);
			}
			else
			{
				EntityRef ref = (EntityRef) tmp;
				ColumnContainer cc = new ColumnContainer(this, this.columnMap, tmpColumns);
				EntityImpl.addItems(factory, ref, cc);
			}
		}

		this.columns = new ArrayList(tmpColumns.size());
		this.columns.addAll(tmpColumns);
		Iterator columnItr = this.getColumns();
		while (columnItr.hasNext())
		{
			Column column = (Column) columnItr.next();
			column.initialize(factory, this);
		}
		if (this.columnOrder != null)
		{
			this.columns = OrderManager.doOrder(this.columns,
					this.columnOrder, new ColumnNameHandler());
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

	protected void fillSubs(EternaFactory factory)
			throws EternaException
	{
		ListIterator itr = this.columns.listIterator();
		while (itr.hasNext())
		{
			Column tmp = (Column) itr.next();
			boolean settedTypical = false;
			if (this.typicalColumn != null)
			{
				if (!tmp.isIgnoreGlobalParam() && tmp instanceof ColumnImpl)
				{
					this.fillEmptyAttr((ColumnImpl) tmp, this.typicalColumn, this.typicalColumn.getTypicalComponent());
					settedTypical = true;
				}
			}
			if (!settedTypical && tmp instanceof ColumnImpl)
			{
				ColumnImpl col = (ColumnImpl) tmp;
				col.setBeforeInit(ViewTool.addParentScript(tmp.getBeforeInit(), null));
				col.setInitScript(ViewTool.addParentScript(tmp.getInitScript(), null));
			}

			if (tmp.getCaption() == null && tmp instanceof ColumnImpl)
			{
				String caption = Tool.translateCaption(factory, tmp.getName());
				if (caption != null)
				{
					((ColumnImpl) tmp).setCaption(caption);
				}
				else
				{
					((ColumnImpl) tmp).setCaption(tmp.getName());
				}
			}
		}
	}

	ColumnImpl createColumnByItem(EntityItem item, EternaFactory factory)
			throws EternaException
	{
		ColumnImpl column = new ColumnImpl();
		this.fillSubByItem(column, item);
		String widthStr = (String) item.getAttribute("width");
		if (!StringTool.isEmpty(widthStr))
		{
			column.setWidth(Integer.parseInt(widthStr));
		}

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
			throws EternaException
	{
		if (nowColumn.getWidth() == -1 && typical.getWidth() != -1)
		{
			nowColumn.setWidth(typical.getWidth());
		}
		this.fillEmptySubAttr(nowColumn, new ColumnSub(typical), typicalComponent,
				nowColumn.defaultBeforeInit);
		nowColumn.defaultBeforeInit = ColumnImpl.DEFAULT_BINIT.equals(nowColumn.getBeforeInit());
	}

	public void printSpecialBody(Writer out, AppData data, View view)
			throws IOException, EternaException
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

	/**
	 * 添加一个实体的引用.
	 */
	public void addEntityRef(EntityRef ref)
			throws EternaException
	{
		if (this.initialized)
		{
			throw new EternaException("You can't invoke addEntityRef after initialized.");
		}
		this.columns.add(ref);
	}

	public void addColumn(Column column)
			throws EternaException
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
				throw new EternaException("The Column [" + column.getName()
						+ "] allready in this TableList [" + this.getName() + "].");
			}
			this.columns.add(column);
		}
	}

	public void deleteColumn(Column column)
			throws EternaException
	{
		this.columnMap.remove(column.getName());
		this.columns.remove(column);
	}

	public void clearColumns()
	{
		this.columnMap.clear();
		this.columns.clear();
	}

	protected ModifiableViewRes getModifiableViewRes()
			throws EternaException
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
			implements Column
	{
		private static final String DEFAULT_BINIT = "/*DBI*/ if(typeof $E.D[eg_temp.dataName].rowCount==\"number\"){checkResult=false;checkResult=$E.D[eg_temp.dataName].names[eg_temp.srcName]!=null;}";
		private int width = -1;
		private boolean defaultBeforeInit;
		private boolean cloneInitParam;

		public void initialize(EternaFactory factory, Component parent)
				throws EternaException
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

		public boolean getCloneInitParam()
		{
			return this.cloneInitParam;
		}

		public void setCloneInitParam(boolean clone)
		{
			this.cloneInitParam = clone;
		}

		protected void printSpecialTitle(Writer out, AppData data)
		{
		}

		protected void printSpecialContainer(Writer out, AppData data)
		{
		}

		protected void printSpecialElse(Writer out, AppData data)
				throws IOException, EternaException
		{
			if (this.getWidth() != -1)
			{
				out.write(",width:");
				out.write(String.valueOf(this.getWidth()));
			}
			if (this.getCloneInitParam())
			{
				out.write(",cloneInitParam:1");
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
				throws EternaException
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

}

/**
 * 处理EntityRef的容器.
 */
class ColumnContainer
		implements EntityImpl.Container
{
	public ColumnContainer(TableListImpl tableList, Map nameCache, List itemList)
	{
		this.tableList = tableList;
		this.nameCache = nameCache;
		this.itemList = itemList;
	}
	private final Map nameCache;
	private final List itemList;
	private final TableListImpl tableList;

	public String getName()
	{
		return this.tableList.getName();
	}

	public String getType()
	{
		return "TableList";
	}

	public boolean contains(String name)
	{
		return this.nameCache.containsKey(name);
	}

	public void add(EntityItem item, String tableAlias)
	{
		if (AbstractTable.checkVisible(item))
		{
			Object column = this.tableList.createColumnByItem(
					item, this.tableList.getFactory());
			this.itemList.add(column);
			this.nameCache.put(item.getName(), column);
		}
	}

}

class ColumnNameHandler
		implements OrderManager.NameHandler
{
	public String getName(Object obj)
	{
		return ((TableList.Column) obj).getName();
	}

}
