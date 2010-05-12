package org.onebusaway.geocoder.impl;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.cfg.Configuration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.onebusaway.geocoder.model.GeocoderResult;
import org.onebusaway.geocoder.model.GeocoderResults;
import org.onebusaway.geocoder.services.GeocoderService;

public class DatabaseCachingGeocoderImplTest {

  private SessionFactory _sessionFactory;

  @Before
  public void setup() {
    Configuration config = new AnnotationConfiguration();
    config = config.configure("org/onebusaway/geocoder/impl/hibernate-configuration.xml");
    _sessionFactory = config.buildSessionFactory();
  }

  @After
  public void tearDown() {
    if (_sessionFactory != null)
      _sessionFactory.close();
  }

  @Test
  public void test() {

    GeocoderResults results = new GeocoderResults();

    GeocoderResult result = new GeocoderResult();
    result.setAddress("101 test blvd");
    result.setAdministrativeArea("wa");
    result.setCity("seattle");
    result.setCountry("usa");
    result.setLatitude(47.0);
    result.setLongitude(-122.0);
    result.setPostalCode("98000");
    results.addResult(result);

    GeocoderService mock = Mockito.mock(GeocoderService.class);
    Mockito.when(mock.geocode("test")).thenReturn(results);

    DatabaseCachingGeocoderImpl geocoder = new DatabaseCachingGeocoderImpl();
    geocoder.setSessionFactory(_sessionFactory);
    geocoder.setGeocoderService(mock);

    GeocoderResults resultsA = geocoder.geocode("test");
    List<GeocoderResult> resultsListA = resultsA.getResults();
    assertEquals(1, resultsListA.size());
    GeocoderResult resultA = resultsListA.get(0);

    assertEquals("101 test blvd", resultA.getAddress());
    assertEquals("wa", resultA.getAdministrativeArea());
    assertEquals("seattle", resultA.getCity());
    assertEquals("usa", resultA.getCountry());
    assertEquals(47.0, resultA.getLatitude(), 0.0);
    assertEquals(-122.0, resultA.getLongitude(), 0.0);
    assertEquals("98000", resultA.getPostalCode());

    GeocoderResults resultsB = geocoder.geocode("test");
    List<GeocoderResult> resultsListB = resultsB.getResults();
    assertEquals(1, resultsListB.size());
    GeocoderResult resultB = resultsListB.get(0);

    assertEquals("101 test blvd", resultB.getAddress());
    assertEquals("wa", resultB.getAdministrativeArea());
    assertEquals("seattle", resultB.getCity());
    assertEquals("usa", resultB.getCountry());
    assertEquals(47.0, resultB.getLatitude(), 0.0);
    assertEquals(-122.0, resultB.getLongitude(), 0.0);
    assertEquals("98000", resultB.getPostalCode());

    // Verify this happened only once, as the db should cache
    Mockito.verify(mock).geocode("test");
  }
}
