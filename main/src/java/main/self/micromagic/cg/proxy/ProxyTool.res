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
# 代理方法的定义
## methodProxy.invoke.declare
public Object invoke(Object target, Object[] args)
		throws Throwable

# 检查目标对象的类型
## methodProxy.check.target
if (target == null)
{
	throw new NullPointerException("The param target is null.");
}
if (!(target instanceof ${type}))
{
	throw new IllegalArgumentException("The param target (" + target.getClass() + ") isn't wanted type.");
}

# 检查目标参数的个数
## methodProxy.check.args
if (args == null)
{
	throw new NullPointerException("The param args is null.");
}
if (args.length != ${paramCount})
{
	throw new IllegalArgumentException("Wrong number of arguments " + args.length + ", wanted ${paramCount}.");
}

# 目标方法的调用 无返回值
## methodProxy.doInvoke.void
${target}.${method}(${params});
return null;

# 目标方法的调用 基本类型
## methodProxy.doInvoke.primitive
return new ${wrapType}(${target}.${method}(${params}));

# 目标方法的调用
## methodProxy.doInvoke
return ${target}.${method}(${params});

# 参数的强制类型转换 带定义
## methodProxy.param.cast.withDeclare
${type} param${index} = (${type}) args[${index}];

# 参数的强制类型转换 带检查
## methodProxy.param.cast.withCheck
if (args[${index}] != null && !(args[${index}] instanceof ${type}))
{
	throw new IllegalArgumentException("The arg${index} (" + args[${index}].getClass() + ") isn't wanted type.");
}
${type} param${index} = (${type}) args[${index}];

# 参数的强制类型转换 基本类型 带定义
## methodProxy.param.cast.primitive.withDeclare
${type} param${index} = ((${wrapType}) args[${index}]).${type}Value();

# 参数的强制类型转换 基本类型 带检查
## methodProxy.param.cast.primitive.withCheck
if (args[${index}] == null)
{
	throw new NullPointerException("The arg${index} is null.");
}
${type} param${index};
${primitiveTypeCheck}
else
{
	throw new IllegalArgumentException("The arg${index} (" + args[${index}].getClass() + ") isn't wanted type.");
}

# 参数的强制类型转换 基本类型 带检查 分段检查部分
## methodProxy.param.cast.primitive.withCheck.doCheck
${elseKey}if (args[${index}] instanceof ${wrapType})
{
	param${index} = ((${wrapType}) args[${index}]).${tempType}Value();
}

