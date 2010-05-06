package org.onebusaway.gtfs.csv.schema;

public interface EntityValidatorFactory {
  public EntityValidator createEntityValidator(EntitySchemaFactory schemaFactory);
}
