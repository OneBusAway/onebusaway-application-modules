package org.onebusaway.where.web.api.actions;

import org.onebusaway.common.web.api.AbstractApiAction;
import org.onebusaway.common.web.api.ResponseBean;
import org.onebusaway.common.web.common.client.rpc.ServiceException;
import org.onebusaway.where.web.common.client.model.StopWithArrivalsBean;
import org.onebusaway.where.web.common.client.rpc.WhereService;
import org.springframework.beans.factory.annotation.Autowired;

public class DeparturesByStopIdAction extends AbstractApiAction {

  private static final long serialVersionUID = 1L;

  private static final String VERSION = "1.0";

  @Autowired
  private WhereService _service;

  private String _id;

  public DeparturesByStopIdAction() {
    super(VERSION);
  }

  public void setId(String id) {
    _id = id;
  }

  /**
   * By default, the JSON result type only examines bean methods in the main
   * action class and not parent classes. As such, we hoist up the response bean
   * from the parent.
   */
  @Override
  public ResponseBean getResponse() {
    return super.getResponse();
  }

  @Override
  protected ResponseBean executeWithResponse() {
    if (_id == null || "".equals(_id))
      return getInvalidArgumentResponse("id");
    try {
      StopWithArrivalsBean result = _service.getArrivalsByStopId(_id);
      return getOkResponse(result);
    } catch (ServiceException e) {
      return getServiceExceptionResponse(e);
    }
  }
}
