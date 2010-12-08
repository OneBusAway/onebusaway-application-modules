package org.onebusaway.webapp.actions.admin.debug;

import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.apache.struts2.interceptor.validation.SkipValidation;
import org.onebusaway.transit_data.model.realtime.VehicleLocationRecordBean;
import org.onebusaway.transit_data.services.TransitDataService;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.ActionSupport;
import com.opensymphony.xwork2.ModelDriven;
import com.opensymphony.xwork2.validator.annotations.RequiredStringValidator;
import com.opensymphony.xwork2.validator.annotations.Validations;

@Results({@Result(type = "redirectAction", name = "submitSuccess", params = {
    "actionName", "vehicle-location-record",})})
public class VehicleLocationRecordAction extends ActionSupport implements
    ModelDriven<VehicleLocationRecordBean> {

  private static final long serialVersionUID = 1L;

  private VehicleLocationRecordBean _model = new VehicleLocationRecordBean();

  private TransitDataService _transitDataService;

  @Autowired
  public void setTransitDataService(TransitDataService transitDataService) {
    _transitDataService = transitDataService;
  }

  @Override
  public VehicleLocationRecordBean getModel() {
    return _model;
  }

  @SkipValidation
  @Override
  public String execute() {
    return SUCCESS;
  }

  @Validations(requiredStrings = {@RequiredStringValidator(fieldName = "vehicleId", key = "requiredField")})
  public String submit() {

    if (_model.getTimeOfRecord() == 0)
      _model.setTimeOfRecord(System.currentTimeMillis());

    _model.setBlockId(clean(_model.getBlockId()));
    _model.setTripId(clean(_model.getTripId()));
    _model.setVehicleId(clean(_model.getVehicleId()));

    _transitDataService.submitVehicleLocation(_model);
    return "submitSuccess";
  }

  @Validations(requiredStrings = {@RequiredStringValidator(fieldName = "vehicleId", key = "requiredField")})
  public String reset() {
    _transitDataService.resetVehicleLocation(_model.getVehicleId());
    return "submitSuccess";
  }

  private String clean(String value) {
    if (value != null && value.trim().isEmpty())
      return null;
    return value;
  }
}
