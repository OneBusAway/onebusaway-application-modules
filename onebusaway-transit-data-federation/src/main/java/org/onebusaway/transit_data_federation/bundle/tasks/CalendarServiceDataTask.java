package org.onebusaway.transit_data_federation.bundle.tasks;

import java.io.IOException;

import org.onebusaway.container.refresh.RefreshService;
import org.onebusaway.gtfs.impl.calendar.CalendarServiceDataFactoryImpl;
import org.onebusaway.gtfs.impl.calendar.CalendarServiceImpl;
import org.onebusaway.gtfs.model.calendar.CalendarServiceData;
import org.onebusaway.gtfs.services.GtfsRelationalDao;
import org.onebusaway.gtfs.services.calendar.CalendarService;
import org.onebusaway.transit_data_federation.bundle.model.FederatedTransitDataBundle;
import org.onebusaway.transit_data_federation.impl.RefreshableResources;
import org.onebusaway.utility.ObjectSerializationLibrary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Pre-computes the {@link CalendarServiceData} that is needed to power a
 * {@link CalendarServiceImpl} and serializes it to disk as part of the transit
 * data bundle.
 * 
 * @author bdferris
 * @see CalendarServiceData
 * @see CalendarServiceImpl
 * @see CalendarService
 * @see CalendarServiceDataFactoryImpl
 */
@Component
public class CalendarServiceDataTask implements Runnable {

  private GtfsRelationalDao _dao;

  private FederatedTransitDataBundle _bundle;

  private RefreshService _refreshService;

  @Autowired
  public void setGtfsDao(GtfsRelationalDao dao) {
    _dao = dao;
  }

  @Autowired
  public void setBundle(FederatedTransitDataBundle bundle) {
    _bundle = bundle;
  }

  @Autowired
  public void setRefreshService(RefreshService refreshService) {
    _refreshService = refreshService;
  }

  public void run() {

    CalendarServiceDataFactoryImpl factory = new CalendarServiceDataFactoryImpl();
    factory.setGtfsDao(_dao);
    CalendarServiceData data = factory.createData();

    try {

      ObjectSerializationLibrary.writeObject(
          _bundle.getCalendarServiceDataPath(), data);
      
      _refreshService.refresh(RefreshableResources.CALENDAR_DATA);
    } catch (IOException e) {
      throw new IllegalStateException(
          "error serializing service calendar data", e);
    }
  }
}
