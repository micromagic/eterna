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
## root
eterna-config
sub:{factory}


## factory
factory
create:{type,${factory},1}
sub:{objs,attributes}


## attributes
attributes
sub:{attribute}


## attribute
attribute
method:{setAttribute,name,$body(attr=value,i=1)}


## objs
objs
sub:{query,format}


## format
format
same:{name}
log:{name}
create:{generator,${format}}
attr:{name,type,$body(attr=pattern,body=pattern,m=0):pattern}
sub:{attribute}
stack:{addFormat,n:1,g:1}


## query
query
same:{name}
log:{name}
create:{generator,${query}}
attr:{name,logType(m=0,i=0),forwardOnly(m=0,i=0),orderIndex(m=0,i=0),$body(body=prepared-sql):preparedSQL}
sub:{readers,attribute}
stack:{registerQueryAdapter,n:0,g:0}


## readers
readers
attr:{baseReaderManager(m=0):readerManagerName,readerOrder(m=0,i=0)}
sub:{reader}


## reader
reader
log:{$E}
create:{generator,${reader}}
attr:{
	name,colName(m=0):columnName,colIndex(m=0,i=0):columnIndex,format(m=0):formatName,
	orderName(m=0),caption(m=0),width(m=0,i=0),permissions(m=0),htmlFilter(d=true,i=0),
	type(m=0),visible(d=true,i=0)
}
sub:{attribute}
stack:{addResultReader,n:0,g:1}



