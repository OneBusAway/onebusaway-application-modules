package org.onebusaway.transit_data_federation.model.modifications;

/**
 * Captures a modification to a entity with specified type and id, where the
 * entity's specified property will be set to the specified value.
 * 
 * @author bdferris
 * @see Modifications
 */
public class Modification {

  private Class<?> type;

  private String id;

  private String property;

  private Object value;

  public Class<?> getType() {
    return type;
  }

  public void setType(Class<?> type) {
    this.type = type;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getProperty() {
    return property;
  }

  public void setProperty(String property) {
    this.property = property;
  }

  public Object getValue() {
    return value;
  }

  public void setValue(Object value) {
    this.value = value;
  }

}
