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
import java.util.Arrays;

import org.onebusaway.csv_entities.EntityHandler;
import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.serialization.GtfsReader;

/**
 * Utility script to compute the set of stops from a gtfs feed that fall within
 * a simple lat-lon rectangular bounding box.
 * 
 * @author bdferris
 * @see GtfsStopsInPolygonMain
 */
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
