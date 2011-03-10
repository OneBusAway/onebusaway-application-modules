package org.onebusaway.transit_data_federation.impl.realtime.mybus;

import its.SQL.ContentsData;
import its.backbone.sdd.SddReceiver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.onebusaway.container.model.HasListeners;
import org.onebusaway.container.model.Listeners;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.impl.calendar.BlockCalendarService;
import org.onebusaway.transit_data_federation.services.TransitGraphDao;
import org.onebusaway.transit_data_federation.services.realtime.VehiclePositionListener;
import org.onebusaway.transit_data_federation.services.realtime.VehiclePositionRecord;
import org.onebusaway.transit_data_federation.services.tripplanner.TripEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class TimepointPredictionServiceImpl implements
    HasListeners<VehiclePositionListener> {

  private final Logger _log = LoggerFactory.getLogger(TimepointPredictionServiceImpl.class);

  private static final String TIMEPOINT_PREDICTION_SERVER_NAME = "carpool.its.washington.edu";

  private static final int TIMEPOINT_PREDICTION_SERVER_PORT = 9002;

  private TimepointPredictionReceiver _receiver;

  private Listeners<VehiclePositionListener> _listeners = new Listeners<VehiclePositionListener>();

  private String _serverName = TIMEPOINT_PREDICTION_SERVER_NAME;

  private int _serverPort = TIMEPOINT_PREDICTION_SERVER_PORT;

  private File _tripIdMappingFile;

  private Map<String, String> _tripIdMapping = new HashMap<String, String>();

  private List<String> _agencyIds = Arrays.asList("1", "40");

  private int _predictionCount = 0;

  private int _mappedTripIdCount = 0;

  private BlockCalendarService _blockCalendarService;

  private TransitGraphDao _transitGraph;

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

  public void setAgencyId(String agencyId) {
    _agencyIds = Arrays.asList(agencyId);
  }

  public void setAgencyIds(List<String> agencyIds) {
    _agencyIds = agencyIds;
  }

  @Autowired
  public void setTransitGraphDao(TransitGraphDao transitGraph) {
    _transitGraph = transitGraph;
  }

  @Autowired
  public void setBlockCalendarService(BlockCalendarService blockCalendarService) {
    _blockCalendarService = blockCalendarService;
  }

  @Override
  public void addListener(VehiclePositionListener listener) {
    _listeners.addListener(listener);
  }

  @Override
  public void removeListener(VehiclePositionListener listener) {
    _listeners.removeListener(listener);
  }

  @PostConstruct
  public void startup() {
    try {
      loadTripIdMappingFromFile();

      _log.info("Starting timepoint receiver");
      if (!_tripIdMapping.isEmpty())
        System.out.println("  tripIdMappings=" + _tripIdMapping.size());
      _receiver = new TimepointPredictionReceiver(_serverName, _serverPort);
      _receiver.start();

    } catch (IOException ex) {
      _log.error("error starting transit prediction data receiver", ex);
    }
  }

  @PreDestroy
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

    Map<AgencyAndId, List<TimepointPrediction>> predictionsByTripId = new HashMap<AgencyAndId, List<TimepointPrediction>>();

    if (ht.containsKey("PREDICTIONS")) {
      ContentsData data = (ContentsData) ht.get("PREDICTIONS");
      data.resetRowIndex();
      while (data.next()) {

        _predictionCount++;

        String agencyId = data.getString(0);

        // We override the agency id in the stream, since it's usually 0
        if( ! _agencyIds.isEmpty() )
          agencyId = _agencyIds.get(0);
        
        String tripId = data.getString(2);

        TripEntry tripEntry = getTripEntryForId(agencyId, tripId);

        if (tripEntry == null)
          continue;

        TimepointPrediction record = new TimepointPrediction();

        // record.setBlockId(new AgencyAndId(agencyId, data.getString(1)));
        record.setTripId(tripEntry.getId());
        record.setBlockId(tripEntry.getBlockId());

        String tripAgencyId = tripEntry.getId().getAgencyId();
        record.setVehicleId(new AgencyAndId(tripAgencyId, data.getString(6)));
        record.setScheduleDeviation(data.getInt(14));
        record.setTimepointId(new AgencyAndId(agencyId, data.getString(3)));
        record.setTimepointScheduledTime(data.getInt(4));
        record.setTimepointPredictedTime(data.getInt(13));
        record.setTimeOfPrediction(data.getInt(9));

        // Indicates that we don't have any real-time predictions for this
        // record
        if (record.getTimepointPredictedTime() == -1)
          continue;

        List<TimepointPrediction> records = predictionsByTripId.get(record.getTripId());
        if (records == null) {
          records = new ArrayList<TimepointPrediction>();
          predictionsByTripId.put(record.getTripId(), records);
        }

        records.add(record);
      }
    }

    if (predictionsByTripId.isEmpty())
      return;

    long t = System.currentTimeMillis();
    Date from = new Date(t - 30 * 60 * 1000);
    Date to = new Date(t + 30 * 60 * 1000);

    List<VehiclePositionRecord> records = new ArrayList<VehiclePositionRecord>();

    for (List<TimepointPrediction> recordsForTrip : predictionsByTripId.values()) {
      VehiclePositionRecord record = getBestScheduleAdherenceRecord(recordsForTrip);
      List<Date> dates = _blockCalendarService.getServiceDatesWithinRangeForBlockId(
          record.getBlockId(), from, to);
      if (dates.size() != 1)
        continue;
      record.setServiceDate(dates.get(0).getTime());
      records.add(record);
    }

    for (VehiclePositionListener listener : _listeners)
      listener.handleVehiclePositionRecords(records);
  }

  private VehiclePositionRecord getBestScheduleAdherenceRecord(
      List<TimepointPrediction> recordsForTrip) {

    TimepointPrediction best = getBestTimepointPrediction(recordsForTrip);

    VehiclePositionRecord r = new VehiclePositionRecord();
    r.setBlockId(best.getBlockId());
    r.setCurrentTime(System.currentTimeMillis());
    r.setScheduleDeviation(best.getScheduleDeviation());
    r.setTimepointId(best.getTimepointId());
    r.setTimepointPredictedTime(best.getTimepointPredictedTime());
    r.setTimepointScheduledTime(best.getTimepointScheduledTime());
    r.setTripId(best.getTripId());
    r.setVehicleId(best.getVehicleId());
    return r;
  }

  private TimepointPrediction getBestTimepointPrediction(
      List<TimepointPrediction> recordsForTrip) {

    TimepointPrediction prev = null;
    int prevDelta = -1;

    for (TimepointPrediction record : recordsForTrip) {

      int delta = record.getTimepointPredictedTime()
          - record.getTimeOfPrediction();

      if (prev != null) {
        if (!prev.getTimepointId().equals(record.getTimepointId())
            && prevDelta >= 0 && delta > prevDelta)
          break;
      }

      prev = record;
      prevDelta = delta;
    }

    return prev;
  }

  private TripEntry getTripEntryForId(String agencyId, String tripId) {

    String mappedTripId = _tripIdMapping.get(tripId);
    if (mappedTripId != null)
      tripId = mappedTripId;

    // TODO - what about ST routes?
    for (String aid : _agencyIds) {
      AgencyAndId fullTripId = new AgencyAndId(aid, tripId);
      TripEntry tripEntry = _transitGraph.getTripEntryForId(fullTripId);
      if (tripEntry != null)
        return tripEntry;
    }

    return null;
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
