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
package org.onebusaway.webapp.gwt.where_library;

import org.onebusaway.users.client.model.UserBean;
import org.onebusaway.webapp.gwt.where_library.rpc.WebappServiceAsync;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class UserContext {

  private static WebappServiceAsync _service = WebappServiceAsync.SERVICE;

  private static UserContext _instance;

  private UserBean _user = null;

  private boolean _userLoaded = false;

  public static UserContext getContext() {
    if (_instance == null) {
      _instance = new UserContext();
      _instance.getCurrentUser(null);
    }
    return _instance;
  }

  protected UserContext() {

  }

  public void getCurrentUser(AsyncCallback<UserBean> callback) {
    if (_userLoaded)
      callback.onSuccess(_user);
    else
      WebappServiceAsync.SERVICE.getCurrentUser(new UserHandler(callback));
  }

  public void setDefaultSearchLocationForCurrentUser(String locationName,
      double lat, double lon, boolean onlyIfNotAlreadySet) {
    setDefaultSearchLocationForCurrentUser(locationName, lat, lon,
        onlyIfNotAlreadySet, null);
  }

  public void setDefaultSearchLocationForCurrentUser(final String locationName,
      final double lat, final double lon, final boolean onlyIfNotAlreadySet,
      final AsyncCallback<UserBean> callback) {

    getCurrentUser(new AsyncCallback<UserBean>() {

      @Override
      public void onSuccess(UserBean user) {
        if (onlyIfNotAlreadySet && user.hasDefaultLocation())
          return;
        _service.setDefaultLocationForUser(locationName, lat, lon,
            new UserHandler(callback));
      }

      @Override
      public void onFailure(Throwable ex) {
        if (callback != null)
          callback.onFailure(ex);
      }
    });
  }

  public void clearDefaultSearchLocationForCurrentUser(
      AsyncCallback<UserBean> callback) {
    WebappServiceAsync.SERVICE.clearDefaultLocationForUser(new UserHandler(callback));
  }

  private class UserHandler implements AsyncCallback<UserBean> {

    private AsyncCallback<UserBean> _callback;

    public UserHandler(AsyncCallback<UserBean> callback) {
      _callback = callback;
    }

    public void onSuccess(UserBean user) {
      _user = user;
      _userLoaded = true;
      if (_callback != null)
        _callback.onSuccess(_user);
    }

    public void onFailure(Throwable arg0) {
      _user = null;
      _userLoaded = true;
      if (_callback != null)
        _callback.onSuccess(_user);
    }
  }

}
