/**
 * 
 */
package org.onebusaway.transit_data_federation.utilities;

import java.util.HashMap;
import java.util.Map;

import org.onebusaway.gtfs.csv.CsvEntityContext;
import org.onebusaway.gtfs.csv.schema.AbstractFieldMapping;
import org.onebusaway.gtfs.csv.schema.BeanWrapper;
import org.onebusaway.gtfs.csv.schema.DefaultEntitySchemaFactory;
import org.onebusaway.gtfs.csv.schema.beans.CsvEntityMappingBean;

/**
 * Field mapping to add a field to the serialized csv fields of an object
 * 
 * @author bdferris
 * 
 */
class GenericAdditionalFieldMapping extends AbstractFieldMapping {

  private Map<Object, Object> _valuesById = new HashMap<Object, Object>();

  public static void addGenericFieldMapping(DefaultEntitySchemaFactory factory,
      Class<?> entityType, String csvFieldName, String objFieldName) {

    GenericAdditionalFieldMapping fieldMapping = new GenericAdditionalFieldMapping(
        entityType, csvFieldName, objFieldName);

    // Move this to the end of the list!
    fieldMapping.setOrder(100);

    CsvEntityMappingBean mappingBean = new CsvEntityMappingBean(entityType);
    mappingBean.addAdditionalFieldMapping(fieldMapping);
    factory.addBean(mappingBean);
  }

  public GenericAdditionalFieldMapping(Class<?> entityType,
      String csvFieldName, String objFieldName) {
    super(entityType, csvFieldName, objFieldName, false);
  }

  @Override
  public void translateFromCSVToObject(CsvEntityContext context,
      Map<String, Object> csvValues, BeanWrapper object) {

    Object id = object.getPropertyValue("id");

    if (id == null)
      throw new IllegalStateException("id not found: " + csvValues);

    Object value = csvValues.get(_csvFieldName);

    if (value != null)
      _valuesById.put(id, value);
  }

  @Override
  public void translateFromObjectToCSV(CsvEntityContext context,
      BeanWrapper object, Map<String, Object> csvValues) {
    Object id = object.getPropertyValue("id");
    Object value = _valuesById.get(id);
    if (value != null)
      csvValues.put(_csvFieldName, value);
  }
}