package org.onebusaway.csv;

public interface FieldMappingFactory {
  public FieldMapping createFieldMapping(EntitySchemaFactory schemaFactory,
      String csvFieldName, String objFieldName, Class<?> objFieldType,
      boolean required);
}
