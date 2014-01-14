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
package org.onebusaway.sms.actions.sms;

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
