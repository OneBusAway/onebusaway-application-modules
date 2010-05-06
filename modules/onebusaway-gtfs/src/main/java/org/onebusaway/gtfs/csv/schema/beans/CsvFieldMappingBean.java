package org.onebusaway.gtfs.csv.schema.beans;

import org.onebusaway.gtfs.csv.schema.FieldMappingFactory;

import java.lang.reflect.Field;

public class CsvFieldMappingBean {
  
  private final Field field;
  
  private boolean nameSet = false;
  private String name;
  
  private boolean ignoreSet = false;
  private boolean ignore;
  
  private boolean optionalSet = false;
  private boolean optional;
  
  private boolean mappingSet = false;
  private FieldMappingFactory mapping;
  
  private boolean orderSet = false;
  private int order;

  public CsvFieldMappingBean(Field field) {
    this.field = field;
  }

  public Field getField() {
    return field;
  }

  public boolean isNameSet() {
    return nameSet;
  }
  
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.nameSet = true;
    this.name = name;
  }

  public boolean isIgnoreSet() {
    return ignoreSet;
  }
  
  public boolean isIgnore() {
    return ignore;
  }

  public void setIgnore(boolean ignore) {
    this.ignoreSet = true;
    this.ignore = ignore;
  }

  public boolean isOptionalSet() {
    return optionalSet;
  }
  
  public boolean isOptional() {
    return optional;
  }

  public void setOptional(boolean optional) {
    this.optionalSet = true;
    this.optional = optional;
  }

  public boolean isMappingSet() {
    return mappingSet;
  }
  public FieldMappingFactory getMapping() {
    return mapping;
  }

  public void setMapping(FieldMappingFactory mapping) {
    this.mappingSet = true;
    this.mapping = mapping;
  }

  public boolean isOrderSet() {
    return orderSet;
  }
  
  public int getOrder() {
    return order;
  }

  public void setOrder(int order) {
    this.optionalSet = true;
    this.order = order;
  }
}
