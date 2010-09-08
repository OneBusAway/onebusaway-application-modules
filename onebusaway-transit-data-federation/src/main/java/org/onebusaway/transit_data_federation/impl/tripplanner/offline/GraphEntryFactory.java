package org.onebusaway.transit_data_federation.impl.tripplanner.offline;

import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.geospatial.services.SphericalGeometryLibrary;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.services.tripplanner.StopEntry;

public class GraphEntryFactory {

  public StopEntry createStopEntry(AgencyAndId stopId, double lat, double lon,
      StopEntry... transfers) {

    StopEntryImpl entry = new StopEntryImpl(stopId, lat, lon,
        new StopTimeIndexImpl());
    CoordinatePoint stopLocation = entry.getStopLocation();

    for (StopEntry transfer : transfers) {
      CoordinatePoint transferStopLocation = transfer.getStopLocation();
      double distance = SphericalGeometryLibrary.distance(transferStopLocation,
          stopLocation);
      entry.addTransfer(transfer, distance);
    }
    return entry;
  }
}
