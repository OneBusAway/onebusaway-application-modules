/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
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
package org.onebusaway.webapp.actions.rss;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts2.ServletActionContext;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.onebusaway.transit_data.model.AgencyBean;
import org.onebusaway.transit_data.model.ListBean;
import org.onebusaway.transit_data.model.StopBean;
import org.onebusaway.transit_data.model.problems.EProblemReportStatus;
import org.onebusaway.transit_data.model.problems.StopProblemReportBean;
import org.onebusaway.transit_data.model.problems.StopProblemReportQueryBean;
import org.onebusaway.transit_data.services.TransitDataService;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.ActionSupport;
import com.opensymphony.xwork2.validator.annotations.RequiredStringValidator;
import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndContentImpl;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndEntryImpl;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndFeedImpl;

@Results({@Result(type = "rome")})
public class StopProblemReportsAction extends ActionSupport {

  private static final long serialVersionUID = 1L;

  private TransitDataService _transitDataService;

  private String _agencyId;

  private int _days = 7;

  private String _status;

  private SyndFeed _feed;

  @Autowired
  public void setTransitDataService(TransitDataService transitDataService) {
    _transitDataService = transitDataService;
  }

  @RequiredStringValidator(key = "requiredField")
  public void setAgencyId(String agencyId) {
    _agencyId = agencyId;
  }

  public String getAgencyId() {
    return _agencyId;
  }

  public void setDays(int days) {
    _days = days;
  }

  public void setStatus(String status) {
    _status = status;
  }

  public SyndFeed getFeed() {
    return _feed;
  }

  @Override
  public String execute() {

    AgencyBean agency = _transitDataService.getAgency(_agencyId);

    if (agency == null)
      return INPUT;

    Calendar c = Calendar.getInstance();
    long timeTo = c.getTimeInMillis();
    c.add(Calendar.DAY_OF_WEEK, -_days);
    long timeFrom = c.getTimeInMillis();

    StopProblemReportQueryBean query = new StopProblemReportQueryBean();
    query.setAgencyId(_agencyId);
    query.setTimeFrom(timeFrom);
    query.setTimeTo(timeTo);
    if (_status != null)
      query.setStatus(EProblemReportStatus.valueOf(_status));

    ListBean<StopProblemReportBean> result = _transitDataService.getStopProblemReports(query);
    List<StopProblemReportBean> reports = result.getList();

    _feed = new SyndFeedImpl();

    StringBuilder title = new StringBuilder();
    title.append(getText("rss.OneBusAwayStopProblemReports"));
    title.append(" - ");
    title.append(agency.getName());
    title.append(" - ");
    title.append(getText("rss.LastXDays", Arrays.asList((Object) _days)));

    HttpServletRequest request = ServletActionContext.getRequest();

    StringBuilder b = new StringBuilder();
    b.append("http://");
    b.append(request.getServerName());
    if (request.getServerPort() != 80)
      b.append(":").append(request.getServerPort());
    if (request.getContextPath() != null)
      b.append(request.getContextPath());
    String baseUrl = b.toString();

    _feed.setTitle(title.toString());
    _feed.setLink(baseUrl);
    _feed.setDescription(getText("rss.UserSubmittedStopProblemReports",
        Arrays.asList((Object) agency.getName(), _days)));

    List<SyndEntry> entries = new ArrayList<SyndEntry>();
    _feed.setEntries(entries);

    for (StopProblemReportBean report : reports) {

      StopBean stop = report.getStop();
      SyndEntry entry = new SyndEntryImpl();

      StringBuilder entryTitle = new StringBuilder();
      if (stop == null) {
        entryTitle.append("stopId=");
        entryTitle.append(report.getStopId());
        entryTitle.append(" (?)");
      } else {
        entryTitle.append(getText("StopNum", Arrays.asList(stop.getCode())));
        entryTitle.append(" - ");
        entryTitle.append(stop.getName());
        if (stop.getDirection() != null)
          entryTitle.append(" - ").append(getText("bound", Arrays.asList(stop.getDirection())));
      }

      StringBuilder entryUrl = new StringBuilder();
      entryUrl.append(baseUrl);
      entryUrl.append("/admin/problems/stop-problem-reports!edit.action?stopId=");
      entryUrl.append(report.getStopId());
      entryUrl.append("&id=");
      entryUrl.append(report.getId());

      StringBuilder entryDesc = new StringBuilder();
      entryDesc.append(getText("Data"));
      entryDesc.append(": ");
      entryDesc.append(report.getData());
      entryDesc.append("<br/>");

      if (report.getUserComment() != null) {
        entryDesc.append(getText("Comment"));
        entryDesc.append(": ");
        entryDesc.append(report.getUserComment());
        entryDesc.append("<br/>");
      }

      if (report.getStatus() != null) {
        entryDesc.append(getText("Status"));
        entryDesc.append(": ");
        entryDesc.append(report.getStatus());
        entryDesc.append("<br/>");
      }

      entry = new SyndEntryImpl();
      entry.setTitle(entryTitle.toString());
      entry.setLink(entryUrl.toString());
      entry.setPublishedDate(new Date(report.getTime()));

      SyndContent description = new SyndContentImpl();
      description.setType("text/html");
      description.setValue(entryDesc.toString());
      entry.setDescription(description);
      entries.add(entry);
    }

    return SUCCESS;
  }
}
