package org.onebusaway.transit_data_federation.impl.predictions;

import static org.junit.Assert.assertEquals;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.TransitDataFederationBaseTest;
import org.onebusaway.transit_data_federation.model.predictions.TripTimePrediction;
import org.onebusaway.transit_data_federation.services.TransitDataFederationMutableDao;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

public class TripTimePredictionServiceImplTest {

  private TripTimePredictionServiceImpl _service;
  private TransitDataFederationMutableDao _dao;

  @Before
  public void setup() {
    _service = new TripTimePredictionServiceImpl();
    _service.setTripTimePredictionsCache(TransitDataFederationBaseTest.createCache());

    _dao = Mockito.mock(TransitDataFederationMutableDao.class);
    _service.setTransitDataFederationMutableDao(_dao);
  }

  @Test
  public void test() {

    AgencyAndId tripId = new AgencyAndId("1", "trip");
    long serviceDate = 1000;
    long time = 2000;
    int scheduleDeviation = 5;

    _service.putScheduleDeviationPrediction(tripId, serviceDate, time,
        scheduleDeviation);

    ArgumentCaptor<TripTimePrediction> predictionCapture = ArgumentCaptor.forClass(TripTimePrediction.class);
    Mockito.verify(_dao).save(predictionCapture.capture());

    TripTimePrediction prediction = predictionCapture.getValue();
    assertEquals(tripId, prediction.getTripId());
    assertEquals(serviceDate, prediction.getServiceDate());
    assertEquals(time, prediction.getTime());
    assertEquals(scheduleDeviation, prediction.getScheduleDeviation());

    int deviation = _service.getScheduledDeviationPrediction(tripId,
        serviceDate, 2050);

    assertEquals(5, deviation);

    _service.putScheduleDeviationPrediction(tripId, serviceDate, 5000, 9);

    assertEquals(5, _service.getScheduledDeviationPrediction(tripId,
        serviceDate, 2000));
    assertEquals(7, _service.getScheduledDeviationPrediction(tripId,
        serviceDate, 3500));
    assertEquals(9, _service.getScheduledDeviationPrediction(tripId,
        serviceDate, 5000));
  }
}
