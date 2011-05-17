package org.onebusaway.api.actions.api;

import org.apache.struts2.rest.DefaultHttpHeaders;
import org.onebusaway.api.actions.api.where.Messages;
import org.onebusaway.api.services.AlarmService;
import org.onebusaway.exceptions.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.validator.annotations.RequiredStringValidator;

public class AlarmCallbackAction extends ApiActionSupport {

  private static final long serialVersionUID = 1L;

  private static final int V2 = 2;

  private AlarmService _alarmService;

  private String _id;

  @Autowired
  public void setAlarmService(AlarmService alarmService) {
    _alarmService = alarmService;
  }

  public AlarmCallbackAction() {
    super(V2);
  }

  @RequiredStringValidator(message = Messages.MISSING_REQUIRED_FIELD)
  public void setId(String id) {
    _id = id;
  }

  public String getId() {
    return _id;
  }

  public DefaultHttpHeaders show() throws ServiceException {

    if (hasErrors())
      return setValidationErrorsResponse();

    _alarmService.fireAlarm(_id);

    return setOkResponse(null);
  }
}
