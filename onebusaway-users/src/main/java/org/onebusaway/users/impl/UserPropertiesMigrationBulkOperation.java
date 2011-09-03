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

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

import org.onebusaway.users.model.User;
import org.onebusaway.users.model.UserProperties;
import org.onebusaway.users.services.UserDao;
import org.onebusaway.users.services.UserPropertiesMigration;
import org.onebusaway.users.services.UserPropertiesMigrationStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserPropertiesMigrationBulkOperation<T extends UserProperties> {

  private static final int FETCH_LIMIT = 100;

  private static Logger _log = LoggerFactory.getLogger(UserPropertiesMigrationBulkOperation.class);

  private ScheduledExecutorService _executor = Executors.newSingleThreadScheduledExecutor();

  private UserPropertiesMigration _propertiesMigration;

  private Class<T> _target;

  private UserDao _dao;

  private int _numberOfUsers;

  private AtomicInteger _numberOfUsersProcessed = new AtomicInteger();

  private AtomicInteger _numberOfUsersConverted = new AtomicInteger();

  private boolean _canceled = false;

  public static <T extends UserProperties> UserPropertiesMigrationBulkOperation<T> execute(
      UserDao dao, UserPropertiesMigration propertiesMigration, Class<T> target) {
    UserPropertiesMigrationBulkOperation<T> op = new UserPropertiesMigrationBulkOperation<T>(
        dao, propertiesMigration, target);
    op.run();
    return op;
  }

  private UserPropertiesMigrationBulkOperation(UserDao dao,
      UserPropertiesMigration propertiesMigration, Class<T> target) {
    _dao = dao;
    _propertiesMigration = propertiesMigration;
    _target = target;
  }

  public void run() {
    _numberOfUsers = _dao.getNumberOfUsers();
    for (int i = 0; i < _numberOfUsers; i += FETCH_LIMIT)
      _executor.submit(new Go(i));
  }

  public synchronized void cancel() {
    _canceled = true;
    _executor.shutdownNow();
  }

  public UserPropertiesMigrationStatus getStatus() {
    UserPropertiesMigrationStatus status = new UserPropertiesMigrationStatus();
    status.setCanceled(isCanceled());
    status.setComplete(isComplete());
    status.setNumberOfUsers(getNumberOfUsers());
    status.setNumberOfUsersConverted(getNumberOfUsersConverted());
    status.setNumberOfUsersProcessed(getNumberOfUsersProcessed());
    return status;
  }

  public int getNumberOfUsers() {
    return _numberOfUsers;
  }

  public int getNumberOfUsersConverted() {
    return _numberOfUsersConverted.get();
  }

  public int getNumberOfUsersProcessed() {
    return _numberOfUsersProcessed.get();
  }

  public synchronized boolean isCanceled() {
    return _canceled;
  }

  public boolean isComplete() {
    return getNumberOfUsers() <= getNumberOfUsersProcessed();
  }

  /****
   * Private Methods
   ****/

  private void updateStatistics(int usersProcessed, int usersConverted) {
    _numberOfUsersProcessed.addAndGet(usersProcessed);
    _numberOfUsersConverted.addAndGet(usersConverted);
  }

  private class Go implements Runnable {

    private int _offset;

    public Go(int offset) {
      _offset = offset;
    }

    @Override
    public void run() {

      _log.info("offset=" + _offset);

      int usersProcessed = 0;
      int usersConverted = 0;

      try {
        List<Integer> userIds = _dao.getAllUserIdsInRange(_offset, FETCH_LIMIT);

        for (int userId : userIds) {
          User user = _dao.getUserForId(userId);
          _log.info("processing user: id=" + user.getId());
          if (_propertiesMigration.needsMigration(user.getProperties(), _target)) {
            _log.info("migrating user: id=" + user.getId());
            UserProperties properties = _propertiesMigration.migrate(
                user.getProperties(), _target);
            user.setProperties(properties);
            _dao.saveOrUpdateUser(user);
            usersConverted++;
          }
          usersProcessed++;
        }

      } catch (Throwable ex) {
        _log.warn("error processing users for verion migration", ex);
        usersProcessed = Math.min(FETCH_LIMIT, _numberOfUsers - _offset);
      }

      updateStatistics(usersProcessed, usersConverted);
    }
  }

}
