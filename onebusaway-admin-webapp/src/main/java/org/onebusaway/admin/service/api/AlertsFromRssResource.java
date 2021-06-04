/**
 * Copyright (C) 2015 Cambridge Systematics, Inc.
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
package org.onebusaway.admin.service.api;

import java.io.IOException;
import java.io.OutputStream;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.onebusaway.admin.service.server.IntegratingServiceAlertsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.google.transit.realtime.GtfsRealtime.FeedMessage;

@Path("/alerts-from-rss")
@Component
public class AlertsFromRssResource {

  private static Logger _log = LoggerFactory.getLogger(AlertsFromRssResource.class);
  
  @Autowired
  @Qualifier("rssServiceAlertsService")
  private IntegratingServiceAlertsService _alertsService;
  
  
  @Path("/service-alerts")
  @GET
  @Produces("application/x-google-protobuff") 
  public Response getAll() {
    final FeedMessage serviceAlertFeed = _alertsService.getServiceAlertFeed();
    if (serviceAlertFeed == null) {
      return Response.ok().build();
    }
    StreamingOutput stream = new StreamingOutput() {
      @Override
      public void write(OutputStream os) throws IOException, WebApplicationException {
        // Service Alerts know how to write themselves as protocol buffers
        serviceAlertFeed.writeTo(os);
      }
    };
    Response response = Response.ok(stream).build();
    return response;
  }
  
  
  @Path("/service-alerts-debug")
  @GET
  @Produces("text/plain")
  public Response getAllDebug() {
    FeedMessage serviceAlertFeed = _alertsService.getServiceAlertFeed();
    if (serviceAlertFeed == null) {
      _log.info("empty feed");
      return Response.ok().build();
    }
    // return the string representation of the protocol buffers
    Response response = Response.ok(serviceAlertFeed.toString()).build();
    return response;
  }

}
