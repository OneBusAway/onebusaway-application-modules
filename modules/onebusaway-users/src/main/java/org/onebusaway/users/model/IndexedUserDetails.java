package org.onebusaway.users.model;

import org.springframework.security.userdetails.UserDetails;

public interface IndexedUserDetails extends UserDetails {
  public UserIndex getUserIndex();
}
