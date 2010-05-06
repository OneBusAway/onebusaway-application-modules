package org.onebusaway.webapp.actions.sms;

import org.onebusaway.exceptions.ServiceException;
import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.presentation.impl.ArrivalAndDepartureComparator;
import org.onebusaway.presentation.services.text.TextModification;
import org.onebusaway.transit_data.model.ArrivalAndDepartureBean;
import org.onebusaway.transit_data.model.StopWithArrivalsAndDeparturesBean;
import org.onebusaway.transit_data.services.TransitDataService;
import org.onebusaway.users.client.model.UserBean;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import edu.emory.mathcs.backport.java.util.Collections;

public class ArrivalsAndDeparturesAction extends AbstractTextmarksAction {

  private static final long serialVersionUID = 1L;

  @Autowired
  private TransitDataService _service;

  private TextModification _abbreviations;

  private StopWithArrivalsAndDeparturesBean _result;

  @Autowired
  public void setDestinationAbbreviations(
      @Qualifier("smsDestinationAbbreviations") TextModification strategy) {
    _abbreviations = strategy;
  }

  public void setCurrentUser(UserBean currentUser) {
    _currentUser = currentUser;
  }

  public StopWithArrivalsAndDeparturesBean getResult() {
    return _result;
  }

  @Override
  public String execute() throws ServiceException {

    if (_text != null)
      _text.trim();

    if (_text == null || _text.length() == 0)
      return INPUT;

    String[] tokens = _text.trim().split("\\s+");

    if (tokens.length == 0)
      return INPUT;

    CoordinateBounds serviceArea = getServiceArea();

    if (serviceArea == null) {
      pushNextAction("arrivals-and-departures", _text);
      return "query-default-search-location";
    }

    String stopId = tokens[0];

    _result = _service.getStopWithArrivalsAndDepartures(stopId, new Date());

    // Filter by route
    if (tokens.length > 1) {
      Set<String> routes = new HashSet<String>();
      for (int i = 1; i < tokens.length; i++) {
        String[] routeNames = tokens[i].split(",");
        for (String routeName : routeNames)
          routes.add(routeName);
      }
      Iterator<ArrivalAndDepartureBean> it = _result.getArrivalsAndDepartures().iterator();
      while (it.hasNext()) {
        ArrivalAndDepartureBean bean = it.next();
        if (!routes.contains(bean.getRouteShortName()))
          it.remove();
      }
    }

    // Sort results
    Collections.sort(_result.getArrivalsAndDepartures(),
        new ArrivalAndDepartureComparator());

    return SUCCESS;
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
