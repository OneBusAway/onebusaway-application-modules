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
package org.onebusaway.enterprise.webapp.actions.rss;

import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndContentImpl;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndEntryImpl;
import com.sun.syndication.feed.synd.SyndFeedImpl;
import org.apache.commons.lang.StringUtils;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.onebusaway.presentation.impl.service_alerts.NotificationStrategy;
import org.onebusaway.transit_data.model.AgencyWithCoverageBean;
import org.onebusaway.transit_data.model.ListBean;
import org.onebusaway.transit_data.model.service_alerts.NaturalLanguageStringBean;
import org.onebusaway.transit_data.model.service_alerts.ServiceAlertBean;
import org.onebusaway.transit_data.model.service_alerts.SituationAffectsBean;
import org.onebusaway.transit_data.services.TransitDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

@Results({@Result(type = "rss", params={"feedName", "feed", "feedType", "rss_2.0"})})
/**
 * Simple service alert output via RSS.  @See NotificationStrategy for tuning the
 * affects clause.
 */
public class AlertsAction extends RssFeedAction {

    private static Logger _log = LoggerFactory.getLogger(StatusUpdateAction.class);

    @Autowired
    private TransitDataService _transitDataService;

    @Autowired
    protected NotificationStrategy _strategy;


    private String _agencyId;
    public void setAgencyId(String agencyId) {
        _agencyId = agencyId;
    }

    private String _locale = "en";
    public void setLocaleString(String localeString) {
        _locale = localeString;
    }

    @Override
    public String execute() {
        try {
            _feed = new SyndFeedImpl();

            _feed.setTitle("OneBusAway Service Alerts");
            String baseUrl = createBaseUrl(ServletActionContext.getRequest());
            _feed.setLink("");
            _feed.setDescription("Service Information");

            List<SyndEntry> entries = new ArrayList<SyndEntry>();
            setServiceAlerts(entries, baseUrl);
            _feed.setEntries(entries);

            return SUCCESS;
        } catch (Throwable t) {
            _log.error("Exception in execute: ", t);
            return ERROR;
        }
    }

    private void setServiceAlerts(List<SyndEntry> entries, String baseUrl) {
        SyndEntry serviceAlertEntry = new SyndEntryImpl();
        SyndContent saContent = new SyndContentImpl();
        serviceAlertEntry.setTitle("Agency Advisories");
        serviceAlertEntry.setLink(baseUrl + "/rss/alerts");
        entries.add(serviceAlertEntry);

        List<AgencyWithCoverageBean> agencies = _transitDataService.getAgenciesWithCoverage();

        for (AgencyWithCoverageBean agency : agencies) {

            String agencyId = agency.getAgency().getId();
            String agencyName = agency.getAgency().getName();

            if (StringUtils.isNotBlank(_agencyId)) {
                // we have a filter, only return service alerts for that agency
                if (_agencyId.equals(agencyId)) {
                    setServiceAlerts(agency, entries, baseUrl);
                } else {
                    // here we reject the agency and its alerts
                }
            } else {
                // we don't have a filter, render all agencies
                setServiceAlerts(agency, entries, baseUrl);
            }

        }
    }

    private void setServiceAlerts(AgencyWithCoverageBean agency, List<SyndEntry> entries, String baseUrl) {

        ListBean<ServiceAlertBean> allServiceAlertsForAgencyId
                = _transitDataService.getAllServiceAlertsForAgencyId(agency.getAgency().getId());

        if (allServiceAlertsForAgencyId == null) {
            _log.info("no service alerts returned for agency " + agency.getAgency().getId());
            return;
        }

        for (ServiceAlertBean sab : allServiceAlertsForAgencyId.getList()) {
            SyndEntry serviceAlertEntry = new SyndEntryImpl();
            SyndContent saContent = new SyndContentImpl();
            serviceAlertEntry = new SyndEntryImpl();
            serviceAlertEntry.setTitle(simplify(sab.getSummaries()));
            saContent.setValue(summarize(sab.getDescriptions(), sab.getAllAffects()));
            serviceAlertEntry.setDescription(saContent);
            entries.add(serviceAlertEntry);
        }

    }

    // given a list of language strings pick the most appropriate
    private String simplify(List<NaturalLanguageStringBean> items) {
        if (items == null || items.isEmpty()) return "";
        if (items.size() == 1) return items.get(0).getValue();
        for (NaturalLanguageStringBean bean : items) {
            if (_locale.equalsIgnoreCase(bean.getLang())) {
                return bean.getValue();
            }
        }
        // we had more than one item but did not match on language
        // log it and return the first item
        _log.warn("unmatched locale " + _locale + " for items=" + items);
        return items.get(0).getValue();
    }

    // summarize a service alert's description in consequences
    private String summarize(List<NaturalLanguageStringBean> descriptions, List<SituationAffectsBean> affects) {
        StringBuffer text = new StringBuffer();
        text.append(simplify(descriptions));
        text.append(" for"); // this really should come from a resource bundle

        for(String s : getOnlyAgencyAffects(affects)) {
            // if this fires the others will not
            text.append(s);
        }

        for(String s : getRouteAffects(affects)) {
            text.append(" route ")
                    .append(_strategy.summarizeRoute(s))
                    .append(",");
        }
        text.deleteCharAt(text.length()-1); // remove trailing comma
        for (String s : getStopAffects(affects)) {
            text.append(" stop ")
                    .append(_strategy.summarizeStop(s))
                    .append(",");
        }
        text.deleteCharAt(text.length()-1); // remove trailing comma


        return text.toString();
    }

    private List<String> getOnlyAgencyAffects(List<SituationAffectsBean> affects) {
        List<String> results = new ArrayList<String>();
        if (affects == null) return results;
        for (SituationAffectsBean affectsBean : affects) {
            if (affectsBean != null
                    && StringUtils.isBlank(affectsBean.getRouteId())
                    && StringUtils.isBlank(affectsBean.getStopId())
                    && StringUtils.isBlank(affectsBean.getTripId())
                    && StringUtils.isNotBlank(affectsBean.getAgencyId())) {
                results.add(affectsBean.getAgencyId());
            }
        }
        return results;
    }


    private List<String> getRouteAffects(List<SituationAffectsBean> affects) {
        List<String> results = new ArrayList<String>();
        if (affects == null) return results;
        for (SituationAffectsBean affectsBean : affects) {
            if (affectsBean != null
                    && StringUtils.isNotBlank(affectsBean.getRouteId())) {
                results.add(affectsBean.getRouteId());
            }
        }
        return results;
    }

    private List<String> getStopAffects(List<SituationAffectsBean> affects) {
        List<String> results = new ArrayList<String>();
        if (affects == null) return results;
        for (SituationAffectsBean affectsBean : affects) {
            if (affectsBean != null
                    && StringUtils.isNotBlank(affectsBean.getStopId())) {
                results.add(affectsBean.getStopId());
            }
        }
        return results;
    }

}
