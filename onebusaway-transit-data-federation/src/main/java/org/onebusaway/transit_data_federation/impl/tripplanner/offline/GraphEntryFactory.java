package org.onebusaway.transit_data_federation.impl.tripplanner.offline;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.impl.tripplanner.DistanceLibrary;
import org.onebusaway.transit_data_federation.services.tripplanner.StopEntry;

import edu.washington.cs.rse.geospatial.latlon.CoordinatePoint;

public class GraphEntryFactory {

  public StopEntry createStopEntry(AgencyAndId stopId, double lat, double lon,
      StopEntry... transfers) {

    StopEntryImpl entry = new StopEntryImpl(stopId, lat, lon, new StopTimeIndexImpl());
    CoordinatePoint stopLocation = entry.getStopLocation();

    for (StopEntry transfer : transfers) {
      CoordinatePoint transferStopLocation = transfer.getStopLocation();
      double distance = DistanceLibrary.distance(transferStopLocation,
          stopLocation);
      entry.addTransfer(transfer, distance);
    }
    return entry;
  }
}
