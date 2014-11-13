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

import self.micromagic.eterna.digester.ConfigurationException;
import self.micromagic.eterna.model.AppData;
import self.micromagic.eterna.share.EternaFactory;
import self.micromagic.eterna.view.BaseManager;
import self.micromagic.eterna.view.Component;
import self.micromagic.eterna.view.TableForm;
import self.micromagic.eterna.view.TableFormGenerator;
import self.micromagic.eterna.view.TableList;
import self.micromagic.eterna.view.TableListGenerator;
import self.micromagic.eterna.view.ViewAdapter;
import self.micromagic.eterna.view.ViewAdapterGenerator;

/**
 * @author micromagic@sina.com
 */
public abstract class AbstractTable extends ComponentImpl
{
	protected boolean autoArrange = true;
	protected boolean percentWidth = true;
	protected boolean caculateWidth;
	protected int caculateWidthFix = -1;

	protected Component tr;
	protected String dataName;
	protected String baseName;
	protected BaseManager baseManager;

	private ViewAdapterGenerator.ModifiableViewRes viewRes = null;

	protected abstract void initSubs(EternaFactory factory) throws ConfigurationException;

	protected abstract void initBaseManager(String baseManagerName, EternaFactory factory) throws ConfigurationException;

	protected abstract void fillSubs(EternaFactory factory) throws ConfigurationException;

	public void initialize(EternaFactory factory, Component parent)
			throws ConfigurationException
	{
		if (this.initialized)
		{
			return;
		}
		super.initialize(factory, parent);
		this.initSubs(factory);

		if (this.baseName != null)
		{
			this.initBaseManager(this.baseName, factory);
		}
		this.fillSubs(factory);

		if (this.tr != null)
		{
			this.tr.initialize(factory, this);
		}

		if (this.getDataName() != null)
		{
			// 将dataName在before-init中设置到eg_temp.dataName
			String frontScript = "eg_temp.dataName=\""
						+ this.stringCoder.toJsonString(this.getDataName()) + "\";";
			if (this.getBeforeInit() == null)
			{
				this.setBeforeInit(frontScript);
			}
			else
			{
				this.setBeforeInit(frontScript + this.getBeforeInit());
			}
		}
	}

	protected void fillSubByItem(Sub sub, BaseManager.Item item)
			throws ConfigurationException
	{
		sub.setName(item.getName());
		if (item.getCaption() != null && item.getCaption().length() > 0)
		{
			// 标题不为空及空串时才设置
			sub.setCaption(item.getCaption());
		}
		if (item.getDataSrc() != null)
		{
			sub.srcName = item.getDataSrc();
		}
		else
		{
			sub.srcName = item.getName();
		}
		if (item.getInputType() != null)
		{
			sub.typicalComponentName = item.getInputType();
		}
		if (item.getInitParam() != null)
		{
			sub.initParam = item.getInitParam();
		}
		if (item.getBeforeInit() != null)
		{
			sub.beforeInit = item.getBeforeInit();
		}
		if (item.getInitScript() != null)
		{
			sub.initScript = item.getInitScript();
		}
		if (item.getContainerParam() != null)
		{
			sub.componentParam = item.getContainerParam();
		}
		if (item.getTitleParam() != null)
		{
			sub.titleParam = item.getTitleParam();
		}
	}

	protected void fillEmptySubAttr(Sub nowSub, CommonSub typical, Component typicalComponent,
			boolean defaultBeforeInit)
			throws ConfigurationException
	{
		if (nowSub.getSrcName() == null && typical.getSrcName() != null)
		{
			if (ViewTool.TYPICAL_SAME_AS_NAME.equals(typical.getSrcName()))
			{
				nowSub.setSrcName(nowSub.getName());
			}
			else
			{
				nowSub.setSrcName(typical.isOtherData() ?
						typical.getDataName() + "/" + typical.getSrcName() : typical.getSrcName());
			}
		}
		if (nowSub.getCaption() == null && typical.getCaption() != null && typical.getCaption().length() > 0)
		{
			// 对于标题, 必须不为空字符串才可设置
			if (ViewTool.TYPICAL_SAME_AS_NAME.equals(typical.getCaption()))
			{
				nowSub.setCaption(nowSub.getName());
			}
			else
			{
				nowSub.setCaption(typical.getCaption());
			}
		}
		if (nowSub.typicalComponentName == null && typicalComponent != null)
		{
			nowSub.setTypicalComponentName(typicalComponent.getName());
			nowSub.typicalComponent = typicalComponent;
		}
		if (nowSub.getContainerParam() == null && typical.getContainerParam() != null)
		{
			nowSub.setComponentParam(typical.getContainerParam());
		}
		if (nowSub.getTitleParam() == null && typical.getTitleParam() != null)
		{
			nowSub.setTitleParam(typical.getTitleParam());
		}
		if (nowSub.getInitParam() == null && typical.getInitParam() != null)
		{
			nowSub.setInitParam(typical.getInitParam());
		}
		if (typical.getBeforeInit() != null)
		{
			if (defaultBeforeInit)
			{
				nowSub.setBeforeInit(typical.getBeforeInit());
			}
			else
			{
				nowSub.setBeforeInit(ViewTool.addParentScript(nowSub.getBeforeInit(), typical.getBeforeInit()));
			}
		}
		if (typical.getInitScript() != null)
		{
			nowSub.setInitScript(ViewTool.addParentScript(nowSub.getInitScript(), typical.getInitScript()));
		}
		if (!nowSub.ignoreGlobalContainerParamSetted && typical.isIgnoreGlobalContainerParam())
		{
			nowSub.setIgnoreGlobalParam(true);
		}
		if (!nowSub.ignoreGlobalTitleParamSetted && typical.isIgnoreGlobalTitleParam())
		{
			nowSub.setIgnoreGlobalTitleParam(true);
		}
	}

	public void printSpecialBody(Writer out, AppData data, ViewAdapter view)
			throws IOException, ConfigurationException
	{
		if (!this.isPercentWidth())
		{
			out.write(",percentWidth:0");
		}
		if (this.isCaculateWidth())
		{
			out.write(",caculateWidth:1");
		}
		if (this.isCaculateWidth() && this.getCaculateWidthFix() != -1)
		{
			out.write(",caculateWidth_fix:");
			out.write(String.valueOf(this.getCaculateWidthFix()));
		}

		if (this.getDataName() != null)
		{
			out.write(",dataName:\"");
			this.stringCoder.toJsonStringWithoutCheck(out, this.getDataName());
			out.write('"');
		}

		if (this.getTR() != null)
		{
			out.write(",tr:{");
			this.getTR().printBody(out, data, view);
			out.write('}');
		}
	}

	public boolean isAutoArrange()
	{
		return this.autoArrange;
	}

	public void setAutoArrange(boolean autoArrange)
	{
		this.autoArrange = autoArrange;
	}

	public boolean isPercentWidth()
	{
		return this.percentWidth;
	}

	public void setPercentWidth(boolean percentWidth)
	{
		this.percentWidth = percentWidth;
	}

	public boolean isCaculateWidth()
	{
		return this.caculateWidth;
	}

	public void setCaculateWidth(boolean caculateWidth)
	{
		this.caculateWidth = caculateWidth;
	}

	public int getCaculateWidthFix()
	{
		return this.caculateWidthFix;
	}

	public void setCaculateWidthFix(int caculateWidthFix)
	{
		this.caculateWidthFix = caculateWidthFix;
	}

	public Component getTR()
	{
		return this.tr;
	}

	public void setTR(Component tr)
			throws ConfigurationException
	{
		if (!"tr".equalsIgnoreCase(tr.getType()))
		{
			throw new ConfigurationException("Need the Component type is [tr], but you gived [" + tr.getType() + "].");
		}
		if (tr.getName() == null)
		{
			throw new ConfigurationException("You must set tr's name.");
		}
		if (this instanceof TableList || this instanceof TableListGenerator)
		{
			if (!tr.getName().startsWith(TableList.TR_NAME_PERFIX))
			{
				throw new ConfigurationException("In table-list tr name must start with ["
						+ TableList.TR_NAME_PERFIX + "].");
			}
		}
		else if (this instanceof TableForm || this instanceof TableFormGenerator)
		{
			if (!tr.getName().startsWith(TableForm.TR_NAME_PERFIX))
			{
				throw new ConfigurationException("In table-form tr name must start with ["
						+ TableForm.TR_NAME_PERFIX + "].");
			}
		}
		this.tr = tr;
	}

	public String getBaseName()
	{
		return this.baseName;
	}

	public void setBaseName(String name)
	{
		this.baseName = name;
	}

	public String getDataName()
	{
		return this.dataName;
	}

	public void setDataName(String dataName)
	{
		this.dataName = dataName;
	}

	public void addComponent(Component com)
			throws ConfigurationException
	{
		throw new ConfigurationException("The " + this.getType() + " [" + this.getName()
				+ "] can't add a Component.");
	}

	protected ViewAdapterGenerator.ModifiableViewRes getModifiableViewRes()
			throws ConfigurationException
	{
		if (this.viewRes == null)
		{
			this.viewRes = super.getModifiableViewRes();
			if (this.tr != null)
			{
				this.viewRes.addAll(this.tr.getViewRes());
			}
		}
		return this.viewRes;
	}

	public static abstract class Sub extends ComponentImpl
	{
		protected String titleParam;
		protected boolean ignoreGlobalTitleParam;
		protected boolean ignoreGlobalTitleParamSetted = false;
		protected boolean ignoreGlobalContainerParam;
		protected boolean ignoreGlobalContainerParamSetted = false;
		protected String caption = null;
		protected String defaultValue;
		protected boolean ignore = false;
		protected String srcName;
		protected String otherDataName;
		protected String typicalComponentName;
		protected Component typicalComponent;
		protected String initParam;

		private ViewAdapterGenerator.ModifiableViewRes viewRes = null;

		public void initialize(EternaFactory factory, Component parent) throws ConfigurationException
		{
			if (this.initialized)
			{
				return;
			}
			super.initialize(factory, parent);
			if (this.typicalComponentName != null && this.typicalComponentName.length() > 0)
			{
				this.typicalComponent = factory.getTypicalComponent(this.typicalComponentName);
				if (this.typicalComponent == null)
				{
					log.warn("The Typical Component [" + this.typicalComponentName + "] not found.");
				}
			}
		}

		protected boolean checkName(String name)
		{
			if (name == null)
			{
				return true;
			}
			if (ViewTool.TYPICAL_NAME.equals(name))
			{
				return true;
			}
			return super.checkName(name);
		}

		protected abstract String getDataName() throws ConfigurationException;

		protected abstract void printSpecialTitle(Writer out, AppData data) throws IOException, ConfigurationException;

		protected abstract void printSpecialContainer(Writer out, AppData data) throws IOException, ConfigurationException;

		protected abstract void printSpecialElse(Writer out, AppData data) throws IOException, ConfigurationException;

		public void printSpecialBody(Writer out, AppData data, ViewAdapter view)
				throws IOException, ConfigurationException
		{
			out.write(",title:{");
			if (this.getCaption() != null)
			{
				out.write("caption:\"");
				this.stringCoder.toJsonStringWithoutCheck(out, this.getCaption());
				out.write('"');
			}
			else
			{
				out.write("caption:\"\"");
			}
			this.printSpecialTitle(out, data);
			if (this.getTitleParam() != null)
			{
				out.write(',');
				out.write(this.getTitleParam());
			}
			if (this.isIgnoreGlobalTitleParam())
			{
				out.write(",ignoreGlobal:1");
			}
			out.write("},container:{");
			if (this.getDefaultValue() != null)
			{
				out.write("defaultValue:\"");
				this.stringCoder.toJsonStringWithoutCheck(out, this.getDefaultValue());
				out.write('"');
			}
			else
			{
				out.write("defaultValue:null");
			}
			this.printSpecialContainer(out, data);
			if (this.getSrcName() != null && this.getSrcName().length() > 0)
			{
				out.write(",value:{dataName:");
				if (!this.isOtherData())
				{
					out.write('"');
					this.stringCoder.toJsonString(out, this.getDataName());
					out.write('"');
				}
				else
				{
					out.write('"');
					this.stringCoder.toJsonString(out, this.otherDataName);
					out.write('"');
				}
				out.write(",srcName:\"");
				this.stringCoder.toJsonString(out, this.getSrcName());
				out.write("\"}");
			}
			else
			{
				log.warn("The table sub [" + this.getParent().getName() + ":" + this.getName()
						+ "]'s attribute srcName not setted!");
			}
			if (this.getContainerParam() != null)
			{
				out.write(',');
				out.write(this.getContainerParam());
			}
			if (this.isIgnoreGlobalContainerParam())
			{
				out.write(",ignoreGlobal:1");
			}
			out.write('}');

			this.printSpecialElse(out, data);

			if (this.getInitParam() != null)
			{
				out.write(",initParam:{");
				out.write(this.getInitParam());
				out.write('}');
			}
			if (this.getTypicalComponent() != null)
			{
				String idName = ViewTool.createTypicalComponentName(data, this.getTypicalComponent());
				out.write(",typicalComponent:\"");
				this.stringCoder.toJsonString(out, idName);
				out.write('"');
			}

		}

		public String getTitleParam()
		{
			return this.titleParam;
		}

		public void setTitleParam(String param)
		{
			this.titleParam = param;
		}

		public boolean isIgnoreGlobalTitleParam()
		{
			return this.ignoreGlobalTitleParam;
		}

		public void setIgnoreGlobalTitleParam(boolean ignore)
		{
			this.ignoreGlobalTitleParamSetted = true;
			this.ignoreGlobalTitleParam = ignore;
		}

		public String getComponentParam()
		{
			// 这里返回null, 不设componentParam, 因为要在container属性中生成
			return null;
		}

		public String getContainerParam()
		{
			return this.componentParam;
		}

		public void setIgnoreGlobalParam(boolean ignore)
		{
			this.ignoreGlobalContainerParamSetted = true;
			this.ignoreGlobalParam = ignore;
			this.ignoreGlobalContainerParam = ignore;
		}

		public boolean isIgnoreGlobalContainerParam()
		{
			return this.ignoreGlobalContainerParam;
		}

		public String getCaption()
		{
			return this.caption;
		}

		public void setCaption(String caption)
		{
			this.caption = caption;
		}

		public String getDefaultValue()
		{
			return this.defaultValue;
		}

		public void setDefaultValue(String defaultValue)
		{
			this.defaultValue = defaultValue;
		}

		public boolean isIgnore()
		{
			return this.ignore;
		}

		public void setIgnore(boolean ignore)
		{
			this.ignore = ignore;
		}

		public String getSrcName()
		{
			return this.srcName;
		}

		public void setSrcName(String srcName)
		{
			int index = srcName.indexOf('/');
			if (index != -1)
			{
				this.otherDataName = srcName.substring(0, index);
				this.srcName = srcName.substring(index + 1);
			}
			else
			{
				this.srcName = srcName;
			}
		}

		public boolean isOtherData()
		{
			return this.otherDataName != null;
		}

		public Component getTypicalComponent()
		{
			return this.typicalComponent;
		}

		public void setTypicalComponentName(String name)
		{
			this.typicalComponentName = name;
		}

		public String getInitParam()
		{
			return this.initParam;
		}

		public void setInitParam(String param)
		{
			this.initParam = param;
		}

		protected ViewAdapterGenerator.ModifiableViewRes getModifiableViewRes()
				throws ConfigurationException
		{
			if (this.viewRes == null)
			{
				this.viewRes = super.getModifiableViewRes();
				this.titleParam = this.dealParamPart(this.titleParam, this.viewRes);
				this.initParam = this.dealParamPart(this.initParam, this.viewRes);
				this.caption = ViewTool.dealScriptPart(
						this.viewRes, this.caption, ViewTool.GRAMMER_TYPE_NONE, this.getFactory());
				this.defaultValue = ViewTool.dealScriptPart(
						this.viewRes, this.defaultValue, ViewTool.GRAMMER_TYPE_NONE, this.getFactory());
				if (this.typicalComponent != null)
				{
					this.viewRes.addAll(this.typicalComponent.getViewRes());
				}
			}
			return this.viewRes;
		}

	}

	protected interface CommonSub
	{
		String getTitleParam() throws ConfigurationException;

		String getContainerParam() throws ConfigurationException;

		boolean isIgnoreGlobalTitleParam() throws ConfigurationException;

		boolean isIgnoreGlobalContainerParam() throws ConfigurationException;

		String getCaption() throws ConfigurationException;

		String getDefaultValue() throws ConfigurationException;

		String getSrcName() throws ConfigurationException;

		String getDataName() throws ConfigurationException;

		boolean isOtherData() throws ConfigurationException;

		String getInitParam() throws ConfigurationException;

		String getName() throws ConfigurationException;

		boolean isIgnoreGlobalParam() throws ConfigurationException;

		String getBeforeInit() throws ConfigurationException;

		String getInitScript() throws ConfigurationException;

	}

	protected static class CellSub
			implements CommonSub
	{
		private TableForm.Cell cell;

		public CellSub(TableForm.Cell cell)
		{
			this.cell = cell;
		}

		public String getTitleParam()
				throws ConfigurationException
		{
			return this.cell.getTitleParam();
		}

		public String getContainerParam()
				throws ConfigurationException
		{
			return this.cell.getContainerParam();
		}

		public boolean isIgnoreGlobalTitleParam()
				throws ConfigurationException
		{
			return this.cell.isIgnoreGlobalTitleParam();
		}

		public boolean isIgnoreGlobalContainerParam()
				throws ConfigurationException
		{
			return this.cell.isIgnoreGlobalContainerParam();
		}

		public String getCaption()
				throws ConfigurationException
		{
			return this.cell.getCaption();
		}

		public String getDefaultValue()
				throws ConfigurationException
		{
			return this.cell.getDefaultValue();
		}

		public String getSrcName()
				throws ConfigurationException
		{
			return this.cell.getSrcName();
		}

		public String getDataName()
				throws ConfigurationException
		{
			return this.cell.getDataName();
		}

		public boolean isOtherData()
				throws ConfigurationException
		{
			return this.cell.isOtherData();
		}

		public String getInitParam()
				throws ConfigurationException
		{
			return this.cell.getInitParam();
		}

		public String getName()
				throws ConfigurationException
		{
			return this.cell.getName();
		}

		public boolean isIgnoreGlobalParam()
				throws ConfigurationException
		{
			return this.cell.isIgnoreGlobalParam();
		}

		public String getBeforeInit()
				throws ConfigurationException
		{
			return this.cell.getBeforeInit();
		}

		public String getInitScript()
				throws ConfigurationException
		{
			return this.cell.getInitScript();
		}

	}

	protected static class ColumnSub
			implements CommonSub
	{
		private TableList.Column column;

		public ColumnSub(TableList.Column column)
		{
			this.column = column;
		}

		public String getTitleParam()
				throws ConfigurationException
		{
			return this.column.getTitleParam();
		}

		public String getContainerParam()
				throws ConfigurationException
		{
			return this.column.getContainerParam();
		}

		public boolean isIgnoreGlobalTitleParam()
				throws ConfigurationException
		{
			return this.column.isIgnoreGlobalTitleParam();
		}

		public boolean isIgnoreGlobalContainerParam()
				throws ConfigurationException
		{
			return this.column.isIgnoreGlobalContainerParam();
		}

		public String getCaption()
				throws ConfigurationException
		{
			return this.column.getCaption();
		}

		public String getDefaultValue()
				throws ConfigurationException
		{
			return this.column.getDefaultValue();
		}

		public String getSrcName()
				throws ConfigurationException
		{
			return this.column.getSrcName();
		}

		public String getDataName()
				throws ConfigurationException
		{
			return this.column.getDataName();
		}

		public boolean isOtherData()
				throws ConfigurationException
		{
			return this.column.isOtherData();
		}

		public String getInitParam()
				throws ConfigurationException
		{
			return this.column.getInitParam();
		}

		public String getName()
				throws ConfigurationException
		{
			return this.column.getName();
		}

		public boolean isIgnoreGlobalParam()
				throws ConfigurationException
		{
			return this.column.isIgnoreGlobalParam();
		}

		public String getBeforeInit()
				throws ConfigurationException
		{
			return this.column.getBeforeInit();
		}

		public String getInitScript()
				throws ConfigurationException
		{
			return this.column.getInitScript();
		}

	}

}