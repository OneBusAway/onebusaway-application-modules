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

    import com.opensymphony.xwork2.conversion.annotations.TypeConversion;
    import org.apache.struts2.rest.DefaultHttpHeaders;
    import org.onebusaway.api.actions.api.ApiActionSupport;
    import org.onebusaway.api.model.transit.BeanFactoryV2;
    import org.onebusaway.exceptions.ServiceException;
    import org.onebusaway.transit_data.model.StopsForRouteBean;
    import org.onebusaway.gtfs.model.calendar.ServiceDate;
    import org.onebusaway.transit_data.services.TransitDataService;
    import org.onebusaway.util.SystemTime;
    import org.onebusaway.util.services.configuration.ConfigurationService;
    import org.slf4j.Logger;
    import org.slf4j.LoggerFactory;
    import org.springframework.beans.factory.annotation.Autowired;

    import com.opensymphony.xwork2.validator.annotations.RequiredFieldValidator;

    import java.util.Date;

public class StopsForRouteAction extends ApiActionSupport {

  private static final long serialVersionUID = 1L;

  private static Logger _log = LoggerFactory.getLogger(StopsForRouteAction.class);

  private static final int V1 = 1;

  private static final int V2 = 2;

  @Autowired
  private TransitDataService _service;

  private String _id;

  private Date _date = null;


  @Autowired
  private ConfigurationService _configService;

  private boolean _includePolylines = true;

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

  @TypeConversion(converter = "org.onebusaway.presentation.impl.conversion.DateConverter")
  public void setDate(Date date) {
    _date = date;
  }

  public DefaultHttpHeaders show() throws ServiceException {

    if (hasErrors())
      return setValidationErrorsResponse();

    boolean serviceDateFilterOn = Boolean.parseBoolean(_configService.getConfigurationValueAsString("display.serviceDateFiltering", "false"));
    _log.info("serviceDateFilterOn=" + serviceDateFilterOn);
    if (serviceDateFilterOn && _date == null) {
      _date = new Date(System.currentTimeMillis());
    }
    StopsForRouteBean result;
    if (serviceDateFilterOn || _date != null) {
      _log.info("using serviceDate " + _date + " with id=" + _id);
      result = _service.getStopsForRouteForServiceDate(_id, new ServiceDate(_date));
    } else {
      _log.info("using all service with id=" + _id);
      result = _service.getStopsForRoute(_id);
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
