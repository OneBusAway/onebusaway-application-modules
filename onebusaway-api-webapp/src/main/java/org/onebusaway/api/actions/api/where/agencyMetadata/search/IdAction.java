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
package org.onebusaway.api.actions.api.where.agencyMetadata.search;

import java.io.IOException;
import java.util.List;

import org.apache.struts2.rest.DefaultHttpHeaders;
import org.onebusaway.agency_metadata.model.AgencyMetadata;
import org.onebusaway.agency_metadata.service.AgencyMetadataService;
import org.onebusaway.api.actions.api.ApiActionSupport;
import org.onebusaway.api.actions.api.where.Messages;
import org.onebusaway.exceptions.ServiceException;
import org.onebusaway.transit_data.services.TransitDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.validator.annotations.RequiredFieldValidator;

public class IdAction extends ApiActionSupport {

	  private static Logger _log = LoggerFactory.getLogger(IdAction.class);
	  private static final int V2 = 2;
	  private String id;
	  
	  public IdAction() {
	    super(V2);
	  }
	  
	  public IdAction(int defaultVersion) {
	    super(defaultVersion);
	  }

	  private static final long serialVersionUID = 1L;
	  
	  @Autowired
	  private TransitDataService _service;
	  
	  @Autowired
	  private AgencyMetadataService _agencyMetadataService;
	  
	  @RequiredFieldValidator(message = Messages.MISSING_REQUIRED_FIELD)
	  public void setId(String id) {
	    this.id = id;
	  }

	  public String getId() {
	    return id;
	  }

	  
	  public DefaultHttpHeaders show() throws IOException, ServiceException {
	    if (hasErrors())
	      return setValidationErrorsResponse();

	    List<AgencyMetadata> agencyMetadata =  _agencyMetadataService.getAgencyMetadataForId(id);
	    
	    return setOkResponse(agencyMetadata);
	  }
}
