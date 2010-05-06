package org.onebusaway.users.model;

import org.hibernate.annotations.AccessType;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "oba_user_indices")
@AccessType("field")
public class UserIndex implements Serializable {

  private static final long serialVersionUID = 1L;

  @Id
  private UserIndexKey id;

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
}
