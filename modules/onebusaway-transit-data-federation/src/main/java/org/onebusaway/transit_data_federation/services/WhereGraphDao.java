package org.onebusaway.transit_data_federation.services;

import org.onebusaway.gtfs.model.AgencyAndId;

import java.util.Set;

public interface WhereGraphDao {
  public Set<AgencyAndId> getRouteCollectionIdsForStop(AgencyAndId stopId);
}
