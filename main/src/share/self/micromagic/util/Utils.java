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

package self.micromagic.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Collections;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspWriter;

import self.micromagic.eterna.search.SearchAdapter;
import self.micromagic.eterna.search.SearchManager;
import self.micromagic.eterna.sql.ResultRow;
import self.micromagic.eterna.sql.ResultIterator;
import self.micromagic.eterna.digester.ConfigurationException;

public class Utils
{
	public static char[] CODE16 = {
		'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
		'A', 'B', 'C', 'D', 'E', 'F'
	};

	public static String TOTAL_RECORD_TAG = "self.total.record";
	public static String PAGE_COUNT_TAG = "self.page.count";
	public static String PRE_PAGE_TAG = "self.pre.page";
	public static String NEXT_PAGE_TAG = "self.next.page";

	public static final String SERVER_ROOT_TAG = "self.server.root";

	public static int parseInt(String str)
	{
		return parseInt(str, 0);
	}

	public static int parseInt(String str, int defaultValue)
	{
		try
		{
			return Integer.parseInt(str);
		}
		catch (Exception ex)
		{
			return defaultValue;
		}
	}

	public static double parseDouble(String str)
	{
		return parseDouble(str, 0.0);
	}

	public static double parseDouble(String str, double defaultValue)
	{
		try
		{
			return Double.parseDouble(str);
		}
		catch (Exception ex)
		{
			return defaultValue;
		}
	}

	public static void setListPageAttributes(String listURL, SearchManager.Attributes attributes,
			SearchAdapter.Result result, HttpServletRequest request)
			throws SQLException, ConfigurationException
	{
		String root = request.getContextPath();
		int totalRecord = result.queryResult.getRealRecordCount();
		int pageCount;
		if ((totalRecord % result.pageSize) == 0)
		{
			pageCount = totalRecord / result.pageSize;
		}
		else
		{
			pageCount = totalRecord / result.pageSize + 1;
		}
		request.setAttribute(TOTAL_RECORD_TAG, totalRecord + "");
		request.setAttribute(PAGE_COUNT_TAG, pageCount + "");

		String prePageHref, nextPageFref;
		int prePage = (result.pageNum - 1) > pageCount ? (pageCount - 1) : result.pageNum - 1;
		if (result.pageNum > 0)
		{
			StringAppender temp = StringTool.createStringAppender(256);
			temp.append("<a href=\"").append(root).append(listURL).append(attributes.pageNumTag)
					.append('=').append(prePage).append("\">上一页</a>");
			prePageHref = temp.toString();
		}
		else
		{
			prePageHref = "<a>上一页</a>";
		}
		if (result.queryResult.isHasMoreRecord())
		{
			StringAppender temp = StringTool.createStringAppender(256);
			temp.append("<a href=\"").append(root).append(listURL).append(attributes.pageNumTag)
					.append('=').append(result.pageNum + 1).append("\">下一页</a>");
			nextPageFref = temp.toString();
		}
		else
		{
			nextPageFref = "<a>下一页</a>";
		}
		request.setAttribute(PRE_PAGE_TAG, prePageHref);
		request.setAttribute(NEXT_PAGE_TAG, nextPageFref);
	}

	public static String getServerRoot(HttpServletRequest request)
	{
		HttpSession session = request.getSession();
		String serverRoot = (String) session.getAttribute(SERVER_ROOT_TAG);
		if (serverRoot == null)
		{
			serverRoot = request.getScheme() + "://" + request.getServerName()
					+ ":" + request.getServerPort();
			session.setAttribute(SERVER_ROOT_TAG, serverRoot);
		}
		return serverRoot;
	}

	public static String getThisPageHref(HttpServletRequest request)
	{
		String href;
		String queryStr = request.getQueryString();
		if (queryStr != null)
		{
			href = request.getRequestURL().append('?')
					.append(request.getQueryString()).toString();
		}
		else
		{
			href = request.getRequestURL().toString();
		}
		return href;
	}

	/**
	 * 生成用于树型选择的script, 方法名称为doTreeSelect<p>
	 * 注: value如果是字符串请在外面加上双引号<p>
	 * 代码示例:
	 * <p><blockquote><pre>
	 *    function doTreeSelect()
	 *    {
	 *       var sName = [value];
	 *       showModalDialog("[root]/eterna/tree.jsp?treeName=[treeName]&selectedName=" + sName,
	 *             new Array(window, [returnMethod]), "dialogWidth:380px;dialogHeight:460px;center:yes;status:no;help:no");
	 *    }
	 * </pre></blockquote>
	 * 此外, 如果withScriptTag为true, 则代码段前后还会加上<script language="javascript">和</script>
	 */
	public static void printTreeSelectScript(JspWriter out, String root, String treeName,
			String value, String returnMethod, boolean withScriptTag)
			throws IOException
	{
		if (withScriptTag)
		{
			out.println("<script language=\"javascript\">");
		}

		out.println("function doTreeSelect()");
		out.println('{');
		out.print("   var sName = ");
		out.print(value);
		out.println(';');

		out.print("   showModalDialog(\"");
		out.print(root);
		out.print("/eterna/tree.jsp?treeName=");
		out.print(treeName);
		out.println("&selectedName=\" + sName,");

		out.print("         new Array(window, ");
		out.print(returnMethod);
		out.println("), \"dialogWidth:380px;dialogHeight:460px;center:yes;status:no;help:no\");");
		out.println('}');

		if (withScriptTag)
		{
			out.println("</script>");
		}
	}

	/**
	 * 生成用于查询的javascript代码, 方法名称为doSearch<p>
	 * 代码示例:
	 * <p><blockquote><pre>
	 *    function doSearch()
	 *    {
	 *       var queryXML = document.[formName].[attributes.querySettingTag];
	 *       queryXML.value = "";
	 *       showModalDialog("[root]/eterna/query.jsp?searchName==[searchName]",
	 *             new Array(window, queryXML), "dialogWidth:520px;dialogHeight:420px;center:yes;status:no;help:no");
	 *       if (queryXML.value != "")
	 *       {
	 *          document.[formName].submit();
	 *       }
	 *    }
	 * </pre></blockquote>
	 * 此外, 如果withScriptTag为true, 则代码段前后还会加上<script language="javascript">和</script>
	 */
	public static void printQueryScript(JspWriter out, SearchManager.Attributes attributes,
			String root, String searchName, String formName, boolean withScriptTag)
			throws IOException
	{
		if (withScriptTag)
		{
			out.println("<script language=\"javascript\">");
		}

		out.println("function doSearch()");
		out.println('{');
		out.print("   var queryXML = document.");
		out.print(formName);
		out.print('.');
		out.print(attributes.querySettingTag);
		out.println(';');
		out.println("   queryXML.value = \"\";");

		out.print("   showModalDialog(\"");
		out.print(root);
		out.print("/eterna/query.jsp?searchName=");
		out.print(searchName);
		out.println("\",");
		out.println("         new Array(window, queryXML), \"dialogWidth:520px;dialogHeight:420px;center:yes;status:no;help:no\");");

		out.println("   if (queryXML.value != \"\")");
		out.println("   {");
		out.print("      document.");
		out.print(formName);
		out.println(".submit();");
		out.println("   }");
		out.println('}');

		if (withScriptTag)
		{
			out.println("</script>");
		}
	}

	/**
	 * 生成用于跳转至某页的javascript代码, 方法名称为doPageJump<p>
	 * 代码示例:
	 * <p><blockquote><pre>
	 *    function doPageJump()
	 *    {
	 *       var queryXML = document.[formName].[attributes.querySettingTag];
	 *       queryXML.value = "";
	 *       showModalDialog("[root]/eterna/query.jsp?searchName==[searchName]",
	 *             new Array(window, queryXML), "dialogWidth:520px;dialogHeight:420px;center:yes;status:no;help:no");
	 *       if (queryXML.value != "")
	 *       {
	 *          document.queryForm.submit();
	 *       }
	 *    }
	 * </pre></blockquote>
	 * 此外, 如果withScriptTag为true, 则代码段前后还会加上<script language="javascript">和</script>
	 */
	public static void printPageJumpScript(JspWriter out, SearchManager.Attributes attributes,
			String root, String searchName, String formName, boolean withScriptTag)
			throws IOException
	{
		if (withScriptTag)
		{
			out.println("<script language=\"javascript\">");
		}

		out.println("function doSearch()");
		out.println('{');
		out.print("   var queryXML = document.");
		out.print(formName);
		out.print('.');
		out.print(attributes.querySettingTag);
		out.println(';');
		out.println("   queryXML.value = \"\";");

		out.print("   showModalDialog(\"");
		out.print(root);
		out.print("/eterna/query.jsp?searchName=");
		out.print(searchName);
		out.println("\",");
		out.println("         new Array(window, queryXML), \"dialogWidth:520px;dialogHeight:420px;center:yes;status:no;help:no\");");

		out.println("   if (queryXML.value != \"\")");
		out.println("   {");
		out.println("      document.queryForm.submit();");
		out.println("   }");
		out.println('}');

		if (withScriptTag)
		{
			out.println("</script>");
		}
	}

	/**
	 * 生成用于查询form<p>
	 * 代码示例:
	 * <p><blockquote><pre>
	 *    <form name="[formName]" method="post" action="[root]+[queryUrl]">
	 *       <input type="hidden" name="[attributes.querySettingTag]" value="">
	 *       <input type="hidden" name="[attributes.queryTypeTag]" value="[attributes.queryTypeReset]">
	 *       <input type="hidden" name="[attributes.pageNumTag]" value="0">
	 *    </form>
	 * </pre></blockquote>
	 *
	 * @param withFormEnd   是否要添加form的结束标签&lt;/form&gt;
	 */
	public static void printQueryForm(JspWriter out, SearchManager.Attributes attributes,
			String root, String formName, String queryUrl, boolean withFormEnd)
			throws IOException
	{
		out.print("<form name=\"");
		out.print(formName);
		out.print("\" method=\"post\" action=\"");
		out.print(root);
		out.print(queryUrl);
		out.println("\">");

		out.print("   <input type=\"hidden\" name=\"");
		out.print(attributes.querySettingTag);
		out.println("\" value=\"\">");
		out.print("   <input type=\"hidden\" name=\"");
		out.print(attributes.queryTypeTag);
		out.print("\" value=\"");
		out.print(attributes.queryTypeReset);
		out.println("\">");
		out.print("   <input type=\"hidden\" name=\"");
		out.print(attributes.pageNumTag);
		out.println("\" value=\"0\">");

		if (withFormEnd)
		{
			out.println("</form>");
		}
	}

	/**
	 * 生成用于查询form<p>
	 * 代码示例:
	 * <p><blockquote><pre>
	 *    <form name="[formName]" method="post" action="[root]+[queryUrl]">
	 *       <input type="hidden" name="[attributes.querySettingTag]" value="">
	 *       <input type="hidden" name="[attributes.queryTypeTag]" value="[attributes.queryTypeReset]">
	 *       <input type="hidden" name="[attributes.pageNumTag]" value="0">
	 *    </form>
	 * </pre></blockquote>
	 */
	public static void printQueryForm(JspWriter out, SearchManager.Attributes attributes,
			String root, String formName, String queryUrl)
			throws IOException
	{
		printQueryForm(out, attributes, root, formName, queryUrl, true);
	}


	/**
	 * 生成select控件的一组option<p>
	 */
	public static void printOptions(JspWriter out, List rows, String indentSpace)
			throws IOException, SQLException, ConfigurationException
	{
		printOptions(out, rows, indentSpace, "codeId", "codeValue");
	}

	/**
	 * 生成select控件的一组option<p>
	 */
	public static void printOptions(JspWriter out, List rows, String indentSpace,
			String colNameCodeId, String colNameCodeValue)
			throws IOException, SQLException, ConfigurationException
	{
		Iterator itr = rows.iterator();
		while (itr.hasNext())
		{
			ResultRow row = (ResultRow) itr.next();
			printOption(out, row, indentSpace, colNameCodeId, colNameCodeValue);
		}
	}

	/**
	 * 生成select控件的option<p>
	 * 代码示例:
	 * <p><blockquote><pre>
	 *    <option value="[codeId]">[codeValue]</option>
	 * </pre></blockquote>
	 */
	public static void printOption(JspWriter out, ResultRow row, String indentSpace)
			throws IOException, SQLException, ConfigurationException
	{
		printOption(out, row, indentSpace, "codeId", "codeValue");
	}

	/**
	 * 生成select控件的option<p>
	 * 代码示例:
	 * <p><blockquote><pre>
	 *    <option value="[codeId]">[codeValue]</option>
	 * </pre></blockquote>
	 */
	public static void printOption(JspWriter out, ResultRow row, String indentSpace,
			String colNameCodeId, String colNameCodeValue)
			throws IOException, SQLException, ConfigurationException
	{
		out.print(indentSpace);
		out.print("<option value=\"");
		out.print(Utils.getResult(row, colNameCodeId, false));
		out.print("\">");
		out.print(Utils.getResult(row, colNameCodeValue, true));
		out.println("</option>");
	}

	/**
	 * 处理字符串, 将其转换为可放入双引号内赋值的字符串.
	 * 同时会将小于空格的代码(不包括:\r,\n,\t), 转换为空格.
	 */
	public static String dealString2EditCode(String str)
	{
		if (str == null)
		{
			return "";
		}
		StringAppender temp = null;
		int modifyCount = 0;
		for (int i = 0; i < str.length(); i++)
		{
			char c = str.charAt(i);
			String appendStr = null;
			if (c < ' ')
			{
				if (c == '\r')
				{
					appendStr = "\\r";
				}
				else if (c == '\n')
				{
					appendStr = "\\n";
				}
				else if (c == '\t')
				{
					appendStr = "\\t";
				}
				else
				{
					appendStr = " ";
				}
				modifyCount++;
			}
			else if (c == '"')
			{
				appendStr = "\\\"";
				modifyCount++;
			}
			else if (c == '\'')
			{
				appendStr = "\\'";
				modifyCount++;
			}
			else if (c == '\\')
			{
				appendStr = "\\\\";
				modifyCount++;
			}
			else if (c == '<')
			{
				appendStr = "\\074";  // 074 = 0x3C = '<'
				modifyCount++;
			}
			if (modifyCount == 1)
			{
				temp = StringTool.createStringAppender(str.length() + 16);
				temp.append(str.substring(0, i));
				//这里将modifyCount的个数增加, 防止下一次调用使他继续进入这个初始化
				modifyCount++;
			}
			if (modifyCount > 0)
			{
				if (appendStr == null)
				{
					temp.append(c);
				}
				else
				{
					temp.append(appendStr);
				}
			}
		}
		return temp == null ? str : temp.toString();
	}

	/**
	 * 处理字符串, 将其转换为HTML格式的代码, 并直接输出.
	 * 这种方法适合处理比较长的字符串
	 */
	public static void dealString2HTML(String str, Writer out)
			throws IOException
	{
		dealString2HTML(str, out, false);
	}

	/**
	 * 处理字符串, 将其转换为HTML格式的代码, 并直接输出.
	 * 这种方法适合处理比较长的字符串
	 *
	 * @param dealNewLine  是否要将换行"\n"处理成"<br>"
	 */
	public static void dealString2HTML(String str, Writer out, boolean dealNewLine)
			throws IOException
	{
		if (str == null)
		{
			return;
		}
		boolean preSpace = true;
		for (int i = 0; i < str.length(); i++)
		{
			char c = str.charAt(i);
			switch (c)
			{
				case '<':
					out.write("&lt;");
					break;
				case '>':
					out.write("&gt;");
					break;
				case '&':
					out.write("&amp;");
					break;
				case '"':
					out.write("&quot;");
					break;
				case '\'':
					out.write("&#39;");
					break;
				case '\n':
					if (dealNewLine)
					{
						out.write("\n<br>");
						preSpace = true;
					}
					else
					{
						out.write('\n');
					}
					break;
				case ' ':
					if (dealNewLine)
					{
						if (preSpace)
						{
							out.write("&nbsp;");
							preSpace = false;
						}
						else
						{
							out.write(' ');
							preSpace = true;
						}
					}
					else
					{
						out.write(' ');
					}
					break;
				default:
					out.write(c);
			}
			if (c > ' ')
			{
				preSpace = false;
			}
		}
	}

	/**
	 * 处理字符串, 将其转换为HTML格式的代码.
	 */
	public static String dealString2HTML(String str)
	{
		return dealString2HTML(str, false);
	}

	/**
	 * 处理字符串, 将其转换为HTML格式的代码.
	 *
	 * @param dealNewLine  是否要将换行"\n"处理成"<br>"
	 */
	public static String dealString2HTML(String str, boolean dealNewLine)
	{
		if (str == null)
		{
			return "";
		}
		StringAppender temp = null;
		int modifyCount = 0;
		boolean preSpace = true;
		for (int i = 0; i < str.length(); i++)
		{
			char c = str.charAt(i);
			String appendStr = null;
			switch (c)
			{
				case '<':
					appendStr = "&lt;";
					modifyCount++;
					break;
				case '>':
					appendStr = "&gt;";
					modifyCount++;
					break;
				case '&':
					appendStr = "&amp;";
					modifyCount++;
					break;
				case '"':
					appendStr = "&quot;";
					modifyCount++;
					break;
				case '\'':
					appendStr = "&#39;";
					modifyCount++;
					break;
				case '\n':
					if (dealNewLine)
					{
						appendStr = "\n<br>";
						modifyCount++;
						preSpace = true;
					}
					break;
				case ' ':
					if (dealNewLine)
					{
						if (preSpace)
						{
							appendStr = "&nbsp;";
							modifyCount++;
							preSpace = false;
						}
						else
						{
							preSpace = true;
						}
					}
					break;
			}
			if (c > ' ')
			{
				preSpace = false;
			}
			if (modifyCount == 1)
			{
				temp = StringTool.createStringAppender(str.length() + 16);
				temp.append(str.substring(0, i));
				//这里将modifyCount的个数增加, 防止下一次调用使他继续进入这个初始化
				modifyCount++;
			}
			if (modifyCount > 0)
			{
				if (appendStr == null)
				{
					temp.append(c);
				}
				else
				{
					temp.append(appendStr);
				}
			}
		}
		return temp == null ? str : temp.toString();
	}

	/**
	 * 处理字符串, 将其转换为URL格式的代码.
	 */
	public static String dealString2URL(String str)
	{
		return dealString2URL(str, null);
	}

	/**
	 * 处理字符串, 将其转换为URL格式的代码.
	 */
	public static String dealString2URL(String str, String charsetName)
	{
		if (str == null)
		{
			return "";
		}
		StringAppender temp = null;
		int modifyCount = 0;
		for (int i = 0; i < str.length(); i++)
		{
			int c = (int) str.charAt(i);
			String appendStr = null;
			if (charsetName != null && c >= 128)
			{
				try
				{
					byte[] bytes = null;
					/*
					 * If this character represents the start of a Unicode
					 * surrogate pair, then pass in two characters. It's not
					 * clear what should be done if a bytes reserved in the
					 * surrogate pairs range occurs outside of a legal
					 * surrogate pair. For now, just treat it as if it were
					 * any other character.
					 */
					if (c >= 0xD800 && c <= 0xDBFF)
					{
						if ((i + 1) < str.length())
						{
							int d = (int) str.charAt(i + 1);
							if (d >= 0xDC00 && d <= 0xDFFF)
							{
								StringAppender tmpBuf = StringTool.createStringAppender(2)
										.append((char) c).append((char) d);
								bytes = tmpBuf.toString().getBytes(charsetName);
								i++;
							}
						}
						if (bytes == null)
						{
							bytes = String.valueOf((char) c).getBytes(charsetName);
						}
					}
					else
					{
						bytes = String.valueOf((char) c).getBytes(charsetName);
					}
					StringAppender tAS = StringTool.createStringAppender(bytes.length * 3);
					for (int index = 0; index < bytes.length; index++)
					{
						tAS.append('%');
						int tbyte = bytes[index] & 0xff;
						if (tbyte < 16)
						{
							tAS.append('0');
						}
						else
						{
							tAS.append(CODE16[tbyte >> 4]);
						}
						tAS.append(CODE16[tbyte & 0xf]);
					}
					appendStr = tAS.toString();
					modifyCount++;
				}
				catch (UnsupportedEncodingException ex)
				{
					throw new RuntimeException(ex);
				}
			}
			else
			{
				if (c == ' ')
				{
					appendStr = "+";
					modifyCount++;
				}
				else if (!(c >= 'a' && c <= 'z') && !(c >= '0' && c <= '9') && !(c >= 'A' && c <= 'Z')
						&& c != '.' && c != '-' && c != '_' && c != '*')
				{
					char[] tAS = new char[3];
					tAS[0] = '%';
					if (c < 16)
					{
						tAS[1] = '0';
					}
					else
					{
						tAS[1] = CODE16[(c >> 4) & 0xf];
					}
					tAS[2] = CODE16[c & 0xf];
					appendStr = new String(tAS);
					modifyCount++;
				}
			}
			if (modifyCount == 1)
			{
				temp = StringTool.createStringAppender(str.length() + 16);
				temp.append(str.substring(0, i));
				//这里将modifyCount的个数增加, 防止下一次调用使他继续进入这个初始化
				modifyCount++;
			}
			if (modifyCount > 0)
			{
				if (appendStr == null)
				{
					temp.append((char) c);
				}
				else
				{
					temp.append(appendStr);
				}
			}
		}
		return temp == null ? str : temp.toString();
	}

	/**
	 * 获取某月的起始日期字符串, 用于按日期查询.
	 *
	 * @param offset   离本月的偏移月数, 向前为负
	 */
	public static String getMonthFirstDayString(int offset)
	{
		return getMonthDayString(offset, 1);
	}

	/**
	 * 获取某月的起始日期字符串, 用于按日期查询.
	 *
	 * @param offset   离本月的偏移月数, 向前为负
	 * @param monthDay   指定的某月日期, 必须在1和25之间
	 */
	public static String getMonthDayString(int offset, int monthDay)
	{
		if (monthDay < 1 || monthDay > 25)
		{
			monthDay = 1;
		}
		Calendar c = Calendar.getInstance();
		int year = c.get(Calendar.YEAR);
		int month = c.get(Calendar.MONTH);
		month += offset;
		if (month < 0)
		{
			int offYear = (-month) / 12 + 1;
			year -= offYear;
			month += 12 * offYear;
		}
		else if (month >= 12)
		{
			int offYear = month / 12;
			year += offYear;
			month -= 12 * offYear;
		}
		StringAppender date = StringTool.createStringAppender(10);
		return date.append(year).append('-').append(month + 1).append('-')
				.append(monthDay).toString();
	}

	/**
	 * 获取某月的起始日期, 用于按日期查询.
	 *
	 * @param offset   离本月的偏移月数, 向前为负
	 */
	public static Date getMonthFirstDay(int offset)
	{
		return getMonthDay(offset, 1);
	}

	/**
	 * 获取某月的指定日期, 用于按日期查询.
	 *
	 * @param offset     离本月的偏移月数, 向前为负
	 * @param monthDay   指定的某月日期, 必须在1和25之间
	 */
	public static Date getMonthDay(int offset, int monthDay)
	{
		if (monthDay < 1 || monthDay > 25)
		{
			monthDay = 1;
		}
		Calendar c = Calendar.getInstance();
		int year = c.get(Calendar.YEAR);
		int month = c.get(Calendar.MONTH);
		month += offset;
		if (month < 0)
		{
			int offYear = (-month) / 12 + 1;
			year -= offYear;
			month += 12 * offYear;
		}
		else if (month >= 12)
		{
			int offYear = month / 12;
			year += offYear;
			month -= 12 * offYear;
		}
		c.set(year, month, monthDay, 0, 0, 0);
		c.set(Calendar.MILLISECOND, 0);
		return new Date(c.getTimeInMillis());
	}

	/**
	 * 控制一个字符串的长度, 英文字母(0x20~0x7f)算半个长度.
	 * 如果字符串的长度超出, 则只输出长度内的字符串, 并在最后添上"...".
	 */
	public static String formatLength(String str, int length)
	{
		if (str == null)
		{
			return "";
		}
		if (str.length() < length)
		{
			return str;
		}

		int countLimit = length * 2;
		int count = 0;
		int preCount = 0;
		for (int i = 0; i < str.length(); i++)
		{
			preCount = count;
			char c = str.charAt(i);
			if (c >= 0x20 && c < 0x7f)
			{
				count++;
			}
			else
			{
				count += 2;
			}
			if (count > countLimit)
			{
				int end = i - 1;
				c = str.charAt(i - 1);
				if (c >= 0x20 && c < 0x7f && preCount == countLimit)
				{
					end--;
				}
				return str.substring(0, end) + "...";
			}
		}
		return str;
	}

	/**
	 * 解析动态表格传递过来的行数据。
	 * 返回一个List，解析出来的字符串都放在里面。
	 */
	public static List parseDynamicTableValue(String str)
	{
		return separateString(str, '\t');
	}

	/**
	 * 根据分隔符"separate"来分隔一个字符串"str".
	 * 返回一个List，分隔出来的字符串都放在里面。
	 */
	public static List separateString(String str, char separate)
	{
		if (str == null)
		{
			return null;
		}
		int count = str.length();
		if (count == 0)
		{
			return Collections.EMPTY_LIST;
		}

		List list = new ArrayList();
		int i = 0;
		int begin = 0;
		while (i < count)
		{
			if (str.charAt(i) == separate)
			{
				list.add(str.substring(begin, i));
				begin = ++i;
			}
			else
			{
				i++;
			}
		}
		list.add(str.substring(begin, i));
		return list;
		/*
		原来旧的实现
		String strSep = separate + "";
		StringTokenizer token = new StringTokenizer(str, strSep, true);
		ArrayList list = new ArrayList();
		String nowValue = "";
		while (token.hasMoreTokens())
		{
			String temp = token.nextToken();
			if (temp.equals(strSep))
			{
				list.add(nowValue);
				nowValue = "";
			}
			else
			{
				nowValue = temp;
			}
		}
		list.add(nowValue);
		return list;
		*/
	}

	/**
	 * 将一个字符串数组"arr"用一个给定的字符串"linkChar"链接起来。
	 * 注: 返回值的最后包括一个连接字符串。
	 */
	public static String linkStringArray(String[] arr, String linkChar)
	{
		if (arr == null)
		{
			return null;
		}
		StringAppender buf = StringTool.createStringAppender(
				linkChar.length() * arr.length + arr.length * 8);
		for (int i = 0; i < arr.length; i++)
		{
			buf.append(arr[i]);
			buf.append(linkChar);
		}
		return buf.toString();
	}

	/**
	 * 分解选项中的数据，格式为：每条记录用“;”分割，每列用“,”分割。
	 * 返回一个List，每条记录作为一个String数组存放在List中。
	 */
	public static List parseSelection(String selectionStr)
	{
		StringTokenizer token = new StringTokenizer(selectionStr, ";");
		ArrayList list = new ArrayList();
		while (token.hasMoreTokens())
		{
			String tempStr = token.nextToken();
			StringTokenizer subToken = new StringTokenizer(tempStr, ",");
			String[] record = new String[subToken.countTokens()];
			for (int i = 0; i < record.length; i++)
			{
				record[i] = subToken.nextToken();
			}
			list.add(record);
		}
		return list;
	}

	/**
	 * 将一个Iterator转换为List。
	 */
	public static List iterator2List(Iterator itr)
	{
		LinkedList list = new LinkedList();
		while (itr.hasNext())
		{
			list.add(itr.next());
		}
		return list;
	}

	/**
	 * 处理为null的对象的字符串转换
	 */
	public static String dealNull(Object str)
	{
		return str == null ? "" : str.toString();
	}

	/**
	 * 根据name读取row中的数据, 并将null作为空的字符串.
	 * 如果row为null, 则直接返回空字符串.
	 *
	 * @param toHTML   是否要处理HTML的字符
	 */
	public static String getResult(ResultRow row, String name, boolean toHTML)
			throws SQLException, ConfigurationException
	{
		if (row == null)
		{
			return "";
		}
		String str = row.getFormated(name).toString();
		return str == null ? "" : toHTML ? Utils.dealString2HTML(str) : str;
	}

	public static abstract class Print
	{
		public void print(String name)
				throws SQLException, IOException, ConfigurationException
		{
			this.print(name, true);
		}

		public void print(String name, String defaultVale)
				throws SQLException, IOException, ConfigurationException
		{
			this.print(name, true, defaultVale);
		}

		public void print(String name, boolean toHTML)
				throws SQLException, IOException, ConfigurationException
		{
			this.print(name, toHTML, null);
		}

		public abstract void print(String name, boolean toHTML, String defaultVale)
				throws SQLException, IOException, ConfigurationException;
	}

	public static class ResultPrint extends Print
	{
		private Writer out;
		private ResultRow row;

		public ResultPrint(Writer out, Object obj)
				throws SQLException, ConfigurationException
		{
			this.out = out;
			this.row = null;
			if (obj != null)
			{
				if (obj instanceof ResultRow)
				{
					this.row = (ResultRow) obj;
				}
				else if (obj instanceof ResultIterator)
				{
					ResultIterator ritr = (ResultIterator) obj;
					if (ritr.hasMoreRow())
					{
						this.row = ritr.nextRow();
					}
				}
			}
		}

		public ResultPrint(Writer out, ResultRow row)
		{
			this.out = out;
			this.row = row;
		}

		public void print(String name, boolean toHTML, String defaultVale)
				throws SQLException, IOException, ConfigurationException
		{
			if (this.row == null)
			{
				if (defaultVale != null)
				{
					this.out.write(defaultVale);
				}
				return;
			}
			if (toHTML)
			{
				Utils.dealString2HTML(this.row.getFormated(name).toString(), this.out, true);
			}
			else
			{
				this.out.write(this.row.getFormated(name).toString());
			}
		}

	}

	public static class ConditionPrint extends Print
	{
		private Writer out;
		private SearchManager searchManager;

		public ConditionPrint(Writer out, Object searchManager)
		{
			this.out = out;
			if (searchManager instanceof SearchManager)
			{
				this.searchManager = (SearchManager) searchManager;
			}
		}

		public ConditionPrint(Writer out, SearchManager searchManager)
		{
			this.out = out;
			this.searchManager = searchManager;
		}

		public void print(String name, boolean toHTML, String defaultVale)
				throws SQLException, IOException
		{
			if (this.searchManager == null)
			{
				if (defaultVale != null)
				{
					this.out.write(defaultVale);
				}
				return;
			}
			SearchManager.Condition condition = this.searchManager.getCondition(name);
			if (condition == null || condition.value == null)
			{
				return;
			}
			if (toHTML)
			{
				Utils.dealString2HTML(condition.value, this.out, true);
			}
			else
			{
				this.out.write(condition.value);
			}
		}

	}

	public static void main(String[] args)
			throws Exception
	{
		/*
		System.out.println(dealString2HTML("<a>"));
		System.out.println(Utility.getProperty("self.micromagic.logger.level", "INFO"));
		Log log = Utility.createLog("test");
		System.out.println(Level.parse(Utility.getProperty("self.micromagic.logger.level", "INFO")));
		System.out.println(((Jdk14Logger) log).getLogger().getLevel());
		System.out.println(log.isDebugEnabled());
		System.out.println(log.isInfoEnabled());
		java.lang.reflect.Field f = String.class.getDeclaredField("value");
		f.setAccessible(true);
		String str = "123";
		char[] buf = (char[]) f.get(str);
		System.out.println(str);
		buf[0] = 'd';
		System.out.println(str);
		*/
	}

}