package org.onebusaway.users.impl.authentication;

import org.onebusaway.users.model.IndexedUserDetails;
import org.springframework.security.providers.dao.SaltSource;
import org.springframework.security.userdetails.UserDetails;

public class IndexedUserDetailsSaltSourceImpl implements SaltSource {

  @Override
  public Object getSalt(UserDetails user) {
    if (user instanceof IndexedUserDetails) {
      IndexedUserDetails details = (IndexedUserDetails) user;
      return details.getUserIndexKey().getValue();
    }
    return user.getUsername();
  }
}
