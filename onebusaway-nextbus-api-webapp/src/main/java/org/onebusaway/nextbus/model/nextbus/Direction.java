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
package org.onebusaway.nextbus.model.nextbus;

import java.util.ArrayList;
import java.util.List;

import org.onebusaway.nextbus.impl.rest.jackson.CapitalizeSerializer;
import org.onebusaway.nextbus.impl.rest.xstream.CapitalizeConverter;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

@XStreamAlias("direction")
public class Direction {
	@XStreamImplicit
	private List<DisplayStop> stops = new ArrayList<DisplayStop>();
	
	@XStreamAsAttribute 
	private String tag;
	
	@XStreamAsAttribute 
	@XStreamConverter(CapitalizeConverter.class)
  @JsonSerialize(using = CapitalizeSerializer.class)
	private String title;
	
	@XStreamAsAttribute 
	private String name;
	
	@XStreamAsAttribute 
	private boolean useForUI;
	
	public List<DisplayStop> getStops() {
		return stops;
	}

	public void setStops(List<DisplayStop> stops) {
		this.stops = stops;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

}
