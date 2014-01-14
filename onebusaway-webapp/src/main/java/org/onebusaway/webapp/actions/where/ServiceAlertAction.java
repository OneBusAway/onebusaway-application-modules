/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onebusaway.webapp.actions.where;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Actions;
import org.onebusaway.collections.CollectionsLibrary;
import org.onebusaway.transit_data.model.service_alerts.NaturalLanguageStringBean;
import org.onebusaway.transit_data.model.service_alerts.ServiceAlertBean;
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

  private ServiceAlertBean _situation;

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

  public ServiceAlertBean getSituation() {
    return _situation;
  }
  
  public String getNLSValue(List<NaturalLanguageStringBean> values){
    if( values == null || values.isEmpty() ) {
      return null;
    }
    return values.get(0).getValue();
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

    if (!CollectionsLibrary.isEmpty(_situation.getDescriptions())) {
      for (NaturalLanguageStringBean desc : _situation.getDescriptions()) {
        if (desc.getValue() != null) {
          String value = desc.getValue();
          value = htmlify(value);
          desc.setValue(value);
        }
      }
    }

    _currentUserService.markServiceAlertAsRead(_situation.getId(),
        System.currentTimeMillis(), true);

    return SUCCESS;
  }

  private String htmlify(String content) {

    Pattern p = Pattern.compile("(http://[^\\s]+)");
    Matcher m = p.matcher(content);
    StringBuffer sb = new StringBuffer();
    while (m.find()) {
      m.appendReplacement(sb, "<a href=\"" + m.group(1) + "\">" + m.group(1)
          + "</a> ");
    }
    m.appendTail(sb);

    content = sb.toString();

    content = content.replaceAll("\r\n", "<br/>");
    content = content.replaceAll("\n", "<br/>");

    return content;
  }
}
