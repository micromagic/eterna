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

package self.micromagic.eterna.share;

public class EternaException extends RuntimeException
{
	/**
	 * 构造一个<code>EternaException</code>.
	 */
	public EternaException()
	{
		super();
	}

	/**
	 * 通过参数<code>message</code>来构造一个<code>EternaException</code>.
	 *
	 * @param message   出错信息
	 */
	public EternaException(String message)
	{
		super(message);
	}

	/**
	 * 通过一个抛出的对象来构造一个<code>EternaException</code>.
	 *
	 * @param origin    异常或错误
	 */
	public EternaException(Throwable origin)
	{
		super(origin);
	}

	/**
	 * 通过参数<code>message</code>和一个抛出的对象来构造一个<code>EternaException</code>.
	 *
	 * @param message   出错信息
	 * @param origin    异常或错误
	 */
	public EternaException(String message, Throwable origin)
	{
		super(message, origin);
	}

	public String getMessage()
	{
		String msg = super.getMessage();
		if (msg == null && this.getCause() != null)
		{
			msg = this.getCause().getMessage();
		}
		return msg;
	}

	private static final long serialVersionUID = 1L;

}