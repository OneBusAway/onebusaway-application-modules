package org.onebusaway.nextbus.impl.xstream;

import org.onebusaway.nextbus.model.nextbus.BodyError;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.SingleValueConverter;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class BodyErrorConverter implements Converter {

	public boolean canConvert(Class type) {
		return type.equals(BodyError.class);
	}

	@Override
	public void marshal(Object source, HierarchicalStreamWriter writer,
			MarshallingContext context) {
		BodyError error = (BodyError) source;
		writer.addAttribute("shouldRetry", Boolean.toString(error.isShouldRetry()));
		writer.setValue(error.getContent());

	}

	@Override
	public Object unmarshal(HierarchicalStreamReader reader,
			UnmarshallingContext context) {
		BodyError error = new BodyError();
		error.setContent(reader.getValue());
		error.setShouldRetry(Boolean.parseBoolean(reader.getAttribute("shouldRetry")));
		return error;
	}
}