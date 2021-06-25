/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onebusaway.geocoder.impl;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.hibernate.SessionFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.onebusaway.geocoder.model.GeocoderResult;
import org.onebusaway.geocoder.model.GeocoderResults;
import org.onebusaway.geocoder.services.GeocoderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@ContextConfiguration(locations = "classpath:org/onebusaway/geocoder/impl/application-context-test.xml")
@RunWith(SpringJUnit4ClassRunner.class)
public class DatabaseCachingGeocoderImplTest {

  @Autowired
  private SessionFactory _sessionFactory;

  @Test
  @Transactional
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
