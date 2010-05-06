package org.onebusaway.where.impl;

import edu.washington.cs.rse.collections.FactoryMap;

import its.SQL.ContentsData;
import its.backbone.sdd.SddReceiver;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

import org.onebusaway.common.spring.PostConstruct;
import org.onebusaway.common.spring.PreDestroy;
import org.onebusaway.gtdf.model.StopTime;
import org.onebusaway.gtdf.model.Trip;
import org.onebusaway.where.model.StopTimeInstance;
import org.onebusaway.where.model.Timepoint;
import org.onebusaway.where.model.TimepointPrediction;
import org.onebusaway.where.services.StopTimePredictionService;
import org.onebusaway.where.services.WhereDao;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class StopTimePredictionServiceImpl implements StopTimePredictionService {

  private static Logger _log = Logger.getLogger(StopTimePredictionServiceImpl.class.getName());

  private static TimepointPredictionComparator TIMEPOINT_PREDICTION_COMPARATOR = new TimepointPredictionComparator();

  private static final String TIMEPOINT_PREDICTION_SERVER_NAME = "carpool.its.washington.edu";

  private static final int TIMEPOINT_PREDICTION_SERVER_PORT = 9002;

  private WhereDao _dao;

  private TimepointPredictionReceiver _receiver;

  private Cache _timepointsByTripCache;

  private Cache _predictionsByTripCache;

  private String _timepointPredictionServerName = TIMEPOINT_PREDICTION_SERVER_NAME;

  private int _timepointPredictionServerPort = TIMEPOINT_PREDICTION_SERVER_PORT;

  @Autowired
  public void setWhereDao(WhereDao dao) {
    _dao = dao;
  }

  public void setTimepointsByTripCache(Cache cache) {
    _timepointsByTripCache = cache;
  }

  public void setPredictionsByTripCache(Cache cache) {
    _predictionsByTripCache = cache;
  }

  @PostConstruct
  public void startup() {
    try {
      _receiver = new TimepointPredictionReceiver(
          _timepointPredictionServerName, _timepointPredictionServerPort);
      _receiver.start();
    } catch (IOException ex) {
      _log.log(Level.SEVERE, "error starting transit prediction data receiver",
          ex);
    }
  }

  @PreDestroy
  public void shutdown() {
    _receiver.stop();
  }

  public void getPredictions(List<StopTimeInstance> stopTimes) {

    Map<String, List<StopTimeInstance>> stisByTripId = getStopTimeInstancesByTripId(stopTimes);

    Set<String> tripIds = stisByTripId.keySet();

    Map<String, Map<String, List<Timepoint>>> timepointsByTripId = getTimepointsByTripIds(tripIds);
    Map<String, List<TimepointPrediction>> predictionsByTripId = getPredictionsByTripIds(tripIds);

    for (String tripId : tripIds) {
      List<StopTimeInstance> stis = stisByTripId.get(tripId);
      Map<String, List<Timepoint>> timepoints = timepointsByTripId.get(tripId);
      List<TimepointPrediction> predictions = predictionsByTripId.get(tripId);
      applyPredictions(tripId, stis, timepoints, predictions);
    }
  }

  private Map<String, List<StopTimeInstance>> getStopTimeInstancesByTripId(
      List<StopTimeInstance> stopTimes) {
    Map<String, List<StopTimeInstance>> stisByTripId = new FactoryMap<String, List<StopTimeInstance>>(
        new ArrayList<StopTimeInstance>());

    for (StopTimeInstance sti : stopTimes) {
      StopTime stopTime = sti.getStopTime();
      Trip trip = stopTime.getTrip();
      String tripId = trip.getId();
      stisByTripId.get(tripId).add(sti);
    }
    return stisByTripId;
  }

  @SuppressWarnings("unchecked")
  private Map<String, Map<String, List<Timepoint>>> getTimepointsByTripIds(
      Set<String> tripIds) {

    Set<String> cacheMissTripIds = new HashSet<String>();

    Map<String, Map<String, List<Timepoint>>> allTimepointsByTripIdAndTimepointId = new HashMap<String, Map<String, List<Timepoint>>>();

    for (String tripId : tripIds) {
      Element element = _timepointsByTripCache.get(tripId);
      if (element == null) {
        cacheMissTripIds.add(tripId);
      } else {
        Map<String, List<Timepoint>> timepointsById = (Map<String, List<Timepoint>>) element.getValue();
        allTimepointsByTripIdAndTimepointId.put(tripId, timepointsById);
      }
    }

    List<Timepoint> missingTimepoints = _dao.getTimepointsByTripIds(cacheMissTripIds);
    Map<String, Map<String, List<Timepoint>>> byTripId = getTimepointsByTripIdAndTimepointId(missingTimepoints);

    for (Map.Entry<String, Map<String, List<Timepoint>>> entry : byTripId.entrySet()) {
      String tripId = entry.getKey();
      Map<String, List<Timepoint>> timepointsById = entry.getValue();
      Element element = new Element(tripId, timepointsById);
      _timepointsByTripCache.put(element);
    }

    allTimepointsByTripIdAndTimepointId.putAll(byTripId);

    return allTimepointsByTripIdAndTimepointId;
  }

  private Map<String, Map<String, List<Timepoint>>> getTimepointsByTripIdAndTimepointId(
      List<Timepoint> timepoints) {

    Map<String, Map<String, List<Timepoint>>> byTripId = new HashMap<String, Map<String, List<Timepoint>>>();

    for (Timepoint timepoint : timepoints) {

      String tripId = timepoint.getId().getTripId();

      Map<String, List<Timepoint>> byTimepointId = byTripId.get(tripId);

      if (byTimepointId == null) {
        byTimepointId = new HashMap<String, List<Timepoint>>();
        byTripId.put(tripId, byTimepointId);
      }

      String timepointId = timepoint.getId().getTimepointId();

      List<Timepoint> ts = byTimepointId.get(timepointId);
      if (ts == null) {
        ts = new ArrayList<Timepoint>();
        byTimepointId.put(timepointId, ts);
      }
      ts.add(timepoint);
    }

    return byTripId;
  }

  @SuppressWarnings("unchecked")
  private Map<String, List<TimepointPrediction>> getPredictionsByTripIds(
      Set<String> tripIds) {

    Map<String, List<TimepointPrediction>> predictionsByTripId = new HashMap<String, List<TimepointPrediction>>();
    List<TimepointPrediction> empty = new ArrayList<TimepointPrediction>();

    for (String tripId : tripIds) {
      Element element = _predictionsByTripCache.get(tripId);
      if (element == null) {
        predictionsByTripId.put(tripId, empty);
      } else {
        List<TimepointPrediction> predictions = (List<TimepointPrediction>) element.getValue();
        predictionsByTripId.put(tripId, predictions);
      }
    }

    return predictionsByTripId;
  }

  private void applyPredictions(String tripId, List<StopTimeInstance> stis,
      Map<String, List<Timepoint>> timepointsByTimepointId,
      List<TimepointPrediction> predictions) {

    // No point supplying predictions if we have none
    if (predictions.isEmpty())
      return;

    SortedMap<Double, Integer> offsetBy = new TreeMap<Double, Integer>();

    for (TimepointPrediction prediction : predictions) {

      String timepointId = prediction.getTimepointId();
      List<Timepoint> timepoints = timepointsByTimepointId.get(timepointId);

      if (timepoints == null || timepoints.size() == 0) {
        // _log.warning("mismatched timepoint prediction : " + prediction);
        continue;
      }

      if (timepoints.size() > 1) {
        // _log.warning("multiple timepoint prediction matches: " + prediction);
        continue;
      }

      Timepoint timepoint = timepoints.get(0);
      double distance = timepoint.getShapeDistanceTraveled();
      int deviation = prediction.getGoalDeviation();
      offsetBy.put(distance, deviation);
    }

    // No point supplying predictions if we have none
    if (offsetBy.isEmpty())
      return;

    for (StopTimeInstance sti : stis) {
      StopTime stopTime = sti.getStopTime();
      double distance = stopTime.getShapeDistanceTraveled();
      double offset = getInterpolatedGoalDeviation(offsetBy, distance);
      sti.setPredictedOffset((long) offset * 1000);
    }
  }

  private double getInterpolatedGoalDeviation(
      SortedMap<Double, Integer> offsetBy, double distance) {

    SortedMap<Double, Integer> before = offsetBy.headMap(distance);
    SortedMap<Double, Integer> after = offsetBy.tailMap(distance);

    if (before.isEmpty() && after.isEmpty()) {
      _log.severe("offsets set is empty?");
      return 0;
    } else if (before.isEmpty()) {
      return after.get(after.firstKey());
    } else if (after.isEmpty()) {
      return before.get(before.lastKey());
    } else {

      double fromKey = before.lastKey();
      double toKey = after.firstKey();
      double fromValue = before.get(fromKey);
      double toValue = after.get(toKey);

      double ratio = (distance - fromKey) / (toKey - fromKey);
      return (fromValue + (toValue - fromValue) * ratio);
    }
  }

  /*****
   * 
   ****/

  private class TimepointPredictionReceiver extends SddReceiver {

    public TimepointPredictionReceiver(String serverName, int serverPort)
        throws IOException {
      super(serverName, serverPort);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void extractedDataReceived(Hashtable ht, String serialNum) {
      super.extractedDataReceived(ht, serialNum);

      Map<String, List<TimepointPrediction>> predictionsByTripId = new FactoryMap<String, List<TimepointPrediction>>(
          new ArrayList<TimepointPrediction>());

      if (ht.containsKey("PREDICTIONS")) {
        ContentsData data = (ContentsData) ht.get("PREDICTIONS");
        data.resetRowIndex();
        while (data.next()) {
          TimepointPrediction p = new TimepointPrediction();
          p.setAgencyId(data.getString(0));
          p.setBlockId(data.getString(1));
          p.setTripId(data.getString(2));
          p.setTimepointId(data.getString(3));
          p.setScheduledTime(data.getInt(4));
          p.setPredictorType(data.getString(12));
          p.setGoalTime(data.getInt(13));
          p.setGoalDeviation(data.getInt(14));
          predictionsByTripId.get(p.getTripId()).add(p);
        }
      }

      for (Map.Entry<String, List<TimepointPrediction>> entry : predictionsByTripId.entrySet()) {
        String tripId = entry.getKey();
        List<TimepointPrediction> predictions = entry.getValue();
        Collections.sort(predictions, TIMEPOINT_PREDICTION_COMPARATOR);
        Element element = new Element(tripId, predictions);
        _predictionsByTripCache.put(element);
      }
    }
  }

  private static class TimepointPredictionComparator implements
      Comparator<TimepointPrediction> {

    public int compare(TimepointPrediction o1, TimepointPrediction o2) {
      int t1 = o1.getScheduledTime();
      int t2 = o2.getScheduledTime();
      return t1 == t2 ? 0 : (t1 < t2 ? -1 : 1);
    }
  }
}
