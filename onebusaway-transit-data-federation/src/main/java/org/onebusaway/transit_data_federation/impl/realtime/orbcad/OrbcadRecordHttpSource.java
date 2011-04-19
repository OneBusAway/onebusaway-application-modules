package org.onebusaway.transit_data_federation.impl.realtime.orbcad;

import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;
import java.net.URL;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.onebusaway.csv_entities.CsvEntityReader;
import org.onebusaway.csv_entities.schema.AnnotationDrivenEntitySchemaFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jmx.export.annotation.ManagedResource;

@ManagedResource("org.onebusaway.transit_data_federation.impl.realtime.orbcad:name=OrbcadRecordHttpSource")
public class OrbcadRecordHttpSource extends AbstractOrbcadRecordSource {

  private static Logger _log = LoggerFactory.getLogger(OrbcadRecordHttpSource.class);

  private CsvEntityReader _reader;

  private String _url;

  public void setUrl(String url) {
    _url = url;
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

    _reader = new CsvEntityReader();

    AnnotationDrivenEntitySchemaFactory entitySchemaFactory = new AnnotationDrivenEntitySchemaFactory();
    entitySchemaFactory.addEntityClass(OrbcadRecord.class);
    _reader.setEntitySchemaFactory(entitySchemaFactory);

    _reader.addEntityHandler(new RecordHandler());
  }

  @Override
  protected void handleRefresh() throws IOException {

    URL url = new URL(_url);
    InputStream in = url.openStream();

    _reader.readEntities(OrbcadRecord.class, in);
    in.close();
  }
}
