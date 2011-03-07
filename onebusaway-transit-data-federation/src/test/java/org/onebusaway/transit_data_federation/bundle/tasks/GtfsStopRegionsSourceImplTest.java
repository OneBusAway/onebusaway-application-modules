package org.onebusaway.transit_data_federation.bundle.tasks;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.mockito.Mockito;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.services.GtfsDao;
import org.onebusaway.transit_data_federation.bundle.tasks.GtfsStopRegionsSourceImpl;

import com.vividsolutions.jts.geom.Envelope;

public class GtfsStopRegionsSourceImplTest {

  @Test
  public void test() {

    GtfsDao dao = Mockito.mock(GtfsDao.class);

    Stop a = new Stop();
    a.setLat(47.664809318453564);
    a.setLon(-122.3023796081543);

    Stop b = new Stop();
    b.setLat(47.66931784410792);
    b.setLon(-122.38992691040039);

    Mockito.when(dao.getAllStops()).thenReturn(Arrays.asList(a, b));

    GtfsStopRegionsSourceImpl source = new GtfsStopRegionsSourceImpl();
    source.setGtfsDao(dao);
    source.setRadius(2500);

    Iterable<Envelope> it = source.getRegions();
    List<Envelope> regions = new ArrayList<Envelope>();
    for (Envelope env : it)
      regions.add(env);

    assertEquals(2, regions.size());

    Envelope envA = regions.get(0);
    assertEquals(-122.3023796081543, envA.centre().x,0.0001);
    assertEquals(47.664809318453564, envA.centre().y,0.0001);
    assertEquals(0.04496600971673814, envA.getHeight(),0.0001);
    assertEquals(0.066767981533431, envA.getWidth(),0.0001);
    
    Envelope envB = regions.get(1);
    assertEquals(-122.38992691040039, envB.centre().x,0.0001);
    assertEquals(47.66931784410792, envB.centre().y,0.0001);
    assertEquals(0.04496600971673814, envB.getHeight(),0.0001);
    assertEquals(0.066767981533431, envB.getWidth(),0.0001);

  }
}
