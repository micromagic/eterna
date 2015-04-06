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

