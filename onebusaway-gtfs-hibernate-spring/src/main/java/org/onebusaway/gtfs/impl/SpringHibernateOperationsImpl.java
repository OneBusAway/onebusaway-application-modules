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
import org.springframework.transaction.annotation.Transactional;

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

  @Override
  @Deprecated
  public Object execute(final HibernateOperation callback) {
    return null;
  }

  @SuppressWarnings("unchecked")
  @Override
  @Transactional
  public <T> List<T> find(String queryString) {
    return getSession().createQuery(queryString).list();
  }

  @SuppressWarnings("unchecked")
  @Override
  @Transactional
  public <T> List<T> findByNamedQuery(String namedQuery) {
    return getSession().getNamedQuery(namedQuery).list();
  }

  @SuppressWarnings("unchecked")
  @Override
  @Transactional
  public <T> List<T> findByNamedQueryAndNamedParam(String namedQuery,
      String paramName, Object paramValue) {
    return getSession().getNamedQuery(namedQuery).setParameter(paramName, paramValue).list();
  }

  @SuppressWarnings("unchecked")
  @Override
  @Transactional
  public <T> List<T> findByNamedQueryAndNamedParams(String namedQuery,
      String[] paramNames, Object[] values) {
	Query query = getSession().getNamedQuery(namedQuery);
    for(int i=0; i < paramNames.length; i++){
    	query.setParameter(paramNames[i], values[i]);
    }
    return query.list();
  }

  @SuppressWarnings("unchecked")
  @Override
  @Transactional
  public <T> List<T> findWithNamedParam(String queryString, String paramName,
      Object value) {
    return getSession().createQuery(queryString).setParameter(paramName, value).list();
  }

  @SuppressWarnings("unchecked")
  @Override
  @Transactional
  public <T> List<T> findWithNamedParams(String queryString,
      String[] paramNames, Object[] values) {
    Query query = getSession().createQuery(queryString);
    for(int i=0; i < paramNames.length; i++){
    	query.setParameter(paramNames[i], values[i]);
    }
    return query.list();
  }

  @SuppressWarnings("unchecked")
  @Override
  @Transactional
  public <T> T get(Class<T> entityType, Serializable id) {
    return (T) getSession().get(entityType, id);
  }

  /****
   * Mutable Methods
   ****/

  @Override
  @Transactional
  public void update(Object entity) {
	  getSession().update(entity);
  }

  @Override
  @Transactional
  public void save(Object entity) {
    getSession().save(entity);
  }

  @Override
  @Transactional
  public void saveOrUpdate(Object entity) {
    getSession().saveOrUpdate(entity);
  }

  @Override
  @Transactional
  public <T> void removeEntity(T entity) {
    getSession().delete(entity);
  }

  @Override
  @Transactional
  public <T> void clearAllEntitiesForType(Class<T> type) {
	 String stringQuery = "DELETE FROM " + type.getName();
	 Query query = getSession().createQuery(stringQuery);
	 query.executeUpdate();
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
  
  private Session getSession(){
	  return _sessionFactory.getCurrentSession();
  }
}
