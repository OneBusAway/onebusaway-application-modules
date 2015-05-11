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
package org.onebusaway.api.actions.api.where;

import java.io.IOException;

import org.apache.struts2.rest.DefaultHttpHeaders;
import org.onebusaway.api.actions.api.ApiActionSupport;
import org.onebusaway.api.model.transit.BeanFactoryV2;
import org.onebusaway.exceptions.ServiceException;
import org.onebusaway.transit_data.model.config.BundleMetadata;
import org.onebusaway.transit_data.services.TransitDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class ConfigAction extends ApiActionSupport {

  private static Logger _log = LoggerFactory.getLogger(ConfigAction.class);
  private static final int V2 = 2;
  
  public ConfigAction() {
    super(V2);
  }
  
  public ConfigAction(int defaultVersion) {
    super(defaultVersion);
  }

  private static final long serialVersionUID = 5104142427310052565L;
  
  @Autowired
  private TransitDataService _service;
  
  public String getId() {
    _log.error("in id!");
    return _service.getActiveBundleId();
  }
  
  public DefaultHttpHeaders index() throws IOException, ServiceException {
    if (hasErrors())
      return setValidationErrorsResponse();
    BundleMetadata meta = _service.getBundleMetadata();
    BeanFactoryV2 factory = getBeanFactoryV2();

    return setOkResponse(factory.getResponse(meta));

  }
}
