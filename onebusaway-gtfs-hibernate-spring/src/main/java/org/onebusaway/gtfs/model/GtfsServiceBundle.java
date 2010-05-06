package org.onebusaway.gtfs.model;

import java.io.File;

public class GtfsServiceBundle {

  private File _path;

  public GtfsServiceBundle() {

  }

  public GtfsServiceBundle(File path) {
    _path = path;
  }

  public void setPath(File path) {
    _path = path;
  }

  public File getCalendarServiceDataPath() {
    return new File(_path, "CalendarServiceData.obj");
  }
}
