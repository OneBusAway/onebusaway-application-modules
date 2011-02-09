package org.onebusaway.api.actions.api.where;

import org.apache.struts2.rest.DefaultHttpHeaders;
import org.onebusaway.api.actions.api.ApiActionSupport;
import org.onebusaway.api.model.transit.BeanFactoryV2;
import org.onebusaway.api.model.transit.EntryWithReferencesBean;
import org.onebusaway.api.model.transit.TripV2Bean;
import org.onebusaway.exceptions.ServiceException;
import org.onebusaway.transit_data.model.trips.TripBean;
import org.onebusaway.transit_data.services.TransitDataService;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.validator.annotations.RequiredFieldValidator;

public class TripController extends ApiActionSupport {

  private static final long serialVersionUID = 1L;

  private static final int V2 = 2;

  @Autowired
  private TransitDataService _service;

  private String _id;

  public TripController() {
    super(V2);
  }

  @RequiredFieldValidator
  public void setId(String id) {
    _id = id;
  }

  public String getId() {
    return _id;
  }

  public DefaultHttpHeaders show() throws ServiceException {

    if (!isVersion(V2))
      return setUnknownVersionResponse();

    if (hasErrors())
      return setValidationErrorsResponse();

    TripBean trip = _service.getTrip(_id);

    if (trip == null)
      return setResourceNotFoundResponse();

    BeanFactoryV2 factory = getBeanFactoryV2();
    EntryWithReferencesBean<TripV2Bean> response = factory.getResponse(trip);
    return setOkResponse(response);
  }
}
