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

import java.util.Map;
import java.util.HashMap;

import javax.servlet.jsp.tagext.TagSupport;

import self.micromagic.eterna.view.ViewAdapter;
import self.micromagic.eterna.share.EternaException;

/**
 * @author micromagic@sina.com
 */
public class InitBaseTag extends TagSupport
{
	/**
	 * 在_eterna.cache中存放区分多个同名控件时使用的后缀的名称.
	 */
	public static final String SUFFIX_ID_FLAG = "eSuffixId";

	/**
	 * 在_eterna.cache中存放根控件的名称.
	 */
	public static final String ROOT_OBJ_ID_FLAG = "eRootObjId";

	/**
	 * 在_eterna.cache中存放模板根控件标记的名称.
	 */
	public static final String SCATTER_FLAG = "scatterFlag";


	private String parentElement;
	private String suffixId;
	private boolean useAJAX;
	private String scatterFlag;

	protected Map getCacheMap(ViewAdapter view)
			throws EternaException
	{
		Map cache = new HashMap();
		if (this.suffixId != null)
		{
			cache.put(SUFFIX_ID_FLAG, this.suffixId);
		}
		if (this.parentElement != null)
		{
			cache.put(ROOT_OBJ_ID_FLAG, this.parentElement);
		}
		if (this.scatterFlag != null)
		{
			cache.put(SCATTER_FLAG, this.scatterFlag);
		}
		String width = view.getWidth();
		String height = view.getHeight();
		if (width != null)
		{
			cache.put(ROOT_OBJ_ID_FLAG + ".width", width);
		}
		if (height != null)
		{
			cache.put(ROOT_OBJ_ID_FLAG + ".height", height);
		}
		return cache.size() > 0 ? cache : null;
	}

	public void release()
	{
		this.parentElement = null;
		this.suffixId = null;
		this.useAJAX = false;
		this.scatterFlag = null;
		super.release();
	}

	public String getParentElement()
	{
		return this.parentElement;
	}

	public void setParentElement(String parentElement)
	{
		this.parentElement = parentElement;
	}

	public String getSuffixId()
	{
		return this.suffixId;
	}

	public void setSuffixId(String suffixId)
	{
		this.suffixId = suffixId;
	}

	public boolean isUseAJAX()
	{
		return this.useAJAX;
	}

	public void setUseAJAX(boolean useAJAX)
	{
		this.useAJAX = useAJAX;
	}

	public String getScatterFlag()
	{
		return this.scatterFlag;
	}

	public void setScatterFlag(String scatterFlag)
	{
		this.scatterFlag = scatterFlag;
	}

}