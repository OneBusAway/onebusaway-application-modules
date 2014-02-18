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

import java.util.List;

import org.onebusaway.transit_data.model.ListBean;
import org.onebusaway.transit_data.model.service_alerts.NaturalLanguageStringBean;
import org.onebusaway.transit_data.model.service_alerts.ServiceAlertBean;
import org.onebusaway.transit_data.model.service_alerts.SituationAffectsBean;
import org.onebusaway.transit_data.model.service_alerts.TimeRangeBean;

import com.google.transit.realtime.GtfsRealtime.Alert;
import com.google.transit.realtime.GtfsRealtime.EntitySelector;
import com.google.transit.realtime.GtfsRealtime.FeedEntity;
import com.google.transit.realtime.GtfsRealtime.FeedMessage;
import com.google.transit.realtime.GtfsRealtime.TimeRange;
import com.google.transit.realtime.GtfsRealtime.TranslatedString;
import com.google.transit.realtime.GtfsRealtime.TranslatedString.Translation;
import com.google.transit.realtime.GtfsRealtime.TripDescriptor;

public class AlertsForAgencyAction extends GtfsRealtimeActionSupport {

  private static final long serialVersionUID = 1L;

  @Override
  protected void fillFeedMessage(FeedMessage.Builder feed, String agencyId,
      long timestamp) {

    ListBean<ServiceAlertBean> alerts = _service.getAllServiceAlertsForAgencyId(agencyId);

    for (ServiceAlertBean serviceAlert : alerts.getList()) {
      FeedEntity.Builder entity = feed.addEntityBuilder();
      entity.setId(Integer.toString(feed.getEntityCount()));
      Alert.Builder alert = entity.getAlertBuilder();

      fillTranslations(serviceAlert.getSummaries(),
          alert.getHeaderTextBuilder());
      fillTranslations(serviceAlert.getDescriptions(),
          alert.getDescriptionTextBuilder());

      if (serviceAlert.getActiveWindows() != null) {
        for (TimeRangeBean range : serviceAlert.getActiveWindows()) {
          TimeRange.Builder timeRange = alert.addActivePeriodBuilder();
          if (range.getFrom() != 0) {
            timeRange.setStart(range.getFrom() / 1000);
          }
          if (range.getTo() != 0) {
            timeRange.setEnd(range.getTo() / 1000);
          }
        }
      }

      if (serviceAlert.getAllAffects() != null) {
        for (SituationAffectsBean affects : serviceAlert.getAllAffects()) {
          EntitySelector.Builder entitySelector = alert.addInformedEntityBuilder();
          if (affects.getAgencyId() != null) {
            entitySelector.setAgencyId(affects.getAgencyId());
          }
          if (affects.getRouteId() != null) {
            entitySelector.setRouteId(normalizeId(affects.getRouteId()));
          }
          if (affects.getTripId() != null) {
            TripDescriptor.Builder trip = entitySelector.getTripBuilder();
            trip.setTripId(normalizeId(affects.getTripId()));
            entitySelector.setTrip(trip);
          }
          if (affects.getStopId() != null) {
            entitySelector.setStopId(normalizeId(affects.getStopId()));
          }
        }
      }
    }
  }

  private void fillTranslations(List<NaturalLanguageStringBean> input,
      TranslatedString.Builder output) {
    for (NaturalLanguageStringBean nls : input) {
      Translation.Builder translation = output.addTranslationBuilder();
      translation.setText(nls.getValue());
      if (nls.getLang() != null) {
        translation.setLanguage(nls.getLang());
      }
    }
  }
}
