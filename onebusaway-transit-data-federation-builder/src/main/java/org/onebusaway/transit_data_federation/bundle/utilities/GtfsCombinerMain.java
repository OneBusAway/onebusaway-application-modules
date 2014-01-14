/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onebusaway.transit_data_federation.bundle.utilities;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.onebusaway.container.ContainerLibrary;
import org.onebusaway.csv_entities.schema.DefaultEntitySchemaFactory;
import org.onebusaway.gtfs.impl.GtfsDaoImpl;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.serialization.GtfsEntitySchemaFactory;
import org.onebusaway.gtfs.serialization.GtfsWriter;
import org.onebusaway.transit_data_federation.bundle.tasks.GtfsReadingSupport;
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
