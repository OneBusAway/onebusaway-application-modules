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
import org.onebusaway.alerts.service.ServiceAlerts;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data.model.ListBean;
import org.onebusaway.transit_data.model.service_alerts.EEffect;
import org.onebusaway.transit_data.model.service_alerts.ESeverity;
import org.onebusaway.transit_data.model.service_alerts.NaturalLanguageStringBean;
import org.onebusaway.transit_data.model.service_alerts.ServiceAlertBean;
import org.onebusaway.transit_data.model.service_alerts.SituationAffectsBean;
import org.onebusaway.transit_data.model.service_alerts.SituationConsequenceBean;
import org.onebusaway.transit_data.model.service_alerts.TimeRangeBean;
import org.onebusaway.util.AgencyAndIdLibrary;

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

    public static ServiceAlerts.ServiceAlertsCollection fillServiceAlerts(ListBean<ServiceAlertBean> alerts,
                                                                          String agencyId, long time) {

        ServiceAlerts.ServiceAlertsCollection.Builder feedCollection = ServiceAlerts.ServiceAlertsCollection.newBuilder();

        for (ServiceAlertBean serviceAlert : alerts.getList()) {
            ServiceAlerts.ServiceAlert.Builder feedAlert =  ServiceAlerts.ServiceAlert.newBuilder();
            feedAlert.setId(normalizeToId(agencyId, serviceAlert.getId()));
            ServiceAlerts.TranslatedString translatedSummary = toTranslatedString(serviceAlert.getSummaries());
            if (translatedSummary != null)
                feedAlert.setSummary(translatedSummary);
            ServiceAlerts.TranslatedString translatedDescription = toTranslatedString(serviceAlert.getDescriptions());
            if (translatedDescription != null)
                feedAlert.setDescription(translatedDescription);
            ServiceAlerts.TranslatedString translatedUrl = toTranslatedString(serviceAlert.getUrls());
            if (translatedUrl != null)
                feedAlert.setUrl(translatedUrl);
            feedAlert.setCreationTime(serviceAlert.getCreationTime());
            feedAlert.setModifiedTime(serviceAlert.getCreationTime());

            if (serviceAlert.getActiveWindows() != null) {
                for (TimeRangeBean activeWindow : serviceAlert.getActiveWindows()) {
                    feedAlert.addActiveWindow(toTimeRange(activeWindow));
                }
            }

            if (serviceAlert.getAllAffects() != null) {
                for (SituationAffectsBean affect : serviceAlert.getAllAffects()) {
                    feedAlert.addAffects(toAffects(affect));
                }
            }

            if (serviceAlert.getConsequences() != null) {
                for (SituationConsequenceBean bean : serviceAlert.getConsequences()) {
                    feedAlert.addConsequence(toConsequence(bean));
                }
            }

            if (serviceAlert.getPublicationWindows() != null) {
                for (TimeRangeBean trb : serviceAlert.getPublicationWindows()) {
                    feedAlert.addPublicationWindow(toTimeRange(trb));
                }
            }

            if (serviceAlert.getSeverity() != null
                    && toSeverity(serviceAlert.getSeverity()) != null)
                feedAlert.setSeverity(toSeverity(serviceAlert.getSeverity()));
            if (serviceAlert.getReason() != null
                    && toCause(serviceAlert.getReason()) != null)
                feedAlert.setCause(toCause(serviceAlert.getReason()));
            if (serviceAlert.getSource() != null)
                feedAlert.setSource(serviceAlert.getSource());

            feedCollection.addServiceAlerts(feedAlert);
        }
        return feedCollection.build();
    }

    private static ServiceAlerts.ServiceAlert.Cause toCause(String reason) {
        switch (reason) {
            case "OTHER_CAUSE":
                return ServiceAlerts.ServiceAlert.Cause.OTHER_CAUSE;
            case "TECHNICAL_PROBLEM":
                return ServiceAlerts.ServiceAlert.Cause.TECHNICAL_PROBLEM;
            case "STRIKE":
                return ServiceAlerts.ServiceAlert.Cause.STRIKE;
            case "DEMONSTRATION":
                return ServiceAlerts.ServiceAlert.Cause.DEMONSTRATION;
            case "ACCIDENT":
                return ServiceAlerts.ServiceAlert.Cause.ACCIDENT;
            case "HOLIDAY":
                return ServiceAlerts.ServiceAlert.Cause.HOLIDAY;
            case "WEATHER":
                return ServiceAlerts.ServiceAlert.Cause.WEATHER;
            case "MAINTENANCE":
                return ServiceAlerts.ServiceAlert.Cause.MAINTENANCE;
            case "CONSTRUCTION":
                return ServiceAlerts.ServiceAlert.Cause.CONSTRUCTION;
            case "POLICE_ACTIVITY":
                return ServiceAlerts.ServiceAlert.Cause.POLICE_ACTIVITY;
            case "MEDICAL_EMERGENCY":
                return ServiceAlerts.ServiceAlert.Cause.MEDICAL_EMERGENCY;
            case "UNKNOWN_CAUSE":
            default:
                return ServiceAlerts.ServiceAlert.Cause.UNKNOWN_CAUSE;
        }
    }

    private static ServiceAlerts.ServiceAlert.Severity toSeverity(ESeverity eValue) {
        switch (eValue) {
            case VERY_SLIGHT:
                return ServiceAlerts.ServiceAlert.Severity.VERY_SLIGHT;
            case SLIGHT:
                return ServiceAlerts.ServiceAlert.Severity.SLIGHT;
            case NORMAL:
                return ServiceAlerts.ServiceAlert.Severity.NORMAL;
            case SEVERE:
                return ServiceAlerts.ServiceAlert.Severity.SEVERE;
            case VERY_SEVERE:
                return ServiceAlerts.ServiceAlert.Severity.VERY_SEVERE;
            case UNDEFINED:
            case UNKNOWN:
            default:
                return ServiceAlerts.ServiceAlert.Severity.UNKNOWN;
        }

    }

    private static ServiceAlerts.Consequence toConsequence(SituationConsequenceBean bean) {
        ServiceAlerts.Consequence.Builder builder = ServiceAlerts.Consequence.newBuilder();
        builder.setEffect(toEffect(bean.getEffect()));
        return builder.build();
    }

    private static ServiceAlerts.Consequence.Effect toEffect(EEffect effect) {
        if (effect == null)
            return ServiceAlerts.Consequence.Effect.UNKNOWN_EFFECT;
        return ServiceAlerts.Consequence.Effect.valueOf(effect.toString());
    }

    private static ServiceAlerts.Affects toAffects(SituationAffectsBean affect) {
        ServiceAlerts.Affects.Builder builder = ServiceAlerts.Affects.newBuilder();
        if (affect.getAgencyId() != null)
            builder.setAgencyId(affect.getAgencyId());

        if (affect.getApplicationId() != null)
            builder.setApplicationId(affect.getApplicationId());

        if (affect.getRouteId() != null)
            builder.setRouteId(normalizeToId(affect.getAgencyId(), affect.getRouteId()));

        if (affect.getDirectionId() != null)
            builder.setDirectionId(affect.getDirectionId());

        if (affect.getStopId() != null)
            builder.setStopId(normalizeToId(affect.getAgencyId(), affect.getStopId()));

        if (affect.getTripId() != null)
            builder.setTripId(normalizeToId(affect.getAgencyId(), affect.getTripId()));
        return builder.build();
    }

    private static ServiceAlerts.TimeRange toTimeRange(TimeRangeBean activeWindow) {
        ServiceAlerts.TimeRange.Builder tr = ServiceAlerts.TimeRange.newBuilder();
        tr.setStart(activeWindow.getTo());
        tr.setEnd(activeWindow.getFrom());
        return tr.build();
    }

    // NOTE: we only grab the first translation, we don't respect languages here
    private static ServiceAlerts.TranslatedString toTranslatedString(List<NaturalLanguageStringBean> input) {
        if (input == null || input.isEmpty()) return null;
        ServiceAlerts.TranslatedString.Builder builderTS = ServiceAlerts.TranslatedString.newBuilder();
        ServiceAlerts.TranslatedString.Translation.Builder translation = ServiceAlerts.TranslatedString.Translation.newBuilder();
        String lang = input.get(0).getLang();
        if (lang == null)
            translation.setLanguage("en");
        else
            translation.setLanguage(lang);
        String txt = input.get(0).getValue();
        if (txt == null)
            return null;
        translation.setText(txt);
        builderTS.addTranslation(translation);
        return builderTS.build();
    }

    private static ServiceAlerts.Id normalizeToId(String defaultAgencyId, String id) {
        if (id == null) return null;
        ServiceAlerts.Id.Builder builder = ServiceAlerts.Id.newBuilder();
        if (id.contains("_")) {
            // id is compound, use both parts
            AgencyAndId agencyAndId = AgencyAndIdLibrary.convertFromString(id);
            builder.setAgencyId(agencyAndId.getAgencyId());
            builder.setId(agencyAndId.getId());
        } else {
            // use default agency
            builder.setAgencyId(defaultAgencyId);
            builder.setId(id);
        }
        return builder.build();
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
