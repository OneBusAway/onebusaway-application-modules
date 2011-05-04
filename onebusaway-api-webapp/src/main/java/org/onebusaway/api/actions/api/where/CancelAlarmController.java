package org.onebusaway.api.actions.api.where;

import org.apache.struts2.rest.DefaultHttpHeaders;
import org.onebusaway.api.actions.api.ApiActionSupport;
import org.onebusaway.api.services.AlarmService;
import org.onebusaway.exceptions.ServiceException;
import org.onebusaway.transit_data.services.TransitDataService;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.validator.annotations.RequiredFieldValidator;

public class CancelAlarmController extends ApiActionSupport {

  private static final long serialVersionUID = 1L;

  private static final int V2 = 2;

  @Autowired
  private TransitDataService _service;

  @Autowired
  private AlarmService _alarmService;

  private String _id;

  public CancelAlarmController() {
    super(V2);
  }

  @RequiredFieldValidator(message = Messages.MISSING_REQUIRED_FIELD)
  public void setId(String id) {
    _id = id;
  }

  public String getId() {
    return _id;
  }

  public DefaultHttpHeaders show() throws ServiceException {

    if (hasErrors())
      return setValidationErrorsResponse();

    _service.cancelAlarmForArrivalAndDepartureAtStop(_id);
    _alarmService.cancelAlarm(_id);

    if (isVersion(V2)) {
      return setOkResponse("");
    } else {
      return setUnknownVersionResponse();
    }
  }
}
