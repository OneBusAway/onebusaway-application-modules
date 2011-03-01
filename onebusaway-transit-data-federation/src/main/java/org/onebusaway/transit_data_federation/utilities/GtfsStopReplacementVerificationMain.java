package org.onebusaway.transit_data_federation.utilities;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.onebusaway.csv_entities.EntityHandler;
import org.onebusaway.gtfs.model.Agency;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.serialization.GtfsReader;
import org.onebusaway.transit_data_federation.bundle.tasks.EntityReplacementStrategyFactory;
import org.onebusaway.transit_data_federation.services.AgencyAndIdLibrary;

/**
 * Given a stop-consolidation list, verifies that the specified stops still
 * exist in the target GTFS feeds.
 * 
 * @author bdferris
 * @see EntityReplacementStrategyFactory
 */
public class GtfsStopReplacementVerificationMain {

  @SuppressWarnings("unchecked")
  public static void main(String[] args) throws IOException {

    if (args.length == 0) {
      System.err.println("usage: stop-consolidation-file gtfs_path agencyId [gtfs_path agencyId ...]");
      System.exit(-1);
    }

    EntityHandlerImpl handler = new EntityHandlerImpl();

    for (int i = 1; i < args.length; i += 2) {

      GtfsReader reader = new GtfsReader();
      reader.setDefaultAgencyId(args[i + 1]);
      reader.setInputLocation(new File(args[i]));
      reader.getEntityClasses().retainAll(
          Arrays.asList(Agency.class, Stop.class));
      reader.addEntityHandler(handler);
      reader.run();
    }

    Set<AgencyAndId> ids = handler.getIds();

    BufferedReader reader = new BufferedReader(new FileReader(args[0]));
    String line = null;

    while ((line = reader.readLine()) != null) {
      if (line.startsWith("#") || line.startsWith("{{{") || line.startsWith("}}}") || line.length() == 0)
        continue;
      String[] tokens = line.split("\\s+");
      AgencyAndId id = AgencyAndIdLibrary.convertFromString(tokens[0]);
      if (!ids.contains(id))
        System.out.println(id + " <- " + line);
    }

  }

  private static class EntityHandlerImpl implements EntityHandler {

    private Set<AgencyAndId> _ids = new HashSet<AgencyAndId>();

    public Set<AgencyAndId> getIds() {
      return _ids;
    }

    @Override
    public void handleEntity(Object bean) {
      if (!(bean instanceof Stop))
        return;
      Stop stop = (Stop) bean;
      _ids.add(stop.getId());
    }

  }
}
