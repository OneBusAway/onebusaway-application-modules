package org.onebusaway.webapp.actions.sms;

import java.util.List;

import org.onebusaway.geocoder.model.GeocoderResult;
import org.onebusaway.geocoder.model.GeocoderResults;
import org.onebusaway.presentation.impl.GeocoderResultPresentationServiceImpl;
import org.onebusaway.presentation.services.GeocoderResultPresentationService;
import org.onebusaway.presentation.services.SetUserDefaultSearchFromGeocoderService;
import org.springframework.beans.factory.annotation.Autowired;

public class SetDefaultSearchLocationAction extends AbstractTextmarksAction {

  private static final long serialVersionUID = 1L;

  private GeocoderResultPresentationService _geocoderResultPresentation = new GeocoderResultPresentationServiceImpl();

  private SetUserDefaultSearchFromGeocoderService _service;

  private List<GeocoderResult> _records;

  @Autowired
  public void setService(SetUserDefaultSearchFromGeocoderService service) {
    _service = service;
  }

  public List<GeocoderResult> getRecords() {
    return _records;
  }

  @Override
  public String execute() {

    if (_text == null || _text.length() == 0)
      return INPUT;

    GeocoderResults results = _service.setUserDefaultSearchFromGeocoderService(_text);
    _records = results.getResults();

    if (_records.isEmpty()) {
      return "noRecords";
    } else if (_records.size() > 1) {
      _session.put(TextmarksActionConstants.SESSION_KEY_GEOCODER_RESULTS,
          _records);
      return "multipleRecords";
    }

    return getNextActionOrSuccess();
  }

  public String getGeocoderResultAsString(GeocoderResult result) {
    return _geocoderResultPresentation.getGeocoderResultAsString(result);
  }
}
