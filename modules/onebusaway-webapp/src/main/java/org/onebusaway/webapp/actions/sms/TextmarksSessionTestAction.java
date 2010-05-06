package org.onebusaway.webapp.actions.sms;

import com.opensymphony.xwork2.ActionSupport;

import org.apache.struts2.interceptor.ServletRequestAware;
import org.apache.struts2.interceptor.SessionAware;

import java.util.Date;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class TextmarksSessionTestAction extends ActionSupport implements
    SessionAware, ServletRequestAware {

  private static final long serialVersionUID = 1L;

  private static final String KEY = TextmarksSessionTestAction.class.getName()
      + ".key";

  private Map<String, Object> _session;

  private HttpServletRequest _request;

  public void setSession(Map<String, Object> session) {
    _session = session;
  }

  public void setServletRequest(HttpServletRequest request) {
    _request = request;
  }

  @Override
  public String execute() {

    HttpSession session = _request.getSession();
    System.out.println("==== id=" + session.getId());
    System.out.println("  sessionId=" + _request.getRequestedSessionId() + " " + _request.isRequestedSessionIdFromCookie() + " " + _request.isRequestedSessionIdFromURL());
    String value = (String) _session.get(KEY);

    if (value != null)
      System.out.println("  value=" + value);
    else
      System.out.println("  no value");

    value = "request=" + new Date();
    _session.put(KEY, value);

    return SUCCESS;
  }

}
