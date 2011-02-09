package org.onebusaway.api.actions.api.where;

import java.io.IOException;
import java.util.List;

import org.apache.struts2.rest.DefaultHttpHeaders;
import org.onebusaway.api.actions.api.ApiActionSupport;
import org.onebusaway.api.impl.MaxCountSupport;
import org.onebusaway.api.model.transit.BeanFactoryV2;
import org.onebusaway.exceptions.ServiceException;
import org.onebusaway.transit_data.model.AgencyWithCoverageBean;
import org.onebusaway.transit_data.services.TransitDataService;
import org.springframework.beans.factory.annotation.Autowired;

public class AgenciesWithCoverageController extends ApiActionSupport {

  private static final long serialVersionUID = 1L;

  private static final int V1 = 1;

  private static final int V2 = 2;

  private MaxCountSupport _maxCount = new MaxCountSupport();

  @Autowired
  private TransitDataService _service;

  public AgenciesWithCoverageController() {
    super(V1);
  }

  public void setMaxCount(int maxCount) {
    _maxCount.setMaxCount(maxCount);
  }

  public DefaultHttpHeaders index() throws IOException, ServiceException {

    if (hasErrors())
      return setValidationErrorsResponse();

    List<AgencyWithCoverageBean> beans = _service.getAgenciesWithCoverage();

    if (isVersion(V1)) {
      return setOkResponse(beans);
    } else if (isVersion(V2)) {
      BeanFactoryV2 factory = getBeanFactoryV2();
      return setOkResponse(factory.getResponse(beans));
    } else {
      return setUnknownVersionResponse();
    }
  }
}
