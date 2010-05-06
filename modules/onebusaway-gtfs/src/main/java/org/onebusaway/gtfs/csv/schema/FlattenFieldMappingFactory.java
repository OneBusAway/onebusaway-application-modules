package org.onebusaway.gtfs.csv.schema;

public class FlattenFieldMappingFactory implements FieldMappingFactory {

  public FieldMapping createFieldMapping(EntitySchemaFactory schemaFactory, String csvFieldName, String objFieldName,
      Class<?> objFieldType, boolean required) {

    EntitySchema schema = schemaFactory.getSchema(objFieldType);
    return new FlattenFieldMapping(csvFieldName, objFieldName, objFieldType, required, schema);
  }
}
