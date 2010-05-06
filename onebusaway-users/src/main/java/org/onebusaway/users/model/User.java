package org.onebusaway.users.model;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;
import org.onebusaway.container.model.IdentityBean;
import org.onebusaway.users.impl.hibernate.UserPropertiesUserType;

@Entity
@Table(name = "oba_users")
@AccessType("field")
@TypeDefs( {@TypeDef(name = "userProperties", typeClass = UserPropertiesUserType.class)})
public class User extends IdentityBean<Integer> {

  private static final long serialVersionUID = 1L;

  @Id
  @GeneratedValue
  @AccessType("property")
  private Integer id;

  private Date creationTime;
  
  private Date lastAccessTime;

  private boolean temporary;

  @Type(type = "userProperties")
  private UserProperties properties;

  @ManyToMany(fetch = FetchType.EAGER)
  @JoinTable(name = "oba_user_roles_mapping", joinColumns = @JoinColumn(name = "user_id"))
  @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
  private Set<UserRole> roles = new HashSet<UserRole>();

  @OneToMany(cascade = {CascadeType.REMOVE}, mappedBy = "user", fetch = FetchType.EAGER)
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

  public void addUserIndex(UserIndex index) {
    if (index.getUser() != null && !index.getUser().equals(this))
      throw new IllegalArgumentException(
          "attempt to add already assigned UserIndex to new User");
    index.setUser(this);
    userIndices.add(index);
  }

  @Override
  public String toString() {
    return "User(id=" + id + ")";
  }
}
