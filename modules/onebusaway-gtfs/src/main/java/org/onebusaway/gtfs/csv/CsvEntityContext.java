package org.onebusaway.gtfs.csv;

public interface CsvEntityContext {

  public Object put(Object key, Object value);

  public Object get(Object key);
}
