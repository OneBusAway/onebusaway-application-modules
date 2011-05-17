package org.onebusaway.api.actions;

import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.onebusaway.api.actions.api.ApiKeyAuthorization;

@ApiKeyAuthorization(enabled = false)
@Results({@Result(name = "success-xml", location = "crossdomain.xml", type = "xml")})
public class CrossdomainAction extends OneBusAwayApiActionSupport {

  private static final long serialVersionUID = 1L;

  public String index() {
    return SUCCESS;
  }

}
