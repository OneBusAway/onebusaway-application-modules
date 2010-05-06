package org.onebusaway.webapp.gwt.where_library.view;

import org.onebusaway.transit_data.model.ArrivalAndDepartureBean;
import org.onebusaway.webapp.gwt.where_library.WhereMessages;

public class ArrivalsAndDeparturesMethods {

  private static final String CANCELLED = "cancelled";

  private WhereMessages _messages;

  public void setMessages(WhereMessages messages) {
    _messages = messages;
  }

  public String getArrivalLabel(ArrivalAndDepartureBean pab, long now) {

    if (CANCELLED.equals(pab.getStatus()))
      return "suspended";

    long predicted = pab.getPredictedArrivalTime();
    long scheduled = pab.getScheduledArrivalTime();

    if (predicted > 0) {

      double diff = ((pab.getPredictedArrivalTime() - pab.getScheduledArrivalTime()) / (1000.0 * 60));
      int minutes = (int) Math.abs(Math.round(diff));

      boolean departed = predicted < now;

      if (diff < -1.5) {
        return departed ? _messages.departedEarly(minutes)
            : _messages.early(minutes);
      } else if (diff < 1.5) {
        return departed ? _messages.departedOnTime() : _messages.onTime();
      } else {
        return departed ? _messages.departedLate(minutes)
            : _messages.delayed(minutes);
      }

    } else {
      if (scheduled < now)
        return _messages.scheduledDeparture();
      else
        return _messages.scheduledArrival();
    }
  }

  public String getArrivalStatusLabelStyle(ArrivalAndDepartureBean pab, long now) {

    if (CANCELLED.equals(pab.getStatus()))
      return "arrivalStatusCancelled";

    long predicted = pab.getPredictedArrivalTime();
    long scheduled = pab.getScheduledArrivalTime();

    if (predicted > 0) {

      if (predicted < now)
        return "arrivalStatusDeparted";

      double diff = ((pab.getPredictedArrivalTime() - pab.getScheduledArrivalTime()) / (1000.0 * 60));

      if (diff < -1.5) {
        return "arrivalStatusEarly";
      } else if (diff < 1.5) {
        return "arrivalStatusDefault";
      } else {
        return "arrivalStatusDelayed";
      }

    } else {
      if (scheduled < now)
        return "arrivalStatusDeparted";
      else
        return "arrivalStatusNoInfo";
    }
  }

  public String getMinutesLabel(ArrivalAndDepartureBean pab, long now) {

    if (CANCELLED.equals(pab.getStatus()))
      return "-";

    boolean isNow = isArrivalNow(pab, now);
    long t = getBestArrivalTime(pab);
    int minutes = (int) Math.round((t - now) / (1000.0 * 60.0));
    return isNow ? "NOW" : Integer.toString(minutes);
  }

  public boolean isArrivalNow(ArrivalAndDepartureBean pab, long now) {
    if (CANCELLED.equals(pab.getStatus()))
      return true;
    long t = getBestArrivalTime(pab);
    int minutes = (int) Math.round((t - now) / (1000.0 * 60.0));
    return Math.abs(minutes) <= 1;
  }

  private long getBestArrivalTime(ArrivalAndDepartureBean pab) {
    long t = pab.getScheduledArrivalTime();
    if (pab.hasPredictedArrivalTime())
      t = pab.getPredictedArrivalTime();
    return t;
  }
}
