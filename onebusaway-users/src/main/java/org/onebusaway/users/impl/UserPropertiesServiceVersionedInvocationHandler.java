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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicInteger;

import org.onebusaway.users.model.User;
import org.onebusaway.users.model.UserIndex;
import org.onebusaway.users.model.UserProperties;
import org.onebusaway.users.model.UserPropertiesV1;
import org.onebusaway.users.model.properties.UserPropertiesV2;
import org.onebusaway.users.model.properties.UserPropertiesV3;
import org.onebusaway.users.model.properties.UserPropertiesV4;
import org.onebusaway.users.services.UserPropertiesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;

@ManagedResource("org.onebusaway.users.impl:name=UserPropertiesServiceVersionedInvocationHandler")
public class UserPropertiesServiceVersionedInvocationHandler implements InvocationHandler {

  private static Logger _log = LoggerFactory.getLogger(UserPropertiesServiceVersionedInvocationHandler.class);

  private UserPropertiesService _userServiceV1;

  private UserPropertiesService _userServiceV2;

  private UserPropertiesService _userServiceV3;

  private UserPropertiesService _userServiceV4;

  private int _preferredVersion = -1;

  private AtomicInteger _v1References = new AtomicInteger();

  private AtomicInteger _v2References = new AtomicInteger();

  private AtomicInteger _v3References = new AtomicInteger();

  private AtomicInteger _v4References = new AtomicInteger();

  public void setUserPropertiesServiceV1(UserPropertiesService userServiceV1) {
    _userServiceV1 = userServiceV1;
  }

  public void setUserPropertiesServiceV2(UserPropertiesService userServiceV2) {
    _userServiceV2 = userServiceV2;
  }

  public void setUserPropertiesServiceV3(UserPropertiesService userServiceV3) {
    _userServiceV3 = userServiceV3;
  }

  public void setUserPropertiesServiceV4(UserPropertiesService userServiceV4) {
      _userServiceV4 = userServiceV4;
  }

  public void setPreferredVersion(int preferredVersion) {
    _preferredVersion = preferredVersion;
  }

  @ManagedAttribute
  public int getV1References() {
    return _v1References.get();
  }

  @ManagedAttribute
  public int getV2References() {
    return _v2References.get();
  }

  @ManagedAttribute
  public int getV3References() {
    return _v3References.get();
  }

  @ManagedAttribute
  public int getV4References() {
    return _v4References.get();
}

  @Override
  public Object invoke(Object proxy, Method method, Object[] args)
      throws Throwable {
    UserPropertiesService service = getServiceForUserArgs(args);
    return method.invoke(service, args);
  }

  private UserPropertiesService getServiceForUserArgs(Object[] args) {

    if (_preferredVersion != -1)
      return getServiceForVersion(_preferredVersion);

    int maxVersion = 0;
    if (args != null) {
      for (Object arg : args) {
        if (arg instanceof User) {
          User user = (User) arg;
          int version = getPropertiesVersion(user);
          maxVersion = Math.max(maxVersion, version);
        } else if (arg instanceof UserIndex) {
          UserIndex userIndex = (UserIndex) arg;
          int version = getPropertiesVersion(userIndex.getUser());
          maxVersion = Math.max(maxVersion, version);
        }
      }
    }

    return getServiceForVersion(maxVersion);
  }

  private int getPropertiesVersion(User user) {
    UserProperties props = user.getProperties();
    if (props instanceof UserPropertiesV1)
      return 1;
    if (props instanceof UserPropertiesV2)
      return 2;
    if (props instanceof UserPropertiesV3)
      return 3;
    if (props instanceof UserPropertiesV4)
        return 4;

    _log.warn("unknown user properties version: " + props.getClass());
    return 0;
  }

  private UserPropertiesService getServiceForVersion(int maxVersion) {
    switch (maxVersion) {
      case 1:
      default:
        _v1References.incrementAndGet();
        return _userServiceV1;
      case 2:
        _v2References.incrementAndGet();
        return _userServiceV2;
      case 3:
        _v3References.incrementAndGet();
        return _userServiceV3;
      case 4:
        _v4References.incrementAndGet();
        return _userServiceV4;
    }
  }

}
