package org.onebusaway.csv;

import org.springframework.beans.BeanWrapper;

import java.util.Collection;
import java.util.Map;

public interface FieldMapping {

  public void getCSVFieldNames(Collection<String> names);

  public void translateFromCSVToObject(CsvEntityContext context,
      Map<String, Object> csvValues, BeanWrapper object);

  public void translateFromObjectToCSV(CsvEntityContext context,
      BeanWrapper object, Map<String, Object> csvValues);
}
