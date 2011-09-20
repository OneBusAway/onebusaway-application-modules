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
package org.onebusaway.api.actions.api.where;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.struts2.rest.DefaultHttpHeaders;
import org.onebusaway.api.actions.api.ApiActionSupport;
import org.onebusaway.api.model.transit.BeanFactoryV2;
import org.onebusaway.collections.Max;
import org.onebusaway.exceptions.ServiceException;
import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.presentation.impl.StackInterceptor.AddToStack;
import org.onebusaway.transit_data.model.ListBean;
import org.onebusaway.transit_data.model.realtime.CurrentVehicleEstimateBean;
import org.onebusaway.transit_data.model.realtime.CurrentVehicleEstimateQueryBean;
import org.onebusaway.transit_data.model.realtime.CurrentVehicleEstimateQueryBean.Record;
import org.onebusaway.transit_data.services.TransitDataService;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.validator.annotations.RequiredFieldValidator;

@AddToStack("query")
public class EstimateCurrentVehicleAction extends ApiActionSupport {

  private static final long serialVersionUID = 1L;

  private static final int V2 = 2;

  @Autowired
  private TransitDataService _service;

  private CurrentVehicleEstimateQueryBean _query = new CurrentVehicleEstimateQueryBean();

  private String _data;

  public EstimateCurrentVehicleAction() {
    super(V2);
  }

  public CurrentVehicleEstimateQueryBean getQuery() {
    return _query;
  }

  public void setQuery(CurrentVehicleEstimateQueryBean query) {
    _query = query;
  }

  @RequiredFieldValidator(message = Messages.MISSING_REQUIRED_FIELD)
  public void setData(String data) {
    _data = data;
  }

  public String getData() {
    return _data;
  }

  public DefaultHttpHeaders index() throws IOException, ServiceException {

    if (!isVersion(V2))
      return setUnknownVersionResponse();

    if (hasErrors())
      return setValidationErrorsResponse();

    fillInQuery();

    if (hasErrors())
      return setValidationErrorsResponse();

    BeanFactoryV2 factory = getBeanFactoryV2();

    ListBean<CurrentVehicleEstimateBean> estimates = _service.getCurrentVehicleEstimates(_query);
    return setOkResponse(factory.getCurrentVehicleEstimates(estimates));
  }

  private void fillInQuery() {

    List<CurrentVehicleEstimateQueryBean.Record> records = new ArrayList<CurrentVehicleEstimateQueryBean.Record>();
    
    Max<Record> max = new Max<Record>(); 

    for (String record : _data.split("\\|")) {

      String[] tokens = record.split(",");

      if (tokens.length != 4) {
        addFieldError("data", Messages.INVALID_FIELD_VALUE);
        return;
      }

      try {
        long t = Long.parseLong(tokens[0]);
        double lat = Double.parseDouble(tokens[1]);
        double lon = Double.parseDouble(tokens[2]);
        double accuracy = Double.parseDouble(tokens[3]);

        Record r = new Record();
        r.setTimestamp(t);
        r.setLocation(new CoordinatePoint(lat, lon));
        r.setAccuracy(accuracy);
        records.add(r);
        
        max.add(t, r);

      } catch (NumberFormatException ex) {
        addFieldError("data", Messages.INVALID_FIELD_VALUE);
        return;
      }
    }

    _query.setRecords(records);

    if (records.isEmpty()) {
      addFieldError("data", Messages.INVALID_FIELD_VALUE);
      return;
    }
    
    _query.setMostRecentLocation(max.getMaxElement().getLocation());
  }
}
