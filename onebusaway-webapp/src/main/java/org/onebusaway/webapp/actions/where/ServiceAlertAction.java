package org.onebusaway.webapp.actions.where;

import java.util.NoSuchElementException;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Actions;
import org.onebusaway.transit_data.model.service_alerts.SituationBean;
import org.onebusaway.transit_data.services.TransitDataService;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.ActionSupport;
import com.opensymphony.xwork2.validator.annotations.RequiredStringValidator;

public class ServiceAlertAction extends ActionSupport {

  private static final long serialVersionUID = 1L;

  private TransitDataService _transitDataService;

  private String _id;

  private SituationBean _situation;

  @Autowired
  public void setTransitDataService(TransitDataService transitDataService) {
    _transitDataService = transitDataService;
  }

  @RequiredStringValidator(key = "requiredField")
  public void setId(String id) {
    _id = id;
  }
  
  public String getId() {
    return _id;
  }

  public SituationBean getSituation() {
    return _situation;
  }

  @Override
  @Actions({
      @Action(value = "/where/standard/service-alert"),
      @Action(value = "/where/iphone/service-alert"),
      @Action(value = "/where/text/service-alert")})
  public String execute() {
    _situation = _transitDataService.getServiceAlertForId(_id);
    if (_situation == null)
      throw new NoSuchElementException();
    return SUCCESS;
  }
}
