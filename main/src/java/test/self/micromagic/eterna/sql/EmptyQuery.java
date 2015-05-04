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

package self.micromagic.eterna.sql;

import java.sql.Connection;
import java.sql.SQLException;

import self.micromagic.eterna.dao.Dao;
import self.micromagic.eterna.dao.CustomResultIterator;
import self.micromagic.eterna.dao.Query;
import self.micromagic.eterna.dao.ResultIterator;
import self.micromagic.eterna.dao.impl.QueryImpl;
import self.micromagic.eterna.share.EternaException;

public class EmptyQuery extends QueryImpl
{
	private String specialSQL = null;

	public Query createQueryAdapter()
			throws EternaException
	{
		EmptyQuery other = new EmptyQuery();
		this.copy(other);
		return other;
	}

	protected void copy(Dao copyObj)
	{
		super.copy(copyObj);
		EmptyQuery other = (EmptyQuery) copyObj;
		other.specialSQL = this.specialSQL;
	}

	public void setSpecialSQL(String specialSQL)
	{
		this.specialSQL = specialSQL;
	}

	public String getPreparedScript()
			throws EternaException
	{
		if (this.specialSQL != null)
		{
			return this.specialSQL;
		}
		return super.getPreparedScript();
	}

	public ResultIterator executeQueryHoldConnection(Connection conn)
			throws EternaException, SQLException
	{
		return this.executeQuery(conn);
	}

	public ResultIterator executeQuery(Connection conn)
			throws EternaException, SQLException
	{
		try
		{
			CustomResultIterator ritr = new CustomResultIterator(this.getReaderManager(), this.getPermission0());
			ritr.finishCreateRow();
			return ritr;
		}
		finally
		{
			this.logSQL(0L, null, conn);
		}
	}

}