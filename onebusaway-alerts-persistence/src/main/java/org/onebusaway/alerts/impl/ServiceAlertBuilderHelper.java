/**
 * Copyright (C) 2020 Cambridge Systematics, Inc.
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
package org.onebusaway.alerts.impl;

import com.google.transit.realtime.GtfsRealtime;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data.model.ListBean;
import org.onebusaway.transit_data.model.service_alerts.NaturalLanguageStringBean;
import org.onebusaway.transit_data.model.service_alerts.ServiceAlertBean;
import org.onebusaway.transit_data.model.service_alerts.SituationAffectsBean;
import org.onebusaway.transit_data.model.service_alerts.TimeRangeBean;

import java.util.List;

/**
 * convenience methods to convert from Service Alerts to GTFS-RT.
 */
public class ServiceAlertBuilderHelper {

    private static boolean REMOVE_AGENCY_ID = true;

    public static void fillFeedMessage(GtfsRealtime.FeedMessage.Builder feed, ListBean<ServiceAlertBean> alerts,
                                       String agencyId, long time) {

        for (ServiceAlertBean serviceAlert : alerts.getList()) {
        GtfsRealtime.FeedEntity.Builder entity = feed.addEntityBuilder();
        entity.setId(Integer.toString(feed.getEntityCount()));
        GtfsRealtime.Alert.Builder alert = entity.getAlertBuilder();

        fillTranslations(serviceAlert.getSummaries(),
                alert.getHeaderTextBuilder());
        fillTranslations(serviceAlert.getDescriptions(),
                alert.getDescriptionTextBuilder());
        fillTranslations(serviceAlert.getUrls(),
                alert.getUrlBuilder());

        if (serviceAlert.getActiveWindows() != null) {
            for (TimeRangeBean range : serviceAlert.getActiveWindows()) {
                GtfsRealtime.TimeRange.Builder timeRange = alert.addActivePeriodBuilder();
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
                GtfsRealtime.EntitySelector.Builder entitySelector = alert.addInformedEntityBuilder();
                if (affects.getAgencyId() != null) {
                    entitySelector.setAgencyId(affects.getAgencyId());
                }
                if (affects.getRouteId() != null) {
                    entitySelector.setRouteId(normalizeId(affects.getRouteId()));
                }
                if (affects.getTripId() != null) {
                    GtfsRealtime.TripDescriptor.Builder trip = entitySelector.getTripBuilder();
                    trip.setTripId(normalizeId(affects.getTripId()));
                    entitySelector.setTrip(trip);
                }
                if (affects.getStopId() != null) {
                    AgencyAndId stopId = modifiedStopId(agencyId, affects.getStopId());
                    if (stopId.getAgencyId().equals(agencyId)) {
                        entitySelector.setStopId(normalizeId(stopId.toString()));
                    }
                }
            }
        }
    }
}

    public static void fillTranslations(List<NaturalLanguageStringBean> input,
                                  GtfsRealtime.TranslatedString.Builder output) {
        if (input != null) {
            for (NaturalLanguageStringBean nls : input) {
                GtfsRealtime.TranslatedString.Translation.Builder translation = output.addTranslationBuilder();
                translation.setText(nls.getValue());
                if (nls.getLang() != null) {
                    translation.setLanguage(nls.getLang());
                }
            }
        }
    }

    protected static String normalizeId(String id) {
        if (REMOVE_AGENCY_ID) {
            int index = id.indexOf('_');
            if (index != -1) {
                id = id.substring(index + 1);
            }
        }
        return id;
    }


    protected static AgencyAndId modifiedStopId(String agency, String stopId) {
        // if we had access to the TDS we would use _stopModification table to convert
        AgencyAndId id = AgencyAndId.convertFromString(stopId);
        return id;
    }

}
