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
import org.onebusaway.api.model.transit.StopSearchResultBean;
import org.onebusaway.exceptions.ServiceException;
import org.onebusaway.transit_data.model.ArrivalsAndDeparturesQueryBean;
import org.onebusaway.transit_data.model.ListBean;
import org.onebusaway.transit_data.model.RouteSorting;
import org.onebusaway.transit_data.model.StopBean;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Search for a stop based on its name.  Accepts partial input
 * as used by autocomplete controls.
 */
public class StopAction extends ApiSearchAction {
  private ArrivalsAndDeparturesQueryBean _query = new ArrivalsAndDeparturesQueryBean();
  private List<Integer> routeTypesToBeFiltered = Arrays.asList(711,712,713,714);

  public void setRouteTypesToBeFiltered(List<Integer> routeTypesToBeFiltered) {
    this.routeTypesToBeFiltered = routeTypesToBeFiltered;
  }

  @Autowired
  private RouteSorting customRouteSort;

  public StopAction() {
    super(V2);
  }


  public DefaultHttpHeaders index() throws IOException, ServiceException {
    if (isVersion(V2)) {
      ListBean<StopBean> stopSuggestions = _service.getStopSuggestions(null, _input, maxCount);
      if (stopSuggestions == null || stopSuggestions.getList().isEmpty())
        return setResourceNotFoundResponse();

      BeanFactoryV2 factory = getBeanFactoryV2();
      StopSearchResultBean result = new StopSearchResultBean();
      List<StopBean> filteredStopSuggestions = stopSuggestions.getList().stream()
              .filter(stopBean -> stopBean.getRoutes() != null &&
                      stopBean.getRoutes().size() > 1 || stopBean.getRoutes().size() == 1 &&
                      stopBean.getRoutes().stream().noneMatch(r -> routeTypesToBeFiltered.contains(r.getType())))
              .collect(Collectors.toList());
      stopSuggestions.setList(filteredStopSuggestions);
      result.setStopSuggestions(stopSuggestions);
      factory.setCustomRouteSort(customRouteSort);
      return setOkResponse(factory.getResponse(result));
    } else {
      return setUnknownVersionResponse();
    }
  }
}
