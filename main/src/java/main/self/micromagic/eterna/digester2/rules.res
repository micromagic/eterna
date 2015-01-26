# Copyright 2009-2015 xinjunli (micromagic@sina.com).
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
sub:{objs,factoryAttributes}


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
sub:{query,update,format,prepare,entity,dataPrinter}


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
attr:{name,type}
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
attr:{name,logType(m=0,i=0),forwardOnly(m=0,i=0),orderIndex(m=0,i=0),$body(body=prepared-sql):preparedSQL}
sub:{readers,parameters,attribute}
stack:{registerObject,n:0,g:0}


## update
update
same:{name}
log:{name}
create:{generator,${update}}
attr:{name,logType(m=0,i=0),$body(body=prepared-sql):preparedSQL}
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
	colName(m=0):columnName,caption(m=0),permissions(m=0),type(m=0,i=0)
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
attr:{name,colName(m=0):columnName,type(m=0,i=0):paramType,prepare(m=0):prepareName}
sub:{attribute}
stack:{addParameter,n:0,g:0}


## dataPrinter
data-printer
same:{name}
log:{name}
create:{generator}
attr:{name}
stack:{registerObject,n:0,g:0}
