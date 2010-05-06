package org.onebusaway.gtfs.examples;

import org.onebusaway.gtfs.csv.EntityHandler;
import org.onebusaway.gtfs.impl.GtfsDaoImpl;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.serialization.GtfsReader;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class GtfsReaderExampleMain {

  public static void main(String[] args) throws IOException {

    if (args.length != 1) {
      System.err.println("usage: gtfsPath");
      System.exit(-1);
    }

    GtfsReader reader = new GtfsReader();
    reader.setInputLocation(new File(args[0]));

    /**
     * You can register an entity handler that listens for new objects as they
     * are read
     */
    reader.addEntityHandler(new GtfsEntityHandler());

    /**
     * Or you can use the internal entity store, which has references to all the
     * loaded entities
     */
    GtfsDaoImpl store = new GtfsDaoImpl();
    reader.setEntityStore(store);
    
    reader.run();

    // Access entities through the store
    Map<AgencyAndId, Route> routesById = store.getEntitiesByIdForEntityType(
        AgencyAndId.class, Route.class);

    for (Route route : routesById.values()) {
      System.out.println("route: " + route.getShortName());
    }
  }

  private static class GtfsEntityHandler implements EntityHandler {

    public void handleEntity(Object bean) {
      if (bean instanceof Stop) {
        Stop stop = (Stop) bean;
        System.out.println("stop: " + stop.getName());
      }
    }
  }
}
