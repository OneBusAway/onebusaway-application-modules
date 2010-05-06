package org.onebusaway.gtdf.serialization;


import com.vividsolutions.jts.geom.Point;

import org.onebusaway.common.services.ProjectionService;
import org.onebusaway.csv.AbstractFieldMapping;
import org.onebusaway.csv.CsvEntityContext;
import org.onebusaway.csv.EntitySchemaFactory;
import org.onebusaway.csv.FieldMapping;
import org.onebusaway.csv.FieldMappingFactory;
import org.springframework.beans.BeanWrapper;

import java.util.Map;

public class LocationFieldMappingFactory implements FieldMappingFactory {

  public static final String PROJECTION_KEY = LocationFieldMappingFactory.class.getName()
      + ".projection";

  public FieldMapping createFieldMapping(EntitySchemaFactory schemaFactory,
      String csvFieldName, String objFieldName, Class<?> objFieldType,
      boolean required) {
    return new FieldMappingImpl(csvFieldName, objFieldName);
  }

  private static class FieldMappingImpl extends AbstractFieldMapping {

    public FieldMappingImpl(String csvFieldName, String objFieldName) {
      super(csvFieldName, objFieldName, true);
    }

    public void translateFromCSVToObject(CsvEntityContext context,
        Map<String, Object> csvValues, BeanWrapper object) {

      ProjectionService projection = (ProjectionService) context.get(PROJECTION_KEY);
      Double lat = (Double) object.getPropertyValue("lat");
      Double lon = (Double) object.getPropertyValue("lon");
      Point location = projection.getLatLonAsPoint(lat, lon);
      object.setPropertyValue(_objFieldName, location);
    }

    public void translateFromObjectToCSV(CsvEntityContext context,
        BeanWrapper object, Map<String, Object> csvValues) {
      // Do nothing
    }
  }

}
