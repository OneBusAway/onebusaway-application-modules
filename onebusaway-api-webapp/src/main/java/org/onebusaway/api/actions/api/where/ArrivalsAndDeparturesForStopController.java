package org.onebusaway.api.actions.api.where;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.struts2.rest.DefaultHttpHeaders;
import org.onebusaway.api.actions.api.ApiActionSupport;
import org.onebusaway.api.model.transit.BeanFactoryV2;
import org.onebusaway.api.model.where.ArrivalAndDepartureBeanV1;
import org.onebusaway.api.model.where.StopWithArrivalsAndDeparturesBeanV1;
import org.onebusaway.exceptions.ServiceException;
import org.onebusaway.transit_data.model.ArrivalAndDepartureBean;
import org.onebusaway.transit_data.model.RouteBean;
import org.onebusaway.transit_data.model.StopWithArrivalsAndDeparturesBean;
import org.onebusaway.transit_data.model.TripBean;
import org.onebusaway.transit_data.services.TransitDataService;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.validator.annotations.RequiredFieldValidator;

public class ArrivalsAndDeparturesForStopController extends ApiActionSupport {

  private static final long serialVersionUID = 1L;

  private static final int V1 = 1;

  private static final int V2 = 2;

  @Autowired
  private TransitDataService _service;

  private String _id;

  private Date _time;

  private int _minutesBefore = 5;

  private int _minutesAfter = 35;

  public ArrivalsAndDeparturesForStopController() {
    super(V1);
  }

  @RequiredFieldValidator(message = "whoa there")
  public void setId(String id) {
    _id = id;
  }

  public String getId() {
    return _id;
  }

  public void setTime(Date time) {
    _time = time;
  }

  public void setMinutesBefore(int minutesBefore) {
    _minutesBefore = minutesBefore;
  }

  public void setMinutesAfter(int minutesAfter) {
    _minutesAfter = minutesAfter;
  }

  public DefaultHttpHeaders show() throws ServiceException {

    if (hasErrors())
      return setValidationErrorsResponse();

    if (_time == null)
      _time = new Date();

    Calendar c = Calendar.getInstance();
    c.setTime(_time);
    c.add(Calendar.MINUTE, -_minutesBefore);
    Date timeFrom = c.getTime();

    c.setTime(_time);
    c.add(Calendar.MINUTE, _minutesAfter);
    Date timeTo = c.getTime();

    StopWithArrivalsAndDeparturesBean result = _service.getStopWithArrivalsAndDepartures(
        _id, timeFrom, timeTo);

    if (result == null)
      return setResourceNotFoundResponse();

    if (isVersion(V1)) {
      // Convert data to v1 form
      List<ArrivalAndDepartureBeanV1> arrivals = getArrivalsAsV1(result);
      StopWithArrivalsAndDeparturesBeanV1 v1 = new StopWithArrivalsAndDeparturesBeanV1(
          result.getStop(), arrivals, result.getNearbyStops());
      return setOkResponse(v1);
    } else if (isVersion(V2)) {
      BeanFactoryV2 factory = getBeanFactoryV2();
      return setOkResponse(factory.getResponse(result));
    } else {
      return setUnknownVersionResponse();
    }
  }

  private List<ArrivalAndDepartureBeanV1> getArrivalsAsV1(
      StopWithArrivalsAndDeparturesBean result) {

    List<ArrivalAndDepartureBeanV1> v1s = new ArrayList<ArrivalAndDepartureBeanV1>();

    for (ArrivalAndDepartureBean bean : result.getArrivalsAndDepartures()) {

      TripBean trip = bean.getTrip();
      RouteBean route = trip.getRoute();

      ArrivalAndDepartureBeanV1 v1 = new ArrivalAndDepartureBeanV1();
      v1.setPredictedArrivalTime(bean.getPredictedArrivalTime());
      v1.setPredictedDepartureTime(bean.getPredictedDepartureTime());
      v1.setRouteId(route.getId());
      v1.setRouteShortName(route.getShortName());
      v1.setScheduledArrivalTime(bean.getScheduledArrivalTime());
      v1.setScheduledDepartureTime(bean.getScheduledDepartureTime());
      v1.setStatus(bean.getStatus());
      v1.setStopId(bean.getStopId());
      v1.setTripHeadsign(trip.getTripHeadsign());
      v1.setTripId(trip.getId());

      v1s.add(v1);
    }

    return v1s;
  }
}
