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
package org.onebusaway.api.actions.api.where;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.struts2.rest.DefaultHttpHeaders;
import org.onebusaway.api.actions.api.ApiActionSupport;
import org.onebusaway.api.model.transit.BeanFactoryV2;
import org.onebusaway.api.model.where.ArrivalAndDepartureBeanV1;
import org.onebusaway.api.model.where.StopWithArrivalsAndDeparturesBeanV1;
import org.onebusaway.exceptions.NoSuchStopServiceException;
import org.onebusaway.exceptions.ServiceException;
import org.onebusaway.transit_data.model.ArrivalAndDepartureBean;
import org.onebusaway.transit_data.model.ArrivalsAndDeparturesQueryBean;
import org.onebusaway.transit_data.model.RouteBean;
import org.onebusaway.transit_data.model.StopBean;
import org.onebusaway.transit_data.model.StopWithArrivalsAndDeparturesBean;
import org.onebusaway.transit_data.model.trips.TripBean;
import org.onebusaway.transit_data.services.TransitDataService;
import org.onebusaway.util.SystemTime;
import org.onebusaway.util.services.configuration.ConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.conversion.annotations.TypeConversion;
import com.opensymphony.xwork2.validator.annotations.RequiredFieldValidator;

public class ArrivalsAndDeparturesForStopAction extends ApiActionSupport {

  private static final long serialVersionUID = 1L;

  private static final int V1 = 1;

  private static final int V2 = 2;

  @Autowired
  private TransitDataService _service;

  @Autowired
  private ConfigurationService _configService;

  private String _id;
  
  private ArrivalsAndDeparturesQueryBean _query = new ArrivalsAndDeparturesQueryBean();

  public ArrivalsAndDeparturesForStopAction() {
    super(LegacyV1ApiSupport.isDefaultToV1() ? V1 : V2);
  }

  @RequiredFieldValidator(message = "whoa there")
  public void setId(String id) {
    _id = id;
  }

  public String getId() {
    return _id;
  }

  @TypeConversion(converter = "org.onebusaway.presentation.impl.conversion.DateTimeConverter")
  public void setTime(Date time) {
    _query.setTime(time.getTime());
  }

  public void setMinutesBefore(int minutesBefore) {
    _query.setMinutesBefore(minutesBefore);
  }

  public void setMinutesAfter(int minutesAfter) {
    _query.setMinutesAfter(minutesAfter);
  }
  
  public void setFrequencyMinutesBefore(int frequncyMinutesBefore) {
    _query.setFrequencyMinutesBefore(frequncyMinutesBefore);
  }

  public void setFrequencyMinutesAfter(int frequencyMinutesAfter) {
    _query.setFrequencyMinutesAfter(frequencyMinutesAfter);
  }


  public DefaultHttpHeaders show() throws ServiceException {

    if (hasErrors())
      return setValidationErrorsResponse();

    boolean serviceDateFilterOn = Boolean.parseBoolean(_configService.getConfigurationValueAsString("display.serviceDateFiltering", "false"));
    if(serviceDateFilterOn || _query.getTime() == 0)
      _query.setTime(SystemTime.currentTimeMillis());

    StopWithArrivalsAndDeparturesBean result = null;
    try {
      result = _service.getStopWithArrivalsAndDepartures(
              _id, _query);
    } catch (NoSuchStopServiceException nsse) {
      return setResourceNotFoundResponse();
    } catch (ServiceException any) {
      return setResourceNotFoundResponse();
    } catch (Exception any) {
      return setExceptionResponse();
    }

    if (result == null)
      return setResourceNotFoundResponse();

    if (isVersion(V1)) {
      // Convert data to v1 form
      List<ArrivalAndDepartureBeanV1> arrivals = getArrivalsAsV1(result);
      StopWithArrivalsAndDeparturesBeanV1 v1 = new StopWithArrivalsAndDeparturesBeanV1(
          result.getStop(), arrivals, result.getNearbyStops());
      return setOkResponse(v1);
    } else if (isVersion(V2)) {
      BeanFactoryV2 factory = getBeanFactoryV2();
      return setOkResponse(factory.getResponse(result));
    } else {
      return setUnknownVersionResponse();
    }
  }

  private List<ArrivalAndDepartureBeanV1> getArrivalsAsV1(
      StopWithArrivalsAndDeparturesBean result) {

    List<ArrivalAndDepartureBeanV1> v1s = new ArrayList<ArrivalAndDepartureBeanV1>();

    for (ArrivalAndDepartureBean bean : result.getArrivalsAndDepartures()) {

      TripBean trip = bean.getTrip();
      RouteBean route = trip.getRoute();
      StopBean stop = bean.getStop();
      
      ArrivalAndDepartureBeanV1 v1 = new ArrivalAndDepartureBeanV1();
      v1.setPredictedArrivalTime(bean.getPredictedArrivalTime());
      v1.setPredictedDepartureTime(bean.getPredictedDepartureTime());
      v1.setRouteId(route.getId());
      if (trip.getRouteShortName() != null)
        v1.setRouteShortName(trip.getRouteShortName());
      else
        v1.setRouteShortName(route.getShortName());
      v1.setScheduledArrivalTime(bean.getScheduledArrivalTime());
      v1.setScheduledDepartureTime(bean.getScheduledDepartureTime());
      v1.setStatus(bean.getStatus());      
      v1.setStopId(stop.getId());
      v1.setTripHeadsign(trip.getTripHeadsign());
      v1.setTripId(trip.getId());
      v1.setOccupancyStatus(bean.getOccupancyStatus());
      v1.setHistoricalOccupancy(bean.getHistoricalOccupancy());

      v1s.add(v1);
    }

    return v1s;
  }
}
