package org.onebusaway.gtfs.csv.schema;

import org.onebusaway.gtfs.csv.CsvEntityContext;

import java.util.Map;

public interface EntityValidator {

  public int getOrder();

  public void setOrder(int order);

  public void validateEntity(CsvEntityContext context, Map<String, Object> csvValues, BeanWrapper object);

  public void validateCSV(CsvEntityContext context, BeanWrapper object, Map<String, Object> csvValues);
}
