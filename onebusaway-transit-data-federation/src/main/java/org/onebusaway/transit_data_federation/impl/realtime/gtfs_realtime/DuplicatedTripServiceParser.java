package org.onebusaway.transit_data_federation.impl.realtime.gtfs_realtime;

import com.google.transit.realtime.GtfsRealtime;

public interface DuplicatedTripServiceParser {
    AddedTripInfo parse(GtfsRealtime.TripUpdate tu);

  void setGtfsRealtimeEntitySource(GtfsRealtimeEntitySource entitySource);
}
