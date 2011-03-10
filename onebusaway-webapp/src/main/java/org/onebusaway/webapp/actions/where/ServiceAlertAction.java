package org.onebusaway.webapp.actions.where;

import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Actions;
import org.onebusaway.transit_data.model.service_alerts.NaturalLanguageStringBean;
import org.onebusaway.transit_data.model.service_alerts.SituationBean;
import org.onebusaway.transit_data.services.TransitDataService;
import org.onebusaway.users.services.CurrentUserService;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.ActionSupport;
import com.opensymphony.xwork2.validator.annotations.RequiredStringValidator;

public class ServiceAlertAction extends ActionSupport {

  private static final long serialVersionUID = 1L;

  private TransitDataService _transitDataService;

  private CurrentUserService _currentUserService;

  private String _id;

  private SituationBean _situation;

  @Autowired
  public void setTransitDataService(TransitDataService transitDataService) {
    _transitDataService = transitDataService;
  }

  @Autowired
  public void setCurrentUserService(CurrentUserService currentUserService) {
    _currentUserService = currentUserService;
  }

  @RequiredStringValidator(key = "requiredField")
  public void setId(String id) {
    _id = id;
  }

  public String getId() {
    return _id;
  }

  public SituationBean getSituation() {
    return _situation;
  }

  @Override
  @Actions({
      @Action(value = "/where/standard/service-alert"),
      @Action(value = "/where/iphone/service-alert"),
      @Action(value = "/where/text/service-alert")})
  public String execute() {
    
    _situation = _transitDataService.getServiceAlertForId(_id);
    if (_situation == null)
      throw new NoSuchElementException();
    
    NaturalLanguageStringBean desc = _situation.getDescription();
    
    if (desc != null && desc.getValue() != null) {
      String value = desc.getValue();
      value = htmlify(value);
      desc.setValue(value);
    }

    _currentUserService.markServiceAlertAsRead(_situation.getId(),
        System.currentTimeMillis(), true);

    return SUCCESS;
  }

  private String htmlify(String content) {

    content = content.replaceAll("\r\n", "<br/>");
    content = content.replaceAll("\n", "<br/>");

    Pattern p = Pattern.compile("(http://[^\\s]+)");
    Matcher m = p.matcher(content);
    StringBuffer sb = new StringBuffer();
    while (m.find()) {
      m.appendReplacement(sb, "<a href=\"" + m.group(1) + "\">" + m.group(1)
          + "</a> ");
    }
    m.appendTail(sb);

    content = sb.toString();

    return content;
  }
}
