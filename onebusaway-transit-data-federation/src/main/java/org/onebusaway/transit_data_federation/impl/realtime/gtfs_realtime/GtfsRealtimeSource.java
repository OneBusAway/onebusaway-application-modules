package org.onebusaway.transit_data_federation.impl.realtime.gtfs_realtime;

import java.net.URL;
import java.util.concurrent.ScheduledExecutorService;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class GtfsRealtimeSource {

  private static final Logger _log = LoggerFactory.getLogger(GtfsRealtimeSource.class);

  private ScheduledExecutorService _scheduledExecutorService;

  private URL _tripUpdatesUrl;

  private URL _vehiclePositionsUrl;

  private int _refreshInterval;

  public void setTripUpdatesUrl(URL tripUpdatesUrl) {
    _tripUpdatesUrl = tripUpdatesUrl;
  }

  public void setVehiclePositionsUrl(URL vehiclePositionsUrl) {
    _vehiclePositionsUrl = vehiclePositionsUrl;
  }

  public void setRefeshInterval(int refreshInterval) {
    _refreshInterval = refreshInterval;
  }

  @Autowired
  public void setScheduledExecutorService(
      ScheduledExecutorService scheduledExecutorService) {
    _scheduledExecutorService = scheduledExecutorService;
  }

  @PostConstruct
  public void start() {

  }

  /****
   *
   ****/

  private class RefreshTask implements Runnable {

    @Override
    public void run() {

    }
  }
}
