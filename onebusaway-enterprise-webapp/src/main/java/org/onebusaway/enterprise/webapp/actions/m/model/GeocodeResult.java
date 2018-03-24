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
package org.onebusaway.enterprise.webapp.actions.m.model;

import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.geocoder.enterprise.services.EnterpriseGeocoderResult;
import org.onebusaway.presentation.model.SearchResult;

/**
 * Location search result.
 * 
 * @author jmaki
 * 
 */
public class GeocodeResult implements SearchResult {

	private EnterpriseGeocoderResult result;

	public GeocodeResult(EnterpriseGeocoderResult result) {
		this.result = result;
	}

	public String getFormattedAddress() {
		return result.getFormattedAddress();
	}

	public String getNeighborhood() {
		return result.getNeighborhood();
	}

	public Double getLatitude() {
		return result.getLatitude();
	}

	public Double getLongitude() {
		return result.getLongitude();
	}

	public Boolean getIsRegion() {
		return result.isRegion();
	}

	public CoordinateBounds getBounds() {
		return result.getBounds();
	}
}
