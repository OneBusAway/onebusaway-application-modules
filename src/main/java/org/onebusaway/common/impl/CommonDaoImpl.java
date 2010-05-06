package org.onebusaway.common.impl;

import org.hibernate.SessionFactory;
import org.onebusaway.common.model.Place;
import org.onebusaway.common.services.CommonDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.HibernateTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class CommonDaoImpl implements CommonDao {

  private HibernateTemplate _template;

  @Autowired
  public void setSessionFactory(SessionFactory sessionFactory) {
    _template = new HibernateTemplate(sessionFactory);
  }

  @SuppressWarnings("unchecked")
  public List<Place> getPlacesByIds(Set<String> ids) {
    if( ids.isEmpty() )
      return new ArrayList<Place>();
    return _template.findByNamedParam(
        "FROM Place place WHERE place.id IN (:ids)", "ids", ids);
  }
}
