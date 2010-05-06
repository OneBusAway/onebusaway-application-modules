package org.onebusaway.transit_data_federation.impl.predictions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.TransitDataFederationBaseTestSupport;
import org.onebusaway.transit_data_federation.model.predictions.ScheduleDeviation;
import org.onebusaway.transit_data_federation.model.predictions.TripTimePrediction;
import org.onebusaway.transit_data_federation.services.predictions.TripTimePredictionDao;

public class TripTimePredictionServiceImplTest {

  private TripTimePredictionServiceImpl _service;
  private TripTimePredictionDao _dao;

  @Before
  public void setup() {
    _service = new TripTimePredictionServiceImpl();
    _service.setTripTimePredictionsCache(TransitDataFederationBaseTestSupport.createCache());

    _dao = Mockito.mock(TripTimePredictionDao.class);
    _service.setTripTimePredictionDao(_dao);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void test() throws InterruptedException {

    AgencyAndId tripId = new AgencyAndId("1", "trip");
    long serviceDate = 1000;
    long time = 2000;
    int scheduleDeviation = 5;
    AgencyAndId vehicleId = new AgencyAndId("1", "vehicle");

    _service.setPersistTripTimePredictions(true);
    TripTimePrediction sourcePrediction = new TripTimePrediction(tripId,
        serviceDate, time, scheduleDeviation, vehicleId);
    _service.start();
    _service.putScheduleDeviationPrediction(sourcePrediction);
    Thread.sleep(2000);
    _service.stop();
    
    // TODO : This seems like a hack just to get an instance of Class<List<TripTimePrediction>>
    List<TripTimePrediction> v = new ArrayList<TripTimePrediction>();
    Class<List<TripTimePrediction>> c = (Class<List<TripTimePrediction>>) v.getClass();
    
    ArgumentCaptor<List<TripTimePrediction>> predictionCapture = ArgumentCaptor.forClass(c);
    Mockito.verify(_dao).saveTripTimePredictions(predictionCapture.capture());

    List<TripTimePrediction> predictions = predictionCapture.getValue();
    assertEquals(1,predictions.size());
    TripTimePrediction prediction = predictions.get(0);
    assertEquals(tripId, prediction.getTripId());
    assertEquals(serviceDate, prediction.getServiceDate());
    assertEquals(time, prediction.getTime());
    assertEquals(scheduleDeviation, prediction.getScheduleDeviation());

    ScheduleDeviation deviation = _service.getScheduledDeviationPrediction(
        tripId, serviceDate, 2050);

    assertEquals(5, deviation.getScheduleDeviation());
    assertTrue(deviation.isPredicted());

    sourcePrediction = new TripTimePrediction(tripId,serviceDate,5000,9,vehicleId);
    _service.putScheduleDeviationPrediction(sourcePrediction);

    deviation = _service.getScheduledDeviationPrediction(tripId, serviceDate,
        2000);
    assertEquals(5, deviation.getScheduleDeviation());
    deviation = _service.getScheduledDeviationPrediction(tripId, serviceDate,
        3500);
    assertEquals(7, deviation.getScheduleDeviation());
    deviation = _service.getScheduledDeviationPrediction(tripId, serviceDate,
        5000);
    assertEquals(9, deviation.getScheduleDeviation());
  }
}
