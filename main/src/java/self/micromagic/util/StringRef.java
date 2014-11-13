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

import java.io.Serializable;

public class StringRef extends ObjectRef
		implements Serializable
{
	public StringRef()
	{
		super("");
	}

	public StringRef(String str)
	{
		super(str);
	}

	public boolean isString()
	{
		return true;
	}

	public static String getStringValue(Object obj)
	{
		return obj == null ? null : obj.toString();
	}

	public void setObject(Object obj)
	{
		super.setObject(StringRef.getStringValue(obj));
	}

	public void setString(String str)
	{
		this.setObject(str);
	}

	public String getString()
	{
		return (String) this.getObject();
	}

}