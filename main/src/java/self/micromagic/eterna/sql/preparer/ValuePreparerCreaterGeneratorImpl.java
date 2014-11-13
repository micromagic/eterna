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

package self.micromagic.eterna.sql.preparer;

import java.io.InputStream;
import java.io.Reader;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;

import self.micromagic.eterna.digester.ConfigurationException;
import self.micromagic.eterna.share.AbstractGenerator;
import self.micromagic.eterna.share.EternaFactory;
import self.micromagic.eterna.share.TypeManager;

public class ValuePreparerCreaterGeneratorImpl extends AbstractGenerator
		implements ValuePreparerCreaterGenerator
{
	private ValuePreparerCreater[] creaters
			= new ValuePreparerCreater[TypeManager.TYPES_COUNT];
	private NullPreparer.Creater nullPreparer = new NullPreparer.Creater(this);
	private BooleanPreparer.Creater booleanPreparer = new BooleanPreparer.Creater(this);
	private BytePreparer.Creater bytePreparer = new BytePreparer.Creater(this);
	private BytesPreparer.Creater bytesPreparer = new BytesPreparer.Creater(this);
	private ShortPreparer.Creater shortPreparer = new ShortPreparer.Creater(this);
	private IntegerPreparer.Creater intPreparer = new IntegerPreparer.Creater(this);
	private LongPreparer.Creater longPreparer = new LongPreparer.Creater(this);
	private FloatPreparer.Creater floatPreparer = new FloatPreparer.Creater(this);
	private DoublePreparer.Creater doublePreparer = new DoublePreparer.Creater(this);
	private StringPreparer.Creater stringPreparer = new StringPreparer.Creater(this);
	private StreamPreparer.Creater streamPreparer = new StreamPreparer.Creater(this);
	private ReaderPreparer.Creater readerPreparer = new ReaderPreparer.Creater(this);
	private DatePreparer.Creater datePreparer = new DatePreparer.Creater(this);
	private TimePreparer.Creater timePreparer = new TimePreparer.Creater(this);
	private TimestampPreparer.Creater timpstampPreparer = new TimestampPreparer.Creater(this);
	private ObjectPreparer.Creater objectPreparer = new ObjectPreparer.Creater(this);

	{
		this.creaters[TypeManager.TYPE_IGNORE] = this.nullPreparer;
		this.creaters[TypeManager.TYPE_STRING] = this.stringPreparer;
		this.creaters[TypeManager.TYPE_BIGSTRING] = this.stringPreparer;
		this.creaters[TypeManager.TYPE_BOOLEAN] = this.booleanPreparer;

		this.creaters[TypeManager.TYPE_BYTE] = this.bytePreparer;
		this.creaters[TypeManager.TYPE_SHORT] = this.shortPreparer;
		this.creaters[TypeManager.TYPE_INTEGER] = this.intPreparer;
		this.creaters[TypeManager.TYPE_LONG] = this.longPreparer;
		this.creaters[TypeManager.TYPE_FLOAT] = this.floatPreparer;
		this.creaters[TypeManager.TYPE_DOUBLE] = this.doublePreparer;

		this.creaters[TypeManager.TYPE_DATE] = this.datePreparer;
		this.creaters[TypeManager.TYPE_TIME] = this.timePreparer;
		this.creaters[TypeManager.TYPE_TIMPSTAMP] = this.timpstampPreparer;
		this.creaters[TypeManager.TYPE_OBJECT] = this.objectPreparer;

		this.creaters[TypeManager.TYPE_BYTES] = this.bytesPreparer;
		this.creaters[TypeManager.TYPE_STREAM] = this.streamPreparer;
		this.creaters[TypeManager.TYPE_READER] = this.readerPreparer;
	}

	protected boolean initialized = false;
	protected EternaFactory factory;
	protected boolean emptyStringToNull = true;

	public void initialize(EternaFactory factory)
			throws ConfigurationException
	{
		if (this.initialized)
		{
			return;
		}
		this.initialized = true;
		this.factory = factory;
		String tmp;

		tmp = (String) this.getAttribute("dateFormat");
		if (tmp != null)
		{
			this.datePreparer.setFormat(tmp);
		}
		tmp = (String) this.getAttribute("timeFormat");
		if (tmp != null)
		{
			this.timePreparer.setFormat(tmp);
		}
		tmp = (String) this.getAttribute("datetimeFormat");
		if (tmp == null)
		{
			tmp = (String) this.getAttribute("timpstampFormat");
		}
		if (tmp != null)
		{
			this.timpstampPreparer.setFormat(tmp);
		}

		tmp = (String) this.getAttribute("stringFormat");
		if (tmp != null)
		{
			this.stringPreparer.setFormat(tmp);
		}
		tmp = (String) this.getAttribute("booleanFormat");
		if (tmp != null)
		{
			this.booleanPreparer.setFormat(tmp);
		}

		tmp = (String) this.getAttribute("numberFormat");
		if (tmp != null)
		{
			this.bytePreparer.setFormat(tmp);
			this.shortPreparer.setFormat(tmp);
			this.intPreparer.setFormat(tmp);
			this.longPreparer.setFormat(tmp);
			this.floatPreparer.setFormat(tmp);
			this.doublePreparer.setFormat(tmp);
		}

		tmp = (String) this.getAttribute(ValuePreparerCreater.EMPTY_STRING_TO_NULL);
		if (tmp == null)
		{
			tmp = (String) factory.getAttribute(ValuePreparerCreater.EMPTY_STRING_TO_NULL);
		}
		if (tmp != null)
		{
			this.emptyStringToNull = "true".equals(tmp);
		}

		tmp = (String) this.getAttribute("charset");
		if (tmp != null)
		{
			this.bytesPreparer.setCharset(tmp);
			this.streamPreparer.setCharset(tmp);
		}
	}

	public void setName(String name)
			throws ConfigurationException
	{
		if (this.initialized)
		{
			throw new ConfigurationException("Initialized ValuePreparerCreaterGenerator can't change name.");
		}
		super.setName(name);
	}

	public EternaFactory getFactory()
	{
		return this.factory;
	}

	public boolean isEmptyStringToNull()
	{
		return this.emptyStringToNull;
	}

	public Object create()
	{
		return this.createValuePreparerCreater(TypeManager.TYPE_STRING);
	}

	public ValuePreparerCreater createValuePreparerCreater(int pureType)
	{
		return this.creaters[pureType];
	}

	public ValuePreparer createNullPreparer(int index, int type)
			throws ConfigurationException
	{
		ValuePreparer preparer = this.nullPreparer.createPreparer(type);
		preparer.setRelativeIndex(index);
		return preparer;
	}

	public ValuePreparer createBooleanPreparer(int index, boolean v)
			throws ConfigurationException
	{
		ValuePreparer preparer = this.booleanPreparer.createPreparer(v);
		preparer.setRelativeIndex(index);
		return preparer;
	}

	public ValuePreparer createBytePreparer(int index, byte v)
			throws ConfigurationException
	{
		ValuePreparer preparer = this.bytePreparer.createPreparer(v);
		preparer.setRelativeIndex(index);
		return preparer;
	}

	public ValuePreparer createBytesPreparer(int index, byte[] v)
			throws ConfigurationException
	{
		ValuePreparer preparer = this.bytesPreparer.createPreparer(v);
		preparer.setRelativeIndex(index);
		return preparer;
	}

	public ValuePreparer createShortPreparer(int index, short v)
			throws ConfigurationException
	{
		ValuePreparer preparer = this.shortPreparer.createPreparer(v);
		preparer.setRelativeIndex(index);
		return preparer;
	}

	public ValuePreparer createIntPreparer(int index, int v)
			throws ConfigurationException
	{
		ValuePreparer preparer = this.intPreparer.createPreparer(v);
		preparer.setRelativeIndex(index);
		return preparer;
	}

	public ValuePreparer createLongPreparer(int index, long v)
			throws ConfigurationException
	{
		ValuePreparer preparer = this.longPreparer.createPreparer(v);
		preparer.setRelativeIndex(index);
		return preparer;
	}

	public ValuePreparer createFloatPreparer(int index, float v)
			throws ConfigurationException
	{
		ValuePreparer preparer = this.floatPreparer.createPreparer(v);
		preparer.setRelativeIndex(index);
		return preparer;
	}

	public ValuePreparer createDoublePreparer(int index, double v)
			throws ConfigurationException
	{
		ValuePreparer preparer = this.doublePreparer.createPreparer(v);
		preparer.setRelativeIndex(index);
		return preparer;
	}

	public ValuePreparer createStringPreparer(int index, String v)
			throws ConfigurationException
	{
		ValuePreparer preparer = this.stringPreparer.createPreparer(v);
		preparer.setRelativeIndex(index);
		return preparer;
	}

	public ValuePreparer createStreamPreparer(int index, InputStream v, int length)
			throws ConfigurationException
	{
		ValuePreparer preparer = this.streamPreparer.createPreparer(v, length);
		preparer.setRelativeIndex(index);
		return preparer;
	}

	public ValuePreparer createReaderPreparer(int index, Reader v, int length)
			throws ConfigurationException
	{
		ValuePreparer preparer = this.readerPreparer.createPreparer(v, length);
		preparer.setRelativeIndex(index);
		return preparer;
	}

	public ValuePreparer createDatePreparer(int index, Date v)
			throws ConfigurationException
	{
		ValuePreparer preparer = this.datePreparer.createPreparer(v);
		preparer.setRelativeIndex(index);
		return preparer;
	}

	public ValuePreparer createTimePreparer(int index, Time v)
			throws ConfigurationException
	{
		ValuePreparer preparer = this.timePreparer.createPreparer(v);
		preparer.setRelativeIndex(index);
		return preparer;
	}

	public ValuePreparer createTimestampPreparer(int index, Timestamp v)
			throws ConfigurationException
	{
		ValuePreparer preparer = this.timpstampPreparer.createPreparer(v);
		preparer.setRelativeIndex(index);
		return preparer;
	}

	public ValuePreparer createObjectPreparer(int index, Object v)
			throws ConfigurationException
	{
		ValuePreparer preparer = this.objectPreparer.createPreparer(v);
		preparer.setRelativeIndex(index);
		return preparer;
	}

}

