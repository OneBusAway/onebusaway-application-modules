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

import org.apache.struts2.rest.DefaultHttpHeaders;
import org.onebusaway.api.actions.api.ApiActionSupport;
import org.onebusaway.api.impl.TripsConfigurationServiceClientFileImpl;
import org.onebusaway.api.model.transit.BeanFactoryV2;
import org.onebusaway.api.services.TripsConfigurationServiceClient;
import org.onebusaway.exceptions.ServiceException;
import org.onebusaway.transit_data.model.StopsForRouteBean;
import org.onebusaway.transit_data.services.TransitDataService;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.validator.annotations.RequiredFieldValidator;

public class StopsForRouteAction extends ApiActionSupport {

  private static final long serialVersionUID = 1L;

  private static final int V1 = 1;

  private static final int V2 = 2;

  @Autowired
  private TransitDataService _service;

  private String _id;

  private boolean _includePolylines = true;
  
  @Autowired
  private TripsConfigurationServiceClientFileImpl tConfigurationServiceClientFileImpl;

  public StopsForRouteAction() {
    super(LegacyV1ApiSupport.isDefaultToV1() ? V1 : V2);
  }

  @RequiredFieldValidator
  public void setId(String id) {
    _id = id;
  }

  public String getId() {
    return _id;
  }
  
  
  public void setIncludePolylines(boolean includePolylines) {
    _includePolylines = includePolylines;
  }


  public DefaultHttpHeaders show() throws ServiceException {

    if (hasErrors())
      return setValidationErrorsResponse();

    StopsForRouteBean result = _service.getStopsForRoute(_id);
    
    for(int i = 0; i < result.getStopGroupings().size(); i++) {
    	for(int j = 0; j < result.getStopGroupings().get(i).getStopGroups().size(); j++) {
    		try {
				String new_trip = tConfigurationServiceClientFileImpl.getItem("trip", _id, 
						result.getStopGroupings().get(i).getStopGroups().get(j).getId(), 
						result.getStopGroupings().get(i).getStopGroups().get(j).getName().getName());
				if(new_trip != null)
					result.getStopGroupings().get(i).getStopGroups().get(j).getName().setName(new_trip);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    }

    if (result == null)
      return setResourceNotFoundResponse();

    if (isVersion(V1)) {
      return setOkResponse(result);
    } else if (isVersion(V2)) {
      BeanFactoryV2 factory = getBeanFactoryV2();
      return setOkResponse(factory.getResponse(result,_includePolylines));
    } else {
      return setUnknownVersionResponse();
    }
  }
}
