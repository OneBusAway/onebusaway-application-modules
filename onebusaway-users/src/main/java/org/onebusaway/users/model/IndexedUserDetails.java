package org.onebusaway.users.model;

import org.springframework.security.userdetails.UserDetails;

public interface IndexedUserDetails extends UserDetails {
  public UserIndexKey getUserIndexKey();
  public boolean isAnonymous();
  public boolean isAdmin();
}
