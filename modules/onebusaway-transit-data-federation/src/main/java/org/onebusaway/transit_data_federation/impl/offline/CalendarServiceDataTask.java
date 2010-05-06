package org.onebusaway.transit_data_federation.impl.offline;

import org.onebusaway.gtfs.impl.calendar.CalendarServiceDataFactoryImpl;
import org.onebusaway.gtfs.services.GtfsRelationalDao;
import org.onebusaway.gtfs.services.calendar.CalendarServiceData;
import org.onebusaway.transit_data_federation.services.RunnableWithOutputPath;
import org.onebusaway.utility.ObjectSerializationLibrary;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;

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
    CalendarServiceData data = factory.createServiceCalendarData();

    try {
      ObjectSerializationLibrary.writeObject(_outputPath, data);
    } catch (IOException e) {
      throw new IllegalStateException(
          "error serializing service calendar data", e);
    }
  }
}
