/**
 * Copyright (C) 2023 Cambridge Systematics, Inc.
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
package org.onebusaway.api.actions.api.where.search;


import org.apache.struts2.rest.DefaultHttpHeaders;
import org.onebusaway.api.model.transit.BeanFactoryV2;
import org.onebusaway.api.model.transit.RouteSearchResultBean;
import org.onebusaway.exceptions.ServiceException;
import org.onebusaway.transit_data.model.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

/**
 * Search for a route based on its name.  Accepts partial input
 * as used by autocomplete controls.
 */
public class RouteAction extends ApiSearchAction {

  private ArrivalsAndDeparturesQueryBean _query = new ArrivalsAndDeparturesQueryBean();

  @Autowired
  private RouteSorting customRouteSort;

  public RouteAction() {
    super(V2);
  }

  public DefaultHttpHeaders index() throws IOException, ServiceException {
    if (isVersion(V2)) {
      ListBean<RouteBean> routeSuggestions = _service.getRouteSuggestions(null, _input, maxCount);
      if (routeSuggestions == null || routeSuggestions.getList().isEmpty())
        return setResourceNotFoundResponse();

      BeanFactoryV2 factory = getBeanFactoryV2();
      RouteSearchResultBean result = new RouteSearchResultBean();
      result.setSuggestions(routeSuggestions);
      factory.setCustomRouteSort(customRouteSort);
      return setOkResponse(factory.getResponse(result));
    } else {
      return setUnknownVersionResponse();
    }
  }
}