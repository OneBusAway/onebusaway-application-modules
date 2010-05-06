package org.onebusaway.gtfs.serialization.mappings;

import org.onebusaway.gtfs.csv.CsvEntityContext;
import org.onebusaway.gtfs.csv.schema.BeanWrapper;
import org.onebusaway.gtfs.csv.schema.BeanWrapperFactory;
import org.onebusaway.gtfs.csv.schema.DefaultFieldMapping;
import org.onebusaway.gtfs.csv.schema.EntitySchemaFactory;
import org.onebusaway.gtfs.csv.schema.FieldMapping;
import org.onebusaway.gtfs.csv.schema.FieldMappingFactory;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.serialization.GtfsReader;
import org.onebusaway.gtfs.serialization.GtfsReaderContext;

import java.util.Map;

public class DefaultAgencyIdFieldMappingFactory implements FieldMappingFactory {

  private String _agencyIdPath = null;

  public DefaultAgencyIdFieldMappingFactory() {
    this(null);
  }

  public DefaultAgencyIdFieldMappingFactory(String agencyIdPath) {
    _agencyIdPath = agencyIdPath;
  }

  public FieldMapping createFieldMapping(EntitySchemaFactory schemaFactory, String csvFieldName, String objFieldName,
      Class<?> objFieldType, boolean required) {

    return new FieldMappingImpl(csvFieldName, objFieldName, String.class, required);
  }

  private class FieldMappingImpl extends DefaultFieldMapping {

    public FieldMappingImpl(String csvFieldName, String objFieldName, Class<?> objFieldType, boolean required) {
      super(csvFieldName, objFieldName, objFieldType, required);
    }

    @Override
    public void translateFromObjectToCSV(CsvEntityContext context, BeanWrapper object, Map<String, Object> csvValues) {
      AgencyAndId id = (AgencyAndId) object.getPropertyValue(_objFieldName);
      csvValues.put(_csvFieldName, id.getId());
    }

    @Override
    public void translateFromCSVToObject(CsvEntityContext context, Map<String, Object> csvValues, BeanWrapper object) {

      String agencyId = resolveAgencyId(context,object);

      String id = (String) csvValues.get(_csvFieldName);
      AgencyAndId agencyAndId = new AgencyAndId(agencyId, id);
      object.setPropertyValue(_objFieldName, agencyAndId);
    }

    private String resolveAgencyId(CsvEntityContext context, BeanWrapper object) {

      if (_agencyIdPath == null) {
        GtfsReaderContext ctx = (GtfsReaderContext) context.get(GtfsReader.KEY_CONTEXT);
        return ctx.getDefaultAgencyId();
      }
      
      for( String property : _agencyIdPath.split("\\.") ) {
        Object value = object.getPropertyValue(property);
        object = BeanWrapperFactory.wrap(value);
      }
      
      return object.getWrappedInstance(Object.class).toString();
    }
  }
}
