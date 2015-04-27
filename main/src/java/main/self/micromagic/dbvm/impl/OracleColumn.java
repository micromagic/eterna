/*
 * Copyright 2015 xinjunli (micromagimport self.micromagic.dbvm.ColumnDefiner;
import self.micromagic.dbvm.ColumnDesc;
import self.micromagic.dbvm.TableDesc;
import self.micromagic.eterna.dao.Update;
import self.micromagic.eterna.dao.preparer.PreparerManager;
import self.micromagic.eterna.dao.preparer.ValuePreparer;
import self.micromagic.eterna.share.EternaException;
import self.micromagic.util.StringAppender;
import self.micromagic.util.StringTool;
import self.micromagic.util.ref.ObjectRef;
lied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package self.micromagic.dbvm.impl;

import self.micromagic.dbvm.AbstractObject;
import self.micromagic.dbvm.ColumnDefiner;
import self.micromagic.dbvm.ColumnDesc;
import self.micromagic.dbvm.TableDesc;
import self.micromagic.eterna.dao.Update;
import self.micromagic.eterna.dao.preparer.PreparerManager;
import self.micromagic.eterna.dao.preparer.ValuePreparer;
import self.micromagic.eterna.share.EternaException;
import self.micromagic.util.StringAppender;
import self.micromagic.util.StringTool;
import self.micromagic.util.ref.ObjectRef;

/**
 * mysql的数据库列定义.
 */
public class OracleColumn extends AbstractObject
		implements ColumnDefiner
{
	public String getColumnDefine(TableDesc tableDesc, ColumnDesc colDesc, ObjectRef param)
	{
		String tableName = tableDesc.tableName;
		if (!StringTool.isEmpty(tableDesc.newName))
		{
			tableName = tableDesc.newName;
		}
		StringAppender buf = StringTool.createStringAppender(16);
		if (colDesc.optType == OPT_TYPE_CREATE)
		{
			if (tableDesc.optType == OPT_TYPE_CREATE)
			{
				buf.append(colDesc.colName).append(' ')
						.append(this.typeDefiner.getTypeDefine(colDesc.typeId));
				if (!colDesc.nullable)
				{
					buf.append(" not null");
				}
			}
			else
			{
				buf.append("alter table ").append(tableName).append(" add ")
						.append(colDesc.colName).append(' ')
						.append(this.typeDefiner.getTypeDefine(colDesc.typeId));
				if (!colDesc.nullable)
				{
					buf.append(" not null");
				}
			}
		}
		else if (colDesc.optType == OPT_TYPE_MODIFY)
		{
			buf.append("alter table ").append(tableName).append(" modify ")
					.append(colDesc.colName).append(' ')
					.append(this.typeDefiner.getTypeDefine(colDesc.typeId));
			if (!colDesc.nullable)
			{
				buf.append(" not null");
			}
			if (!StringTool.isEmpty(colDesc.newName))
			{
				buf.append(";").appendln();
				buf.append("alter table ").append(tableName).append(" rename column ")
						.append(colDesc.colName).append(" to ").append(colDesc.newName);
			}
		}
		else if (colDesc.optType == OPT_TYPE_DROP)
		{
			buf.append("alter table ").append(tableName).append(" drop column ")
					.append(colDesc.colName);
		}
		else
		{
			throw new EternaException("Error opt type [" + colDesc.optType + "].");
		}
		if (!StringTool.isEmpty(colDesc.desc) && colDesc.optType != OPT_TYPE_DROP)
		{
			StringAppender s = StringTool.createStringAppender(16);
			s.append("comment on column ").append(tableName).append('.')
					.append(colDesc.colName).append(" is ?");
			Update u = this.factory.createUpdate(COMMON_EXEC);
			PreparerManager m = new PreparerManager(1);
			ValuePreparer p = this.preparerCreater.createPreparer(colDesc.desc);
			p.setRelativeIndex(1);
			m.setValuePreparer(p);
			u.setSubSQL(1, s.toString(), m);
			if (param != null)
			{
				param.setObject(u);
			}
		}
		return buf.toString();
	}

}
