## myTest
test:config
injectSub:{reader}
log:{$}
method:{setAttribute,id,$body(attr=text,i=1)}


## newPSQL
prepared_sql
injectSub:{query}
attr:{$body:preparedSQL}


## newAttr
item;self.micromagic.eterna.digester2.InjectRule
map:{setAttribute,x1(m=0,c=1),x2(m=0):web.h,x3(m=0):i}


## item2
item2
injectSub:{entity}
log:{$}
create:{generator,${entityItem}}
attr:{$serial(pattern=ID00000):name,colName(m=0):columnName,type(m=0,i=0),$text.iName:caption}
stack:{addItem,n:0,g:1}


