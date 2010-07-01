package org.onebusaway.users.impl;

import java.sql.SQLException;
import java.util.ArrayList;
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
  public List<Integer> getAllUsersIds(final int firstResult,
      final int maxResults) {
    return (List<Integer>) _template.execute(new HibernateCallback() {
      @Override
      public Object doInHibernate(Session session) throws HibernateException,
          SQLException {
        Query query = session.createQuery("SELECT user.id FROM User user");
        query.setFirstResult(firstResult);
        query.setMaxResults(maxResults);
        return query.list();
      }
    });
  }

  @Override
  public User getUserForId(int id) {
    return (User) _template.get(User.class, id);
  }

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
