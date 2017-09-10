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

import java.util.ArrayList;
import java.util.List;

import org.apache.struts2.ServletActionContext;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.onebusaway.enterprise.webapp.actions.status.model.StatusGroup;
import org.onebusaway.enterprise.webapp.actions.status.model.StatusItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rometools.rome.feed.synd.SyndContent;
import com.rometools.rome.feed.synd.SyndContentImpl;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndEntryImpl;
import com.rometools.rome.feed.synd.SyndFeedImpl;

@Results({@Result(type = "rss", params={"feedName", "feed", "feedType", "rss_2.0"})})
public class StatusUpdateAction extends RssFeedAction {
  private static Logger _log = LoggerFactory.getLogger(StatusUpdateAction.class);
  private static final long serialVersionUID = 1L;

  @Override
  public String execute() {
    _feed = new SyndFeedImpl();
    StringBuilder title = new StringBuilder();
    title.append("OneBusAway System Status");

    String baseUrl = createBaseUrl(ServletActionContext.getRequest());

    _feed.setTitle(title.toString());
    //_feed.setLink(baseUrl);
    _feed.setLink("");
    _feed.setDescription("System Status");

    List<SyndEntry> entries = new ArrayList<SyndEntry>();

    setIcingaAlerts(entries, baseUrl);
    setAgencyServiceAlerts(entries, baseUrl);
    setAgencyMessages(entries, baseUrl);

    _feed.setEntries(entries);
    return SUCCESS;
  }


  private void setIcingaAlerts(List<SyndEntry> entries, String baseUrl) {
    // Add Icinga Alerts
    SyndEntry icingaEntry = new SyndEntryImpl();
    SyndContent icingaContent = new SyndContentImpl();
    icingaEntry.setTitle("System Monitoring");
    icingaEntry.setLink(baseUrl + "/rss/monitoring-alerts-update");
    entries.add(icingaEntry);
    StatusGroup icingaGroup = _statusProvider.getIcingaStatus();
    StatusGroup icingaProblems = new StatusGroup();
    icingaProblems.setTitle(icingaGroup.getTitle());
    // Only report items where status is not "OK"
    for (StatusItem icingaItem : icingaGroup.getItems()) {
      if (icingaItem.getStatus() != StatusItem.Status.OK) {
        icingaProblems.addItem(icingaItem);
      }
    }

    if (icingaProblems.getItems().size() == 0) {
      icingaEntry = new SyndEntryImpl();
      icingaEntry.setTitle("All systems operational");
      entries.add(icingaEntry);
    } else {
      for (StatusItem icingaItem : icingaProblems.getItems()) {
        icingaEntry = new SyndEntryImpl();
        icingaEntry.setTitle(icingaItem.getTitle());
        icingaContent = new SyndContentImpl();
        icingaContent.setValue(icingaItem.getStatus() + ": " + icingaItem.getDescription());
        icingaEntry.setDescription(icingaContent);
        entries.add(icingaEntry);
      }
    }

  }

  // these are top-level service alerts without a route/stop affects clause
  protected void setAgencyServiceAlerts(List<SyndEntry> entries, String baseUrl) {
    // Add Service Alerts
    SyndEntry serviceAlertEntry = new SyndEntryImpl();
    SyndContent saContent = new SyndContentImpl();
    serviceAlertEntry.setTitle("Agency Advisories");
    serviceAlertEntry.setLink(baseUrl + "/rss/service-alerts-update");
    entries.add(serviceAlertEntry);
    StatusGroup saGroup = _statusProvider.getAgencyServiceAlertStatus();
    if (saGroup.getItems().size() == 0) {
      serviceAlertEntry = new SyndEntryImpl();
      serviceAlertEntry.setTitle("All systems operational");
      entries.add(serviceAlertEntry);
    } else {
      for (StatusItem saItem : saGroup.getItems()) {
        serviceAlertEntry = new SyndEntryImpl();
        serviceAlertEntry.setTitle(saItem.getTitle());
        saContent = new SyndContentImpl();
        saContent.setValue(saItem.getDescription());
        serviceAlertEntry.setDescription(saContent);
        entries.add(serviceAlertEntry);
      }
    }

  }

  private void setAgencyMessages(List<SyndEntry> entries, String baseUrl) {

    // Add Agency Messages
    SyndEntry agencyMsgEntry = new SyndEntryImpl();
    agencyMsgEntry.setTitle("General Notices");
    agencyMsgEntry.setLink(baseUrl + "/rss/agency-messages-update");
    entries.add(agencyMsgEntry);
    StatusGroup agencyMsgGroup = _statusProvider.getAgencyMetadataStatus();
    if (agencyMsgGroup.getItems().size() == 0) {
      agencyMsgEntry = new SyndEntryImpl();
      agencyMsgEntry.setTitle("No Agency Messages");
      entries.add(agencyMsgEntry);
    } else {
      for (StatusItem agencyMsgItem : agencyMsgGroup.getItems()) {
        agencyMsgEntry = new SyndEntryImpl();
        agencyMsgEntry.setTitle(agencyMsgItem.getTitle());
        entries.add(agencyMsgEntry);
      }
    }
  }
}
