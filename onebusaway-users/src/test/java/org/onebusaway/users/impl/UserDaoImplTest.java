/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
 * Copyright (C) 2012 Google, Inc.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.onebusaway.users.model.User;
import org.onebusaway.users.model.UserIndex;
import org.onebusaway.users.model.UserIndexKey;
import org.onebusaway.users.model.UserPropertiesV1;
import org.onebusaway.users.model.UserRole;
import org.onebusaway.users.model.properties.UserPropertiesV2;
import org.onebusaway.users.services.UserDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@ContextConfiguration(locations = "classpath:org/onebusaway/users/application-context-test.xml")
@RunWith(SpringJUnit4ClassRunner.class)
public class UserDaoImplTest {

  @Autowired
  private SessionFactory _sessionFactory;
  @Autowired
  private UserDao _dao;

  @Before
  public void setup() throws IOException {
  }

  @Test
  @Transactional
  public void test() {

    assertEquals(0, _dao.getNumberOfUserRoles());

    UserRole adminRole = new UserRole("admin");
    UserRole userRole = new UserRole("user");

    _dao.saveOrUpdateUserRole(adminRole);
    _dao.saveOrUpdateUserRole(userRole);

    assertEquals(2, _dao.getNumberOfUserRoles());

    assertEquals(0, _dao.getNumberOfUsersWithRole(adminRole));
    assertEquals(0, _dao.getNumberOfUsersWithRole(userRole));

    User userA = new User();
    userA.setCreationTime(new Date());
    userA.setProperties(new UserPropertiesV1());
    userA.getRoles().add(userRole);

    _dao.saveOrUpdateUser(userA);

    assertEquals(0, _dao.getNumberOfUsersWithRole(adminRole));
    assertEquals(1, _dao.getNumberOfUsersWithRole(userRole));

    User userB = new User();
    userB.setCreationTime(new Date());
    userB.setProperties(new UserPropertiesV1());
    userB.getRoles().add(adminRole);

    _dao.saveOrUpdateUser(userB);

    assertEquals(1, _dao.getNumberOfUsersWithRole(adminRole));
    assertEquals(1, _dao.getNumberOfUsersWithRole(userRole));

    userA.getRoles().add(adminRole);

    _dao.saveOrUpdateUser(userA);

    assertEquals(2, _dao.getNumberOfUsersWithRole(adminRole));
    assertEquals(1, _dao.getNumberOfUsersWithRole(userRole));
  }

  @Test
  @Transactional
  public void testGetNumberOfUsers() {

    assertEquals(0, _dao.getNumberOfUsers());

    User userA = new User();
    userA.setCreationTime(new Date());
    userA.setProperties(new UserPropertiesV1());

    _dao.saveOrUpdateUser(userA);

    assertEquals(1, _dao.getNumberOfUsers());

    User userB = new User();
    userB.setCreationTime(new Date());
    userB.setProperties(new UserPropertiesV1());

    _dao.saveOrUpdateUser(userB);

    assertEquals(2, _dao.getNumberOfUsers());
  }
  
  @Test
  @Transactional
  public void testGetNumberOfStaleUsers() {
    
    Calendar c = Calendar.getInstance();
    c.add(Calendar.MONTH, -1);
    Date oneMonth = c.getTime();
    c.add(Calendar.MONTH, -1);
    Date twoMonth = c.getTime();
    c.add(Calendar.MONTH, -1);
    Date threeMonth = c.getTime();

    assertEquals(0, _dao.getNumberOfStaleUsers(oneMonth));

    User userA = new User();
    userA.setCreationTime(new Date());
    userA.setLastAccessTime(twoMonth);
    userA.setProperties(new UserPropertiesV2());

    _dao.saveOrUpdateUser(userA);

    assertEquals(1, _dao.getNumberOfStaleUsers(oneMonth));
    assertEquals(0, _dao.getNumberOfStaleUsers(twoMonth));
    assertEquals(0, _dao.getNumberOfStaleUsers(threeMonth));

    User userB = new User();
    userB.setCreationTime(new Date());
    userB.setLastAccessTime(threeMonth);
    userB.setProperties(new UserPropertiesV2());

    _dao.saveOrUpdateUser(userB);

    assertEquals(2, _dao.getNumberOfStaleUsers(oneMonth));
    assertEquals(1, _dao.getNumberOfStaleUsers(twoMonth));
    assertEquals(0, _dao.getNumberOfStaleUsers(threeMonth));
  }

  @Test
  @Transactional
  public void testGetAllUserIds() {

    Set<Integer> ids = new HashSet<Integer>();

    for (int i = 0; i < 100; i++) {

      User userA = new User();
      userA.setCreationTime(new Date());
      userA.setProperties(new UserPropertiesV1());

      _dao.saveOrUpdateUser(userA);

      ids.add(userA.getId());
    }

    int n = _dao.getNumberOfUsers();
    assertEquals(100, n);

    Set<Integer> retreivedIds = new HashSet<Integer>();
    final int limit = 20;

    for (int i = 0; i < n; i += limit) {
      List<Integer> pageOfIds = _dao.getAllUserIdsInRange(i, limit);
      assertTrue(pageOfIds.size() <= limit);
      retreivedIds.addAll(pageOfIds);
    }

    assertEquals(ids, retreivedIds);
  }

  @Test
  @Transactional
  public void deleteUser() {

    UserRole userRole = new UserRole("user");

    _dao.saveOrUpdateUserRole(userRole);

    User user = new User();
    user.setCreationTime(new Date());
    user.setProperties(new UserPropertiesV2());
    user.getRoles().add(userRole);

    UserIndexKey key = new UserIndexKey("phone", "2065551234");

    UserIndex index = new UserIndex();
    index.setId(key);
    index.setUser(user);
    user.getUserIndices().add(index);

    _dao.saveOrUpdateUser(user);

    assertEquals(1, _dao.getNumberOfUsers());

    UserIndex index2 = _dao.getUserIndexForId(key);
    assertEquals(key, index2.getId());
    assertEquals(user, index2.getUser());

    _dao.deleteUser(user);

    assertEquals(0, _dao.getNumberOfUsers());
    index2 = _dao.getUserIndexForId(key);
    assertNull(index2);
  }

  @Test
  @Transactional
  public void testTransitionUserIndex() {

    User userA = new User();
    userA.setCreationTime(new Date());
    userA.setProperties(new UserPropertiesV2());

    UserIndex index = new UserIndex();
    index.setId(new UserIndexKey("test", "A"));
    index.setUser(userA);
    userA.getUserIndices().add(index);

    _dao.saveOrUpdateUser(userA);

    User userB = new User();
    userB.setCreationTime(new Date());
    userB.setProperties(new UserPropertiesV2());

    _dao.saveOrUpdateUser(userB);

    assertEquals(1, _dao.getUserForId(userA.getId()).getUserIndices().size());
    assertEquals(0, _dao.getUserForId(userB.getId()).getUserIndices().size());

    index.setUser(userB);
    userA.getUserIndices().remove(index);
    userB.getUserIndices().add(index);

    _dao.saveOrUpdateUsers(userA, userB);

    assertEquals(0, _dao.getUserForId(userA.getId()).getUserIndices().size());
    assertEquals(1, _dao.getUserForId(userB.getId()).getUserIndices().size());
  }
}
