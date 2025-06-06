/**
 * Copyright (C) 2025 Aaron Brethorst <aaron@onebusaway.org>
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
import org.onebusaway.api.model.transit.BeanFactoryV2;
import org.onebusaway.transit_data.model.MetricsBean;
import org.onebusaway.transit_data.services.TransitDataService;
import org.springframework.beans.factory.annotation.Autowired;

public class MetricsAction extends ApiActionSupport {

  private static final long serialVersionUID = 1L;

  private static final int V2 = 2;

  @Autowired
  private TransitDataService _service;

  public MetricsAction() {
    super(V2);
  }

  public DefaultHttpHeaders index() {

    if (hasErrors())
      return setValidationErrorsResponse();

    if( ! isVersion(V2))
      return setUnknownVersionResponse();

    MetricsBean metrics = _service.getMetrics();
    BeanFactoryV2 factory = getBeanFactoryV2();
    return setOkResponse(factory.getResponse(metrics));
  }
}
