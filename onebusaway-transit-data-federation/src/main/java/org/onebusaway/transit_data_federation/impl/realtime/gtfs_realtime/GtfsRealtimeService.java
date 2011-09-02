package org.onebusaway.transit_data_federation.impl.realtime.gtfs_realtime;

import com.google.transit.realtime.GtfsRealtime.FeedMessage;

public interface GtfsRealtimeService {

  public FeedMessage getTripUpdates();

  public FeedMessage getVehiclePositions();

  public FeedMessage getAlerts();
}
