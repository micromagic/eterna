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

package self.micromagic.eterna.dao.reader;

import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import self.micromagic.eterna.share.TypeManager;

public class BlobReader extends ObjectReader
{
	public BlobReader(String name)
	{
		super(name);
	}

	public int getType()
	{
		return TypeManager.TYPE_BLOB;
	}

	public Object readCall(CallableStatement call, int index)
			throws SQLException
	{
		return call.getBlob(index);
	}

	public Object readResult(ResultSet rs)
			throws SQLException
	{
		Blob blob = this.isUseColumnIndex() || this.transIndex(rs) ?
				rs.getBlob(this.columnIndex) : rs.getBlob(this.realAlias);
		return blob;
	}

}
