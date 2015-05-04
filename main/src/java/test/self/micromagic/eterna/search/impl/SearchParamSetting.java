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

package self.micromagic.eterna.search.impl;

import java.sql.Connection;

import self.micromagic.eterna.dao.Query;
import self.micromagic.eterna.dao.preparer.CreaterManager;
import self.micromagic.eterna.dao.preparer.PreparerManager;
import self.micromagic.eterna.dao.preparer.ValuePreparer;
import self.micromagic.eterna.model.AppData;
import self.micromagic.eterna.search.ParameterSetting;
import self.micromagic.eterna.search.Search;
import self.micromagic.eterna.share.EternaException;
import self.micromagic.eterna.share.TypeManager;

public class SearchParamSetting
		implements ParameterSetting
{
	protected String userIdParamName = "userId";
	protected int rightSubIndex = -1;

	public void initParameterSetting(Search search)
			throws EternaException
	{
		try
		{
			this.rightSubIndex = Integer.parseInt((String) search.getAttribute("rightSubIndex"));
		}
		catch (Exception ex) {}
	}

	public void setParameter(Query query, Search search, boolean first,
			AppData data, Connection conn)
			throws EternaException
	{
		int tmpIndex = this.rightSubIndex == -1 ? search.getConditionIndex() + 1 : this.rightSubIndex;
		String checkStr = "BITAND(authorType, " + (1 << 2) + ") <> 0";
		String subStr = "p.valid = 1 and ( "
				+ "exists (select 1 from TUR_CHARGE_AUTHOR where userId = ? and projectId = p.projectId and " + checkStr + ")"
				+ " or exists (select 1 from TUR_DEPT_AUTHOR where userId = ? and deptId = p.deptId and " + checkStr + ")"
				+ " )";
		PreparerManager pm = new PreparerManager(2);
		for (int i = 0; i < 2; i++)
		{
			ValuePreparer preparer = CreaterManager.createPreparerCreater(TypeManager.TYPE_STRING, null, query.getFactory())
					.createPreparer("userId");
			preparer.setRelativeIndex(i + 1);
			pm.setValuePreparer(preparer);
		}
		query.setSubScript(tmpIndex, subStr, pm);
		query.setString(this.userIdParamName, "userId");
	}
}