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

import org.apache.commons.digester.Digester;
import org.apache.commons.digester.Rule;
import org.apache.commons.digester.RuleSetBase;
import org.xml.sax.Attributes;
import self.micromagic.eterna.model.Execute;
import self.micromagic.eterna.model.ModelAdapterGenerator;
import self.micromagic.eterna.model.ModelCaller;
import self.micromagic.eterna.model.ModelExport;
import self.micromagic.eterna.model.ParamBind;
import self.micromagic.eterna.model.impl.CheckExecute;
import self.micromagic.eterna.model.impl.ModelAdapterImpl;
import self.micromagic.eterna.model.impl.ModelCallerImpl;
import self.micromagic.eterna.model.impl.ModelExecute;
import self.micromagic.eterna.model.impl.ParamBindImpl;
import self.micromagic.eterna.model.impl.QueryExecute;
import self.micromagic.eterna.model.impl.SearchExecute;
import self.micromagic.eterna.model.impl.TransExecute;
import self.micromagic.eterna.model.impl.UpdateExecute;
import self.micromagic.util.StringTool;
import self.micromagic.util.Utility;

/**
 * model模块初始化的规则集.
 *
 * @author micromagic@sina.com
 */
public class ModelRuleSet extends RuleSetBase
{
	public ModelRuleSet()
	{
	}

	public void addRuleInstances(Digester digester)
	{
		PropertySetter setter;
		PropertySetter[] setters;
		Rule rule;


		//--------------------------------------------------------------------------------
		// 构造ModelCaller
		rule = new ObjectCreateRule(ModelCallerImpl.class.getName(), "className", ModelCaller.class);
		digester.addRule("eterna-config/factory/model-caller", rule);
		setter = new StackPropertySetter("setModelCaller", ModelCaller.class, 1);
		digester.addRule("eterna-config/factory/model-caller", new PropertySetRule(setter));
		digester.addRule("eterna-config/factory/model-caller",
				new ObjectLogRule("name", "ModelCaller"));


		//--------------------------------------------------------------------------------
		// 构造ModelAdapter
		rule = new ObjectCreateRule(ModelAdapterImpl.class.getName(), "generator",
				ModelAdapterGenerator.class);
		digester.addRule("eterna-config/factory/objs/model", rule);

		setter = new StackPropertySetter("registerModelAdapter", ModelAdapterGenerator.class, 1);
		digester.addRule("eterna-config/factory/objs/model", new PropertySetRule(setter));

		setters = new PropertySetter[] {
			new StringPropertySetter("name", "setName", true),
			new BooleanPropertySetter("needFrontModel", "setNeedFrontModel", false),
			new StringPropertySetter("frontModelName", "setFrontModelName", false),
			new StringPropertySetter("modelExportName", "setModelExportName", false),
			new StringPropertySetter("errorExportName", "setErrorExportName", false),
			new StringPropertySetter("transactionType", "setTransactionType", false),
			new StringPropertySetter("dataSourceName", "setDataSourceName", false),
			new StringPropertySetter("positions", "setAllowPosition", false)
		};
		digester.addRule("eterna-config/factory/objs/model",
				new PropertySetRule(setters, false));
		digester.addRule("eterna-config/factory/objs/model",
				new ObjectLogRule("name", "Model"));
		digester.addRule("eterna-config/factory/objs/model/attribute",
				new AttributeSetRule());

		//设置普通的execute
		setter = new GeneratorPropertySetter("generator", "addExecute",
				null, Execute.class);
		digester.addRule("eterna-config/factory/objs/model/execute",
				new PropertySetRule(setter));
		setters = new PropertySetter[] {
			new StringPropertySetter("name", "setName", false)
		};
		digester.addRule("eterna-config/factory/objs/model/execute",
				new PropertySetRule(setters, false));
		digester.addRule("eterna-config/factory/objs/model/execute/attribute",
				new AttributeSetRule());

		//设置update-execute
		setter = new GeneratorPropertySetter("generator", "addExecute",
				UpdateExecute.class.getName(), Execute.class);
		digester.addRule("eterna-config/factory/objs/model/update-execute",
				new PropertySetRule(setter));
		setters = new PropertySetter[] {
			new StringPropertySetter("updateName", "setName", true),
			new IntegerPropertySetter("sqlCache", "setCache", false),
			new BooleanPropertySetter("doExecute", "setDoExecute", false),
			new BooleanPropertySetter("multiType", "setMultiType", false),
			new BooleanPropertySetter("pushResult", "setPushResult", "false")
		};
		digester.addRule("eterna-config/factory/objs/model/update-execute",
				new PropertySetRule(setters, false));
		this.setParamBindRule(digester, "eterna-config/factory/objs/model/update-execute");

		//设置query-execute
		setter = new GeneratorPropertySetter("generator", "addExecute",
				QueryExecute.class.getName(), Execute.class);
		digester.addRule("eterna-config/factory/objs/model/query-execute",
				new PropertySetRule(setter));
		setters = new PropertySetter[] {
			new StringPropertySetter("queryName", "setName", true),
			new IntegerPropertySetter("sqlCache", "setCache", false),
			new BooleanPropertySetter("doExecute", "setDoExecute", false),
			new IntegerPropertySetter("start", "setStart", false),
			new IntegerPropertySetter("count", "setCount", false),
			new StringPropertySetter("countType", "setCountType", false),
			new BooleanPropertySetter("pushResult", "setPushResult", "true")
		};
		digester.addRule("eterna-config/factory/objs/model/query-execute",
				new PropertySetRule(setters, false));
		this.setParamBindRule(digester, "eterna-config/factory/objs/model/query-execute");

		//设置trans-execute
		setter = new GeneratorPropertySetter("generator", "addExecute",
				TransExecute.class.getName(), Execute.class);
		digester.addRule("eterna-config/factory/objs/model/trans-execute",
				new PropertySetRule(setter));
		setters = new PropertySetter[] {
			new BooleanPropertySetter("pushResult", "setPushResult", "false"),
			new StringPropertySetter("from", "setFrom", true),
			new BooleanPropertySetter("removeFrom", "setRemoveFrom", false),
			new BooleanPropertySetter("mustExist", "setMustExist", false),
			new StringPropertySetter("opt", "setOpt", false),
			new StringPropertySetter("to", "setTo", false)
		};
		digester.addRule("eterna-config/factory/objs/model/trans-execute",
				new PropertySetRule(setters, false));

		//设置search-execute
		setter = new GeneratorPropertySetter("generator", "addExecute",
				SearchExecute.class.getName(), Execute.class);
		digester.addRule("eterna-config/factory/objs/model/search-execute",
				new PropertySetRule(setter));
		setters = new PropertySetter[] {
			new StringPropertySetter("searchNameTag", "setSearchNameTag", false),
			new StringPropertySetter("searchName", "setSearchName", false),
			new IntegerPropertySetter("searchCache", "setCache", false),
			new StringPropertySetter("queryResultName", "setQueryResultName", false),
			new StringPropertySetter("searchManagerName", "setSearchManagerName", false),
			new StringPropertySetter("searchCountName", "setSearchCountName", false),
			new BooleanPropertySetter("saveCondition", "setSaveCondition", false),
			new BooleanPropertySetter("forceSetParam", "setForceSetParam", false),
			new IntegerPropertySetter("start", "setStart", false),
			new IntegerPropertySetter("count", "setCount", false),
			new BooleanPropertySetter("holdConnection", "setHoldConnection", false),
			new BooleanPropertySetter("doExecute", "setDoExecute", false)
		};
		digester.addRule("eterna-config/factory/objs/model/search-execute",
				new PropertySetRule(setters, false));

		//设置model-execute
		setter = new GeneratorPropertySetter("generator", "addExecute",
				ModelExecute.class.getName(), Execute.class);
		digester.addRule("eterna-config/factory/objs/model/model-execute",
				new PropertySetRule(setter));
		setters = new PropertySetter[] {
			new StringPropertySetter("modelName", "setName", false),
			new StringPropertySetter("exportName", "setExportName", false),
			new StringPropertySetter("transactionType", "setTransactionType", false),
			new BooleanPropertySetter("noJump", "setNoJump", false)
		};
		digester.addRule("eterna-config/factory/objs/model/model-execute",
				new PropertySetRule(setters, false));
		digester.addRule("eterna-config/factory/objs/model/model-execute/attribute",
				new AttributeSetRule());

		//设置check-execute
		setter = new GeneratorPropertySetter("generator", "addExecute",
				CheckExecute.class.getName(), Execute.class);
		digester.addRule("eterna-config/factory/objs/model/check-execute",
				new PropertySetRule(setter));
		setters = new PropertySetter[] {
			new StringPropertySetter("checkPattern", "setCheckPattern", true),
			new IntegerPropertySetter("loopType", "setLoopType", false),
			new StringPropertySetter("trueModelName", "setTrueModelName", false),
			new StringPropertySetter("falseModelName", "setFalseModelName", false),
			new StringPropertySetter("trueExportName", "setTrueExportName", false),
			new StringPropertySetter("falseExportName", "setFalseExportName", false),
			new StringPropertySetter("trueTransactionType", "setTrueTransactionType", false),
			new StringPropertySetter("falseTransactionType", "setFalseTransactionType", false)
		};
		digester.addRule("eterna-config/factory/objs/model/check-execute",
				new PropertySetRule(setters, false));
		digester.addRule("eterna-config/factory/objs/model/check-execute/attribute",
				new AttributeSetRule());


		//--------------------------------------------------------------------------------
		// 构造export
		digester.addRule("eterna-config/factory/objs/export",
				new PropertySetRule(new ModelExportSetter(), false));
		digester.addRule("eterna-config/factory/objs/export",
				new ObjectLogRule("name", "ModelExport"));

	}

	private void setParamBindRule(Digester digester, String path)
	{
		//eterna-config/factory/objs/model/update-execute
		//eterna-config/factory/objs/model/query-execute

		PropertySetter setter;
		PropertySetter[] setters;

		setter = new GeneratorPropertySetter("generator", "addParamBind",
				ParamBindImpl.class.getName(), ParamBind.class);
		digester.addRule(path + "/param-begin",
				new PropertySetRule(setter));

		setters = new PropertySetter[] {
			new StringPropertySetter("src", "setSrc", true),
			new StringPropertySetter("names", "setNames", false, false),
			new BooleanPropertySetter("loop", "setLoop", false),
			new BooleanPropertySetter("subSQL", "setSubSQL", false)
		};
		digester.addRule(path + "/param-begin",
				new PropertySetRule(setters, false));

		digester.addRule(path + "/param-begin/attribute", new AttributeSetRule());
	}

	public static class ModelExportSetter extends SinglePropertySetter
	{
		protected ModelExport modelExport;

		public ModelExportSetter()
		{
			super("name", "addModelExport", null);
			this.mustExist = true;
			this.type = new Class[]{String.class, ModelExport.class};
		}

		public Object prepareProperty(String namespace, String name, Attributes attributes)
				throws Exception
		{
			String exportName = this.getValue(namespace, name, attributes);

			String viewName = attributes.getValue("viewName");
			String modelName = attributes.getValue("modelName");
			String path = Utility.resolveDynamicPropnames(attributes.getValue("path"));
			boolean redirect = "true".equalsIgnoreCase(attributes.getValue("redirect"));
			if (modelName != null)
			{
				if (viewName != null || path != null)
				{
					throw new ConfigurationException(
							"Can't set the attribute 'path' or 'viewName' when given the attribute 'modelName'.");
				}
				this.modelExport = new ModelExport(exportName, redirect, StringTool.intern(modelName));
			}
			else if (path != null)
			{
				path = StringTool.intern(path);
				this.modelExport = viewName != null ?
						new ModelExport(exportName, path, StringTool.intern(viewName), redirect)
						: new ModelExport(exportName, path, redirect);
			}
			else
			{
				throw new ConfigurationException(
						"Must set the attribute 'path' or 'modelName'.");
			}
			// 判断是否设置了errorExport的标志
			String errorExport = attributes.getValue("errorExport");
			if ("true".equalsIgnoreCase(errorExport))
			{
				this.modelExport.setErrorExport(true);
			}
			this.value = new Object[]{exportName, this.modelExport};
			return this.modelExport;
		}

	}

}