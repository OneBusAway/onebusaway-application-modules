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
package org.onebusaway.phone.actions.settings;

import java.util.List;

import org.onebusaway.geocoder.model.GeocoderResult;
import org.onebusaway.geocoder.model.GeocoderResults;
import org.onebusaway.presentation.services.SetUserDefaultSearchFromGeocoderService;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.ActionSupport;

public class SetDefaultSearchLocationAction extends ActionSupport {

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
