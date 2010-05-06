package org.onebusaway.gtfs.serialization;

import org.onebusaway.gtfs.model.Agency;

import java.io.Serializable;
import java.util.List;

public interface GtfsReaderContext {

  public String getDefaultAgencyId();
  
  public String getTranslatedAgencyId(String agencyId);

  public List<Agency> getAgencies();

  public Object getEntity(Class<?> entityClass, Serializable id);

  public String getAgencyForEntity(Class<?> entityType, String entityId);
}
