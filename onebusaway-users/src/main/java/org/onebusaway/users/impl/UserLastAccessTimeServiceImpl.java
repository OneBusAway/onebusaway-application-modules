package org.onebusaway.users.impl;

import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.onebusaway.users.model.User;
import org.onebusaway.users.services.UserDao;
import org.springframework.beans.factory.annotation.Autowired;

public class UserLastAccessTimeServiceImpl {

  private Map<Integer, Long> _lastAccessByUserId = new ConcurrentHashMap<Integer, Long>();

  private UserDao _userDao;

  private ScheduledFuture<?> _task;

  private long _evictionThreshold = 15 * 60 * 1000;

  private long _evictionFrequency = 5 * 60 * 1000;

  @Autowired
  public void setUserDao(UserDao userDao) {
    _userDao = userDao;
  }

  public void setEvictionThreshold(long evictionThresholdInMilliseconds) {
    _evictionThreshold = evictionThresholdInMilliseconds;
  }

  public void setEvictionFrequency(long evictionFrequencyInMilliseconds) {
    _evictionFrequency = evictionFrequencyInMilliseconds;
  }

  public int getNumberOfActiveUsers() {
    return _lastAccessByUserId.size();
  }
  
  @PostConstruct
  public void start() {
    ScheduledExecutorService _executor = Executors.newSingleThreadScheduledExecutor();
    _task = _executor.scheduleAtFixedRate(new EvictionRunner(),
        _evictionFrequency, _evictionFrequency, TimeUnit.MILLISECONDS);
  }

  @PreDestroy
  public void stop() {
    _task.cancel(true);
  }

  public void handleAccessForUser(int userId, long accessTime) {
    Long existing = _lastAccessByUserId.put(userId, accessTime);
    // User did not already exist in the DB
    if (existing == null) {
      User user = _userDao.getUserForId(userId);
      user.setLastAccessTime(new Date(accessTime));
      _userDao.saveOrUpdateUser(user);
    }
  }

  private class EvictionRunner implements Runnable {

    @Override
    public void run() {
      long now = System.currentTimeMillis();
      for (Iterator<Map.Entry<Integer, Long>> it = _lastAccessByUserId.entrySet().iterator(); it.hasNext();) {
        Entry<Integer, Long> entry = it.next();
        if (entry.getValue() + _evictionThreshold < now)
          it.remove();
        if (Thread.interrupted())
          return;
      }
    }
  }

}
