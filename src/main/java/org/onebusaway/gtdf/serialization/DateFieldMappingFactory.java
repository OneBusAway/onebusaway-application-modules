package org.onebusaway.gtdf.serialization;

import org.onebusaway.csv.AbstractFieldMapping;
import org.onebusaway.csv.CsvEntityContext;
import org.onebusaway.csv.EntitySchemaFactory;
import org.onebusaway.csv.FieldMapping;
import org.onebusaway.csv.FieldMappingFactory;
import org.springframework.beans.BeanWrapper;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

public class DateFieldMappingFactory implements FieldMappingFactory {

  private static DateFormat _format = new SimpleDateFormat("yyyyMMdd");

  public FieldMapping createFieldMapping(EntitySchemaFactory schemaFactory,
      String csvFieldName, String objFieldName, Class<?> objFieldType, boolean required) {
    return new FieldMappingImpl(csvFieldName, objFieldName);
  }

  private static class FieldMappingImpl extends AbstractFieldMapping {

    public FieldMappingImpl(String csvFieldName, String objFieldName) {
      super(csvFieldName, objFieldName, true);
    }

    public void translateFromCSVToObject(CsvEntityContext context,
        Map<String, Object> csvValues, BeanWrapper object) {

      if (isMissingAndOptional(csvValues))
        return;

      Object value = csvValues.get(_csvFieldName);

      try {

        Date date = _format.parse(value.toString());
        object.setPropertyValue(_objFieldName, date);
      } catch (ParseException ex) {
        throw new IllegalStateException("error parsing date: " + value, ex);
      }
    }

    public void translateFromObjectToCSV(CsvEntityContext context,
        BeanWrapper object, Map<String, Object> csvValues) {

      Date date = (Date) object.getPropertyValue(_objFieldName);
      String value = _format.format(date);
      csvValues.put(_csvFieldName, value);
    }
  }

}
