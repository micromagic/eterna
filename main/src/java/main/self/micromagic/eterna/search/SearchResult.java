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

package self.micromagic.eterna.search;

import java.io.IOException;
import java.io.Writer;
import java.sql.SQLException;

import self.micromagic.eterna.dao.ResultIterator;
import self.micromagic.eterna.share.EternaException;
import self.micromagic.eterna.view.DataPrinter;

/**
 * 搜索的结果.
 */
public final class SearchResult
		implements DataPrinter.BeanPrinter
{
	/**
	 * 分页的尺寸.
	 */
	public final int pageSize;

	/**
	 * 当前的页数.
	 * 从0开始, 第一页为0 第二页为1 ...
	 */
	public final int pageNum;

	/**
	 * 使用的search对象的名称.
	 */
	public final String searchName;

	/**
	 * 使用的query对象的名称.
	 */
	public final String queryName;

	/**
	 * 搜索的结果集.
	 */
	public final ResultIterator queryResult;

	/**
	 * 搜索统计的结果集.
	 */
	public final ResultIterator searchCount;

	/**
	 * 排序的配置.
	 */
	public final String orderConfig;

	/**
	 * 搜索的属性配置.
	 */
	public final SearchAttributes searchAttrs;

	public SearchResult(String searchName, String queryName, ResultIterator queryResult,
			ResultIterator searchCount, int pageSize, int pageNum, String orderConfig,
			SearchAttributes searchAttrs)
	{
		this.pageSize = pageSize;
		this.pageNum = pageNum;
		this.searchName = searchName;
		this.queryName = queryName;
		this.queryResult = queryResult;
		this.searchCount = searchCount;
		this.orderConfig = orderConfig;
		this.searchAttrs = searchAttrs;
	}

	/**
	 * 使用一个现有的搜索结果以及新的查询结果构造一个新的搜索结果.
	 *
	 * @param old          现有的搜索结果
	 * @param queryName    新的查询操作的名称
	 * @param queryResult  新的查询结果
	 */
	public SearchResult(SearchResult old, String queryName, ResultIterator queryResult)
	{
		this.pageSize = old.pageSize;
		this.pageNum = old.pageNum;
		this.searchName = old.searchName;
		this.queryName = queryName;
		this.queryResult = queryResult;
		this.searchCount = old.searchCount;
		this.orderConfig = old.orderConfig;
		this.searchAttrs = old.searchAttrs;
	}

	public void print(DataPrinter p, Writer out, Object bean)
			throws IOException, EternaException
	{
		try
		{

			p.printObjectBegin(out);
			p.printResultIterator(out, this.queryResult, false);
			p.printPairWithoutCheck(out, this.searchAttrs.pageNumTag, this.pageNum, false);
			p.printPairWithoutCheck(out, this.searchAttrs.pageSizeTag, this.pageSize, false);
			p.printPairWithoutCheck(out, "searchName", this.searchName, false);
			if (this.queryResult.isTotalCountAvailable())
			{
				p.printPairWithoutCheck(out, this.searchAttrs.totalCountTag,
						this.queryResult.getTotalCount(), false);
			}
			if (this.orderConfig != null)
			{
				p.printPairWithoutCheck(out, this.searchAttrs.orderConfigTag,
						this.orderConfig, false);
			}
			p.printPairWithoutCheck(out, this.searchAttrs.hasMoreRecordTag,
					this.queryResult.hasMoreRecord() ? 1 : 0, false);
			p.printObjectEnd(out);
		}
		catch (SQLException ex)
		{
			throw new EternaException(ex);
		}
	}

}