package org.onebusaway.kcmetro_tcip.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.services.calendar.CalendarService;
import org.onebusaway.kcmetro.model.TimepointToStopMapping;
import org.onebusaway.kcmetro_tcip.model.TimepointPrediction;
import org.onebusaway.kcmetro_tcip.services.KCMetroTcipDao;
import org.onebusaway.tcip.model.CPTStoppointIden;
import org.onebusaway.tcip.model.CPTVehicleIden;
import org.onebusaway.tcip.model.PISchedAdherenceCountdown;
import org.onebusaway.tcip.model.SCHTripIden;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;

import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TimepointPredictionToPiSchedAdherenceCountdownServiceImplTest {
  
  @SuppressWarnings("unchecked")
  @Test
  public void test() {

    PiScheduleAdherenceCountdownHandler handler = new PiScheduleAdherenceCountdownHandler();

    TimepointPredictionToPiSchedAdherenceCountdownServiceImpl service = new TimepointPredictionToPiSchedAdherenceCountdownServiceImpl();
    service.addListener(handler);
    
    CacheManager cacheManager = new CacheManager(getClass().getResourceAsStream("ehcache.xml"));
    cacheManager.addCache("cache");
    Cache cache = cacheManager.getCache("cache");
    service.setTimepointToStopMappingsByTrackerTripIdCache(cache);
    
    CalendarService mockCalendarService = Mockito.mock(CalendarService.class);
    Map<AgencyAndId, List<Date>> serviceDates = new HashMap<AgencyAndId, List<Date>>();

    Date now = getDateAsDay(new Date());
    Date tomorrow = addDays(now, 1);

    serviceDates.put(new AgencyAndId("1", "111-WEEK"),
        Arrays.asList(now));
    serviceDates.put(new AgencyAndId("1", "111-SUN"),
        Arrays.asList(tomorrow));

    Mockito.when(
        mockCalendarService.getServiceDatesWithinRange(Mockito.any(Set.class),
            Mockito.any(Date.class), Mockito.any(Date.class))).thenReturn(
        serviceDates);
    service.setCalendarService(mockCalendarService);

    List<TimepointToStopMapping> mappings = getMappings();
    KCMetroTcipDao mockDao = Mockito.mock(KCMetroTcipDao.class);
    Mockito.when(
        mockDao.getTimepointToStopMappingsForTrackerTripId(new AgencyAndId("1",
            "tripId13579"))).thenReturn(mappings);
    service.setKCMetroTcipDao(mockDao);

    List<TimepointPrediction> predictions = getPredictions(now);
    service.handleTimepointPredictions(predictions);

    List<List<PISchedAdherenceCountdown>> allEvents = handler.getAllEvents();

    assertEquals(1, allEvents.size());
    List<PISchedAdherenceCountdown> events = allEvents.get(0);
    assertEquals(1, events.size());
    PISchedAdherenceCountdown event = events.get(0);
    
    assertTrue(Math.abs(event.getNextArrivalCountdown()-150) < 2);
    
    CPTStoppointIden stoppoint = event.getStoppoint();
    assertEquals("1",stoppoint.getAgencyId());
    assertEquals("stopId123",stoppoint.getStoppointId());

    SCHTripIden trip = event.getTrip();
    assertEquals("1",trip.getAgencyId());
    assertEquals("tripId13579-111-WEEK",trip.getTripId());
    
    CPTVehicleIden vehicle = event.getVehicle();
    assertEquals("1", vehicle.getAgencyId());
    assertEquals("12", vehicle.getVehicleId());
  }

  private List<TimepointPrediction> getPredictions(Date today) {

    TimepointPrediction prediction = new TimepointPrediction();

    int scheduleTime = (int) ((System.currentTimeMillis() - today.getTime())/1000);
    
    prediction.setAgencyId("1");
    prediction.setBlockId("1234");
    prediction.setGoalDeviation(150);
    prediction.setGoalTime(scheduleTime+150);
    prediction.setPredictorType("p");
    prediction.setScheduledTime(scheduleTime);
    prediction.setTimepointId("timepointId4321");
    prediction.setTrackerTripId("tripId13579");
    prediction.setVehicleId(12);

    return Arrays.asList(prediction);
  }

  private List<TimepointToStopMapping> getMappings() {

    TimepointToStopMapping mappingA = new TimepointToStopMapping();
    mappingA.setId(1);
    mappingA.setServiceId(new AgencyAndId("1", "111-WEEK"));
    mappingA.setStopId(new AgencyAndId("1", "stopId123"));
    mappingA.setTimepointId(new AgencyAndId("1", "timepointId4321"));
    mappingA.setTrackerTripId(new AgencyAndId("1", "tripId13579"));
    mappingA.setTripId(new AgencyAndId("1", "tripId13579-111-WEEK"));

    TimepointToStopMapping mappingB = new TimepointToStopMapping();
    mappingB.setId(2);
    mappingB.setServiceId(new AgencyAndId("1", "111-SUN"));
    mappingB.setStopId(new AgencyAndId("1", "stopId123"));
    mappingB.setTimepointId(new AgencyAndId("1", "timepointId4321"));
    mappingB.setTrackerTripId(new AgencyAndId("1", "tripId13579"));
    mappingB.setTripId(new AgencyAndId("1", "tripId13579-111-SUN"));

    TimepointToStopMapping mappingC = new TimepointToStopMapping();
    mappingC.setId(3);
    mappingC.setServiceId(new AgencyAndId("1", "111-WEEK"));
    mappingC.setStopId(new AgencyAndId("1", "stopId456"));
    mappingC.setTimepointId(new AgencyAndId("1", "timepointId9876"));
    mappingC.setTrackerTripId(new AgencyAndId("1", "tripId13579"));
    mappingC.setTripId(new AgencyAndId("1", "tripId13579-111-WEEK"));

    TimepointToStopMapping mappingD = new TimepointToStopMapping();
    mappingD.setId(4);
    mappingD.setServiceId(new AgencyAndId("1", "111-SUN"));
    mappingD.setStopId(new AgencyAndId("1", "stopId456"));
    mappingD.setTimepointId(new AgencyAndId("1", "timepointId9876"));
    mappingD.setTrackerTripId(new AgencyAndId("1", "tripId13579"));
    mappingD.setTripId(new AgencyAndId("1", "tripId13579-111-SUN"));

    return Arrays.asList(mappingA, mappingB, mappingC, mappingD);
  }

  private Date getDateAsDay(Date date) {
    Calendar c = Calendar.getInstance();
    c.setTime(date);
    c.set(Calendar.HOUR_OF_DAY, 0);
    c.set(Calendar.MINUTE, 0);
    c.set(Calendar.SECOND, 0);
    c.set(Calendar.MILLISECOND, 0);
    return c.getTime();
  }

  private Date addDays(Date date, int days) {
    Calendar c = Calendar.getInstance();
    c.add(Calendar.DAY_OF_YEAR, days);
    return c.getTime();
  }

  private static class PiScheduleAdherenceCountdownHandler implements
      PISchedAdherenceCountdownListener {

    private List<List<PISchedAdherenceCountdown>> _allEvents = new ArrayList<List<PISchedAdherenceCountdown>>();

    public List<List<PISchedAdherenceCountdown>> getAllEvents() {
      return _allEvents;
    }

    public void handle(List<PISchedAdherenceCountdown> events) {
      _allEvents.add(events);
    }

  }
}
