
package self.micromagic.eterna.dao.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import self.micromagic.eterna.share.EternaException;
import self.micromagic.eterna.share.EternaFactory;
import self.micromagic.util.StringAppender;
import self.micromagic.util.StringTool;
import self.micromagic.util.converter.BooleanConverter;

/**
 * 数据库操作脚本的片断, 可以是一部分普通的脚本, 或一个子语句脚本,
 * 或可选参数, 或一个常量.
 */
public abstract class PartScript
{
	public void initialize(EternaFactory factory)
			throws EternaException
	{
	}

	public abstract PartScript copy(boolean clear, DaoManager manager);

	public abstract int getLength() throws EternaException;

	public abstract String getScript() throws EternaException;

}

/**
 * 动态参数片段.
 */
class ParameterPart extends PartScript
{
	private ParameterManager paramManager;
	private int templateIndex;

	public ParameterPart(ParameterManager paramManager, int templateIndex)
	{
		if (paramManager == null)
		{
			throw new NullPointerException();
		}
		this.paramManager = paramManager;
		this.templateIndex = templateIndex;
	}

	private ParameterPart()
	{
	}

	public ParameterManager getParameterManager()
	{
		return this.paramManager;
	}

	public PartScript copy(boolean clear, DaoManager manager)
	{
		ParameterManager pm = manager.getParameterManager(this.paramManager.getIndex());
		ParameterPart other = new ParameterPart();
		other.paramManager = pm;
		other.templateIndex = this.templateIndex;
		return other;
	}

	public int getLength()
			throws EternaException
	{
		try
		{
			String temp = this.paramManager.getParameterTemplate(this.templateIndex);
			return this.paramManager.isParameterSetted() ? temp.length() : 0;
		}
		catch (Exception ex)
		{
			throw new EternaException(ex);
		}
	}

	public String getScript()
			throws EternaException
	{
		String temp = this.paramManager.getParameterTemplate(this.templateIndex);
		return this.paramManager.isParameterSetted() ? temp : "";
	}

}

/**
 * 子句标志片段, 即替换"$"的片段.
 */
class SubFlagPart extends PartScript
{
	private String insertString = null;

	public void setSubPart(String subPart)
	{
		this.insertString = subPart;
	}

	public PartScript copy(boolean clear, DaoManager manager)
	{
		SubFlagPart other = new SubFlagPart();
		other.insertString = clear ? null : this.insertString;
		return other;
	}

	public int getLength()
	{
		return this.insertString == null ? 1 : this.insertString.length();
	}

	public String getScript()
	{
		return this.insertString == null ? DaoManager.SUBPART_FLAG + "" : this.insertString;
	}

}

/**
 * 子句片段.
 */
class SubPart extends PartScript
{
	private int flagPartIndex = -1;
	private PartScript[] parts = null;
	private final String template;
	private final int aheadParamCount;
	private int subIndex = 0;

	private String insertString = null;
	private String backupString = null;

	public SubPart(String template, int aheadParamCount)
	{
		if (template == null)
		{
			throw new NullPointerException();
		}
		this.template = template;
		this.aheadParamCount = aheadParamCount;
	}

	public PartScript copy(boolean clear, DaoManager manager)
	{
		SubPart other = new SubPart(this.template, this.aheadParamCount);
		other.subIndex = this.subIndex;
		other.insertString = clear ? null : this.insertString;
		other.backupString = clear ? null : this.backupString;
		if (this.parts != null)
		{
			other.flagPartIndex = this.flagPartIndex;
			other.parts = new PartScript[this.parts.length];
			for (int i = 0; i < this.parts.length; i++)
			{
				other.parts[i] = this.parts[i].copy(clear, manager);
			}
		}
		return other;
	}

	public void initialize(EternaFactory factory)
			throws EternaException
	{
		if (this.parts == null)
		{
			super.initialize(factory);
			ArrayList partList = new ArrayList();
			ArrayList paramList = new ArrayList();
			ArrayList subScriptList = new ArrayList();
			ArrayList subList = new ArrayList();
			DaoManager.parse(this.template, true, false,
					partList, paramList, subScriptList, subList);

			if (paramList.size() > 0)
			{
				throw new EternaException( "The parameter flag '?' can't int the sub tamplet:"
						+ this.template + ".");
			}
			if (subList.size() != 1)
			{
				throw new EternaException("Error sub flag count in template ["
						+ this.template + "].");
			}
			this.parts = new PartScript[partList.size()];
			Iterator itr = partList.iterator();
			for (int i = 0; i < this.parts.length; i++)
			{
				PartScript ps = (PartScript) itr.next();
				ps.initialize(factory);
				this.parts[i] = ps;
			}
			SubFlagPart flagPart = (SubFlagPart) subList.get(0);

			for (int i = 0; i < this.parts.length; i++)
			{
				if (this.parts[i] == flagPart)
				{
					this.flagPartIndex = i;
					break;
				}
			}
		}
	}

	public int getAheadParamCount()
	{
		return this.aheadParamCount;
	}

	public void setSubPart(String subPart)
	{
		this.insertString = subPart;
		((SubFlagPart) this.parts[this.flagPartIndex]).setSubPart(subPart);
	}

	public boolean checkSubPartSame(String subPart)
	{
		return this.insertString == subPart;
	}

	public int getLength()
			throws EternaException
	{
		if (this.insertString == null)
		{
			throw new EternaException("Sub part unsetted, template [" + this.template + "].");
		}

		if (this.insertString.length() == 0)
		{
			return 0;
		}
		else
		{
			int size = 0;
			for (int i = 0; i < this.parts.length; i++)
			{
				size += this.parts[i].getLength();
			}
			return size;
		}
	}

	public String getScript() throws EternaException
	{
		if (this.insertString == null)
		{
			throw new EternaException("Sub part unsetted, index [" + this.subIndex
					+ "], template [" + this.template + "].");
		}

		if (this.insertString.length() == 0)
		{
			return "";
		}

		StringAppender temp = StringTool.createStringAppender(this.getLength());
		for (int i = 0; i < this.parts.length; i++)
		{
			temp.append(this.parts[i].getScript());
		}
		return temp.toString();
	}

	public int getSubIndex()
	{
		return this.subIndex;
	}

	public void setSubIndex(int subIndex)
	{
		this.subIndex = subIndex;
	}

	public void backup()
	{
		this.backupString = this.insertString;
	}

	public void recover()
	{
		if (this.backupString != null)
		{
			this.insertString = this.backupString;
			this.backupString = null;
		}
	}

}

/**
 * 常量片段.
 */
class ConstantScript extends PartScript
{
	private final String name;

	private String value = null;

	public ConstantScript(String name)
	{
		if (name == null)
		{
			throw new NullPointerException();
		}
		this.name = StringTool.intern(name);
	}

	public void initialize(EternaFactory factory)
			throws EternaException
	{
		if (this.value == null)
		{
			super.initialize(factory);
			String temp = factory.getConstantValue(this.name);
			if (temp == null)
			{
				throw new EternaException("The constant [" + this.name + "] not found.");
			}
			this.value = temp;

			ArrayList partList = new ArrayList();
			ArrayList paramList = new ArrayList();
			ArrayList subScriptList = new ArrayList();
			ArrayList subList = new ArrayList();
			DaoManager.parse(this.value, true, false,
					partList, paramList, subScriptList, subList);

			if (paramList.size() > 0)
			{
				throw new EternaException(
						"The parameter flag [?] can't int the constant ["
						+ this.value + "].");
			}
			StringAppender buf = StringTool.createStringAppender(this.value.length() + 16);
			Iterator itr = partList.iterator();
			for (int i = 0; i < partList.size(); i++)
			{
				PartScript ps = (PartScript) itr.next();
				ps.initialize(factory);
				buf.append(ps.getScript());
			}
			this.value = StringTool.intern(buf.toString(), true);
		}
	}

	public PartScript copy(boolean clear, DaoManager manager)
	{
		return this;
	}

	public String getName()
	{
		return this.name;
	}

	public void setValue(String value)
	{
		if (value != null)
		{
			this.value = value;
		}
	}

	public int getLength()
	{
		return this.value.length();
	}

	public String getScript()
	{
		if (this.value == null)
		{
			throw new NullPointerException();
		}
		return this.value;
	}

}

/**
 * 检测片段.
 */
class CheckPart extends PartScript
{
	private String hasSubStr;
	private String noneSubStr;
	private boolean checkNormal;
	private boolean end;

	public CheckPart(String params)
	{
		this.paramStr = params;
	}
	private String paramStr;

	private CheckPart(boolean waitEnd)
	{
		this.waitEnd = waitEnd;
	}
	private boolean waitEnd;

	public void initialize(EternaFactory factory)
			throws EternaException
	{
		if (this.paramStr != null)
		{
			String tmpStr = this.paramStr;
			this.paramStr = null;
			super.initialize(factory);
			ArrayList partList = new ArrayList();
			ArrayList paramList = new ArrayList();
			ArrayList subScriptList = new ArrayList();
			ArrayList subList = new ArrayList();
			DaoManager.parse(tmpStr, true, false,
					partList, paramList, subScriptList, subList);

			if (paramList.size() > 0)
			{
				throw new EternaException(
						"The parameter flag '?' can't int the check param ["
						+ tmpStr + "].");
			}
			StringAppender buf = StringTool.createStringAppender();
			int count = partList.size();
			Iterator itr = partList.iterator();
			for (int i = 0; i < count; i++)
			{
				PartScript ps = (PartScript) itr.next();
				ps.initialize(factory);
				buf.append(ps.getScript());
			}

			Map map = StringTool.string2Map(
					buf.toString(), ";", '=', true, false, null, null);
			String tmp;
			if ((tmp = (String) map.get("hasSub")) != null
					|| (tmp = (String) map.get("append")) != null)
			{
				this.hasSubStr = tmp;
			}
			if ((tmp = (String) map.get("noneSub")) != null)
			{
				this.noneSubStr = tmp;
			}
			if ((tmp = (String) map.get("checkNormal")) != null)
			{
				this.checkNormal = BooleanConverter.toBoolean(tmp);
			}
			if ((tmp = (String) map.get("end")) != null)
			{
				this.end = BooleanConverter.toBoolean(tmp);
			}
		}
	}

	/**
	 * 对脚本片段进行检查.
	 *
	 * @param part          需要检查的脚本片段
	 * @param currentCheck  当前的检查片段的引用
	 * @return  经过检查后需要输出的脚本片段
	 */
	public String doCheck(PartScript part, CheckContainer currentCheck)
	{
		if (this.end)
		{
			// 不能直接设置一个结束
			throw new EternaException("Error nesting check.");
		}
		StringAppender currentBuf = currentCheck.buf;
		if (part instanceof CheckPart)
		{
			CheckPart other = (CheckPart) part;
			if (!other.end)
			{
				// 不是结束标记, 作为新的检查, 当前检查保存至parent
				currentCheck.setCheckPart(other, true);
				return "";
			}
			currentCheck.setCheckPart(null, true);
			if (this.waitEnd)
			{
				String result = other.hasSubStr;
				return result != null ? result : "";
			}
			else
			{
				// 不是等待结束标记, 那就是无子句
				String partStr = this.buildResult(
						this.noneSubStr, other.noneSubStr, currentBuf);
				return this.buildParentCheck(partStr, currentCheck);
			}
		}
		if (this.waitEnd)
		{
			String partStr = part == null ? "" : part.getScript();
			if (partStr.trim().length() == 0)
			{
				// 等待结束标记时, 空白片段原样输出
				return partStr;
			}
			if (!(part instanceof SubPart || part instanceof ParameterPart))
			{
				// 遇到非空的非子句, 取消waitEnd
				currentCheck.setCheckPart(null, false);
			}
			return partStr;
		}
		if (part instanceof SubPart || part instanceof ParameterPart)
		{
			String partStr = part.getScript();
			if (partStr.length() == 0)
			{
				return "";
			}
			// 存在子句, 结束当前的检查
			currentCheck.setCheckPart(new CheckPart(true), false);
			String resultStr = this.buildResult(
					this.hasSubStr, this.removeFirst(partStr), currentBuf);
			return this.buildParentCheck(resultStr, currentCheck.parent);
		}
		String partStr = part == null ? "" : part.getScript();
		if (part != null && partStr.trim().length() == 0)
		{
			if (currentCheck.buf == null)
			{
				currentCheck.buf = StringTool.createStringAppender();
			}
			currentCheck.buf.append(partStr);
			return "";
		}
		String beginStr = this.noneSubStr;
		if (part != null && this.checkNormal)
		{
			beginStr = this.hasSubStr;
			partStr = this.removeFirst(partStr);
		}
		// 遇到Normal部分就不需要等待结束标记了
		currentCheck.setCheckPart(null, false);
		return this.buildResult(beginStr, partStr, currentBuf);
	}

	/**
	 * 检查CheckContainer的parent属性并构造子语句.
	 */
	private String buildParentCheck(String part, CheckContainer pCheck)
	{
		if (pCheck == null)
		{
			return part;
		}
		CheckPart cPart = pCheck.checkPart;
		if (cPart == null || cPart.waitEnd)
		{
			// 没有parent或parent为等待, 则返回子语句
			return part;
		}
		if (part.trim().length() > 0)
		{
			String partStr = this.buildResult(
					cPart.hasSubStr, this.removeFirst(part), pCheck.buf);
			pCheck.checkPart = new CheckPart(true);
			pCheck.buf = null;
			return this.buildParentCheck(partStr, pCheck.parent);
		}
		else
		{
			if (pCheck.buf == null)
			{
				pCheck.buf = StringTool.createStringAppender();
			}
			pCheck.buf.append(part);
			return "";
		}
	}

	private String buildResult(String begin, String part, StringAppender currentBuf)
	{
		StringAppender buf = StringTool.createStringAppender();
		if (begin != null)
		{
			buf.append(begin);
		}
		if (currentBuf != null)
		{
			// 如果中间有缓存的字符, 需要在中间输出
			buf.append(currentBuf.toString());
		}
		if (part != null)
		{
			buf.append(part);
		}
		return buf.toString();
	}

	private String removeFirst(String part)
	{
		int count = part.length();
		int begin = 0;
		for (; begin < count; begin++)
		{
			// 忽略起始部分的空格
			if (part.charAt(begin) > ' ')
			{
				break;
			}
		}
		if (begin < count && part.charAt(begin) == ',')
		{
			// 是","就作为第一个字符去除
			return part.substring(begin + 1);
		}
		for (; begin < count; begin++)
		{
			char c = part.charAt(begin);
			if (c <= ' ' || c == ',')
			{
				// 遇到第一个特殊字符, 将前面的字符去除
				break;
			}
		}
		return part.substring(begin);
	}

	public PartScript copy(boolean clear, DaoManager manager)
	{
		return this;
	}

	public int getLength()
	{
		return 0;
	}

	public String getScript()
	{
		return "";
	}

}

/**
 * 存放CheckPart的容器.
 */
class CheckContainer
{
	/**
	 * 存放中间输出的字符的缓存.
	 */
	public StringAppender buf;
	public CheckPart checkPart;

	public void setCheckPart(CheckPart other, boolean dealStack)
	{
		if (dealStack)
		{
			if (other == null)
			{
				if (this.parent != null)
				{
					this.checkPart = this.parent.checkPart;
					this.buf = this.parent.buf;
					this.parent = this.parent.parent;
				}
				else
				{
					this.checkPart = null;
					this.buf = null;
				}
			}
			else
			{
				CheckContainer tmp = new CheckContainer();
				tmp.checkPart = this.checkPart;
				tmp.buf = this.buf;
				tmp.parent = this.parent;
				this.parent = tmp;
				this.checkPart = other;
				this.buf = null;
			}
		}
		else
		{
			if (other == null && this.parent != null)
			{
				// 遇到清空当前CheckPart且不需要处理堆栈时, 堆栈必须是空的
				throw new EternaException("Error nesting check.");
			}
			this.checkPart = other;
			this.buf = other == null ? null : this.buf;
		}
	}
	CheckContainer parent = null;

}

/**
 * 普通脚本的片段.
 */
class NormalScript extends PartScript
{
	private final String script;

	public NormalScript(String script)
	{
		if (script == null)
		{
			throw new NullPointerException();
		}
		// 这里的脚本大部分是intern处理过的字符串的字串
		this.script = script;
	}

	public PartScript copy(boolean clear, DaoManager manager)
	{
		return this;
	}

	public int getLength()
	{
		return this.script.length();
	}

	public String getScript()
	{
		return this.script;
	}

}
