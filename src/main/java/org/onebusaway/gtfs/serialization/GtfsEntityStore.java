package org.onebusaway.gtfs.serialization;

import java.io.Serializable;

public interface GtfsEntityStore {

  public Object load(Class<?> entityClass, Serializable id);

  public void save(Object entity);

  public void flush();
}
