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
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.onebusaway.gtfs.services.HibernateOperation;
import org.onebusaway.gtfs.services.HibernateOperations;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;

public class SpringHibernateOperationsImpl implements HibernateOperations {

  private HibernateTemplate _ops;

  public SpringHibernateOperationsImpl() {

  }

  public SpringHibernateOperationsImpl(SessionFactory sessionFactory) {
    setSessionFactory(sessionFactory);
  }

  public void setSessionFactory(SessionFactory sessionFactory) {
    _ops = new HibernateTemplate(sessionFactory);
  }

  @Override
  public SessionFactory getSessionFactory() {
    return _ops.getSessionFactory();
  }

  @Override
  public Object execute(final HibernateOperation callback) {
    return _ops.execute(new HibernateCallback<Object>() {
      @Override
      public Object doInHibernate(Session session) throws HibernateException,
          SQLException {
        return callback.doInHibernate(session);
      }
    });
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> List<T> find(String queryString) {
    return _ops.find(queryString);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> List<T> findByNamedQuery(String namedQuery) {
    return _ops.findByNamedQuery(namedQuery);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> List<T> findByNamedQueryAndNamedParam(String namedQuery,
      String paramName, Object paramValue) {
    return _ops.findByNamedQueryAndNamedParam(namedQuery, paramName, paramValue);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> List<T> findByNamedQueryAndNamedParams(String namedQuery,
      String[] paramNames, Object[] values) {
    return _ops.findByNamedQueryAndNamedParam(namedQuery, paramNames, values);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> List<T> findWithNamedParam(String queryString, String paramName,
      Object value) {
    return _ops.findByNamedParam(queryString, paramName, value);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> List<T> findWithNamedParams(String queryString,
      String[] paramNames, Object[] values) {
    return _ops.findByNamedParam(queryString, paramNames, values);
  }

  @Override
  public <T> T get(Class<T> entityType, Serializable id) {
    return (T) _ops.get(entityType, id);
  }

  /****
   * Mutable Methods
   ****/

  @Override
  public void update(Object entity) {
    _ops.update(entity);
  }

  @Override
  public void save(Object entity) {
    _ops.save(entity);
  }

  @Override
  public void saveOrUpdate(Object entity) {
    _ops.saveOrUpdate(entity);
  }

  @Override
  public <T> void removeEntity(T entity) {
    _ops.delete(entity);
  }

  @Override
  public <T> void clearAllEntitiesForType(Class<T> type) {
    _ops.bulkUpdate("DELETE FROM " + type.getName());
  }

  @Override
  public void open() {

  }

  @Override
  public void close() {

  }

  @Override
  public void flush() {
    _ops.flush();
  }
}
