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

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.onebusaway.container.model.IdentityBean;
import org.onebusaway.users.services.UserDao;
import org.onebusaway.users.services.UserPropertiesMigration;
import org.onebusaway.users.services.UserPropertiesService;

/**
 * Main user account object, designed to be serialized to a database.
 * 
 * A couple of design notes:
 * 
 * <h2>User Properties</h2>
 * 
 * The only properites that are included in the User object itself are those
 * that need be accessible from SQL/HQL queries. For the most part, there aren't
 * many of those. The bulk of user properties and data is stored in the
 * {@link UserProperties} object attached to each user. This is just a Java bean
 * that is serialized to and from the database for each user.
 * 
 * Why the serialized bean for user properties? This approach gives us more
 * flexibility as user properties change over time, as driven by new
 * functionality. The addition, modification, or deletion of a property can get
 * quite complex when it means changing the underlying database schema,
 * especially as the user table grows larger and you consider the need to
 * upgrade production systems on the fly.
 * 
 * Instead, the serialized bean approach allows us to lazily upgrade beans as
 * they are accessed. We rely on handlers registered with the
 * {@link UserPropertiesService} to manage versioning of {@link UserProperties}
 * implementations and migration between them.
 * 
 * <h2>User Indices</h2>
 * 
 * Each user has a numeric id that uniquely identifies the user account.
 * However, we often want to lookup and authenticate a user by a variety of
 * other indices: username, phone number, web cookie, OpenID id, Twitter
 * account, email, Facebook account. To support all those mechanisms in a
 * flexible way, each {@link User} object has a set of {@link UserIndex} objects
 * associated with it (see {@link #getUserIndices()}). Each user index has a
 * type (ex. username) and value (ex. admin), plus some credential information
 * that can be used for authenticating a user through that user index.
 * 
 * 
 * @author bdferris
 * @see UserIndex
 * @see UserRole
 * @see UserProperties
 * @see UserDao
 * @see UserService
 * @see UserPropertiesService
 * @see UserPropertiesMigration
 */
@Entity
@Table(name = "oba_users")
@AccessType("field")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class User extends IdentityBean<Integer> {

  private static final long serialVersionUID = 1L;

  @Id
  @GeneratedValue
  @AccessType("property")
  private Integer id;

  private Date creationTime;

  private Date lastAccessTime;

  @Column(columnDefinition = "BIT", length = 1)
  private boolean temporary;

  @Lob
  private UserProperties properties;

  @ManyToMany(fetch = FetchType.EAGER)
  @JoinTable(name = "oba_user_roles_mapping", joinColumns = @JoinColumn(name = "user_id"))
  @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
  private Set<UserRole> roles = new HashSet<UserRole>();

  @OneToMany(cascade = {CascadeType.ALL}, mappedBy = "user", fetch = FetchType.EAGER)
  @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
  private Set<UserIndex> userIndices = new HashSet<UserIndex>();

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public Date getCreationTime() {
    return creationTime;
  }

  public void setCreationTime(Date creationTime) {
    this.creationTime = creationTime;
  }

  public Date getLastAccessTime() {
    return lastAccessTime;
  }

  public void setLastAccessTime(Date lastAccessTime) {
    this.lastAccessTime = lastAccessTime;
  }

  public boolean isTemporary() {
    return temporary;
  }

  public void setTemporary(boolean temporary) {
    this.temporary = temporary;
  }

  public UserProperties getProperties() {
    return properties;
  }

  public void setProperties(UserProperties properties) {
    this.properties = properties;
  }

  public Set<UserRole> getRoles() {
    return roles;
  }

  public void setRoles(Set<UserRole> roles) {
    this.roles = roles;
  }

  public Set<UserIndex> getUserIndices() {
    return userIndices;
  }

  public void setUserIndices(Set<UserIndex> userIndices) {
    this.userIndices = userIndices;
  }

  @Override
  public String toString() {
    return "User(id=" + id + ")";
  }
}
