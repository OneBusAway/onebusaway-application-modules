package org.onebusaway.gtfs_realtime.archiver.controller;

import java.io.IOException;
import java.util.Date;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.onebusaway.gtfs_realtime.archiver.service.GtfsRealtimeRetriever;
import org.onebusaway.gtfs_realtime.archiver.service.TimeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.transit.realtime.GtfsRealtime.FeedMessage;

@Controller
public class GtfsRealtimePlaybackController {

  @Autowired
  GtfsRealtimeRetriever _gtfsRealtimeRetriever;

  @Autowired
  TimeService timeService;
  
  @RequestMapping(value = "/gtfs-realtime/trip-updates")
  public void tripUpdates(ServletRequest request, HttpServletResponse response,
      @RequestParam(value = "time", required = true) long end,
      @RequestParam(value = "session", required = true) String session,
      @RequestParam(value = "interval", required = false, defaultValue = "30") long interval)
          throws IOException {
    
    checkTimeService(session, new Date(end * 1000));
    
    Date endDate = timeService.getCurrentTime(session);
    Date startDate = new Date((endDate.getTime() - (interval * 1000))); 
        
    FeedMessage tripUpdates = _gtfsRealtimeRetriever.getTripUpdates(startDate, endDate);
    render(request, response, tripUpdates);
  }
  
  @RequestMapping(value = "/gtfs-realtime/vehicle-positions")
  public void vehiclePositions(ServletRequest request, HttpServletResponse response,
      @RequestParam(value = "time", required = true) long end,
      @RequestParam(value = "session", required = true) String session,
      @RequestParam(value = "interval", required = false, defaultValue = "30") long interval)
          throws IOException {
    
    checkTimeService(session, new Date(end * 1000));
    
    Date endDate = timeService.getCurrentTime(session);
    Date startDate = new Date((endDate.getTime() - (interval * 1000))); 
        
    FeedMessage tripUpdates = _gtfsRealtimeRetriever.getVehiclePositions(startDate, endDate);
    render(request, response, tripUpdates);
  }
  
  
  
  @RequestMapping(value = "/gtfs-realtime/clear")
  public @ResponseBody String clear(
      @RequestParam(value = "session", required = true) String session) {
    timeService.clear(session);
    return "SUCCESS\n";
  }

  private void render(ServletRequest request, HttpServletResponse response,
      FeedMessage message) throws IOException {
    if (request.getParameter("debug") != null) {
      response.setContentType("text/plain");
      response.getWriter().write(message.toString());
    } else {
      response.setContentType("application/x-google-protobuf");
      message.writeTo(response.getOutputStream());
    }
  }
  
  private void checkTimeService(String session, Date time) {
    if (!timeService.isTimeSet(session)) {
      timeService.setCurrentTime(session, time);
    }
  }
}
