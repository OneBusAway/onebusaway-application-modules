package org.onebusaway.api.actions.api.where;

import java.util.Date;

import org.onebusaway.api.actions.api.ApiActionSupport;
import org.onebusaway.api.model.TimeBean;
import org.onebusaway.utility.DateLibrary;

import org.apache.struts2.rest.DefaultHttpHeaders;

public class CurrentTimeController extends ApiActionSupport {

  private static final long serialVersionUID = 1L;

  private static final int V1 = 1;

  public CurrentTimeController() {
    super(V1);
  }

  public DefaultHttpHeaders index() {
    
    if( ! isVersion(V1))
      return setUnknownVersionResponse();
    
    Date date = new Date();
    String readableTime = DateLibrary.getTimeAsIso8601String(date);
    TimeBean time = new TimeBean(date,readableTime);
    return setOkResponse(time);
  }
}
