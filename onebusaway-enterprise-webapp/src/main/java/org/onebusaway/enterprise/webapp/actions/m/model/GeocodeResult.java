package org.onebusaway.enterprise.webapp.actions.m.model;

import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.nyc.geocoder.service.NycGeocoderResult;
import org.onebusaway.nyc.presentation.model.SearchResult;

/**
 * Location search result.
 * 
 * @author jmaki
 * 
 */
public class GeocodeResult implements SearchResult {

	private NycGeocoderResult result;

	public GeocodeResult(NycGeocoderResult result) {
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
