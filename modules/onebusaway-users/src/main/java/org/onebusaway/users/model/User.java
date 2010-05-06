package org.onebusaway.users.model;

import org.onebusaway.container.model.IdentityBean;

import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import java.util.Date;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

@Entity
@Table(name = "oba_users")
@AccessType("field")
public class User extends IdentityBean<Integer> {

  private static final long serialVersionUID = 1L;

  @Id
  @GeneratedValue
  @AccessType("property")
  private Integer id;

  private Date creationTime;

  private boolean temporary;

  private UserPropertiesV1 properties;

  @ManyToMany(fetch = FetchType.EAGER)
  @JoinTable(name = "oba_user_roles_mapping", joinColumns = @JoinColumn(name = "user_id"))
  @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
  private Set<UserRole> roles;

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

  public boolean isTemporary() {
    return temporary;
  }

  public void setTemporary(boolean temporary) {
    this.temporary = temporary;
  }

  public UserPropertiesV1 getProperties() {
    return properties;
  }

  public void setProperties(UserPropertiesV1 properties) {
    this.properties = properties;
  }

  public Set<UserRole> getRoles() {
    return roles;
  }

  public void setRoles(Set<UserRole> roles) {
    this.roles = roles;
  }
}
