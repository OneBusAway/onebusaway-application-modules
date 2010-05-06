package org.onebusaway.gtfs.csv.schema;


public interface BeanWrapper {
  /**
   * Get the current value of the specified property.
   * 
   * @param propertyName the name of the property to get the value of (may be a
   *          nested path and/or an indexed/mapped property)
   * @return the value of the property
   * @throws InvalidPropertyException if there is no such property or if the
   *           property isn't readable
   * @throws PropertyAccessException if the property was valid but the accessor
   *           method failed
   */
  Object getPropertyValue(String propertyName);

  /**
   * Set the specified value as current property value.
   * 
   * @param propertyName the name of the property to set the value of (may be a
   *          nested path and/or an indexed/mapped property)
   * @param value the new value
   * @throws InvalidPropertyException if there is no such property or if the
   *           property isn't writable
   * @throws PropertyAccessException if the property was valid but the accessor
   *           method failed or a type mismatch occured
   */
  void setPropertyValue(String propertyName, Object value);

  public <T> T getWrappedInstance(Class<T> type);
}
