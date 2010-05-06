package org.onebusaway.webapp.gwt.where_library;

import org.onebusaway.users.client.model.UserBean;
import org.onebusaway.webapp.gwt.where_library.rpc.WebappServiceAsync;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class UserContext {

  private UserBean _user = null;

  private boolean _userLoaded = false;

  public static UserContext create() {
    UserContext context = new UserContext();
    context.getCurrentUser(null);
    return context;
  }

  protected UserContext() {

  }

  public void getCurrentUser(AsyncCallback<UserBean> callback) {
    if (_userLoaded)
      callback.onSuccess(_user);
    else
      WebappServiceAsync.SERVICE.getCurrentUser(new UserHandler(callback));
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
