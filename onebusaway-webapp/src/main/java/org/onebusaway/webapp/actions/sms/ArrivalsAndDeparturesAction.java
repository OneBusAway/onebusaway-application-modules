package org.onebusaway.webapp.actions.sms;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.onebusaway.exceptions.ServiceException;
import org.onebusaway.presentation.client.RoutePresenter;
import org.onebusaway.presentation.impl.ArrivalAndDepartureComparator;
import org.onebusaway.presentation.services.text.TextModification;
import org.onebusaway.transit_data.model.ArrivalAndDepartureBean;
import org.onebusaway.transit_data.model.RouteBean;
import org.onebusaway.transit_data.model.StopsWithArrivalsAndDeparturesBean;
import org.onebusaway.transit_data.model.TripBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class ArrivalsAndDeparturesAction extends AbstractTextmarksAction {

  private static final long serialVersionUID = 1L;

  private TextModification _abbreviations;

  private List<String> _stopIds = new ArrayList<String>();

  private StopsWithArrivalsAndDeparturesBean _result;

  private String[] _args;

  @Autowired
  public void setDestinationAbbreviations(
      @Qualifier("smsDestinationAbbreviations") TextModification strategy) {
    _abbreviations = strategy;
  }

  public void setStopId(String stopId) {
    _stopIds.add(stopId);
  }

  public void setStopIds(List<String> stopIds) {
    _stopIds.addAll(stopIds);
  }

  public void setArgs(String[] args) {
    _args = args;
  }

  public StopsWithArrivalsAndDeparturesBean getResult() {
    return _result;
  }

  @Override
  public String execute() throws ServiceException {
    
    if( _stopIds.isEmpty() )
      return INPUT;

    Calendar c = Calendar.getInstance();
    Date now = new Date();

    c.setTime(now);
    c.add(Calendar.MINUTE, -5);
    Date timeFrom = c.getTime();

    c.setTime(now);
    c.add(Calendar.MINUTE, 35);
    Date timeTo = c.getTime();

    _result = _transitDataService.getStopsWithArrivalsAndDepartures(_stopIds,
        timeFrom, timeTo);

    if (_args != null && _args.length > 0)
      filterArrivalsAndDeparturesByRoute(_args);

    // Sort results
    Collections.sort(_result.getArrivalsAndDepartures(),
        new ArrivalAndDepartureComparator());

    _currentUserService.setLastSelectedStopIds(_stopIds);

    return SUCCESS;
  }

  private void filterArrivalsAndDeparturesByRoute(String[] tokens) {

    // Filter by route
    Set<String> routes = new HashSet<String>();

    for (String token : tokens) {
      String[] routeNames = token.split(",");
      for (String routeName : routeNames)
        routes.add(routeName);
    }

    Iterator<ArrivalAndDepartureBean> it = _result.getArrivalsAndDepartures().iterator();

    while (it.hasNext()) {
      ArrivalAndDepartureBean bean = it.next();
      TripBean trip = bean.getTrip();
      RouteBean route = trip.getRoute();
      String routeName = RoutePresenter.getNameForRoute(route);
      if (!routes.contains(routeName))
        it.remove();
    }
  }

  public long getNow() {
    return System.currentTimeMillis();
  }

  public String getMinutesLabel(ArrivalAndDepartureBean pab, long now) {
    long t = pab.getScheduledDepartureTime();
    if (pab.hasPredictedDepartureTime())
      t = pab.getPredictedDepartureTime();
    int minutes = (int) Math.round((t - now) / (1000.0 * 60.0));
    boolean isNow = Math.abs(minutes) <= 1;
    return isNow ? "NOW" : (Integer.toString(minutes) + "m");
  }

  public String abbreviate(String destination) {
    return _abbreviations.modify(destination);
  }
}
