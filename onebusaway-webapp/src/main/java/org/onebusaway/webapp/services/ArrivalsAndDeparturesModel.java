package org.onebusaway.webapp.services;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

import org.onebusaway.transit_data.model.AgencyBean;
import org.onebusaway.transit_data.model.StopsWithArrivalsAndDeparturesBean;
import org.onebusaway.webapp.gwt.where_library.view.ArrivalsAndDeparturesPresentaion;

public interface ArrivalsAndDeparturesModel {

  public void setStopIds(List<String> stopIds);

  public void setRouteFilter(Set<String> routeFilter);

  public boolean setOrderFromString(String order);

  public void setTime(Date time);

  public void setMinutesBefore(int minutesBefore);

  public void setMinutesAfter(int minutesAfter);

  public void process();

  public ArrivalsAndDeparturesPresentaion getArrivalsAndDeparturesPresentation();

  public TimeZone getTimeZone();

  public StopsWithArrivalsAndDeparturesBean getResult();

  public List<AgencyBean> getAgencies();
}
