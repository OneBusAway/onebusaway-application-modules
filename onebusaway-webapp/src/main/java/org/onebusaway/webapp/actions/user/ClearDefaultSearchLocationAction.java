package org.onebusaway.webapp.actions.user;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Actions;
import org.onebusaway.presentation.services.DefaultSearchLocationService;
import org.springframework.beans.factory.annotation.Autowired;

public class ClearDefaultSearchLocationAction extends AbstractRedirectAction {

  private static final long serialVersionUID = 1L;

  private DefaultSearchLocationService _defaultSearchLocationService;

  @Autowired
  public void setDefaultSearchLocationService(
      DefaultSearchLocationService defaultSearchLocationService) {
    _defaultSearchLocationService = defaultSearchLocationService;
  }

  @Override
  @Actions( {
    @Action(value = "/user/clear-default-search-location"),
    @Action(value = "/where/iphone/user/clear-default-search-location"),
    @Action(value = "/where/text/user/clear-default-search-location")})
  public String execute() {
    _defaultSearchLocationService.clearDefaultLocationForCurrentUser();
    return SUCCESS;
  }
}
