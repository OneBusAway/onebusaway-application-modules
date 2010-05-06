package org.onebusaway.kcmetro_tcip.impl;

import org.onebusaway.kcmetro_tcip.model.TimepointPrediction;
import org.onebusaway.kcmetro_tcip.services.TimepointPredictionService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import its.SQL.ContentsData;
import its.backbone.sdd.SddReceiver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

public class TimepointPredictionServiceImpl implements
    TimepointPredictionService {

  private final Logger _log = LoggerFactory.getLogger(TimepointPredictionServiceImpl.class);

  private static final String TIMEPOINT_PREDICTION_SERVER_NAME = "carpool.its.washington.edu";

  private static final int TIMEPOINT_PREDICTION_SERVER_PORT = 9002;

  private TimepointPredictionReceiver _receiver;

  private List<TimepointPredictionListener> _listeners = new ArrayList<TimepointPredictionListener>();

  private String _serverName = TIMEPOINT_PREDICTION_SERVER_NAME;

  private int _serverPort = TIMEPOINT_PREDICTION_SERVER_PORT;

  private File _tripIdMappingFile;

  private Map<String, String> _tripIdMapping = new HashMap<String, String>();

  private String _defaultAgencyId;

  private int _predictionCount = 0;

  private int _mappedTripIdCount = 0;

  public void setServerName(String name) {
    _serverName = name;
  }

  public void setServerPort(int port) {
    _serverPort = port;
  }

  public void setTripIdMappingPath(File tripIdMapping) {
    _tripIdMappingFile = tripIdMapping;
  }

  public void setTripIdMapping(Map<String, String> tripIdMapping) {
    _tripIdMapping = tripIdMapping;
  }

  public void setDefaultAgencyId(String agencyId) {
    _defaultAgencyId = agencyId;
  }

  public void setListener(TimepointPredictionListener listener) {
    addListener(listener);
  }

  public void addListener(TimepointPredictionListener listener) {
    _listeners.add(listener);
  }

  public void removeListener(TimepointPredictionListener listener) {
    _listeners.remove(listener);
  }

  public void startup() {
    try {

      loadTripIdMappingFromFile();

      System.out.println("=== Starting timepoint receiver ===");
      if (!_tripIdMapping.isEmpty())
        System.out.println("  tripIdMappings=" + _tripIdMapping.size());
      _receiver = new TimepointPredictionReceiver(_serverName, _serverPort);
      _receiver.start();

    } catch (IOException ex) {
      _log.error("error starting transit prediction data receiver", ex);
    }
  }

  public void shutdown() {
    _receiver.stop();
  }

  /****
   * Statistics Methods
   ****/

  public int getPredictionCount() {
    return _predictionCount;
  }

  public int getMappedTripIdCount() {
    return _mappedTripIdCount;
  }

  /*****
   * 
   ****/

  private void loadTripIdMappingFromFile() throws FileNotFoundException,
      IOException {

    if (_tripIdMappingFile == null || !_tripIdMappingFile.exists())
      return;

    BufferedReader reader = new BufferedReader(new FileReader(
        _tripIdMappingFile));
    String line = null;

    while ((line = reader.readLine()) != null) {
      int index = line.indexOf(' ');
      if (index == -1)
        throw new IllegalStateException("bad timepoint mapping line: " + line);
      String fromTripId = line.substring(0, index);
      String toTripId = line.substring(index + 1);
      _tripIdMapping.put(fromTripId, toTripId);
    }
  }

  private void parsePredictions(Hashtable<?, ?> ht) {
    List<TimepointPrediction> predictions = new ArrayList<TimepointPrediction>();

    if (ht.containsKey("PREDICTIONS")) {
      ContentsData data = (ContentsData) ht.get("PREDICTIONS");
      data.resetRowIndex();
      while (data.next()) {
        TimepointPrediction p = new TimepointPrediction();
        p.setAgencyId(data.getString(0));
        if (_defaultAgencyId != null)
          p.setAgencyId(_defaultAgencyId);
        p.setBlockId(data.getString(1));
        p.setTrackerTripId(data.getString(2));
        p.setTimepointId(data.getString(3));
        p.setScheduledTime(data.getInt(4));
        p.setVehicleId(data.getInt(6));
        p.setPredictorType(data.getString(12));
        p.setGoalTime(data.getInt(13));
        p.setGoalDeviation(data.getInt(14));

        String tripId = _tripIdMapping.get(p.getTrackerTripId());
        if (tripId != null) {
          p.setTrackerTripId(tripId);
          _mappedTripIdCount++;
        }

        _predictionCount++;
        predictions.add(p);
      }
    }

    if (!predictions.isEmpty())
      firePredictions(predictions);
  }

  private void firePredictions(List<TimepointPrediction> predictions) {

    for (TimepointPredictionListener listener : _listeners)
      listener.handleTimepointPredictions(predictions);
  }

  private class TimepointPredictionReceiver extends SddReceiver {

    public TimepointPredictionReceiver(String serverName, int serverPort)
        throws IOException {
      super(serverName, serverPort);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void extractedDataReceived(Hashtable ht, String serialNum) {
      super.extractedDataReceived(ht, serialNum);

      try {
        parsePredictions(ht);
      } catch (Throwable ex) {
        _log.error("error parsing predictions from sdd data stream", ex);
      }
    }

  }
}
