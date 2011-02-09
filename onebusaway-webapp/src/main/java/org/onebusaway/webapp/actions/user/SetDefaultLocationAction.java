package org.onebusaway.webapp.actions.user;

import org.onebusaway.presentation.services.DefaultSearchLocationService;
import org.springframework.beans.factory.annotation.Autowired;

public class SetDefaultLocationAction extends AbstractRedirectAction {

  private static final long serialVersionUID = 1L;

  private DefaultSearchLocationService _defaultSearchLocationService;

  private String _name;

  private double _lat;

  private double _lon;

  public void setName(String name) {
    _name = name;
  }

  public void setLat(double lat) {
    _lat = lat;
  }

  public void setLon(double lon) {
    _lon = lon;
  }

  @Autowired
  public void setDefaultSearchLocationService(
      DefaultSearchLocationService defaultSearchLocationService) {
    _defaultSearchLocationService = defaultSearchLocationService;
  }

  @Override
  public String execute() {

    if (_name == null || _name.length() == 0)
      return INPUT;

    _defaultSearchLocationService.setDefaultLocationForCurrentUser(_name, _lat,
        _lon);

    return SUCCESS;
  }
}
