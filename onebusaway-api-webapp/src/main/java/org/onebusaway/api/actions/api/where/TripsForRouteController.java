package org.onebusaway.api.actions.api.where;

import java.io.IOException;
import java.util.Date;

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
import org.onebusaway.transit_data.model.trips.TripsForRouteQueryBean;
import org.onebusaway.transit_data.services.TransitDataService;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.conversion.annotations.TypeConversion;
import com.opensymphony.xwork2.validator.annotations.RequiredFieldValidator;

public class TripsForRouteController extends ApiActionSupport {

  private static final long serialVersionUID = 1L;

  private static final int V2 = 2;

  @Autowired
  private TransitDataService _service;

  private String _id;

  private long _time = 0;

  private MaxCountSupport _maxCount = new MaxCountSupport();

  private boolean _includeTrip = true;
  
  private boolean _includeStatus = false;

  private boolean _includeSchedule = false;

  public TripsForRouteController() {
    super(V2);
  }

  @RequiredFieldValidator
  public void setId(String id) {
    _id = id;
  }

  public String getId() {
    return _id;
  }

  @TypeConversion(converter = "org.onebusaway.presentation.impl.conversion.DateTimeConverter")
  public void setTime(Date time) {
    _time = time.getTime();
  }
  
  public void setMaxCount(int maxCount) {
    _maxCount.setMaxCount(maxCount);
  }

  public void setIncludeTrip(boolean includeTrip) {
    _includeTrip = includeTrip;
  }
  
  public void setIncludeStatus(boolean includeStatus) {
    _includeStatus = includeStatus;
  }

  public void setIncludeSchedule(boolean includeSchedule) {
    _includeSchedule = includeSchedule;
  }

  public DefaultHttpHeaders show() throws IOException, ServiceException {

    if (!isVersion(V2))
      return setUnknownVersionResponse();

    if (hasErrors())
      return setValidationErrorsResponse();

    long time = System.currentTimeMillis();
    if (_time != 0)
      time = _time;

    TripsForRouteQueryBean query = new TripsForRouteQueryBean();
    query.setRouteId(_id);
    query.setTime(time);
    query.setMaxCount(_maxCount.getMaxCount());

    TripDetailsInclusionBean inclusion = query.getInclusion();
    inclusion.setIncludeTripBean(_includeTrip);
    inclusion.setIncludeTripSchedule(_includeSchedule);
    inclusion.setIncludeTripStatus(_includeStatus);

    BeanFactoryV2 factory = getBeanFactoryV2();

    try {
      ListBean<TripDetailsBean> trips = _service.getTripsForRoute(query);
      return setOkResponse(factory.getTripDetailsResponse(trips));
    } catch (OutOfServiceAreaServiceException ex) {
      return setOkResponse(factory.getEmptyList(TripDetailsV2Bean.class, true));
    }
  }
}
