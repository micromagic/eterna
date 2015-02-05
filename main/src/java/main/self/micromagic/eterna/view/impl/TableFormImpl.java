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
import self.micromagic.eterna.view.TableForm;
import self.micromagic.eterna.view.View;
import self.micromagic.util.StringTool;
import self.micromagic.util.container.PreFetchIterator;
import self.micromagic.util.converter.BooleanConverter;

/**
 * @author micromagic@sina.com
 */
public class TableFormImpl extends AbstractTable
		implements TableForm
{
	private ModifiableViewRes viewRes = null;

	private String columns;

	private String cellOrder = null;
	private List cells = new LinkedList();
	private final Map cellMap = new HashMap();

	private Cell typicalCell = null;

	protected void initSubs(EternaFactory factory)
			throws EternaException
	{
		List tmpCells = new ArrayList();
		Iterator itr = this.cells.iterator();
		while (itr.hasNext())
		{
			Object tmp = itr.next();
			if (tmp instanceof Cell)
			{
				tmpCells.add(tmp);
			}
			else
			{
				EntityRef ref = (EntityRef) tmp;
				CellContainer cc = new CellContainer(this, this.cellMap, tmpCells);
				EntityImpl.addItems(factory, ref, cc);
			}
		}

		this.cells = new ArrayList(tmpCells.size());
		this.cells.addAll(tmpCells);
		Iterator celltr = this.getCells();
		while (celltr.hasNext())
		{
			Cell cell = (Cell) celltr.next();
			cell.initialize(factory, this);
		}
		if (this.cellOrder != null)
		{
			this.cells = OrderManager.doOrder(this.cells,
					this.cellOrder, new CellNameHandler());
		}
		if (this.typicalCell != null)
		{
			this.typicalCell.initialize(factory, this);
		}
	}

	protected void fillSubs(EternaFactory factory)
			throws EternaException
	{
		ListIterator itr = this.cells.listIterator();
		while (itr.hasNext())
		{
			Cell tmp = (Cell) itr.next();
			boolean settedTypical = false;
			if (this.typicalCell != null)
			{
				if (!tmp.isIgnoreGlobalParam() && tmp instanceof CellImpl)
				{
					this.fillEmptyAttr((CellImpl) tmp, this.typicalCell, this.typicalCell.getTypicalComponent());
					settedTypical = true;
				}
			}
			if (!settedTypical && tmp instanceof CellImpl)
			{
				CellImpl c = (CellImpl) tmp;
				c.setBeforeInit(ViewTool.addParentScript(tmp.getBeforeInit(), null));
				c.setInitScript(ViewTool.addParentScript(tmp.getInitScript(), null));
			}

			if (tmp.getCaption() == null && tmp instanceof CellImpl)
			{
				String caption = Tool.translateCaption(factory, tmp.getName());
				if (caption != null)
				{
					((CellImpl) tmp).setCaption(caption);
				}
				else
				{
					((CellImpl) tmp).setCaption(tmp.getName());
				}
			}
		}
	}

	CellImpl createCellByItem(EntityItem item, EternaFactory factory)
			throws EternaException
	{
		CellImpl cell = new CellImpl();
		this.fillSubByItem(cell, item);
		String tmpStr = (String) cell.getAttribute("cellSize");
		if (tmpStr != null)
		{
			cell.removeAttribute("cellSize");
			String[] sizeArr = StringTool.separateString(tmpStr, ",", true);
			cell.setTitleSize(Integer.parseInt(sizeArr[0]));
			cell.setContainerSize(Integer.parseInt(sizeArr[1]));
		}
		tmpStr = (String) cell.getAttribute("newRow");
		if (tmpStr != null)
		{
			cell.removeAttribute("newRow");
			cell.setNewRow(booleanConverter.convertToBoolean(tmpStr));
		}
		tmpStr = (String) cell.getAttribute("required");
		if (tmpStr != null)
		{
			cell.removeAttribute("required");
			cell.setRequired(booleanConverter.convertToBoolean(tmpStr));
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
	private static BooleanConverter booleanConverter = new BooleanConverter();

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

	public void printSpecialBody(Writer out, AppData data, View view)
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
		this.cells.add(ref);
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

	protected ModifiableViewRes getModifiableViewRes()
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
			implements Cell
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

}

/**
 * 处理EntityRef的容器.
 */
class CellContainer
		implements EntityImpl.Container
{
	public CellContainer(TableFormImpl tableForm, Map nameCache, List itemList)
	{
		this.tableForm = tableForm;
		this.nameCache = nameCache;
		this.itemList = itemList;
	}
	private final Map nameCache;
	private final List itemList;
	private final TableFormImpl tableForm;

	public String getName()
	{
		return this.tableForm.getName();
	}

	public String getType()
	{
		return "TableForm";
	}

	public boolean contains(String name)
	{
		return this.nameCache.containsKey(name);
	}

	public void add(EntityItem item, String tableAlias)
	{
		if (AbstractTable.checkVisible(item))
		{
			Object column = this.tableForm.createCellByItem(
					item, this.tableForm.getFactory());
			this.itemList.add(column);
			this.nameCache.put(item.getName(), column);
		}
	}

}

class CellNameHandler
		implements OrderManager.NameHandler
{
	public String getName(Object obj)
	{
		return ((TableForm.Cell) obj).getName();
	}

}