package org.onebusaway.api.actions.api.where;

import java.io.IOException;
import java.util.Date;

import org.apache.struts2.rest.DefaultHttpHeaders;
import org.onebusaway.api.actions.api.ApiActionSupport;
import org.onebusaway.api.model.transit.TripDetailsV2Bean;
import org.onebusaway.exceptions.ServiceException;
import org.onebusaway.transit_data.model.tripplanner.TripPlannerConstraintsBean;
import org.onebusaway.transit_data.services.TransitDataService;

import com.opensymphony.xwork2.conversion.annotations.TypeConversion;

public class TripPlannerController extends ApiActionSupport {

  private static final long serialVersionUID = 1L;

  private static final int V2 = 2;

  private TransitDataService _transitDataService;

  private double _latFrom;

  private double _lonFrom;

  private double _latTo;

  private double _lonTo;

  private Date _timeFrom;

  public TripPlannerController() {
    super(V2);
  }

  public void setLatFrom(double latFrom) {
    _latFrom = latFrom;
  }

  public void setLonFrom(double lonFrom) {
    _lonFrom = lonFrom;
  }

  public void setLatTo(double latTo) {
    _latTo = latTo;
  }

  public void setLonTo(double lonTo) {
    _lonTo = lonTo;
  }

  @TypeConversion(converter = "org.onebusaway.api.impl.DateTimeConverter")
  public void setTimeFrom(Date timeFrom) {
    _timeFrom = timeFrom;
  }

  public void setTransitDataService(TransitDataService transitDataService) {
    _transitDataService = transitDataService;
  }

  public DefaultHttpHeaders index() throws IOException, ServiceException {

    if (_timeFrom == null)
      _timeFrom = new Date();

    TripPlannerConstraintsBean constraints = new TripPlannerConstraintsBean();
    constraints.setMinDepartureTime(_timeFrom.getTime());

    _transitDataService.getTripsBetween(_latFrom, _lonFrom, _latTo, _lonTo,
        constraints);

    return setOkResponse(null);
  }

}
