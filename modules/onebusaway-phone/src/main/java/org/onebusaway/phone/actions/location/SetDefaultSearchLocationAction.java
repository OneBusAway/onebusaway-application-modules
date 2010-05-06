package org.onebusaway.phone.actions.location;

import org.onebusaway.geocoder.model.GeocoderResult;
import org.onebusaway.geocoder.model.GeocoderResults;
import org.onebusaway.presentation.services.SetUserDefaultSearchFromGeocoderService;

import com.opensymphony.xwork2.ActionSupport;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class SetDefaultSearchLocationAction extends ActionSupport implements
    LocationActionConstants {

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
  public String execute() {

    GeocoderResults results = _service.setUserDefaultSearchFromGeocoderService(_location);
    _records = results.getResults();

    if (_records.isEmpty()) {
      return "noRecords";
    } else if (_records.size() > 1) {
      return "multipleRecords";
    } else {
      return SUCCESS;
    }
  }
}
