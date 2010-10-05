package org.onebusaway.transit_data_federation.services.offline;

public interface UniqueService {
  public <T> T unique(T object);
}
