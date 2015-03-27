## myTest
test:config
injectSub:{reader}
log:{$}
method:{setAttribute,id,$body(attr=text,i=1)}


## newPSQL
prepared_sql
injectSub:{query}
attr:{$body:preparedSQL}