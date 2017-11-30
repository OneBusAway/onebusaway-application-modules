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
package org.onebusaway.enterprise.webapp.actions.where;

import java.util.Collections;
import java.util.List;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Actions;
import org.onebusaway.exceptions.ServiceException;
import org.onebusaway.presentation.impl.AgencyWithCoverageBeanComparator;
import org.onebusaway.transit_data.model.AgencyWithCoverageBean;
import org.onebusaway.transit_data.services.TransitDataService;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.ActionSupport;
import com.opensymphony.xwork2.ModelDriven;

public class AgenciesAction extends ActionSupport implements
    ModelDriven<List<AgencyWithCoverageBean>> {

  private static final long serialVersionUID = 1L;

  private TransitDataService _transitDataService;

  private List<AgencyWithCoverageBean> _model;

  @Autowired
  public void setTransitDataService(TransitDataService transitDataService) {
    _transitDataService = transitDataService;
  }

  @Override
  public List<AgencyWithCoverageBean> getModel() {
    return _model;
  }

  @Override
  @Actions({
      @Action(value = "/where/iphone/agencies")
  })
  public String execute() throws ServiceException {
    _model = _transitDataService.getAgenciesWithCoverage();
    Collections.sort(_model, new AgencyWithCoverageBeanComparator());
    return SUCCESS;
  }

}
