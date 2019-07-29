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

import org.hibernate.Session;
import org.onebusaway.geocoder.model.GeocoderResultsEntity;
import org.onebusaway.geocoder.model.GeocoderResults;
import org.onebusaway.geocoder.services.GeocoderService;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

public class DatabaseCachingGeocoderImpl implements GeocoderService {

  private GeocoderService _geocoderService;

  @Autowired
  private SessionFactory _sessionFactory;

  public void setGeocoderService(GeocoderService geocoderService) {
    _geocoderService = geocoderService;
  }

  @Autowired
  public void setSessionFactory(SessionFactory sessionFactory) {
      _sessionFactory = sessionFactory;
  }

  @Transactional
  public GeocoderResults geocode(String location) {
    Session session = _sessionFactory.getCurrentSession();
    GeocoderResultsEntity entity = (GeocoderResultsEntity) session.get(
            GeocoderResultsEntity.class, location);

    if (entity != null)
      return entity.getResults();

    GeocoderResults results = _geocoderService.geocode(location);

    entity = new GeocoderResultsEntity();
    entity.setLocation(location);
    entity.setResults(results);
    session.saveOrUpdate(entity);

    return results;
  }
}
