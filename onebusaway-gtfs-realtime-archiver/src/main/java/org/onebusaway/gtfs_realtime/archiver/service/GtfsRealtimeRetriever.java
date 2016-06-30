package org.onebusaway.gtfs_realtime.archiver.service;

import java.util.Date;

import com.google.transit.realtime.GtfsRealtime.FeedMessage;

public interface GtfsRealtimeRetriever {
  public FeedMessage getTripUpdates(Date startDate, Date endDate);
  public FeedMessage getVehiclePositions(Date startDate, Date endDate);
}
