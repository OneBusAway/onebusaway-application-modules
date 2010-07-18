package org.onebusaway.users.impl;

import org.onebusaway.users.model.IndexedUserDetails;
import org.onebusaway.users.model.User;
import org.onebusaway.users.model.UserIndex;

public interface CurrentUserStrategy {

  /**
   * 
   * @param createUserIfAppropriate
   * @return
   */
  public User getCurrentUser(boolean createUserIfAppropriate);

  /**
   * 
   * @param createUserIfAppropriate
   * @return
   */
  public UserIndex getCurrentUserIndex(boolean createUserIfAppropriate);

  /**
   * 
   * @param createUserIfAppropriate
   * @return
   */
  public IndexedUserDetails getCurrentUserDetails(
      boolean createUserIfAppropriate);

  public void setCurrentUser(UserIndex userIndex);

  public void clearCurrentUser();
}
