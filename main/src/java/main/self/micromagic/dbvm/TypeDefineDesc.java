/*
 * Copyright 2015 xinjunli (micromagic@sina.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package self.micromagic.dbvm;

import self.micromagic.eterna.share.EternaFactory;

/**
 * 类型定义的描述信息.
 */
public class TypeDefineDesc
{
	public TypeDefineDesc()
	{
	}
	public TypeDefineDesc(String constName)
	{
		this.constName = constName;
	}

	public void init(EternaFactory factory)
	{
		this.define = factory.getConstantValue(this.constName);
	}
	protected String define;

	public void setConstName(String constName)
	{
		this.constName = constName;
	}
	protected String constName;

	/**
	 * 获取类型定义的常量名.
	 */
	public String getConstName()
	{
		return this.constName;
	}

	/**
	 * 获取类型的定义.
	 */
	public String getDefine(int type)
	{
		return this.define;
	}

}
