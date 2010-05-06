package org.onebusaway.tripplanner.impl.state;

import org.onebusaway.gtdf.model.StopTime;
import org.onebusaway.where.model.StopTimeInstance;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class StopTimeBinarySearch {

  public List<StopTimeInstance> getNextStopTime(long serviceDate,
      List<StopTime> stopTimes, long currentTime) {

    if (stopTimes.isEmpty())
      return new ArrayList<StopTimeInstance>();

    return search(serviceDate, stopTimes, currentTime, 0, stopTimes.size());
  }

  private List<StopTimeInstance> search(long serviceDate,
      List<StopTime> stopTimes, long currentTime, int from, int to) {

    int index = (from + to) / 2;
    int prevStopTimeIndex = index - 1;
    int nextStopTimeIndex = index;

    if (index == 0) {
      StopTime nextStopTime = stopTimes.get(nextStopTimeIndex);
      long nextTime = serviceDate + nextStopTime.getDepartureTime() * 1000;
      if (currentTime <= nextTime)
        return getStopTimeInstances(serviceDate, stopTimes, nextStopTimeIndex);
      else
        return search(serviceDate, stopTimes, currentTime, index + 1, to);
    }

    if (index == stopTimes.size()) {
      StopTime prevStopTime = stopTimes.get(prevStopTimeIndex);
      long prevTime = serviceDate + prevStopTime.getDepartureTime() * 1000;

      if (prevTime < currentTime)
        return new ArrayList<StopTimeInstance>();
      else
        return search(serviceDate, stopTimes, currentTime, from, index - 1);
    }

    StopTime prevStopTime = stopTimes.get(prevStopTimeIndex);
    StopTime nextStopTime = stopTimes.get(nextStopTimeIndex);

    long prevTime = serviceDate + prevStopTime.getDepartureTime() * 1000;
    long nextTime = serviceDate + nextStopTime.getDepartureTime() * 1000;

    if (prevTime > nextTime)
      throw new IllegalStateException(
          "stop times are not in monotonic increasinng order");

    if (prevTime < currentTime && currentTime <= nextTime) {
      return getStopTimeInstances(serviceDate, stopTimes, nextStopTimeIndex);
    } else if (currentTime <= prevTime) {
      return search(serviceDate, stopTimes, currentTime, from, index - 1);
    } else { /* nextTime < currentTime */
      return search(serviceDate, stopTimes, currentTime, index + 1, to);
    }
  }

  private List<StopTimeInstance> getStopTimeInstances(long serviceDate,
      List<StopTime> stopTimes, int currentIndex) {

    List<StopTimeInstance> stis = new ArrayList<StopTimeInstance>();

    StopTime st = stopTimes.get(currentIndex);
    Date date = new Date(serviceDate);

    long stTime = serviceDate + st.getDepartureTime() * 1000;
    long targetTime = stTime;

    while (true) {
      stis.add(new StopTimeInstance(st, date));
      currentIndex++;
      if (currentIndex >= stopTimes.size())
        break;
      st = stopTimes.get(currentIndex);
      stTime = serviceDate + st.getDepartureTime() * 1000;
      if (targetTime < stTime)
        break;
    }
    return stis;
  }
}
