package org.onebusaway.csv;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import javax.persistence.EmbeddedId;

public class EntitySchemaFactory {

  public EntitySchema getSchema(Class<?> entityClass) {

    CsvFields csvFields = entityClass.getAnnotation(CsvFields.class);

    if (csvFields == null)
      throw new IllegalStateException("no csv fields info for entity class: "
          + entityClass);

    String name = csvFields.filename();
    String prefix = csvFields.prefix();
    boolean required = csvFields.required();

    EntitySchema schema = new EntitySchema(entityClass, name, required);

    for (Field field : entityClass.getDeclaredFields()) {

      if (isFieldIgnored(field))
        continue;

      FieldMapping mapping = getFieldMapping(field, prefix);
      schema.addField(mapping);
    }

    return schema;
  }

  public boolean isFieldIgnored(Field field) {

    if ((field.getModifiers() & (Modifier.FINAL | Modifier.STATIC)) != 0)
      return true;

    CsvField csvField = field.getAnnotation(CsvField.class);
    if (csvField != null)
      return csvField.ignore();

    return false;
  }

  public FieldMapping getFieldMapping(Field field, String prefix) {

    FieldMapping mapping = null;

    String objFieldName = field.getName();
    Class<?> objFieldType = field.getType();

    String csvFieldName = prefix
        + getObjectFieldNameAsCSVFieldName(objFieldName);
    boolean required = true;

    CsvField csvField = field.getAnnotation(CsvField.class);

    if (csvField != null) {

      required = !csvField.optional();

      if (!csvField.name().equals(""))
        csvFieldName = csvField.name();

      if (!csvField.mapping().equals(FieldMappingFactory.class)) {
        Class<? extends FieldMappingFactory> c = csvField.mapping();
        try {
          FieldMappingFactory factory = c.newInstance();
          mapping = factory.createFieldMapping(this, csvFieldName,
              objFieldName, objFieldType, required);
        } catch (Exception ex) {
          throw new IllegalStateException(
              "error instantiating fromCVS converter: " + c, ex);
        }
      }
    }

    if (mapping == null) {
      EmbeddedId eid = field.getAnnotation(EmbeddedId.class);
      if (eid != null)
        mapping = new EmbeddedIdMapping(this, field);
    }

    if (mapping == null)
      mapping = new DefaultFieldMapping(csvFieldName, objFieldName,
          objFieldType, required);

    return mapping;
  }

  private String getObjectFieldNameAsCSVFieldName(String fieldName) {

    StringBuilder b = new StringBuilder();
    boolean wasUpperCase = false;

    for (int i = 0; i < fieldName.length(); i++) {
      char c = fieldName.charAt(i);
      boolean isUpperCase = Character.isUpperCase(c);
      if (isUpperCase)
        c = Character.toLowerCase(c);
      if (isUpperCase && !wasUpperCase)
        b.append('_');
      b.append(c);
      wasUpperCase = isUpperCase;
    }

    return b.toString();
  }
}
