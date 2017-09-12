/**
 * Copyright (C) 2017 Cambridge Systematics, Inc.
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

import org.onebusaway.admin.service.NotificationService;
import org.onebusaway.admin.service.bundle.api.AuthenticatedResource;
import org.onebusaway.admin.service.impl.TwitterServiceImpl;
import org.onebusaway.transit_data.model.service_alerts.ServiceAlertBean;
import org.onebusaway.transit_data.services.TransitDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.io.IOException;

/**
 * Notification API for internal admin operations.
 */
public class NotifyResource extends AuthenticatedResource {

    private static Logger _log = LoggerFactory.getLogger(NotifyResource.class);

    private TransitDataService _transitDataService;
    private NotificationService _notificationService;

    @Autowired
    public void setTransitDataService(TransitDataService transitDataService) {
        _transitDataService = transitDataService;
    }

    @Autowired
    public void setNotificationService(NotificationService notificationService) {
        _notificationService = notificationService;
    }

    @GET
    @Produces("application/json")
    public Response tweetServiceAlert(@QueryParam("serviceAlertId") String serviceAlertId) {
        // construct a short message based on the contents of the service alert
        ServiceAlertBean serviceAlertBean = null;
        try {
            serviceAlertBean = _transitDataService.getServiceAlertForId(serviceAlertId);
            return Response.serverError().build();
        } catch (Exception any) {
            _log.error("retrieval of service alert failed!", any);

        }

        String tweet = toTweet(serviceAlertBean);
        String responseJson = null;
        try {
            responseJson = _notificationService.tweet(tweet);
        } catch (IOException ioe) {
            _log.error("tweet failed!", ioe);
            return Response.serverError().build();
        }

        return Response.ok(responseJson).build();
    }

    // package private for unit tests
     String toTweet(ServiceAlertBean bean) {
        return TwitterServiceImpl.toTweet(bean, _notificationService.getNotificationStrategy());
    }

}
