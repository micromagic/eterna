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

import javax.servlet.jsp.tagext.TagSupport;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

import self.micromagic.util.ResManager;

/**
 * 在JSP中, 可通过此标签载入一个资源文件.
 *
 * @author micromagic@sina.com
 */
public class EternaRes extends TagSupport
{
	/**
	 * 在request范围内, 存放载入资源方法已初始化的名称.
	 */
	public static final String LOAD_RES_INITED_FLAG = "___loadResInited";

   /**
	 * 初始化的JS脚本.
	 */
	private static final String RES_JS;
	static
	{
		String js = "";
		try
		{
			ResManager res = new ResManager();
			res.load(EternaInit.class.getResourceAsStream("js.res"));
			js = res.getRes("res.js");
		}
		catch (Exception ex)
		{
			DefaultFinder.log.error("Error in get js res.", ex);
		}
		RES_JS = js;
	}

	private String url;
	private String charset;
	private boolean jsResource = true;
	private String scriptParam;

	public int doStartTag()
			throws JspException
	{
		try
		{
			JspWriter out = this.pageContext.getOut();
			out.println("<script type=\"text/javascript\">");
			String inited = (String) this.pageContext.getAttribute(
					LOAD_RES_INITED_FLAG, PageContext.REQUEST_SCOPE);
			if (inited == null)
			{
				this.pageContext.setAttribute(LOAD_RES_INITED_FLAG, "1", PageContext.REQUEST_SCOPE);
				//this.printLoadResScript(out);
				out.println(RES_JS);
			}
			String charsetDef = this.charset == null ? "" : ", \"" + this.charset + "\"";
			String tmpURL = this.url;
			if (tmpURL.startsWith("/"))
			{
				ServletRequest req = this.pageContext.getRequest();
				if (req instanceof HttpServletRequest)
				{
					tmpURL = ((HttpServletRequest) req).getContextPath() + this.url;
				}
			}
			String params;
			if (this.jsResource && this.scriptParam != null)
			{
				params = this.scriptParam + ", \"" + tmpURL + "\"" + charsetDef;
			}
			else
			{
				params = this.jsResource + ", \"" + tmpURL + "\"" + charsetDef;
			}
			out.println( "window.ef_loadResource(" + params + ");");
			out.println("</script>");
		}
		catch (Throwable ex)
		{
			DefaultFinder.log.error("Other Error in service.", ex);
		}
		return SKIP_BODY;
	}

	public void release()
	{
		this.url = null;
		this.charset = null;
		this.jsResource = true;
		this.scriptParam = null;
		super.release();
	}

	public boolean isJsResource()
	{
		return this.jsResource;
	}

	public void setJsResource(boolean jsResource)
	{
		this.jsResource = jsResource;
	}

	public String getScriptParam()
	{
		return this.scriptParam;
	}

	public void setScriptParam(String param)
	{
		this.scriptParam = param;
	}

	public String getUrl()
	{
		return this.url;
	}

	public void setUrl(String url)
	{
		this.url = url;
	}

	public String getCharset()
	{
		return this.charset;
	}

	public void setCharset(String charset)
	{
		this.charset = charset;
	}

}