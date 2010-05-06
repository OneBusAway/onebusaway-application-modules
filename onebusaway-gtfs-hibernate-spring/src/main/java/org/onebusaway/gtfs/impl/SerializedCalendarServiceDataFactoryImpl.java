package org.onebusaway.gtfs.impl;

import java.io.File;

import org.onebusaway.gtfs.model.calendar.CalendarServiceData;
import org.onebusaway.gtfs.services.calendar.CalendarServiceDataFactory;
import org.onebusaway.utility.ObjectSerializationLibrary;

public class SerializedCalendarServiceDataFactoryImpl implements
    CalendarServiceDataFactory {

  private File _path;

  public void setPath(File path) {
    _path = path;
  }

  public CalendarServiceData createData() {
    try {
      return ObjectSerializationLibrary.readObject(_path);
    } catch (Exception ex) {
      throw new IllegalStateException("error reading service calendar data at "
          + _path, ex);
    }
  }

}
