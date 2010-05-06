package org.onebusaway.gtfs.serialization.mappings;

import org.onebusaway.gtfs.csv.CsvEntityContext;
import org.onebusaway.gtfs.csv.schema.AbstractFieldMapping;
import org.onebusaway.gtfs.csv.schema.BeanWrapper;
import org.onebusaway.gtfs.csv.schema.EntitySchemaFactory;
import org.onebusaway.gtfs.csv.schema.FieldMapping;
import org.onebusaway.gtfs.csv.schema.FieldMappingFactory;
import org.onebusaway.gtfs.model.Agency;
import org.onebusaway.gtfs.serialization.GtfsReader;
import org.onebusaway.gtfs.serialization.GtfsReaderContext;

import java.util.Map;

public class RouteAgencyFieldMappingFactory implements FieldMappingFactory {

  public FieldMapping createFieldMapping(EntitySchemaFactory schemaFactory, String csvFieldName, String objFieldName,
      Class<?> objFieldType, boolean required) {

    return new RouteAgencyFieldMapping(csvFieldName, objFieldName, Agency.class, required);
  }

  private class RouteAgencyFieldMapping extends AbstractFieldMapping {

    public RouteAgencyFieldMapping(String csvFieldName, String objFieldName, Class<?> objFieldType, boolean required) {
      super(csvFieldName, objFieldName, required);
    }

    public void translateFromCSVToObject(CsvEntityContext context, Map<String, Object> csvValues, BeanWrapper object) {

      GtfsReaderContext ctx = (GtfsReaderContext) context.get(GtfsReader.KEY_CONTEXT);
      String agencyId = (String) csvValues.get(_csvFieldName);
      
      if (isMissing(csvValues))
        agencyId = ctx.getDefaultAgencyId();
      
      agencyId = ctx.getTranslatedAgencyId(agencyId);
      
      Agency agency = null;

      for (Agency testAgency : ctx.getAgencies()) {
        if (testAgency.getId().equals(agencyId)) {
          agency = testAgency;
          break;
        }
      }
      if (agency == null)
        throw new IllegalStateException("no agency found for route");

      object.setPropertyValue(_objFieldName,agency);
    }

    public void translateFromObjectToCSV(CsvEntityContext context, BeanWrapper object, Map<String, Object> csvValues) {

      Agency agency = (Agency) object.getPropertyValue(_objFieldName);

      if (isOptional() && agency == null)
        return;

      csvValues.put(_csvFieldName, agency.getId());
    }

  }
}
