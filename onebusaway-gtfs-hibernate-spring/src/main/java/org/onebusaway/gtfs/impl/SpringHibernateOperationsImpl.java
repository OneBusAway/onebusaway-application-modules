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
package org.onebusaway.gtfs.impl;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.onebusaway.gtfs.services.HibernateOperation;
import org.onebusaway.gtfs.services.HibernateOperations;

public class SpringHibernateOperationsImpl implements HibernateOperations {

  private SessionFactory _sessionFactory;

  public SpringHibernateOperationsImpl() {

  }

  public SpringHibernateOperationsImpl(SessionFactory sessionFactory) {
    setSessionFactory(sessionFactory);
  }

  public void setSessionFactory(SessionFactory sessionFactory) {
    _sessionFactory = sessionFactory;
  }

  @Override
  public SessionFactory getSessionFactory() {
    return _sessionFactory;
  }

  private Session getSession(){
    return _sessionFactory.getCurrentSession();
  }

  @Override
  public Object execute(final HibernateOperation callback) {
    throw new UnsupportedOperationException("removed in v 2.0.3");
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> List<T> find(String queryString) {
    Query query = getSession().createQuery(queryString);
    return query.list();
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> List<T> findByNamedQuery(String namedQuery) {
    Query query = getSession().getNamedQuery(namedQuery);
    return query.list();
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> List<T> findByNamedQueryAndNamedParam(String namedQuery,
      String paramName, Object paramValue) {
    Query query = getSession().getNamedQuery(namedQuery);
    query.setParameter(paramName, paramValue);
    return query.list();
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> List<T> findByNamedQueryAndNamedParams(String namedQuery,
      String[] paramNames, Object[] values) {
    Query query = getSession().getNamedQuery(namedQuery);
    for (int i = 0; i < paramNames.length; i++) {
      query.setParameter(paramNames[i], values[i]);
    }
    return query.list();
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> List<T> findWithNamedParam(String queryString, String paramName,
      Object value) {
    Query query = getSession().createQuery(queryString);
    query.setParameter(paramName, value);
    return query.list();
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> List<T> findWithNamedParams(String queryString,
      String[] paramNames, Object[] values) {
    Query query = getSession().createQuery(queryString);
    for (int i = 0; i < paramNames.length; i++) {
      query.setParameter(paramNames[i], values[i]);
    }
    return query.list();
  }

  @Override
  public <T> T get(Class<T> entityType, Serializable id) {
    return (T) getSession().get(entityType, id);
  }

  /****
   * Mutable Methods
   ****/

  @Override
  public void update(Object entity) {
    getSession().update(entity);
  }

  @Override
  public void save(Object entity) {
    getSession().save(entity);
  }

  @Override
  public void saveOrUpdate(Object entity) {
    getSession().saveOrUpdate(entity);
  }

  @Override
  public <T> void removeEntity(T entity) {
    getSession().delete(entity);
  }

  @Override
  public <T> void clearAllEntitiesForType(Class<T> type) {
    Query query = getSession().createQuery("DELETE FROM " + type.getName());
    List<T> list = query.list();
    for (T t: list) {
      removeEntity(t);
    }

  }

  @Override
  public void open() {

  }

  @Override
  public void close() {

  }

  @Override
  public void flush() {
    getSession().flush();
  }
}
