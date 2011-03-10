package org.onebusaway.transit_data_federation.impl.offline;

import org.onebusaway.gtfs.impl.calendar.CalendarServiceDataFactoryImpl;
import org.onebusaway.gtfs.impl.calendar.CalendarServiceImpl;
import org.onebusaway.gtfs.model.calendar.CalendarServiceData;
import org.onebusaway.gtfs.services.GtfsRelationalDao;
import org.onebusaway.gtfs.services.calendar.CalendarService;
import org.onebusaway.transit_data_federation.services.RunnableWithOutputPath;
import org.onebusaway.utility.ObjectSerializationLibrary;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;

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
public class CalendarServiceDataTask implements RunnableWithOutputPath {

  private GtfsRelationalDao _dao;

  private File _outputPath;

  @Autowired
  public void setGtfsDao(GtfsRelationalDao dao) {
    _dao = dao;
  }

  public void setOutputPath(File path) {
    _outputPath = path;
  }

  public void run() {

    CalendarServiceDataFactoryImpl factory = new CalendarServiceDataFactoryImpl();
    factory.setGtfsDao(_dao);
    CalendarServiceData data = factory.createData();

    try {
      ObjectSerializationLibrary.writeObject(_outputPath, data);
    } catch (IOException e) {
      throw new IllegalStateException(
          "error serializing service calendar data", e);
    }
  }
}
