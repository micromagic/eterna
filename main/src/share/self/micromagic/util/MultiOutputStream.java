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

import java.io.IOException;
import java.io.OutputStream;

public class MultiOutputStream extends OutputStream
{
	private OutputStream out1;
	private OutputStream out2;

	public MultiOutputStream(OutputStream out1, OutputStream out2)
	{
		this.out1 = out1;
		this.out2 = out2;
	}

	public void write(int b)
			throws IOException
	{
		this.out1.write(b);
		this.out2.write(b);
	}

	public void write(byte b[])
			throws IOException
	{
		this.out1.write(b);
		this.out2.write(b);
	}

	public void write(byte b[], int off, int len)
			throws IOException
	{
		this.out1.write(b, off, len);
		this.out2.write(b, off, len);
	}

	public void flush()
			throws IOException
	{
		this.out1.flush();
		this.out2.flush();
	}

	public void close()
			throws IOException
	{
		this.out1.close();
		this.out2.close();
	}

}