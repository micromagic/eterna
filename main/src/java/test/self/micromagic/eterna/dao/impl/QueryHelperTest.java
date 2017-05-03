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

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;
import self.micromagic.eterna.dao.EmptyQuery;
import self.micromagic.eterna.dao.OnlyCountResultSet;
import self.micromagic.eterna.dao.Query;

public class QueryHelperTest extends TestCase
{
	public void testForwardOnly()
			throws Exception
	{
		callReadResults(ResultSet.TYPE_FORWARD_ONLY);
	}

	public void testScroll()
			throws Exception
	{
		callReadResults(ResultSet.TYPE_SCROLL_INSENSITIVE);
	}

	public void testOracle()
			throws Exception
	{
		List readerList = new ArrayList();
		EmptyQuery query = new EmptyQuery();
		query.setTotalCountModel(Query.TOTAL_COUNT_MODEL_NONE);
		QueryHelper qh = new OracleQueryHelper(query);
		ResultSet rs;

		rs = new OnlyCountResultSet(0);
		query.setStartRow(11);
		assertEquals("select * from (select tmpTable1.*, rownum as " + QueryHelper.ORACLE_ROW_NUM + " from (TEST) tmpTable1) tmpTable2 where " + QueryHelper.ORACLE_ROW_NUM + " >= 11", qh.getQuerySQL("TEST"));
		qh.readResults(rs, readerList);
		assertResult(0, 0, false, false, false, qh);

		rs = new OnlyCountResultSet(1);
		query.setStartRow(10);
		assertEquals("select * from (select tmpTable1.*, rownum as " + QueryHelper.ORACLE_ROW_NUM + " from (TEST) tmpTable1) tmpTable2 where " + QueryHelper.ORACLE_ROW_NUM + " >= 10", qh.getQuerySQL("TEST"));
		qh.readResults(rs, readerList);
		assertResult(1, 10, true, false, false, qh);

		rs = new OnlyCountResultSet(0);
		query.setStartRow(15);
		assertEquals("select * from (select tmpTable1.*, rownum as " + QueryHelper.ORACLE_ROW_NUM + " from (TEST) tmpTable1) tmpTable2 where " + QueryHelper.ORACLE_ROW_NUM + " >= 15", qh.getQuerySQL("TEST"));
		qh.readResults(rs, readerList);
		assertResult(0, 0, false, false, false, qh);

		rs = new OnlyCountResultSet(6);
		query.setMaxCount(5);
		query.setStartRow(2);
		assertEquals("select * from (select tmpTable1.*, rownum as " + QueryHelper.ORACLE_ROW_NUM + " from (TEST) tmpTable1 where rownum <= 7) tmpTable2 where " + QueryHelper.ORACLE_ROW_NUM + " >= 2", qh.getQuerySQL("TEST"));
		qh.readResults(rs, readerList);
		assertResult(5, 7, false, true, false, qh);

		rs = new OnlyCountResultSet(9);
		query.setMaxCount(-1);
		query.setStartRow(2);
		assertEquals("select * from (select tmpTable1.*, rownum as " + QueryHelper.ORACLE_ROW_NUM + " from (TEST) tmpTable1) tmpTable2 where " + QueryHelper.ORACLE_ROW_NUM + " >= 2", qh.getQuerySQL("TEST"));
		qh.readResults(rs, readerList);
		assertResult(9, 10, true, false, false, qh);

		rs = new OnlyCountResultSet(10);
		query.setMaxCount(-1);
		query.setStartRow(1);
		assertEquals("TEST", qh.getQuerySQL("TEST"));
		qh.readResults(rs, readerList);
		assertResult(10, 10, true, false, false, qh);

		rs = new OnlyCountResultSet(3);
		query.setMaxCount(5);
		query.setStartRow(8);
		assertEquals("select * from (select tmpTable1.*, rownum as " + QueryHelper.ORACLE_ROW_NUM + " from (TEST) tmpTable1 where rownum <= 13) tmpTable2 where " + QueryHelper.ORACLE_ROW_NUM + " >= 8", qh.getQuerySQL("TEST"));
		qh.readResults(rs, readerList);
		assertResult(3, 10, true, false, false, qh);

		rs = new OnlyCountResultSet(10);
		query.setMaxCount(3);
		query.setStartRow(2);
		query.setTotalCountModel(Query.TOTAL_COUNT_MODEL_AUTO);
		assertEquals("TEST", qh.getQuerySQL("TEST"));
		qh.readResults(rs, readerList);
		assertResult(3, 10, true, true, false, qh);

		rs = new OnlyCountResultSet(10);
		query.setMaxCount(13);
		query.setStartRow(2);
		query.setTotalCountModel(Query.TOTAL_COUNT_MODEL_AUTO);
		assertEquals("TEST", qh.getQuerySQL("TEST"));
		qh.readResults(rs, readerList);
		assertResult(9, 10, true, false, false, qh);

		rs = new OnlyCountResultSet(10);
		query.setMaxCount(-1);
		query.setStartRow(2);
		query.setTotalCountModel(Query.TOTAL_COUNT_MODEL_AUTO);
		assertEquals("TEST", qh.getQuerySQL("TEST"));
		qh.readResults(rs, readerList);
		assertResult(9, 10, true, false, false, qh);

		rs = new OnlyCountResultSet(10);
		query.setMaxCount(3);
		query.setStartRow(2);
		query.setTotalCountModel(Query.TOTAL_COUNT_MODEL_COUNT);
		assertEquals("select * from (select tmpTable1.*, rownum as " + QueryHelper.ORACLE_ROW_NUM + " from (TEST) tmpTable1 where rownum <= 5) tmpTable2 where " + QueryHelper.ORACLE_ROW_NUM + " >= 2", qh.getQuerySQL("TEST"));
		qh.readResults(rs, readerList);
		assertResult(3, 0, false, true, true, qh);

		rs = new OnlyCountResultSet(9);
		query.setMaxCount(10);
		query.setStartRow(2);
		query.setTotalCountModel(Query.TOTAL_COUNT_MODEL_COUNT);
		assertEquals("select * from (select tmpTable1.*, rownum as " + QueryHelper.ORACLE_ROW_NUM + " from (TEST) tmpTable1 where rownum <= 12) tmpTable2 where " + QueryHelper.ORACLE_ROW_NUM + " >= 2", qh.getQuerySQL("TEST"));
		qh.readResults(rs, readerList);
		assertResult(9, 10, true, false, false, qh);

		rs = new OnlyCountResultSet(4);
		query.setMaxCount(3);
		query.setStartRow(2);
		query.setTotalCountModel(15);
		assertEquals("select * from (select tmpTable1.*, rownum as " + QueryHelper.ORACLE_ROW_NUM + " from (TEST) tmpTable1 where rownum <= 5) tmpTable2 where " + QueryHelper.ORACLE_ROW_NUM + " >= 2", qh.getQuerySQL("TEST"));
		qh.readResults(rs, readerList);
		assertResult(3, 15, true, true, false, qh);

		rs = new OnlyCountResultSet(9);
		query.setMaxCount(10);
		query.setStartRow(2);
		query.setTotalCountModel(15);
		assertEquals("select * from (select tmpTable1.*, rownum as " + QueryHelper.ORACLE_ROW_NUM + " from (TEST) tmpTable1 where rownum <= 12) tmpTable2 where " + QueryHelper.ORACLE_ROW_NUM + " >= 2", qh.getQuerySQL("TEST"));
		qh.readResults(rs, readerList);
		assertResult(9, 15, true, false, false, qh);

		rs = new OnlyCountResultSet(0);
		query.setMaxCount(3);
		query.setStartRow(2);
		query.setTotalCountModel(15);
		assertEquals("select * from (select tmpTable1.*, rownum as " + QueryHelper.ORACLE_ROW_NUM + " from (TEST) tmpTable1 where rownum <= 5) tmpTable2 where " + QueryHelper.ORACLE_ROW_NUM + " >= 2", qh.getQuerySQL("TEST"));
		qh.readResults(rs, readerList);
		assertResult(0, 15, true, false, false, qh);

		rs = new OnlyCountResultSet(2);
		query.setMaxCount(-1);
		query.setStartRow(2);
		query.setTotalCountModel(15, new Query.TotalCountInfo(true, false));
		assertEquals("select * from (select tmpTable1.*, rownum as " + QueryHelper.ORACLE_ROW_NUM + " from (TEST) tmpTable1) tmpTable2 where " + QueryHelper.ORACLE_ROW_NUM + " >= 2", qh.getQuerySQL("TEST"));
		qh.readResults(rs, readerList);
		assertResult(2, 15, false, true, false, qh);

		rs = new OnlyCountResultSet(0);
		query.setStartRow(11);
		query.setTotalCountModel(Query.TOTAL_COUNT_MODEL_COUNT);
		assertEquals("select * from (select tmpTable1.*, rownum as " + QueryHelper.ORACLE_ROW_NUM + " from (TEST) tmpTable1) tmpTable2 where " + QueryHelper.ORACLE_ROW_NUM + " >= 11", qh.getQuerySQL("TEST"));
		qh.readResults(rs, readerList);
		assertResult(0, 0, false, false, true, qh);

		rs = new OnlyCountResultSet(1);
		query.setStartRow(10);
		query.setTotalCountModel(Query.TOTAL_COUNT_MODEL_COUNT);
		assertEquals("select * from (select tmpTable1.*, rownum as " + QueryHelper.ORACLE_ROW_NUM + " from (TEST) tmpTable1) tmpTable2 where " + QueryHelper.ORACLE_ROW_NUM + " >= 10", qh.getQuerySQL("TEST"));
		qh.readResults(rs, readerList);
		assertResult(1, 10, true, false, false, qh);

		String sql1 = qh.getQuerySQL("TEST");
		String sql2 = qh.getQuerySQL("TEST");
		assertTrue(sql1 == sql2);
		sql2 = qh.getQuerySQL(new String("TEST"));
		assertEquals(sql1, sql2);
		assertFalse(sql1 == sql2);
	}

	public void testH2()
			throws Exception
	{
		List readerList = new ArrayList();
		EmptyQuery query = new EmptyQuery();
		query.setTotalCountModel(Query.TOTAL_COUNT_MODEL_NONE);
		QueryHelper qh = new LimitQueryHelper(query, QueryHelper.DB_NAME_H2);
		ResultSet rs;
		long maxValue = Integer.MAX_VALUE;

		rs = new OnlyCountResultSet(0);
		query.setStartRow(11);
		assertEquals("TEST limit 10, " + maxValue, qh.getQuerySQL("TEST"));
		qh.readResults(rs, readerList);
		assertResult(0, 0, false, false, false, qh);

		rs = new OnlyCountResultSet(1);
		query.setStartRow(10);
		assertEquals("TEST limit 9, " + maxValue, qh.getQuerySQL("TEST"));
		qh.readResults(rs, readerList);
		assertResult(1, 10, true, false, false, qh);

		rs = new OnlyCountResultSet(0);
		query.setStartRow(15);
		assertEquals("TEST limit 14, " + maxValue, qh.getQuerySQL("TEST"));
		qh.readResults(rs, readerList);
		assertResult(0, 0, false, false, false, qh);

		rs = new OnlyCountResultSet(6);
		query.setMaxCount(5);
		query.setStartRow(2);
		assertEquals("TEST limit 1, 6", qh.getQuerySQL("TEST"));
		qh.readResults(rs, readerList);
		assertResult(5, 7, false, true, false, qh);

		rs = new OnlyCountResultSet(9);
		query.setMaxCount(-1);
		query.setStartRow(2);
		assertEquals("TEST limit 1, " + maxValue, qh.getQuerySQL("TEST"));
		qh.readResults(rs, readerList);
		assertResult(9, 10, true, false, false, qh);

		rs = new OnlyCountResultSet(10);
		query.setMaxCount(-1);
		query.setStartRow(1);
		assertEquals("TEST", qh.getQuerySQL("TEST"));
		qh.readResults(rs, readerList);
		assertResult(10, 10, true, false, false, qh);

		rs = new OnlyCountResultSet(3);
		query.setMaxCount(5);
		query.setStartRow(8);
		assertEquals("TEST limit 7, 6", qh.getQuerySQL("TEST"));
		qh.readResults(rs, readerList);
		assertResult(3, 10, true, false, false, qh);

		rs = new OnlyCountResultSet(10);
		query.setMaxCount(3);
		query.setStartRow(2);
		query.setTotalCountModel(Query.TOTAL_COUNT_MODEL_AUTO);
		assertEquals("TEST", qh.getQuerySQL("TEST"));
		qh.readResults(rs, readerList);
		assertResult(3, 10, true, true, false, qh);

		rs = new OnlyCountResultSet(10);
		query.setMaxCount(13);
		query.setStartRow(2);
		query.setTotalCountModel(Query.TOTAL_COUNT_MODEL_AUTO);
		assertEquals("TEST", qh.getQuerySQL("TEST"));
		qh.readResults(rs, readerList);
		assertResult(9, 10, true, false, false, qh);

		rs = new OnlyCountResultSet(10);
		query.setMaxCount(-1);
		query.setStartRow(2);
		query.setTotalCountModel(Query.TOTAL_COUNT_MODEL_AUTO);
		assertEquals("TEST", qh.getQuerySQL("TEST"));
		qh.readResults(rs, readerList);
		assertResult(9, 10, true, false, false, qh);

		rs = new OnlyCountResultSet(10);
		query.setMaxCount(3);
		query.setStartRow(2);
		query.setTotalCountModel(Query.TOTAL_COUNT_MODEL_COUNT);
		assertEquals("TEST limit 1, 4", qh.getQuerySQL("TEST"));
		qh.readResults(rs, readerList);
		assertResult(3, 0, false, true, true, qh);

		rs = new OnlyCountResultSet(9);
		query.setMaxCount(10);
		query.setStartRow(2);
		query.setTotalCountModel(Query.TOTAL_COUNT_MODEL_COUNT);
		assertEquals("TEST limit 1, 11", qh.getQuerySQL("TEST"));
		qh.readResults(rs, readerList);
		assertResult(9, 10, true, false, false, qh);

		rs = new OnlyCountResultSet(4);
		query.setMaxCount(3);
		query.setStartRow(2);
		query.setTotalCountModel(15);
		assertEquals("TEST limit 1, 4", qh.getQuerySQL("TEST"));
		qh.readResults(rs, readerList);
		assertResult(3, 15, true, true, false, qh);

		rs = new OnlyCountResultSet(9);
		query.setMaxCount(10);
		query.setStartRow(2);
		query.setTotalCountModel(15);
		assertEquals("TEST limit 1, 11", qh.getQuerySQL("TEST"));
		qh.readResults(rs, readerList);
		assertResult(9, 15, true, false, false, qh);

		rs = new OnlyCountResultSet(0);
		query.setMaxCount(3);
		query.setStartRow(2);
		query.setTotalCountModel(15);
		assertEquals("TEST limit 1, 4", qh.getQuerySQL("TEST"));
		qh.readResults(rs, readerList);
		assertResult(0, 15, true, false, false, qh);

		rs = new OnlyCountResultSet(2);
		query.setMaxCount(-1);
		query.setStartRow(2);
		query.setTotalCountModel(15, new Query.TotalCountInfo(true, false));
		assertEquals("TEST limit 1, " + maxValue, qh.getQuerySQL("TEST"));
		qh.readResults(rs, readerList);
		assertResult(2, 15, false, true, false, qh);

		String sql1 = qh.getQuerySQL("TEST");
		String sql2 = qh.getQuerySQL("TEST");
		assertTrue(sql1 == sql2);
		sql2 = qh.getQuerySQL(new String("TEST"));
		assertEquals(sql1, sql2);
		assertFalse(sql1 == sql2);
	}

	public void callReadResults(int type)
			throws Exception
	{
		List readerList = new ArrayList();
		EmptyQuery query = new EmptyQuery();
		query.setTotalCountModel(Query.TOTAL_COUNT_MODEL_NONE);
		QueryHelper qh = new QueryHelper(query);
		ResultSet rs;

		rs = new OnlyCountResultSet(10, type);
		query.setStartRow(11);
		qh.readResults(rs, readerList);
		assertResult(0, 10, true, false, false, qh);

		rs = new OnlyCountResultSet(10, type);
		query.setStartRow(10);
		qh.readResults(rs, readerList);
		assertResult(1, 10, true, false, false, qh);

		rs = new OnlyCountResultSet(10, type);
		query.setStartRow(15);
		qh.readResults(rs, readerList);
		assertResult(0, 10, true, false, false, qh);

		rs = new OnlyCountResultSet(10, type);
		query.setMaxCount(5);
		query.setStartRow(2);
		qh.readResults(rs, readerList);
		assertResult(5, 7, false, true, false, qh);

		rs = new OnlyCountResultSet(10, type);
		query.setMaxCount(-1);
		query.setStartRow(2);
		qh.readResults(rs, readerList);
		assertResult(9, 10, true, false, false, qh);

		rs = new OnlyCountResultSet(10, type);
		query.setMaxCount(-1);
		query.setStartRow(1);
		qh.readResults(rs, readerList);
		assertResult(10, 10, true, false, false, qh);

		rs = new OnlyCountResultSet(10, type);
		query.setMaxCount(5);
		query.setStartRow(8);
		qh.readResults(rs, readerList);
		assertResult(3, 10, true, false, false, qh);

		rs = new OnlyCountResultSet(10, type);
		query.setMaxCount(3);
		query.setStartRow(2);
		query.setTotalCountModel(Query.TOTAL_COUNT_MODEL_AUTO);
		qh.readResults(rs, readerList);
		assertResult(3, 10, true, true, false, qh);

		rs = new OnlyCountResultSet(10, type);
		query.setMaxCount(13);
		query.setStartRow(2);
		query.setTotalCountModel(Query.TOTAL_COUNT_MODEL_AUTO);
		qh.readResults(rs, readerList);
		assertResult(9, 10, true, false, false, qh);

		rs = new OnlyCountResultSet(10, type);
		query.setMaxCount(3);
		query.setStartRow(2);
		query.setTotalCountModel(Query.TOTAL_COUNT_MODEL_COUNT);
		qh.readResults(rs, readerList);
		assertResult(3, 0, false, true, true, qh);

		rs = new OnlyCountResultSet(10, type);
		query.setMaxCount(10);
		query.setStartRow(2);
		query.setTotalCountModel(Query.TOTAL_COUNT_MODEL_COUNT);
		qh.readResults(rs, readerList);
		assertResult(9, 10, true, false, false, qh);

		rs = new OnlyCountResultSet(10, type);
		query.setMaxCount(3);
		query.setStartRow(2);
		query.setTotalCountModel(15);
		qh.readResults(rs, readerList);
		assertResult(3, 15, true, true, false, qh);

		rs = new OnlyCountResultSet(10, type);
		query.setMaxCount(10);
		query.setStartRow(2);
		query.setTotalCountModel(15);
		qh.readResults(rs, readerList);
		assertResult(9, 15, true, false, false, qh);

		rs = new OnlyCountResultSet(0, type);
		query.setMaxCount(3);
		query.setStartRow(2);
		query.setTotalCountModel(15);
		qh.readResults(rs, readerList);
		assertResult(0, 15, true, false, false, qh);

		rs = new OnlyCountResultSet(2, type);
		query.setMaxCount(-1);
		query.setStartRow(2);
		query.setTotalCountModel(15, new Query.TotalCountInfo(true, false));
		qh.readResults(rs, readerList);
		assertResult(1, 15, false, true, false, qh);
	}

	public void assertResult(int recordCount, int realRecordCount, boolean realRecordCountAvailable,
			boolean hasMoreRecord, boolean needCount, QueryHelper qh)
			throws Exception
	{
		assertEquals("recordCount", recordCount, qh.getRecordCount());
		assertEquals("realRecordCount", realRecordCount, qh.getRealRecordCount());
		assertEquals("realRecordCountAvailable", realRecordCountAvailable, qh.isRealRecordCountAvailable());
		assertEquals("hasMoreRecord", hasMoreRecord, qh.isHasMoreRecord());
		assertEquals("needCount", needCount, qh.needCount());
	}

}