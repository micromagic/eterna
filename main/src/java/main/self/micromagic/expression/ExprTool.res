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
# 单目运算
## oneOpt_num
public Object exec(Object num1, Object num2)
{
	${type} tmp = ((Number) num1).${type}Value();
	tmp = ${opt}(tmp);
	return new ${wrapType}(tmp);
}

# 双目运算
## twoOpt_num
public Object exec(Object num1, Object num2)
{
	${type} tmp1 = ((Number) num1).${type}Value();
	${type} tmp2 = ((Number) num2).${type}Value();
	return new ${wrapType}(tmp1 ${opt} tmp2);
}

# 比较运算
## compareOpt_num
public Object exec(Object num1, Object num2)
{
	${type} tmp1 = ((Number) num1).${type}Value();
	${type} tmp2 = ((Number) num2).${type}Value();
	return tmp1 ${opt} tmp2 ? Boolean.TRUE : Boolean.FALSE;
}

# 方法运算
## methodOpt_num
public Object exec(Object num1, Object num2)
{
	${type} tmp1 = ((Number) num1).${type}Value();
	${type} tmp2 = ((Number) num2).${type}Value();
	return new ${wrapType}(${opt}(tmp1, tmp2));
}


