package org.onebusaway.kcmetro_tcip.impl;

import static org.junit.Assert.assertEquals;

import org.onebusaway.kcmetro_tcip.model.TimepointPrediction;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TimepointPredictionServiceImplTest {

  private MockPredictionTransmitter _transmitter;

  @Before
  public void before() throws Exception {
    _transmitter = new MockPredictionTransmitter(8080);
    _transmitter.sendSchema();
  }

  @After
  public void after() {
    _transmitter.stop();
  }

  @Test
  public void testSinglePrediction() throws Exception {

    TimepointPredictionHandler handler = new TimepointPredictionHandler();

    TimepointPredictionServiceImpl service = new TimepointPredictionServiceImpl();
    service.setServerName("localhost");
    service.setServerPort(8080);
    service.addListener(handler);
    service.startup();

    TimepointPrediction expected = getPrediction();

    _transmitter.sendData(expected);

    Thread.sleep(1000);

    service.shutdown();

    List<List<TimepointPrediction>> allPredictions = handler.getPredictions();
    assertEquals(1, allPredictions.size());
    List<TimepointPrediction> predictions = allPredictions.get(0);
    assertEquals(1, predictions.size());
    TimepointPrediction actual = predictions.get(0);
    assertEquals(expected.getAgencyId(), actual.getAgencyId());
    assertEquals(expected.getBlockId(), actual.getBlockId());
    assertEquals(expected.getGoalDeviation(), actual.getGoalDeviation());
    assertEquals(expected.getGoalTime(), actual.getGoalTime());
    assertEquals(expected.getPredictorType(), actual.getPredictorType());
    assertEquals(expected.getScheduledTime(), actual.getScheduledTime());
    assertEquals(expected.getTimepointId(), actual.getTimepointId());
    assertEquals(expected.getTrackerTripId(), actual.getTrackerTripId());
    assertEquals(expected.getVehicleId(), actual.getVehicleId());
  }

  @Test
  public void testAgencyAndTripOverride() throws Exception {

    TimepointPredictionHandler handler = new TimepointPredictionHandler();

    Map<String, String> tripIdMapping = new HashMap<String, String>();
    tripIdMapping.put("13579", "24680");
    TimepointPredictionServiceImpl service = new TimepointPredictionServiceImpl();
    service.setServerName("localhost");
    service.setServerPort(8080);
    service.setDefaultAgencyId("override");
    service.setTripIdMapping(tripIdMapping);
    service.addListener(handler);
    service.startup();

    TimepointPrediction expected = getPrediction();

    _transmitter.sendData(expected);

    Thread.sleep(1000);

    service.shutdown();

    List<List<TimepointPrediction>> allPredictions = handler.getPredictions();
    assertEquals(1, allPredictions.size());
    List<TimepointPrediction> predictions = allPredictions.get(0);
    assertEquals(1, predictions.size());
    TimepointPrediction actual = predictions.get(0);
    assertEquals("override", actual.getAgencyId());
    assertEquals(expected.getBlockId(), actual.getBlockId());
    assertEquals(expected.getGoalDeviation(), actual.getGoalDeviation());
    assertEquals(expected.getGoalTime(), actual.getGoalTime());
    assertEquals(expected.getPredictorType(), actual.getPredictorType());
    assertEquals(expected.getScheduledTime(), actual.getScheduledTime());
    assertEquals(expected.getTimepointId(), actual.getTimepointId());
    assertEquals("24680", actual.getTrackerTripId());
    assertEquals(expected.getVehicleId(), actual.getVehicleId());
  }

  @Test
  public void testTripFileOverride() throws Exception {

    TimepointPredictionHandler handler = new TimepointPredictionHandler();

    Map<String, String> tripIdMapping = new HashMap<String, String>();
    tripIdMapping.put("13579", "24680");

    File tmp = getTripIdMappingAsFile(tripIdMapping);

    TimepointPredictionServiceImpl service = new TimepointPredictionServiceImpl();
    service.setServerName("localhost");
    service.setServerPort(8080);
    service.setDefaultAgencyId("override");
    service.setTripIdMappingPath(tmp);
    service.addListener(handler);
    service.startup();

    TimepointPrediction expected = getPrediction();

    _transmitter.sendData(expected);

    Thread.sleep(1000);

    service.shutdown();

    List<List<TimepointPrediction>> allPredictions = handler.getPredictions();
    assertEquals(1, allPredictions.size());
    List<TimepointPrediction> predictions = allPredictions.get(0);
    assertEquals(1, predictions.size());
    TimepointPrediction actual = predictions.get(0);
    assertEquals("override", actual.getAgencyId());
    assertEquals(expected.getBlockId(), actual.getBlockId());
    assertEquals(expected.getGoalDeviation(), actual.getGoalDeviation());
    assertEquals(expected.getGoalTime(), actual.getGoalTime());
    assertEquals(expected.getPredictorType(), actual.getPredictorType());
    assertEquals(expected.getScheduledTime(), actual.getScheduledTime());
    assertEquals(expected.getTimepointId(), actual.getTimepointId());
    assertEquals("24680", actual.getTrackerTripId());
    assertEquals(expected.getVehicleId(), actual.getVehicleId());
  }

  /****
   * Private Methods
   ****/

  private TimepointPrediction getPrediction() {
    TimepointPrediction expected = new TimepointPrediction();

    expected.setAgencyId("1");
    expected.setBlockId("1234");
    expected.setGoalDeviation(150);
    expected.setGoalTime(1000);
    expected.setPredictorType("p");
    expected.setScheduledTime(850);
    expected.setTimepointId("4321");
    expected.setTrackerTripId("13579");
    expected.setVehicleId(12);
    return expected;
  }

  private File getTripIdMappingAsFile(Map<String, String> tripIdMapping)
      throws IOException {
    File tmp = File.createTempFile("TimepointPredictionServiceImplTest-",
        ".tmp");
    tmp.deleteOnExit();
    PrintWriter writer = new PrintWriter(new FileWriter(tmp));

    for (Map.Entry<String, String> entry : tripIdMapping.entrySet())
      writer.println(entry.getKey() + " " + entry.getValue());
    writer.close();
    return tmp;
  }

  private static class TimepointPredictionHandler implements
      TimepointPredictionListener {

    private List<List<TimepointPrediction>> _predictions = new ArrayList<List<TimepointPrediction>>();

    public List<List<TimepointPrediction>> getPredictions() {
      return _predictions;
    }

    public void handleTimepointPredictions(List<TimepointPrediction> predictions) {
      _predictions.add(predictions);
    }
  }
}
