package org.onebusaway.gtfs.csv.schema;

import java.util.ArrayList;
import java.util.List;

public class EntitySchema {

  private String _filename;

  private List<FieldMapping> _fields = new ArrayList<FieldMapping>();

  private List<EntityValidator> _validators = new ArrayList<EntityValidator>();

  private Class<?> _entityClass;

  private boolean _required;

  public EntitySchema(Class<?> entityClass, String filename, boolean required) {
    _entityClass = entityClass;
    _filename = filename;
    _required = required;
  }

  public void addField(FieldMapping field) {
    _fields.add(field);
  }

  public void addValidator(EntityValidator entityValidator) {
    _validators.add(entityValidator);
  }

  public Class<?> getEntityClass() {
    return _entityClass;
  }

  public String getFilename() {
    return _filename;
  }

  public boolean isRequired() {
    return _required;
  }

  public List<FieldMapping> getFields() {
    return _fields;
  }

  public List<EntityValidator> getValidators() {
    return _validators;
  }
}
