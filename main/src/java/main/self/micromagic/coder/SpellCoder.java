/*
 * SunriseSpell - A Chinese pinyin library
 *
 * Copyright (C) 2004 mic <mic4free@hotmail.com>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * In case your copy of SunriseSpell does not include a copy of the license, you may find it online at
 * http://www.gnu.org/copyleft/gpl.html
 */

package self.micromagic.coder;

import java.io.UnsupportedEncodingException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import self.micromagic.util.Utility;
import self.micromagic.util.StringAppender;
import self.micromagic.util.StringTool;


/**
 * 将中文字符变成拼音的编码处理. <p>
 * 由Sunrise.Spell改编而来
 */
public class SpellCoder
{
	/**
	 * 只转换拼音首字母，默认转换全部
	 */
	private boolean firstLetterOnly = false;

	/**
	 * 转换未知字符为问号，默认不转换
	 */
	private boolean unknowWordToInterrogation = false;

	/**
	 * 保留非字母、非数字字符，默认不保留
	 */
	private boolean enableUnicodeLetter = false;

	/**
	 * 忽略其它字符，仅转换拼音，默认不忽略
	 */
	private boolean ignoreOtherLetter = false;

	public SpellCoder()
	{
	}

	public SpellCoder(boolean firstLetterOnly, boolean unknowWordToInterrogation,
			boolean enableUnicodeLetter, boolean ignoreLetter)
	{
		this.firstLetterOnly = firstLetterOnly;
		this.unknowWordToInterrogation = unknowWordToInterrogation;
		this.enableUnicodeLetter = enableUnicodeLetter;
		this.ignoreOtherLetter = ignoreLetter;
		if (this.ignoreOtherLetter)
		{
			this.unknowWordToInterrogation = false;
		}
	}

	/**
	 * 获取汉字的汉语拼音
	 * @param str    欲转换的字符串
	 * @return
	 */
	public String makeSpellCode(String str)
			throws UnsupportedEncodingException
	{
		if (str == null)
		{
			return null;
		}
		byte[] bytes = str.getBytes("GBK");

		int i = 0;
		StringAppender result = StringTool.createStringAppender();
		String tmp = "";

		while (i < bytes.length)
		{
			tmp = "";
			// 是否为GBK 字符
			int tmpB1 = bytes[i] & 0xff;
			if ((tmpB1 >= 129))
			{
				int tmpB2 = bytes[i + 1] & 0xff;
				if (tmpB2 >= 64)
				{
					switch (tmpB1)
					{
						case 163:	// 全角 ASCII
							if (this.ignoreOtherLetter) break;
							tmpB2 -= 128;
							if (tmpB2 > 0)
							{
								tmp = String.valueOf((char) (tmpB2));
								if (!this.enableUnicodeLetter)
								{
									if ((tmpB2 < 'a' || tmpB2 > 'z') && (tmpB2 < 'A' || tmpB2 > 'Z')
											&& (tmpB2 < '0' || tmpB2 > '9'))
									{
										// 控制不能输出非数字, 字母的字符
										tmp = this.unknowWordToInterrogation ? "?" : "";
									}
								}
							}
							else
							{
								tmp = this.unknowWordToInterrogation ? "?" : "";
							}
							break;
						case 162: // 罗马数字
							if (this.ignoreOtherLetter) break;
							if (tmpB2 >= 161 && (tmpB2 - 161) < _charIndex1.length)
							{
								tmp = _charIndex1[tmpB2 - 161];
							}
							else
							{
								// 在罗马数字区, 不能翻译的字符非罗马数字
								tmp = this.unknowWordToInterrogation ? "?" : "";
							}
							break;
						case 166: // 希腊字母
							if (this.ignoreOtherLetter) break;
							if (tmpB2 >= 193 && (tmpB2 - 193) < _charIndex2.length)
							{
								tmp = _charIndex2[tmpB2 - 193];
							}
							else
							{
								tmp = this.unknowWordToInterrogation ? "?" : "";
							}
							break;
						default :
							int index = _spellCodeIndex[tmpB1 - 129][tmpB2 - 64] - 1;
							if (index == -1)	// 无此汉字, 不能翻译的字符, GBK 保留
							{
								tmp = this.unknowWordToInterrogation ? "?" : "";
							}
							else if (this.firstLetterOnly)	 //是单拼音
							{
								tmp = _spellMusicCode[index].substring(0, 1);
							}
							else
							{
								tmp = _spellMusicCode[index];
							}
							break;
					}	//end of swicth()
					result.append(tmp);
					i += 2;
				}
				else
				{
					// 第二位字符不正确, 不明字符
					tmp = this.unknowWordToInterrogation ? "?" : "";
					i++;
				}
			}
			else
			{
				// 在 GBK 字符集外, 即半角字符
				if (!this.ignoreOtherLetter)
				{
					tmp = String.valueOf((char) tmpB1);
					if (!this.enableUnicodeLetter)
					{
						if ((tmpB1 < 'a' || tmpB1 > 'z') && (tmpB1 < 'A' || tmpB1 > 'Z')
								&& (tmpB1 < '0' || tmpB1 > '9'))
						{
							// 控制不能输出非数字, 字母的字符
							tmp = this.unknowWordToInterrogation ? "?" : "";
						}
					}
					result.append(tmp);
				}
				i++;
			}
		}
		return result.toString();
	}


	public boolean isFirstLetterOnly()
	{
		return this.firstLetterOnly;
	}

	public void setFirstLetterOnly(boolean firstLetterOnly)
	{
		this.firstLetterOnly = firstLetterOnly;
	}

	public boolean isUnknowWordToInterrogation()
	{
		return this.unknowWordToInterrogation;
	}

	public void setUnknowWordToInterrogation(boolean unknowWordToInterrogation)
	{
		this.unknowWordToInterrogation = unknowWordToInterrogation;
	}

	public boolean isEnableUnicodeLetter()
	{
		return this.enableUnicodeLetter;
	}

	public void setEnableUnicodeLetter(boolean enableUnicodeLetter)
	{
		this.enableUnicodeLetter = enableUnicodeLetter;
	}

	public boolean isIgnoreOtherLetter()
	{
		return this.ignoreOtherLetter;
	}

	public void setIgnoreOtherLetter(boolean ignoreOtherLetter)
	{
		this.ignoreOtherLetter = ignoreOtherLetter;
		if (this.ignoreOtherLetter)
		{
			this.unknowWordToInterrogation = false;
		}
	}

	/**
	 * 拼音代码表
	 */
	private static String[] _spellMusicCode = {
		"A", "Ai", "An", "Ang", "Ao", "Ba", "Bai", "Ban", "Bang", "Bao",
		"Bei", "Ben", "Beng", "Bi", "Bian", "Biao", "Bie", "Bin", "Bing", "Bo",
		"Bu", "Ca", "Cai", "Can", "Cang", "Cao", "Ce", "Ceng", "Cha", "Chai",
		"Chan", "Chang", "Chao", "Che", "Chen", "Cheng", "Chi", "Chong", "Chou", "Chu",
		"Chuai", "Chuan", "Chuang", "Chui", "Chun", "Chuo", "Ci", "Cong", "Cou", "Cu",
		"Cuan", "Cui", "Cun", "Cuo", "Da", "Dai", "Dan", "Dang", "Dao", "De",
		"Deng", "Di", "Dian", "Diao", "Die", "Ding", "Diu", "Dong", "Dou", "Du",
		"Duan", "Dui", "Dun", "Duo", "E", "En", "Er", "Fa", "Fan", "Fang",
		"Fei", "Fen", "Feng", "Fu", "Fou", "Ga", "Gai", "Gan", "Gang", "Gao",
		"Ge", "Ji", "Gen", "Geng", "Gong", "Gou", "Gu", "Gua", "Guai", "Guan",
		"Guang", "Gui", "Gun", "Guo", "Ha", "Hai", "Han", "Hang", "Hao", "He",
		"Hei", "Hen", "Heng", "Hong", "Hou", "Hu", "Hua", "Huai", "Huan", "Huang",
		"Hui", "Hun", "Huo", "Jia", "Jian", "Jiang", "qiao", "Jiao", "Jie", "Jin",
		"Jing", "Jiong", "Jiu", "Ju", "Juan", "Jue", "Jun", "Ka", "Kai", "Kan",
		"Kang", "Kao", "Ke", "Ken", "Keng", "Kong", "Kou", "Ku", "Kua", "Kuai",
		"Kuan", "Kuang", "Kui", "Kun", "Kuo", "La", "Lai", "Lan", "Lang", "Lao",
		"Le", "Lei", "Leng", "Li", "Lia", "Lian", "Liang", "Liao", "Lie", "Lin",
		"Ling", "Liu", "Long", "Lou", "Lu", "Luan", "Lue", "Lun", "Luo", "Ma",
		"Mai", "Man", "Mang", "Mao", "Me", "Mei", "Men", "Meng", "Mi", "Mian",
		"Miao", "Mie", "Min", "Ming", "Miu", "Mo", "Mou", "Mu", "Na", "Nai",
		"Nan", "Nang", "Nao", "Ne", "Nei", "Nen", "Neng", "Ni", "Nian", "Niang",
		"Niao", "Nie", "Nin", "Ning", "Niu", "Nong", "Nu", "Nuan", "Nue", "yao",
		"Nuo", "O", "Ou", "Pa", "Pai", "Pan", "Pang", "Pao", "Pei", "Pen",
		"Peng", "Pi", "Pian", "Piao", "Pie", "Pin", "Ping", "Po", "Pou", "Pu",
		"Qi", "Qia", "Qian", "Qiang", "Qie", "Qin", "Qing", "Qiong", "Qiu", "Qu",
		"Quan", "Que", "Qun", "Ran", "Rang", "Rao", "Re", "Ren", "Reng", "Ri",
		"Rong", "Rou", "Ru", "Ruan", "Rui", "Run", "Ruo", "Sa", "Sai", "San",
		"Sang", "Sao", "Se", "Sen", "Seng", "Sha", "Shai", "Shan", "Shang", "Shao",
		"She", "Shen", "Sheng", "Shi", "Shou", "Shu", "Shua", "Shuai", "Shuan", "Shuang",
		"Shui", "Shun", "Shuo", "Si", "Song", "Sou", "Su", "Suan", "Sui", "Sun",
		"Suo", "Ta", "Tai", "Tan", "Tang", "Tao", "Te", "Teng", "Ti", "Tian",
		"Tiao", "Tie", "Ting", "Tong", "Tou", "Tu", "Tuan", "Tui", "Tun", "Tuo",
		"Wa", "Wai", "Wan", "Wang", "Wei", "Wen", "Weng", "Wo", "Wu", "Xi",
		"Xia", "Xian", "Xiang", "Xiao", "Xie", "Xin", "Xing", "Xiong", "Xiu", "Xu",
		"Xuan", "Xue", "Xun", "Ya", "Yan", "Yang", "Ye", "Yi", "Yin", "Ying",
		"Yo", "Yong", "You", "Yu", "Yuan", "Yue", "Yun", "Za", "Zai", "Zan",
		"Zang", "Zao", "Ze", "Zei", "Zen", "Zeng", "Zha", "Zhai", "Zhan", "Zhang",
		"Zhao", "Zhe", "Zhen", "Zheng", "Zhi", "Zhong", "Zhou", "Zhu", "Zhua", "Zhuai",
		"Zhuan", "Zhuang", "Zhui", "Zhun", "Zhuo", "Zi", "Zong", "Zou", "Zu", "Zuan",
		"Zui", "Zun", "Zuo", "", "Ei", "M", "N", "Dia", "Cen", "Nou",
		"Jv", "Qv", "Xv", "Lv", "Nv"
	};

	/**
	 * 拼音索引表
	 */
	private static final short[][] _spellCodeIndex;

	static
	{
		// 从对应的配置文件中读取拼音索引
		_spellCodeIndex = readSpellCodeIndex();
	}

	private static short[][] readSpellCodeIndex()
	{
		List indexArr = new ArrayList();
		try
		{
			String rName = SpellCoder.class.getName().replace('.', '/') + ".res";
			InputStream in = SpellCoder.class.getClassLoader().getResourceAsStream(rName);
			int b = in.read();
			short[] iArr = new short[256 - 64];
			int index = 0;
			short tmpI = 0;
			while (b != -1)
			{
				if (b == ',')
				{
					iArr[index++] = tmpI;
					tmpI = 0;
				}
				else if (b == '\n')
				{
					indexArr.add(iArr);
					iArr = new short[256 - 64];
					index = 0;
				}
				else if (b >= '0' && b <= '9')
				{
					tmpI = (short) (tmpI * 10 + (b - '0'));
				}
				b = in.read();
			}
		}
		catch (Throwable ex)
		{
			Utility.createLog("eterna.spell").error("Error in init.", ex);
		}
		return (short[][]) indexArr.toArray(new short[0][0]);
	}

	/**
	 * 罗马字母
	 */
	private static final String[] _charIndex1 = {
		"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "", "", "", "", "", "",
		"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20",
		"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20",
		"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "", "",
		"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "", "",
		"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "", ""
	};

	/**
	 * 希腊字母
	 */
	private static final String[] _charIndex2 = {
		"a", "b", "g", "d", "e", "z", "e", "th", "i", "k", "l", "m", "n", "x", "o", "p", "r",
		"s", "t", "u", "ph", "kh", "ps", "o"
	};

}