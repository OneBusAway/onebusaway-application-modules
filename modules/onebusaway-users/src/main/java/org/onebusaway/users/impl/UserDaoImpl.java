package org.onebusaway.users.impl;

import org.onebusaway.users.model.User;
import org.onebusaway.users.model.UserIndex;
import org.onebusaway.users.model.UserIndexKey;
import org.onebusaway.users.model.UserRole;
import org.onebusaway.users.services.UserDao;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
class UserDaoImpl implements UserDao {

  private HibernateTemplate _template;

  @Autowired
  public void setSessionFactory(SessionFactory sessionFactory) {
    _template = new HibernateTemplate(sessionFactory);
  }

  public void saveOrUpdateUser(User user) {
    _template.saveOrUpdate(user);
  }

  public int getNumberOfUserRoles() {
    List<?> values = _template.findByNamedQuery("numberOfUserRoles");
    if (values == null || values.size() == 0)
      return 0;
    Number v = (Number) values.get(0);
    return v.intValue();
  }

  public UserRole getUserRoleForName(String name) {
    return (UserRole) _template.get(UserRole.class, name);
  }

  public void saveOrUpdateUserRole(UserRole userRole) {
    _template.saveOrUpdate(userRole);
  }

  public UserIndex getUserIndexForId(UserIndexKey key) {
    return (UserIndex) _template.get(UserIndex.class, key);
  }

  public void saveOrUpdateUserIndex(UserIndex userIndex) {
    _template.save(userIndex);
  }

}
