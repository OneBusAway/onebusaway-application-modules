package org.onebusaway.webapp.actions.bundles;

import com.google.gwt.i18n.client.Messages;

public interface ArrivalAndDepartureMessages extends Messages {

  /****
   * Arrivals and Departures Fields
   ****/
  
  public String arrivedEarly(int minutes);
  
  public String departedEarly(int minutes);

  public String early(int minutes);
  
  public String arrivedOnTime();
  
  public String departedOnTime();

  public String onTime();
  
  public String arrivedLate(int minutes);

  public String departedLate(int minutes);

  public String delayed(int minutes);

  public String scheduledArrival();
  
  public String scheduledDeparture();
}
