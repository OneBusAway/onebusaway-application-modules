package org.onebusaway.gtdf.serialization;

import org.onebusaway.csv.CsvEntityContext;
import org.onebusaway.csv.EntitySchemaFactory;
import org.onebusaway.csv.FieldMapping;
import org.onebusaway.csv.FieldMappingFactory;
import org.onebusaway.gtdf.model.Agency;
import org.springframework.beans.BeanWrapper;

import java.util.List;
import java.util.Map;

public class RouteAgencyFieldMappingFactory implements FieldMappingFactory {

  public FieldMapping createFieldMapping(EntitySchemaFactory schemaFactory,
      String csvFieldName, String objFieldName, Class<?> objFieldType,
      boolean required) {

    return new RouteAgencyFieldMapping(csvFieldName, objFieldName,
        Agency.class, required);
  }

  private class RouteAgencyFieldMapping extends
      EntityFieldMappingFactory.FieldMappingImpl {

    public RouteAgencyFieldMapping(String csvFieldName, String objFieldName,
        Class<?> objFieldType, boolean required) {
      super(csvFieldName, objFieldName, objFieldType, required);
    }

    @Override
    public void translateFromCSVToObject(CsvEntityContext context,
        Map<String, Object> csvValues, BeanWrapper object) {

      if (isMissing(csvValues)) {

        GTDFReaderContext ctx = (GTDFReaderContext) context.get(GTDFReader.KEY_CONTEXT);

        List<Agency> agencies = ctx.getAgencies();

        if (agencies.size() == 0)
          throw new IllegalStateException(
              "no default agency specified for route, but no agencies have been loaded");

        if (agencies.size() > 1)
          throw new IllegalStateException(
              "no default agency specified for route, but multiple agencies have been loaded");

        // Put in the default agency id
        Agency agency = agencies.get(0);
        csvValues.put(_csvFieldName, agency.getId());
      }

      super.translateFromCSVToObject(context, csvValues, object);
    }
  }
}
