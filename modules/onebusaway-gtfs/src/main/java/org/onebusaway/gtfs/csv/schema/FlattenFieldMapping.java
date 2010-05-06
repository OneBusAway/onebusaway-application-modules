/**
 * 
 */
package org.onebusaway.gtfs.csv.schema;

import org.onebusaway.gtfs.csv.CsvEntityContext;

import java.util.Collection;
import java.util.Map;

class FlattenFieldMapping extends AbstractFieldMapping {

  private Class<?> _objFieldType;

  private EntitySchema _schema;

  public FlattenFieldMapping(String csvFieldName, String objFieldName,
      Class<?> objFieldType, boolean required, EntitySchema schema) {
    super(csvFieldName, objFieldName, required);
    _objFieldType = objFieldType;
    _schema = schema;
  }

  public void getCSVFieldNames(Collection<String> names) {
    for (FieldMapping mapping : _schema.getFields())
      mapping.getCSVFieldNames(names);
  }

  public void translateFromCSVToObject(CsvEntityContext context,
      Map<String, Object> csvValues, BeanWrapper object) {

    Object id = getInstance(_objFieldType);
    BeanWrapper wrapper = BeanWrapperFactory.wrap(id);
    for (FieldMapping mapping : _schema.getFields())
      mapping.translateFromCSVToObject(context, csvValues, wrapper);
    object.setPropertyValue(_objFieldName, id);
  }

  public void translateFromObjectToCSV(CsvEntityContext context,
      BeanWrapper object, Map<String, Object> csvValues) {
    Object id = object.getPropertyValue(_objFieldName);
    BeanWrapper wrapper = BeanWrapperFactory.wrap(id);
    for (FieldMapping mapping : _schema.getFields())
      mapping.translateFromObjectToCSV(context, wrapper, csvValues);
  }

  private Object getInstance(Class<?> type) {
    try {
      return type.newInstance();
    } catch (Exception ex) {
      throw new IllegalStateException("error instantiating embedded id class "
          + type, ex);
    }
  }
}