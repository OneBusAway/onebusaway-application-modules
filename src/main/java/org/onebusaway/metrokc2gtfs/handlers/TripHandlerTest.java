package org.onebusaway.metrokc2gtfs.handlers;

import org.junit.Test;
import org.onebusaway.csv.CsvEntityWriter;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Trip;

import java.io.File;

public class TripHandlerTest {

  @Test
  public void testGo() {
    
    CsvEntityWriter writer = new CsvEntityWriter();
    writer.setOutputLocation(new File("/tmp"));
    
    StopTime st = new StopTime();

    Trip trip = new Trip();
    trip.setId("trip123");
    st.setTrip(trip);

    Stop stop = new Stop();
    stop.setId("stop123");
    st.setStop(stop);
    
    writer.handleEntity(st);
    writer.close();
  }
}
