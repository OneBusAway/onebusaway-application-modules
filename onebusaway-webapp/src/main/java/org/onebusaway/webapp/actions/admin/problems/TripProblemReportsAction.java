/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
 * Copyright (C) 2012 Google, Inc.
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
package org.onebusaway.webapp.actions.admin.problems;

import java.util.List;

import org.onebusaway.transit_data.model.problems.TripProblemReportBean;
import org.onebusaway.transit_data.model.problems.TripProblemReportQueryBean;
import org.onebusaway.transit_data.services.TransitDataService;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.ActionSupport;
import com.opensymphony.xwork2.ModelDriven;

public class TripProblemReportsAction extends ActionSupport implements
    ModelDriven<TripProblemReportQueryBean> {

  private static final long serialVersionUID = 1L;

  private TransitDataService _transitDataService;

  private List<TripProblemReportBean> _reports;

  private TripProblemReportQueryBean _model = new TripProblemReportQueryBean();

  @Autowired
  public void setTransitDataService(TransitDataService transitDataService) {
    _transitDataService = transitDataService;
  }

  @Override
  public TripProblemReportQueryBean getModel() {
    return _model;
  }

  public List<TripProblemReportBean> getReports() {
    return _reports;
  }

  @Override
  public String execute() {
    _reports = _transitDataService.getTripProblemReports(_model).getList();
    return SUCCESS;
  }
}
