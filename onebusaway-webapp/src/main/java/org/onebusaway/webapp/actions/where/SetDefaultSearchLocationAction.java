package org.onebusaway.webapp.actions.where;

import java.util.List;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Actions;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.onebusaway.geocoder.model.GeocoderResult;
import org.onebusaway.geocoder.model.GeocoderResults;
import org.onebusaway.presentation.services.SetUserDefaultSearchFromGeocoderService;
import org.springframework.beans.factory.annotation.Autowired;

@Results( {@Result(type = "redirectAction", params = {"actionName", "index"})})
public class SetDefaultSearchLocationAction extends AbstractWhereAction {

  private static final long serialVersionUID = 1L;

  private SetUserDefaultSearchFromGeocoderService _service;

  private String _location;

  private List<GeocoderResult> _records;

  @Autowired
  public void setService(SetUserDefaultSearchFromGeocoderService service) {
    _service = service;
  }

  public void setLocation(String location) {
    _location = location;
  }

  public List<GeocoderResult> getRecords() {
    return _records;
  }

  @Override
  @Actions( {
      @Action(value = "/where/standard/set-default-search-location"),
      @Action(value = "/where/iphone/set-default-search-location"),
      @Action(value = "/where/text/set-default-search-location")})
  public String execute() {

    if (_location == null || _location.length() == 0)
      return INPUT;

    GeocoderResults results = _service.setUserDefaultSearchFromGeocoderService(_location);
    _records = results.getResults();

    if (_records.isEmpty()) {
      return "noRecords";
    } else if (_records.size() > 1) {
      return "multipleRecords";
    }

    return getNextActionOrSuccess();
  }
}
