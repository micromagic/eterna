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
import java.util.Collections;
import java.util.Map;

import self.micromagic.eterna.share.EternaException;
import self.micromagic.eterna.view.DataPrinter;

/**
 * 查询相关的配置属性.
 */
public final class SearchAttributes
		implements DataPrinter.BeanPrinter
{
	/**
	 * 存储页号的参数名称
	 */
	public final String pageNumTag;

	/**
	 * 存储每页显示个数的参数名称
	 */
	public final String pageSizeTag;

	/**
	 * 存储XML格式查询条件的参数名称
	 */
	public final String querySettingTag;

	/**
	 * 存储查询方式的参数名称
	 */
	public final String queryTypeTag;

	/**
	 * 清除条件的查询方式
	 */
	public final String queryTypeClear;

	/**
	 * 设置条件的查询方式
	 */
	public final String queryTypeReset;

	/**
	 * 单列排序的参数名称.
	 */
	public final String singleOrderTag;

	/**
	 * 单列排序方式的参数名称.
	 */
	public final String singleOrderTypeTag;

	public SearchAttributes(Map attrs)
	{
		if (attrs == null)
		{
			attrs = Collections.EMPTY_MAP;
		}
		String tmp;
		tmp = (String) attrs.get("pageNumTag");
		this.pageNumTag = tmp == null ? "pageNum" : tmp;
		tmp = (String) attrs.get("pageSizeTag");
		this.pageSizeTag = tmp == null ? "pageSize" : tmp;
		tmp = (String) attrs.get("querySettingTag");
		this.querySettingTag = tmp == null ? "querySetting" : tmp;
		tmp = (String) attrs.get("queryTypeTag");
		this.queryTypeTag = tmp == null ? "queryType" : tmp;
		tmp = (String) attrs.get("queryTypeClear");
		this.queryTypeClear = tmp == null ? "clear" : tmp;
		tmp = (String) attrs.get("queryTypeReset");
		this.queryTypeReset = tmp == null ? "set" : tmp;
		tmp = (String) attrs.get("singleOrderTag");
		this.singleOrderTag = tmp == null ? "$order" : tmp;
		tmp = (String) attrs.get("singleOrderTypeTag");
		this.singleOrderTypeTag = tmp == null ? "$orderType" : tmp;
	}

	public void print(DataPrinter p, Writer out, Object bean)
			throws IOException, EternaException
	{
		p.printObjectBegin(out);
		p.printPairWithoutCheck(out, "pageNumTag", this.pageNumTag, true);
		p.printPairWithoutCheck(out, "pageSizeTag", this.pageSizeTag, false);
		p.printPairWithoutCheck(out, "querySettingTag", this.querySettingTag, false);
		p.printPairWithoutCheck(out, "queryTypeClear", this.queryTypeClear, false);
		p.printPairWithoutCheck(out, "queryTypeReset", this.queryTypeReset, false);
		p.printPairWithoutCheck(out, "queryTypeTag", this.queryTypeTag, false);
		p.printPairWithoutCheck(out, "singleOrderTag", this.singleOrderTag, false);
		p.printPairWithoutCheck(out, "singleOrderTypeTag", this.singleOrderTypeTag, false);
		p.printObjectEnd(out);
	}

}