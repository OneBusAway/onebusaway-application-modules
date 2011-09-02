package org.onebusaway.transit_data_federation_webapp.controllers.gtfs_realtime;

import java.io.IOException;
import java.io.OutputStream;

import org.onebusaway.transit_data_federation.impl.realtime.gtfs_realtime.GtfsRealtimeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.google.transit.realtime.GtfsRealtime.FeedMessage;

@Controller
public class GtfsRealtimeController {

  private GtfsRealtimeService _gtfsRealtimeService;

  @Autowired
  public void setGtfsRealtimeService(GtfsRealtimeService gtfsRealtimeService) {
    _gtfsRealtimeService = gtfsRealtimeService;
  }

  @RequestMapping(value = "/gtfs-realtime/trip-updates.action")
  public void tripUpdates(OutputStream out) throws IOException {
    FeedMessage tripUpdates = _gtfsRealtimeService.getTripUpdates();
    tripUpdates.writeTo(out);
  }

  @RequestMapping(value = "/gtfs-realtime/vehicle-positions.action")
  public void vehiclePositions(OutputStream out) throws IOException {
    FeedMessage vehiclePositions = _gtfsRealtimeService.getVehiclePositions();
    vehiclePositions.writeTo(out);
  }

  @RequestMapping(value = "/gtfs-realtime/alerts.action")
  public void alerts(OutputStream out) throws IOException {
    FeedMessage alerts = _gtfsRealtimeService.getAlerts();
    alerts.writeTo(out);
  }
}