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

package self.micromagic.eterna.tag;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;

import self.micromagic.eterna.model.AppData;
import self.micromagic.eterna.share.EternaException;
import self.micromagic.eterna.view.View;
import self.micromagic.util.ResManager;
import self.micromagic.util.StringTool;

/**
 * 在JSP中, 可通过此标签在页面中初始化Eterna对象.
 *
 * @author micromagic@sina.com
 */
public class EternaInit extends InitBaseTag
{
	/**
	 * 默认存放view对象的标签.
	 */
	public static final String VIEW_TAG = "eterna.view";
	/**
	 * 默认存放AppData对象的标签.
	 */
	public static final String APPDATA_TAG = "eterna.appData";

	/**
	 * 不输出html代码.
	 */
	public static final int PRINT_HTML_NONE = 0;

	/**
	 * 仅输出eterna容器对象的div代码.
	 */
	public static final int PRINT_HTML_PART = 1;

	/**
	 * 输出完整的html页面代码.
	 */
	public static final int PRINT_HTML_ALL = 2;

	/**
	 * 参数中控制debug的参数名.
	 */
	public static final String PARAM_DEBUF_FLAG = "___debug";

	/**
	 * 初始化的JS脚本.
	 */
	private static final String INIT_JS;
	static
	{
		String js = "";
		try
		{
			ResManager res = new ResManager();
			res.load(EternaInit.class.getResourceAsStream("js.res"));
			js = res.getRes("init.js");
		}
		catch (Exception ex)
		{
			log.error("Error in get js res.", ex);
		}
		INIT_JS = js;
	}

	private String view;
	private String appData;
	private int printHTML;
	private String charset;
	private String docType;
	private String divClass;
	private boolean includeBody = false;
	private String includeBegin;
	private String includeEnd;

	public int doStartTag()
			throws JspException
	{
		try
		{
			String viewTag = this.view == null ? VIEW_TAG : this.view;
			String appDataTag = this.appData == null ? APPDATA_TAG : this.appData;
			JspWriter out = this.pageContext.getOut();
			View view = (View) this.pageContext.findAttribute(viewTag);
			AppData data = (AppData) this.pageContext.findAttribute(appDataTag);
			String dataType = view.getDataType(data);
			if (View.DATA_TYPE_WEB.equals(dataType))
			{
				this.includeBody = true;
				this.printInitPage(view, data, out);
				return EVAL_BODY_INCLUDE;
			}
			else
			{
				this.includeBody = false;
				view.printView(out, data, this.getCacheMap(view));
			}
		}
		catch (EternaException ex)
		{
			log.warn("Error in init.", ex);
		}
		catch (Throwable ex)
		{
			log.error("Other Error in init.", ex);
		}
		return SKIP_BODY;
	}

	public int doEndTag()
			throws JspException
	{
		if (!this.includeBody)
		{
			return EVAL_PAGE;
		}
		try
		{
			JspWriter out = this.pageContext.getOut();
			if (this.printHTML == PRINT_HTML_ALL)
			{
				out.println("</head>");
				out.println("<body>");
				if (!StringTool.isEmpty(this.includeBegin))
				{
					this.pageContext.include(this.includeBegin);
				}
			}
			if (this.printHTML >= PRINT_HTML_PART)
			{
				String divName = this.getParentElement();
				String sId = this.getSuffixId();
				if (sId != null)
				{
					divName += sId;
				}
				String classDef = this.divClass == null ? "" : " class=\"" + this.divClass + "\"";
				out.println("<div id=\"" + divName + "\"" + classDef + "></div>");
			}
			if (this.printHTML == PRINT_HTML_ALL)
			{
				if (!StringTool.isEmpty(this.includeEnd))
				{
					this.pageContext.include(this.includeEnd);
				}
				out.println("</body>");
				out.println("</html>");
			}
		}
		catch (Throwable ex)
		{
			log.error("Other Error in init.", ex);
		}
		return EVAL_PAGE;
	}

	/**
	 * 输出初始化的脚本.
	 */
	private void printInitScript(View view, AppData data, JspWriter out)
			throws IOException, EternaException
	{
		out.println("<script type=\"text/javascript\">");
		out.println("(function() {");
		this.printEternaScript(view, data, out);
		out.println(INIT_JS);
		out.println("})();");
		out.println("</script>");
	}

	/**
	 * 打印Eterna的初始化脚本.
	 */
	private void printEternaScript(View view, AppData data, JspWriter out)
			throws IOException, EternaException
	{
		// 定义初始化Eterna的变量
		out.print("var $E = ");
		view.printView(out, data, this.getCacheMap(view));
		out.println(';');
		out.println("var eternaData = $E;");
		out.println("var needAJAX = " + this.isUseAJAX() + ";");
		String debug = this.pageContext.getRequest().getParameter(PARAM_DEBUF_FLAG);
		if (debug == null)
		{
			debug = view.getDebug() + "";
		}
		else if (!this.checkDebugStr(debug))
		{
			debug = "0";
		}
		out.println("var eterna_debug = " + debug + ";");
		out.println("var _eterna;");
	}

	/**
	 * 检查debug的字符串格式是否正确.
	 */
	private boolean checkDebugStr(String debug)
	{
		if (debug.length() > 2 && debug.charAt(0) == '0' && Character.toLowerCase(debug.charAt(1)) == 'x')
		{
			try
			{
				Integer.parseInt(debug.substring(2), 16);
				return true;
			}
			catch (NumberFormatException ex)
			{
				return false;
			}
		}
		else
		{
			try
			{
				Integer.parseInt(debug);
				return true;
			}
			catch (NumberFormatException ex)
			{
				return false;
			}
		}
	}

	/**
	 * 输出初始化的页面.
	 */
	private void printInitPage(View view, AppData data, JspWriter out)
			throws IOException, EternaException
	{
		if (this.printHTML == PRINT_HTML_ALL)
		{
			if (this.docType == null)
			{
				out.println("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\""
						+ " \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">");
			}
			else
			{
				out.println(this.docType);
			}
			String charset = this.charset == null ? "UTF-8" : this.charset;
			out.println("<html xmlns=\"http://www.w3.org/1999/xhtml\">");
			out.println("<head>");
			out.println("<meta http-equiv=\"content-type\" content=\"text/html; charset=" + charset + "\"/>");
			out.println("<meta http-equiv=\"pragma\" content=\"no-cache\"/>");
		}
		this.printInitScript(view, data, out);
	}

	public void release()
	{
		this.view = null;
		this.appData = null;
		this.printHTML = 0;
		this.charset = null;
		this.docType = null;
		this.divClass = null;
		this.includeBody = false;
		this.includeBegin = null;
		this.includeEnd = null;
		super.release();
	}

	public String getView()
	{
		return this.view;
	}

	public void setView(String view)
	{
		this.view = view;
	}

	public String getAppData()
	{
		return this.appData;
	}

	public void setAppData(String appData)
	{
		this.appData = appData;
	}

	public int getPrintHTML()
	{
		return this.printHTML;
	}

	public void setPrintHTML(int printHTML)
	{
		this.printHTML = printHTML;
	}

	public String getCharset()
	{
		return this.charset;
	}

	public void setCharset(String charset)
	{
		this.charset = charset;
	}

	public String getDocType()
	{
		return this.docType;
	}

	public void setDocType(String docType)
	{
		this.docType = docType;
	}

	public String getDivClass()
	{
		return this.divClass;
	}

	public void setDivClass(String divClass)
	{
		this.divClass = divClass;
	}

	public boolean isIncludeBody()
	{
		return this.includeBody;
	}

	public void setIncludeBody(boolean includeBody)
	{
		this.includeBody = includeBody;
	}

	public String getIncludeEnd()
	{
		return this.includeEnd;
	}

	public void setIncludeEnd(String includeEnd)
	{
		this.includeEnd = includeEnd;
	}

	private static final long serialVersionUID = 1L;

}