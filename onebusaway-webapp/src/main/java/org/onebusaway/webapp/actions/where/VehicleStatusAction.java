package org.onebusaway.webapp.actions.where;

import java.util.Date;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Actions;
import org.onebusaway.transit_data.model.VehicleStatusBean;
import org.onebusaway.transit_data.services.TransitDataService;
import org.onebusaway.webapp.actions.OneBusAwayActionSupport;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.conversion.annotations.TypeConversion;
import com.opensymphony.xwork2.validator.annotations.RequiredFieldValidator;

public class VehicleStatusAction extends OneBusAwayActionSupport {

  private static final long serialVersionUID = 1L;

  private TransitDataService _transitDataService;

  private String _vehicleId;

  private Date _time;

  private VehicleStatusBean _result;

  @Autowired
  public void setTransitDataService(TransitDataService transitDataService) {
    _transitDataService = transitDataService;
  }

  @RequiredFieldValidator(key = "requiredField")
  public void setVehicleId(String vehicleId) {
    _vehicleId = vehicleId;
  }

  public String getVehicleId() {
    return _vehicleId;
  }

  public VehicleStatusBean getResult() {
    return _result;
  }

  @TypeConversion(converter = "org.onebusaway.presentation.impl.conversion.DateConverter")
  public void setTime(Date time) {
    _time = time;
  }

  @Override
  @Actions({@Action(value = "/where/standard/vehicle-status")})
  public String execute() {
    if (_time == null)
      _time = new Date();
    _result = _transitDataService.getVehicleForAgency(_vehicleId,
        _time.getTime());
    return SUCCESS;
  }
}
