package org.onebusaway.api.actions;

import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.onebusaway.api.actions.api.ApiKeyAuthorization;

import com.opensymphony.xwork2.ActionSupport;

@ApiKeyAuthorization(enabled = false)
@Results( {@Result(name = "success-xml", location = "crossdomain.xml", type = "xml")})
public class CrossdomainController extends ActionSupport {

  private static final long serialVersionUID = 1L;

  public String index() {
    return SUCCESS;
  }

}
