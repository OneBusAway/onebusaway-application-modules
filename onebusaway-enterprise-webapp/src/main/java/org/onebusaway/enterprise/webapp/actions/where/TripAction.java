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
package org.onebusaway.enterprise.webapp.actions.where;

import com.opensymphony.xwork2.ActionSupport;
import com.opensymphony.xwork2.conversion.annotations.TypeConversion;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Actions;
import org.onebusaway.exceptions.NoSuchTripServiceException;
import org.onebusaway.exceptions.ServiceException;
import org.onebusaway.presentation.services.realtime.RealtimeService;
import org.onebusaway.transit_data.model.TripStopTimeBean;
import org.onebusaway.transit_data.model.TripStopTimesBean;
import org.onebusaway.transit_data.model.trips.TripDetailsBean;
import org.onebusaway.transit_data.model.trips.TripDetailsQueryBean;
import org.onebusaway.transit_data.model.trips.TripStatusBean;
import org.onebusaway.transit_data.services.TransitDataService;
import org.onebusaway.util.SystemTime;
import org.onebusaway.util.services.configuration.ConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class TripAction extends ActionSupport {

  private static final long serialVersionUID = 1L;

  @Autowired
  private TransitDataService _service;

  private String _id;

  private Date _serviceDate;

  private Date _time;

  private TripDetailsBean _tripDetails;

  private TimeZone _timeZone;

  private long _actualServiceDate;

  private String _vehicleId;

  private String _stopId;

  private boolean _showArrivals = false;

  private boolean _showVehicleId = true;
  
  @Autowired
  private ConfigurationService _configurationService;

  public String getGoogleMapsClientId() {
    return _configurationService.getConfigurationValueAsString("display.googleMapsClientId", "");    
  }
  
  public String getGoogleMapsChannelId() {
	  return _configurationService.getConfigurationValueAsString("display.googleMapsChannelId", "");    
  }

  public String getGoogleAdClientId() {
	return _configurationService.getConfigurationValueAsString("display.googleAdsClientId", "");    
  }

  public String getGoogleMapsApiKey() {
    return _configurationService.getConfigurationValueAsString("display.googleMapsApiKey", "");
  }

  public void setId(String id) {
    _id = id;
  }

  public String getId() {
    return _id;
  }

  @TypeConversion(converter = "org.onebusaway.presentation.impl.conversion.DateTimeConverter")
  public void setServiceDate(Date serviceDate) {
    _serviceDate = serviceDate;
  }

  public Date getServiceDate() {
    return _serviceDate;
  }

  @TypeConversion(converter = "org.onebusaway.presentation.impl.conversion.DateTimeConverter")
  public void setTime(Date time) {
    _time = time;
  }

  public Date getTime() {
    return _time;
  }

  public void setVehicleId(String vehicleId) {
    _vehicleId = vehicleId;
  }

  public String getVehicleId() {
    return _vehicleId;
  }

  public void setStopId(String stopId) {
    _stopId = stopId;
  }

  public String getStopId() {
    return _stopId;
  }

  public void setShowArrivals(boolean showArrivals) {
    _showArrivals = showArrivals;
  }

  public boolean isShowArrivals() {
    return _showArrivals;
  }
  
  public void setShowVehicleId(boolean showVehicleId) {
    _showVehicleId = showVehicleId;
  }

  public boolean isShowVehicleId() {
    return _showVehicleId;
  }
  
  public TripDetailsBean getResult() {
    return _tripDetails;
  }

  public TimeZone getTimeZone() {
    return _timeZone;
  }
  
  @Override
  @Actions({
      @Action(value = "/where/trip"),
      @Action(value = "/where/iphone/trip")})
  public String execute() throws ServiceException {

    if (_id == null)
      return INPUT;

    if (_time == null)
      _time = new Date(SystemTime.currentTimeMillis());

    TripDetailsQueryBean query = new TripDetailsQueryBean();
    query.setTripId(_id);
    if (_serviceDate != null)
      query.setServiceDate(_serviceDate.getTime());
    query.setVehicleId(_vehicleId);

    query.setTime(_time.getTime());

    _tripDetails = _service.getSingleTripDetails(query);

    if (_tripDetails == null)
      throw new NoSuchTripServiceException(_id);
    
    TripStopTimesBean stopTimes = _tripDetails.getSchedule();
    _timeZone = TimeZone.getTimeZone(stopTimes.getTimeZone());

    _actualServiceDate = getActualServiceDate();
    return SUCCESS;
  }

  public int getStopTimeRaw(TripStopTimeBean stopTime) {
    return _showArrivals ? stopTime.getArrivalTime()
        : stopTime.getDepartureTime();
  }

  public Date getStopTime(TripStopTimeBean stopTime) {
    int t = getStopTimeRaw(stopTime);
    return new Date(_actualServiceDate + t * 1000);
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
