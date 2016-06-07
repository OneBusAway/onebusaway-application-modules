/**
 * Copyright (C) 2011 Metropolitan Transportation Authority
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
package org.onebusaway.transit_data_federation.bundle.tasks.stif.model;

import java.io.Serializable;

public class GeographyRecord implements StifRecord, Serializable {
	private static final long serialVersionUID = 1L;

	private String identifier;
	private float latitude;
	private float longitude;
	private String boxID;

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}
	
	public String getIdentifier() {
		return identifier;
	}

	public void setLatitude(float latitude) {
		this.latitude = latitude;		
	}
	
	public float getLatitude() {
		return latitude;
	}
	
	public void setLongitude(float longitude) {
		this.longitude = longitude;		
	}
	public float getLongitude() {
		return longitude;
	}

	public void setBoxID(String boxID) {
		this.boxID = boxID;
	}
	
	public String getBoxID() {
		return boxID;
	}
}
