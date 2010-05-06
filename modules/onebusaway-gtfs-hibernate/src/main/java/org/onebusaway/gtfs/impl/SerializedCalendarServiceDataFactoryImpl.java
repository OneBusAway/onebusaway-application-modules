package org.onebusaway.gtfs.impl;

import org.onebusaway.gtfs.services.calendar.CalendarServiceData;
import org.onebusaway.gtfs.services.calendar.CalendarServiceDataFactory;
import org.onebusaway.utility.ObjectSerializationLibrary;

import java.io.File;

public class SerializedCalendarServiceDataFactoryImpl implements
    CalendarServiceDataFactory {

  private File _path;

  public void setPath(File path) {
    _path = path;
  }

  public CalendarServiceData createServiceCalendarData() {
    try {
      return ObjectSerializationLibrary.readObject(_path);
    } catch (Exception ex) {
      throw new IllegalStateException("error reading service calendar data at "
          + _path, ex);
    }
  }

}
