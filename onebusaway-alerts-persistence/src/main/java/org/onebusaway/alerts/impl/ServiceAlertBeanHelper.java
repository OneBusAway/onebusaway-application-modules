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

import org.onebusaway.collections.CollectionsLibrary;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data.model.service_alerts.ECause;
import org.onebusaway.transit_data.model.service_alerts.NaturalLanguageStringBean;
import org.onebusaway.transit_data.model.service_alerts.ServiceAlertBean;
import org.onebusaway.transit_data.model.service_alerts.ServiceAlertRecordBean;
import org.onebusaway.transit_data.model.service_alerts.SituationAffectsBean;
import org.onebusaway.transit_data.model.service_alerts.SituationConsequenceBean;
import org.onebusaway.transit_data.model.service_alerts.TimeRangeBean;
import org.onebusaway.util.AgencyAndIdLibrary;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * translate between ServiceAlert Beans and Records.
 */
public class ServiceAlertBeanHelper {


    public static List<ServiceAlertBean> list(List<ServiceAlertRecord> serviceAlerts) {
        List<ServiceAlertBean> beans = new ArrayList<ServiceAlertBean>();
        for (ServiceAlertRecord serviceAlert : serviceAlerts)
            beans.add(getServiceAlertAsBean(serviceAlert));
        return beans;
    }
    public static ServiceAlertBean getServiceAlertAsBean(ServiceAlertRecord serviceAlert) {

        ServiceAlertBean bean = new ServiceAlertBean();
        if (serviceAlert == null) return bean;

        AgencyAndId id = ServiceAlertLibrary.agencyAndId(serviceAlert.getAgencyId(), serviceAlert.getServiceAlertId());
        bean.setId(AgencyAndIdLibrary.convertToString(id));
        bean.setCreationTime(serviceAlert.getCreationTime());

        bean.setActiveWindows(getRangesAsBeans(serviceAlert.getActiveWindows()));
        bean.setPublicationWindows(getRangesAsBeans(serviceAlert.getPublicationWindows()));

        /**
         * Reasons
         */
        if (serviceAlert.getCause() != null)
            bean.setReason(getCauseAsReason(serviceAlert.getCause()));

        /**
         * Text descriptions
         */
        bean.setSummaries(getTranslatedStringsAsNLSBeans(serviceAlert.getSummaries()));
        bean.setDescriptions(getTranslatedStringsAsNLSBeans(serviceAlert.getDescriptions()));
        bean.setUrls(getTranslatedStringsAsNLSBeans(serviceAlert.getUrls()));

        if (serviceAlert.getSeverity() != null)
            bean.setSeverity(serviceAlert.getSeverity());

        bean.setAllAffects(getAffectsAsBeans(serviceAlert));
        bean.setConsequences(getConsequencesAsBeans(serviceAlert));
        bean.setSource(serviceAlert.getSource());

        return bean;
    }

    public static ServiceAlertRecord getServiceAlertRecordFromServiceAlertBean(
            ServiceAlertBean bean, String agencyId) {

        ServiceAlertRecord serviceAlertRecord = new ServiceAlertRecord();
        serviceAlertRecord.setAgencyId(agencyId);
        if (bean.getId() != null && !bean.getId().isEmpty()) {
            AgencyAndId id;
            if(bean.getId().indexOf("_") > -1){
                id = AgencyAndIdLibrary.convertFromString(bean.getId());
            }else{
                id = AgencyAndIdLibrary.convertFromString(agencyId + "_" + bean.getId());
            }
            serviceAlertRecord.setServiceAlertId(id.getId());
            serviceAlertRecord.setAgencyId(id.getAgencyId());
        }
        serviceAlertRecord.setCreationTime(bean.getCreationTime());
        serviceAlertRecord.setActiveWindows(getBeansAsRanges(bean.getActiveWindows()));
        serviceAlertRecord.setPublicationWindows(getBeansAsRanges(bean.getPublicationWindows()));

        /**
         * Reasons
         */
        serviceAlertRecord.setCause(getReasonAsCause(bean.getReason()));

        /**
         * Text descriptions
         */
        serviceAlertRecord.setSummaries(new HashSet<ServiceAlertLocalizedString>());
        if(bean.getSummaries() != null){
            for(NaturalLanguageStringBean summary : bean.getSummaries()){
                ServiceAlertLocalizedString string = new ServiceAlertLocalizedString();
                string.setLanguage(summary.getLang());
                string.setValue(summary.getValue());
                string.setServiceAlertRecord(serviceAlertRecord);
                serviceAlertRecord.getSummaries().add(string);
            }
        }

        serviceAlertRecord.setDescriptions(new HashSet<ServiceAlertLocalizedString>());
        if(bean.getDescriptions() != null){
            for(NaturalLanguageStringBean summary : bean.getDescriptions()){
                ServiceAlertLocalizedString string = new ServiceAlertLocalizedString();
                string.setLanguage(summary.getLang());
                string.setValue(summary.getValue());
                string.setServiceAlertRecord(serviceAlertRecord);
                serviceAlertRecord.getDescriptions().add(string);
            }
        }

        serviceAlertRecord.setUrls(new HashSet<ServiceAlertLocalizedString>());
        if(bean.getUrls() != null){
            for(NaturalLanguageStringBean url : bean.getUrls()){
                ServiceAlertLocalizedString string = new ServiceAlertLocalizedString();
                string.setLanguage(url.getLang());
                string.setValue(url.getValue());
                string.setServiceAlertRecord(serviceAlertRecord);
                serviceAlertRecord.getUrls().add(string);
            }
        }

        if (bean.getSeverity() != null)
            serviceAlertRecord.setSeverity(bean.getSeverity());

        serviceAlertRecord.setAllAffects(getBeanAsAffects(bean));
        for(ServiceAlertsSituationAffectsClause clause : serviceAlertRecord.getAllAffects()){
            clause.setServiceAlertRecord(serviceAlertRecord);
        }

        serviceAlertRecord.setConsequences(getBeanAsConsequences(bean));
        for(ServiceAlertSituationConsequenceClause clause : serviceAlertRecord.getConsequences()){
            clause.setServiceAlertRecord(serviceAlertRecord);
        }

        serviceAlertRecord.setSource(bean.getSource());

        return serviceAlertRecord;
    }



    public static List<TimeRangeBean> getRangesAsBeans(Set<ServiceAlertTimeRange> ranges) {
        if (ranges == null || ranges.isEmpty())
            return null;
        List<TimeRangeBean> beans = new ArrayList<TimeRangeBean>();
        for (ServiceAlertTimeRange range : ranges) {
            TimeRangeBean bean = new TimeRangeBean();
            if (range.getFromValue() != null)
                bean.setFrom(range.getFromValue());
            if (range.getToValue() != null)
                bean.setTo(range.getToValue());
            beans.add(bean);
        }
        return beans;
    }

    public static Set<ServiceAlertTimeRange> getBeansAsRanges(List<TimeRangeBean> beans) {
        if (beans == null)
            return Collections.emptySet();
        Set<ServiceAlertTimeRange> ranges = new HashSet<ServiceAlertTimeRange>();
        for (TimeRangeBean bean : beans) {
            ServiceAlertTimeRange range = new ServiceAlertTimeRange();
            if (bean.getFrom() > 0)
                range.setFromValue(bean.getFrom());
            if (bean.getTo() > 0)
                range.setToValue(bean.getTo());
            if (range.getFromValue() != null || range.getToValue() != null)
                ranges.add(range);
        }
        return ranges;
    }

    public static List<NaturalLanguageStringBean> getTranslatedStringsAsNLSBeans(
            Set<ServiceAlertLocalizedString> strings) {

        if (strings == null || strings.size() == 0)
            return null;

        List<NaturalLanguageStringBean> nlsBeans = new ArrayList<NaturalLanguageStringBean>();
        for (ServiceAlertLocalizedString translation : strings) {
            NaturalLanguageStringBean nls = new NaturalLanguageStringBean();
            nls.setValue(translation.getValue());
            nls.setLang(translation.getLanguage());
            nlsBeans.add(nls);
        }

        return nlsBeans;
    }

    /****
     * Consequence
     ****/

    public static List<SituationConsequenceBean> getConsequencesAsBeans(
            ServiceAlertRecord serviceAlert) {
        if (serviceAlert.getConsequences().size() == 0)
            return null;
        List<SituationConsequenceBean> beans = new ArrayList<SituationConsequenceBean>();
        for (ServiceAlertSituationConsequenceClause consequence : serviceAlert.getConsequences()) {
            SituationConsequenceBean bean = new SituationConsequenceBean();
            if (consequence.getEffect() != null)
                bean.setEffect(consequence.getEffect());
            if (consequence.getDetourPath() != null)
                bean.setDetourPath(consequence.getDetourPath());
            if (consequence.getDetourStopIds().size() != 0) {
                List<String> stopIds = new ArrayList<String>();
                for (String stopId : consequence.getDetourStopIds()) {
                    AgencyAndId id = ServiceAlertLibrary.agencyAndId(serviceAlert.getAgencyId(), stopId);
                    stopIds.add(AgencyAndId.convertToString(id));
                }
                bean.setDetourStopIds(stopIds);
            }
            beans.add(bean);
        }
        return beans;
    }

    public static Set<ServiceAlertSituationConsequenceClause> getBeanAsConsequences(ServiceAlertBean bean) {

        Set<ServiceAlertSituationConsequenceClause> consequences = new HashSet<ServiceAlertSituationConsequenceClause>();

        if (!CollectionsLibrary.isEmpty(bean.getConsequences())) {
            for (SituationConsequenceBean consequence : bean.getConsequences()) {
                ServiceAlertSituationConsequenceClause consequenceClause = new ServiceAlertSituationConsequenceClause();
                if (consequence.getEffect() != null)
                    consequenceClause.setEffect(consequence.getEffect());
                if (consequence.getDetourPath() != null)
                    consequenceClause.setDetourPath(consequence.getDetourPath());
                if (!CollectionsLibrary.isEmpty(consequence.getDetourStopIds())) {
                    Set<String> detourStopIds = new HashSet<String>();
                    for (String detourStopId : consequence.getDetourStopIds()) {
                        detourStopIds.add(detourStopId);
                    }
                    consequenceClause.setDetourStopIds(detourStopIds);
                }
                consequences.add(consequenceClause);
            }
        }

        return consequences;
    }

    /****
     * Situations Affects
     ****/

    public static List<SituationAffectsBean> getAffectsAsBeans(ServiceAlertRecord serviceAlert) {

        if (serviceAlert.getAllAffects().size() == 0)
            return null;

        List<SituationAffectsBean> beans = new ArrayList<SituationAffectsBean>();

        for (ServiceAlertsSituationAffectsClause affects : serviceAlert.getAllAffects()) {
            SituationAffectsBean bean = new SituationAffectsBean();
            if (affects.getAgencyId() != null)
                bean.setAgencyId(affects.getAgencyId());
            if (affects.getApplicationId() != null)
                bean.setApplicationId(affects.getApplicationId());
            if (affects.getRouteId() != null) {
                bean.setRouteId(affects.getRouteId());
            }
            if (affects.getDirectionId() != null)
                bean.setDirectionId(affects.getDirectionId());
            if (affects.getTripId() != null) {
                bean.setTripId(affects.getTripId());
            }
            if (affects.getStopId() != null) {
                bean.setStopId(affects.getStopId());
            }
            if (affects.getApplicationId()  != null)
                bean.setApplicationId(affects.getApplicationId());
            beans.add(bean);
        }
        return beans;
    }

    public static Set<ServiceAlertsSituationAffectsClause> getBeanAsAffects(ServiceAlertBean bean) {

        Set<ServiceAlertsSituationAffectsClause> affectsList = new HashSet<ServiceAlertsSituationAffectsClause>();

        if (!CollectionsLibrary.isEmpty(bean.getAllAffects())) {
            for (SituationAffectsBean affectsBean : bean.getAllAffects()) {
                ServiceAlertsSituationAffectsClause affects = new ServiceAlertsSituationAffectsClause();
                if (affectsBean.getAgencyId() != null)
                    affects.setAgencyId(affectsBean.getAgencyId());
                if (affectsBean.getApplicationId() != null)
                    affects.setApplicationId(affectsBean.getApplicationId());
                if (affectsBean.getRouteId() != null) {
                    affects.setRouteId(affectsBean.getRouteId());
                }
                if (affectsBean.getDirectionId() != null)
                    affects.setDirectionId(affectsBean.getDirectionId());
                if (affectsBean.getTripId() != null) {
                    affects.setTripId(affectsBean.getTripId());
                }
                if (affectsBean.getStopId() != null) {
                    affects.setStopId(affectsBean.getStopId());
                }
                affectsList.add(affects);
            }
        }

        return affectsList;
    }


    public static String getCauseAsReason(ECause cause) {
        return cause.toString();
    }

    public static ECause getReasonAsCause(String reason) {
        if (reason == null)
            return ECause.UNKNOWN_CAUSE;
        return ECause.valueOf(reason);
    }

    public static List<ServiceAlertRecordBean> listRecordBeans(List<ServiceAlertRecord> serviceAlerts) {
        List<ServiceAlertRecordBean> beans = new ArrayList<ServiceAlertRecordBean>();
        for (ServiceAlertRecord serviceAlert : serviceAlerts)
            beans.add(getServiceAlertAsRecordBean(serviceAlert));
        return beans;
    }

    public static ServiceAlertRecordBean getServiceAlertAsRecordBean(ServiceAlertRecord serviceAlert) {
        ServiceAlertBean bean = ServiceAlertBeanHelper.getServiceAlertAsBean(serviceAlert);
        ServiceAlertRecordBean serviceAlertRecordBean = new ServiceAlertRecordBean();
        serviceAlertRecordBean.setServiceAlertBean(bean);
        serviceAlertRecordBean.setCopy(serviceAlert.isCopy());

        return serviceAlertRecordBean;
    }

}
