package org.onebusaway.common.services;

import org.onebusaway.common.model.Place;

import java.util.List;
import java.util.Set;

public interface CommonDao {
  public List<Place> getPlacesByIds(Set<String> ids);
}
