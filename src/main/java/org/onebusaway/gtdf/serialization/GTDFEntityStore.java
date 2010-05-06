package org.onebusaway.gtdf.serialization;

import java.io.Serializable;

public interface GTDFEntityStore {

  public Object load(Class<?> entityClass, Serializable id);

  public void save(Object entity);

  public void flush();
}
