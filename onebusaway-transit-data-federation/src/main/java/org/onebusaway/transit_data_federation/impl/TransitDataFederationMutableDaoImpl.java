package org.onebusaway.transit_data_federation.impl;

import org.onebusaway.transit_data_federation.services.TransitDataFederationMutableDao;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
class TransitDataFederationMutableDaoImpl extends TransitDataFederationDaoImpl implements TransitDataFederationMutableDao {

  @Autowired
  public void setSessionFactory(SessionFactory sessionFactory) {
    // We override the HibernateTemplate creation, because we want a non-read-only version
    _template = new HibernateTemplate(sessionFactory);
  }

  public void save(Object object) {
    _template.save(object);
  }

  public void saveOrUpdate(Object object) {
    _template.saveOrUpdate(object);
  }

  public <T> void saveOrUpdateAllEntities(List<T> updates) {
    _template.saveOrUpdateAll(updates);
  }

  public void update(Object object) {
    _template.update(object);
  }

  public void flush() {
    _template.flush();
  }

  public <T> void deleteAllEntities(Iterable<T> entities) {
    for( T entity : entities)
      _template.delete(entity);
  }
}
