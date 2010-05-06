/*
 * Copyright 2008 Brian Ferris
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.onebusaway.where.web.actions;

import com.opensymphony.xwork2.ActionSupport;

import org.onebusaway.common.web.common.client.rpc.ServiceException;
import org.onebusaway.where.impl.StopTimePredictionServiceImpl;
import org.onebusaway.where.model.TimepointPrediction;
import org.onebusaway.where.web.common.client.model.DepartureBean;
import org.onebusaway.where.web.common.client.model.StopWithArrivalsBean;
import org.onebusaway.where.web.common.client.rpc.WhereService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class StopByNumberDebugAction extends ActionSupport {

  private static final long serialVersionUID = 1L;

  @Autowired
  private WhereService _service;

  @Autowired
  private StopTimePredictionServiceImpl _stopTimePredictionServiceImpl;

  private String _id;

  private StopWithArrivalsBean _result;

  private List<TimepointPrediction> _predictions = new ArrayList<TimepointPrediction>();

  public void setId(String id) {
    _id = id;
  }

  public StopWithArrivalsBean getResult() {
    return _result;
  }

  public List<TimepointPrediction> getPredictions() {
    return _predictions;
  }

  @Override
  public String execute() throws ServiceException {

    _result = _service.getArrivalsByStopId(_id);

    for (DepartureBean bean : _result.getPredictedArrivals()) {
      String tripId = bean.getTripId();
      List<TimepointPrediction> predictions = _stopTimePredictionServiceImpl.getCachedTripEntry(tripId);
      if (predictions != null)
        _predictions.addAll(predictions);
    }
    return SUCCESS;
  }

  public Date getTimeAsDate(long time) {
    return new Date(time);
  }
}
