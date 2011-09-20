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
package org.onebusaway.users.model;

import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.onebusaway.users.services.UserIndexTypes;
import org.onebusaway.users.services.UserService;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * A database-serialized linkage between some type-value id index (see
 * {@link UserIndexKey}) and a {@link User} account. Each {@link UserIndex}
 * refers to just one {@link User} account but each user account potentially has
 * multiple user indices.
 * 
 * Each user index also has an type-specific credentials value that can be used
 * to authenticate a user using the specified user index. See
 * {@link UserIndexTypes} for a set of common user index types used in the
 * system.
 * 
 * See discussion in the document for the {@link User} class.
 * 
 * @author bdferris
 * @see UserIndexKey
 * @see User
 * @see UserService
 * @see UserIndexTypes
 */
@Entity
@Table(name = "oba_user_indices")
@AccessType("field")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class UserIndex implements Serializable {

  private static final long serialVersionUID = 1L;

  @Id
  private UserIndexKey id;

  /**
   * Credentials encapsulate some secret specific to a UserIndex. For most of
   * the indices, it's just a randomly generated string that is generated when a
   * UserIndex is created so that it can be used in RememberMe cookie generation
   * to keep you from being able to generate a RememberMe cookie using known
   * information. However, for a username + password situation, it could also be
   * the password of some form.
   */
  private String credentials;

  @ManyToOne(fetch = FetchType.EAGER)
  private User user;

  public UserIndexKey getId() {
    return id;
  }

  public void setId(UserIndexKey id) {
    this.id = id;
  }

  public String getCredentials() {
    return credentials;
  }

  public void setCredentials(String credentials) {
    this.credentials = credentials;
  }

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
  }

  /****
   * {@link Object} Interface
   ****/

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    UserIndex other = (UserIndex) obj;
    if (id == null) {
      if (other.id != null)
        return false;
    } else if (!id.equals(other.id))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "UserIndex(id=" + id + ")";
  }

}
