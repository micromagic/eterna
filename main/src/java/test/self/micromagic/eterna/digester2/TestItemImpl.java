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

package self.micromagic.eterna.digester2;

import java.lang.reflect.Member;
import java.lang.reflect.Method;

import self.micromagic.cg.BeanDescriptor;
import self.micromagic.cg.BeanMap;
import self.micromagic.cg.BeanTool;
import self.micromagic.cg.CellDescriptor;
import self.micromagic.cg.proxy.BeanPropertyWriter;
import self.micromagic.eterna.dao.Entity;
import self.micromagic.eterna.dao.EntityItem;
import self.micromagic.eterna.dao.impl.ParameterGroup;
import self.micromagic.eterna.security.PermissionSet;
import self.micromagic.eterna.share.AbstractGenerator;
import self.micromagic.eterna.share.EternaException;
import self.micromagic.eterna.share.EternaFactory;
import self.micromagic.eterna.share.TypeManager;
import self.micromagic.util.AbstractPreparerCreater;

public class TestItemImpl extends AbstractGenerator
		implements EntityItem
{
	public void initialize(Entity entity)
			throws EternaException
	{
		this.type = TypeManager.getTypeId(this.typeName);
		this.columnName = this.columnName == null ? this.getName() : this.columnName;
		this.attributes.convertType(entity.getFactory(), "item");
		this.entity = entity;
		Object obj = this.attributes.getAttribute("myType");
		if (obj != null)
		{
			TestItemPrepare p = new TestItemPrepare();
			p.setType((String) obj);
			String tmpName = "n_" + this.getName();
			p.setName(tmpName);
			entity.getFactory().registerObject(p);
			this.setAttribute(ParameterGroup.PREPARE_FLAG, tmpName);
		}
	}
	private Entity entity;

	public Entity getEntity()
	{
		return this.entity;
	}

	public String getColumnName()
	{
		return this.colName;
	}
	private String colName;

	public int getType()
	{
		return this.type;
	}
	private int type;

	public String getCaption()
	{
		return this.caption;
	}

	public PermissionSet getPermissionSet()
			throws EternaException
	{
		return null;
	}

	public Object create()
	{
		return this;
	}

	public void setColumnName(String colName)
	{
		this.columnName = colName;
	}
	private String columnName;

	public void setType(String type)
	{
		this.typeName = type;
	}
	private String typeName;

	public void setCaption(String caption)
	{
		this.caption = caption;
	}
	private String caption;

	public void setPermission(String permission)
	{
	}

	public void merge(EntityItem other)
	{
	}

	static
	{
		BeanDescriptor bd = BeanTool.getBeanDescriptor(TestItemImpl.class);
		bd.getCell("type").setWriteProcesser(new BeanPropertyWriter() {
			public int setBeanValue(CellDescriptor cd, int[] indexs, Object bean, Object value,
					String prefix, BeanMap beanMap, Object originObj, Object oldValue)
					throws Exception
			{
				((TestItemImpl) bean).setType((String) value);
				return 1;
			}

			public Member getMember()
			{
				if (this.tmpMethod != null)
				{
					return this.tmpMethod;
				}
				try
				{
					Method m = TestItemImpl.class.getMethod("setType", new Class[]{String.class});
					return this.tmpMethod = m;
				}
				catch (Exception ex)
				{
					return null;
				}
			}
			private Member tmpMethod;

		});
	}

}

class TestItemPrepare extends AbstractPreparerCreater
{
	public boolean initialize(EternaFactory factory)
			throws EternaException
	{
		//System.out.println("Init ---- " + this.getName());
		return super.initialize(factory);
	}

	protected Object convertValue(String value)
			throws EternaException
	{
		return value;
	}

	protected Object convertValue(Object value)
			throws EternaException
	{
		return value == null ? null : new Integer(value.hashCode());
	}

}
