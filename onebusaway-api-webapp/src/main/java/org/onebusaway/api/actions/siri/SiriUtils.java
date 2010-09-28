package org.onebusaway.api.actions.siri;

import org.onebusaway.siri.model.OnwardCall;
import org.onebusaway.transit_data.model.StopBean;
import org.onebusaway.transit_data.model.TripStopTimeBean;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;

public class SiriUtils {

  public static String getIdWithoutAgency(String id) {
    int startIndex = id.indexOf('_') + 1;
    id = id.substring(startIndex);
    return id;
  }

  public static List<OnwardCall> getOnwardCalls(
      List<TripStopTimeBean> stopTimes, long serviceDate,
      StopBean currentStopTime) {

    ArrayList<OnwardCall> onwardCalls = new ArrayList<OnwardCall>();

    HashMap<String, Integer> visitNumberForStop = new HashMap<String, Integer>();
    boolean afterStop = false;

    for (TripStopTimeBean stopTime : stopTimes) {

      StopBean stop = stopTime.getStop();
      int visitNumber = getVisitNumber(visitNumberForStop, stop);
      if (afterStop) {
        OnwardCall onwardCall = new OnwardCall();
        onwardCall.StopPointRef = SiriUtils.getIdWithoutAgency(stop.getId());
        onwardCall.StopPointName = stop.getName();
        onwardCall.VisitNumber = visitNumber;
        Calendar arrivalTime = new GregorianCalendar();
        long millis = serviceDate + stopTime.getArrivalTime() * 1000;
        arrivalTime.setTimeInMillis(millis);
        onwardCall.AimedArrivalTime = arrivalTime;

        Calendar departureTime = new GregorianCalendar();
        millis = serviceDate + stopTime.getDepartureTime() * 1000;
        departureTime.setTimeInMillis(millis);
        onwardCall.AimedDepartureTime = departureTime;
        onwardCalls.add(onwardCall);

      }
      if (stop == currentStopTime) {
        afterStop = true;
      }
    }
    return onwardCalls;
  }

  public static int getVisitNumber(HashMap<String, Integer> visitNumberForStop,
      StopBean stop) {
    int visitNumber;
    if (visitNumberForStop.containsKey(stop.getId())) {
      visitNumber = visitNumberForStop.get(stop.getId()) + 1;
    } else {
      visitNumber = 1;
    }
    visitNumberForStop.put(stop.getId(), visitNumber);
    return visitNumber;
  }
}
