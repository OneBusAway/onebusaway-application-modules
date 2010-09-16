/*
 * Copyright 2008 Brian Ferris
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.onebusaway.webapp.actions.where;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Actions;
import org.onebusaway.exceptions.NoSuchTripServiceException;
import org.onebusaway.exceptions.ServiceException;
import org.onebusaway.transit_data.model.TripStopTimeBean;
import org.onebusaway.transit_data.model.TripStopTimesBean;
import org.onebusaway.transit_data.model.trips.TripDetailsBean;
import org.onebusaway.transit_data.model.trips.TripDetailsQueryBean;
import org.onebusaway.transit_data.model.trips.TripStatusBean;
import org.onebusaway.transit_data.services.TransitDataService;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.ActionSupport;

public class TripAction extends ActionSupport {

  private static final long serialVersionUID = 1L;

  @Autowired
  private TransitDataService _service;

  private String _id;

  private Date _serviceDate = new Date();

  private Date _time = new Date();

  private TripDetailsBean _tripDetails;

  private TimeZone _timeZone;

  private long _actualServiceDate;

  public void setId(String id) {
    _id = id;
  }

  public void setServiceDate(Date serviceDate) {
    _serviceDate = serviceDate;
  }

  public void setTime(Date time) {
    _time = time;
  }

  public void setStop(String stopId) {

  }

  public TripDetailsBean getResult() {
    return _tripDetails;
  }

  public TimeZone getTimeZone() {
    return _timeZone;
  }

  @Override
  @Actions( {
      @Action(value = "/where/standard/trip"),
      @Action(value = "/where/iphone/trip"),
      @Action(value = "/where/text/trip")})
  public String execute() throws ServiceException {

    if (_id == null)
      return INPUT;

    TripDetailsQueryBean query = new TripDetailsQueryBean();
    query.setTripId(_id);
    query.setServiceDate(_serviceDate.getTime());
    query.setTime(_time.getTime());
    _tripDetails = _service.getSpecificTripDetails(query);

    if (_tripDetails == null)
      throw new NoSuchTripServiceException(_id);

    TripStopTimesBean stopTimes = _tripDetails.getSchedule();
    _timeZone = TimeZone.getTimeZone(stopTimes.getTimeZone());

    _actualServiceDate = getActualServiceDate();
    return SUCCESS;
  }

  public Date getStopTime(TripStopTimeBean stopTime) {
    return new Date(_actualServiceDate + stopTime.getDepartureTime() * 1000);
  }

  private long getActualServiceDate() {
    TripStatusBean status = _tripDetails.getStatus();

    if (status != null)
      return status.getServiceDate();

    Calendar c = Calendar.getInstance(_timeZone);

    // Initial set time to noon
    c.set(Calendar.HOUR_OF_DAY, 12);
    c.set(Calendar.MINUTE, 0);
    c.set(Calendar.SECOND, 0);
    c.set(Calendar.MILLISECOND, 0);

    // Subtract 12 hours. Usually takes you to midnight, except on DST days
    c.add(Calendar.HOUR_OF_DAY, -12);

    return c.getTimeInMillis();
  }
}
