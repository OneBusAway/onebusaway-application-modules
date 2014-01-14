/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * 
 */
package org.onebusaway.transit_data_federation.bundle.utilities;

import java.util.HashMap;
import java.util.Map;

import org.onebusaway.csv_entities.CsvEntityContext;
import org.onebusaway.csv_entities.schema.AbstractFieldMapping;
import org.onebusaway.csv_entities.schema.BeanWrapper;
import org.onebusaway.csv_entities.schema.DefaultEntitySchemaFactory;
import org.onebusaway.csv_entities.schema.beans.CsvEntityMappingBean;

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