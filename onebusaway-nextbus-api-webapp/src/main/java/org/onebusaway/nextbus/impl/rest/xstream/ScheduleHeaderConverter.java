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

import org.onebusaway.nextbus.model.transiTime.ScheduleHeader;
import org.onebusaway.nextbus.model.transiTime.ScheduleStop;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class ScheduleHeaderConverter implements Converter {

	public boolean canConvert(Class type) {
		return type.equals(ScheduleHeader.class);
	}

	 @Override
	  public void marshal(Object source, HierarchicalStreamWriter writer,
	      MarshallingContext context) {
	   ScheduleHeader header = (ScheduleHeader) source;
	    writer.addAttribute("tag", header.getStopId());
	    writer.setValue(header.getStopName());
	  }

	  @Override
	  public Object unmarshal(HierarchicalStreamReader reader,
	      UnmarshallingContext context) {
	    ScheduleHeader header = new ScheduleHeader();
	    header.setStopName(reader.getValue());
	    header.setStopId((reader.getAttribute("tag")));
	    return header;
	  }
}