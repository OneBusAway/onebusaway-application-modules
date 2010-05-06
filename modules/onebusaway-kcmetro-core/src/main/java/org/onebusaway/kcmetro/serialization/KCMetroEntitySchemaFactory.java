package org.onebusaway.kcmetro.serialization;

import org.onebusaway.gtfs.csv.schema.DefaultEntitySchemaFactory;
import org.onebusaway.gtfs.csv.schema.EntitySchemaFactoryHelper;
import org.onebusaway.gtfs.csv.schema.beans.CsvEntityMappingBean;
import org.onebusaway.gtfs.serialization.mappings.AgencyIdFieldMappingFactory;
import org.onebusaway.kcmetro.model.TimepointToStopMapping;

public class KCMetroEntitySchemaFactory {

  public static DefaultEntitySchemaFactory createEntitySchemaFactory() {
    
    DefaultEntitySchemaFactory factory = new DefaultEntitySchemaFactory();
    EntitySchemaFactoryHelper helper = new EntitySchemaFactoryHelper(factory);

    CsvEntityMappingBean timepoint = helper.addEntity(TimepointToStopMapping.class, "timepoints_to_stops.txt");
    helper.addOptionalField(timepoint, "id");
    helper.addField(timepoint,"trackerTripId",new AgencyIdFieldMappingFactory());
    helper.addField(timepoint,"tripId",new AgencyIdFieldMappingFactory());
    helper.addField(timepoint,"serviceId",new AgencyIdFieldMappingFactory());
    helper.addField(timepoint,"timepointId",new AgencyIdFieldMappingFactory());
    helper.addField(timepoint,"stopId",new AgencyIdFieldMappingFactory());
    helper.addField(timepoint,"time");
    
    return factory;
  }
}
