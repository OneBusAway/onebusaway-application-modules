package org.onebusaway.api.actions.api.where;

import java.util.Date;

import org.apache.struts2.rest.DefaultHttpHeaders;
import org.onebusaway.api.actions.api.ApiActionSupport;
import org.onebusaway.api.model.transit.BeanFactoryV2;
import org.onebusaway.exceptions.ServiceException;
import org.onebusaway.transit_data.model.StopScheduleBean;
import org.onebusaway.transit_data.services.TransitDataService;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.conversion.annotations.TypeConversion;
import com.opensymphony.xwork2.validator.annotations.RequiredFieldValidator;

public class ScheduleForStopController extends ApiActionSupport {

  private static final long serialVersionUID = 1L;

  private static final int V2 = 2;

  @Autowired
  private TransitDataService _service;

  private String _id;

  private Date _date = new Date();

  public ScheduleForStopController() {
    super(V2);
  }

  @RequiredFieldValidator
  public void setId(String id) {
    _id = id;
  }

  public String getId() {
    return _id;
  }

  @TypeConversion(converter = "org.onebusaway.presentation.impl.conversion.DateConverter")
  public void setDate(Date date) {
    _date = date;
  }

  public DefaultHttpHeaders show() throws ServiceException {

    if (hasErrors())
      return setValidationErrorsResponse();

    StopScheduleBean stopSchedule = _service.getScheduleForStop(_id, _date);

    BeanFactoryV2 factory = getBeanFactoryV2();
    return setOkResponse(factory.getResponse(stopSchedule));
  }
}
