package org.onebusaway.gtfs.csv.schema;

import org.onebusaway.gtfs.csv.CsvEntityContext;

import java.util.Collection;
import java.util.Map;

public interface FieldMapping {

  public int getOrder();

  public void setOrder(int order);

  public void getCSVFieldNames(Collection<String> names);

  public void translateFromCSVToObject(CsvEntityContext context,
      Map<String, Object> csvValues, BeanWrapper object);

  public void translateFromObjectToCSV(CsvEntityContext context,
      BeanWrapper object, Map<String, Object> csvValues);
}
