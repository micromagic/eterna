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

package self.micromagic.grammer;

import java.io.IOException;
import java.io.PrintStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import self.micromagic.util.StringAppender;
import self.micromagic.util.StringTool;

public class ParserData
{
	private boolean endSrc = false;
	private int currentIndex = -1;
	private final Reader src;
	private StringAppender buf;

	private final List checkerStack = new ArrayList();
	private final List errorStack = new ArrayList();
	private final List indexResetStack = new ArrayList();

	private final IndexStore globalStore = new IndexStore(0);

	private String maxBuf = null;
	private Checker maxChecker = null;

	public ParserData(Reader src)
	{
		this.src = src;
		char[] tmpBuf = new char[1024 * 2];
		try
		{
			int count = src.read(tmpBuf);
			this.buf = StringTool.createStringAppender();
			this.buf.append(tmpBuf, 0, count);
			if (count < tmpBuf.length)
			{
				this.endSrc = true;
			}
		}
		catch (IOException ex)
		{
			GrammerManager.log.error("Error in init ParserData.", ex);
		}
	}

	public ParserData(String src)
	{
		this.buf = StringTool.createStringAppender(src, 16, false);
		this.src = new StringReader(src);
		this.endSrc = true;
	}

	public boolean isEnd()
	{
		if (this.endSrc)
		{
			return this.currentIndex == this.buf.length() - 1;
		}
		return false;
	}

	public int getCurrentIndex()
	{
		return this.currentIndex;
	}

	public char getCurrentChar()
			throws GrammerException
	{
		if (this.currentIndex == -1)
		{
			throw new GrammerException("Hasn't read a char.");
		}
		return this.buf.charAt(this.currentIndex);
	}

	public char getNextChar()
			throws GrammerException
	{
		if (this.currentIndex < this.buf.length() - 1)
		{
			return this.buf.charAt(++this.currentIndex);
		}
		if (!this.endSrc)
		{
			try
			{
				int c = this.src.read();
				if (c == -1)
				{
					this.endSrc = true;
				}
				else
				{
					this.buf.append((char) c);
					return this.buf.charAt(++this.currentIndex);
				}
			}
			catch (IOException ex)
			{
				throw new GrammerException(ex);
			}
		}
		throw new GrammerException("End of src.");
	}

	public int addResetPoint()
	{
		this.indexResetStack.add(new IndexStore(this.currentIndex));
		return this.currentIndex;
	}

	public int reset()
	{
		if (this.indexResetStack.size() > 0)
		{
			IndexStore is = (IndexStore) this.indexResetStack.remove(this.indexResetStack.size() - 1);
			this.currentIndex = is.storeIndex;
		}
		return this.currentIndex;
	}

	public void removeResetPoint()
	{
		if (this.indexResetStack.size() > 0)
		{
			IndexStore is = (IndexStore) this.indexResetStack.remove(this.indexResetStack.size() - 1);
			IndexStore pIs = this.globalStore;
			if (this.indexResetStack.size() > 0)
			{
				pIs = (IndexStore) this.indexResetStack.get(this.indexResetStack.size() - 1);
			}
			if (is != null && is.getGrammerCellList() != null)
			{
				pIs.addGrammerCells(is.getGrammerCellList());
			}
		}
	}

	public void storeElement(GrammerElement element)
	{
		String buf = this.getCurrentSubBuf();
		IndexStore is = null;
		if (this.indexResetStack.size() > 0)
		{
			is = (IndexStore) this.indexResetStack.remove(this.indexResetStack.size() - 1);
		}
		IndexStore pIs = this.globalStore;
		if (this.indexResetStack.size() > 0)
		{
			pIs = (IndexStore) this.indexResetStack.get(this.indexResetStack.size() - 1);
		}
		if (element.getType() == GrammerElement.TYPE_NONE)
		{
			if (is != null && is.getGrammerCellList() != null)
			{
				pIs.addGrammerCells(is.getGrammerCellList());
			}
		}
		else
		{
			if (element.getType() != GrammerElement.TYPE_BLANK || buf.length() > 0)
			{
				GrammerCell cell = new GrammerCell(is.storeIndex + 1, buf, element,
						is != null ? is.getGrammerCellList() : null);
				pIs.addGrammerCell(cell);
			}
		}
	}

	public String getCurrentBuf()
	{
		return this.buf.toString().substring(0, this.currentIndex + 1);
	}

	public String getCurrentSubBuf()
	{
		if (this.indexResetStack.size() > 0)
		{
			IndexStore is = (IndexStore) this.indexResetStack.get(this.indexResetStack.size() - 1);
			return this.buf.substring(is.storeIndex + 1, this.currentIndex + 1);
		}
		return "";
	}

	public Checker popChecker(boolean verified)
	{
		if (this.checkerStack.size() > 0)
		{
			Checker tmp = (Checker) this.checkerStack.remove(this.checkerStack.size() - 1);
			if (!verified)
			{
				String str = this.getCurrentBuf();
				if (this.maxBuf == null || this.maxBuf.length() < str.length())
				{
					this.maxBuf = str;
					this.maxChecker = tmp;
				}
				this.errorStack.add(this.getCurrentBuf());
				this.errorStack.add(tmp);
			}
			return tmp;
		}
		return null;
	}

	public void pushChecker(Checker checker)
	{
		this.errorStack.clear();
		this.checkerStack.add(checker);
	}

	public List getGrammerCellLst()
	{
		return this.globalStore.getGrammerCellList();
	}

	public String getMaxErrorBuffer()
	{
		return this.maxBuf;
	}

	public void printErrorStack(PrintStream out)
	{
		if (this.maxBuf != null)
		{
			out.println(StringTool.dealString2EditCode(this.maxBuf));
			out.println(StringTool.dealString2EditCode(this.maxChecker.toString()));
		}
		Iterator itr = this.errorStack.iterator();
		while (itr.hasNext())
		{
			out.println(StringTool.dealString2EditCode(itr.next().toString()));
		}
	}

	public String toString()
	{
		List list = this.globalStore.getGrammerCellList();
		if (list == null)
		{
			return "";
		}
		StringAppender buf = StringTool.createStringAppender();
		this.appendGrammerCell(list, buf);
		return buf.toString();
	}

	private void appendGrammerCell(List gclist, StringAppender buf)
	{
		buf.append('[');
		Iterator itr = gclist.iterator();
		while (itr.hasNext())
		{
			GrammerCell cell = (GrammerCell) itr.next();
			String type = GrammerManager.getGrammerElementTypeName(cell.grammerElement.getType());
			buf.append(type).append(':');
			if (cell.subCells != null)
			{
				this.appendGrammerCell(cell.subCells, buf);
			}
			else
			{
				buf.append(StringTool.dealString2EditCode(cell.textBuf));
			}
			if (itr.hasNext())
			{
				buf.append(',');
			}
		}
		buf.append(']');
	}

	private static class IndexStore
	{
		public final int storeIndex;
		private List grammerCellList = null;

		public IndexStore(int storeIndex)
		{
			this.storeIndex = storeIndex;
		}

		public void addGrammerCells(List cells)
		{
			if (this.grammerCellList == null)
			{
				this.grammerCellList = new ArrayList(10);
			}
			this.grammerCellList.addAll(cells);
		}

		public void addGrammerCell(GrammerCell cell)
		{
			if (this.grammerCellList == null)
			{
				this.grammerCellList = new ArrayList(5);
			}
			this.grammerCellList.add(cell);
		}

		public List getGrammerCellList()
		{
			return grammerCellList;
		}

	}

	public static class GrammerCell
	{
		public final int startIndex;
		public final String textBuf;
		public final GrammerElement grammerElement;
		public final List subCells;

		public GrammerCell(int startIndex, String textBuf, GrammerElement grammerElement, List subCells)
		{
			this.startIndex = startIndex;
			this.textBuf = textBuf;
			this.grammerElement = grammerElement;
			this.subCells = subCells;
		}

	}

}