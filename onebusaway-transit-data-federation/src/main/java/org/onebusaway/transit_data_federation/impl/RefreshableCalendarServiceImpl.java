package org.onebusaway.transit_data_federation.impl;

import java.io.File;
import java.io.IOException;

import javax.annotation.PostConstruct;

import org.onebusaway.container.refresh.Refreshable;
import org.onebusaway.gtfs.impl.calendar.CalendarServiceImpl;
import org.onebusaway.gtfs.model.calendar.CalendarServiceData;
import org.onebusaway.transit_data_federation.bundle.model.FederatedTransitDataBundle;
import org.onebusaway.utility.ObjectSerializationLibrary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RefreshableCalendarServiceImpl extends CalendarServiceImpl {

  private FederatedTransitDataBundle _bundle;

  @Autowired
  public void setBundle(FederatedTransitDataBundle bundle) {
    _bundle = bundle;
  }

  @PostConstruct
  @Refreshable(dependsOn = RefreshableResources.CALENDAR_DATA)
  public void setup() throws IOException, ClassNotFoundException {
    File path = _bundle.getCalendarServiceDataPath();
    if (path.exists()) {
      CalendarServiceData data = ObjectSerializationLibrary.readObject(path);
      setData(data);
    } else {
      setData(new CalendarServiceData());
    }
  }

}
