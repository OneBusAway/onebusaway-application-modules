package org.onebusaway.federations;

import org.onebusaway.geospatial.model.CoordinateBounds;

import java.util.List;
import java.util.Map;

public interface FederatedService {
  public Map<String,List<CoordinateBounds>> getAgencyIdsWithCoverageArea();
}
