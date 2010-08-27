package org.onebusaway.users.impl.internal;

import java.util.Date;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;
import net.sf.ehcache.Statistics;

import org.onebusaway.users.model.User;
import org.onebusaway.users.services.UserDao;
import org.onebusaway.users.services.internal.UserLastAccessTimeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UserLastAccessTimeServiceImpl implements UserLastAccessTimeService {

  private Logger _log = LoggerFactory.getLogger(UserLastAccessTimeServiceImpl.class);

  private UserDao _userDao;

  private Cache _cache;

  @Autowired
  public void setUserDao(UserDao userDao) {
    _userDao = userDao;
  }

  public void setCache(Cache cache) {
    _cache = cache;
  }

  public long getNumberOfActiveUsers() {
    Statistics stats = _cache.getStatistics();
    return stats.getObjectCount();
  }

  public void handleAccessForUser(int userId, long accessTime) {
    Element element = _cache.get(userId);
    if (element == null) {
      User user = _userDao.getUserForId(userId);
      user.setLastAccessTime(new Date(accessTime));
      _userDao.saveOrUpdateUser(user);
      if (_log.isDebugEnabled())
        _log.debug("user last access set " + userId);
      element = new Element(userId, accessTime);
      _cache.put(element);
    }
  }
}
