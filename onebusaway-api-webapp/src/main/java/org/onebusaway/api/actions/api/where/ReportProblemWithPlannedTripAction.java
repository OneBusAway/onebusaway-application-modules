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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import org.apache.struts2.rest.DefaultHttpHeaders;
import org.onebusaway.api.actions.api.ApiActionSupport;
import org.onebusaway.api.model.transit.BeanFactoryV2;
import org.onebusaway.api.model.transit.ItineraryV2BeanFactory;
import org.onebusaway.exceptions.ServiceException;
import org.onebusaway.presentation.impl.StackInterceptor.AddToStack;
import org.onebusaway.transit_data.model.problems.PlannedTripProblemReportBean;
import org.onebusaway.transit_data.model.tripplanning.ConstraintsBean;
import org.onebusaway.transit_data.model.tripplanning.TransitLocationBean;
import org.onebusaway.transit_data.services.TransitDataService;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.conversion.annotations.TypeConversion;

@AddToStack({"constraints", "problemReport"})
public class ReportProblemWithPlannedTripAction extends ApiActionSupport {

  private static final long serialVersionUID = 1L;

  private static final int V2 = 2;

  private TransitDataService _transitDataService;

  private TransitLocationBean _from = new TransitLocationBean();

  private TransitLocationBean _to = new TransitLocationBean();

  private long _time;

  private String _includeSelectedItinerary;

  private ConstraintsBean _constraints = new ConstraintsBean();

  private PlannedTripProblemReportBean _problemReport = new PlannedTripProblemReportBean();

  public ReportProblemWithPlannedTripAction() {
    super(V2);
  }

  @Autowired
  public void setTransitDataService(TransitDataService transitDataService) {
    _transitDataService = transitDataService;
  }

  public void setLatFrom(double latFrom) {
    _from.setLat(latFrom);
  }

  public void setLonFrom(double lonFrom) {
    _from.setLon(lonFrom);
  }

  public void setFromBlockId(String fromBlockId) {
    _from.setBlockId(fromBlockId);
  }

  public void setFromServiceDate(long fromServiceDate) {
    _from.setServiceDate(fromServiceDate);
  }

  public void setFromVehicleId(String fromVehicleId) {
    _from.setVehicleId(fromVehicleId);
  }

  public void setLatTo(double latTo) {
    _to.setLat(latTo);
  }

  public void setLonTo(double lonTo) {
    _to.setLon(lonTo);
  }

  @TypeConversion(converter = "org.onebusaway.presentation.impl.conversion.DateTimeConverter")
  public void setTime(Date time) {
    _time = time.getTime();
  }

  public void setDateAndTime(String value) throws ParseException {
    SimpleDateFormat f = new SimpleDateFormat("MM/dd/yy hh:mmaa");
    Date time = f.parse(value);
    setTime(time);
  }

  public void setIncludeSelectedItinerary(String includeSelectedItinerary) {
    _includeSelectedItinerary = includeSelectedItinerary;
  }

  public String getIncludeSelectedItinerary() {
    return _includeSelectedItinerary;
  }

  public void setConstraints(ConstraintsBean constraints) {
    _constraints = constraints;
  }

  public ConstraintsBean getConstraints() {
    return _constraints;
  }

  public void setMode(List<String> modes) {
    _constraints.setModes(new HashSet<String>(modes));
  }

  public PlannedTripProblemReportBean getProblemReport() {
    return _problemReport;
  }

  public void setProblemReport(PlannedTripProblemReportBean problemReport) {
    _problemReport = problemReport;
  }

  public DefaultHttpHeaders create() throws IOException, ServiceException {
    return index();
  }

  public DefaultHttpHeaders index() throws IOException, ServiceException {

    if (_time == 0)
      _time = System.currentTimeMillis();
    if (_constraints.getCurrentTime() == -1)
      _constraints.setCurrentTime(System.currentTimeMillis());

    BeanFactoryV2 factory = getBeanFactoryV2();
    ItineraryV2BeanFactory itineraryFactory = new ItineraryV2BeanFactory(
        factory);

    PlanTripAction.parseSelectedItinerary(itineraryFactory,
        _includeSelectedItinerary, _constraints);

    _transitDataService.reportProblemWithPlannedTrip(_from, _to, _time,
        _constraints, _problemReport);

    return setOkResponse(new Object());
  }
}
