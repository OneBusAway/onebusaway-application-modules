package org.onebusaway.nextbus.impl.xstream;

import org.onebusaway.nextbus.model.nextbus.BodyError;
import org.onebusaway.nextbus.model.nextbus.DisplayStop;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class DisplayStopConverter implements Converter {

	public boolean canConvert(Class type) {
		return type.equals(DisplayStop.class);
	}

	@Override
	public void marshal(Object source, HierarchicalStreamWriter writer,
			MarshallingContext context) {
		DisplayStop stop = (DisplayStop) source;
		writer.addAttribute("tag", stop.getTag());
		writer.setValue(stop.getValue());

	}

	@Override
	public Object unmarshal(HierarchicalStreamReader reader,
			UnmarshallingContext context) {
		DisplayStop stop = new DisplayStop();
		stop.setValue(reader.getValue());
		stop.setTag(reader.getAttribute("tag"));
		return stop;
	}
}