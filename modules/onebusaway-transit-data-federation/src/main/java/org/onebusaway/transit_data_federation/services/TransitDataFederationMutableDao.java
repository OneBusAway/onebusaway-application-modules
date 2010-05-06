package org.onebusaway.transit_data_federation.services;

import java.util.List;

public interface TransitDataFederationMutableDao extends
    TransitDataFederationDao {

  public void save(Object object);

  public void update(Object object);

  public void saveOrUpdate(Object object);

  public <T> void saveOrUpdateAllEntities(List<T> updates);

  public <T> void deleteAllEntities(Iterable<T> entities);

  public void flush();
}
