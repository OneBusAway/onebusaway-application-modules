/**
 * Copyright (C) 2013 Google, Inc.
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
package org.onebusaway.api.actions.api.gtfs_realtime;


import org.apache.struts2.ServletActionContext;
import org.onebusaway.alerts.impl.ServiceAlertBuilderHelper;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data.model.ListBean;
import org.onebusaway.transit_data.model.service_alerts.ServiceAlertBean;

import com.google.transit.realtime.GtfsRealtime.FeedMessage;
import org.onebusaway.transit_data.model.service_alerts.SituationAffectsBean;

import javax.servlet.http.HttpServletResponse;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class AlertsForAgencyAction extends GtfsRealtimeActionSupport {

  private static final long serialVersionUID = 1L;
  private static final SimpleDateFormat _sdf;

  static {
    _sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss 'GMT' ", Locale.US);
    _sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
  }

  @Override
  protected void fillFeedMessage(FeedMessage.Builder feed, String agencyId,
      long timestamp, FILTER_TYPE filterType, String filterValue) {

    long feedTimestamp = feed.getHeader().getTimestamp();
    ListBean<ServiceAlertBean> alerts = _service.getAllServiceAlertsForAgencyId(agencyId);
    if (FILTER_TYPE.ROUTE_ID == filterType) {
      ArrayList<ServiceAlertBean> filteredList = new ArrayList<>();
      for (ServiceAlertBean potentialAlert : alerts.getList()) {
        for (SituationAffectsBean affects : potentialAlert.getAllAffects())
          if (affects.getRouteId() != null) {
            try {
              // try route as qualified
              AgencyAndId andId = AgencyAndId.convertFromString(affects.getRouteId());
              if (filterValue.equals(andId.getId())) {
                filteredList.add(potentialAlert);
              }
            } catch (Exception any) {
              // try it as raw routeId
              if (filterValue.equals(affects.getRouteId())) {
                filteredList.add(potentialAlert);
              }
            }
        }
      }
      ListBean<ServiceAlertBean> filteredListBean = new ListBean<ServiceAlertBean>();
      filteredListBean.setList(filteredList);
      ServiceAlertBuilderHelper.fillFeedMessage(feed, filteredListBean, agencyId, timestamp);
    } else {
      ServiceAlertBuilderHelper.fillFeedMessage(feed, alerts, agencyId, timestamp);
    }

    if(feedTimestamp != 0){
      long lastModifiedMills = feedTimestamp * 1000L;
      String lastModifiedHeader = _sdf.format(new Date(lastModifiedMills));
      HttpServletResponse response = ServletActionContext.getResponse();
      response.setHeader("Last-Modified", lastModifiedHeader);
    } else {
      long lastModifiedMills = timestamp * 1000L;
      String lastModifiedHeader = _sdf.format(new Date(lastModifiedMills));

      HttpServletResponse response = ServletActionContext.getResponse();
      response.setHeader("Last-Modified", lastModifiedHeader);
    }

  }
}
