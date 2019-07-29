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

import org.onebusaway.users.model.IndexedUserDetails;
import org.onebusaway.users.model.User;
import org.onebusaway.users.model.UserIndex;
import org.onebusaway.users.model.UserIndexKey;
import org.onebusaway.users.model.UserRole;
import org.onebusaway.users.services.StandardAuthoritiesService;

import org.springframework.security.core.GrantedAuthority;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public class IndexedUserDetailsImpl extends
    org.springframework.security.core.userdetails.User implements IndexedUserDetails {

  private static final long serialVersionUID = 2L;

  private UserIndexKey _userIndexKey;

  public IndexedUserDetailsImpl(StandardAuthoritiesService authoritiesService,
      UserIndex userIndex) {
    super(userIndex.getId().toString(), userIndex.getCredentials(), true, true,
        true, true, getGrantedAuthoritiesForUser(authoritiesService,
            userIndex.getUser()));

    _userIndexKey = userIndex.getId();
  }

  public UserIndexKey getUserIndexKey() {
    return _userIndexKey;
  }

  public boolean isAnonymous() {
    return hasAuthority(StandardAuthoritiesService.ANONYMOUS);
  }

  public boolean isAdmin() {
    return hasAuthority(StandardAuthoritiesService.ADMINISTRATOR);
  }
  
  public boolean isReporting() {
	return hasAuthority(StandardAuthoritiesService.REPORTING);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result
        + ((_userIndexKey == null) ? 0 : _userIndexKey.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (!super.equals(obj))
      return false;
    if (getClass() != obj.getClass())
      return false;
    IndexedUserDetailsImpl other = (IndexedUserDetailsImpl) obj;
    if (_userIndexKey == null) {
      if (other._userIndexKey != null)
        return false;
    } else if (!_userIndexKey.equals(other._userIndexKey))
      return false;
    return true;
  }

  private boolean hasAuthority(String authorityToCheck) {
    Collection<GrantedAuthority> authorities = getAuthorities();
    for (GrantedAuthority authority : authorities) {
      if (authority.getAuthority().equals(authorityToCheck))
        return true;
    }
    return false;
  }

  private static Collection<? extends GrantedAuthority> getGrantedAuthoritiesForUser(
      StandardAuthoritiesService authoritiesService, User user) {
    Set<UserRole> roles = user.getRoles();
    List<GrantedAuthority> authorities = new ArrayList<>(roles.size());
    int index = 0;
    for (UserRole role : roles)
      authorities.add(index++, authoritiesService.getNameBasedAuthority(role.getName()));
    return authorities;
  }
}
