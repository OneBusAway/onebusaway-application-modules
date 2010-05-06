package org.onebusaway.gtfs.serialization.mappings;

import org.onebusaway.gtfs.csv.CsvEntityContext;
import org.onebusaway.gtfs.csv.schema.AbstractFieldMapping;
import org.onebusaway.gtfs.csv.schema.BeanWrapper;
import org.onebusaway.gtfs.csv.schema.EntitySchemaFactory;
import org.onebusaway.gtfs.csv.schema.FieldMapping;
import org.onebusaway.gtfs.csv.schema.FieldMappingFactory;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.IdentityBean;
import org.onebusaway.gtfs.serialization.GtfsReader;
import org.onebusaway.gtfs.serialization.GtfsReaderContext;

import java.util.Map;

public class EntityFieldMappingFactory implements FieldMappingFactory {

  public EntityFieldMappingFactory() {

  }

  public EntityFieldMappingFactory(String csvFieldName) {

  }

  public FieldMapping createFieldMapping(EntitySchemaFactory schemaFactory,
      String csvFieldName, String objFieldName, Class<?> objFieldType,
      boolean required) {
    return new FieldMappingImpl(csvFieldName, objFieldName, objFieldType,
        required);
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
        String entityId = (String) csvValues.get(_csvFieldName);
        String agencyId = ctx.getAgencyForEntity(_objFieldType, entityId);
        AgencyAndId id = new AgencyAndId(agencyId, entityId);
        Object entity = ctx.getEntity(_objFieldType, id);
        object.setPropertyValue(_objFieldName, entity);
      } catch (Exception ex) {
        throw new IllegalStateException("error setting entity: csvField="
            + _csvFieldName + " objField=" + _objFieldName + " objType="
            + _objFieldType, ex);
      }
    }

    @SuppressWarnings("unchecked")
    public void translateFromObjectToCSV(CsvEntityContext context,
        BeanWrapper object, Map<String, Object> csvValues) {

      IdentityBean<AgencyAndId> entity = (IdentityBean<AgencyAndId>) object.getPropertyValue(_objFieldName);

      if (isOptional() && entity == null)
        return;

      AgencyAndId id = entity.getId();

      csvValues.put(_csvFieldName, id.getId());
    }
  }

}