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

package self.micromagic.dbvm.impl;

import self.micromagic.dbvm.TypeDefineDesc;
import self.micromagic.eterna.share.TypeManager;
import self.micromagic.util.ref.IntegerRef;

/**
 * oracle的浮点型定义.
 */
public class OracleDouble extends TypeDefineDesc
{
	/**
	 * 获取类型的定义.
	 */
	public String getDefine(int type)
	{
		IntegerRef sub = new IntegerRef();
		int ext = TypeManager.getTypeExtend(type, sub);
		if (ext > 0)
		{
			if (sub.value > 0)
			{
				return this.define.concat("(" + ext + "," + sub + ")");
			}
			return this.define.concat("(" + ext + ")");
		}
		return this.define;
	}

}