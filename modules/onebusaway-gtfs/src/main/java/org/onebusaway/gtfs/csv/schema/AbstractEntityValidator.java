package org.onebusaway.gtfs.csv.schema;

import org.onebusaway.gtfs.csv.CsvEntityContext;

import java.util.Map;

public class AbstractEntityValidator implements EntityValidator {

  private int _order = 0;

  public int getOrder() {
    return _order;
  }

  public void setOrder(int order) {
    _order = order;
  }

  public void validateCSV(CsvEntityContext context, BeanWrapper object, Map<String, Object> csvValues) {

  }

  public void validateEntity(CsvEntityContext context, Map<String, Object> csvValues, BeanWrapper object) {

  }

}
