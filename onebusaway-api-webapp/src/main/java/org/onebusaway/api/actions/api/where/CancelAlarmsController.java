package org.onebusaway.api.actions.api.where;

import java.util.List;

import org.apache.struts2.rest.DefaultHttpHeaders;
import org.onebusaway.api.actions.api.ApiActionSupport;
import org.onebusaway.api.services.AlarmService;
import org.onebusaway.exceptions.ServiceException;
import org.onebusaway.transit_data.services.TransitDataService;
import org.springframework.beans.factory.annotation.Autowired;

public class CancelAlarmsController extends ApiActionSupport {

  private static final long serialVersionUID = 1L;

  private static final int V2 = 2;

  @Autowired
  private TransitDataService _service;

  @Autowired
  private AlarmService _alarmService;

  private List<String> _ids;

  public CancelAlarmsController() {
    super(V2);
  }

  public void setId(List<String> ids) {
    _ids = ids;
  }

  public DefaultHttpHeaders index() throws ServiceException {

    if (hasErrors())
      return setValidationErrorsResponse();

    if (_ids != null) {
      for (String id : _ids) {
        _service.cancelAlarmForArrivalAndDepartureAtStop(id);
        _alarmService.cancelAlarm(id);
      }
    }

    if (isVersion(V2)) {
      return setOkResponse("");
    } else {
      return setUnknownVersionResponse();
    }
  }
}
