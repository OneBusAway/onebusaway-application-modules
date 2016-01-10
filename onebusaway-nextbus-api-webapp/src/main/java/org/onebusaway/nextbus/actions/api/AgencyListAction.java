/**
 * Copyright (C) 2015 Cambridge Systematics
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
package org.onebusaway.nextbus.actions.api;

import java.util.List;

import org.apache.struts2.rest.DefaultHttpHeaders;
import org.apache.struts2.rest.HttpHeaders;
import org.onebusaway.exceptions.ServiceException;
import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.nextbus.model.nextbus.Agency;
import org.onebusaway.nextbus.model.nextbus.Body;
import org.onebusaway.nextbus.model.nextbus.BodyError;
import org.onebusaway.nextbus.validation.ErrorMsg;
import org.onebusaway.transit_data.model.AgencyWithCoverageBean;
import org.onebusaway.transit_data.services.TransitDataService;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.ModelDriven;

public class AgencyListAction extends NextBusApiBase implements ModelDriven<Body<Agency>> {

  @Autowired
  private TransitDataService _service;

  public HttpHeaders index() {
    return new DefaultHttpHeaders("success");
  }

  @Override
  public Body<Agency> getModel() {

    Body<Agency> body = new Body<Agency>();
    try{
	    List<AgencyWithCoverageBean> agencies = _service.getAgenciesWithCoverage();
	    for (AgencyWithCoverageBean agencyBean : agencies) {
	      Agency agency = new Agency();
	      agency.setTag(agencyBean.getAgency().getId());
	      agency.setTitle(agencyBean.getAgency().getName());
	      agency.setRegionTitle(agencyBean.getAgency().getName());
	      body.getResponse().add(agency);
	    }
    }
    catch (ServiceException e){
    	body.getErrors().add(new BodyError(ErrorMsg.SERVICE_ERROR.getDescription()));
    }

    return body;

  }

}
