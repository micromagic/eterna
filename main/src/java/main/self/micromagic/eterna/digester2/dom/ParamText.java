
package self.micromagic.eterna.digester2.dom;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import self.micromagic.eterna.digester2.ParseException;
import self.micromagic.eterna.share.EternaException;
import self.micromagic.util.MultiLineText;
import self.micromagic.util.StringAppender;
import self.micromagic.util.StringTool;

/**
 * 一个可定义参数的文本.
 */
public class ParamText
{
	protected Map parameters = new HashMap();
	protected Map defines = new HashMap();
	protected List parseList = new ArrayList();

	private Reader reader;
	private char charBuf[];
	private int charIndex = 0;
	private int charCount;

	private final static int BUFFER_SIZE = 1024;

	/**
	 * 当前处理所处的状态.
	 */
	private int status;

	/**
	 * 普通状态, 读取的是普通文本.
	 */
	private static final int STATUS_COMMON = 0;
	/**
	 * 定义状态, 读取的是常量定义.
	 */
	private static final int STATUS_DEFINE = 1;
	/**
	 * 参数状态, 读取的是参数定义.
	 */
	private static final int STATUS_PARAM = 2;
	/**
	 * 文本状态, 读取的是常量的文本.
	 */
	private static final int STATUS_LINES = 3;

	/**
	 * 获取所有定义的参数.
	 */
	public Parameter[] getParams()
	{
		Parameter[] pArr = new Parameter[this.parameters.size()];
		this.parameters.values().toArray(pArr);
		return pArr;
	}

	/**
	 * 根据名称获取一个定义的参数.
	 */
	public Parameter getParameter(String name)
	{
		Parameter r = (Parameter) this.parameters.get(name);
		if (r == null)
		{
			throw new ParseException("The not found the param [" + name + "].");
		}
		return r;
	}

	/**
	 * 根据名称设置一个参数的值.
	 */
	public void setParameter(String name, String value)
	{
		this.getParameter(name).setValue(value);
	}

	/**
	 * 获取所有定义的常量的名称.
	 */
	public String[] getDefineNames()
	{
		String[] names = new String[this.defines.size()];
		this.defines.keySet().toArray(names);
		return names;
	}

	/**
	 * 获取指定名称的常量的值.
	 */
	public String getDefine(String name)
	{
		String v = (String) this.defines.get(name);
		if (v == null)
		{
			throw new ParseException("The not found the define [" + name + "].");
		}
		return v;
	}

	/**
	 * 对一个输入流进行解析.
	 */
	public void parse(InputStream in)
			throws IOException
	{
		InputStream stream = in;
		if (!in.markSupported())
		{
			stream = new BufferedInputStream(in);
		}
		String charset = EternaSAXReader.getEncoding(stream);
		this.reader = new InputStreamReader(stream, charset);
		this.doParse();
	}

	/**
	 * 对一个字符流进行解析.
	 */
	public void parse(Reader reader)
			throws IOException
	{
		this.reader = reader;
		this.doParse();
	}

	/**
	 * 获取下一个字符.
	 */
	private int getNextChar()
	{
		if (this.charBuf == null)
		{
			this.charCount = this.refreshBuf(0);
		}
		if (this.charIndex < 0)
		{
			this.charIndex = 0;
		}
		if (this.charIndex >= this.charCount)
		{
			this.charCount = refreshBuf(0);
			this.charIndex = 0;
		}
		if (this.charIndex < this.charCount)
		{
			return this.charBuf[this.charIndex++];
		}
		return -1;
	}

	/**
	 * 读取并刷新字符的缓存区.
	 */
	private int refreshBuf(int begin)
	{
		if (this.charBuf == null)
		{
			this.charBuf = new char[BUFFER_SIZE];
		}
		int len = 0;
		try
		{
			len = this.reader.read(this.charBuf, begin, this.charBuf.length - begin);
			if (len == -1)
			{
				this.reader.close();
				this.reader = null;
			}
		}
		catch (IOException ex)
		{
			throw new ParseException(ex);
		}
		return len + begin;
	}

	/**
	 * 读取指定个数的字符且不移动游标的位置.
	 */
	private char[] getChars(int count)
	{
		if (this.charBuf == null)
		{
			this.charCount = this.refreshBuf(0);
		}
		char[] r = new char[count];
		if (this.charIndex + count <= this.charCount)
		{
			System.arraycopy(this.charBuf, this.charIndex, r, 0, r.length);
			return r;
		}
		if (this.reader != null)
		{
			int leftCount = this.charCount - this.charIndex;
			// 如果还有字符未读完, 则将尾部复制到前面, 再刷新缓存
			System.arraycopy(this.charBuf, this.charIndex, this.charBuf, 0, leftCount);
			this.charCount = this.refreshBuf(leftCount);
			this.charIndex = 0;
		}
		if (this.charIndex + count <= this.charCount)
		{
			System.arraycopy(this.charBuf, this.charIndex, r, 0, r.length);
		}
		else
		{
			System.arraycopy(this.charBuf, this.charIndex,
					r, 0, this.charCount - this.charIndex);
		}
		return r;
	}

	/**
	 * 检查给出的字符串是否和缓存中的起始部分相同, 如果相同则读取
	 * 与字符串长度相同的字符.
	 */
	private boolean checkFlag(String str, char[] tmpBuf)
	{
		int count = str.length();
		for (int i = 0; i < count; i++)
		{
			if (tmpBuf[i] != str.charAt(i))
			{
				return false;
			}
		}
		for (int i = 0; i < count; i++)
		{
			this.getNextChar();
		}
		return true;
	}

	/**
	 * 状态检查.
	 */
	private boolean statusCheck()
	{
		char[] tmpBuf = this.getChars(8);
		if (this.checkFlag("define${", tmpBuf))
		{
			this.status = STATUS_DEFINE;
			return true;
		}
		if (this.checkFlag("param${", tmpBuf))
		{
			this.status = STATUS_PARAM;
			return true;
		}
		return false;
	}

	/**
	 * 解析字符流.
	 */
	private void doParse()
	{
		StringAppender strBuffer = StringTool.createStringAppender();
		String defKey = null, defValue = null;
		Parameter param = null;
		int ppCount = 0;  // 参数定义的参数个数
		int inChar;
		boolean defValueBegin = false;
		while ((inChar = getNextChar()) != -1)
		{
			char ch = (char) inChar;
			if (this.status == STATUS_COMMON)
			{
				if (ch != '$')
				{
					strBuffer.append(ch);
				}
				else
				{
					if (statusCheck())
					{
						parseList.add(strBuffer.toString());
						strBuffer = StringTool.createStringAppender();
						continue;
					}
					strBuffer.append(ch);
				}
			}
			else if (this.status == STATUS_DEFINE)
			{
				if (defKey == null)
				{
					// 读取定义的键值
					if (ch == ':')
					{
						defKey = strBuffer.toString().trim();
						strBuffer = StringTool.createStringAppender();
						continue;
					}
					else if (ch == '_' || ch <= ' '|| (ch <= 'z' && ch >= 'a')
							|| (ch <= 'Z' && ch >= 'A') || (ch <= '9' && ch >= '0'))
					{
						strBuffer.append(ch);
						continue;
					}
					else if (ch != '}')
					{
						throw new EternaException("Error define key char [" + ch + "].");
					}
				}
				else
				{
					if (defValueBegin)
					{
						if (ch == '{')
						{
							throw new EternaException("Error define value char [{].");
						}
					}
					else
					{
						if (ch == '{')
						{
							char[] tmpBuf = this.getChars(2);
							if (this.checkFlag("{{", tmpBuf))
							{
								this.status = STATUS_LINES;
								continue;
							}
							else
							{
								throw new EternaException("Error define value char [{].");
							}
						}
						else if (ch > ' ')
						{
							defValueBegin = true;
						}
					}
				}
				if (ch == ';' || ch == '}')
				{
					defValue = strBuffer.toString().trim();
					strBuffer = StringTool.createStringAppender();
					if (ch == '}')
					{
						this.status = STATUS_COMMON;
					}
				}
				else
				{
					strBuffer.append(ch);
				}

				if (defKey != null && defValue != null)
				{
					this.defines.put(defKey, defValue);
					defKey = null;
					defValue = null;
					defValueBegin = false;
				}
			}
			else if (this.status == STATUS_LINES)
			{
				if (ch == '}')
				{
					char[] tmpBuf = this.getChars(3);
					if (this.checkFlag("}};", tmpBuf) || this.checkFlag("}}", tmpBuf))
					{
						this.status = STATUS_DEFINE;
						defValue = strBuffer.toString();
						strBuffer = StringTool.createStringAppender();
						this.defines.put(defKey, MultiLineText.skipEmptyEndsLine(defValue));
						defKey = null;
						defValue = null;
						defValueBegin = false;
						continue;
					}
				}
				strBuffer.append(ch);
			}
			else if (this.status == STATUS_PARAM)
			{
				if (ch == '}')
				{
					this.status = STATUS_COMMON;
					String tmpStr = strBuffer.toString().trim();
					strBuffer = StringTool.createStringAppender();
					if (ppCount == 0)
					{
						param = this.getParam(tmpStr);
					}
					else if (ppCount == 1)
					{
						param.setDescribe(tmpStr);
					}
					else if (ppCount == 2)
					{
						param.setDefaultValue(tmpStr);
					}
					this.parameters.put(param.getName(), param);
					ppCount = 0;
					parseList.add(param);
					param = null;
				}
				else if (ch == ',')
				{
					String tmpStr = strBuffer.toString().trim();
					strBuffer = StringTool.createStringAppender();
					if (ppCount == 0)
					{
						param = this.getParam(tmpStr);
					}
					else if (ppCount == 1)
					{
						param.setDescribe(tmpStr);
					}
					else
					{
						throw new ParseException("Too many param's param count.");
					}
					ppCount += 1;
				}
				else
				{
					strBuffer.append(ch);
				}
			}
		}
		this.parseList.add(strBuffer.toString());
		this.initParam();
	}

	/**
	 * 获取或创建一个参数对象.
	 */
	private Parameter getParam(String name)
	{
		Parameter param = (Parameter) this.parameters.get(name);
		if (param == null)
		{
			param = new Parameter(name);
		}
		return param;
	}

	/**
	 * 初始化parameter中参数值.
	 */
	private void initParam()
	{
		Iterator itr = this.parameters.values().iterator();
		while (itr.hasNext())
		{
			((Parameter) itr.next()).init(this);
		}
	}

	/**
	 * 将解析结果转成字符串.
	 */
	public String getResultString()
	{
		if (this.parseList == null)
		{
			return null;
		}
		StringAppender result = StringTool.createStringAppender(1024);
		Iterator itr = this.parseList.iterator();
		while (itr.hasNext())
		{
			Object obj = itr.next();
			if (obj instanceof String)
			{
				result.append((String) obj);
			}
			else
			{
				result.append(((Parameter) obj).getValue());
			}
		}
		return result.toString();
	}

}
