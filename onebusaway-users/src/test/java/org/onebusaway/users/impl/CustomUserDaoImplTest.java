package org.onebusaway.users.impl;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.cfg.Configuration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.onebusaway.users.model.User;
import org.onebusaway.users.model.UserIndex;
import org.onebusaway.users.model.UserIndexKey;
import org.onebusaway.users.model.UserRole;
import org.onebusaway.users.model.properties.UserPropertiesV2;

public class CustomUserDaoImplTest {

  private SessionFactory _sessionFactory;

  private UserDaoImpl _dao;

  @Before
  public void setup() throws IOException {

    Configuration config = new AnnotationConfiguration();
    config = config.configure("org/onebusaway/users/custom-hibernate-configuration.xml");
    _sessionFactory = config.buildSessionFactory();

    _dao = new UserDaoImpl();
    _dao.setSessionFactory(_sessionFactory);
  }

  @After
  public void teardown() {
    if (_sessionFactory != null)
      _sessionFactory.close();
  }

  @Test
  public void testTransitionUserIndex() throws InterruptedException {

    UserRole userRole = new UserRole("user");
    _dao.saveOrUpdateUserRole(userRole);

    User userA = new User();
    userA.setCreationTime(new Date());
    userA.setProperties(new UserPropertiesV2());
    userA.getRoles().add(userRole);
    
    UserIndex indexA = new UserIndex();
    indexA.setId(new UserIndexKey("test", "A"));
    indexA.setUser(userA);
    userA.getUserIndices().add(indexA);

    _dao.saveOrUpdateUser(userA);
    
    User userB = new User();
    userB.setCreationTime(new Date());
    userB.setProperties(new UserPropertiesV2());
    userB.getRoles().add(userRole);
    
    UserIndex indexB = new UserIndex();
    indexB.setId(new UserIndexKey("test", "B"));
    indexB.setUser(userB);
    userB.getUserIndices().add(indexB);

    _dao.saveOrUpdateUser(userB);

    assertEquals(2, _dao.getNumberOfUsers());

    System.out.println("A=" + _dao.getUserForId(userA.getId()).getUserIndices());
    System.out.println("B=" + _dao.getUserForId(userB.getId()).getUserIndices());

    userA.getUserIndices().remove(indexA);
    userB.getUserIndices().add(indexA);
    indexA.setUser(userB);

    _dao.getHibernateTemplate().saveOrUpdateAll(Arrays.asList(userA,userB));
    
    System.out.println("A=" + _dao.getUserForId(userA.getId()).getUserIndices());
    System.out.println("B=" + _dao.getUserForId(userB.getId()).getUserIndices());
    
    _dao.getHibernateTemplate().delete(indexA);
    
    System.out.println("A=" + _dao.getUserForId(userA.getId()).getUserIndices());
    System.out.println("B=" + _dao.getUserForId(userB.getId()).getUserIndices());
    
    System.out.println("here");
  }
}
