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

package self.micromagic.eterna.digester;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.digester.Digester;
import org.apache.commons.digester.Rule;
import org.apache.commons.digester.RuleSetBase;
import self.micromagic.eterna.search.ColumnSetting;
import self.micromagic.eterna.search.ConditionBuilder;
import self.micromagic.eterna.search.ConditionProperty;
import self.micromagic.eterna.search.ParameterSetting;
import self.micromagic.eterna.search.SearchAdapterGenerator;
import self.micromagic.eterna.search.SearchManagerGenerator;
import self.micromagic.eterna.search.impl.ConditionBuilderGeneratorImpl;
import self.micromagic.eterna.search.impl.ConditionPropertyGeneratorImpl;
import self.micromagic.eterna.search.impl.SearchAdapterImpl;
import self.micromagic.eterna.search.impl.SearchManagerImpl;
import self.micromagic.eterna.share.AbstractGenerator;

/**
 * search模块初始化的规则集.
 *
 * @author micromagic@sina.com
 */
public class SearchRuleSet extends RuleSetBase
{
	public SearchRuleSet()
	{
	}

	public void addRuleInstances(Digester digester)
	{
		PropertySetter setter;
		PropertySetter[] setters;
		Rule rule;


		//--------------------------------------------------------------------------------
		// 构造ConditionBuilder和ConditionBuilder列表
		setter = new GeneratorPropertySetter("generator", "addConditionBuilder",
				ConditionBuilderGeneratorImpl.class.getName(), ConditionBuilder.class, true);
		digester.addRule("eterna-config/factory/objs/builder",
				new PropertySetRule(setter));

		setters = new PropertySetter[] {
			new StringPropertySetter("name", "setName", true),
			new StringPropertySetter("caption", "setCaption", false),
			new StringPropertySetter("operator", "setOperator", false)
		};
		digester.addRule("eterna-config/factory/objs/builder",
				new PropertySetRule(setters, false));
		digester.addRule("eterna-config/factory/objs/builder",
				new ObjectLogRule("name", "ConditionBuilder"));
		digester.addRule("eterna-config/factory/objs/builder/attribute",
				new AttributeSetRule());

		// 构造ConditionBuilder列表
		setter = new GeneratorPropertySetter(null, "addConditionBuilderList",
				ConditionBuilderListGenerator.class.getName(), List.class, true);
		digester.addRule("eterna-config/factory/objs/builder-list",
				new PropertySetRule(setter));

		digester.addRule("eterna-config/factory/objs/builder-list",
				new PropertySetRule("name", "setName", true, false));
		digester.addRule("eterna-config/factory/objs/builder-list",
				new ObjectLogRule("name", "ConditionBuilderList"));
		digester.addRule("eterna-config/factory/objs/builder-list/builder-name",
				new PropertySetRule("name", "addConditionBuilderName", true, false));


		//--------------------------------------------------------------------------------
		// 构造SearchManager
		rule = new ObjectCreateRule(SearchManagerImpl.class.getName(), "generator",
				SearchManagerGenerator.class);
		digester.addRule("eterna-config/factory/search-manager", rule);
		setter = new StackPropertySetter("registerSearchManager", SearchManagerGenerator.class, 1);
		digester.addRule("eterna-config/factory/search-manager",
				new PropertySetRule(setter));
		digester.addRule("eterna-config/factory/search-manager",
				new ObjectLogRule("name", "SearchManager"));
		digester.addRule("eterna-config/factory/search-manager/attribute",
				new AttributeSetRule());


		//--------------------------------------------------------------------------------
		// 构造SearchAdapter
		rule = new ObjectCreateRule(SearchAdapterImpl.class.getName(), "generator",
				SearchAdapterGenerator.class);
		digester.addRule("eterna-config/factory/objs/search", rule);

		setter = new StackPropertySetter("registerSearchAdapter", SearchAdapterGenerator.class, 1);
		digester.addRule("eterna-config/factory/objs/search", new PropertySetRule(setter));

		setters = new PropertySetter[] {
			new StringPropertySetter("name", "setName", true),
			new StringPropertySetter("queryName", "setQueryName", true),
			new IntegerPropertySetter("pageSize", "setPageSize", false),
			new StringPropertySetter("countType", "setCountType", false),
			new StringPropertySetter("searchManager", "setSearchManagerName", false),
			new BooleanPropertySetter("specialCondition", "setSpecialCondition", false),
			new BooleanPropertySetter("needWrap", "setNeedWrap", false),
			new IntegerPropertySetter("conditionIndex", "setConditionIndex", "1")
		};
		digester.addRule("eterna-config/factory/objs/search",
				new PropertySetRule(setters, false));
		digester.addRule("eterna-config/factory/objs/search",
				new ObjectLogRule("name", "SearchAdapter"));
		digester.addRule("eterna-config/factory/objs/search/attribute",
				new AttributeSetRule());

		//  设置other-search-manager
		setters = new PropertySetter[] {
			new StringPropertySetter("otherName", "setOtherSearchManagerName", false),
			new StringPropertySetter("propertyOrder", "setConditionPropertyOrderWithOther", false)
		};
		digester.addRule("eterna-config/factory/objs/search/other-search-manager",
				new PropertySetRule(setters, false));

		//  设置condition-propertys的parent
		setters = new PropertySetter[] {
			new StringPropertySetter("parent", "setParentConditionPropretyName", false),
			new StringPropertySetter("propertyOrder", "setConditionPropertyOrder", false)
		};
		digester.addRule("eterna-config/factory/objs/search/condition-propertys",
				new PropertySetRule(setters, false));

		// 添加condition-property
		setter = new GeneratorPropertySetter("generator", "addConditionProperty",
				ConditionPropertyGeneratorImpl.class.getName(), ConditionProperty.class);
		digester.addRule("eterna-config/factory/objs/search/condition-propertys/condition-property",
				new PropertySetRule(setter));

		setters = new PropertySetter[] {
			new StringPropertySetter("name", "setName", true),
			new StringPropertySetter("colName", "setColumnName", false),
			new StringPropertySetter("caption", "setColumnCaption", false),
			new StringPropertySetter("colType", "setColumnType", true),
			new StringPropertySetter("vpcName", "setColumnVPC", false),
			new StringPropertySetter("inputType", "setConditionInputType", false),
			new StringPropertySetter("defaultValue", "setDefaultValue", false),
			new StringPropertySetter("permissions", "setPermissions", false),
			new BooleanPropertySetter("useDefaultBuilder", "setUseDefaultConditionBuilder", false),
			new StringPropertySetter("defaultBuilder", "setDefaultConditionBuilderName", false),
			new StringPropertySetter("builderList", "setConditionBuilderListName", false),
			new BooleanPropertySetter("visible", "setVisible", "true")
		};
		digester.addRule("eterna-config/factory/objs/search/condition-propertys/condition-property",
				new PropertySetRule(setters, false));
		digester.addRule("eterna-config/factory/objs/search/condition-propertys/condition-property/attribute",
				new AttributeSetRule());

		// 设置column-setting
		digester.addRule("eterna-config/factory/objs/search/column-setting",
				new PropertySetRule("columnType", "setColumnSettingType", true, false));
		setter = new ObjectPropertySetter("className", "setColumnSetting",
				null, ColumnSetting.class);
		digester.addRule("eterna-config/factory/objs/search/column-setting",
				new PropertySetRule(setter));

		// 设置parameter-setting
		setter = new ObjectPropertySetter("className", "setParameterSetting",
				null, ParameterSetting.class);
		digester.addRule("eterna-config/factory/objs/search/parameter-setting",
				new PropertySetRule(setter));
	}

	public static class ConditionBuilderListGenerator extends AbstractGenerator
	{
		private List names = new ArrayList();

		public void addConditionBuilderName(String name)
		{
			this.names.add(name);
		}

		public Object create() throws ConfigurationException
		{
			return this.names;
		}

	}

}

