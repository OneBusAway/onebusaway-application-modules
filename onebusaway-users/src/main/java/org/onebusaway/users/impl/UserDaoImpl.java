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
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
class UserDaoImpl implements UserDao {

  private HibernateTemplate _template;

  @Autowired
  public void setSessionFactory(SessionFactory sessionFactory) {
    _template = new HibernateTemplate(sessionFactory);
  }

  public HibernateTemplate getHibernateTemplate() {
    return _template;
  }

  @Override
  public int getNumberOfUsers() {
    List<?> values = _template.findByNamedQuery("numberOfUsers");
    if (values == null || values.size() == 0)
      return 0;
    Number v = (Number) values.get(0);
    return v.intValue();
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<Integer> getAllUserIds() {
    return _template.find("SELECT user.id FROM User user");
  }

  @Override
  public List<Integer> getAllUserIdsInRange(final int firstResult,
      final int maxResults) {
    return _template.execute(new HibernateCallback<List<Integer>>() {
      @SuppressWarnings("unchecked")
      @Override
      public List<Integer> doInHibernate(Session session)
          throws HibernateException, SQLException {
        Query query = session.createQuery("SELECT user.id FROM User user");
        query.setFirstResult(firstResult);
        query.setMaxResults(maxResults);
        return query.list();
      }
    });
  }
  
  @Override
  public long getNumberOfStaleUsers(Date lastAccessTime) {
    List<?> values = _template.findByNamedQueryAndNamedParam("numberOfStaleUsers", "lastAccessTime", lastAccessTime);
    if (values == null || values.size() == 0)
      return 0;
    Number v = (Number) values.get(0);
    return v.intValue();

  }
  
  @Override
  public List<Integer> getStaleUserIdsInRange(final Date lastAccessTime, final int firstResult, final int maxResults) {
    return _template.execute(new HibernateCallback<List<Integer>>() {
      @SuppressWarnings("unchecked")
      @Override
      public List<Integer> doInHibernate(Session session)
          throws HibernateException, SQLException {
        Query query = session.createQuery("SELECT user.id FROM User user WHERE lastAccessTime < :lastAccessTime");
        query.setFirstResult(firstResult);
        query.setMaxResults(maxResults);
        query.setTimestamp("lastAccessTime", lastAccessTime);
        return query.list();
      }
    });  
  }

  @Override
  public User getUserForId(int id) {
    return (User) _template.get(User.class, id);
  }

  @Transactional
  @Override
  public void saveOrUpdateUser(User user) {
    _template.saveOrUpdate(user);
  }

  @Override
  public void saveOrUpdateUsers(User... users) {
    List<User> list = new ArrayList<User>(users.length);
    for (User user : users)
      list.add(user);
    _template.saveOrUpdateAll(list);
  }

  @Override
  public void deleteUser(User user) {
    _template.delete(user);
  }

  @Override
  public int getNumberOfUserRoles() {
    List<?> values = _template.findByNamedQuery("numberOfUserRoles");
    if (values == null || values.size() == 0)
      return 0;
    Number v = (Number) values.get(0);
    return v.intValue();
  }

  @Override
  public int getNumberOfUsersWithRole(UserRole role) {
    List<?> values = _template.findByNamedQueryAndNamedParam(
        "numberOfUsersWithRole", "role", role);
    if (values == null || values.size() == 0)
      return 0;
    Number v = (Number) values.get(0);
    return v.intValue();
  }

  @Override
  public UserRole getUserRoleForName(String name) {
    return (UserRole) _template.get(UserRole.class, name);
  }

  @Override
  public void saveOrUpdateUserRole(UserRole userRole) {
    _template.saveOrUpdate(userRole);
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<String> getUserIndexKeyValuesForKeyType(String keyType) {
    return _template.findByNamedQueryAndNamedParam(
        "userIndexKeyValuesForKeyType", "type", keyType);
  }

  @Transactional
  @Override
  public UserIndex getUserIndexForId(UserIndexKey key) {
    return (UserIndex) _template.get(UserIndex.class, key);
  }

  @Override
  public void saveOrUpdateUserIndex(UserIndex userIndex) {
    _template.saveOrUpdate(userIndex);
  }

  @Override
  public void deleteUserIndex(UserIndex index) {
    _template.delete(index);
  }

}
