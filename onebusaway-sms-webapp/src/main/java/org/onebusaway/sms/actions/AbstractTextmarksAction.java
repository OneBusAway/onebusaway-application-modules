package org.onebusaway.sms.actions;

import org.apache.struts2.convention.annotation.InterceptorRef;
import org.apache.struts2.convention.annotation.InterceptorRefs;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.onebusaway.presentation.impl.NextActionSupport;
import org.onebusaway.transit_data.services.TransitDataService;
import org.onebusaway.users.services.CurrentUserService;
import org.springframework.beans.factory.annotation.Autowired;

@InterceptorRefs( {@InterceptorRef("onebusaway-sms-webapp-stack")})
@Results( {
    @Result(type = "chain", name = "stop-by-number", location = "stop-by-number"),
    @Result(type = "chain", name = "arrivals-and-departures", location = "arrivals-and-departures"),
    @Result(type = "chain", name = "command", location = "command"),
    @Result(type = "chain", name = "handle-multi-selection", location = "handle-multi-selection"),
    @Result(type = "chain", name = "query-default-search-location", location = "query-default-search-location"),
    @Result(type = "chain", name = "set-default-search-location", location = "set-default-search-location")})
public class AbstractTextmarksAction extends NextActionSupport {

  private static final long serialVersionUID = 1L;

  protected TransitDataService _transitDataService;

  protected CurrentUserService _currentUserService;

  protected String _text;

  @Autowired
  public void setTransitDataService(TransitDataService transitDataService) {
    _transitDataService = transitDataService;
  }

  @Autowired
  public void setCurrentUserService(CurrentUserService currentUserService) {
    _currentUserService = currentUserService;
  }

  public void setMessage(String message) {
    if (_text == null)
      _text = message.trim();
  }

  public void setText(String text) {
    _text = text;
  }

  public String getText() {
    return _text;
  }
}
