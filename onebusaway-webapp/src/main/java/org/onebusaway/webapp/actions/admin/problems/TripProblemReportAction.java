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
package org.onebusaway.webapp.actions.admin.problems;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.onebusaway.presentation.bundles.ResourceBundleSupport;
import org.onebusaway.transit_data.model.ListBean;
import org.onebusaway.transit_data.model.problems.TripProblemReportBean;
import org.onebusaway.transit_data.model.realtime.VehicleLocationRecordBean;
import org.onebusaway.transit_data.model.realtime.VehicleLocationRecordQueryBean;
import org.onebusaway.transit_data.model.trips.TripBean;
import org.onebusaway.transit_data.services.TransitDataService;
import org.onebusaway.webapp.actions.bundles.ProblemReportStatus;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.ActionSupport;
import com.opensymphony.xwork2.ModelDriven;
import com.opensymphony.xwork2.validator.annotations.RequiredFieldValidator;
import com.opensymphony.xwork2.validator.annotations.RequiredStringValidator;
import com.opensymphony.xwork2.validator.annotations.Validations;

@Results({
    @Result(type = "redirectAction", name = "list", params = {
        "actionName", "trip-problem-reports", "tripId", "${model.tripId}",
        "parse", "true"}),
    @Result(type = "redirectAction", name = "update", params = {
        "actionName", "trip-problem-report", "tripId", "${model.tripId}", "id",
        "${model.id}", "m", "1", "parse", "true"})})
public class TripProblemReportAction extends ActionSupport implements
    ModelDriven<TripProblemReportBean> {

  private static final long serialVersionUID = 1L;

  private TransitDataService _transitDataService;

  private TripProblemReportBean _model = new TripProblemReportBean();

  private List<VehicleLocationRecordBean> _vehicleLocationRecords = Collections.emptyList();

  private int _minutesBefore = 20;

  private int _minutesAfter = 10;

  private List<String> _labels;

  private int _m = 0;

  @Autowired
  public void setTransitDataService(TransitDataService transitDataService) {
    _transitDataService = transitDataService;
  }

  @Override
  public TripProblemReportBean getModel() {
    return _model;
  }

  public void setMinutesBefore(int minutesBefore) {
    _minutesBefore = minutesBefore;
  }

  public void setMinutesAfter(int minutesAfter) {
    _minutesAfter = minutesAfter;
  }
  
  public void setM(int m) {
    _m = m;
  }

  public List<VehicleLocationRecordBean> getVehicleLocationRecords() {
    return _vehicleLocationRecords;
  }

  @Validations(requiredFields = {@RequiredFieldValidator(fieldName = "model.id", message = "missing id")}, requiredStrings = {@RequiredStringValidator(fieldName = "model.tripId", message = "missing tripId")})
  @Override
  public String execute() {
    long t1 = System.currentTimeMillis();
    _model = _transitDataService.getTripProblemReportForTripIdAndId(
        _model.getTripId(), _model.getId());
    long t2 = System.currentTimeMillis();
    System.out.println("getTripProblemReportForTripIdAndId=" + (t2-t1));
    if (_model == null)
      return ERROR;

    long time = _model.getTime();
    long timeFrom = time - _minutesBefore * 60 * 1000;
    long timeTo = time + _minutesAfter * 60 * 1000;

    TripBean trip = _model.getTrip();

    if (_model.getVehicleId() != null
        || (_model.getTrip() != null && _model.getServiceDate() > 0)) {
      VehicleLocationRecordQueryBean query = new VehicleLocationRecordQueryBean();
      query.setServiceDate(_model.getServiceDate());
      if (trip != null)
        query.setBlockId(trip.getBlockId());
      query.setVehicleId(_model.getVehicleId());
      query.setFromTime(timeFrom);
      query.setToTime(timeTo);
      long t3 = System.currentTimeMillis();
      ListBean<VehicleLocationRecordBean> records = _transitDataService.getVehicleLocationRecords(query);
      long t4 = System.currentTimeMillis();
      System.out.println("getVehicleLocationRecords=" + (t4-t3));
      _vehicleLocationRecords = records.getList();
    }

    long t5 = System.currentTimeMillis();
    _labels = _transitDataService.getAllTripProblemReportLabels();
    long t6 = System.currentTimeMillis();
    System.out.println("getAllTripProblemReportLabels=" + (t6-t5));

    // Deduplicate labels
    _labels = new ArrayList<String>(new HashSet<String>(_labels));
    Collections.sort(_labels);
    
    switch(_m) {
      case 1:
        addActionMessage("Report Updated!");
        break;
    }

    return SUCCESS;
  }

  @Validations(requiredFields = {@RequiredFieldValidator(fieldName = "model.id", message = "missing id")}, requiredStrings = {@RequiredStringValidator(fieldName = "model.tripId", message = "missing tripId")})
  public String update() {
    _transitDataService.updateTripProblemReport(_model);
    return "update";
  }

  @Validations(requiredFields = {@RequiredFieldValidator(fieldName = "model.id", message = "missing id")}, requiredStrings = {@RequiredStringValidator(fieldName = "model.tripId", message = "missing tripId")})
  public String delete() {
    _transitDataService.deleteTripProblemReportForTripIdAndId(
        _model.getTripId(), _model.getId());

    return "list";
  }

  public Map<String, String> getStatusValues() {
    return ResourceBundleSupport.getLocaleMap(this, ProblemReportStatus.class);
  }

  public List<String> getLabels() {
    return _labels;
  }
}
