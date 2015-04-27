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

import self.micromagic.eterna.share.EternaObject;
import self.micromagic.util.ref.ObjectRef;

/**
 * 数据库索引的定义者.
 */
public interface IndexDefiner extends EternaObject
{
	/**
	 * 获取索引的定义.
	 *
	 * @param indexDesc    列的描述信息
	 * @param param      出参
	 * @return  列定义字符串
	 */
	String getIndexDefine(IndexDesc indexDesc, ObjectRef param);

}
