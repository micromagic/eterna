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

package self.micromagic.eterna.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import org.dom4j.Element;

import self.micromagic.eterna.dao.Update;
import self.micromagic.eterna.model.AppDataLogExecute;
import self.micromagic.eterna.share.EternaException;
import self.micromagic.eterna.share.EternaFactory;
import self.micromagic.util.logging.TimeLogger;

public class UpdateImpl extends BaseDao
		implements Update
{
	public int getExecutedResult()
	{
		return this.executedResult;
	}
	private int executedResult = -1;

	public Class getObjectType()
	{
		return this.getClass();
	}

	protected void initElse(EternaFactory factory)
	{
	}

	public String getType()
	{
		return DAO_TYPE_UPDATE;
	}

	public Object create()
			throws EternaException
	{
		UpdateImpl other = new UpdateImpl();
		this.copy(other);
		return other;
	}

	public void execute(Connection conn)
			throws EternaException, SQLException
	{
		TimeLogger startTime = new TimeLogger();
		Statement stmt = null;
		Throwable error = null;
		this.executedResult = -1;
		try
		{
			if (this.hasActiveParam())
			{
				PreparedStatement temp = conn.prepareStatement(this.getPreparedScript());
				stmt = temp;
				this.prepareValues(temp);
				temp.execute();
			}
			else
			{
				stmt = conn.createStatement();
				stmt.execute(this.getPreparedScript());
			}
		}
		catch (SQLException ex)
		{
			error = ex;
			throw ex;
		}
		catch (RuntimeException ex)
		{
			error = ex;
			throw ex;
		}
		catch (Error ex)
		{
			error = ex;
			throw ex;
		}
		finally
		{
			log(this, startTime, error, conn);
			if (stmt != null)
			{
				stmt.close();
			}
		}
	}

	public int executeUpdate(Connection conn)
			throws EternaException, SQLException
	{
		TimeLogger startTime = new TimeLogger();
		Statement stmt = null;
		Throwable error = null;
		int result = this.executedResult = -1;
		try
		{
			if (this.hasActiveParam())
			{
				PreparedStatement temp = conn.prepareStatement(this.getPreparedScript());
				stmt = temp;
				this.prepareValues(temp);
				result = temp.executeUpdate();
			}
			else
			{
				stmt = conn.createStatement();
				result = stmt.executeUpdate(this.getPreparedScript());
			}
			this.executedResult = result;
			return result;
		}
		catch (SQLException ex)
		{
			error = ex;
			throw ex;
		}
		catch (RuntimeException ex)
		{
			error = ex;
			throw ex;
		}
		catch (Error ex)
		{
			error = ex;
			throw ex;
		}
		finally
		{
			Element node = log(this, startTime, error, conn);
			if (node != null && result != -1)
			{
				Element resultNode = node.addElement(this.getType() + "-result");
				AppDataLogExecute.printObject(resultNode, new Integer(result));
			}
			if (stmt != null)
			{
				stmt.close();
			}
		}
	}

}