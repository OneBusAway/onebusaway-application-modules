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
package org.onebusaway.api.actions.api.where;

import com.opensymphony.xwork2.validator.annotations.RequiredFieldValidator;
import org.apache.struts2.rest.DefaultHttpHeaders;
import org.onebusaway.api.actions.api.ApiActionSupport;
import org.onebusaway.exceptions.ServiceException;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.calendar.ServiceDate;
import org.onebusaway.transit_data.model.ListBean;
import org.onebusaway.transit_data.model.RouteGroupingBean;
import org.onebusaway.transit_data.services.TransitDataService;
import org.onebusaway.util.AgencyAndIdLibrary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * route-details that provides canonical/idealized information on route shapes and stops.
 */
public class RouteDetailsAction extends ApiActionSupport {

  private static final int V2 = 2;

  private static Logger _log = LoggerFactory.getLogger(RouteDetailsAction.class);
  @Autowired
  private TransitDataService _service;

  private String _id;

  private long serviceDate = new ServiceDate().getAsDate().getTime();

  public RouteDetailsAction() {
    super(V2);
  }

  @RequiredFieldValidator
  public void setId(String id) {
    _id = id;
  }

  public String getId() {
    return _id;
  }

  public void setServiceDate(long serviceDate) {
    this.serviceDate = serviceDate;
  }

  public DefaultHttpHeaders show() throws ServiceException {

    if (hasErrors())
      return setValidationErrorsResponse();
    if (!isVersion(V2)) {
      return setUnknownVersionResponse();
    }

    AgencyAndId routeId = null;
    try {
      routeId = AgencyAndIdLibrary.convertFromString(_id);
    } catch (IllegalStateException ise) {
      return setExceptionResponse();
    }
    if (routeId == null)
      return setResourceNotFoundResponse();

    // we allow for the possibility of multiple results
    ListBean<RouteGroupingBean> beans = null;
    try {
      beans = _service.getCanonicalRoute(serviceDate, routeId);
    } catch (Throwable t) {
      _log.error("exception for canonical data ", t, t);
      return setExceptionResponse();
    }

    return setOkResponse(getBeanFactoryV2().getResponse(beans));
  }
}
