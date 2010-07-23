package org.onebusaway.transit_data_federation.services.beans;

import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.gtfs.model.AgencyAndId;

import java.util.List;

/**
 * 
 * @author bdferris
 *
 */
public interface GeospatialBeanService {
  
  public List<AgencyAndId> getStopsByBounds(CoordinateBounds bounds);
}
