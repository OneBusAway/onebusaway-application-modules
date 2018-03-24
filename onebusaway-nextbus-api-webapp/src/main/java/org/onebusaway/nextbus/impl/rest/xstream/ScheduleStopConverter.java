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

import java.text.SimpleDateFormat;
import java.util.Date;

import org.onebusaway.nextbus.impl.util.ConversionUtil;
import org.onebusaway.nextbus.model.nextbus.BodyError;
import org.onebusaway.nextbus.model.nextbus.DisplayStop;
import org.onebusaway.nextbus.model.nextbus.ScheduleStop;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class ScheduleStopConverter implements Converter {

	public boolean canConvert(Class type) {
		return type.equals(ScheduleStop.class);
	}

	@Override
	public void marshal(Object source, HierarchicalStreamWriter writer,
			MarshallingContext context) {
		ScheduleStop stop = (ScheduleStop) source;
		writer.addAttribute("tag", stop.getTag());
		writer.addAttribute("epochTime", Long.toString(stop.getEpochTime()));
		
		Date date = new Date(stop.getEpochTime());
		date = ConversionUtil.convertLocalDateToDateTimezone(date);
		
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
		
		writer.setValue(sdf.format(date));

	}

	@Override
	public Object unmarshal(HierarchicalStreamReader reader,
			UnmarshallingContext context) {
		ScheduleStop stop = new ScheduleStop();
		stop.setValue(reader.getValue());
		stop.setTag(reader.getAttribute("tag"));
		return stop;
	}
}