package org.onebusaway.geocoder.impl;

import static org.junit.Assert.assertEquals;

import org.onebusaway.geocoder.model.GeocoderResult;
import org.onebusaway.geocoder.model.GeocoderResults;

import org.junit.Test;

import java.util.List;

public class YahooGeocoderImplTest {

  @Test
  public void testGeocoder() {

    YahooGeocoderImpl geocoder = new YahooGeocoderImpl();
    geocoder.setAppId("YD-9G7bey8_JXxQP6rxl.fBFGgCdNjoDMACQA--");

    GeocoderResults results = geocoder.geocode("98105");

    List<GeocoderResult> records = results.getResults();

    assertEquals(1, records.size());

    GeocoderResult result = records.get(0);

    assertEquals(47.663640, result.getLatitude(), 0.000001);
    assertEquals(-122.298869, result.getLongitude(), 0.000001);
    assertEquals("",result.getAddress());
    assertEquals("Seattle", result.getCity());
    assertEquals("WA", result.getAdministrativeArea());
    assertEquals("98105", result.getPostalCode());
    assertEquals("US", result.getCountry());
  }
}
