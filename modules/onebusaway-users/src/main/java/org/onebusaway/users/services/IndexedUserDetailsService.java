package org.onebusaway.users.services;

import org.onebusaway.users.model.IndexedUserDetails;
import org.onebusaway.users.model.UserIndexKey;

import org.springframework.dao.DataAccessException;
import org.springframework.security.userdetails.UsernameNotFoundException;

public interface IndexedUserDetailsService {

  public IndexedUserDetails getUserForIndexKey(UserIndexKey key)
      throws UsernameNotFoundException, DataAccessException;

  public IndexedUserDetails getOrCreateUserForIndexKey(UserIndexKey key,
      String credentials) throws DataAccessException;
}
