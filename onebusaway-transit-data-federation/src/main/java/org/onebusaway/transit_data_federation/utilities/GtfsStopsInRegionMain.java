package org.onebusaway.transit_data_federation.utilities;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.gtfs.csv.EntityHandler;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.serialization.GtfsReader;

public class GtfsStopsInRegionMain {

  @SuppressWarnings("unchecked")
  public static void main(String[] args) throws IOException {

    if (args.length != 5) {
      System.err.println("usage: gtfs_path lat1 lon1 lat2 lon2");
      System.exit(-1);
    }

    double lat1 = Double.parseDouble(args[1]);
    double lon1 = Double.parseDouble(args[2]);
    double lat2 = Double.parseDouble(args[3]);
    double lon2 = Double.parseDouble(args[4]);

    CoordinateBounds bounds = new CoordinateBounds(lat1, lon1, lat2, lon2);

    GtfsReader reader = new GtfsReader();
    reader.setDefaultAgencyId("1");
    reader.getEntityClasses().retainAll(Arrays.asList(Stop.class));
    reader.setInputLocation(new File(args[0]));
    reader.addEntityHandler(new EntityHandlerImpl(bounds));
    reader.run();
  }

  private static class EntityHandlerImpl implements EntityHandler {

    private CoordinateBounds _bounds;

    public EntityHandlerImpl(CoordinateBounds bounds) {
      _bounds = bounds;
    }

    @Override
    public void handleEntity(Object bean) {

      Stop stop = (Stop) bean;

      if (_bounds.contains(stop.getLat(), stop.getLon()))
        System.out.println(stop.getLat() + " " + stop.getLon() + " "
            + stop.getId());
    }

  }
}
