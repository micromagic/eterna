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

import java.util.List;

import self.micromagic.dbvm.AbstractObject;
import self.micromagic.dbvm.ColumnDefiner;
import self.micromagic.dbvm.ColumnDesc;
import self.micromagic.dbvm.TableDesc;
import self.micromagic.eterna.dao.Update;
import self.micromagic.eterna.share.EternaException;
import self.micromagic.util.StringAppender;
import self.micromagic.util.StringTool;

/**
 * mysql的数据库列定义.
 */
public class OracleColumn extends AbstractObject
		implements ColumnDefiner
{
	public String getColumnDefine(TableDesc tableDesc, ColumnDesc colDesc, List paramList)
	{
		String tableName = tableDesc.tableName;
		if (!StringTool.isEmpty(tableDesc.newName))
		{
			tableName = tableDesc.newName;
		}
		StringAppender buf = StringTool.createStringAppender(16);
		String defExp = makeDefaultExpression(colDesc, null);
		if (colDesc.optType == OPT_TYPE_CREATE)
		{
			if (tableDesc.optType == OPT_TYPE_CREATE)
			{
				buf.append(colDesc.colName).append(' ')
						.append(this.typeDefiner.getTypeDefine(colDesc.typeId));
				if (defExp != null)
				{
					buf.append(' ').append(defExp);
				}
				if (colDesc.nullable != null && !colDesc.nullable.booleanValue())
				{
					buf.append(" not null");
				}
			}
			else
			{
				buf.append("alter table ").append(tableName).append(" add ")
						.append(colDesc.colName).append(' ')
						.append(this.typeDefiner.getTypeDefine(colDesc.typeId));
				if (defExp != null)
				{
					buf.append(' ').append(defExp);
				}
				if (colDesc.nullable != null)
				{
					buf.append(colDesc.nullable.booleanValue() ? " null" : " not null");
				}
			}
		}
		else if (colDesc.optType == OPT_TYPE_MODIFY)
		{
			buf.append("alter table ").append(tableName).append(" modify ")
					.append(colDesc.colName).append(' ')
					.append(this.typeDefiner.getTypeDefine(colDesc.typeId));
			if (defExp != null)
			{
				buf.append(' ').append(defExp);
			}
			if (colDesc.nullable != null)
			{
				buf.append(colDesc.nullable.booleanValue() ? " null" : " not null");
			}
			if (!StringTool.isEmpty(colDesc.newName))
			{
				StringAppender s = StringTool.createStringAppender(16);
				s.append("alter table ").append(tableName).append(" rename column ")
						.append(colDesc.colName).append(" to ").append(colDesc.newName);
				Update u = this.factory.createUpdate(COMMON_EXEC);
				u.setSubScript(1, s.toString());
				if (paramList != null)
				{
					paramList.add(u);
				}
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
		if (colDesc.desc != null && colDesc.optType != OPT_TYPE_DROP)
		{
			if (!StringTool.isEmpty(colDesc.desc) || colDesc.optType == OPT_TYPE_MODIFY)
			{
				String colName = colDesc.colName;
				if (!StringTool.isEmpty(colDesc.newName))
				{
					colName = colDesc.newName;
				}
				StringAppender s = StringTool.createStringAppender(16);
				s.append("comment on column ").append(tableName).append('.')
						.append(colName).append(" is '")
						.append(StringTool.replaceAll(colDesc.desc, "'", "''")).append("'");
				Update u = this.factory.createUpdate(COMMON_EXEC);
				u.setSubScript(1, s.toString());
				if (paramList != null)
				{
					paramList.add(u);
				}
			}
		}
		return buf.toString();
	}

}
