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

import org.apache.struts2.rest.DefaultHttpHeaders;
import org.onebusaway.api.actions.api.ApiActionSupport;
import org.onebusaway.api.model.transit.BeanFactoryV2;
import org.onebusaway.api.model.transit.RegisteredAlarmV2Bean;
import org.onebusaway.api.services.AlarmDetails;
import org.onebusaway.api.services.AlarmService;
import org.onebusaway.exceptions.ServiceException;
import org.onebusaway.presentation.impl.StackInterceptor.AddToStack;
import org.onebusaway.transit_data.model.ArrivalAndDepartureForStopQueryBean;
import org.onebusaway.transit_data.model.RegisterAlarmQueryBean;
import org.onebusaway.transit_data.services.TransitDataService;
import org.onebusaway.util.SystemTime;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.validator.annotations.RequiredFieldValidator;

@AddToStack({"query", "alarm"})
public class RegisterAlarmForArrivalAndDepartureAtStopAction extends
    ApiActionSupport {

  private static final long serialVersionUID = 1L;

  private static final int V2 = 2;

  @Autowired
  private TransitDataService _service;

  @Autowired
  private AlarmService _alarmService;

  private ArrivalAndDepartureForStopQueryBean _query = new ArrivalAndDepartureForStopQueryBean();

  private RegisterAlarmQueryBean _alarm = new RegisterAlarmQueryBean();

  private String _data;

  public RegisterAlarmForArrivalAndDepartureAtStopAction() {
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

  public void setData(String data) {
    _data = data;
  }

  public DefaultHttpHeaders show() throws ServiceException {

    if (hasErrors())
      return setValidationErrorsResponse();

    if (_query.getTime() == 0)
      _query.setTime(SystemTime.currentTimeMillis());

    AlarmDetails details = _alarmService.alterAlarmQuery(_alarm, _data);

    String alarmId = _service.registerAlarmForArrivalAndDepartureAtStop(_query,
        _alarm);

    if (alarmId == null)
      return setResourceNotFoundResponse();

    if (details != null) {
      _alarmService.registerAlarm(alarmId, details);
    }

    if (isVersion(V2)) {
      RegisteredAlarmV2Bean bean = new RegisteredAlarmV2Bean();
      bean.setAlarmId(alarmId);
      BeanFactoryV2 factory = getBeanFactoryV2();
      return setOkResponse(factory.entry(bean));
    } else {
      return setUnknownVersionResponse();
    }
  }
}
