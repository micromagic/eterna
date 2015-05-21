# Copyright 2015 xinjunli (micromagic@sina.com).
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
# @author micromagic@sina.com
#
#
# 根节点
## eterna-config
eterna-config
sub:{factory}


## factory
factory
log:{$factory}
create:{type,${factory},1}
sub:{include,objs,permissionSet,factoryAttributes}


## include
include
log:{$include}
create:{,${include},0}
attr:{src}
sub:{includeParam}
method:{registerInclude}


## includeParam
param
log:{$include.param}
method:{addParam,name(i=0),$body(attr=value)}


## permissionSet
permission-set
log:{$permissionSet}
create:{className}
stack:{setPermissionSetCreater,n:0,g:0}


## factoryAttributes
attributes
log:{$factory.attribute}
sub:{attribute}


## attributes
attributes
sub:{attribute}


## attribute
attribute
log:{$}
method:{setAttribute,name,$body(attr=value,i=1)}


## objs
objs
sub:{
	query,update,format,prepare,entity,constant,daoLogger,
	search,builderList,builder,
	model,export,dataPrinter,typicalComponent,view,function,resource
}


## daoLogger
dao-logger
same:{name}
log:{name}
create:{generator}
attr:{name}
stack:{registerObject,n:0,g:0}


## constant
constant
same:{name}
log:{name}
create:{generator,${constant}}
attr:{name,$body(attr=value):value}
stack:{registerObject,n:0,g:1}


## format
format
same:{name}
log:{name}
create:{generator,${format}}
attr:{name,type,$body(attr=pattern,body=pattern,m=0):pattern}
sub:{attribute}
stack:{registerObject,n:0,g:1}


## prepare
prepare
same:{name}
log:{name}
create:{generator,${prepare}}
attr:{name,type,$body(attr=pattern,body=pattern,m=0):pattern}
sub:{attribute}
stack:{registerObject,n:0,g:1}


## entity
entity
same:{name}
log:{name}
create:{generator,${entity}}
attr:{name,order(m=0,i=0)}
sub:{attributes,item,entityRef}
stack:{registerObject,n:0,g:1}


## item
item
log:{$}
create:{generator,${entityItem}}
attr:{name,colName(m=0):columnName,type(m=0,i=0),caption(m=0),permission(m=0)}
sub:{attribute}
stack:{addItem,n:0,g:1}


## entityRef
entity-ref
${entityRefConfig}
stack:{addEntityRef,n:0,g:0}


## query
query
same:{name}
log:{name}
create:{generator,${query}}
attr:{
	name,logType(m=0,i=0):logTypeName,forwardOnly(m=0,i=0),
	orderIndex(m=0,i=0),$body(body=prepared-sql,m=0):preparedSQL
}
sub:{readers,parameters,attribute}
stack:{registerObject,n:0,g:0}


## update
update
same:{name}
log:{name}
create:{generator,${update}}
attr:{name,logType(m=0,i=0):logTypeName,$body(body=prepared-sql,m=0):preparedSQL}
sub:{parameters,attribute}
stack:{registerObject,n:0,g:0}


## readers
readers
attr:{readerOrder(m=0,i=0)}
sub:{reader,entityRefReader}


## entityRefReader
entity-ref
${entityRefConfig}
stack:{addReaderEntityRef,n:0,g:0}


## reader
reader
log:{$}
create:{generator,${reader}}
attr:{
	name,alias(m=0),colIndex(m=0,i=0):columnIndex,format(m=0):formatName,
	colName(m=0):columnName,caption(m=0),permission(m=0),type(m=0,i=0)
}
sub:{attribute}
stack:{addResultReader,n:0,g:1}


## parameters
parameters
sub:{parameter,entityRef}


## parameter
parameter
log:{$}
create:{generator,${parameter}}
attr:{
	name,colName(m=0):columnName,type(m=0,i=0):paramType,
	prepare(m=0):prepareName,permission(m=0)
}
sub:{attribute}
stack:{addParameter,n:0,g:0}


## builder
builder
same:{name}
log:{name}
create:{generator,${builder}}
attr:{name,caption(m=0),operator(m=0),prepare(m=0)}
sub:{attribute}
stack:{registerObject,n:0,g:1}


## builderList
builder-list
same:{name}
log:{name}
create:{generator,${builderList}}
attr:{name}
sub:{builderName}
stack:{registerObject,n:0,g:0}


## builderName
builder-name
method:{addBuilder,name}


## search
search
same:{name}
log:{name}
create:{generator,${search}}
attr:{name,queryName,pageSize(m=0,i=0),countType(m=0,i=0),conditionIndex(d=1,i=0)}
sub:{otherManager,conditions,attribute}
stack:{registerObject,n:0,g:0}


## otherManager
other-search-manager
attr:{otherName(m=0):otherSearchManagerName}


## conditions
conditions
sub:{condition,entityRef}


## condition
condition
log:{$}
create:{generator,${condition}}
attr:{
	name,colName(m=0):columnName,caption(m=0):columnCaption,colType(i=0):columnType,
	prepare(m=0):prepareName,inputType(m=0):conditionInputType,defaultValue(m=0),
	permissions(m=0),useDefaultBuilder(m=0,i=0):useDefaultConditionBuilder,
	defaultBuilder(m=0):defaultConditionBuilderName,visible(d=1,i=0),
	builderList(m=0):conditionBuilderListName
}
sub:{attribute}
stack:{addConditionProperty,n:0,g:1}


## model
model
same:{name}
log:{name}
create:{generator,${model}}
attr:{
	name,needFrontModel(d=0,i=0),frontModelName(m=0),modelExportName(m=0),
	errorExportName(m=0),transactionType(m=0,i=0),dataSourceName(m=0),
	position(m=0):allowPosition
}
sub:{searchExecute,attribute}
stack:{registerObject,n:0,g:0}


## export
export
same:{name}
log:{name}
create:{generator,${export}}
attr:{
	name,viewName(m=0),modelName(m=0),path(m=0,r=1),redirect(m=0,i=0),
	errorExport(m=0,i=0)
}
stack:{registerObject,n:0,g:1}


## searchExecute
search-execute
log:{$}
create:{generator,${searchExecute}}
attr:{
	searchName,queryResultTo(m=0):queryResultName,conditionTo(m=0):searchManagerName
}
stack:{addExecute,n:0,g:1}


## queryExecute
query-execute
log:{$}
create:{generator,${queryExecute}}
attr:{
	queryName,resultTo(m=0),start(m=0,i=0),count(m=0,i=0),countType(m=0,i=0)
}
sub:{paramBind}
stack:{addExecute,n:0,g:1}


## updateExecute
update-execute
log:{$}
create:{generator,${updateExecute}}
attr:{
	updateName,resultTo(m=0),multiType(m=0,i=0)
}
sub:{paramBind}
stack:{addExecute,n:0,g:1}


## paramBind
param-bind
log:{$}
create:{generator,${paramBind}}
attr:{
	src,names(m=0),loop(m=0,i=0),subSQL(m=0,i=0)
}
stack:{addParamBind,n:0,g:1}


## transExecute
trans-execute
log:{$}
create:{generator,${transExecute}}
attr:{
	from,removeFrom(m=0,i=0),mustExist(d=1,i=0),opt(m=0),to(m=0)
}
stack:{addExecute,n:0,g:1}


## checkExecute
check-execute
log:{$}
create:{generator,${checkExecute}}
attr:{
	checkPattern,loopType(m=0,i=0),trueModelName(m=0),falseModelName(m=0)
}
stack:{addExecute,n:0,g:1}


## dataPrinter
data-printer
same:{name}
log:{name}
create:{generator}
attr:{name}
stack:{registerObject,n:0,g:0}


## view
view
same:{name}
log:{name}
create:{generator,${view}}
attr:{
	name,dataPrinterName(m=0,i=0),defaultDataType(m=0,i=0)
}
sub:{${comSub}}
stack:{registerObject,n:0,g:0}


## typicalComponent
typical-component
same:{name}
log:{name}
create:{generator,${typicalComponent}}
attr:{name}
sub:{${comSub}}
stack:{registerObject,n:0,g:0}


## component
component
log:{$}
create:{generator,${component}}
attr:{
	name(d=),type,ignoreGlobal(m=0,i=0):ignoreGlobalParam,
	$body(attr=beforeInit,body=before-init,m=0):beforeInit,
	$body(attr=initScript,body=init-script,m=0):initScript,
	$body(attr=comParam,body=component-param,m=0):componentParam
}
sub:{${comSub}}
stack:{addComponent,n:0,g:0}


## replacement
replacement
log:{$}
create:{generator,${replacement}}
attr:{
	name(d=),baseComponentName,ignoreGlobal(m=0,i=0):ignoreGlobalParam,
	$body(attr=beforeInit,body=before-init,m=0):beforeInit,
	$body(attr=initScript,body=init-script,m=0):initScript,
	$body(attr=comParam,body=component-param,m=0):componentParam
}
sub:{${comSub}}
stack:{addComponent,n:0,g:0}


##events
events
sub:{event}


## event
event
log:{$}
create:{generator,${event}}
attr:{name,scriptParam(m=0,i=0),$body(m=0):scriptBody}
stack:{addEvent,n:0,g:0}



## tr
tr
log:{$}
create:{generator,${tr}}
attr:{
	name(d=),type(d=tr),ignoreGlobal(m=0,i=0):ignoreGlobalParam,
	$body(attr=beforeInit,body=before-init,m=0):beforeInit,
	$body(attr=initScript,body=init-script,m=0):initScript,
	$body(attr=comParam,body=component-param,m=0):componentParam
}
sub:{${comSub}}
stack:{setTR,n:0,g:0}


## tableList
table-list
log:{$}
create:{generator,${tableList}}
attr:{
	name(d=),dataName(m=0),ignoreGlobal(m=0,i=0):ignoreGlobalParam,
	autoArrange(m=0,i=0),percentWidth(m=0,i=0),
	caculateWidth(m=0,i=0),caculateWidthFix(m=0,i=0),
	$body(attr=beforeInit,body=before-init,m=0):beforeInit,
	$body(attr=initScript,body=init-script,m=0):initScript,
	$body(attr=comParam,body=component-param,m=0):componentParam
}
sub:{tr,columns,events,attributes}
stack:{addComponent,n:0,g:0}


## columns
columns
attr:{columnOrder(m=0,i=0)}
sub:{column,entityRef}


## column
column
create:{generator,${column}}
attr:{
	name(d=),width(m=0),ignoreGlobal(m=0,i=0):ignoreGlobalParam,
	ignoreGlobalTitle(m=0,i=0):ignoreGlobalTitleParam
	caption(m=0),typicalComponentName(m=0),cloneInitParam(m=0,i=0),
	defaultValue(m=0,i=0),srcName(m=0),
	$body(attr=beforeInit,body=before-init,m=0):beforeInit,
	$body(attr=initScript,body=init-script,m=0):initScript,
	$body(attr=comParam,body=component-param,m=0):componentParam,
	$body(attr=titleParam,body=title-param,m=0):titleParam,
	$body(attr=initParam,body=init-param,m=0):initParam
}
sub:{${comSub}}
stack:{addColumn,n:0,g:0}


## tableForm
table-form
log:{$}
create:{generator,${tableForm}}
attr:{
	name(d=),dataName(m=0),ignoreGlobal(m=0,i=0):ignoreGlobalParam,
	autoArrange(m=0,i=0),percentWidth(m=0,i=0),columns,
	caculateWidth(m=0,i=0),caculateWidthFix(m=0,i=0),
	$body(attr=beforeInit,body=before-init,m=0):beforeInit,
	$body(attr=initScript,body=init-script,m=0):initScript,
	$body(attr=comParam,body=component-param,m=0):componentParam
}
sub:{tr,cells,events,attributes}
stack:{addComponent,n:0,g:0}


## cells
cells
attr:{cellOrder(m=0,i=0)}
sub:{cell,entityRef}


## cell
cell
create:{generator,${cell}}
attr:{
	name(d=),width(m=0),ignoreGlobal(m=0,i=0):ignoreGlobalParam,
	ignoreGlobalTitle(m=0,i=0):ignoreGlobalTitleParam,required(m=0,i=0),
	caption(m=0),typicalComponentName(m=0),needIndex(m=0,i=0),
	defaultValue(m=0,i=0),srcName(m=0),titleSize(m=0,i=0),
	containerSize(m=0,i=0),rowSpan(m=0,i=0),newRow(m=0,i=0),
	$body(attr=beforeInit,body=before-init,m=0):beforeInit,
	$body(attr=initScript,body=init-script,m=0):initScript,
	$body(attr=comParam,body=component-param,m=0):componentParam,
	$body(attr=titleParam,body=title-param,m=0):titleParam,
	$body(attr=initParam,body=init-param,m=0):initParam
}
sub:{${comSub}}
stack:{addCell,n:0,g:0}


## function
function
same:{name}
log:{name}
create:{generator,${function}}
attr:{name,param(m=0,i=0),$body(m=0):body}
stack:{registerObject,n:0,g:1}


## resource
resource
same:{name}
log:{name}
create:{generator,${resource}}
attr:{name,$body(m=0,r=1):resourceText}
stack:{registerObject,n:0,g:1}


