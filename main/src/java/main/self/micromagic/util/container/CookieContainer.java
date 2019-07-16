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

package self.micromagic.util.container;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.iterators.IteratorEnumeration;

import self.micromagic.coder.Base64;
import self.micromagic.util.StringTool;
import self.micromagic.util.Utility;
import self.micromagic.util.ref.BooleanRef;
import self.micromagic.util.ref.StringRef;

/**
 * cookie信息的容器.
 */
public class CookieContainer extends AbstractContainerSetting
		implements ValueContainer
{
	/**
	 * 标明cookie的数据是否为压缩存储的前缀.
	 */
	public static final String COMPRESS_VALUE_PREFIX = "--zip-";

	/**
	 * 压缩时使用的字符集.
	 */
	private static final String COMPRESS_CHARSET = "UTF-8";

	private static final char[] BASE64_CHARS = {
		'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
		'a', 'b', 'c', 'e', 'd', 'f', 'g', 'h', 'i', 'j',
		'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't',
		'u', 'v', 'w', 'x', 'y', 'z', 'A', 'B', 'C', 'E',
		'D', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N',
		'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X',
		'Y', 'Z', '_', '+', '.'
	};

	/**
	 * 对压缩后的编码进行转码的base64编码的实例.
	 */
	private static final Base64 COMPRESS_CODER = new Base64(BASE64_CHARS);

	private final HttpServletRequest request;
	private HttpServletResponse response;

	/**
	 * 用于保存cookie对象.
	 */
	private Map cookieMap;

	/**
	 * 是否要对保存的数据进行压缩.
	 */
	private boolean compressValue = true;

	/**
	 * 需要进行压缩的最小尺寸.
	 */
	private int minCompressSize = 32;

	/**
	 * 通过此构造函数创建的容器只能进行读取.
	 *
	 * @param request  cookie信息所在的request对象
	 */
	public CookieContainer(HttpServletRequest request)
	{
		if (request == null)
		{
			throw new NullPointerException("Request can't be null.");
		}
		this.request = request;
		this.parseValue = true;
		this.parseSuffix = "*";
	}

	/**
	 * 通过此构造函数创建的容器才能进行写入.
	 *
	 * @param request   cookie信息所在的request对象
	 * @param response  设置cookie信息所需要的response对象
	 */
	public CookieContainer(HttpServletRequest request, HttpServletResponse response)
	{
		this(request);
		this.response = response;
	}

	/**
	 * 初始化cookie信息.
	 * 如果不初始化cookie信息每次获取都将遍历一次cookie.
	 */
	public void initCookie()
	{
		if (this.cookieMap != null)
		{
			return;
		}
		this.cookieMap = new HashMap();
		Cookie[] cookies = this.request.getCookies();
		if (cookies == null)
		{
			return;
		}
		for (int i = 0; i < cookies.length; i++)
		{
			Cookie cookie = cookies[i];
			this.cookieMap.put(this.decodeStr(cookie.getName()), cookie);
		}
	}

	/**
	 * 在以String类型设置cookie的值时, 是否要进行压缩.
	 */
	public boolean isCompressValue()
	{
		return this.compressValue;
	}

	/**
	 * 设置在以String类型设置cookie的值时, 是否要进行压缩.
	 */
	public void setCompressValue(boolean compressValue)
	{
		this.compressValue = compressValue;
	}

	/**
	 * 在以String类型设置cookie的值时, 需要压缩的最小尺寸.
	 * 即字符串的长度大于这个值时才进行压缩.
	 */
	public int getMinCompressSize()
	{
		return this.minCompressSize;
	}

	/**
	 * 设置在以String类型设置cookie的值时, 需要压缩的最小尺寸.
	 * 即字符串的长度大于这个值时才进行压缩.
	 */
	public void setMinCompressSize(int size)
	{
		this.minCompressSize = size;
	}

	private String encodeStr(String value)
	{
		return this.encodeStr(value, this.response.getCharacterEncoding());
	}

	private String decodeStr(String value)
	{
		return this.decodeStr(value, this.request.getCharacterEncoding());
	}

	public Object getValue(Object key)
	{
		if (key == null)
		{
			return null;
		}
		String name = key.toString();
		if (this.parseValue)
		{
			if (name != null && name.endsWith(this.parseSuffix))
			{
				return this.findCookie(
						name.substring(0, name.length() - this.parseSuffix.length()));
			}
			else
			{
				Cookie cookie = this.findCookie(name);
				if (cookie == null)
				{
					return null;
				}
				String value = cookie.getValue();
				if (value != null && value.startsWith(COMPRESS_VALUE_PREFIX))
				{
					return this.doInflater(value.substring(COMPRESS_VALUE_PREFIX.length()));
				}
				return this.decodeStr(cookie.getValue());
			}
		}
		else
		{
			return this.findCookie(name);
		}
	}

	/**
	 * 如果cookie已初始化, 则从cookieMap中获取, 如果未初始化, 则从
	 * request中遍历需要的cookie.
	 */
	private Cookie findCookie(String name)
	{
		if (this.cookieMap != null)
		{
			return (Cookie) this.cookieMap.get(name);
		}
		name = this.encodeStr(name, this.request.getCharacterEncoding());
		Cookie[] cookies = this.request.getCookies();
		if (cookies == null)
		{
			return null;
		}
		for (int i = 0; i < cookies.length; i++)
		{
			Cookie cookie = cookies[i];
			if (name.equals(cookie.getName()))
			{
				return cookie;
			}
		}
		return null;
	}

	public boolean containsKey(Object key)
	{
		if (this.cookieMap == null)
		{
			return this.getValue(key) != null;
		}
		return this.cookieMap.containsKey(key);
	}

	public void setValue(Object key, Object value)
	{
		if (this.response == null)
		{
			throw new UnsupportedOperationException();
		}
		if (key == null)
		{
			throw new NullPointerException("Key can't be null.");
		}
		if (this.cookieMap == null)
		{
			this.initCookie();
		}
		String name = key.toString();
		Cookie cookie = null;
		if (value == null)
		{
			Cookie oldCookie = (Cookie) this.cookieMap.get(name);
			if (oldCookie != null)
			{
				cookie = new Cookie(this.encodeStr(name), "");
				cookie.setMaxAge(0);
				String oldDomain = oldCookie.getDomain();
				if (!StringTool.isEmpty(oldDomain)
						&& !(oldDomain.equals(this.request.getServerName())))
				{
					// 存在原始的域, 且与当前服务器host不同, 才重新设置域
					cookie.setDomain(oldDomain);
				}
				if (oldCookie.getPath() != null)
				{
					cookie.setPath(oldCookie.getPath());
				}
				else
				{
					cookie.setPath(this.request.getContextPath().concat("/"));
				}
			}
		}
		else if (value instanceof Cookie)
		{
			cookie = (Cookie) value;
			String cookieName = this.decodeStr(cookie.getName(),
					this.response.getCharacterEncoding());
			if (!(name.equals(cookieName)))
			{
				throw new IllegalArgumentException("The cookie name not same, name:["
						+ name + "], cookie:[" + cookieName + "]");
			}
		}
		else
		{
			String str = value.toString();
			if (this.compressValue)
			{
				BooleanRef done = new BooleanRef();
				StringRef encoded = new StringRef(null);
				String tmpStr = this.doDeflater(str, done, encoded);
				if (done.value)
				{
					// 已压缩且压缩后的尺寸小于原始尺寸的, 才作为压缩存放
					str = COMPRESS_VALUE_PREFIX.concat(tmpStr);
				}
				else
				{
					tmpStr = encoded.getString();
					str = tmpStr == null ? this.encodeStr(str) : tmpStr;
				}
			}
			else
			{
				str = this.encodeStr(str);
			}
			cookie = new Cookie(this.encodeStr(name), str);
			cookie.setPath(this.request.getContextPath().concat("/"));
		}
		if (cookie != null)
		{
			if (this.checkSecure())
			{
				cookie.setSecure(true);
			}
			this.response.addCookie(cookie);
			if (cookie.getMaxAge() == 0)
			{
				this.cookieMap.remove(name);
			}
			else
			{
				this.cookieMap.put(name, cookie);
			}
		}
	}

	private boolean checkSecure()
	{
		if ("https".equalsIgnoreCase(this.request.getScheme()))
		{
			return true;
		}
		String scheme = this.request.getHeader("X-Forwarded-Proto");
		return "https".equalsIgnoreCase(scheme);
	}

	public void removeValue(Object key)
	{
		if (this.response == null)
		{
			throw new UnsupportedOperationException();
		}
		this.setValue(key, null);
	}

	public Enumeration getKeys()
	{
		if (this.cookieMap == null)
		{
			this.initCookie();
		}
		return new IteratorEnumeration(this.cookieMap.keySet().iterator());
	}

	public Object[] getKeyValuePairs()
	{
		return null;
	}

	/**
	 * 对被压缩的字符串进行解压操作.
	 */
	private String doInflater(String str)
	{
		try
		{
			byte[] buf = COMPRESS_CODER.base64ToByteArray(str);
			ByteArrayInputStream byteIn = new ByteArrayInputStream(buf);
			InflaterInputStream in = new InflaterInputStream(byteIn);
			ByteArrayOutputStream byteOut = new ByteArrayOutputStream(
					str.length() * 3 + 128);
			Utility.copyStream(in, byteOut);
			in.close();
			return byteOut.toString(COMPRESS_CHARSET);
		}
		catch (IOException ex)
		{
			// 这里不会出现IO异常因为全是内存操作
			throw new Error();
		}
	}

	/**
	 * 对字符串进行压缩操作.
	 */
	private String doDeflater(String str, BooleanRef done, StringRef encoded)
	{
		done.value = false;
		if (str.length() < this.minCompressSize)
		{
			return str;
		}
		try
		{
			ByteArrayOutputStream byteOut = new ByteArrayOutputStream(str.length() + 128);
			DeflaterOutputStream out = new DeflaterOutputStream(byteOut);
			byte[] buf = str.getBytes(COMPRESS_CHARSET);
			out.write(buf);
			out.close();
			int outSize = byteOut.size() * 4 / 3 + 8;
			if (outSize > str.length() && outSize > buf.length)
			{
				// 压缩加Base64后的字节数大于编码后的字节, 不使用压缩
				String tmp = this.encodeStr(str);
				encoded.setString(tmp);
				if (outSize > tmp.length())
				{
					return str;
				}
			}
			byte[] result = byteOut.toByteArray();
			done.value = true;
			return COMPRESS_CODER.byteArrayToBase64(result);
		}
		catch (IOException ex)
		{
			// 这里不会出现IO异常因为全是内存操作
			throw new Error();
		}
	}

}
