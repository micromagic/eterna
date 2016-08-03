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

import java.sql.Connection;
import java.sql.SQLException;

import org.dom4j.Element;

/**
 * 数据库操作的描述.
 */
public interface OptDesc
{
	/**
	 * 获取操作的定义.
	 */
	Element getElement();

	/**
	 * 执行描述所定义的数据库操作.
	 */
	void exec(Connection conn) throws SQLException;

}
