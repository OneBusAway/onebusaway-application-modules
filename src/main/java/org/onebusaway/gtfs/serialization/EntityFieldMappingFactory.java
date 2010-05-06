package org.onebusaway.gtfs.serialization;

import org.onebusaway.common.model.IdentityBean;
import org.onebusaway.csv.AbstractFieldMapping;
import org.onebusaway.csv.CsvEntityContext;
import org.onebusaway.csv.EntitySchemaFactory;
import org.onebusaway.csv.FieldMapping;
import org.onebusaway.csv.FieldMappingFactory;
import org.springframework.beans.BeanWrapper;

import java.io.Serializable;
import java.util.Map;

public class EntityFieldMappingFactory implements FieldMappingFactory {

  public FieldMapping createFieldMapping(EntitySchemaFactory schemaFactory,
      String csvFieldName, String objFieldName, Class<?> objFieldType,
      boolean required) {
    return new FieldMappingImpl(csvFieldName, objFieldName, objFieldType, required);
  }

  public static class FieldMappingImpl extends AbstractFieldMapping {

    private Class<?> _objFieldType;

    public FieldMappingImpl(String csvFieldName, String objFieldName,
        Class<?> objFieldType, boolean required) {
      super(csvFieldName, objFieldName, required);
      _objFieldType = objFieldType;
    }

    public void translateFromCSVToObject(CsvEntityContext context,
        Map<String, Object> csvValues, BeanWrapper object) {

      if (isMissingAndOptional(csvValues))
        return;

      try {
        GtfsReaderContext ctx = (GtfsReaderContext) context.get(GtfsReader.KEY_CONTEXT);

        Serializable id = (Serializable) csvValues.get(_csvFieldName);
        Object entity = ctx.getEntity(_objFieldType, id);
        object.setPropertyValue(_objFieldName, entity);
      } catch (Exception ex) {
        throw new IllegalStateException("error setting entity: csvField="
            + _csvFieldName + " objField=" + _objFieldName + " objType="
            + _objFieldType,ex);
      }
    }

    public void translateFromObjectToCSV(CsvEntityContext context,
        BeanWrapper object, Map<String, Object> csvValues) {

      IdentityBean<?> entity = (IdentityBean<?>) object.getPropertyValue(_objFieldName);

      if (isOptional() && entity == null)
        return;

      csvValues.put(_csvFieldName, entity.getId());
    }
  }

}