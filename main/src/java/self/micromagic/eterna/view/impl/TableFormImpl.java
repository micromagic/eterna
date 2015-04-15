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

import self.micromagic.eterna.share.EternaException;
import self.micromagic.eterna.model.AppData;
import self.micromagic.eterna.search.SearchAdapter;
import self.micromagic.eterna.share.EternaFactory;
import self.micromagic.eterna.share.OrderManager;
import self.micromagic.eterna.share.Tool;
import self.micromagic.eterna.sql.QueryAdapter;
import self.micromagic.eterna.sql.ResultReaderManager;
import self.micromagic.eterna.view.BaseManager;
import self.micromagic.eterna.view.Component;
import self.micromagic.eterna.view.TableForm;
import self.micromagic.eterna.view.TableFormGenerator;
import self.micromagic.eterna.view.ViewAdapter;
import self.micromagic.eterna.view.ViewAdapterGenerator;
import self.micromagic.util.container.PreFetchIterator;

/**
 * @author micromagic@sina.com
 */
public class TableFormImpl extends AbstractTable
		implements TableForm, TableFormGenerator
{
	private ViewAdapterGenerator.ModifiableViewRes viewRes = null;

	private String columns;

	private String cellOrder = null;
	private List cells = new LinkedList();
	private Map cellMap = new HashMap();

	private Cell typicalCell = null;

	protected void initSubs(EternaFactory factory)
			throws EternaException
	{
		Iterator celltr = this.getCells();
		while (celltr.hasNext())
		{
			Cell cell = (Cell) celltr.next();
			cell.initialize(factory, this);
		}
		if (this.typicalCell != null)
		{
			this.typicalCell.initialize(factory, this);
		}
	}

	protected void initBaseManager(String baseManagerName, EternaFactory factory)
			throws EternaException
	{
		ResultReaderManager readerManager = null;
		SearchAdapter search = null;
		if (baseManagerName.startsWith("reader:"))
		{
			readerManager = factory.getReaderManager(baseManagerName.substring(7));
		}
		else if (baseManagerName.startsWith("search:"))
		{
			search = factory.createSearchAdapter(baseManagerName.substring(7));
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
		if (search != null)
		{
			this.baseManager = new BaseManager(factory);
			this.baseManager.setItems(search);
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
				TableFormImpl.CellImpl cell = this.createCellByItem((BaseManager.Item) obj, factory);
				cell.setFactory(factory);
				cell.initialize(factory, this);
				temp.add(cell);
			}

			OrderManager om = new OrderManager();
			List resultList = om.getOrder(new InitCellOrderItem(), new List[]{temp}, "$parent",
					this.cells, this.cellMap);
			this.cells = resultList;
			if (this.cellOrder != null)
			{
				resultList = om.getOrder(new InitCellOrderItem(), null, this.cellOrder,
						this.cells, this.cellMap);
				this.cells = resultList;
			}
		}
	}

	protected void fillSubs(EternaFactory factory)
			throws EternaException
	{
		ListIterator itr = this.cells.listIterator();
		while (itr.hasNext())
		{
			Cell tmp = (Cell) itr.next();
			if (tmp.getParent() != this)
			{
				CellImpl newCell = new CellImpl();
				newCell.setName(tmp.getName());
				this.fillEmptyAttr(newCell, tmp, tmp.getTypicalComponent());
				newCell.setIgnoreGlobalParam(tmp.isIgnoreGlobalContainerParam());
				newCell.setIgnoreGlobalTitleParam(tmp.isIgnoreGlobalTitleParam());
				newCell.setNeedIndex(tmp.isNeedIndex());
				newCell.setNewRow(tmp.isNewRow());
				newCell.setRequired(tmp.isRequired());
				newCell.setFactory(factory);
				newCell.initialize(factory, this);
				itr.set(newCell);
				this.cellMap.put(newCell.getName(), newCell);
				tmp = newCell;
			}
			boolean settedTypical = false;
			if (this.typicalCell != null)
			{
				if (!tmp.isIgnoreGlobalParam() && tmp instanceof CellImpl)
				{
					this.fillEmptyAttr((CellImpl) tmp, this.typicalCell, this.typicalCell.getTypicalComponent());
					settedTypical = true;
				}
			}
			if (!settedTypical && tmp instanceof CellGenerator)
			{
				CellGenerator c = (CellGenerator) tmp;
				c.setBeforeInit(ViewTool.addParentScript(tmp.getBeforeInit(), null));
				c.setInitScript(ViewTool.addParentScript(tmp.getInitScript(), null));
			}

			if (tmp.getCaption() == null && tmp instanceof CellGenerator)
			{
				String caption = Tool.translateCaption(factory, tmp.getName());
				if (caption != null)
				{
					((CellGenerator) tmp).setCaption(caption);
				}
				else
				{
					((CellGenerator) tmp).setCaption(tmp.getName());
				}
			}
		}
	}

	private CellImpl createCellByItem(BaseManager.Item item, EternaFactory factory)
			throws EternaException
	{
		CellImpl cell = new CellImpl();
		this.fillSubByItem(cell, item);
		if (item.getCellSize() != null)
		{
			cell.setTitleSize(item.getCellSize()[0]);
			cell.setContainerSize(item.getCellSize()[1]);
		}
		if (item.isNewRow())
		{
			cell.setNewRow(true);
		}
		if (item.isRequired())
		{
			cell.setRequired(true);
		}

		Cell tempCell = (Cell) this.cellMap.get(item.getName());
		if (tempCell != null && tempCell instanceof CellImpl)
		{
			CellImpl tempCellImpl = (CellImpl) tempCell;
			Component typicalComponent = null;
			if (cell.typicalComponentName != null)
			{
				typicalComponent = factory.getTypicalComponent(cell.typicalComponentName);
			}
			this.fillEmptyAttr(tempCellImpl, cell, typicalComponent);
		}

		return cell;
	}

	private void fillEmptyAttr(CellImpl nowCell, Cell typical, Component typicalComponent)
			throws EternaException
	{
		if (!nowCell.sizeSetted && (typical.getTitleSize() != 1 || typical.getContainerSize() != 1))
		{
			nowCell.setTitleSize(typical.getTitleSize());
			nowCell.setContainerSize(typical.getContainerSize());
		}
		if (!nowCell.needIndexSetted && typical.isNeedIndex())
		{
			nowCell.setNeedIndex(true);
		}
		if (!nowCell.newRowSetted && typical.isNewRow())
		{
			nowCell.setNewRow(true);
		}
		if (!nowCell.requiredSetted && typical.isRequired())
		{
			nowCell.setRequired(true);
		}
		this.fillEmptySubAttr(nowCell, new CellSub(typical), typicalComponent, false);
	}

	public void printSpecialBody(Writer out, AppData data, ViewAdapter view)
			throws IOException, EternaException
	{
		super.printSpecialBody(out, data, view);

		out.write(",columns:[");
		out.write(this.columns);
		out.write(']');

		out.write(",cells:[");
		Iterator cellItr = this.getCells();
		boolean hasCell = false;
		while (cellItr.hasNext())
		{
			if (hasCell)
			{
				out.write(',');
			}
			else
			{
				hasCell = true;
			}
			Cell cell = (Cell) cellItr.next();
			cell.print(out, data, view);
		}
		out.write(']');
	}

	public String getType()
	{
		return "tableForm";
	}

	public void setType(String type) {}


	public String getColumns()
	{
		return this.columns;
	}

	public void setColumns(String columns)
	{
		this.columns = columns;
	}

	public void setCellOrder(String order)
	{
		this.cellOrder = order;
	}

	public Iterator getCells()
	{
		return new PreFetchIterator(this.cells.iterator(), false);
	}

	public void addCell(Cell cell)
			throws EternaException
	{
		if (ViewTool.TYPICAL_NAME.equals(cell.getName()))
		{
			this.typicalCell = cell;
		}
		else
		{
			Object tmp = this.cellMap.put(cell.getName(), cell);
			if (tmp != null)
			{
				throw new EternaException("The Cell [" + cell.getName()
						+ "] allready in this TableForm.");
			}
			this.cells.add(cell);
		}
	}

	public void deleteCell(Cell cell)
			throws EternaException
	{
		this.cellMap.remove(cell.getName());
		this.cells.remove(cell);
	}

	public void clearCells()
	{
		this.cellMap.clear();
		this.cells.clear();
	}

	protected ViewAdapterGenerator.ModifiableViewRes getModifiableViewRes()
			throws EternaException
	{
		if (this.viewRes == null)
		{
			this.viewRes = super.getModifiableViewRes();
			Iterator cellItr = this.getCells();
			while (cellItr.hasNext())
			{
				Cell cell = (Cell) cellItr.next();
				this.viewRes.addAll(cell.getViewRes());
			}
			if (this.typicalCell != null)
			{
				this.viewRes.addAll(this.typicalCell.getViewRes());
			}
		}
		return this.viewRes;
	}

	public Component createComponent()
	{
		return this.createTableForm();
	}

	public TableForm createTableForm()
	{
		return this;
	}

	public static class CellImpl extends Sub
			implements Cell, CellGenerator
	{
		private int titleSize = 1;
		private int containerSize = 1;
		private boolean sizeSetted = false;
		private int rowSpan = 1;
		private boolean needIndex = false;
		private boolean needIndexSetted = false;
		private boolean required;
		private boolean requiredSetted = false;
		private boolean newRow;
		private boolean newRowSetted = false;

		protected void printSpecialTitle(Writer out, AppData data)
				throws IOException, EternaException
		{
			if (this.getTitleSize() != 1)
			{
				out.write(",size:");
				out.write(String.valueOf(this.getTitleSize()));
			}
		}

		protected void printSpecialContainer(Writer out, AppData data)
				throws IOException, EternaException
		{
			if (this.getContainerSize() != 1)
			{
				out.write(",size:");
				out.write(String.valueOf(this.getContainerSize()));
			}
			if (this.isRequired())
			{
				out.write(",required:1");
			}
			if (this.isNeedIndex())
			{
				out.write(",needIndex:1");
			}
		}

		protected void printSpecialElse(Writer out, AppData data)
				throws IOException, EternaException
		{
			if (this.getRowSpan() != 1)
			{
				out.write(",rowSpan:");
				out.write(String.valueOf(this.getRowSpan()));
			}
			if (this.isNewRow())
			{
				out.write(",clearRowNum:1");
			}
		}

		public String getType()
		{
			return "tableFormCell";
		}

		public void setType(String type) {}

		public String getDataName()
				throws EternaException
		{
			return this.isOtherData() ? this.otherDataName : this.getTableForm().getDataName();
		}

		private TableForm getTableForm()
		{
			return (TableForm) this.parent;
		}

		public int getTitleSize()
		{
			return this.titleSize;
		}

		public void setTitleSize(int titleSize)
		{
			this.sizeSetted = true;
			this.titleSize = titleSize;
		}

		public int getContainerSize()
		{
			return this.containerSize;
		}

		public void setContainerSize(int containerSize)
		{
			this.sizeSetted = true;
			this.containerSize = containerSize;
		}

		public int getRowSpan()
		{
			return this.rowSpan;
		}

		public void setRowSpan(int rowSpan)
		{
			this.rowSpan = rowSpan;
		}

		public boolean isRequired()
		{
			return this.required;
		}

		public void setRequired(boolean required)
		{
			this.requiredSetted = true;
			this.required = required;
		}

		public boolean isNeedIndex()
		{
			return this.needIndex;
		}

		public void setNeedIndex(boolean needIndex)
		{
			this.needIndexSetted = true;
			this.needIndex = needIndex;
		}

		public boolean isNewRow()
		{
			this.newRowSetted = true;
			return this.newRow;
		}

		public void setNewRow(boolean newRow)
		{
			this.newRow = newRow;
		}

		public Component createComponent()
		{
			return this.createCell();
		}

		public Cell createCell()
		{
			return this;
		}

	}

	private static class InitCellOrderItem extends OrderManager.OrderItem
	{
		private Cell cell;

		public InitCellOrderItem()
		{
			super("", null);
		}

		protected InitCellOrderItem(String name, Object obj)
		{
			super(name, obj);
			this.cell = (Cell) obj;
		}

		public boolean isIgnore()
				throws EternaException
		{
			return this.cell.isIgnore();
		}

		public OrderManager.OrderItem create(Object obj)
				throws EternaException
		{
			if (obj == null)
			{
				return null;
			}
			Cell cell = (Cell) obj;
			return new InitCellOrderItem(cell.getName(), cell);
		}

		public Iterator getOrderItemIterator(Object container)
				throws EternaException
		{
			if (container instanceof List)
			{
				List temp = (List) container;
				return temp.iterator();
			}
			else
			{
				TableForm temp = (TableForm) container;
				return temp.getCells();
			}
		}

	}

}