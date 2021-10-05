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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.onebusaway.container.cache.Cacheable;
import org.onebusaway.container.cache.CacheableArgument;
import org.onebusaway.users.client.model.UserBean;
import org.onebusaway.users.client.model.UserIndexBean;
import org.onebusaway.users.impl.authentication.VersionedPasswordEncoder;
import org.onebusaway.users.model.User;
import org.onebusaway.users.model.UserIndex;
import org.onebusaway.users.model.UserIndexKey;
import org.onebusaway.users.model.UserRole;
import org.onebusaway.users.model.properties.UserPropertiesV4;
import org.onebusaway.users.services.StandardAuthoritiesService;
import org.onebusaway.users.services.UserDao;
import org.onebusaway.users.services.UserIndexTypes;
import org.onebusaway.users.services.UserPropertiesMigration;
import org.onebusaway.users.services.UserPropertiesMigrationStatus;
import org.onebusaway.users.services.UserPropertiesService;
import org.onebusaway.users.services.UserService;
import org.onebusaway.users.services.internal.UserIndexRegistrationService;
import org.onebusaway.users.services.internal.UserRegistration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class UserServiceImpl implements UserService {

  private UserDao _userDao;

  private StandardAuthoritiesService _authoritiesService;

  private UserPropertiesMigration _userPropertiesMigration;

  private UserPropertiesService _userPropertiesService;

  private UserIndexRegistrationService _userIndexRegistrationService;

  private VersionedPasswordEncoder _passwordEncoder;

  private ExecutorService _executors;

  private Object _deleteStaleUsersLock = new Object();

  private Future<?> _deleteStaleUsersTask;

  @Autowired
  public void setUserDao(UserDao dao) {
    _userDao = dao;
  }

  @Autowired
  public void setAuthoritiesService(
      StandardAuthoritiesService authoritiesService) {
    _authoritiesService = authoritiesService;
  }

  @Autowired
  public void setUserPropertiesService(
      UserPropertiesService userPropertiesService) {
    _userPropertiesService = userPropertiesService;
  }

  @Autowired
  public void setUserIndexRegistrationService(
      UserIndexRegistrationService userIndexRegistrationService) {
    _userIndexRegistrationService = userIndexRegistrationService;
  }

  @Autowired
  @Qualifier(value = "passwordEncoderV1")
  public void setPasswordEncoder(VersionedPasswordEncoder passwordEncoder) {
    _passwordEncoder = passwordEncoder;
  }

  @PostConstruct
  public void start() {
    _executors = Executors.newSingleThreadExecutor();
  }

  @PreDestroy
  public void stop() {
    _executors.shutdownNow();
  }

  /****
   * {@link UserService} Interface
   ****/

  @Override
  @Transactional(readOnly=true)
  public int getNumberOfUsers() {
    return _userDao.getNumberOfUsers();
  }

  @Override
  @Transactional
  public List<Integer> getAllUserIds() {
    return _userDao.getAllUserIds();
  }

  @Override
  @Transactional(readOnly=true)
  public List<Integer> getAllUserIdsInRange(int offset, int limit) {
    return _userDao.getAllUserIdsInRange(offset, limit);
  }

  @Override
  @Transactional(readOnly=true)
  public int getNumberOfAdmins() {
    UserRole admin = _authoritiesService.getAdministratorRole();
    return _userDao.getNumberOfUsersWithRole(admin);
  }

  @Override
  @Transactional(readOnly=true)
  public User getUserForId(int userId) {
    return _userDao.getUserForId(userId);
  }

  @Override
  public UserBean getUserAsBean(User user) {

    UserBean bean = new UserBean();
    bean.setUserId(Integer.toString(user.getId()));

    UserRole anonymous = _authoritiesService.getAnonymousRole();
    boolean isAnonymous = user.getRoles().contains(anonymous);
    bean.setAnonymous(isAnonymous);

    UserRole admin = _authoritiesService.getAdministratorRole();
    boolean isAdmin = user.getRoles().contains(admin);
    bean.setAdmin(isAdmin);

    List<UserIndexBean> indices = new ArrayList<UserIndexBean>();
    bean.setIndices(indices);

    for (UserIndex index : user.getUserIndices()) {
      UserIndexKey key = index.getId();

      UserIndexBean indexBean = new UserIndexBean();
      indexBean.setType(key.getType());
      indexBean.setValue(key.getValue());
      indices.add(indexBean);
    }

    _userPropertiesService.getUserAsBean(user, bean);

    return bean;
  }

  @Override
  public UserBean getAnonymousUser() {

    UserBean bean = new UserBean();
    bean.setUserId("-1");

    bean.setAnonymous(true);
    bean.setAdmin(false);

    _userPropertiesService.getAnonymousUserAsBean(bean);

    return bean;
  }

  @Override
  public void resetUser(User user) {
    _userPropertiesService.resetUser(user);
  }

  @Override
  @Transactional
  public void deleteUser(User user) {
    _userDao.deleteUser(user);
  }

  @Override
  public boolean isAnonymous(User user) {
    return user.getRoles().contains(_authoritiesService.getAnonymousRole());
  }

  @Override
  public boolean isAdministrator(User user) {
    return user.getRoles().contains(_authoritiesService.getAdministratorRole());
  }

  @Override
  @Transactional
  public void enableAdminRoleForUser(User user, boolean onlyIfNoOtherAdmins) {

    UserRole adminRole = _authoritiesService.getUserRoleForName(StandardAuthoritiesService.ADMINISTRATOR);

    if (onlyIfNoOtherAdmins) {
      int count = _userDao.getNumberOfUsersWithRole(adminRole);
      if (count > 0)
        return;
    }

    Set<UserRole> roles = user.getRoles();

    if (roles.add(adminRole))
      _userDao.saveOrUpdateUser(user);
  }

  @Transactional
  public void disableAdminRoleForUser(User user, boolean onlyIfOtherAdmins) {

    UserRole adminRole = _authoritiesService.getUserRoleForName(StandardAuthoritiesService.ADMINISTRATOR);

    if (onlyIfOtherAdmins) {
      int count = _userDao.getNumberOfUsersWithRole(adminRole);
      if (count < 2)
        return;
    }

    Set<UserRole> roles = user.getRoles();

    if (roles.remove(adminRole))
      _userDao.saveOrUpdateUser(user);
  }

  @Override
  @Transactional(readOnly=true)
  public List<String> getUserIndexKeyValuesForKeyType(String keyType) {
    return _userDao.getUserIndexKeyValuesForKeyType(keyType);
  }

  @Override
  @Transactional(readOnly=true)
  public Integer getApiKeyCount(){
    return _userDao.getUserKeyCount(UserIndexTypes.API_KEY);
  }

  @Override
  @Transactional(readOnly=true)
  public List<User> getApiKeys(final int start, final int maxResults){
    return _userDao.getUsersForKeyType(start, maxResults, UserIndexTypes.API_KEY);
  }

  @Override
  @Transactional
  public UserIndex getOrCreateUserForIndexKey(UserIndexKey key,
      String credentials, boolean isAnonymous) {

    UserIndex userIndex = _userDao.getUserIndexForId(key);

    if (userIndex == null) {

      User user = new User();
      user.setCreationTime(new Date());
      user.setTemporary(true);
      user.setProperties(new UserPropertiesV4());
      Set<UserRole> roles = new HashSet<UserRole>();
      if (isAnonymous)
        roles.add(_authoritiesService.getAnonymousRole());
      else
        roles.add(_authoritiesService.getUserRole());
      user.setRoles(roles);

      userIndex = new UserIndex();
      userIndex.setId(key);
      userIndex.setCredentials(credentials);
      userIndex.setUser(user);

      user.getUserIndices().add(userIndex);

      _userDao.saveOrUpdateUser(user);
    }

    return userIndex;
  }

  @Override
  @Transactional
  public UserIndex getOrCreateUserForUsernameAndPassword(String username,
      String password) {

    String credentials = _passwordEncoder.encodePassword(password, username);
    UserIndexKey key = new UserIndexKey(UserIndexTypes.USERNAME, username);
    return getOrCreateUserForIndexKey(key, credentials, false);
  }

  @Override
  @Transactional(readOnly=true)
  public UserIndex getUserIndexForId(UserIndexKey key) {
    return _userDao.getUserIndexForId(key);
  }

  @Override
  public UserIndex getUserIndexForUsername(String username)
          throws UsernameNotFoundException {

      int index = username.indexOf('_');
      if (index == -1)
          throw new UsernameNotFoundException(
                  "username did not take the form type_value: " + username);

      String type = username.substring(0, index);
      String value = username.substring(index + 1);

      UserIndexKey key = new UserIndexKey(type, value);
      UserIndex userIndex = getUserIndexForId(key);

      if (userIndex == null)
          throw new UsernameNotFoundException(key.toString());

      return userIndex;
  }

  @Override
  @Transactional
  public UserIndex addUserIndexToUser(User user, UserIndexKey key,
      String credentials) {
    UserIndex index = new UserIndex();
    index.setId(key);
    index.setCredentials(credentials);
    index.setUser(user);
    user.getUserIndices().add(index);
    _userDao.saveOrUpdateUser(user);
    return index;
  }

  @Override
  @Transactional
  public void removeUserIndexForUser(User user, UserIndexKey key) {
    for (UserIndex index : user.getUserIndices()) {
      if (index.getId().equals(key)) {
        _userDao.deleteUserIndex(index);
        index.setUser(null);
        user.getUserIndices().remove(index);
        _userDao.saveOrUpdateUser(user);
        return;
      }
    }
  }

  @Override
  @Transactional
  public void setCredentialsForUserIndex(UserIndex userIndex, String credentials) {
    userIndex.setCredentials(credentials);
    _userDao.saveOrUpdateUserIndex(userIndex);
  }

  @Override
  @Transactional
  public void setPasswordForUsernameUserIndex(UserIndex userIndex,
      String password) {

    UserIndexKey id = userIndex.getId();
    if (!UserIndexTypes.USERNAME.equals(id.getType()))
      throw new IllegalArgumentException("expected UserIndex of type "
          + UserIndexTypes.USERNAME);

    String credentials = _passwordEncoder.encodePassword(password,
        id.getValue());
    setCredentialsForUserIndex(userIndex, credentials);
  }

  @Override
  @Transactional
  public void mergeUsers(User sourceUser, User targetUser) {

    if (sourceUser.equals(targetUser))
      return;

    if (sourceUser.getCreationTime().before(targetUser.getCreationTime()))
      targetUser.setCreationTime(sourceUser.getCreationTime());

    _userPropertiesService.mergeProperties(sourceUser, targetUser);

    mergeRoles(sourceUser, targetUser);

    List<UserIndex> indices = new ArrayList<UserIndex>(
        sourceUser.getUserIndices());

    for (UserIndex index : indices) {
      index.setUser(targetUser);
      targetUser.getUserIndices().add(index);
    }

    sourceUser.getUserIndices().clear();

    _userDao.saveOrUpdateUsers(sourceUser, targetUser);

    deleteUser(sourceUser);
  }

  @Override
  public String registerPhoneNumber(User user, String phoneNumber) {
    int code = (int) (Math.random() * 8999 + 1000);
    String codeAsString = Integer.toString(code);
    phoneNumber = PhoneNumberLibrary.normalizePhoneNumber(phoneNumber);
    UserIndexKey key = new UserIndexKey(UserIndexTypes.PHONE_NUMBER,
        phoneNumber);
    _userIndexRegistrationService.setRegistrationForUserIndexKey(key,
        user.getId(), codeAsString);
    return codeAsString;
  }

  @Override
  public boolean hasPhoneNumberRegistration(UserIndexKey userIndexKey) {
    return _userIndexRegistrationService.hasRegistrationForUserIndexKey(userIndexKey);
  }

  @Override
  @Transactional
  public UserIndex completePhoneNumberRegistration(UserIndex userIndex,
      String registrationCode) {

    UserRegistration registration = _userIndexRegistrationService.getRegistrationForUserIndexKey(userIndex.getId());
    if (registration == null)
      return null;

    String expectedCode = registration.getRegistrationCode();
    if (!expectedCode.equals(registrationCode))
      return null;

    /**
     * At this point, we have a valid registration code. We may safely clear the
     * registration
     */
    _userIndexRegistrationService.clearRegistrationForUserIndexKey(userIndex.getId());

    User targetUser = _userDao.getUserForId(registration.getUserId());
    if (targetUser == null)
      return null;

    User phoneUser = userIndex.getUser();

    /**
     * If the user index is already registered, our work is done
     */
    if (phoneUser.equals(targetUser))
      return userIndex;

    /**
     * If the phone user only has one index (the phoneNumber index), we merge
     * the phone user with the registration target user. Otherwise, we keep the
     * phone user, but just transfer the phone index
     */
    if (phoneUser.getUserIndices().size() == 1) {
      mergeUsers(userIndex.getUser(), targetUser);
    } else {
      userIndex.setUser(targetUser);
      targetUser.getUserIndices().add(userIndex);
      phoneUser.getUserIndices().remove(userIndex);
      _userDao.saveOrUpdateUsers(phoneUser, targetUser);
    }

    // Refresh the user index
    return getUserIndexForId(userIndex.getId());
  }

  @Override
  public void clearPhoneNumberRegistration(UserIndexKey userIndexKey) {
    _userIndexRegistrationService.clearRegistrationForUserIndexKey(userIndexKey);
  }

  @Override
  public void startUserPropertiesMigration() {
    _userPropertiesMigration.startUserPropertiesBulkMigration(_userPropertiesService.getUserPropertiesType());
  }

  @Override
  public UserPropertiesMigrationStatus getUserPropertiesMigrationStatus() {
    return _userPropertiesMigration.getUserPropertiesBulkMigrationStatus();
  }

  @Override
  public void deleteStaleUsers() {
    synchronized (_deleteStaleUsersLock) {
      if (_deleteStaleUsersTask != null && !_deleteStaleUsersTask.isDone()) {
        return;
      }
      Calendar c = Calendar.getInstance();
      c.add(Calendar.MONTH, -1);
      Date lastAccessTime = c.getTime();
      _deleteStaleUsersTask = _executors.submit(new DeleteStaleUsersTask(
          lastAccessTime));
    }
  }

  @Override
  public boolean isDeletingStaleUsers() {
    synchronized (_deleteStaleUsersLock) {
      return _deleteStaleUsersTask != null && !_deleteStaleUsersTask.isDone();
    }
  }

  @Override
  public void cancelDeleteStaleUsers() {
    synchronized (_deleteStaleUsersLock) {
      if (_deleteStaleUsersTask != null && !_deleteStaleUsersTask.isDone()) {
        _deleteStaleUsersTask.cancel(true);
        _deleteStaleUsersTask = null;
      }
    }
  }

  @Override
  @Transactional(readOnly=true)
  public long getNumberOfStaleUsers() {
    Calendar c = Calendar.getInstance();
    c.add(Calendar.MONTH, -1);
    Date lastAccessTime = c.getTime();
    return _userDao.getNumberOfStaleUsers(lastAccessTime);
  }

  /****
   * Private Methods
   ****/

  private void mergeRoles(User sourceUser, User targetUser) {
    Set<UserRole> roles = new HashSet<UserRole>();
    roles.addAll(sourceUser.getRoles());
    roles.addAll(targetUser.getRoles());

    UserRole anon = _authoritiesService.getAnonymousRole();
    UserRole user = _authoritiesService.getUserRole();
    if (roles.contains(user))
      roles.remove(anon);

    targetUser.setRoles(roles);
  }

  @Cacheable
  @Transactional
  @Override
  public Long getMinApiRequestIntervalForKey(String key,
      @CacheableArgument(cacheRefreshIndicator = true) boolean forceRefresh) {

    UserIndexKey indexKey = new UserIndexKey(UserIndexTypes.API_KEY, key);
    UserIndex userIndex = getUserIndexForId(indexKey);

    if (userIndex == null) {
      return null;
    }

    User user = userIndex.getUser();
    UserBean bean = getUserAsBean(user);
    return bean.getMinApiRequestInterval();
  }

  /**
   * Unfortunately, deleting a user is a somewhat complex operation, so we can
   * do it in bulk (TODO: maybe someone can figure out a clever cascading bulk
   * delete that plays well with all the caches / etc).
   * 
   * @param lastAccessTime
   */
  @Transactional
  public void deleteStaleUsers(Date lastAccessTime) {

    while (true) {
      List<Integer> userIds = _userDao.getStaleUserIdsInRange(lastAccessTime,
          0, 100);

      if (userIds.isEmpty()) {
        return;
      }

      for (int userId : userIds) {
        if (Thread.interrupted() ) {
          return;
        }
        User user = _userDao.getUserForId(userId);
        if (user != null) {
          _userDao.deleteUser(user);
        }
        Thread.yield();
      }
    }
  }

  private class DeleteStaleUsersTask implements Runnable {

    private Date _lastAccessTime;

    public DeleteStaleUsersTask(Date lastAccessTime) {
      _lastAccessTime = lastAccessTime;
    }

    @Override
    public void run() {
      deleteStaleUsers(_lastAccessTime);
    }
  }
}
