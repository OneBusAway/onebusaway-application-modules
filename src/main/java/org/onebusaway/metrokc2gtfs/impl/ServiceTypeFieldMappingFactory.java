package org.onebusaway.metrokc2gtfs.impl;

import org.onebusaway.csv.AbstractFieldMapping;
import org.onebusaway.csv.CsvEntityContext;
import org.onebusaway.csv.EntitySchemaFactory;
import org.onebusaway.csv.FieldMapping;
import org.onebusaway.csv.FieldMappingFactory;
import org.springframework.beans.BeanWrapper;

import java.util.Map;

public class ServiceTypeFieldMappingFactory implements
    FieldMappingFactory {

  public FieldMapping createFieldMapping(EntitySchemaFactory schemaFactory,
      String csvFieldName, String objFieldName, Class<?> objFieldType, boolean required) {

    return new FieldMappingImpl(csvFieldName, objFieldName, required);
  }

  private static class FieldMappingImpl extends AbstractFieldMapping {

    public FieldMappingImpl(String csvFieldName, String objFieldName,
        boolean required) {
      super(csvFieldName, objFieldName, required);
    }

    public void translateFromCSVToObject(CsvEntityContext context,
        Map<String, Object> csvValues, BeanWrapper object) {

      if (isMissingAndOptional(csvValues))
        return;

      String scheduleType = csvValues.get(_csvFieldName).toString();
      boolean isExpress = scheduleType.equals("E");
      object.setPropertyValue(_objFieldName, isExpress);
    }

    public void translateFromObjectToCSV(CsvEntityContext context,
        BeanWrapper object, Map<String, Object> csvValues) {

      boolean isExpress = (Boolean) object.getPropertyValue(_objFieldName);
      String scheduleType = isExpress ? "E" : "L";
      csvValues.put(_csvFieldName, scheduleType);
    }
  }
}
