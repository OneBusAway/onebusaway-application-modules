package org.onebusaway.webapp.actions;

import javax.servlet.http.HttpServletResponse;

import org.apache.struts2.ServletActionContext;

import com.opensymphony.xwork2.ActionSupport;

public class ResourceNotFoundAction extends ActionSupport {

  private static final long serialVersionUID = 1L;

  private int _status = HttpServletResponse.SC_NOT_FOUND;

  public void setStatus(int status) {
    _status = status;
  }

  @Override
  public String execute() {
    HttpServletResponse response = ServletActionContext.getResponse();
    response.setStatus(_status);
    return SUCCESS;
  }
}
