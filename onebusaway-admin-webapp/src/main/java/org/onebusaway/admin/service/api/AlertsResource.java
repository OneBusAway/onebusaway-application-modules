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

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import com.google.transit.realtime.GtfsRealtime;
import org.onebusaway.admin.service.server.ConsoleServiceAlertsService;
import org.onebusaway.alerts.service.ServiceAlerts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.OutputStream;

@Component
@Path("/alerts")
/**
 * expose service alerts from admin console as a GTFS-RT alerts feed.
 */
public class AlertsResource {
    private static Logger _log = LoggerFactory.getLogger(AlertsResource.class);

    @Autowired
    private ConsoleServiceAlertsService _alerts;


    @Path("/gtfsrt/{agencyId}.pb")
    @GET
    @Produces("application/x-google-protobuf")
    public Response get(@PathParam("agencyId") String agencyId) {
        GtfsRealtime.FeedMessage feed = _alerts.getAlerts(agencyId);
        if (feed == null) {
            return Response.ok().build();
        }
        StreamingOutput stream = new StreamingOutput() {
            @Override
            public void write(OutputStream os) throws IOException, WebApplicationException {
                // Service Alerts know how to write themselves as protocol buffers
                feed.writeTo(os);
            }
        };
        Response response = Response.ok(stream).build();
        return response;

    }

    @Path("/service/{agencyId}.pb")
    @GET
    @Produces("application/x-google-protobuf")
    public Response gets(@PathParam("agencyId") String agencyId) {
        ServiceAlerts.ServiceAlertsCollection alertsCollection = _alerts.getActiveAlertsCollection(agencyId);
        if (alertsCollection == null) {
            return Response.ok().build();
        }
        StreamingOutput stream = new StreamingOutput() {
            @Override
            public void write(OutputStream os) throws IOException, WebApplicationException {
                // Service Alerts know how to write themselves as protocol buffers
                alertsCollection.writeTo(os);
            }
        };
        Response response = Response.ok(stream).build();
        return response;

    }


    @GET
    @Path("/gtfsrt/{agencyId}.pbtext")
    @Produces("text/plain")
    public Response getText(@PathParam("agencyId") String agencyId) {
        GtfsRealtime.FeedMessage feed = _alerts.getActiveAlerts(agencyId);
        Response response = Response.ok(feed.toString()).build();
        return response;
    }

    @GET
    @Path("/service/{agencyId}.pbtext")
    @Produces("text/plain")
    public Response getTexts(@PathParam("agencyId") String agencyId) {
        ServiceAlerts.ServiceAlertsCollection alertsCollection = _alerts.getActiveAlertsCollection(agencyId);
        Response response = Response.ok(alertsCollection.toString()).build();
        return response;
    }

}
