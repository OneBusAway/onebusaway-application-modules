package org.onebusaway.api.actions.api.where;

import java.io.IOException;
import java.util.Date;

import org.apache.struts2.rest.DefaultHttpHeaders;
import org.onebusaway.api.actions.api.ApiActionSupport;
import org.onebusaway.api.model.transit.BeanFactoryV2;
import org.onebusaway.api.model.transit.ListWithReferencesBean;
import org.onebusaway.api.model.transit.TripDetailsV2Bean;
import org.onebusaway.api.model.transit.tripplanning.MinTravelTimeToStopV2Bean;
import org.onebusaway.exceptions.OutOfServiceAreaServiceException;
import org.onebusaway.exceptions.ServiceException;
import org.onebusaway.transit_data.model.oba.MinTravelTimeToStopsBean;
import org.onebusaway.transit_data.model.oba.OneBusAwayConstraintsBean;
import org.onebusaway.transit_data.services.TransitDataService;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.conversion.annotations.TypeConversion;
import com.opensymphony.xwork2.validator.annotations.RequiredFieldValidator;

public class MinTravelTimeToStopsController extends ApiActionSupport {

  private static final long serialVersionUID = 1L;

  private static final int V2 = 2;

  private TransitDataService _transitDataService;

  private double _lat;

  private double _lon;

  private OneBusAwayConstraintsBean _constraints = new OneBusAwayConstraintsBean();

  public MinTravelTimeToStopsController() {
    super(V2);
  }

  @Autowired
  public void setTransitDataService(TransitDataService transitDataService) {
    _transitDataService = transitDataService;
  }

  @RequiredFieldValidator
  public void setLat(double lat) {
    _lat = lat;
  }

  public double getLat() {
    return _lat;
  }

  @RequiredFieldValidator
  public void setLon(double lon) {
    _lon = lon;
  }

  public double getLon() {
    return _lon;
  }

  @TypeConversion(converter = "org.onebusaway.presentation.impl.conversion.DateTimeConverter")
  public void setTime(Date time) {
    _constraints.setMinDepartureTime(time.getTime());

  }

  /**
   * @param maxTripDuration in minutes
   */
  public void setMaxTripDuration(int maxTripDuration) {
    _constraints.setMaxTripDuration(maxTripDuration);
  }

  public DefaultHttpHeaders index() throws IOException, ServiceException {

    if (!isVersion(V2))
      return setUnknownVersionResponse();

    if (hasErrors())
      return setValidationErrorsResponse();

    if (_constraints.getMinDepartureTime() <= 0)
      _constraints.setMinDepartureTime(System.currentTimeMillis());

    if (_constraints.getMaxDepartureTime() <= 0)
      _constraints.setMaxDepartureTime(_constraints.getMinDepartureTime() + 60 * 60 * 1000);

    MinTravelTimeToStopsBean result = _transitDataService.getMinTravelTimeToStopsFrom(
        _lat, _lon, _constraints);

    BeanFactoryV2 factory = getBeanFactoryV2();

    try {
      ListWithReferencesBean<MinTravelTimeToStopV2Bean> response = factory.getMinTravelTimeToStops(result);
      return setOkResponse(response);
    } catch (OutOfServiceAreaServiceException ex) {
      return setOkResponse(factory.getEmptyList(TripDetailsV2Bean.class, true));
    }
  }
}
