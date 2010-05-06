package org.onebusaway.kcmetro2gtfs.impl;

import org.onebusaway.gtfs.csv.CsvEntityContext;
import org.onebusaway.gtfs.csv.schema.AbstractFieldMapping;
import org.onebusaway.gtfs.csv.schema.BeanWrapper;
import org.onebusaway.gtfs.csv.schema.EntitySchemaFactory;
import org.onebusaway.gtfs.csv.schema.FieldMapping;
import org.onebusaway.gtfs.csv.schema.FieldMappingFactory;

import java.util.Map;

public class ServiceTypeFieldMappingFactory implements FieldMappingFactory {

  public FieldMapping createFieldMapping(EntitySchemaFactory schemaFactory,
      String csvFieldName, String objFieldName, Class<?> objFieldType,
      boolean required) {

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
