package org.onebusaway.api.actions.api.where;

import java.io.IOException;

import org.apache.struts2.rest.DefaultHttpHeaders;
import org.onebusaway.api.actions.api.ApiActionSupport;
import org.onebusaway.api.impl.MaxCountSupport;
import org.onebusaway.api.model.transit.BeanFactoryV2;
import org.onebusaway.api.model.transit.TripDetailsV2Bean;
import org.onebusaway.exceptions.OutOfServiceAreaServiceException;
import org.onebusaway.exceptions.ServiceException;
import org.onebusaway.transit_data.model.ListBean;
import org.onebusaway.transit_data.model.trips.TripDetailsBean;
import org.onebusaway.transit_data.model.trips.TripDetailsInclusionBean;
import org.onebusaway.transit_data.model.trips.TripsForAgencyQueryBean;
import org.onebusaway.transit_data.services.TransitDataService;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.validator.annotations.RequiredFieldValidator;

public class TripsForAgencyController extends ApiActionSupport {

  private static final long serialVersionUID = 1L;

  private static final int V2 = 2;

  @Autowired
  private TransitDataService _service;

  private String _id;

  private long _time = 0;

  private MaxCountSupport _maxCount = new MaxCountSupport();

  private boolean _includeTrips = true;
  
  private boolean _includeStatus = false;

  private boolean _includeSchedules = false;

  public TripsForAgencyController() {
    super(V2);
  }

  @RequiredFieldValidator
  public void setId(String id) {
    _id = id;
  }

  public String getId() {
    return _id;
  }

  public void setTime(long time) {
    _time = time;
  }

  public void setMaxCount(int maxCount) {
    _maxCount.setMaxCount(maxCount);
  }

  public void setIncludeTrips(boolean includeTrips) {
    _includeTrips = includeTrips;
  }
  
  public void setIncludeStatus(boolean includeStatus) {
    _includeStatus = includeStatus;
  }

  public void setIncludeSchedules(boolean includeSchedules) {
    _includeSchedules = includeSchedules;
  }

  public DefaultHttpHeaders show() throws IOException, ServiceException {

    if (!isVersion(V2))
      return setUnknownVersionResponse();

    if (hasErrors())
      return setValidationErrorsResponse();

    long time = System.currentTimeMillis();
    if (_time != 0)
      time = _time;

    TripsForAgencyQueryBean query = new TripsForAgencyQueryBean();
    query.setAgencyId(_id);
    query.setTime(time);
    query.setMaxCount(_maxCount.getMaxCount());

    TripDetailsInclusionBean inclusion = query.getInclusion();
    inclusion.setIncludeTripBean(_includeTrips);
    inclusion.setIncludeTripStatus(_includeStatus);
    inclusion.setIncludeTripSchedule(_includeSchedules);
    inclusion.setIncludeTripStatus(true);

    BeanFactoryV2 factory = getBeanFactoryV2();

    try {
      ListBean<TripDetailsBean> trips = _service.getTripsForAgency(query);
      return setOkResponse(factory.getTripDetailsResponse(trips));
    } catch (OutOfServiceAreaServiceException ex) {
      return setOkResponse(factory.getEmptyList(TripDetailsV2Bean.class, true));
    }
  }
}
