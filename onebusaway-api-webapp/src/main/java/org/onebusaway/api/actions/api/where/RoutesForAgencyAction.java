package org.onebusaway.api.actions.api.where;

import java.util.ArrayList;
import java.util.List;

import org.apache.struts2.rest.DefaultHttpHeaders;
import org.onebusaway.api.actions.api.ApiActionSupport;
import org.onebusaway.api.model.transit.BeanFactoryV2;
import org.onebusaway.api.model.transit.RouteV2Bean;
import org.onebusaway.transit_data.model.ListBean;
import org.onebusaway.transit_data.model.RouteBean;
import org.onebusaway.transit_data.services.TransitDataService;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.validator.annotations.RequiredFieldValidator;

public class RoutesForAgencyAction extends ApiActionSupport {

  private static final long serialVersionUID = 1L;

  private static final int V2 = 2;

  @Autowired
  private TransitDataService _service;

  private String _id;

  public RoutesForAgencyAction() {
    super(V2);
  }

  @RequiredFieldValidator
  public void setId(String id) {
    _id = id;
  }

  public String getId() {
    return _id;
  }

  public DefaultHttpHeaders show() {

    if (hasErrors())
      return setValidationErrorsResponse();

    if (!isVersion(V2))
      return setUnknownVersionResponse();

    ListBean<RouteBean> routes = _service.getRoutesForAgencyId(_id);

    BeanFactoryV2 factory = getBeanFactoryV2();
    List<RouteV2Bean> beans = new ArrayList<RouteV2Bean>();
    for (RouteBean route : routes.getList())
      beans.add(factory.getRoute(route));

    return setOkResponse(factory.list(beans, false));
  }
}
