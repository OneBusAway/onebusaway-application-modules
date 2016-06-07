/**
 * Copyright (C) 2015 Cambridge Systematics
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onebusaway.nextbus.impl.rest.xstream;

import java.lang.reflect.Field;

import org.apache.commons.lang.StringUtils;

import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.ConverterLookup;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.converters.reflection.ReflectionConverter;
import com.thoughtworks.xstream.converters.reflection.ReflectionProvider;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.mapper.Mapper;

/**
 * This is a simple converter for XSTream that puts the text node of the tag
 * into the specified field and redirects other processing to
 * {@link ReflectionConverter}
 * 
 * 
 * @author Aleksandr Panzin
 * 
 */
public class ValueConverter implements Converter {

  private final Class<?> type;
	private final String fieldName;
	private final Class<?> valueDefinedIn;
	private final Mapper mapper;
	private final ReflectionProvider reflectionProvider;

	public ValueConverter(final Class<?> type, final Mapper mapper,
			final ReflectionProvider reflectionProvider,
			final ConverterLookup lookup, final String valueFieldName) {
		this(type, mapper, reflectionProvider, lookup, valueFieldName, type);
	}

	public ValueConverter(final Class<?> type, final Mapper mapper,
			final ReflectionProvider reflectionProvider,
			final ConverterLookup lookup, final String valueFieldName,
			final Class<?> valueDefinedIn) {
		super();
		this.type = type;
		this.fieldName = valueFieldName;
		this.valueDefinedIn = valueDefinedIn;
		this.mapper = mapper;
		this.reflectionProvider = reflectionProvider;

		try {
			Field f = valueDefinedIn.getDeclaredField(fieldName);
			if (!f.getType().equals(String.class))
				throw new IllegalStateException("The field " + fieldName
						+ " has to be of type String");
		} catch (NoSuchFieldException e) {
			throw new IllegalStateException("Field " + fieldName
					+ " not found on " + valueDefinedIn, e);
		} catch (Exception e) {
			throw new IllegalStateException(
					"Could not initialize ValueConverter", e);
		}

	}

	@Override
	public boolean canConvert(Class type) {
		return this.type.equals(type);
	}

	@Override
	public void marshal(Object source, HierarchicalStreamWriter writer,
			MarshallingContext context) {
		try {
			Field f = valueDefinedIn.getDeclaredField(fieldName);
			f.setAccessible(true);
			Object value = f.get(source);
			if (value != null && StringUtils.isNotBlank(value.toString())) {
				writer.setValue(value.toString());
			}
		} catch (Exception e) {
			throw new ConversionException("Could not convert field: "
					+ fieldName, e);
		}
		context.convertAnother(source, new ReflectionConverter(this.mapper,
				this.reflectionProvider));
	}

	@Override
	public Object unmarshal(HierarchicalStreamReader reader,
			UnmarshallingContext context) {
		Object o = context.convertAnother(context.currentObject(), context
				.getRequiredType(), new ReflectionConverter(this.mapper,
				this.reflectionProvider));

		try {
			Field f = valueDefinedIn.getDeclaredField(fieldName);
			f.setAccessible(true);
			String value = reader.getValue();
			if (StringUtils.isNotBlank(value)) {
				f.set(o, value);
			}
		} catch (Exception e) {
			throw new ConversionException("Could not convert field: "
					+ fieldName, e);
		}

		return o;
	}

}
