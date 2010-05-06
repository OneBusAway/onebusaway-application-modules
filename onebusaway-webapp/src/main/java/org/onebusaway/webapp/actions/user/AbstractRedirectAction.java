package org.onebusaway.webapp.actions.user;

import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;

import com.opensymphony.xwork2.ActionSupport;

@Results( {@Result(type = "redirectAction", params = {"actionName", "index"})})
public class AbstractRedirectAction extends ActionSupport {

  private static final long serialVersionUID = 1L;

}
