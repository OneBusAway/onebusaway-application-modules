package org.onebusaway.presentation.services;

import org.onebusaway.users.client.model.UserBean;

public interface CurrentUserAware {
  public void setCurrentUser(UserBean currentUser);
}
