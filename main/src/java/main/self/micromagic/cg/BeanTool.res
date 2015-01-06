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
# 获取map中的数据
## mapSet.getMapValue
${tmpStr} = ${prefixName}.length() == 0 ? "${pName}" : ${prefixName} + "${pName}";
${tmpObjName} = ${mapName}.get(${tmpStr});

# 检查数据是否为字符串数组且只有一个元素, 是的话取第一个字符串
## getFirstValue
if (${tmpObjName} != null && ${tmpObjName} instanceof String[])
{
	String[] arr = (String[]) ${tmpObjName};
	if (arr.length == 1)
	{
		${tmpObjName} = arr[0];
	}
}


# 下面是设置属性值的代码

# 对基本类型通过属性进行设置
## mapSet.primitiveFieldSet
if (${tmpObjName} != null)
{
	try
	{
		${declareType} v = ((${converterType}) ${converterName}).${converterMethod}(${tmpObjName});
		${beanName}.${fieldName} = v;
		${settedCountName}++;
	}
	catch (Throwable ex) {}
}

# 对基本类型通过方法进行设置
## mapSet.primitiveMethodSet
if (${tmpObjName} != null)
{
	try
	{
		${declareType} v = ((${converterType}) ${converterName}).${converterMethod}(${tmpObjName});
		${beanName}.${methodName}(v);
		${settedCountName}++;
	}
	catch (Throwable ex) {}
}

# 对bean类型通过属性进行设置
## mapSet.beanTypeFieldSet
if (${tmpObjName} != null)
{
	if (${tmpObjName} instanceof ${className})
	{
		${beanName}.${fieldName} = (${className}) ${tmpObjName};
		${settedCountName}++;
	}
	else if (${tmpObjName} instanceof Map)
	{
		${className} tb = new ${className}();
		int tempCount = BeanTool.setBeanValues(tb, (Map) ${tmpObjName});
		if (tempCount > 0)
		{
			${beanName}.${fieldName} = tb;
			${settedCountName} += tempCount;
		}
	}
}
else
{
	${className} tb = new ${className}();
	int tempCount = BeanTool.setBeanValues(tb, ${mapName}, ${prefixName} + "${pName}.");
	if (tempCount > 0)
	{
		${beanName}.${fieldName} = tb;
		${settedCountName} += tempCount;
	}
}

# 对bean类型通过方法进行设置
## mapSet.beanTypeMethodSet
if (${tmpObjName} != null)
{
	if (${tmpObjName} instanceof ${className})
	{
		${beanName}.${methodName}((${className}) ${tmpObjName});
		${settedCountName}++;
	}
	else if (${tmpObjName} instanceof Map)
	{
		${className} tb = new ${className}();
		int tempCount = BeanTool.setBeanValues(tb, (Map) ${tmpObjName});
		if (tempCount > 0)
		{
			${beanName}.${methodName}(tb);
			${settedCountName} += tempCount;
		}
	}
}
else
{
	${className} tb = new ${className}();
	int tempCount = BeanTool.setBeanValues(tb, ${mapName}, ${prefixName} + "${pName}.");
	if (tempCount > 0)
	{
		${beanName}.${methodName}(tb);
		${settedCountName} += tempCount;
	}
}

# 对可转换的类型通过属性进行设置
## convertTypeFieldSet
if (${tmpObjName} != null)
{
	try
	{
		Object tObj = ${converterName}.convert(${tmpObjName});
		if (tObj != null)
		{
			${beanName}.${fieldName} = (${className}) tObj;
			${settedCountName}++;
		}
	}
	catch (Throwable ex) {}
}

# 对可转换的类型通过方法进行设置
## convertTypeMethodSet
if (${tmpObjName} != null)
{
	try
	{
		Object tObj = ${converterName}.convert(${tmpObjName});
		if (tObj != null)
		{
			${beanName}.${methodName}((${className}) tObj);
			${settedCountName}++;
		}
	}
	catch (Throwable ex) {}
}

# 对其他无法转换的类型通过属性进行设置
## otherTypeFieldSet
if (${tmpObjName} != null)
{
	if (${tmpObjName} instanceof ${className})
	{
		${beanName}.${fieldName} = (${className}) ${tmpObjName};
		${settedCountName}++;
	}
}

# 对其他无法转换的类型通过方法进行设置
## otherTypeMethodSet
if (${tmpObjName} != null)
{
	if (${tmpObjName} instanceof ${className})
	{
		${beanName}.${methodName}((${className}) ${tmpObjName});
		${settedCountName}++;
	}
}


# BeanMap对bean类型进行转换
## beanMap.convertBeanType
${className} ${tempItemName} = null;
if (${tmpObjName} != null)
{
	if (${tmpObjName} instanceof ${className})
	{
		${tempItemName} = (${className}) ${tmpObjName};
		${settedCountName}++;
	}
	else if (${tmpObjName} instanceof Map)
	{
		if (${oldValueName} != null && ${oldValueName} instanceof ${className})
		{
			${tempItemName} = (${className}) ${oldValueName};
		}
		else
		{
			${tempItemName} = new ${className}();
		}
		int tempCount = BeanTool.getBeanMap(${tempItemName}).setValues((Map) ${tmpObjName});
		if (tempCount > 0)
		{
			${settedCountName} += tempCount;
		}
		else
		{
			${tempItemName} = null;
		}
	}
	else if (${tmpObjName} instanceof ResultRow)
	{
		if (${oldValueName} != null && ${oldValueName} instanceof ${className})
		{
			${tempItemName} = (${className}) ${oldValueName};
		}
		else
		{
			${tempItemName} = new ${className}();
		}
		int tempCount = BeanTool.getBeanMap(${tempItemName}).setValues((ResultRow) ${tmpObjName});
		if (tempCount > 0)
		{
			${settedCountName} += tempCount;
		}
		else
		{
			${tempItemName} = null;
		}
	}
	else if (BeanTool.checkBean(${tmpObjName}.getClass()))
	{
		if (${oldValueName} != null && ${oldValueName} instanceof ${className})
		{
			${tempItemName} = (${className}) ${oldValueName};
		}
		else
		{
			${tempItemName} = new ${className}();
		}
		BeanMap _beanMap = BeanTool.getBeanMap(${tmpObjName});
		_beanMap.setBean2Map(true);
		int tempCount = BeanTool.getBeanMap(${tempItemName}).setValues(_beanMap);
		if (tempCount > 0)
		{
			${settedCountName} += tempCount;
		}
		else
		{
			${tempItemName} = null;
		}
	}
}
if (${tempItemName} == null)
{
	if (${originObjName} instanceof Map)
	{
		if (${oldValueName} != null && ${oldValueName} instanceof ${className})
		{
			${tempItemName} = (${className}) ${oldValueName};
		}
		else
		{
			${tempItemName} = new ${className}();
		}
		int tempCount = BeanTool.getBeanMap(${tempItemName}, ${prefixName} + "${pName}.")
				.setValues((Map) ${originObjName});
		if (tempCount > 0)
		{
			${settedCountName} += tempCount;
		}
		else
		{
			${tempItemName} = null;
		}
	}
	else if (${originObjName} instanceof ResultRow)
	{
		if (${oldValueName} != null && ${oldValueName} instanceof ${className})
		{
			${tempItemName} = (${className}) ${oldValueName};
		}
		else
		{
			${tempItemName} = new ${className}();
		}
		int tempCount = BeanTool.getBeanMap(${tempItemName}, ${prefixName} + "${pName}.")
				.setValues((ResultRow) ${originObjName});
		if (tempCount > 0)
		{
			${settedCountName} += tempCount;
		}
		else
		{
			${tempItemName} = null;
		}
	}
}

# BeanMap对bean类型通过属性进行设置
## beanMap.beanTypeFieldSet
if (${tempItemName} != null)
{
	${beanName}.${fieldName} = ${tempItemName};
}

# BeanMap中对bean类型通过方法进行设置
## beanMap.beanTypeMethodSet
if (${tempItemName} != null)
{
	${beanName}.${methodName}(${tempItemName});
}


# BeanMap中对基本类型通过属性进行设置
## beanMap.primitiveFieldSet
try
{
	${declareType} v = ((${converterType}) ${converterName}).${converterMethod}(${tmpObjName});
	${beanName}.${fieldName} = v;
	${settedCountName}++;
}
catch (Throwable ex) {}

# BeanMap中对基本类型通过方法进行设置
## beanMap.primitiveMethodSet
try
{
	${declareType} v = ((${converterType}) ${converterName}).${converterMethod}(${tmpObjName});
	${beanName}.${methodName}(v);
	${settedCountName}++;
}
catch (Throwable ex) {}

# BeanMap中对数组类型通过属性进行设置
## beanMap.arrayTypeFieldSet
if (${tmpObjName} != null)
{
	if (${oldValueName} != null && ${oldValueName} instanceof ${className})
	{
		${beanName}.${fieldName} = (${className}) ArrayTool.convertArray(
				${arrayLevel}, ${cellClass}.class, ${tmpObjName}, ${oldValueName}, ${beanMapName});
	}
	else
	{
		${beanName}.${fieldName} = (${className}) ArrayTool.convertArray(
				${arrayLevel}, ${cellClass}.class, ${tmpObjName}, null, ${beanMapName});
	}
}

# BeanMap中对数组类型通过方法进行设置
## beanMap.arrayTypeMethodSet
if (${tmpObjName} != null)
{
	if (${oldValueName} != null && ${oldValueName} instanceof ${className})
	{
		${beanName}.${methodName}((${className}) ArrayTool.convertArray(
				${arrayLevel}, ${cellClass}.class, ${tmpObjName}, ${oldValueName}, ${beanMapName}));
	}
	else
	{
		${beanName}.${methodName}((${className}) ArrayTool.convertArray(
				${arrayLevel}, ${cellClass}.class, ${tmpObjName}, null, ${beanMapName}));
	}
}


# 下面是获取属性值的代码

# 对基本类型通过属性进行获取
## primitiveFieldGet
return new ${wrapName}(${beanName}.${fieldName});

# 对基本类型通过方法进行获取
## primitiveMethodGet
return new ${wrapName}(${beanName}.${methodName}());

# 对其他类型通过属性进行获取
## otherTypeFieldGet
Object tmpResult = ${beanName}.${fieldName};
if (tmpResult != null && ${cellDescriptor}.isBeanType() && ${beanMap}.isBean2Map())
{
	BeanMap _beanMap = BeanTool.getBeanMap(tmpResult);
	_beanMap.setBean2Map(true);
	tmpResult = _beanMap;
}
return tmpResult;

# 对其他类型通过方法进行获取
## otherTypeMethodGet
Object tmpResult = ${beanName}.${methodName}();
if (tmpResult != null && ${cellDescriptor}.isBeanType() && ${beanMap}.isBean2Map())
{
	BeanMap _beanMap = BeanTool.getBeanMap(tmpResult);
	_beanMap.setBean2Map(true);
	tmpResult = _beanMap;
}
return tmpResult;

# 对数组类型通过属性进行获取
## arrayTypeFieldGet
if (${indexs} == null || ${indexs}.length == 0)
{
	Object tmpResult = ${beanName}.${fieldName};
	if (tmpResult != null && ${cellDescriptor}.isArrayBeanType() && ${beanMap}.isBean2Map())
	{
		tmpResult = ArrayTool.beanArray2Map(${arrayLevel}, tmpResult);
	}
	return tmpResult;
}
else
{
	return this.processerArr[${indexs}.length - 1].getBeanValue(${cellDescriptor}, ${indexs},
			${beanName}.${fieldName}, ${prefixName}, ${beanMap});
}

# 对数组类型通过方法进行获取
## arrayTypeMethodGet
if (${indexs} == null || ${indexs}.length == 0)
{
	Object tmpResult = ${beanName}.${methodName}();
	if (tmpResult != null && ${cellDescriptor}.isArrayBeanType() && ${beanMap}.isBean2Map())
	{
		tmpResult = ArrayTool.beanArray2Map(${arrayLevel}, tmpResult);
	}
	return tmpResult;
}
else
{
	return this.processerArr[${indexs}.length - 1].getBeanValue(${cellDescriptor}, ${indexs},
			${beanName}.${methodName}(), ${prefixName}, ${beanMap});
}


# 对集合容器类型通过属性进行获取
## collectionTypeFieldGet
if (${indexs} == null || ${indexs}.length == 0)
{
	return ${beanName}.${fieldName};
}
else
{
	Collection c = ${beanName}.${fieldName};
	${getCollectionIndexCode}
	return null;
}

# 对集合容器类型通过方法进行获取
## collectionTypeMethodGet
if (${indexs} == null || ${indexs}.length == 0)
{
	return ${beanName}.${methodName}();
}
else
{
	Collection c = ${beanName}.${methodName}();
	${getCollectionIndexCode}
	return null;
}

# 对集合容器类型中的值进行获取(集合容器的变量名为 c)
## collectionTypeIndexGet
if (c == null)
{
	return null;
}
if (c instanceof List)
{
	return ((List) c).get(${indexs}[0]);
}
else
{
	Iterator itr = c.iterator();
	int tmpI = 0;
	for (; itr.hasNext() && tmpI < ${indexs}[0]; tmpI++, itr.next());
	if (tmpI == ${indexs}[0] && itr.hasNext())
	{
		return itr.next();
	}
}

# 对基本类型的数组进行获取
## arrayTypePrimitiveGet
return new ${wrapName}(${arrayName}${arrayVisitList});

# 对其他类型的数组进行获取
## arrayTypeOtherGet
return ${arrayName}${arrayVisitList};



# 数组转换中数组的for循环代码片段
## array2array_for
for (int i${levelIndex} = 0; i${levelIndex} < ${src}${levelIndex}.length; i${levelIndex}++)

# 数组转换中判断数组的元素是否为null代码片段
## array2array_if
if (${src}${levelIndex}[i${levelIndex}] != null)

# 数组转换中对数组中间段进行赋值的代码片断
## array2array_def
${srcType}[]${srcArrayDef} ${src}${nextIndex} = ${src}${levelIndex}[i${levelIndex}];
${destType}[]${destArrayDef} ${dest}${nextIndex} = ${dest}${levelIndex}[i${levelIndex}] = new ${destType}[${src}${nextIndex}.length]${destArrayDef};

# 数组转换中对数组中间段进行赋值的代码片断 包含对目标数组长度的判断
## array2array_def_withDest
${srcType}[]${srcArrayDef} ${src}${nextIndex} = ${src}${levelIndex}[i${levelIndex}];
${destType}[]${destArrayDef} ${dest}${nextIndex} = ${dest}${levelIndex}[i${levelIndex}];
if (${dest}${nextIndex} == null)
{
	${dest}${nextIndex} = ${dest}${levelIndex}[i${levelIndex}] = new ${destType}[${src}${nextIndex}.length]${destArrayDef};
}
else if (${dest}${nextIndex}.length < ${src}${nextIndex}.length)
{
	${dest}${levelIndex}[i${levelIndex}] = new ${destType}[${src}${nextIndex}.length]${destArrayDef};
	System.arraycopy(${dest}${levelIndex}[i${levelIndex}], 0, ${dest}${nextIndex}, 0, ${dest}${nextIndex}.length);
	${dest}${nextIndex} = ${dest}${levelIndex}[i${levelIndex}];
}


# 判断是否为基本类型的数组类型并转换成外覆类型
## checkAndConvertPrimitiveArrayType
if (${arrayObj} == null)
{
	return null;
}
String cName = ${arrayObj}.getClass().getName();
int length = cName.length();
if (length == ${arrayLevel} + 1)
{
	char flag = cName.charAt(${arrayLevel});
	if (flag != ';')
	{
		if (flag < 'I')
		{
			// B byte C char F float D dougle
			if (flag == 'D')
			{
				double${arrayDef} tmpArr = (double${arrayDef}) ${arrayObj};
				return this.wrapArray(tmpArr);
			}
			else if (flag == 'B')
			{
				byte${arrayDef} tmpArr = (byte${arrayDef}) ${arrayObj};
				return this.wrapArray(tmpArr);
			}
			else if (flag == 'C')
			{
				char${arrayDef} tmpArr = (char${arrayDef}) ${arrayObj};
				return this.wrapArray(tmpArr);
			}
			else if (flag == 'F')
			{
				float${arrayDef} tmpArr = (float${arrayDef}) ${arrayObj};
				return this.wrapArray(tmpArr);
			}
		}
		else
		{
			// I int J long S short Z boolean
			if (flag == 'I')
			{
				int${arrayDef} tmpArr = (int${arrayDef}) ${arrayObj};
				return this.wrapArray(tmpArr);
			}
			else if (flag == 'Z')
			{
				boolean${arrayDef} tmpArr = (boolean${arrayDef}) ${arrayObj};
				return this.wrapArray(tmpArr);
			}
			else if (flag == 'J')
			{
				long${arrayDef} tmpArr = (long${arrayDef}) ${arrayObj};
				return this.wrapArray(tmpArr);
			}
			else if (flag == 'S')
			{
				short${arrayDef} tmpArr = (short${arrayDef}) ${arrayObj};
				return this.wrapArray(tmpArr);
			}
		}
	}
}
return null;

# 将数组转换成需要的数组类型
## convertArrayType
if (${arrayObj} instanceof ${destType}${destArrayDef})
{
	return ${arrayObj};
}
else if (${arrayObj} instanceof Object${srcArrayDef})
{
	return this.convertArray((Object${srcArrayDef}) ${arrayObj}, ${converter}, ${needThrow});
}
else
{
	Object${srcArrayDef} tmpArr = (Object${srcArrayDef}) ArrayTool.wrapPrimitiveArray(${arrayLevel}, ${arrayObj});
	if (tmpArr != null)
	{
		return this.convertArray(tmpArr, ${converter}, ${needThrow});
	}
}
return null;

# 将数组转换成需要的数组类型 需要将元素值赋值到目标数组中
## convertArrayType.withDest
if (${arrayObj} instanceof ${destType}${destArrayDef} && ${destArr} == null)
{
	// 如果${destArr}不为null, 则会在后面两个else子句中执行
	return ${arrayObj};
}
else if (${arrayObj} instanceof Object${srcArrayDef})
{
	return this.convertArray((Object${srcArrayDef}) ${arrayObj}, (${destType}${destArrayDef}) ${destArr}, ${converter}, ${needThrow});
}
else
{
	Object${srcArrayDef} tmpArr = (Object${srcArrayDef}) ArrayTool.wrapPrimitiveArray(${arrayLevel}, ${arrayObj});
	if (tmpArr != null)
	{
		return this.convertArray(tmpArr, (${destType}${destArrayDef}) ${destArr}, ${converter}, ${needThrow});
	}
}
if (${needThrow})
{
	return null;
}
return ${destArr};

# 数组中将基本类型转换成外覆类型
## arrayCell.convert.wrap
${dest}${levelIndex}[i${levelIndex}] = new ${wrapType}(${src}${levelIndex}[i${levelIndex}]);

# 数组中的元素类型转换成基本类型
## arrayCell.convert.primitive
if (${converter} == null)
{
	${dest}${levelIndex}[i${levelIndex}] = ((${converterType}) BeanTool.getConverter(${vcIndex}))
			.${converterMethod}(${src}${levelIndex}[i${levelIndex}]);
}
else if (${converter} instanceof BeanMap)
{
	${dest}${levelIndex}[i${levelIndex}] = ((${converterType}) ((BeanMap) ${converter}).getConverter(${vcIndex}))
			.${converterMethod}(${src}${levelIndex}[i${levelIndex}]);
}
else
{
	${dest}${levelIndex}[i${levelIndex}] = ((${converterType}) ${converter}).${converterMethod}(${src}${levelIndex}[i${levelIndex}]);
}

# 数组中的元素类型通过convert进行转换
## arrayCell.convert.byTool
if (${converter} == null)
{
	${dest}${levelIndex}[i${levelIndex}] = (${cellType}) BeanTool.getConverter(${vcIndex})
			.convert(${src}${levelIndex}[i${levelIndex}]);
}
else if (${converter} instanceof BeanMap)
{
	${dest}${levelIndex}[i${levelIndex}] = (${cellType}) ((BeanMap) ${converter}).getConverter(${vcIndex})
			.convert(${src}${levelIndex}[i${levelIndex}]);
}
else
{
	${dest}${levelIndex}[i${levelIndex}] = (${cellType}) ((ValueConverter) ${converter})
			.convert(${src}${levelIndex}[i${levelIndex}]);
}

# 数组中的元素类型转换成bean类型
## arrayCell.convert.bean
Object tmpObj = ${src}${levelIndex}[i${levelIndex}];
if (tmpObj instanceof ${cellType})
{
	if (${dest}${levelIndex}[i${levelIndex}] == null)
	{
		${dest}${levelIndex}[i${levelIndex}] = (${cellType}) tmpObj;
	}
	else
	{
		${cellType} tb = ${dest}${levelIndex}[i${levelIndex}];
		BeanMap _beanMap = BeanTool.getBeanMap(tmpObj);
		_beanMap.setBean2Map(true);
		BeanTool.getBeanMap(tb).setValues(_beanMap);
		${dest}${levelIndex}[i${levelIndex}] = tb;
	}
}
else if (tmpObj instanceof Map)
{
	${cellType} tb = ${dest}${levelIndex}[i${levelIndex}] == null ? new ${cellType}() : ${dest}${levelIndex}[i${levelIndex}];
	BeanTool.getBeanMap(tb).setValues((Map) tmpObj);
	${dest}${levelIndex}[i${levelIndex}] = tb;
}
else if (tmpObj instanceof ResultRow)
{
	${cellType} tb = ${dest}${levelIndex}[i${levelIndex}] == null ? new ${cellType}() : ${dest}${levelIndex}[i${levelIndex}];
	BeanTool.getBeanMap(tb).setValues((ResultRow) tmpObj);
	${dest}${levelIndex}[i${levelIndex}] = tb;
}
else if (BeanTool.checkBean(tmpObj.getClass()))
{
	${cellType} tb = ${dest}${levelIndex}[i${levelIndex}] == null ? new ${cellType}() : ${dest}${levelIndex}[i${levelIndex}];
	BeanMap _beanMap = BeanTool.getBeanMap(tmpObj);
	_beanMap.setBean2Map(true);
	BeanTool.getBeanMap(tb).setValues(_beanMap);
	${dest}${levelIndex}[i${levelIndex}] = tb;
}
else if (${converter} instanceof ValueConverter)
{
	if (${dest}${levelIndex}[i${levelIndex}] == null)
	{
		${dest}${levelIndex}[i${levelIndex}] = (${cellType}) ((ValueConverter) ${converter}).convert(tmpObj);
	}
	else
	{
		${cellType} tb = ${dest}${levelIndex}[i${levelIndex}];
		if ((tmpObj = ((ValueConverter) ${converter}).convert(tmpObj)) != null)
		{
			BeanMap _beanMap = BeanTool.getBeanMap(tmpObj);
			_beanMap.setBean2Map(true);
			BeanTool.getBeanMap(tb).setValues(_beanMap);
			${dest}${levelIndex}[i${levelIndex}] = tb;
		}
	}
}
else if (${needThrow} && tmpObj != null)
{
	throw new ClassCastException("Can't cast [" + tmpObj + "](" + tmpObj.getClass() + ") to (${cellType}).");
}

# 数组中的元素类型转换成map
## arrayCell.convert.map
BeanMap _beanMap = BeanTool.getBeanMap(${src}${levelIndex}[i${levelIndex}]);
_beanMap.setBean2Map(true);
${dest}${levelIndex}[i${levelIndex}] = _beanMap;

# 数组中的元素类型对无法转换的类型进行判断并转换
## arrayCell.convert.other
Object tmpObj = ${src}${levelIndex}[i${levelIndex}];
if (tmpObj instanceof ${cellType})
{
	${dest}${levelIndex}[i${levelIndex}] = (${cellType}) tmpObj;
}
else if (${converter} instanceof ValueConverter)
{
	${dest}${levelIndex}[i${levelIndex}] = (${cellType}) ((ValueConverter) ${converter}).convert(tmpObj);
}
else if (${needThrow} && tmpObj != null)
{
	throw new ClassCastException("Can't cast [" + tmpObj + "](" + tmpObj.getClass() + ") to (${cellType}).");
}

