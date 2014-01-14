/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
 * Copyright (C) 2012 Google, Inc.
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
import org.onebusaway.presentation.client.RoutePresenter;
import org.onebusaway.transit_data.model.AgencyBean;
import org.onebusaway.transit_data.model.ListBean;
import org.onebusaway.transit_data.model.StopBean;
import org.onebusaway.transit_data.model.problems.EProblemReportStatus;
import org.onebusaway.transit_data.model.problems.TripProblemReportBean;
import org.onebusaway.transit_data.model.problems.TripProblemReportQueryBean;
import org.onebusaway.transit_data.model.trips.TripBean;
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
public class TripProblemReportsAction extends ActionSupport {

  private static final long serialVersionUID = 1L;

  private TransitDataService _transitDataService;

  private String _agencyId;

  private int _days = 7;

  private String _status;

  private String _label;

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

  public void setLabel(String label) {
    _label = label;
  }

  public String getLabel() {
    return _label;
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

    TripProblemReportQueryBean query = new TripProblemReportQueryBean();
    query.setAgencyId(_agencyId);
    query.setTimeFrom(timeFrom);
    query.setTimeTo(timeTo);
    if (_status != null)
      query.setStatus(EProblemReportStatus.valueOf(_status));
    query.setLabel(_label);

    ListBean<TripProblemReportBean> result = _transitDataService.getTripProblemReports(query);
    List<TripProblemReportBean> reports = result.getList();

    _feed = new SyndFeedImpl();

    StringBuilder title = new StringBuilder();
    title.append(getText("rss.OneBusAwayTripProblemReports"));
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
    _feed.setDescription(getText("rss.UserSubmittedTripProblemReports",
        Arrays.asList((Object) agency.getName(), _days)));

    List<SyndEntry> entries = new ArrayList<SyndEntry>();
    _feed.setEntries(entries);

    for (TripProblemReportBean report : reports) {

      StopBean stop = report.getStop();
      TripBean trip = report.getTrip();

      SyndEntry entry = new SyndEntryImpl();

      StringBuilder entryTitle = new StringBuilder();
      if (trip == null) {
        entryTitle.append("trip_id=");
        entryTitle.append(report.getTripId());
        entryTitle.append(" (?)");
      } else {
        entryTitle.append(RoutePresenter.getNameForRoute(trip));
        entryTitle.append(" - ");
        entryTitle.append(trip.getTripHeadsign());
      }
      if (stop == null) {
        entryTitle.append(" - stop_id=");
        entryTitle.append(report.getStopId());
        entryTitle.append(" (?)");
      } else {
        entryTitle.append(" - ");
        entryTitle.append(getText("StopNum", new String[] {stop.getCode()}));
        entryTitle.append(" - ");
        entryTitle.append(stop.getName());
        if (stop.getDirection() != null) {
          entryTitle.append(" - ");
          entryTitle.append(getText("bound", new String[] {stop.getDirection()}));
        }
      }

      StringBuilder entryUrl = new StringBuilder();
      entryUrl.append(baseUrl);
      entryUrl.append("/admin/problems/trip-problem-report.action?tripId=");
      entryUrl.append(report.getTripId());
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
