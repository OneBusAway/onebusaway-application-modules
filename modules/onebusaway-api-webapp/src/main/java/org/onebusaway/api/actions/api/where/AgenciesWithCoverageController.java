package org.onebusaway.api.actions.api.where;

import org.onebusaway.api.actions.api.ApiActionSupport;
import org.onebusaway.exceptions.ServiceException;
import org.onebusaway.transit_data.model.AgencyWithCoverageBean;
import org.onebusaway.transit_data.services.TransitDataService;

import org.apache.struts2.rest.DefaultHttpHeaders;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.List;

public class AgenciesWithCoverageController extends ApiActionSupport {

  private static final long serialVersionUID = 1L;

  private static final String VERSION = "1.0";

  @Autowired
  private TransitDataService _service;

  public AgenciesWithCoverageController() {
    super(VERSION);
  }

  public DefaultHttpHeaders index() throws IOException, ServiceException {

    if (hasErrors())
      return setValidationErrorsResponse();
    
    List<AgencyWithCoverageBean> beans = _service.getAgenciesWithCoverage();
    return setOkResponse(beans);
  }

}
