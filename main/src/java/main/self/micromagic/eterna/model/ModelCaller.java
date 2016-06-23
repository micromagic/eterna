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

package self.micromagic.eterna.model;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import self.micromagic.eterna.share.EternaException;
import self.micromagic.eterna.share.EternaFactory;
import self.micromagic.eterna.share.EternaObject;
import self.micromagic.util.ref.ObjectRef;

public interface ModelCaller extends EternaObject
{
	/**
	 * 请求的属性中存放默认模块名称的键值.
	 */
	String DEFAULT_ATTR_MODEL_TAG = "eterna.defaultModel";

	/**
	 * 默认的模块名称.
	 */
	String DEFAULT_MODEL_NAME = "index";

	/**
	 * 在对象的属性中设置存放model名称的标签的属性名称.
	 */
	String MODEL_NAME_TAG_FLAG = "modelName.tag";

	/**
	 * 默认参数中存放模块名称的键值.
	 */
	String DEFAULT_MODEL_NAME_TAG = "model";

	/**
	 * 获取参数中存放模块名称的键值.
	 */
	String getModelNameTag();

	Connection getConnection(Model model) throws SQLException, EternaException;

	void closeConnection(Connection conn);

	EternaFactory getFactory() throws EternaException;

	ModelExport callModel(AppData data)
			throws EternaException, SQLException, IOException;

	ModelExport callModel(AppData data, ObjectRef preConn)
			throws EternaException, SQLException, IOException;

	ModelExport callModel(AppData data, Model model, ModelExport export, int tType, ObjectRef preConn)
			throws EternaException, SQLException, IOException;

	/**
	 * 根据数据集<code>data</code>生成重定向的参数
	 */
	String prepareParam(AppData data, String charset) throws EternaException, IOException;

}