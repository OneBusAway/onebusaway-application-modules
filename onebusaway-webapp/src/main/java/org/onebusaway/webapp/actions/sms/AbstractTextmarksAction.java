package org.onebusaway.webapp.actions.sms;

import org.apache.struts2.convention.annotation.InterceptorRef;
import org.apache.struts2.convention.annotation.InterceptorRefs;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.onebusaway.webapp.actions.AbstractAction;

@InterceptorRefs( {@InterceptorRef("onebusaway-webapp-textmarks-stack")})
@Results( {
    @Result(type = "chain", name = "stop-by-number", location = "stop-by-number"),
    @Result(type = "chain", name = "arrivals-and-departures", location = "arrivals-and-departures"),
    @Result(type = "chain", name = "command", location = "command"),
    @Result(type = "chain", name = "handle-multi-selection", location = "handle-multi-selection"),
    @Result(type = "chain", name = "query-default-search-location", location = "query-default-search-location"),
    @Result(type = "chain", name = "set-default-search-location", location = "set-default-search-location")})
public class AbstractTextmarksAction extends AbstractAction {

  private static final long serialVersionUID = 1L;

  protected String _text;

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
