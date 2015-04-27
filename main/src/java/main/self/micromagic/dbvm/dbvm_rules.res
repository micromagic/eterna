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
## dbVersion
dbVersion
log:{$dbVersion}
create:{,${factory},1}
sub:{config,table,index,data}


## data
data
log:{$data}
create:{,self.micromagic.dbvm.DataDesc}
attr:{$body:script,$serial(pattern=DBVM000000):name}
stack:{registerObject,n:0,g:0}


## table
table
log:{$table}
create:{,self.micromagic.dbvm.TableDesc}
attr:{
	$serial(pattern=DBVM000000):name,name(i=0):tableName,newName(m=0),
	opt(m=0,i=0):optName,desc(m=0,i=0)
}
sub:{tableColumn}
stack:{registerObject,n:0,g:0}


## tableColumn
column
log:{$column}
create:{,self.micromagic.dbvm.ColumnDesc}
attr:{name(i=0):colName,newName(m=0),opt(m=0,i=0):optName,desc(m=0,i=0),type(i=0):typeName}
stack:{addColumn,n:0,g:0}


## index
index
log:{$index}
create:{,self.micromagic.dbvm.IndexDesc}
attr:{
	$serial(pattern=DBVM000000):name,name(i=0):indexName,tableName(i=0),
	opt(m=0,i=0):optName,desc(m=0,i=0),type(m=0,i=0)
}
sub:{indexColumn}
stack:{registerObject,n:0,g:0}


## indexColumn
column
method:{addColumn,name(i=0)}


## config
config
sub:{column-def,index-def,type-def}


## column-def
column-def
log:{name}
create:{className}
attr:{$text.columnDefiner:name}
stack:{registerObject,n:0,g:0}


## index-def
index-def
log:{name}
create:{className}
attr:{$text.indexDefiner:name}
stack:{registerObject,n:0,g:0}


## type-def
type-def
log:{name}
create:{className}
attr:{$text.typeDefiner:name}
sub:{type-desc}
stack:{registerObject,n:0,g:0}


## type-desc
type-desc
create:{className}
attr:{constName(i=0)}
stack:{modifyTypeDefineDesc,n:0,g:0}
