/*
 * Copyright 2009-2015 xinjunli (micromagic@sina.com).
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

package self.micromagic.eterna.model.impl;

import java.io.IOException;
import java.lang.reflect.Array;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.sql.DataSource;

import self.micromagic.eterna.share.EternaException;
import self.micromagic.eterna.model.AppData;
import self.micromagic.eterna.model.AppDataLogExecute;
import self.micromagic.eterna.model.ModelAdapter;
import self.micromagic.eterna.model.ModelCaller;
import self.micromagic.eterna.model.ModelExport;
import self.micromagic.eterna.share.DataSourceManager;
import self.micromagic.eterna.share.EternaFactory;
import self.micromagic.eterna.view.View;
import self.micromagic.util.ObjectRef;
import self.micromagic.util.Utility;
import self.micromagic.util.StringAppender;
import self.micromagic.util.StringTool;
import org.dom4j.Element;

public class ModelCallerImpl
		implements ModelCaller
{
	private EternaFactory factory;
	private Map dataSourceMap = null;
	private DataSource defaultDataSource = null;
	private String defaultDataSourceName = null;

	public ModelCallerImpl()
	{
	}

	public void initModelCaller(EternaFactory factory)
			throws EternaException
	{
		this.factory = factory;
		DataSourceManager dsm = factory.getDataSourceManager();
		if (dsm != null)
		{
			this.dataSourceMap = dsm.getDataSourceMap();
			this.defaultDataSource = dsm.getDefaultDataSource();
			this.defaultDataSourceName = dsm.getDefaultDataSourceName();
			return;
		}
		this.dataSourceMap = null;
	}

	public Connection getConnection(ModelAdapter model)
			throws SQLException, EternaException
	{
		DataSource ds;
		if (this.dataSourceMap != null)
		{
			String name;
			name = model.getDataSourceName();
			if (name == null || name.equals(this.defaultDataSourceName))
			{
				ds = this.defaultDataSource;
			}
			else
			{
				ds = (DataSource) this.dataSourceMap.get(name);
				if (ds == null)
				{
					throw new EternaException("Can't find the data source:" + name + ".");
				}
			}
		}
		else
		{
			ds = Utility.getDataSource();
		}
		return ds.getConnection();
	}

	public void closeConnection(Connection conn)
	{
		try
		{
			if (conn != null)
			{
				//conn.rollback();
				conn.close();
			}
		}
		catch (SQLException ex) {}
	}

	public EternaFactory getFactory()
	{
		return this.factory;
	}

	public ModelExport callModel(AppData data)
			throws EternaException, SQLException, IOException
	{
		ObjectRef preConn = (ObjectRef) data.getSpcialData(ModelAdapter.MODEL_CACHE, ModelAdapter.PRE_CONN);
		if (preConn == null)
		{
			preConn = new ObjectRef();
			data.addSpcialData(ModelAdapter.MODEL_CACHE, ModelAdapter.PRE_CONN, preConn);
		}

		Element beginNode = null;
		boolean rootNode = false;
		if (data.getLogType() > 0)
		{
			// 没有当前节点, 说明处于当前线程的根节点
			rootNode = data.getCurrentNode() == null;
			beginNode = data.beginNode("call", data.modelName, null);
			if (rootNode)
			{
				AppDataLogExecute.printAppData(data);
			}
		}
		boolean commited = false;
		try
		{
			ModelExport export = this.callModel(data, preConn);
			if (preConn.getObject() != null)
			{
				if (export == null || !export.isErrorExport())
				{
					commited = true;
				}
			}
			return export;
		}
		finally
		{
			if (beginNode != null)
			{
				try
				{
					if (rootNode)
					{
						AppDataLogExecute.printAppData(data);
					}
					data.endNode(beginNode, null, null);
				}
				catch (Throwable ex) {}
			}
			if (preConn.getObject() != null)
			{
				if (preConn.getObject() instanceof Connection)
				{
					Connection conn = (Connection) preConn.getObject();
					if (!commited) conn.rollback();
					else conn.commit();
					conn.close();
				}
				else if (preConn.getObject() instanceof Map)
				{
					Map map = (Map) preConn.getObject();
					Iterator values = map.values().iterator();
					while (values.hasNext())
					{
						Connection conn = (Connection) values.next();
						try
						{
							if (!commited) conn.rollback();
							else conn.commit();
							conn.close();
						}
						catch (SQLException sex)
						{
							AppData.log.error("*** Error in close connection.", sex);
						}
					}
				}
				else
				{
					AppData.log.error("Error preConn type:" + preConn.getObject().getClass() + ".");
				}
				preConn.setObject(null);
			}
		}
	}

	public ModelExport callModel(AppData data, ObjectRef preConn)
			throws EternaException, SQLException, IOException
	{
		if (data.modelName == null)
		{
			data.modelName = data.getRequestParameter(factory.getModelNameTag());
			if (data.modelName == null)
			{
				data.modelName = (String) data.getRequestAttributeMap().get(DEFAULT_MODEL_TAG);
			}
		}

		ModelAdapter model = null;
		try
		{
			model = factory.createModelAdapter(data.modelName);
		}
		catch (EternaException ex)
		{
			if (AppData.log.isInfoEnabled())
			{
				StringAppender buf = StringTool.createStringAppender(128);
				buf.append("Not found the model:").append(data.modelName).append('.');
				AppData.log.warn(buf, ex);
			}
			return null;
		}
		return this.callModel(data, model, null, model.getTransactionType(), preConn);
	}

	private void setPreConnection(Connection conn, ObjectRef preConn, String dsName)
	{
		if (preConn.getObject() != null)
		{
			Object refObj = preConn.getObject();
			if (refObj instanceof Connection)
			{
				if (dsName == null || dsName.equals(this.defaultDataSourceName))
				{
					preConn.setObject(conn);
				}
				else
				{
					Connection tmpConn = (Connection) refObj;
					Map map = new HashMap();
					map.put(this.defaultDataSourceName, tmpConn);
					preConn.setObject(map);
					map.put(dsName, conn);
				}
			}
			else if (refObj instanceof Map)
			{
				((Map) refObj).put(dsName, conn);
			}
			else
			{
				AppData.log.error("Error preConn type:" + refObj.getClass() + ".");
			}
		}
		else
		{
			if (dsName == null || dsName.equals(this.defaultDataSourceName))
			{
				preConn.setObject(conn);
			}
			else
			{
				Map map = new HashMap();
				map.put(dsName, conn);
				preConn.setObject(map);
			}
		}
	}

	public ModelExport callModel(AppData data, ModelAdapter model, ModelExport export, int tType, ObjectRef preConn)
			throws EternaException, SQLException, IOException
	{
		String dsName = model.getDataSourceName();
		Connection oldConn = null;
		if (preConn.getObject() != null)
		{
			Object refObj = preConn.getObject();
			if (dsName == null || dsName.equals(this.defaultDataSourceName))
			{
				if (refObj instanceof Connection)
				{
					oldConn = (Connection) refObj;
				}
				else if (refObj instanceof Map)
				{
					oldConn = (Connection) ((Map) refObj).get(this.defaultDataSourceName);
				}
				else
				{
					AppData.log.error("Error preConn type:" + refObj.getClass() + ".");
				}
			}
			else
			{
				if (refObj instanceof Map)
				{
					oldConn = (Connection) ((Map) refObj).get(dsName);
				}
				else if (!(refObj instanceof Connection))
				{
					AppData.log.error("Error preConn type:" + refObj.getClass() + ".");
				}
			}
		}
		Connection myConn = null;
		boolean executed = false;
		Object oldHoldedFlag = null;
		switch (tType)
		{
			case ModelAdapter.T_REQUARED:
				if (oldConn == null)
				{
					oldConn = this.getConnection(model);
					oldConn.setAutoCommit(false);
					this.setPreConnection(oldConn, preConn, dsName);
				}
				myConn = oldConn;
				break;
			case ModelAdapter.T_NEW:
				myConn = this.getConnection(model);
				myConn.setAutoCommit(false);
				// 这里不需要判断oldConn, 后面的model如果是requared的话, 将使用这个conn
				this.setPreConnection(myConn, preConn, dsName);
				break;
			case ModelAdapter.T_NONE:
				myConn = this.getConnection(model);
				myConn.setAutoCommit(true);
				break;
			case ModelAdapter.T_HOLD:
				myConn = this.getConnection(model);
				myConn.setAutoCommit(true);
				// 将原来的链接保持标志保存下来
				oldHoldedFlag = data.getSpcialData(ModelAdapter.MODEL_CACHE, ModelAdapter.CONN_HOLDED);
				data.addSpcialData(ModelAdapter.MODEL_CACHE, ModelAdapter.CONN_HOLDED, "0");
				break;
			case ModelAdapter.T_IDLE:
				myConn = this.getConnection(model);
				break;
			case ModelAdapter.T_NOTNEED:
				myConn = oldConn;
		}

		try
		{
			ModelExport tmpExport = model.doModel(data, myConn);
			if (tmpExport != null)
			{
				if (tmpExport.isRedirect() || (!tmpExport.isNextModel() && tmpExport.isErrorExport()))
				{
					executed = !tmpExport.isErrorExport();
					return tmpExport;
				}
			}
			if (export != null)
			{
				tmpExport = export;
				if (tmpExport.isRedirect() || (!tmpExport.isNextModel() && tmpExport.isErrorExport()))
				{
					executed = !export.isErrorExport();
					return export;
				}
			}
			boolean hasError = false;
			if (tmpExport != null && tmpExport.isNextModel())
			{
				if (tmpExport.isErrorExport())
				{
					// 如果是错误的出口, 并且有下一个要执行的model, 则回滚
					if (tType == ModelAdapter.T_REQUARED || tType == ModelAdapter.T_NEW)
					{
						myConn.rollback();
					}
					hasError = true;
				}
				data.modelName = tmpExport.getModelName();
				tmpExport = this.callModel(data, preConn);
			}
			executed = !hasError && (tmpExport == null || !tmpExport.isErrorExport());
			return tmpExport;
		}
		finally
		{
			switch (tType)
			{
				case ModelAdapter.T_REQUARED:
					if (!executed)
					{
						myConn.rollback();
					}
					break;
				case ModelAdapter.T_NEW:
					if (!executed)
					{
						myConn.rollback();
					}
					if (oldConn != null)
					{
						if (executed)
						{
							myConn.commit();
						}
						this.closeConnection(myConn);
						// oldConn不为空, 则设回原来的conn, 如果为空则保留此conn直到请求结束
						this.setPreConnection(oldConn, preConn, dsName);
					}
					break;
				case ModelAdapter.T_NONE:
					this.closeConnection(myConn);
					break;
				case ModelAdapter.T_HOLD:
					if (!"1".equals(data.getSpcialData(ModelAdapter.MODEL_CACHE, ModelAdapter.CONN_HOLDED)))
					{
						// 如果数据库链接保持标志不为"1", 表示业务没有接管数据库链接, 这里需要将其释放
						this.closeConnection(myConn);
					}
					// 恢复原来的链接保持标志
					data.addSpcialData(ModelAdapter.MODEL_CACHE, ModelAdapter.CONN_HOLDED, oldHoldedFlag);
					break;
				case ModelAdapter.T_IDLE:
					this.closeConnection(myConn);
					break;
				case ModelAdapter.T_NOTNEED:
					break;
			}
		}
	}

	public String prepareParam(AppData data, String charset)
			throws EternaException, IOException
	{
		StringAppender buf = StringTool.createStringAppender(512);
		String dataType = data.getRequestParameter(View.DATA_TYPE);
		if (dataType != null)
		{
			buf.append(View.DATA_TYPE).append('=').append(URLEncoder.encode(dataType, charset));
		}
		Iterator params = data.dataMap.entrySet().iterator();
		while (params.hasNext())
		{
			Map.Entry entry = (Map.Entry) params.next();
			if (SearchExecute.SEARCH_MANAGER_ATTRIBUTES.equals(entry.getKey()))
			{
				// search相关的控制标识不用加入重定向的url参数
				continue;
			}
			if (entry.getValue() != null)
			{
				if (buf.length() > 0)
				{
					buf.append('&');
				}
				String name = URLEncoder.encode(String.valueOf(entry.getKey()), charset);
				Object value = entry.getValue();
				if (value instanceof Collection)
				{
					Iterator itr = ((Collection) value).iterator();
					boolean hasValue = false;
					while (itr.hasNext())
					{
						Object tmp = itr.next();
						if (tmp != null)
						{
							if (hasValue)
							{
								buf.append('&');
							}
							hasValue = true;
							buf.append(name).append('=').append(URLEncoder.encode(String.valueOf(tmp), charset));
						}
					}
					if (!hasValue)
					{
						buf.append(name).append('=');
					}
				}
				else if (value.getClass().isArray())
				{
					boolean hasValue = false;
					int length = Array.getLength(value);
					for (int i = 0; i < length; i++)
					{
						Object tmp = Array.get(value, i);
						if (tmp != null)
						{
							if (hasValue)
							{
								buf.append('&');
							}
							hasValue = true;
							buf.append(name).append('=').append(URLEncoder.encode(String.valueOf(tmp), charset));
						}
					}
					if (!hasValue)
					{
						buf.append(name).append('=');
					}
				}
				else
				{
					buf.append(name).append('=').append(URLEncoder.encode(String.valueOf(value), charset));
				}
			}
		}
		return buf.toString();
	}

}