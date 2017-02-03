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

import java.io.IOException;
import java.util.Date;

import org.apache.struts2.rest.DefaultHttpHeaders;
import org.onebusaway.api.actions.api.ApiActionSupport;
import org.onebusaway.api.model.transit.BeanFactoryV2;
import org.onebusaway.api.model.transit.ListWithReferencesBean;
import org.onebusaway.api.model.transit.TripDetailsV2Bean;
import org.onebusaway.api.model.transit.tripplanning.MinTravelTimeToStopV2Bean;
import org.onebusaway.exceptions.OutOfServiceAreaServiceException;
import org.onebusaway.exceptions.ServiceException;
import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.presentation.impl.StackInterceptor.AddToStack;
import org.onebusaway.transit_data.model.oba.MinTravelTimeToStopsBean;
import org.onebusaway.transit_data.model.tripplanning.TransitShedConstraintsBean;
import org.onebusaway.transit_data.services.TransitDataService;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.conversion.annotations.TypeConversion;
import com.opensymphony.xwork2.validator.annotations.RequiredFieldValidator;

@AddToStack({"constraints", "constraints.constraints"})
public class MinTravelTimeToStopsAction extends ApiActionSupport {

  private static final long serialVersionUID = 1L;

  private static final int V2 = 2;

  private TransitDataService _transitDataService;

  private double _lat;

  private double _lon;

  private long _time;

  private TransitShedConstraintsBean _constraints = new TransitShedConstraintsBean();

  public MinTravelTimeToStopsAction() {
    super(V2);
  }

  @Autowired
  public void setTransitDataService(TransitDataService transitDataService) {
    _transitDataService = transitDataService;
  }

  @RequiredFieldValidator
  public void setLat(double lat) {
    _lat = lat;
  }

  public double getLat() {
    return _lat;
  }

  @RequiredFieldValidator
  public void setLon(double lon) {
    _lon = lon;
  }

  public double getLon() {
    return _lon;
  }

  @TypeConversion(converter = "org.onebusaway.presentation.impl.conversion.DateTimeConverter")
  public void setTime(Date time) {
    _time = time.getTime();
  }

  public void setConstraints(TransitShedConstraintsBean constraints) {
    _constraints = constraints;
  }

  public TransitShedConstraintsBean getConstraints() {
    return _constraints;
  }

  public DefaultHttpHeaders index() throws IOException, ServiceException {

    if (!isVersion(V2))
      return setUnknownVersionResponse();

    if (hasErrors())
      return setValidationErrorsResponse();

    if (_time == 0)
      _time = System.currentTimeMillis();

    CoordinatePoint location = new CoordinatePoint(_lat, _lon);

    MinTravelTimeToStopsBean result = _transitDataService.getMinTravelTimeToStopsFrom(
        location, _time, _constraints);

    BeanFactoryV2 factory = getBeanFactoryV2();

    try {
      ListWithReferencesBean<MinTravelTimeToStopV2Bean> response = factory.getMinTravelTimeToStops(result);
      return setOkResponse(response);
    } catch (OutOfServiceAreaServiceException ex) {
      return setOkResponse(factory.getEmptyList(TripDetailsV2Bean.class, true));
    }
  }
}
