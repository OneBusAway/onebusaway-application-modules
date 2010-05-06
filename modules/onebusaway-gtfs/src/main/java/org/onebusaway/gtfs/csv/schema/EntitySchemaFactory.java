package org.onebusaway.gtfs.csv.schema;

public interface EntitySchemaFactory {

  public EntitySchema getSchema(Class<?> entityClass);

}