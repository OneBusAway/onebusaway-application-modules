package org.onebusaway.gtfs.csv.schema;

import org.onebusaway.gtfs.csv.schema.beans.CsvEntityMappingBean;

import java.util.Collection;

public interface ListableCsvMappingFactory {
  public Collection<CsvEntityMappingBean> getEntityMappings();
}
