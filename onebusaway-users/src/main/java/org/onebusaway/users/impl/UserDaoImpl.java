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
package org.onebusaway.users.impl;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.onebusaway.users.model.User;
import org.onebusaway.users.model.UserIndex;
import org.onebusaway.users.model.UserIndexKey;
import org.onebusaway.users.model.UserRole;
import org.onebusaway.users.services.UserDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
class UserDaoImpl implements UserDao {

  private SessionFactory _sessionFactory;

  @Autowired
  public void setSessionFactory(SessionFactory sessionFactory) {
	  _sessionFactory = sessionFactory;
  }

  @Override
  @Transactional
  public int getNumberOfUsers() {
	  _sessionFactory.openSession();
	Query query = getSession().getNamedQuery("numberOfUsers");
    List<?> values = query.list();
    if (values == null || values.size() == 0)
      return 0;
    Number v = (Number) values.get(0);
    return v.intValue();
  }

  @SuppressWarnings("unchecked")
  @Override
  @Transactional
  public List<Integer> getAllUserIds() {
	  _sessionFactory.openSession();
    return getSession().createQuery("SELECT user.id FROM User user").list();
  }

  @Override
  @Transactional
  public List<Integer> getAllUserIdsInRange(final int firstResult,
      final int maxResults) {
	  _sessionFactory.openSession();
        Query query = getSession().createQuery("SELECT user.id FROM User user");
        query.setFirstResult(firstResult);
        query.setMaxResults(maxResults);
        return query.list();
  }
  
  @Override
  @Transactional
  public long getNumberOfStaleUsers(Date lastAccessTime) {
    List<?> values = getSession().getNamedQuery("numberOfStaleUsers").setTimestamp("lastAccessTime", lastAccessTime).list();
    if (values == null || values.size() == 0)
      return 0;
    Number v = (Number) values.get(0);
    return v.intValue();

  }
  
  @Override
  @Transactional
  public List<Integer> getStaleUserIdsInRange(final Date lastAccessTime, final int firstResult, final int maxResults) {
        Query query = getSession().createQuery("SELECT user.id FROM User user WHERE lastAccessTime < :lastAccessTime");
        query.setFirstResult(firstResult);
        query.setMaxResults(maxResults);
        query.setTimestamp("lastAccessTime", lastAccessTime);
        return query.list();
       
  }

  @Override
  @Transactional
  public User getUserForId(int id) {
    return (User) getSession().get(User.class, id);
  }
  
  @Override
  @Transactional
  public void saveOrUpdateUser(User user) {
	getSession().saveOrUpdate(user);
  }

  @Override
  @Transactional
  public void saveOrUpdateUsers(User... users) {
	Session session = getSession();
    List<User> list = new ArrayList<User>(users.length);
    
    for (Iterator<User> it = list.iterator(); it.hasNext();) {
    	session.saveOrUpdate(it.next());
	}
  }

  @Override
  @Transactional
  public void deleteUser(User user) {
    getSession().delete(user);
  }

  @Override
  @Transactional
  public int getNumberOfUserRoles() {
    List<?> values = getSession().getNamedQuery("numberOfUserRoles").list();
    if (values == null || values.size() == 0)
      return 0;
    Number v = (Number) values.get(0);
    return v.intValue();
  }

  @Override
  @Transactional
  public int getNumberOfUsersWithRole(UserRole role) {
	Query query = getSession().getNamedQuery("numberOfUsersWithRole");
	query.setParameter("role", role);
    List<?> values = query.list();
    if (values == null || values.size() == 0)
      return 0;
    Number v = (Number) values.get(0);
    return v.intValue();
  }

  @Override
  @Transactional
  public UserRole getUserRoleForName(String name) {
    return (UserRole) getSession().get(UserRole.class, name);
  }

  @Override
  public void saveOrUpdateUserRole(UserRole userRole) {
	  getSession().saveOrUpdate(userRole);
  }

  @SuppressWarnings("unchecked")
  @Override
  @Transactional
  public List<String> getUserIndexKeyValuesForKeyType(String keyType) {
	  
	  return getSession()
			  .getNamedQuery("userIndexKeyValuesForKeyType")
			  .setString("type", keyType)
			  .list();
  }

  @Transactional
  @Override
  public UserIndex getUserIndexForId(UserIndexKey key) {
    return (UserIndex) getSession().get(UserIndex.class, key);
  }

  @Override
  @Transactional
  public void saveOrUpdateUserIndex(UserIndex userIndex) {
	  getSession().saveOrUpdate(userIndex);
  }

  @Override
  @Transactional
  public void deleteUserIndex(UserIndex index) {
    getSession().delete(index);
  }
  
  private Session getSession(){
	  return _sessionFactory.getCurrentSession();
  }

}
