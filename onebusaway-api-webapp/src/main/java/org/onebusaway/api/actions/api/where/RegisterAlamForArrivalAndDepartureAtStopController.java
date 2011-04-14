package org.onebusaway.api.actions.api.where;

import org.apache.struts2.rest.DefaultHttpHeaders;
import org.onebusaway.api.actions.api.ApiActionSupport;
import org.onebusaway.exceptions.ServiceException;
import org.onebusaway.presentation.impl.StackInterceptor.AddToStack;
import org.onebusaway.transit_data.model.ArrivalAndDepartureForStopQueryBean;
import org.onebusaway.transit_data.model.RegisterAlarmQueryBean;
import org.onebusaway.transit_data.services.TransitDataService;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.validator.annotations.RequiredFieldValidator;

@AddToStack({"query", "alarm"})
public class RegisterAlamForArrivalAndDepartureAtStopController extends
    ApiActionSupport {

  private static final long serialVersionUID = 1L;

  private static final int V2 = 2;

  @Autowired
  private TransitDataService _service;

  private ArrivalAndDepartureForStopQueryBean _query = new ArrivalAndDepartureForStopQueryBean();

  private RegisterAlarmQueryBean _alarm = new RegisterAlarmQueryBean();

  public RegisterAlamForArrivalAndDepartureAtStopController() {
    super(V2);
  }

  @RequiredFieldValidator(message = Messages.MISSING_REQUIRED_FIELD)
  public void setId(String id) {
    _query.setStopId(id);
  }

  public String getId() {
    return _query.getStopId();
  }

  public ArrivalAndDepartureForStopQueryBean getQuery() {
    return _query;
  }

  public void setQuery(ArrivalAndDepartureForStopQueryBean query) {
    _query = query;
  }

  public RegisterAlarmQueryBean getAlarm() {
    return _alarm;
  }

  public void setAlarm(RegisterAlarmQueryBean alarm) {
    _alarm = alarm;
  }

  public DefaultHttpHeaders show() throws ServiceException {

    if (hasErrors())
      return setValidationErrorsResponse();

    if (_query.getTime() == 0)
      _query.setTime(System.currentTimeMillis());

    String alarmId = _service.registerAlarmForArrivalAndDepartureAtStop(_query,
        _alarm);

    if (alarmId == null)
      return setResourceNotFoundResponse();

    if (isVersion(V2)) {
      return setOkResponse(alarmId);
    } else {
      return setUnknownVersionResponse();
    }
  }
}
