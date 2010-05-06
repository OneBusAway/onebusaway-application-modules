package org.onebusaway.gtfs.serialization.mappings;

import org.onebusaway.gtfs.csv.CsvEntityContext;
import org.onebusaway.gtfs.csv.schema.AbstractFieldMapping;
import org.onebusaway.gtfs.csv.schema.BeanWrapper;
import org.onebusaway.gtfs.csv.schema.EntitySchemaFactory;
import org.onebusaway.gtfs.csv.schema.FieldMapping;
import org.onebusaway.gtfs.csv.schema.FieldMappingFactory;
import org.onebusaway.gtfs.serialization.GtfsReader;
import org.onebusaway.gtfs.serialization.GtfsReaderContext;

import java.util.Map;

public class AgencyIdTranslationFieldMappingFactory implements FieldMappingFactory {

  public FieldMapping createFieldMapping(EntitySchemaFactory schemaFactory, String csvFieldName, String objFieldName,
      Class<?> objFieldType, boolean required) {

    return new FieldMappingImpl(csvFieldName, objFieldName, String.class, required);
  }

  private class FieldMappingImpl extends AbstractFieldMapping {

    public FieldMappingImpl(String csvFieldName, String objFieldName, Class<?> objFieldType, boolean required) {
      super(csvFieldName, objFieldName, required);
    }

    public void translateFromObjectToCSV(CsvEntityContext context, BeanWrapper object, Map<String, Object> csvValues) {
      csvValues.put(_csvFieldName, object.getPropertyValue(_objFieldName));
    }

    public void translateFromCSVToObject(CsvEntityContext context, Map<String, Object> csvValues, BeanWrapper object) {

      if (isMissingAndOptional(csvValues))
        return;

      String agencyId = (String) csvValues.get(_csvFieldName);

      GtfsReaderContext ctx = (GtfsReaderContext) context.get(GtfsReader.KEY_CONTEXT);
      agencyId = ctx.getTranslatedAgencyId(agencyId);

      object.setPropertyValue(_objFieldName, agencyId);
    }

  }
}
