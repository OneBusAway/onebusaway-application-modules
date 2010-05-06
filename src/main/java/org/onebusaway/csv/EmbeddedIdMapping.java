package org.onebusaway.csv;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class EmbeddedIdMapping implements FieldMapping {

  private String _idField;

  private Class<?> _idType;

  private List<FieldMapping> _mappings = new ArrayList<FieldMapping>();

  public EmbeddedIdMapping(EntitySchemaFactory factory, Field field) {

    _idField = field.getName();
    _idType = field.getType();

    Class<?> type = field.getType();
    for (Field idField : type.getDeclaredFields()) {
      if (!factory.isFieldIgnored(idField))
        _mappings.add(factory.getFieldMapping(idField, ""));
    }
  }

  public void getCSVFieldNames(Collection<String> names) {
    for (FieldMapping mapping : _mappings)
      mapping.getCSVFieldNames(names);
  }

  public void translateFromCSVToObject(CsvEntityContext context,
      Map<String, Object> csvValues, BeanWrapper object) {

    Object id = getInstance(_idType);
    BeanWrapper wrapper = PropertyAccessorFactory.forBeanPropertyAccess(id);
    for (FieldMapping mapping : _mappings)
      mapping.translateFromCSVToObject(context, csvValues, wrapper);
    object.setPropertyValue(_idField, id);
  }

  public void translateFromObjectToCSV(CsvEntityContext context,
      BeanWrapper object, Map<String, Object> csvValues) {
    Object id = object.getPropertyValue(_idField);
    BeanWrapper wrapper = PropertyAccessorFactory.forBeanPropertyAccess(id);
    for (FieldMapping mapping : _mappings)
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
