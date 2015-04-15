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

package self.micromagic.app;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.jsp.JspWriter;

import self.micromagic.eterna.search.SearchManager;
import self.micromagic.eterna.search.SearchAdapter;
import self.micromagic.eterna.share.EternaException;

public class PageManager
{
	public static final String DOWNLOAD_NAME = "download";
	public static final String FORM_NAME = "pageManagerForm";
	public static final String SEARCH_NAME_TAG = "searchName";
	public static final String QUERY_RESULT_NAME = "queryResult";
	public static final String SEARCH_MANAGER_NAME = "searchManager";
	public static final String SPAN_FLAG = "spanFlag";

	private String nextURL;
	private String preURL;

	private JspWriter out;
	SearchManager.Attributes attributes;
	SearchAdapter.Result result;

	public PageManager(JspWriter out, SearchManager.Attributes attributes,
			SearchAdapter.Result result)
	{
		this.out = out;
		this.attributes = attributes;
		this.result = result;
	}

	public void setNextURL(String nextURL)
	{
		this.nextURL = nextURL;
	}

	public void setPreURL(String preURL)
	{
		this.preURL = preURL;
	}

	public static void printPageAttr(JspWriter out, String searchName, SearchManager.Attributes attributes)
			throws IOException
	{
		out.print("   <input type=\"hidden\" name=\"");
		out.print(SEARCH_NAME_TAG);
		out.print("\" value=\"");
		out.print(searchName);
		out.println("\">");
		out.print("   <input type=\"hidden\" name=\"");
		out.print(attributes.queryTypeTag);
		out.print("\" value=\"");
		out.print(attributes.queryTypeReset);
		out.println("\">");
		out.print("   <input type=\"hidden\" name=\"");
		out.print(attributes.pageNumTag);
		out.println("\" value=\"0\">");
	}

	public void printPageTool(boolean moreOpt)
			throws IOException, SQLException, EternaException
	{
		this.printPageTool(moreOpt, false);
	}

	public void printPageTool(boolean moreOpt, boolean canHidden)
			throws IOException, SQLException, EternaException
	{
		if (canHidden)
		{
			if (!result.queryResult.isHasMoreRecord() && result.pageNum == 0)
			{
				return;
			}
		}

		this.out.print("您目前在第 <font color=\"red\">");
		this.out.print(this.result.pageNum + 1);
		this.out.print("</font> 页");
		if (this.result.queryResult.isRealRecordCountAvailable())
		{
			int totalRecord = this.result.queryResult.getRealRecordCount();
			int pageCount;
			if ((totalRecord % this.result.pageSize) == 0)
			{
				pageCount = totalRecord / this.result.pageSize;
			}
			else
			{
				pageCount = totalRecord / this.result.pageSize + 1;
			}
			this.out.print("，共 <font color=\"red\">");
			this.out.print(pageCount);
			this.out.print("</font> 页，共 <font color=\"red\">");
			this.out.print(totalRecord);
			this.out.print("</font> 条");
		}
		this.out.print(" &nbsp; ");

		if (result.pageNum > 0)
		{
			this.out.print(this.preURL);
		}
		if (result.queryResult.isHasMoreRecord())
		{
			this.out.print(this.nextURL);
		}

		if (moreOpt)
		{
			this.out.print(" &nbsp; ");
			this.out.print("跳转至第 <input type='text' name='theNum' style='width:20px' maxlength='3' value='");
			this.out.print(result.pageNum + 1);
			this.out.print("'> 页 每页显示 <input type='text' name='theSize' size='2' value='");
			this.out.print(result.pageSize);
			this.out.print("' style='width:20px' maxlength='3'> 条");
			this.out.print(" &nbsp; ");
			this.out.print(" <input type='button' value='GO' class='button-1' onClick='javascript:doPageChanged(0, theNum.value, theSize.value)'>");
		}
	}

	public void printFormAndScript(String actionURL)
			throws IOException
	{
		this.out.println("<script language=\"javascript\">");
		this.out.println("var pageChanged = false;");

		this.out.println("function doPageChanged(changeNum, newPage, pageSize)");
		this.out.println('{');

		this.out.println("   if (!pageChanged)");
		this.out.println("   {");
		this.out.println("      pageChanged = true;");
		this.out.println("   }");
		this.out.println("   else");
		this.out.println("   {");
		this.out.println("      return;");
		this.out.println("   }");

		this.out.println("   if (changeNum == 0)");
		this.out.println("   {");
		this.out.print("      document.");
		this.out.print(FORM_NAME);
		this.out.print('.');
		this.out.print(this.attributes.pageNumTag);
		this.out.println(".value = newPage - 1;");
		this.out.print("      document.");
		this.out.print(FORM_NAME);
		this.out.print('.');
		this.out.print(this.attributes.pageSizeTag);
		this.out.println(".value = pageSize < 1 ? 1 : pageSize > 300 ? 300 : pageSize;");
		this.out.println("   }");

		this.out.println("   else");
		this.out.println("   {");
		this.out.print("      document.");
		this.out.print(FORM_NAME);
		this.out.print('.');
		this.out.print(this.attributes.pageNumTag);
		this.out.print(".value = changeNum + ");
		this.out.print(this.result.pageNum);
		this.out.println(';');
		this.out.println("   }");
		this.out.print("   document.");
		this.out.print(FORM_NAME);
		this.out.println(".submit();");
		this.out.println('}');

		this.out.println("</script>");


		this.out.print("<form name=\"");
		this.out.print(FORM_NAME);
		this.out.print("\" method=\"post\" action=\"");
		this.out.print(actionURL);
		this.out.println("\">");

		this.out.print("   <input type=\"hidden\" name=\"");
		this.out.print(SEARCH_NAME_TAG);
		this.out.print("\" value=\"");
		this.out.print(this.result.searchName);
		this.out.println("\">");
		this.out.print("   <input type=\"hidden\" name=\"");
		this.out.print(this.attributes.pageNumTag);
		this.out.println("\">");
		this.out.print("   <input type=\"hidden\" name=\"");
		this.out.print(this.attributes.pageSizeTag);
		this.out.println("\">");

		this.out.println("</form>");
	}

}

