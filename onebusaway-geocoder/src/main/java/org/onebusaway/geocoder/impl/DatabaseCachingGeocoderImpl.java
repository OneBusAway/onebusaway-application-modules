package org.onebusaway.geocoder.impl;

import org.onebusaway.geocoder.model.GeocoderResultsEntity;
import org.onebusaway.geocoder.model.GeocoderResults;
import org.onebusaway.geocoder.services.GeocoderService;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.HibernateTemplate;

public class DatabaseCachingGeocoderImpl implements GeocoderService {

  private GeocoderService _geocoderService;

  private HibernateTemplate _template;

  public void setGeocoderService(GeocoderService geocoderService) {
    _geocoderService = geocoderService;
  }

  @Autowired
  public void setSessionFactory(SessionFactory sessionFactory) {
    _template = new HibernateTemplate(sessionFactory);
  }

  public GeocoderResults geocode(String location) {

    GeocoderResultsEntity entity = (GeocoderResultsEntity) _template.get(
        GeocoderResultsEntity.class, location);

    if (entity != null)
      return entity.getResults();

    GeocoderResults results = _geocoderService.geocode(location);

    entity = new GeocoderResultsEntity();
    entity.setLocation(location);
    entity.setResults(results);
    _template.saveOrUpdate(entity);

    return results;
  }
}
