package org.onebusaway.presentation.services;

import org.onebusaway.geospatial.model.CoordinateBounds;

public interface ServiceAreaService {
  public boolean hasDefaultServiceArea();
  public CoordinateBounds getServiceArea();
}
