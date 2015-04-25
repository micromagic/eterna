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

package self.micromagic.util.logging;

/**
 * 纳秒的获取工具.
 *
 * @author micromagic@sina.com
 */
class TimeLogger$NanoTime
		implements TimeLogger.TimeGetter
{
	public long getTime()
	{
		return System.nanoTime();
	}

	public long getMillionSecond(long time)
	{
		return time / 1000000L;
	}
	
	public String formatMillionSecond(long time, int precision)
	{
		String result;
		if (time >= 0L)
		{
			result = Long.toString(time / 1000000L);
		}
		else
		{
			time = -time;
			result = "-" + Long.toString(time / 1000000L);
		}
		long nano = time % 1000000L;
		if (nano == 0L)
		{
			return result;
		}
		String nanoStr = Long.toString(nano);
		nanoStr = ZERO_NANO.substring(nanoStr.length()) + nanoStr;
		int lastPos = 0;
		precision = Math.min(precision, ZERO_NANO.length());
		for (int i = 0; i < precision; i++)
		{
			if (nanoStr.charAt(i) != '0')
			{
				lastPos = i + 1;
			}
		}
		return lastPos > 0 ? result + "." + nanoStr.substring(0, lastPos) : result;
	}
	private static final String ZERO_NANO = "000000";

}