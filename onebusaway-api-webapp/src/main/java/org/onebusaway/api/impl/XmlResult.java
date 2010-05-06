package org.onebusaway.api.impl;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts2.ServletActionContext;
import org.apache.struts2.dispatcher.StrutsResultSupport;

import com.opensymphony.xwork2.ActionInvocation;

public class XmlResult extends StrutsResultSupport {

  private static final long serialVersionUID = 1L;

  @Override
  protected void doExecute(String finalLocation, ActionInvocation invocation)
      throws Exception {

    HttpServletRequest request = ServletActionContext.getRequest();
    HttpServletResponse response = ServletActionContext.getResponse();
    RequestDispatcher dispatcher = request.getRequestDispatcher(finalLocation);
    response.setContentType("text/xml");
    dispatcher.include(request, response);
  }

}
