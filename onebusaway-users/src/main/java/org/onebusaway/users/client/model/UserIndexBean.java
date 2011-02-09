package org.onebusaway.users.client.model;

import java.io.Serializable;

public final class UserIndexBean implements Serializable, Comparable<UserIndexBean> {

  private static final long serialVersionUID = 1L;

  private String type;

  private String value;

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  @Override
  public int compareTo(UserIndexBean o) {
    int c = type.compareTo(o.type);
    if( c == 0 )
      c = value.compareTo(o.value);
    return c;
  }
}
