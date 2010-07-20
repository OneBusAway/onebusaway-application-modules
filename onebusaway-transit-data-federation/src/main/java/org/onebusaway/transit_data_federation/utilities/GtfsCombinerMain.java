package org.onebusaway.transit_data_federation.utilities;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.onebusaway.container.ContainerLibrary;
import org.onebusaway.gtfs.csv.schema.DefaultEntitySchemaFactory;
import org.onebusaway.gtfs.impl.GtfsDaoImpl;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.serialization.GtfsEntitySchemaFactory;
import org.onebusaway.gtfs.serialization.GtfsWriter;
import org.onebusaway.transit_data_federation.impl.offline.GtfsReadingSupport;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Utility for combining multiple GTFS feeds into one unified feed
 * 
 * @author bdferris
 */
public class GtfsCombinerMain {

  public static void main(String[] args) throws IOException {

    if (args.length != 2) {
      System.err.println("usage: bundle.xml output_path");
      System.exit(-1);
    }

    List<String> paths = new ArrayList<String>();

    paths.add(args[0]);

    ConfigurableApplicationContext context = ContainerLibrary.createContext(paths);
    GtfsDaoImpl store = new GtfsDaoImpl();

    DefaultEntitySchemaFactory schema = GtfsEntitySchemaFactory.createEntitySchemaFactory();
    GenericAdditionalFieldMapping.addGenericFieldMapping(schema, Stop.class,
        "stop_direction", "stopDirection");
    GenericAdditionalFieldMapping.addGenericFieldMapping(schema, Trip.class,
        "block_sequence_id", "blockSequenceId");

    GtfsReadingSupport.readGtfsIntoStore(context, store,schema);

    writeGtfs(new File(args[1]), store, schema);
  }

  private static void writeGtfs(File outputDirectory, GtfsDaoImpl store,
      DefaultEntitySchemaFactory schema) throws IOException {
    
    GtfsWriter writer = new GtfsWriter();
    writer.setOutputLocation(outputDirectory);
    writer.setEntitySchemaFactory(schema);
    writer.run(store);
  }
}
