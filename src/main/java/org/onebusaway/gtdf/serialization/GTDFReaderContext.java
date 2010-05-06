package org.onebusaway.gtdf.serialization;

import org.onebusaway.gtdf.model.Agency;

import java.io.Serializable;
import java.util.List;

public interface GTDFReaderContext {

  public String getFeedId();

  public List<Agency> getAgencies();

  public Object getEntity(Class<?> entityClass, Serializable id);
}
