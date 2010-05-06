package org.onebusaway.federations;

import java.util.List;
import java.util.Map;

import org.onebusaway.geospatial.model.CoordinateBounds;

public interface FederatedService {
  public Map<String, List<CoordinateBounds>> getAgencyIdsWithCoverageArea();
}
