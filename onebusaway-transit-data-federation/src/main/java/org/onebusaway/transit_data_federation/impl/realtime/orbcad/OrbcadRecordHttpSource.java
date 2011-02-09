package org.onebusaway.transit_data_federation.impl.realtime.orbcad;

import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.onebusaway.gtfs.csv.CsvEntityContext;
import org.onebusaway.gtfs.csv.CsvEntityReader;
import org.onebusaway.gtfs.csv.schema.AbstractFieldMapping;
import org.onebusaway.gtfs.csv.schema.BeanWrapper;
import org.onebusaway.gtfs.csv.schema.DefaultEntitySchemaFactory;
import org.onebusaway.gtfs.csv.schema.EntitySchemaFactoryHelper;
import org.onebusaway.gtfs.csv.schema.beans.CsvEntityMappingBean;
import org.onebusaway.transit_data_federation.services.AgencyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jmx.export.annotation.ManagedResource;

@ManagedResource("org.onebusaway.transit_data_federation.impl.realtime.orbcad:name=OrbcadRecordHttpSource")
public class OrbcadRecordHttpSource extends AbstractOrbcadRecordSource {

  private static Logger _log = LoggerFactory.getLogger(OrbcadRecordHttpSource.class);

  private CsvEntityReader _reader;

  private String _url;

  private AgencyService _agencyService;

  public void setUrl(String url) {
    _url = url;
  }

  @Autowired
  public void setAgencyService(AgencyService agencyService) {
    _agencyService = agencyService;
  }

  /****
   * Setup and Teardown
   ****/

  @PostConstruct
  public void start() throws SocketException, IOException {

    _log.info("starting orbcad http download client");
    super.start();

  }

  @PreDestroy
  public void stop() throws IOException {
    _log.info("stopping orbcad http download client");
    super.stop();
  }

  /****
   * Protected Methods
   ****/

  protected void setup() {
    
    TimeZone tz = TimeZone.getDefault();
    
    if( _agencyIds != null) {
      for( String agencyId : _agencyIds ) {
        TimeZone agencyTimeZone = _agencyService.getTimeZoneForAgencyId(agencyId);
        if( agencyTimeZone != null) {
          tz = agencyTimeZone;
          break;
        }
      }
    }
    
    _reader = new CsvEntityReader();

    DefaultEntitySchemaFactory factory = new DefaultEntitySchemaFactory();
    EntitySchemaFactoryHelper helper = new EntitySchemaFactoryHelper(factory);

    CsvEntityMappingBean record = helper.addEntity(OrbcadRecord.class);
    record.setAutoGenerateSchema(false);
    record.addAdditionalFieldMapping(new OrbcadRecordTranslatorFieldMapping(tz));

    _reader.setEntitySchemaFactory(factory);

    _reader.addEntityHandler(new RecordHandler());
  }

  @Override
  protected void handleRefresh() throws IOException {

    URL url = new URL(_url);
    InputStream in = url.openStream();

    _reader.readEntities(OrbcadRecord.class, in);
    in.close();
  }

  private static class OrbcadRecordTranslatorFieldMapping extends
      AbstractFieldMapping {

    private DateFormat _format = new SimpleDateFormat(
        "yyyy-MM-dd HH:mm:ss");

    public OrbcadRecordTranslatorFieldMapping(TimeZone timeZone) {
      super(OrbcadRecord.class, "", "", true);
      _format.setTimeZone(timeZone);
    }

    @Override
    public void translateFromCSVToObject(CsvEntityContext context,
        Map<String, Object> csvValues, BeanWrapper object) {

      try {

        OrbcadRecord record = object.getWrappedInstance(OrbcadRecord.class);

        record.setBlock(Integer.parseInt(csvValues.get("block_id").toString()));

        String timeAsString = csvValues.get("incident_date_time").toString();
        Date timeAsDate = _format.parse(timeAsString);
        record.setTime(timeAsDate.getTime() / 1000);

        record.setRouteId(Integer.parseInt(csvValues.get("route_id").toString()));
        record.setVehicleId(Integer.parseInt(csvValues.get("vehicle_id").toString()));

        // Data is in minutes by default, thus the * 60
        String scheduleDeviationAsString = csvValues.get("deviation").toString();
        int scheduleDeviationInSeconds = Integer.parseInt(scheduleDeviationAsString) * 60;
        record.setScheduleDeviation(scheduleDeviationInSeconds);

        String latlon = csvValues.get("tp_sname").toString();
        int index = latlon.indexOf(' ');
        record.setLat(Double.parseDouble(latlon.substring(0, index)));
        record.setLon(Double.parseDouble(latlon.substring(index + 1)));

      } catch (Exception ex) {
        _log.warn("invalid record: " + csvValues);
      }
    }

    @Override
    public void translateFromObjectToCSV(CsvEntityContext context,
        BeanWrapper object, Map<String, Object> csvValues) {
      throw new UnsupportedOperationException();
    }
  }
}
