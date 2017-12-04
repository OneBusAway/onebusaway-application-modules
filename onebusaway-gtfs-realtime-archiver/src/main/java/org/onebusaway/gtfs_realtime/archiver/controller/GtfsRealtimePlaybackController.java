/**
 * Copyright (C) 2016 Cambridge Systematics, Inc.
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
package org.onebusaway.gtfs_realtime.archiver.controller;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.onebusaway.gtfs_realtime.archiver.service.GtfsRealtimeRetriever;
import org.onebusaway.gtfs_realtime.archiver.service.GtfsRealtimeRetriever.EntityType;
import org.onebusaway.gtfs_realtime.archiver.service.TimeService;
import org.onebusaway.users.services.ApiKeyPermissionService;
import org.onebusaway.users.services.ApiKeyPermissionService.Status;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.transit.realtime.GtfsRealtime.FeedMessage;

@Controller
public class GtfsRealtimePlaybackController {

  private static final int TOO_MANY_REQUESTS = 429;

  @Autowired
  private GtfsRealtimeRetriever _gtfsRealtimeRetriever;

  @Autowired
  private TimeService _timeService;
  
  @Autowired
  private ApiKeyPermissionService _keyService;
  
  private static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
  
  
  @RequestMapping(value = "/gtfs-realtime/{path:trip-updates|vehicle-positions}")
  public void tripUpdates(ServletRequest request, HttpServletResponse response,
      @RequestParam(value = "key", required = true) String key,
      @RequestParam(value = "timestamp", required = false) Long timestampInSeconds,
      @RequestParam(value = "time", required = false) String simpleDate,
      @RequestParam(value = "interval", required = false, defaultValue = "30") long interval,
      @PathVariable String path)
          throws IOException {
    
    Status status = isAllowed(key);
    
    if(Status.RATE_EXCEEDED == status) {
      response.sendError(TOO_MANY_REQUESTS);
      return;
    }
    
    if(Status.AUTHORIZED != status) {
      response.sendError(HttpServletResponse.SC_FORBIDDEN);
      return;
    }
    
    if (simpleDate != null) {
      Date parsed;
      try {
        parsed = DATE_FORMAT.parse(simpleDate);
        timestampInSeconds = parsed.getTime() / 1000;
      } catch (ParseException e) {
        // bury
      }
    }
    
    if (timestampInSeconds == null) {
      response.sendError(HttpServletResponse.SC_PRECONDITION_FAILED, "time or timestamp parameters required");
      return;
    }
    
    EntityType type  = path.equals("trip-updates") ? EntityType.TRIP : EntityType.VEHICLE;

    // will not create new session if time is the same
    Date requestedDate = new Date(timestampInSeconds * 1000);
    _timeService.setCurrentTime(key, requestedDate);
    
    Date endDate = _timeService.getCurrentTime(key);
    Date startDate = new Date((endDate.getTime() - (interval * 1000))); 
        
    FeedMessage tripUpdates = _gtfsRealtimeRetriever.getFeedMessage(type, startDate, endDate);
    render(request, response, tripUpdates);
  }
  
  @RequestMapping(value = "/gtfs-realtime/clear")
  public @ResponseBody String clear(HttpServletResponse response,
      @RequestParam(value = "key", required = true) String key)
          throws IOException {
    
    Status status = isAllowed(key);
    if(Status.AUTHORIZED == status) {
      _timeService.clear(key);
      return "SUCCESS\n";
    }
    
    if (Status.RATE_EXCEEDED == status) {
      response.sendError(TOO_MANY_REQUESTS);
      return "rate limit exceeded";
    }
    
    response.sendError(HttpServletResponse.SC_FORBIDDEN);
    return "permission denied";
    
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
  
  
  private Status isAllowed(String key) {
    return _keyService.getPermission(key, "api");
  }
}
