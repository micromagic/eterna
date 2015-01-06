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

package self.micromagic.eterna.sql.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import org.dom4j.Element;

import self.micromagic.eterna.model.AppData;
import self.micromagic.eterna.model.AppDataLogExecute;
import self.micromagic.eterna.share.EternaException;
import self.micromagic.eterna.sql.SQLAdapter;
import self.micromagic.eterna.sql.UpdateAdapter;
import self.micromagic.eterna.sql.UpdateAdapterGenerator;
import self.micromagic.util.logging.TimeLogger;

public class UpdateAdapterImpl extends SQLAdapterImpl
		implements UpdateAdapter, UpdateAdapterGenerator
{
	public String getType()
	{
		return SQL_TYPE_UPDATE;
	}

	public SQLAdapter createSQLAdapter()
			throws EternaException
	{
		return this.createUpdateAdapter();
	}

	public UpdateAdapter createUpdateAdapter()
			throws EternaException
	{
		UpdateAdapterImpl other = new UpdateAdapterImpl();
		this.copy(other);
		return other;
	}

	public void execute(Connection conn)
			throws EternaException, SQLException
	{
		long startTime = TimeLogger.getTime();
		Statement stmt = null;
		Throwable exception = null;
		try
		{
			if (this.hasActiveParam())
			{
				PreparedStatement temp = conn.prepareStatement(this.getPreparedSQL());
				stmt = temp;
				this.prepareValues(temp);
				temp.execute();
			}
			else
			{
				stmt = conn.createStatement();
				stmt.execute(this.getPreparedSQL());
			}
		}
		catch (EternaException ex)
		{
			exception = ex;
			throw ex;
		}
		catch (SQLException ex)
		{
			exception = ex;
			throw ex;
		}
		catch (RuntimeException ex)
		{
			exception = ex;
			throw ex;
		}
		catch (Error ex)
		{
			exception = ex;
			throw ex;
		}
		finally
		{
			logSQL(this, TimeLogger.getTime() - startTime, exception, conn);
			if (stmt != null)
			{
				stmt.close();
			}
		}
	}

	public int executeUpdate(Connection conn)
			throws EternaException, SQLException
	{
		long startTime = TimeLogger.getTime();
		Statement stmt = null;
		Throwable exception = null;
		int result = -1;
		try
		{
			if (this.hasActiveParam())
			{
				PreparedStatement temp = conn.prepareStatement(this.getPreparedSQL());
				stmt = temp;
				this.prepareValues(temp);
				result = temp.executeUpdate();
			}
			else
			{
				stmt = conn.createStatement();
				result = stmt.executeUpdate(this.getPreparedSQL());
			}
			return result;
		}
		catch (EternaException ex)
		{
			exception = ex;
			throw ex;
		}
		catch (SQLException ex)
		{
			exception = ex;
			throw ex;
		}
		catch (RuntimeException ex)
		{
			exception = ex;
			throw ex;
		}
		catch (Error ex)
		{
			exception = ex;
			throw ex;
		}
		finally
		{
			if (logSQL(this, TimeLogger.getTime() - startTime, exception, conn))
			{
				if (result != -1)
				{
					AppData data = AppData.getCurrentData();
					if (data.getLogType() > 0)
					{
						Element nowNode = data.getCurrentNode();
						if (nowNode != null)
						{
							AppDataLogExecute.printObject(nowNode.addElement(this.getType() + "-result"), new Integer(result));
						}
					}
				}
			}
			if (stmt != null)
			{
				stmt.close();
			}
		}
	}

}