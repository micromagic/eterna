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

package self.micromagic.cg;

import java.util.Collection;
import java.net.URL;

public class TestArrayBean
{
	private int[] arrInt;
	private String[][] arrStr;
	private TestMainBean[] mainBean1;
	private TestMainBean[][] mainBean2;
	private TestSubBean[] subBean1;
	private TestSubBean[][][] subBean2;
	private Collection collection;
	public URL[] urls;
	public TestMainBean mainB;

	public Collection getCollection()
	{
		return collection;
	}

	public void setCollection(Collection collection)
	{
		this.collection = collection;
	}

	public int[] getArrInt()
	{
		return arrInt;
	}

	public void setArrInt(int[] arrInt)
	{
		this.arrInt = arrInt;
	}

	public String[][] getArrStr()
	{
		return arrStr;
	}

	public void setArrStr(String[][] arrStr)
	{
		this.arrStr = arrStr;
	}

	public TestMainBean[] getMainBean1()
	{
		return mainBean1;
	}

	public void setMainBean1(TestMainBean[] mainBean1)
	{
		this.mainBean1 = mainBean1;
	}

	public TestMainBean[][] getMainBean2()
	{
		return mainBean2;
	}

	public void setMainBean2(TestMainBean[][] mainBean2)
	{
		this.mainBean2 = mainBean2;
	}

	public TestSubBean[] getSubBean1()
	{
		return subBean1;
	}

	public void setSubBean1(TestSubBean[] subBean1)
	{
		this.subBean1 = subBean1;
	}

	public TestSubBean[][][] getSubBean2()
	{
		return subBean2;
	}

	public void setSubBean2(TestSubBean[][][] subBean2)
	{
		this.subBean2 = subBean2;
	}

}