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
import java.sql.Date;

import org.apache.struts2.rest.DefaultHttpHeaders;
import org.onebusaway.api.actions.api.ApiActionSupport;
import org.onebusaway.api.impl.MaxCountSupport;
import org.onebusaway.api.impl.SearchBoundsFactory;
import org.onebusaway.api.model.transit.BeanFactoryV2;
import org.onebusaway.api.model.transit.TripDetailsV2Bean;
import org.onebusaway.exceptions.OutOfServiceAreaServiceException;
import org.onebusaway.exceptions.ServiceException;
import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.transit_data.model.ListBean;
import org.onebusaway.transit_data.model.trips.TripDetailsBean;
import org.onebusaway.transit_data.model.trips.TripDetailsInclusionBean;
import org.onebusaway.transit_data.model.trips.TripsForBoundsQueryBean;
import org.onebusaway.transit_data.services.TransitDataService;
import org.onebusaway.util.SystemTime;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.conversion.annotations.TypeConversion;

public class TripsForLocationAction extends ApiActionSupport {

  private static final long serialVersionUID = 1L;

  private static final int V2 = 2;

  private static final double MAX_BOUNDS_RADIUS = 20000.0;

  @Autowired
  private TransitDataService _service;
  
  private SearchBoundsFactory _searchBoundsFactory = new SearchBoundsFactory(MAX_BOUNDS_RADIUS);

  private long _time = 0;

  private MaxCountSupport _maxCount = new MaxCountSupport();

  private boolean _includeTrip = true;
  
  private boolean _includeStatus = false;

  private boolean _includeSchedule = false;

  public TripsForLocationAction() {
    super(V2);
  }

  public void setLat(double lat) {
    _searchBoundsFactory.setLat(lat);
  }

  public void setLon(double lon) {
    _searchBoundsFactory.setLon(lon);
  }
  
  public void setRadius(double radius) {
    _searchBoundsFactory.setRadius(radius);
  }

  public void setLatSpan(double latSpan) {
    _searchBoundsFactory.setLatSpan(latSpan);
  }

  public void setLonSpan(double lonSpan) {
    _searchBoundsFactory.setLonSpan(lonSpan);
  }

  @TypeConversion(converter = "org.onebusaway.presentation.impl.conversion.DateTimeConverter")
  public void setTime(Date time) {
    _time = time.getTime();
  }

  public void setMaxCount(int maxCount) {
    _maxCount.setMaxCount(maxCount);
  }

  public void setIncludeTrip(boolean includeTrip) {
    _includeTrip = includeTrip;
  }
  
  public void setIncludeStatus(boolean includeStatus) {
    _includeStatus = includeStatus;
  }

  public void setIncludeSchedule(boolean includeSchedule) {
    _includeSchedule = includeSchedule;
  }

  public DefaultHttpHeaders index() throws IOException, ServiceException {

    if (!isVersion(V2))
      return setUnknownVersionResponse();

    if (hasErrors())
      return setValidationErrorsResponse();

    CoordinateBounds bounds = _searchBoundsFactory.createBounds();

    long time = SystemTime.currentTimeMillis();
    if (_time != 0)
      time = _time;

    TripsForBoundsQueryBean query = new TripsForBoundsQueryBean();
    query.setBounds(bounds);
    query.setTime(time);
    query.setMaxCount(_maxCount.getMaxCount());

    TripDetailsInclusionBean inclusion = query.getInclusion();
    inclusion.setIncludeTripBean(_includeTrip);
    inclusion.setIncludeTripSchedule(_includeSchedule);
    inclusion.setIncludeTripStatus(_includeStatus);

    BeanFactoryV2 factory = getBeanFactoryV2();

    try {
      ListBean<TripDetailsBean> trips = _service.getTripsForBounds(query);
      return setOkResponse(factory.getTripDetailsResponse(trips));
    } catch (OutOfServiceAreaServiceException ex) {
      return setOkResponse(factory.getEmptyList(TripDetailsV2Bean.class, true));
    }
  }
}
