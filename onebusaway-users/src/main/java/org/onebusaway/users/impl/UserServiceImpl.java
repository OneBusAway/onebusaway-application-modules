package org.onebusaway.users.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.onebusaway.users.client.model.UserBean;
import org.onebusaway.users.client.model.UserIndexBean;
import org.onebusaway.users.model.User;
import org.onebusaway.users.model.UserIndex;
import org.onebusaway.users.model.UserIndexKey;
import org.onebusaway.users.model.UserPropertiesV1;
import org.onebusaway.users.model.UserRole;
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
import org.springframework.stereotype.Component;

@Component
public class UserServiceImpl implements UserService {

  private UserDao _userDao;

  private StandardAuthoritiesService _authoritiesService;

  private UserPropertiesMigration _userPropertiesMigration;

  private UserPropertiesService _userPropertiesService;

  private UserIndexRegistrationService _userIndexRegistrationService;

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

  /****
   * {@link UserService} Interface
   ****/

  @Override
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
  public void resetUser(User user) {
    user.setProperties(_userPropertiesService.createDefaultProperties());
    _userDao.saveOrUpdateUser(user);
  }

  @Override
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

  @Override
  public UserIndex getOrCreateUserForIndexKey(UserIndexKey key,
      String credentials, boolean isAnonymous) {

    UserIndex userIndex = _userDao.getUserIndexForId(key);

    if (userIndex == null) {

      User user = new User();
      user.setCreationTime(new Date());
      user.setTemporary(true);
      user.setProperties(new UserPropertiesV1());
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
  public UserIndex getUserIndexForId(UserIndexKey key) {
    return _userDao.getUserIndexForId(key);
  }

  @Override
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
  public void mergeUsers(User sourceUser, User targetUser) {

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
  public UserIndex completePhoneNumberRegistration(UserIndex userIndex,
      String registrationCode) {

    UserRegistration registration = _userIndexRegistrationService.getRegistrationForUserIndexKey(userIndex.getId());
    if (registration == null)
      return null;

    String expectedCode = registration.getRegistrationCode();
    if (!expectedCode.equals(registrationCode))
      return null;

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
  public void startUserPropertiesMigration() {
    _userPropertiesMigration.startUserPropertiesBulkMigration(_userPropertiesService.getUserPropertiesType());
  }

  @Override
  public UserPropertiesMigrationStatus getUserPropertiesMigrationStatus() {
    return _userPropertiesMigration.getUserPropertiesBulkMigrationStatus();
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
}
