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

package self.micromagic.util.converter;

import junit.framework.TestCase;
import self.micromagic.util.Utility;

public class EmptyToNullTest extends TestCase
{
	public void testFinder()
			throws Exception
	{
		BooleanConverter c0 = new BooleanConverter();
		assertTrue(c0.isEmptyToNull());
		ValueConverter c1 = ConverterFinder.findConverter(boolean.class);
		assertTrue(c1.isEmptyToNull());
		BooleanConverter c2 = new BooleanConverter();
		assertTrue(c2.isEmptyToNull());
		Thread.sleep(10L);
		Utility.setProperty(AbstractNumericalConverter.NUMERICAL_EMPTY_TO_NULL_FLAG, "0");
		BooleanConverter c3 = new BooleanConverter();
		assertFalse(c3.isEmptyToNull());
	}

}
