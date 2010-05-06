package org.onebusaway.gtfs.serialization;

import org.onebusaway.gtfs.model.Agency;

import java.io.Serializable;
import java.util.List;

public interface GtfsReaderContext {

  public String getFeedId();

  public List<Agency> getAgencies();

  public Object getEntity(Class<?> entityClass, Serializable id);
}
