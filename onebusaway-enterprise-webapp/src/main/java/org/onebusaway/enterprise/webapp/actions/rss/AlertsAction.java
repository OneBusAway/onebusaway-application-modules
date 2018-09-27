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
package org.onebusaway.enterprise.webapp.actions.rss;

import com.rometools.rome.feed.synd.SyndFeedImpl;
import org.apache.commons.lang.StringUtils;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.onebusaway.presentation.impl.service_alerts.NotificationStrategy;
import org.onebusaway.rss.model.AffectsClauseRssBean;
import org.onebusaway.rss.model.ServiceAlertRssBean;
import org.onebusaway.rss.model.TimeRangeRssBean;
import org.onebusaway.transit_data.model.AgencyWithCoverageBean;
import org.onebusaway.transit_data.model.ListBean;
import org.onebusaway.transit_data.model.service_alerts.NaturalLanguageStringBean;
import org.onebusaway.transit_data.model.service_alerts.ServiceAlertBean;
import org.onebusaway.transit_data.model.service_alerts.SituationAffectsBean;
import org.onebusaway.transit_data.model.service_alerts.TimeRangeBean;
import org.onebusaway.transit_data.services.TransitDataService;
import org.onebusaway.util.SystemTime;
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
            List<ServiceAlertRssBean> beans = new ArrayList<>();

            String baseUrl = createBaseUrl(ServletActionContext.getRequest());
            setServiceAlerts(beans, baseUrl);

            for (Object objBean : beans) {
                ServiceAlertRssBean rssBean = (ServiceAlertRssBean)objBean;
                _feed.getModules().add(rssBean);
            }

            _feed.setTitle("OneBusAway Service Alerts");
            _feed.setLink("");
            _feed.setDescription("Service Information - Service Alerts");

            return SUCCESS;
        } catch (Throwable t) {
            _log.error("Exception in execute: ", t);
            return ERROR;
        }
    }

    private void setServiceAlerts(List<ServiceAlertRssBean> beans, String baseUrl) {

        List<AgencyWithCoverageBean> agencies = _transitDataService.getAgenciesWithCoverage();

        for (AgencyWithCoverageBean agency : agencies) {

            String agencyId = agency.getAgency().getId();
            String agencyName = agency.getAgency().getName();

            if (StringUtils.isNotBlank(_agencyId)) {
                // we have a filter, only return service alerts for that agency
                if (_agencyId.equals(agencyId)) {
                    setServiceAlerts(agency, beans, baseUrl);
                } else {
                    // here we reject the agency and its alerts
                }
            } else {
                // we don't have a filter, render all agencies
                setServiceAlerts(agency, beans, baseUrl);
            }

        }
    }

    private void setServiceAlerts(AgencyWithCoverageBean agency, List<ServiceAlertRssBean> beans, String baseUrl) {

        ListBean<ServiceAlertBean> allServiceAlertsForAgencyId
                = _transitDataService.getAllServiceAlertsForAgencyId(agency.getAgency().getId());

        if (allServiceAlertsForAgencyId == null) {
            _log.info("no service alerts returned for agency " + agency.getAgency().getId());
            return;
        }
        
        long time = SystemTime.currentTimeMillis();

        for (ServiceAlertBean sab : allServiceAlertsForAgencyId.getList()) {
        	
        	if(!containsActiveAlert(sab, time)){
        		continue;
        	}
        	
            ServiceAlertRssBean bean = new ServiceAlertRssBean();
            bean.setId(sab.getId());
            bean.setReason(sab.getReason());
            if (sab.getSeverity() != null) {
                bean.setSeverity(sab.getSeverity().getTpegCodes()[0]);
            }
            bean.setSummary(ServiceAlertRssBean.getLocalString(sab.getSummaries()));
            bean.setDescription(ServiceAlertRssBean.getLocalString(sab.getDescriptions()));
            bean.setPublicationWindows(toTimeRange(sab.getPublicationWindows()));
            bean.setAffectsClauses(toAffectClause(sab.getAllAffects()));
            beans.add(bean);
        }

    }
    
    private boolean containsActiveAlert(ServiceAlertBean serviceAlert, long time) {

        if (time == -1 || serviceAlert.getPublicationWindows() == null
            || serviceAlert.getPublicationWindows().size() == 0)
          return true;
        for (TimeRangeBean publicationWindow : serviceAlert.getPublicationWindows()) {
          if ((publicationWindow.getFrom() <= time)
              && (publicationWindow.getTo() >= time)) {
            return true;
          }
        }
        return false;
    }

    private List<AffectsClauseRssBean> toAffectClause(List<SituationAffectsBean> clauses) {
        List<AffectsClauseRssBean> beans = new ArrayList<>();
        if (clauses == null)
            return null;

        for (SituationAffectsBean clause : clauses) {
            AffectsClauseRssBean bean = new AffectsClauseRssBean();
            bean.setAgencyId(clause.getAgencyId());
            bean.setRouteId(clause.getRouteId());
            bean.setTripId(clause.getTripId());
            bean.setStopId(clause.getStopId());
            beans.add(bean);
        }
        return beans;
    }

    private List<TimeRangeRssBean> toTimeRange(List<TimeRangeBean> beans) {

        List<TimeRangeRssBean> trrbs = new ArrayList<>();
        if (beans == null) return trrbs;

        for (TimeRangeBean trb : beans) {
            TimeRangeRssBean trrb = new TimeRangeRssBean();
            trrb.setFrom(trb.getFrom());
            trrb.setTo(trb.getTo());
            trrbs.add(trrb);
        }
        return trrbs;
    }

}
