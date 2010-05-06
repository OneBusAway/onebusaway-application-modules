package org.onebusaway.gtfs.csv.schema.beans;

import org.onebusaway.gtfs.csv.schema.EntityValidator;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CsvEntityMappingBean {

  private final Class<?> type;

  private boolean filenameSet = false;

  private String filename;

  private boolean prefixSet = false;

  private String prefix;

  private boolean requiredSet = false;

  private boolean required;

  private List<EntityValidator> _validators = new ArrayList<EntityValidator>();

  private Map<Field, CsvFieldMappingBean> fields = new HashMap<Field, CsvFieldMappingBean>();

  public CsvEntityMappingBean(Class<?> type) {
    this.type = type;
  }

  public Class<?> getType() {
    return type;
  }

  public boolean isFilenameSet() {
    return filenameSet;
  }

  public String getFilename() {
    return filename;
  }

  public void setFilename(String filename) {
    this.filenameSet = true;
    this.filename = filename;
  }

  public boolean isPrefixSet() {
    return prefixSet;
  }

  public String getPrefix() {
    return prefix;
  }

  public void setPrefix(String prefix) {
    this.prefixSet = true;
    this.prefix = prefix;
  }

  public boolean isRequiredSet() {
    return requiredSet;
  }

  public boolean isRequired() {
    return required;
  }

  public void setRequired(boolean required) {
    this.required = required;
  }

  public void addField(CsvFieldMappingBean field) {
    this.fields.put(field.getField(), field);
  }

  public Map<Field, CsvFieldMappingBean> getFields() {
    return fields;
  }

  public void addValidator(EntityValidator validator) {
    _validators.add(validator);
  }

  public List<EntityValidator> getValidators() {
    return _validators;
  }

}
