package org.onebusaway.admin.service.api;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.onebusaway.admin.service.server.WmataRssServiceAlertsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.transit.realtime.GtfsRealtime.FeedMessage;

@Path("/alerts-from-rss/")
@Component
public class AlertsFromRssResource {

  private static Logger _log = LoggerFactory.getLogger(AlertsFromRssResource.class);
  
  @Autowired
  private WmataRssServiceAlertsService _alertsService;
  
  
  @Path("/list")
  @GET
  @Produces("application/x-protobuff") 
  public Response getAll() {
    
    Response response = Response.ok(_alertsService.getServlceAlertFeed()).build();
    return response;
  }
  
  
  @Path("/list-debug")
  @GET
  @Produces("text/plain")
  public Response getAllDebug() {
    FeedMessage serviceAlertFeed = _alertsService.getServlceAlertFeed();
    if (serviceAlertFeed == null) {
      _log.info("empty feed");
      return Response.ok().build();
    }
    Response response = Response.ok(serviceAlertFeed.toString()).build();
    return response;
  }

}
